package monitor_petri;

import java.util.ArrayList;
import java.util.concurrent.Semaphore;

import Petri.PetriNet;
import Petri.Transition;

public class GestorMonitor extends Thread {

	//Red de petri
	private PetriNet rdp;	
	//Cola de entrada es un colaDeEntrada FIFO
	private Semaphore colaDeEntrada = new Semaphore(1,true);
	//Array de colas de variables de condicion
	private Cola[] colasVarCond;	
	//Politica
	private Politica politica;

	//Constructor del gestor del monitor
	public GestorMonitor(final PetriNet red, Politica p) {
		// TODO Auto-generated constructor stub
		rdp = red;
		politica = p;
	}

	public void disparo_transicion(Transition t){
		try {
			colaDeEntrada.acquire();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		boolean disparoDisponible = true;
		while(disparoDisponible){
			// disparoDisponible es el k que dice Orlando (nombre horrible)
			// 
			disparoDisponible = rdp.fire(t);
			if(disparoDisponible){
				//si la pudo disparar, veamos si se sensibilizó alguna automática
				//o existe alguna automática sensibilizada de antes
				Transition vs[] = rdp.getEnabledTransitions();
				Cola ve[] = colasVarCond;
				//WTF??? Transicion ee[] = etiquetas.get_etiquetas_entrada();
				//Las transiciones ee son las que son automaticas (vector booleano T*1)
				// quien es m? que significa?
				//m es un vector de las transiciones que se pueden disparar entre las sensibilizas, las que estan en la cola y las automaticas
				boolean m = (vs.length != 0) && (ve.length != 0 );//|| ee); //ESTA MAL
				if (!m){
					// sí hay alguna transición sensibilizada
					Transition t_aux = politica.cual(vs);
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
					disparoDisponible = false;
				}
			}
			else{
				colaDeEntrada.release();
				// tengo que dormir al hilo actual en la cola asociada a la transición ti
				// eso lo debemos obtener con un mapa
				// colasVarCond[indice_a_obtener].goToSleep(Thread.getCurrentThread());
			}
		};
		colaDeEntrada.release();
	}
	
	public void set_guarda(int i, boolean k){
		
	}
	
	public void disparar_guarda(int i, boolean to){
		
	}
}
