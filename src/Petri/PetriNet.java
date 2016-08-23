package Petri;

import java.io.FileNotFoundException;
import java.util.ArrayList;

import org.javatuples.Quartet;
import org.javatuples.Triplet;

public class PetriNet {
	
	protected Place[] places;
	protected Transition[] transitions;
	protected Arc[] arcs;
	protected Integer[][] pre;
	protected Integer[][] post;
	protected Integer[][] inc;
	protected Integer[] currentMarking;
	protected Integer[] initialMarking;
	protected boolean[] automaticTransitions;
	
	/**
	 * Builds a PetriNet Object. This is intended to be used by PetriNetBuilder
	 * @param _places Array of Place objects (dimension p)
	 * @param _transitions Array of Transition objects (dimension t)
	 * @param _arcs Array of Arcs
	 * @param _initialMarking Array of Integers (tokens in each place) (dimension p)
	 * @param _preI Pre-Incidence matrix (dimension p*t)
	 * @param _posI Post-Incidence matrix (dimension p*t)
	 * @param _I Incidence matrix (dimension p*t)
	 */
	protected PetriNet(Place[] _places, Transition[] _transitions, Arc[] _arcs,
			Integer[] _initialMarking, Integer[][] _preI, Integer[][] _posI, Integer[][] _I){
		this.places = _places;
		this.transitions = _transitions;
		this.automaticTransitions = getAutomatic();
		this.arcs = _arcs;
		this.initialMarking = _initialMarking.clone();
		this.currentMarking = _initialMarking;
		this.pre = _preI;
		this.post = _posI;
		this.inc = _I;
	}
	
	private boolean[] getAutomatic() {
		boolean[] automatics = new boolean[transitions.length];
		for(int i=0; i<automatics.length; i++){
			automatics[i] = transitions[i].getLabel().isAutomatic();
		}
		return automatics;
	}

	/**
	 * Fires the transition t if it's enabled
	 * @param t Transition to be fired
	 * @return true if t was fired
	 */
	public boolean fire(Transition t) {
		return fire(t.getIndex());
	}
	
	/**
	 * Fires the transition whose index if transitionIndex if it's enabled
	 * @param transitionIndex Transition's index to be fired
	 * @return true if transitionIndex was fired
	 */
	public boolean fire(int transitionIndex){
		// m_(i+1) = m_i + I*d
		// when d is a vector where every element is 0 but the nth which is 1
		// it's equivalent to pick nth column from Incidence matrix (I) 
		// and add it to the current marking (m_i)
		if(!isEnabled(transitionIndex)){
			return false;
		}
		
		for(int i = 0; i < currentMarking.length; i++){
			currentMarking[i] +=  inc[i][transitionIndex];
			places[i].setMarking(currentMarking[i]);
		}
		return true;
	}
	
	/**
	 * gets the transitions array and evaluates each one if is enabled or not.
	 * @return a boolean array that contains if each transition is enabled or not (true or false)
	 */
	public Boolean[] getEnabledTransitions(){
		Boolean[] enabledTransitions = new Boolean[transitions.length];
		for(Transition t : transitions){
			enabledTransitions[t.getIndex()] = isEnabled(t);
		}
		return enabledTransitions;
	}
	
	public boolean[] getAutomaticTransitions(){
		return automaticTransitions;
	}
	
	//No sabemos que hace todavia
	public void disparar_guarda(int ti, boolean to) {
	}
	
	//No sabemos que hace todavia (tampoco)
	public void set_guarda(boolean p, int i){
		
	}
	
	/**
	 * @return a copy of the places
	 */
	public Place[] getPlaces() {
		Place[] retPlaces = new Place[this.places.length];
		for(int i = 0; i< this.places.length; i++){
			retPlaces[i] = new Place(this.places[i]); 
		}
		return places;
	}
	
	/**
	 * @return the transitions
	 */
	public Transition[] getTransitions() {
		return transitions;
	}

	/**
	 * @return the arcs
	 */
	public Arc[] getArcs() {
		return arcs;
	}

	/**
	 * @return the pre matrix
	 */
	public Integer[][] getPre() {
		return pre;
	}

	/**
	 * @return the post matrix
	 */
	public Integer[][] getPost() {
		return post;
	}
	/**
	 * @return the incidence matrix
	 */
	public Integer[][] getInc() {
		return inc;
	}

	/**
	 * @return the currentMarking
	 */
	public Integer[] getCurrentMarking() {
		return currentMarking;
	}

	/**
	 * @return the initialMarking
	 */
	public Integer[] getInitialMarking() {
		return initialMarking;
	}
	
	/**
	 * @return whether the transition is enabled or not
	 * 
	 */
	public boolean isEnabled(int transitionIndex){
		boolean enabled = true;
		for (int i=0; i<places.length ; i++){
			if (pre[i][transitionIndex] > currentMarking[i]){
				enabled = false;
				break;
			}
		}
		return enabled;
	}
	
	/**
	 * @return whether the transition is enabled or not
	 * 
	 * TODO: Leer filmina 19 del archivo "Redes_de_Petri_2013"
	 */
	public boolean isEnabled(Transition t){
		return isEnabled(t.getIndex());
	}

	/**
	 * PetriNet builder. Gets info from PNML file. Call buildPetriNet to get a PetriNet object
	 * @author Ariel I. Rabinovich
	 *
	 */
	public static class PetriNetBuilder {
		
		private PNMLreader reader;
		
		public PetriNetBuilder(String pathToPNML) throws NullPointerException{
			try {
				this.reader = new PNMLreader(pathToPNML);
			} catch (FileNotFoundException | SecurityException e) {
				e.printStackTrace();
			}
		}
		
		public PetriNetBuilder(PNMLreader _reader) throws NullPointerException{
			if(_reader == null){
				throw new NullPointerException("Invalid reader argument");
			}
			this.reader = _reader;
		}
		
		/**
		 * builds and returns the petri described in the PNML file passed to the builder
		 * @return PetriNet object containing info described in PNML file
		 */
		public PetriNet buildPetriNet(){
			
			Quartet<Place[], Transition[], Arc[], Integer[]> petriObjects = PNML2PNObjects();
			Triplet<Integer[][], Integer[][], Integer[][]> petriMatrices = 
					rdpObjects2Matrices(petriObjects.getValue0(), petriObjects.getValue1(), petriObjects.getValue2());
			
			return new PetriNet(petriObjects.getValue0(), petriObjects.getValue1(), petriObjects.getValue2(), petriObjects.getValue3(),
					petriMatrices.getValue0(), petriMatrices.getValue1(), petriMatrices.getValue2());
		}
		
		/**
		 * extracts petri net info from PNML file given as argument and returns a 4-tuple containing
		 * places, transitions, arcs and initial marking
		 * @return a 4-tuple containig (places, transitions, arcs, initial marking)
		 */
		private Quartet<Place[], Transition[], Arc[], Integer[]> PNML2PNObjects(){
			Triplet<Place[], Transition[], Arc[]> ret = reader.parseFileAndGetPetriObjects();
			
			return ret.add(getMarkingFromPlaces(ret.getValue0()));
		}
		
		/**
		 * makes and returns petri net matrices from its objects
		 * @param places petri net's places
		 * @param transitions petri net's transitions
		 * @param arcs petri net's arcs
		 * @return a 3-tuple containing (Pre matrix, Post matrix, Incidence matrix)
		 */
		private Triplet<Integer[][], Integer[][], Integer[][]> rdpObjects2Matrices(Place[] places, Transition[] transitions, Arc[] arcs){
			final int placesAmount = places.length;
			final int transitionsAmount = transitions.length;
			Integer[][] pre = new Integer[placesAmount][transitionsAmount];
			Integer[][] pos = new Integer[placesAmount][transitionsAmount];
			Integer[][] inc = new Integer[placesAmount][transitionsAmount];
			
			for(int i = 0; i < placesAmount; i++){
				for(int j = 0; j < transitionsAmount; j++){
					// Since Integer default value is null
					// I need to fill the matrixes with zeros
					pre[i][j] = 0;
					pos[i][j] = 0;
					inc[i][j] = 0;
				}
			}
			
			for( Arc arc : arcs){
				String arcSource = arc.getId_source();
				String arcTarget = arc.getId_target();
				boolean arcDone = false;
				for(int i = 0; i < placesAmount ; i++){
					if(arcDone){ break;}
					if(arcSource.equals(places[i].getId())){
						for(int j = 0; j < transitionsAmount; j++){
							if(arcTarget.equals(transitions[j].getId())){
								// We don't use the place nor transition index here because there might be some index missing or repeated
								// and that could cause and error
								// e.g: t2 doesn't exist and t5 is the last but it will be on position 4 instead of 5
								pre[i][j] = arc.getWeight();
								arcDone = true;
								break;
							}
						}
					}
				}
				if(arcDone){
					// if I already filled a pre matrix field, I won't fill any pos matrix field with this arc's info
					continue;
				}
				for(int j = 0; j < transitionsAmount; j++){
					if(arcDone){ break;	}
					if(arcSource.equals(transitions[j].getId())){
						for(int i = 0; i < placesAmount; i++){
							if(arcTarget.equals(places[i].getId())){
								pos[i][j] = arc.getWeight();
								arcDone = true;
								break;
							}
						}
					}
				}
			}
			
			// now we have both matrixes pre and pos, let's get inc = pos - pre
			for(int i = 0; i < placesAmount; i++){
				for(int j = 0; j < transitionsAmount; j++){
					inc[i][j] = pos[i][j] - pre[i][j];
				}
			}
			
			return new Triplet<Integer[][], Integer[][], Integer[][]>(pre, pos, inc);
		}
		
		/**
		 * gets initial marking from places array and returns an array containing the markings
		 * @param places the places to check
		 * @return place's initial marking
		 */
		private Integer[] getMarkingFromPlaces(Place[] places){
			ArrayList<Integer> initialMarking = new ArrayList<Integer>(places.length);
			for(Place place : places){
				initialMarking.add(place.getMarking());
			}
			Integer[] ret = new Integer[initialMarking.size()];
			return initialMarking.toArray(ret);
		}
		
	}
}
