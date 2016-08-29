package monitor_petri;

import java.util.concurrent.Semaphore;

import Petri.PetriNet;
import Petri.Transition;

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

	public MonitorManager(final PetriNet _petri, TransitionsPolicy _policy) {
		if(_petri == null || _policy == null){
			throw new IllegalArgumentException(this.getClass().getName() + " constructor. Invalid arguments");
		}
		petri = _petri;
		transitionsPolicy = _policy;
		condVarQueue = new FairQueue[petri.getTransitions().length];
		final boolean automaticTransitions[] = petri.getAutomaticTransitions();
		for(int i = 0; i < automaticTransitions.length; i++){
			// Only non-automatic transitions have an associated queue
			// since no thread will try to fire an automatic transition
			// and thus will not sleep if fails
			if(!automaticTransitions[i]){
				condVarQueue[i] = new FairQueue();
			}
		}
	}

	/**
	 * Tries to fire a transition.
	 * <ul>
	 * <li>If fails, the calling thread sleeps in the corresponding VarCondQueue</li>
	 * <li>If succeedes, tries to fire any new enabled transition.
	 * This may lead into two situations:</li>
	 * <ul>
	 * <li>The transition to fire is automatic, just fire it</li>
	 * <li>The transition to fire is not automatic but another thread was waiting to fire it.
	 * 		  In that case, wake up that thread and leave the monitor</li>
	 * </ul>
	 * </ul>
	 * @param transitionToFire the transition to fire
	 * @throws IllegalTransitionFiringError when an request to fire an automatic transition arrives
	 */
	public void fireTransition(Transition transitionToFire) throws IllegalTransitionFiringError{
		// An attempt to fire an automatic transition is a severe error and the application should stop automatically
		if(transitionToFire.getLabel().isAutomatic()){
			throw new IllegalTransitionFiringError("An automatic transition has tried to be fired manually");
		}
		int permitsToRelease = 0;
		try {
			// take the mutex to access the monitor
			inQueue.acquire();
			boolean keepFiring = true;
			while(keepFiring){
				// keepFiring is "k" variable
				keepFiring = petri.fire(transitionToFire); // returns true if transitionToFire was fired
				if(keepFiring){
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
	private boolean[] getQueuesState() {
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
	
	public void setGuard(int i, boolean k){
	}
	
	public void fireGuard(int i, boolean to){
	}
}
