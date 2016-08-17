package monitor_petri;

import java.util.Vector;

import Petri.Transition;

//Posiblemente sea un strategy donde tengamos distintas políticas (FIFO, LRU, etc)
public class Politics { //puede ser un strategy (no es prioritario hacerlo asi, hay que ver si encaja)
	//politica de transiciones (colas de variables del monitor)
	
	public Politics(){
		
	}
	
	public Transition which(Integer[] enabled){
		Vector<Transition> en = new Vector<Transition>();
		en.copyInto(enabled);
		return en.firstElement();
	}
}
