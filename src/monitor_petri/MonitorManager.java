package monitor_petri;

import java.util.ArrayList;
import java.util.concurrent.Semaphore;

import Petri.PetriNet;
import Petri.Transition;

public class MonitorManager extends Thread {

	//Petri net
	private PetriNet pn;	
	//inQueue is a FIFO queue
	private Semaphore inQueue = new Semaphore(1,true);
	//Array of condition variables queues
	private FairQueue[] condVarQueue;	
	//Policies
	private Policy policies;

	//Constructor
	public MonitorManager(final PetriNet net, Policy p) {
		// TODO Auto-generated constructor stub
		pn = net;
		policies = p;
		//Creates an array containing the condition variables queues which contain:
		//a Semaphore (with 0 permits and fifo policy) if the associated transition is not automatic
		//and null otherwise
		condVarQueue = new FairQueue[pn.getTransitions().length];
		for(int i=0; i<pn.getTransitions().length; i++){
			condVarQueue[i] = new FairQueue(pn.getTransitions()[i]);
		}
	}

	public void fireTransition(Transition t){
		try {
			inQueue.acquire();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
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
				Boolean queuesState[] = getQueuesState(); //Is there someone in the queue?
				Boolean automatics[] = pn.getAutomaticTransitions();
				
				Boolean m[] = new Boolean[enabledTransitionsVector.length];
				for(int i=0; i<m.length; i++){
					m[i] = Boolean.logicalAnd(enabledTransitionsVector[i],(Boolean.logicalOr(queuesState[i], automatics[i])));
				}
				
				//someEnabled is true if at least one item of m has a true value.
				Boolean someEnabled = false;
				for(Boolean enabled : m){
					if(enabled){
						someEnabled = true;
					}
				}
				if (someEnabled){
					// is there some enabled transition to be fired?
					int t_aux_index = policies.which(m);
					if(automatics[t_aux_index]){
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
				// eso lo debemos obtener con un mapa
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
