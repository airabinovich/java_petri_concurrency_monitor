package org.unc.lac.javapetriconcurrencymonitor.monitor.policies;

import org.unc.lac.javapetriconcurrencymonitor.petrinets.PetriNet;

/**
 * Extends the TransitionsPolicy abstract class and decides based on the first transition found;
 *
 */
public class FirstInLinePolicy extends TransitionsPolicy {

	public FirstInLinePolicy(PetriNet _petri) {
		super(_petri);
	}

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
