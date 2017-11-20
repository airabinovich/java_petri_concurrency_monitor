package org.unc.lac.javapetriconcurrencymonitor.monitor.policies;

import org.unc.lac.javapetriconcurrencymonitor.petrinets.PetriNet;

/**
 * Transitions Policy. Used for condition variables in Monitor
 *
 */
public abstract class TransitionsPolicy {

	/**
	 * PetriNet field is useful for defining custom policies based on transitions name or other parameters.
	 */
	protected PetriNet petri;

	/**
	 * Given an array of booleans specifying the transition ready to be fired,
	 * the policy decides which should be fired
	 * @param enabled an array of boolean containing true if the matching transition is ready to be fired
	 * @return the transition to fire or -1 if none is enabled
	 */
	public abstract int which(boolean[] enabled);

	public TransitionsPolicy(PetriNet _petri) {
		this.petri = _petri;
	}

}
