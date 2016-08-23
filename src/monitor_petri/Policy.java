package monitor_petri;

//Posiblemente sea un strategy donde tengamos distintas políticas (FIFO, LRU, etc)
/**
 * Transitions Policy. Used for condition variables in Monitor
 *
 */
public class Policy {
	
	public Policy(){
		
	}
	
	public int which(boolean[] enabled){
		for(int i = 0; i < enabled.length; i++){
			if(enabled[i]){
				return i;
			}
		}
		return -1;
	}
}
