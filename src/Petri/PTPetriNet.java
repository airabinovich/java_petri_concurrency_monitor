package Petri;

public class PTPetriNet extends PetriNet{

	/**
	 * extends the abstract class PetriNet
	 * @see PetriNet#PetriNet(Place[], Transition[], Arc[], Integer[], Integer[][], Integer[][], Integer[][])
	 */
	public PTPetriNet(Place[] _places, Transition[] _transitions, Arc[] _arcs, Integer[] _initialMarking,
			Integer[][] _preI, Integer[][] _posI, Integer[][] _I, Boolean[][] _inhibition, Boolean[][] _resetMatrix) {
		super(_places, _transitions, _arcs, _initialMarking, _preI, _posI, _I, _inhibition, _resetMatrix);
	}

}
