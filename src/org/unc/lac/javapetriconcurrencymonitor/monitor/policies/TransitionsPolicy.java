package org.unc.lac.javapetriconcurrencymonitor.monitor.policies;

/**
 * Transitions Policy. Used for condition variables in Monitor
 *
 */
public interface TransitionsPolicy {
	
	/**
	 * Given an array of booleans specifying the transition ready te be fired,
	 * the policy decides which should be fired
	 * @param enabled an array of boolean containing true if the matching transition is ready to be fired
	 * @return the transition to fire or -1 if none is enabled
	 */
	public int which(boolean[] enabled);
	
}
