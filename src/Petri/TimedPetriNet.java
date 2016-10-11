package Petri;

import java.util.Arrays;

public class TimedPetriNet extends PetriNet{

	protected boolean[] enabledTransitions;
	
	/**
	 * extends the abstract class PetriNet, and also has a boolean array containing
	 * the enabled transitions 
	 * @see PetriNet#PetriNet(Place[], Transition[], Arc[], Integer[], Integer[][], Integer[][], Integer[][])
	 */
	public TimedPetriNet(Place[] _places, Transition[] _transitions, Arc[] _arcs, Integer[] _initialMarking,
			Integer[][] _preI, Integer[][] _posI, Integer[][] _I, Boolean[][] _inhibition, Boolean[][] _resetMatrix) {
		super(_places, _transitions, _arcs, _initialMarking, _preI, _posI, _I, _inhibition, _resetMatrix);
		enabledTransitions = new boolean[_transitions.length];
		Arrays.fill(enabledTransitions, false);
		this.enabledTransitions = computeEnabledTransitions();
	}
	
	public boolean fire(int transitionIndex){
		return fire(transitionIndex, false);
	}
	
	public boolean fire(int transitionIndex, boolean perennialFire){
		boolean fire = super.fire(transitionIndex, perennialFire);
		//Compute new enabled transitions and set new timestamp 
		this.enabledTransitions = computeEnabledTransitions();
		return fire;
	}
	
	public boolean[] getEnabledTransitions(){
		return this.enabledTransitions;
	}
	
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
