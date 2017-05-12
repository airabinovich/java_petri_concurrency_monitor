package org.unc.lac.javapetriconcurrencymonitor.monitor.policies;

import java.util.Random;

import org.unc.lac.javapetriconcurrencymonitor.petrinets.PetriNet;

/**
 * Extends the TransitionsPolicy abstract class and decides based on a random number generator
 *
 */
public class RandomPolicy extends TransitionsPolicy {

	private Random random_generator;
	
	public RandomPolicy(PetriNet _petri){
		super(_petri);
		random_generator = new Random(System.currentTimeMillis());
	}
	
	@Override
	public int which(boolean[] enabled) {
		int index;
		int retries = enabled.length * 3;
		do{
			retries--;
			if(retries < 0){
				return -1;
			}
			index = random_generator.nextInt(enabled.length);
		} while(!enabled[index]);
		
		return index;
	}

}
