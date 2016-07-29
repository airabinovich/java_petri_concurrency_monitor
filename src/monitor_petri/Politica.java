package monitor_petri;

import java.util.Vector;

//Posiblemente sea un strategy donde tengamos distintas políticas (FIFO, LRU, etc)
public class Politica {
	
	public Politica(){
		
	}
	
	public Transicion cual(Transicion[] sensibilizadas){
		Vector<Transicion> sens = new Vector<Transicion>();
		sens.copyInto(sensibilizadas);
		return sens.firstElement();
	}
}
