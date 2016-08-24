package monitor_petri;

/**
 * Implements the TransitionsPolicy interface and decides based on the first transition found;
 *
 */
public class FirstInLinePolicy implements TransitionsPolicy {

	@Override
	public int which(boolean[] enabled){
		for(int i = 0; i < enabled.length; i++){
			if(enabled[i]){
				return i;
			}
		}
		return -1;
	}

}
