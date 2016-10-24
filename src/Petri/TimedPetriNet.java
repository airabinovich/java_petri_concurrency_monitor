package Petri;

import java.util.Arrays;

public class TimedPetriNet extends PetriNet{

	protected boolean timesStarted;
	protected boolean[] enabledTransitions;
	
	/**
	 * extends the abstract class PetriNet, and also has a boolean array containing
	 * the enabled transitions
	 * The enabled transitions are not calculated at initialization time, so
	 * before fire the first transition, there must calculate them @see {@link TimedPetriNet#startTimes()}
	 * @see PetriNet#PetriNet(Place[], Transition[], Arc[], Integer[], Integer[][], Integer[][], Integer[][])
	 */
	public TimedPetriNet(Place[] _places, Transition[] _transitions, Arc[] _arcs, Integer[] _initialMarking,
			Integer[][] _preI, Integer[][] _posI, Integer[][] _I, Boolean[][] _inhibition, Boolean[][] _resetMatrix) {
		super(_places, _transitions, _arcs, _initialMarking, _preI, _posI, _I, _inhibition, _resetMatrix);
		enabledTransitions = new boolean[_transitions.length];
		Arrays.fill(enabledTransitions, false);
		this.timesStarted = false;
	}

	/**
	 * Computes the enabled transitions for first time
	 * Uses the method computeEnabledTransitions and set the times
	 * @see TimedPetriNet#computeEnabledTransitions() 
	 */
	public void startTimes(){
		this.enabledTransitions = computeEnabledTransitions();
		this.timesStarted = true;
	}
	
	/**
	 * Fires the transition specified by transitionIndex and updates the enabled transitions with their timestamps
	 * @param transitionIndex The index of the transition to be fired
	 * @return True if the fire was successful
	 * @throws NotInitializedTimedPetriNetException if the times are not initialized
	 * @see TimedPetriNet#startTimes()
	 * @see PetriNet#fire(int)
	 */
	public boolean fire(int transitionIndex){
		boolean wasFired = super.fire(transitionIndex);
		//Compute new enabled transitions and set new timestamp 
		this.enabledTransitions = computeEnabledTransitions();
		return wasFired;
	}

	/**
	 * Fires the specified transition and updates the enabled transitions with their timestamps
	 * @param t The transition to be fired
	 * @return True if the fire was successful
	 * @throws IllegalArgumentException If t is null or if it doesn't match any transition index.
	 * @see TimedPetriNet#startTimes()
	 * @see PetriNet#fire(Transition)
	 */
	public boolean fire(final Transition t) throws IllegalArgumentException{
		if(t == null){
			throw new IllegalArgumentException("Tried to fire null transition");
		}
		return fire(t.getIndex());
	}

	public boolean[] getEnabledTransitions(){
		return this.enabledTransitions;
	}
	
	/**
	 * Computes the enabled transitions, setting the enable times for new enabled transitions only
	 * If a transition was already enabled, its timespan is not updated
	 * @return the boolean array with enabled transitions
	 */
	protected boolean[] computeEnabledTransitions(){
		boolean[] _enabledTransitions = new boolean[transitions.length];
		for(Transition t : transitions){
			int transitionIndex = t.getIndex();
			boolean transitionEnabled = isEnabled(t);
			_enabledTransitions[transitionIndex] = transitionEnabled;
			if(t.getTimeSpan() != null){
				//Check if the enabled transition was enabled by this fire.
				if (transitionEnabled && !enabledTransitions[transitionIndex]){
					t.getTimeSpan().setEnableTime(System.currentTimeMillis());
				}
			}
		}
		return _enabledTransitions;
	}
	
	public boolean isTimesStarted() {
		return this.timesStarted;
	}

}
