package org.unc.lac.javapetriconcurrencymonitor.petrinets;

/**
 * This enum contains all the possible outcomes that may occur when firing a petri net's transition
 */
public enum PetriNetFireOutcome {
	/** Indicates the fire was successful. */
	SUCCESS,
	/** The selected transition wasn't enabled at firing time. */
	NOT_ENABLED,
	
	/** Only for {@link TimedPetriNet}. The firing timestamp is before the transition's timespan. */
	TIMED_BEFORE_TIMESPAN,
	/** Only for {@link TimedPetriNet}. The firing timestamp is after the transition's timespan. */
	TIMED_AFTER_TIMESPAN

}
