package Petri;

import java.util.Arrays;

import org.javatuples.Quartet;
import org.javatuples.Triplet;

public class TimedPetriNet extends PetriNet{

	protected boolean[] enabledTransitions;
	
	protected TimedPetriNet(Place[] _places, Transition[] _transitions, Arc[] _arcs, Integer[] _initialMarking,
			Integer[][] _preI, Integer[][] _posI, Integer[][] _I) {
		super(_places, _transitions, _arcs, _initialMarking, _preI, _posI, _I);
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
			//Check if the enabled transition was enabled by this fire.
			if (isEnabled(t) && !enabledTransitions[t.getIndex()]){
				t.getTimeSpan().setEnableTime(System.currentTimeMillis());
			}
		}
		return _enabledTransitions;
	}
	
	/**
	 * TimedPetriNet builder. Gets info from PNML file. Call buildPetriNet to get a PetriNet object
	 *
	 */
	public static class TimedPetriNetBuilder extends PetriNetBuilder{

		public TimedPetriNetBuilder(String pathToPNML) throws NullPointerException{
			super(pathToPNML);
		}
		
		public TimedPetriNetBuilder(PNMLreader _reader) throws NullPointerException {
			super(_reader);
		}
		
		public TimedPetriNet buildPetriNet(){			
			Quartet<Place[], Transition[], Arc[], Integer[]> petriObjects = PNML2PNObjects();
			Triplet<Integer[][], Integer[][], Integer[][]> petriMatrices = 
					rdpObjects2Matrices(petriObjects.getValue0(), petriObjects.getValue1(), petriObjects.getValue2());
			
			return new TimedPetriNet(petriObjects.getValue0(), petriObjects.getValue1(), petriObjects.getValue2(), petriObjects.getValue3(),
					petriMatrices.getValue0(), petriMatrices.getValue1(), petriMatrices.getValue2());
		}
		
	}
}
