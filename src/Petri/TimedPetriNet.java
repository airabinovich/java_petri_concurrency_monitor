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
			Integer[][] _preI, Integer[][] _posI, Integer[][] _I, Integer[][] _inhibition) {
		super(_places, _transitions, _arcs, _initialMarking, _preI, _posI, _I, _inhibition);
		enabledTransitions = new boolean[_transitions.length];
		Arrays.fill(enabledTransitions, false);
		this.enabledTransitions = computeEnabledTransitions();
	}
	
	public boolean fire(int transitionIndex){
		boolean fire = super.fire(transitionIndex);
		//Compute new enabled transitions and set new timestamp 
		this.enabledTransitions = computeEnabledTransitions();
		return fire;
	}
	
	public boolean[] getEnabledTransitions(){
		return this.enabledTransitions;
	}
	
	public boolean[] computeEnabledTransitions(){
		boolean[] _enabledTransitions = new boolean[transitions.length];
		for(Transition t : transitions){
			_enabledTransitions[t.getIndex()] = isEnabled(t);
			if(t.getTimeSpan() != null){
				//Check if the enabled transition was enabled by this fire.
				if (isEnabled(t) && !enabledTransitions[t.getIndex()]){
					t.getTimeSpan().setEnableTime(System.currentTimeMillis());
				}
			}			
		}
		return _enabledTransitions;
	}
}
