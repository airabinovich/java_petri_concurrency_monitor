package org.unc.lac.javapetriconcurrencymonitor.monitor;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

import org.unc.lac.javapetriconcurrencymonitor.errors.IllegalTransitionFiringError;
import org.unc.lac.javapetriconcurrencymonitor.exceptions.FiringAfterTimespanException;
import org.unc.lac.javapetriconcurrencymonitor.exceptions.FiringBeforeTimespanException;
import org.unc.lac.javapetriconcurrencymonitor.exceptions.NotInitializedPetriNetException;
import org.unc.lac.javapetriconcurrencymonitor.exceptions.PetriNetException;
import org.unc.lac.javapetriconcurrencymonitor.monitor.policies.TransitionsPolicy;
import org.unc.lac.javapetriconcurrencymonitor.petrinets.PetriNet;
import org.unc.lac.javapetriconcurrencymonitor.petrinets.components.Transition;
import org.unc.lac.javapetriconcurrencymonitor.queues.FairQueue;
import org.unc.lac.javapetriconcurrencymonitor.queues.VarCondQueue;
import org.unc.lac.javapetriconcurrencymonitor.utils.PriorityBinaryLock;
import org.unc.lac.javapetriconcurrencymonitor.utils.PriorityBinaryLock.LockPriority;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import rx.Observer;
import rx.Subscription;
import rx.subjects.PublishSubject;

public class PetriMonitor {

	/** Petri Net to command the monitor orchestration */
	private PetriNet petri;
	/** Mutex for the monitor access with a FIFO queue associated*/
	private PriorityBinaryLock inQueue = new PriorityBinaryLock();
	/** Condition variable queues where locked threads will wait */
	private VarCondQueue[] condVarQueue;	
	/** The policy to be used for transitions management. This will decide which transition
	 * should be fired when there are multiple available */
	private TransitionsPolicy transitionsPolicy;
	/** A PublishSubject who sends events for informed transitions
	 * Observers have to explicitly subscribe to an informed transition's events.
	 * @see #subscribeToTransition(Transition, Observer)*/
	private PublishSubject<String> informedTransitionsObservable;
	
	/** Contains true in the nth position if a thread is waiting for the nth transition's time span to occur **/
	private AtomicBoolean[] anyThreadSleepingforTransition;
	
	/** An ObjectMapper used to build and parse JSON info sent as events */
	private ObjectMapper jsonMapper;
	
	private final static String ID = "id";
	private final static String INDEX = "index";
	private final static String NAME = "name";

	public PetriMonitor(final PetriNet _petri, TransitionsPolicy _policy) {
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
		
		anyThreadSleepingforTransition = new AtomicBoolean[transitionsAmount];
		Arrays.fill(anyThreadSleepingforTransition, new AtomicBoolean());
	}

	/**
	 * Tries to fire a transition.
	 * <ul>
	 * <li>If fails, the calling thread sleeps in the corresponding transition's queue</li>
	 * <li>If succeeds, it checks if there are new enabled transitions.
	 * If not it leaves, else it tries to fire some new enabled transition 
	 * (which transition is to be fired is the policy's decision).
	 * This may lead into two situations:</li>
	 * <ul>
	 * <li>The transition to fire is automatic, then just fire it</li>
	 * <li>The transition to fire is not automatic but another thread was waiting to fire it.
	 * In that case, wake up that thread and leave the monitor</li>
	 * </ul>
	 * </ul>
	 * For timed transitions, the calling thread sleeps until the transition reaches its time span, only then fires it.
	 * More info in project readme file.
	 * @param transitionToFire The transition to fire
	 * @throws NotInitializedPetriNetException when firing a timed transition before initializing the petri net
	 * @throws IllegalTransitionFiringError when an request to fire an automatic transition arrives
	 * @see PetriNet#fire(Transition, boolean)
	 */
	public void fireTransition(final Transition transitionToFire) throws IllegalTransitionFiringError, NotInitializedPetriNetException{
		fireTransition(transitionToFire, false);
	}

	/**
	 * Tries to fire a transition.
	 * <ul>
	 * <li>If fails, the calling thread sleeps in the corresponding transition's queue</li>
	 * <li>If succeeds, it checks if there are new enabled transitions.
	 * If not it leaves, else it tries to fire some new enabled transition 
	 * (which transition is to be fired is the policy's decision).
	 * This may lead into two situations:</li>
	 * <ul>
	 * <li>The transition to fire is automatic, then just fire it</li>
	 * <li>The transition to fire is not automatic but another thread was waiting to fire it.
	 * In that case, wake up that thread and leave the monitor</li>
	 * </ul>
	 * </ul>
	 * For timed transitions, the calling thread sleeps until the transition reaches its time span, only then fires it.
	 * A perennial fire doesn't send a thread to sleep when the firing failed.
	 * More info in project readme file.
	 * @param transitionToFire The transition to fire
	 * @param perenialFire True indicates a perennial fire
	 * @throws IllegalTransitionFiringError when an request to fire an automatic transition arrives
	 * @throws NotInitializedPetriNetException when firing a timed transition before initializing the petri net
	 * @see PetriNet#fire(Transition, boolean)
	 */
	public void fireTransition(final Transition transitionToFire, boolean perennialFire) throws IllegalTransitionFiringError, NotInitializedPetriNetException{
		// An attempt to fire an automatic transition is a severe error and the application should stop automatically
		if(transitionToFire.getLabel().isAutomatic()){
			throw new IllegalTransitionFiringError("An automatic transition has tried to be fired manually");
		}
		if(!petri.isInitialized()){
			throw new NotInitializedPetriNetException();
		}
		boolean releaseLock = true;
		try {
			// take the mutex to access the monitor
			inQueue.lock();
			releaseLock = internalFireTransition(transitionToFire, perennialFire);
		} finally{
			// the firing is done, release the mutex and leave
			if(releaseLock){
				inQueue.unlock();
			}
		}
	}
	
	/**
	 * @param transitionName The name of the transition to fire.
	 * @throws IllegalArgumentException If no transition matches transitionName
	 * @throws IllegalTransitionFiringError If transitionName matches an automatic transition
	 * @throws NotInitializedPetriNetException when firing a timed transition before initializing the petri net
	 * @see PetriMonitor#fireTransition(Transition)
	 */
	public void fireTransition(final String transitionName) throws IllegalArgumentException, IllegalTransitionFiringError, NotInitializedPetriNetException {
		fireTransition(transitionName, false);
	}
	
	/**
	 * @param transitionName The name of the transition to fire.
	 * @param perennialFire True indicates a perennial fire
	 * @throws IllegalArgumentException If no transition matches transitionName
	 * @throws IllegalTransitionFiringError If transitionName matches an automatic transition
	 * @throws NotInitializedPetriNetException when firing a timed transition before initializing the petri net
	 * @see PetriMonitor#fireTransition(Transition)
	 */
	public void fireTransition(final String transitionName, boolean perennialFire) throws IllegalArgumentException, IllegalTransitionFiringError, NotInitializedPetriNetException {
		Optional<Transition> filteredTransition = Arrays.stream(petri.getTransitions())
				.filter((Transition t) -> t.getName().equals(transitionName))
				// I can get only the first here because I made sure the name is unique in the parsing
				.findFirst();
		if(!filteredTransition.isPresent()){
			throw new IllegalArgumentException("No transition matches the name " + transitionName);
		}
		
		fireTransition(filteredTransition.get(), perennialFire);
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
	 * Subscribe the given observer to the transition matching the given name's events if it's informed
	 * @param _transitionName the name of the transition to subscribe to
	 * @param _observer the observer to subscribe
	 * @throws IllegalArgumentException if the given transition is not informed, the name or observer is null or the name doesn't match any transition
	 * @return a Subscription object used to unsubscribe
	 */
	public Subscription subscribeToTransition(final String _transitionName, final Observer<String> _observer) throws IllegalArgumentException{
		if(_transitionName == null){
			throw new IllegalArgumentException("Null string given as transition name");
		}
		
		Optional<Transition> transition = Arrays.stream(petri.getTransitions())
				.filter((Transition t) -> t.getName().equals(_transitionName))
				.findFirst();
		
		if(!transition.isPresent()){
			throw new IllegalArgumentException("There is no transition matching name " + _transitionName);
		}
		
		return subscribeToTransition(transition.get(), _observer);
	}
	
	/**
	 * Subscribe the given observer to the given transition events if it's informed
	 * @param _transition the transition to subscribe to
	 * @param _observer the observer to subscribe
	 * @throws IllegalArgumentException if the given transition is not informed or the transition or observer is null
	 * @return a Subscription object used to unsubscribe
	 */
	public Subscription subscribeToTransition(final Transition _transition, final Observer<String> _observer) throws IllegalArgumentException{
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
	
	/**
	 * Set a guard's new value
	 * @param guardName The target guard
	 * @param newValue New value to set
	 * @throws IndexOutOfBoundsException If the guard doesn't exist
	 * @throws NullPointerException If guardName is empty
	 * @throws NotInitializedPetriNetException when firing a transition before initializing the petri net
	 */
	public boolean setGuard(String guardName, boolean newValue) throws IndexOutOfBoundsException, NullPointerException, NotInitializedPetriNetException{
		if(guardName == null || guardName.isEmpty()){
			throw new NullPointerException("Empty guard name not allowed");
		}
		if(!petri.isInitialized()){
			throw new NotInitializedPetriNetException();
		}
		boolean couldSet = false;
		boolean releaseLock = true;
		try{
			inQueue.lock();
			petri.readGuard(guardName);
			couldSet = petri.addGuard(guardName, newValue);
			// setting this guard could've enabled some transitions
			// if any automatic was enabled it should be fired immediately
			// if any fired was enabled, let's wake a sleeping thread if available
			int nextTransitionToFireIndex = getNextTransitionAvailableToFire();
			if(nextTransitionToFireIndex >= 0){
				if(petri.getAutomaticTransitions()[nextTransitionToFireIndex]){
					releaseLock = internalFireTransition(petri.getTransitions()[nextTransitionToFireIndex], false);
				} else {
					// if a fired transition was enabled by the guard, wake up a thread waiting for it
					releaseLock = false;
					condVarQueue[nextTransitionToFireIndex].wakeUp();
				}
			}
		} finally {
			if(releaseLock){
				inQueue.unlock();
			}
		}
		return couldSet;
	}
	
	/**
	 * Searches through enabled transitions looking for automatic transitions and threads sleeping for manual transitions.
	 * If there is any available, the policy will tell which one is the next to be fired
	 * @return The index of the next transition to be fired or -1 if none available
	 */
	private int getNextTransitionAvailableToFire(){
		// let's see if any transition was enabled due to the last fired
		boolean enabledTransitions[] = petri.getEnabledTransitions();
		boolean queueHasThreadSleeping[] = getQueuesState(); //Is there anyone in the queue?
		boolean automaticTransitions[] = petri.getAutomaticTransitions();
		
		boolean availablesToFire[] = new boolean[enabledTransitions.length];
		boolean anyAvailable = false;
		for(int i = 0; i < availablesToFire.length; i++){
			availablesToFire[i] = enabledTransitions[i] && (queueHasThreadSleeping[i] || automaticTransitions[i]);
			anyAvailable |= availablesToFire[i];
		}
		
		if(!anyAvailable){
			return -1;
		}
		
		return transitionsPolicy.which(availablesToFire);
	}
	
	/**
	 * Sends an event to all subscribers in JSON format containing at least the transition's id.
	 * If no error occurs, the index is also added to the message.
	 * This method is intended to be called after a successful fire
	 * @param t the transition to send an event about
	 * @throws IllegalArgumentException If t is not informed
	 */
	private void sendEventAfterFiring(Transition t) throws IllegalArgumentException{
		if(!t.getLabel().isInformed()){
			throw new IllegalArgumentException("Non-informed transitions cannot send events");
		}
		try{
			// The event to send contains a JSON with the transition info
			HashMap<String, String> firedTransitionInfoMap = new HashMap<String, String>();
			firedTransitionInfoMap.put(ID, t.getId());
			firedTransitionInfoMap.put(INDEX, Integer.toString(t.getIndex()));
			firedTransitionInfoMap.put(NAME, t.getName());
			informedTransitionsObservable.onNext(
					jsonMapper.writeValueAsString(firedTransitionInfoMap));
		} catch (JsonProcessingException e) {
			// If there was an error processing the JSON let's send the minimal needed info hardcoded here
			informedTransitionsObservable.onNext("{\"" + ID + "\":\"" + t.getId() + "\"}");
		}
	}
	
	/**
	 * This method implements the firing logic with minimal checks.
	 * It is intended for using internally, when a {@link #inQueue} mutex was already taken.
	 * This method doesn't check for automatic transitions
	 * A perennial fire doesn't send a thread to sleep.
	 * @param transitionToFire
	 * @param perennialFire
	 * @return Whether to release the mutex {@link #inQueue}
	 * @throws NotInitializedPetriNetException If the net hasn't been initialized before calling this method
	 */
	private boolean internalFireTransition(Transition transitionToFire, boolean perennialFire) throws  NotInitializedPetriNetException{
		boolean releaseLock = true;
		boolean keepFiring = true;
		boolean sleptByItselfForThisTransition = false;

		int transitionIndex = transitionToFire.getIndex();

		while(keepFiring){
			keepFiring = petri.getEnabledTransitions()[transitionToFire.getIndex()];
			if(keepFiring){				
				try{
					if(petri.fire(transitionToFire)){
						//the transition was fired successfully. If it's informed let's send an event
						try{
							sendEventAfterFiring(transitionToFire);
						} catch (IllegalArgumentException e){
							//nothing wrong, the transition is not informed
						}
						
						boolean automaticTransitions[] = petri.getAutomaticTransitions();
						int nextTransitionToFireIndex = getNextTransitionAvailableToFire();
						if(nextTransitionToFireIndex >= 0){
							if(automaticTransitions[nextTransitionToFireIndex]){
								transitionToFire = petri.getTransitions()[nextTransitionToFireIndex];
							}
							else{
								// The transition chosen isn't automatic
								// so wake up the associated thread to that transition
								// and leave the monitor without releasing the input mutex
								condVarQueue[nextTransitionToFireIndex].wakeUp();
								releaseLock = false;
								keepFiring = false;
							}
						}
						else{
							// no transition left to fire, leave the monitor releasing the lock
							keepFiring = false;
							releaseLock = true;
						}
					}
					else if(!perennialFire){
						// if the transition wasn't fired sucessfully
						// go to sleep in the transition queue
						sleepInTransitionQueue(transitionToFire, sleptByItselfForThisTransition);
						// after waking up try to fire inside the timespan again
					}
					else {
						// the firing failed but since it's a perennial fire
						// the calling thread doesn't have to sleep
						// so return whether to release the mutex
						return releaseLock;
					}
				} catch(FiringBeforeTimespanException e){
					if(anyThreadSleepingforTransition[transitionIndex].compareAndSet(false, true)){
						// The calling thread came before time span, and there is nobody sleeping waiting for this transition,
						// release the input mutex and sleep here until the time has come.
						inQueue.unlock();
						
						long enablingTime = transitionToFire.getEnablingTime();
						long fireAttemptTime = System.currentTimeMillis();
						
						while(transitionToFire.isBeforeTimeSpan(fireAttemptTime)){
							try {
								// sleep until the time span occurs
								Thread.sleep(enablingTime - fireAttemptTime);
								// if the thread is not interrupted, just exit the loop after sleeping
								break;
							} catch (InterruptedException ex) {
								// recalculate the current time only if the thread was interrupted and sleep again if necessary
								fireAttemptTime = System.currentTimeMillis();
							} catch (IllegalArgumentException ex){
								// The sleeping time was negative. This shouldn't happen but if so, catch the exception and don't try to sleep again
								break;
							}
						}
						
						anyThreadSleepingforTransition[transitionIndex].set(false);
						
						// when this thread wakes up, its time to fire has come and may be short
						// so take the lock with high priority to avoid waiting for the incoming threads
						// This way, only as high-prioritized threads as this one may cause waiting
						inQueue.lock(LockPriority.HIGH);
						// If at waking time the transition has been disabled,
						// this thread has to sleep in the condition queue with high priority
						// to avoid a priority inversion, so set sleptByItselfForThisTransition to true
						sleptByItselfForThisTransition = true;
					} else if (!perennialFire){
						// if any thread was already sleeping on its own for this transition, sleep in the queue
						sleepInTransitionQueue(transitionToFire, sleptByItselfForThisTransition);
					} else {
						// a perennial fire should not wait in the queue for the transition to get enabled again
						return releaseLock;
					}
					
				} catch(FiringAfterTimespanException e){
					if(perennialFire){
						// a perennial fire should not wait in the queue for the transition to get enabled again
						return releaseLock;
					}
					// The calling thread came late, the time is over. Thus the thread releases the input mutex and goes to sleep
					sleepInTransitionQueue(transitionToFire, sleptByItselfForThisTransition);
				} catch (IllegalArgumentException e) {
					throw new IllegalTransitionFiringError(e);
				} catch (PetriNetException e) {
					if(e instanceof NotInitializedPetriNetException){
						throw (NotInitializedPetriNetException)e;
					}
					// other instances of PetriNetException are handled in other catch clauses
				}
			}
			// if this is a perennial fire and the transition is not enabled, don't send the thread to sleep
			else if(!perennialFire){
				// the fire failed, thus the thread releases the input mutex and goes to sleep
				sleepInTransitionQueue(transitionToFire, sleptByItselfForThisTransition);
				keepFiring = true;
			}
		}
		return releaseLock;
	}
	
	private void sleepInTransitionQueue(final Transition transitionToFire, boolean sleptByItselfForThisTransition){
		
		inQueue.unlock();
		if(sleptByItselfForThisTransition){
			// If the flag sleptByItselfForThisTransition is true, it means this thread already slept by itself for this transition
			// which implies that no thread had tried to fire this transition when it arrived the monitor.
			// Additionally, this thread also lost the timespan so it must have the highest priority for next enabling time
			condVarQueue[transitionToFire.getIndex()].sleepWithHighPriority();
		}
		else {
			condVarQueue[transitionToFire.getIndex()].sleep();
		}
		// when waking up, don't take the input lock for the waking thread didn't release it
	}
	
	/**
	 * Check if a thread is sleeping (on its own, outside any queues) for the given transition.
	 * @param transitionIndex The index of the transition to check is a thread is sleeping for.
	 * @return True if a thread is sleeping for the given transition.
	 */
	public boolean isAnyThreadSleepingForTransition(int transitionIndex){
		return anyThreadSleepingforTransition[transitionIndex].get();
	}
}
