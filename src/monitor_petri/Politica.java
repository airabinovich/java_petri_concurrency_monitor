package monitor_petri;

import java.util.Vector;

//Posiblemente sea un strategy donde tengamos distintas políticas (FIFO, LRU, etc)
public class Politica { //puede ser un strategy (no es prioritario hacerlo asi, hay que ver si encaja)
	//politica de transiciones (colas de variables del monitor)
	
	public Politica(){
		
	}
	
	public Transicion cual(Transicion[] sensibilizadas){
		Vector<Transicion> sens = new Vector<Transicion>();
		sens.copyInto(sensibilizadas);
		return sens.firstElement();
	}
}
