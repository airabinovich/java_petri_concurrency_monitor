package monitor_petri;

import java.util.ArrayList;
import java.util.concurrent.Semaphore;

import Petri.PetriNet;
import Petri.Transition;

public class MonitorManager extends Thread {

	private PetriNet pn;	
	//inQueue is a FIFO queue for the monitor access
	private Semaphore inQueue = new Semaphore(1,true);
	private VarCondQueue[] condVarQueue;	
	private Policy policies;

	public MonitorManager(final PetriNet net, Policy p) {
		pn = net;
		policies = p;
		condVarQueue = new FairQueue[pn.getTransitions().length];
		final boolean automaticTransitions[] = pn.getAutomaticTransitions();
		for(int i=0; i<automaticTransitions.length; i++){
			// Only non-automatic transitions have an associated queue
			// since no thread will try to fire an automatic transition
			// and thus will not sleep if fails
			if(!automaticTransitions[i]){
				condVarQueue[i] = new FairQueue();
			}
		}
	}

	public void fireTransition(Transition t){
		try {
			inQueue.acquire();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		boolean enabledFire = true;
		while(enabledFire){
			// enabledFire is "k" variable
			enabledFire = pn.fire(t); // returns true if t was fired
			if(enabledFire){
				//if it's possible to fire, let's see if some automatic transition were enabled 
				//or existed before
				Boolean enabledTransitionsVector[] = pn.getEnabledTransitions();
				Boolean queuesState[] = getQueuesState(); //Is there anyone in the queue?
				boolean automaticTransitions[] = pn.getAutomaticTransitions();
				
				boolean m[] = new boolean[enabledTransitionsVector.length];
				for(int i=0; i<m.length; i++){
					m[i] = enabledTransitionsVector[i] && (queuesState[i] || automaticTransitions[i]);
				}
				
				//someEnabled is true if at least one item of m has a true value.
				boolean anyEnabled = false;
				for(boolean enabled : m){
					if(enabled){
						anyEnabled = true;
						break;
					}
				}
				if (anyEnabled){
					// is there any enabled transition to be fired?
					int t_aux_index = policies.which(m);
					if(automaticTransitions[t_aux_index]){
						t = pn.getTransitions()[t_aux_index];
					}
					else{
						//There is a enabled but not automatic transition.
						//So, wakes up the associated thread to transition.
						condVarQueue[t_aux_index].wakeUp();
					}
				}
				else{
					enabledFire = false;
				}
			}
			else{
				inQueue.release();
				// tengo que dormir al hilo actual en la cola asociada a la transición ti
				// eso se obtiene con el índice de la transición
				// colasVarCond[indice_a_obtener].goToSleep(Thread.getCurrentThread());
			}
		}
		inQueue.release();
		condVarQueue[t.getIndex()].wakeUp();
	}
	
	private Boolean[] getQueuesState() {
		Boolean[] queues = new Boolean[condVarQueue.length];
		Boolean empty = true;
		for(int i=0; i<condVarQueue.length; i++){
			if(condVarQueue[i].isEmpty()){
				empty = false;
			}
			queues[i] = empty;
		}
		return queues;
	}

	public void setGuard(int i, boolean k){
		
	}
	
	public void fireGuard(int i, boolean to){
		
	}
}
