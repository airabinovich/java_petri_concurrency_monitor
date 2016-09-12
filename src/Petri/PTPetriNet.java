package Petri;

public class PTPetriNet extends PetriNet{

	protected PTPetriNet(Place[] _places, Transition[] _transitions, Arc[] _arcs, Integer[] _initialMarking,
			Integer[][] _preI, Integer[][] _posI, Integer[][] _I) {
		super(_places, _transitions, _arcs, _initialMarking, _preI, _posI, _I);
	}

}
