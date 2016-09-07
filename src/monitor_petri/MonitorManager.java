package monitor_petri;

import java.io.IOException;
import java.util.HashMap;
import java.util.concurrent.Semaphore;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import Petri.PetriNet;
import Petri.Transition;
import rx.Observer;
import rx.Subscription;
import rx.subjects.PublishSubject;

public class MonitorManager {

	/** Petri Net to command the monitor orchestration */
	private PetriNet petri;
	/** Mutex for the monitor access with a FIFO queue associated*/
	private Semaphore inQueue = new Semaphore(1,true);
	/** Condition variable queues where locked threads will wait */
	private VarCondQueue[] condVarQueue;	
	/** The policy to be used for transitions management. This will decide which transition
	 * should be fired when there are multiple available */
	private TransitionsPolicy transitionsPolicy;
	/** A PublishSubject who sends events for informed transitions
	 * Observers have to explicitly subscribe to an informed transition's events.
	 * @see #subscribeToTransition(Transition, Observer)*/
	private PublishSubject<String> informedTransitionsObservable;
	
	/** An ObjectMapper used to build and parse JSON info sent as events */
	private ObjectMapper jsonMapper;
	
	private final static String ID = "id";
	private final static String INDEX = "index";

	public MonitorManager(final PetriNet _petri, TransitionsPolicy _policy) {
		if(_petri == null || _policy == null){
			throw new IllegalArgumentException(this.getClass().getName() + " constructor. Invalid arguments");
		}
		petri = _petri;
		transitionsPolicy = _policy;
		
		int transitionsAmount = petri.getTransitions().length;
		condVarQueue = new FairQueue[transitionsAmount];
		informedTransitionsObservable = PublishSubject.create();
		final boolean automaticTransitions[] = petri.getAutomaticTransitions();
		for(int i = 0; i < automaticTransitions.length; i++){
			// Only non-automatic transitions have an associated queue
			// since no thread will try to fire an automatic transition
			// and thus will not sleep if fails
			if(!automaticTransitions[i]){
				condVarQueue[i] = new FairQueue();
			}
		}
		
		jsonMapper = new ObjectMapper();
	}

	/**
	 * Tries to fire a transition.
	 * <ul>
	 * <li>If fails, the calling thread sleeps in the corresponding VarCondQueue</li>
	 * <li>If succeeds, it checks if there are new enabled transitions.
	 * If not it leaves, else it tries to fire any new enabled transition.
	 * This may lead into two situations:</li>
	 * <ul>
	 * <li>The transition to fire is automatic, just fire it</li>
	 * <li>The transition to fire is not automatic but another thread was waiting to fire it.
	 * 		  In that case, wake up that thread and leave the monitor</li>
	 * </ul>
	 * </ul>
	 * @param transitionToFire the transition to fire
	 * @throws IllegalTransitionFiringError when an request to fire an automatic transition arrives
	 * @see PetriNet#fire(Transition)
	 */
	public void fireTransition(Transition transitionToFire) throws IllegalTransitionFiringError{
		// An attempt to fire an automatic transition is a severe error and the application should stop automatically
		if(transitionToFire.getLabel().isAutomatic()){
			throw new IllegalTransitionFiringError("An automatic transition has tried to be fired manually");
		}
		int permitsToRelease = 1;
		try {
			// take the mutex to access the monitor
			inQueue.acquire();
			long timeToFire = System.currentTimeMillis();
			boolean keepFiring = true;
			boolean window = false;
			boolean hasWindow = false;
			while(keepFiring){
				// keepFiring is "k" variable
				keepFiring = petri.isEnabled(transitionToFire);
				if(transitionToFire.getTimeSpan() != null){
					window = transitionToFire.getTimeSpan().inTimeSpan(timeToFire);
					hasWindow = true;
				}
				if(keepFiring){
					while(hasWindow && !window){						
						//I came before time span, and there is nobody sleeping in the transition
						if(transitionToFire.getTimeSpan().beforeTimeSpan(timeToFire) && !transitionToFire.getTimeSpan().anySleeping()){
							inQueue.release();
							transitionToFire.getTimeSpan().sleep(transitionToFire.getTimeSpan().getEnableTime() + transitionToFire.getTimeSpan().getTimeBegin() - timeToFire);
						}
						else{							
							// I came late, the time is over. Thus the thread releases the input mutex and goes to sleep
							inQueue.release();
							condVarQueue[transitionToFire.getIndex()].sleep();
						}
						inQueue.acquire();
						// at this point, the transition may have been disabled when the firing thread was sleeping
						timeToFire = System.currentTimeMillis();
						window = transitionToFire.getTimeSpan().inTimeSpan(timeToFire);
					}
					// TODO: check if the transition was fired sucessfully
					petri.fire(transitionToFire);
					
					//the transition was fired successfully. If it's informed let's send an event
					if(transitionToFire.getLabel().isInformed()){
						try{
							// The event to send contains a JSON with the transition info
							HashMap<String, String> firedTransitionInfoMap = new HashMap<String, String>();
							firedTransitionInfoMap.put(ID, transitionToFire.getId());
							firedTransitionInfoMap.put(INDEX, Integer.toString(transitionToFire.getIndex()));
							informedTransitionsObservable.onNext(
									jsonMapper.writeValueAsString(firedTransitionInfoMap));
						} catch (JsonProcessingException e) {
							// If there was an error processing the JSON let's send the minimal needed info hardcoded here
							informedTransitionsObservable.onNext("{\"" + ID + "\":\"" + transitionToFire.getId() + "\"}");
							e.printStackTrace();
						}
					}
					// let's see if any transition was enabled due to the last fired
					boolean enabledTransitions[] = petri.getEnabledTransitions();
					boolean queueHasThreadSleeping[] = getQueuesState(); //Is there anyone in the queue?
					boolean automaticTransitions[] = petri.getAutomaticTransitions();
					
					// availablesToFire is "m"
					boolean availablesToFire[] = new boolean[enabledTransitions.length];
					boolean anyAvailable = false;
					for(int i = 0; i < availablesToFire.length; i++){
						availablesToFire[i] = enabledTransitions[i] && (queueHasThreadSleeping[i] || automaticTransitions[i]);
						anyAvailable |= availablesToFire[i];
					}
					
					if (anyAvailable){
						int nextTransitionToFireIndex = transitionsPolicy.which(availablesToFire);
						if(nextTransitionToFireIndex >= 0){
							// it should never be the other way but just to be sure
							if(automaticTransitions[nextTransitionToFireIndex]){
								transitionToFire = petri.getTransitions()[nextTransitionToFireIndex];
							}
							else{
								// The transition chosen isn't automatic
								// so wake up the associated thread to that transition
								// and leave the monitor without releasing the input mutex (permits = 0)
								condVarQueue[nextTransitionToFireIndex].wakeUp();
								permitsToRelease = 0;
								return;
							}
						}
					}
					else{
						// no transition left to fire, leave the monitor
						keepFiring = false;
					}
				}
				else{
					// the fire failed, thus the thread releases the input mutex and goes to sleep
					inQueue.release();
					condVarQueue[transitionToFire.getIndex()].sleep();
					keepFiring = true;
				}
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		} finally{
			// the firing is done, release the mutex and leave
			inQueue.release(permitsToRelease);
		}
	}
	
	/**
	 * Fills and returns a vector of booleans containing 
	 * whether at least one thread is sleeping in the matching VarCondQueue
	 * @return a vector of boolean indicating if at least a thread is sleeping in each VarCondQueue
	 */
	public boolean[] getQueuesState() {
		boolean[] queuesNotEmpty = new boolean[condVarQueue.length];
		for(int i = 0; i < condVarQueue.length; i++){
			if(condVarQueue[i] == null){
				queuesNotEmpty[i] = false;
			}
			else{
				queuesNotEmpty[i] = !condVarQueue[i].isEmpty();
			}
		}
		return queuesNotEmpty;
	}

	/**
	 * Changes the transitions policy at runtime. If null just ignores the new policy and keeps the previous
	 * @param _transitionsPolicy the new policy to be set
	 */
	public synchronized void setTransitionsPolicy(TransitionsPolicy _transitionsPolicy){
		if(_transitionsPolicy != null){
			this.transitionsPolicy = _transitionsPolicy;
		}
	}
	
	/**
	 * Subscribe the given observer to the given transition events if it's informed
	 * @param _transition the transition to subscribe to
	 * @param _observer the observer to subscribe
	 * @throws IllegalArgumentException if the given transition is not informed
	 * @return a Subscription object used to unsubscribe
	 */
	public Subscription subscribeToTransition(Transition _transition, Observer<String> _observer) throws IllegalArgumentException{
		if(_transition == null || _observer == null){
			throw new IllegalArgumentException("invalid transition or observer recieved");
		} else if (!_transition.getLabel().isInformed()){
			throw new IllegalArgumentException("Transition " + _transition.getIndex() + " is not informed");
		}
		// the subscription is made only to the specified transition filtering by id
		return informedTransitionsObservable
				.filter((String jsonInfo) -> {
					try{
						// checks that the ID registered is the same as the one in the JSON
						return _transition.getId()
								.equals(jsonMapper.readTree(jsonInfo).get(ID).asText());
					} catch (IOException e) {
						return false;
					}
				})
				.subscribe(_observer);
	}
	
	public void setGuard(int i, boolean k){
	}
	
	public void fireGuard(int i, boolean to){
	}
}
