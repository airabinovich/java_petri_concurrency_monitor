package monitor_petri;

import java.util.Vector;

import Petri.Transition;

//Posiblemente sea un strategy donde tengamos distintas políticas (FIFO, LRU, etc)
/**
 * Transitions Policy. Used for condition variables in Monitor
 *
 */
public class Policy {
	
	public Policy(){
		
	}
	
	public Transition which(Boolean[] enabled){
		Vector<Transition> en = new Vector<Transition>();
		en.copyInto(enabled);
		return en.firstElement();
	}
}
