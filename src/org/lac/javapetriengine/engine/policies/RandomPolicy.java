package org.lac.javapetriengine.engine.policies;

import java.util.Random;

/**
 * Implements the TransitionsPolicy interface and decides based on a random number generator
 *
 */
public class RandomPolicy implements TransitionsPolicy {

	private Random random_generator;
	
	public RandomPolicy(){
		random_generator = new Random(System.currentTimeMillis());
	}
	
	@Override
	public int which(boolean[] enabled) {
		int index;
		int retries = enabled.length * 2;
		do{
			retries--;
			index = random_generator.nextInt(enabled.length + 1);
			if(retries < 0){
				return -1;
			}
		}while(!enabled[index]);
		
		return index;
	}

}
