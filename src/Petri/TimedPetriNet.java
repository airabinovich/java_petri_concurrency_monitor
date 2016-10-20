package Petri;

import java.util.Arrays;

public class TimedPetriNet extends PetriNet{

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
	}

	/**
	 * Computes the enabled transitions
	 * Uses the method computeEnabledTransitions and set the times in order to
	 * avoid the time race condition related to initialization times
	 * @see {@link TimedPetriNet#computeEnabledTransitions()} 
	 */
	public void startTimes(){
		long time = System.currentTimeMillis();
		for(Transition t : transitions){
			boolean transitionEnabled = isEnabled(t);
			this.enabledTransitions[t.getIndex()] = transitionEnabled;
			if (t.getTimeSpan() != null && isEnabled(t)){
				t.getTimeSpan().setEnableTime(time);
			}
		}
	}
	
	/**
	 * Fires the transition specified by transitionIndex and updates the enabled transitions with their timestamps
	 * @param transitionIndex The index of the transition to be fired
	 * @return True if the fire was successful
	 * @throws IllegalArgumentException If the index is negative or greater than the last transition index.
	 * @see PetriNet#fire(int)
	 */
	public boolean fire(int transitionIndex) throws IllegalArgumentException{
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
	 * Computes the enabled transitions, setting the enable times if it is necessary
	 * If a transition was enabled, its timespan is not updated
	 * @return True if the fire was successful
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
}
