package monitor_petri;

import java.util.concurrent.Semaphore;

import Petri.PetriNet;
import Petri.Transition;

public class MonitorManager extends Thread {

	//Petri net
	private PetriNet pn;	
	//inQueue is a FIFO queue
	private Semaphore inQueue = new Semaphore(1,true);
	//Array of condition variables queues
	private Queue[] condVarQueue;	
	//Politics
	private Policy politics;

	//Constructor
	public MonitorManager(final PetriNet net, Policy p) {
		// TODO Auto-generated constructor stub
		pn = net;
		politics = p;
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
				Queue ve[] = condVarQueue;
				
				//WTF??? Transicion ee[] = etiquetas.get_etiquetas_entrada();
				//Las transiciones ee son las que son automaticas (vector booleano T*1)
				// quien es m? que significa?
				//m es un vector de las transiciones que se pueden disparar entre las sensibilizas, las que estan en la cola y las automaticas
				boolean m = (enabledTransitionsVector.length != 0) && (ve.length != 0 );//|| ee); //ESTA MAL
				if (!m){
					// sí hay alguna transición sensibilizada
					Transition t_aux = politics.which(enabledTransitionsVector);
					if(t_aux.getLabel().isAutomatic()){
						t = t_aux;
					}
					else{
						// hay sensibilizada pero no automática. Si hay algún hilo asociado hay que dispararlo
						// tengo que despertar al primer hilo dormido en la cola asociada a la transición t_aux
						// eso lo debemos obtener con un mapa
						//colasVarCond.wakeUp(1);	
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
		};
		inQueue.release();
	}
	
	public void setGuard(int i, boolean k){
		
	}
	
	public void fireGuard(int i, boolean to){
		
	}
}
