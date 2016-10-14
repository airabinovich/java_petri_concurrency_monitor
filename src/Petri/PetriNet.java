package Petri;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;

/**
 * Implementation for petri net model.
 * This class describes a general basic petri net.
 * Every special petri net type (timed, colored, stochastic, etc) has to extend this class
 *
 */
public abstract class PetriNet {
	
	protected Place[] places;
	protected Transition[] transitions;
	protected Arc[] arcs;
	protected Integer[][] pre;
	protected Integer[][] post;
	/** Incidece matrix */
	protected Integer[][] inc;
	protected Integer[] currentMarking;
	protected Integer[] initialMarking;
	protected boolean[] automaticTransitions;
	protected boolean[] informedTransitions;
	/** Inhibition matrix used for inhibition logic */
	protected Boolean[][] inhibitionMatrix;
	protected Boolean[][] resetMatrix;
	protected boolean hasInhibitionArcs;
	protected boolean hasResetArcs;
	
	/** HashMap for guards. These variables can enable or disable associated transitions */
	protected HashMap<String, Boolean> guards;
	/**
	 * Makes a PetriNet Object. This is intended to be used by PetriNetFactory
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
		this(_places, _transitions, _arcs, _initialMarking, _preI, _posI, _I, null, null);
	}
	
	/**
	 * Makes a PetriNet Object. This is intended to be used by PetriNetFactory
	 * @param _places Array of Place objects (dimension p)
	 * @param _transitions Array of Transition objects (dimension t)
	 * @param _arcs Array of Arcs
	 * @param _initialMarking Array of Integers (tokens in each place) (dimension p)
	 * @param _preI Pre-Incidence matrix (dimension p*t)
	 * @param _posI Post-Incidence matrix (dimension p*t)
	 * @param _I Incidence matrix (dimension p*t)
	 * @param _inhibitionMatrix Pre-Incidence matrix for inhibition arcs only
	 * @param _resetMatrix Pre-Incidence matrix for reset arcs only
	 */
	protected PetriNet(Place[] _places, Transition[] _transitions, Arc[] _arcs,
			Integer[] _initialMarking, Integer[][] _preI, Integer[][] _posI, Integer[][] _I,
			Boolean[][] _inhibitionMatrix, Boolean[][] _resetMatrix){
		this.places = _places;
		this.transitions = _transitions;
		
		// this sorting allows using indexes to access these arrays and avoid searching for an index
		Arrays.sort(_transitions, (Transition t0, Transition t1) -> t0.getIndex() - t1.getIndex());
		Arrays.sort(_places, (Place p0, Place p1) -> p0.getIndex() - p1.getIndex());
		
		computeAutomaticandInformed();
		fillGuardsMap();
		
		this.arcs = _arcs;
		this.initialMarking = _initialMarking.clone();
		this.currentMarking = _initialMarking;
		this.pre = _preI;
		this.post = _posI;
		this.inc = _I;
		this.inhibitionMatrix = _inhibitionMatrix;
		this.resetMatrix = _resetMatrix;
		hasInhibitionArcs = isMatrixNonZero(inhibitionMatrix);
		hasResetArcs = isMatrixNonZero(resetMatrix);
	}
	
	private void computeAutomaticandInformed() {
		this.automaticTransitions = new boolean[transitions.length];
		this.informedTransitions = new boolean[transitions.length];
		for(int i=0; i<automaticTransitions.length; i++){
			Label thisTransitionLabel = transitions[i].getLabel();
			automaticTransitions[i] = thisTransitionLabel.isAutomatic();
			informedTransitions[i] = thisTransitionLabel.isInformed();
		}
	}
	
	private void fillGuardsMap(){
		if(guards == null){
			guards = new HashMap<String, Boolean>();
		}
		for(Transition t : transitions){
			if(t.hasGuard()){
				// TODO: get initial guards value
				guards.put(t.getGuardName(), false);
			}
		}
	}

	/**
	 * Fires the transition t if it's enabled and updates current marking.
	 * @param t Transition to be fired.
	 * @return true if t was fired.
	 * @throws IllegalArgumentException If t is null or if it doesn't match any transition index
	 */
	public boolean fire(final Transition t) throws IllegalArgumentException{
		if(t == null){
			throw new IllegalArgumentException("Null Transition passed as argument");
		}
		return fire(t.getIndex());
	}

	
	/**
	 * Fires the transition whose index is transitionIndex if it's enabled and updates current marking.
	 * @param transitionIndex Transition's index to be fired.
	 * @return true if transitionIndex was fired. For a perennial fire, returns true in any case.
	 * @throws IllegalArgumentException If transitionIndex is negative or grater than the last transition index.
	 */
	public synchronized boolean fire(int transitionIndex) throws IllegalArgumentException{
		// m_(i+1) = m_i + I*d
		// when d is a vector where every element is 0 but the nth which is 1
		// it's equivalent to pick nth column from Incidence matrix (I) 
		// and add it to the current marking (m_i)
		// and if there is a reset arc, all tokens from its source place are taken.
		if(transitionIndex < 0 || transitionIndex > transitions.length){
			throw new IllegalArgumentException("Invalid transition index: " + transitionIndex);
		}
		if(!isEnabled(transitionIndex)){
			return false;
		}		
		for(int i = 0; i < currentMarking.length; i++){
			if(resetMatrix[i][transitionIndex]){
				currentMarking[i] = 0;
			}
			else {
				currentMarking[i] +=  inc[i][transitionIndex];
			}
			places[i].setMarking(currentMarking[i]);
		}
		return true;
	}
	
	/**
	 * gets the transitions array and evaluates each one if is enabled or not.
	 * @return a boolean array that contains if each transition is enabled or not (true or false)
	 */
	public boolean[] getEnabledTransitions(){
		boolean[] _enabledTransitions = new boolean[transitions.length];
		for(Transition t : transitions){
			_enabledTransitions[t.getIndex()] = isEnabled(t);
		}
		return _enabledTransitions;
	}
	
	public boolean[] getAutomaticTransitions(){
		return automaticTransitions;
	}
	
	public boolean[] getInformedTransitions(){
		return informedTransitions;
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
	 * Checks if the transition whose index is passed is enabled.
	 * Disabling causes:
	 * <li> Feeding places don't meet arcs weights requirements </li>
	 * <li> Guard has different value than required </li>
	 * @return whether the transition is enabled or not
	 */
	public boolean isEnabled(int transitionIndex){
		// I can access that simply because the transitions array is sorted by indexes
		return isEnabled(transitions[transitionIndex]);
	}
	
	/**
	 * Checks if a transition is enabled
	 * @param t Transition objects to check if it's enabled
	 * @return True if the transition is enabled, False otherwise
	 */
	public boolean isEnabled(final Transition t){
		int transitionIndex = t.getIndex();
		boolean enabled = true;
		for(int i=0; i<places.length ; i++){
			if (pre[i][transitionIndex] > currentMarking[i]){
				return false;
			}
		}
		if(t.hasGuard()){
			String guardName = t.getGuardName();
			enabled &= guards.get(guardName).equals(t.getGuardEnablingValue());
		}
		if(hasInhibitionArcs){
			for(int i = 0; i < places.length; i++){
				boolean emptyPlace = places[i].getMarking() == 0;
				boolean placeInhibitsTransition = inhibitionMatrix[i][transitionIndex];
				if(!emptyPlace && placeInhibitsTransition){
					return false;
				}
			}
		}
		if(hasResetArcs){
			for(int i = 0; i < places.length; i++){
				boolean emptyPlace = places[i].getMarking() == 0;
				//resetMatrix should be a binary matrix, so it never should have an element with value grater than 1
				boolean placeResetsTransition = resetMatrix[i][transitionIndex];
				if(placeResetsTransition && emptyPlace){
					return false;
				}
			}
		}
		return enabled;
		
	}
	
	/**
	 * Adds a new guard to the petriNet or updates a guard's value.
	 * Intended only for internal using. Use {@link monitor_petri.MonitorManager#setGuard(String, boolean)} instead 
	 * @param key the guard name
	 * @param value the new value
	 * @return True when succeeded
	 */
	public synchronized boolean addGuard(String key, Boolean value) {
		return guards.put(key, value) != null;
	}
	
	/**
	 * Used to read a guard's value
	 * @param guard Guard name to get its value
	 * @return the specified guard's value
	 * @throws IndexOutOfBoundsException if the guard does not exist
	 */
	public boolean readGuard(String guard) throws IndexOutOfBoundsException {
		try{
			return guards.get(guard).booleanValue();
		} catch (NullPointerException e){
			throw new IndexOutOfBoundsException("No guard registered for " + guard + " name");
		}
	}
	
	/**
	 * @return The amount of guards stored
	 */
	public int getGuardsAmount() {
		return guards.size();
	}
	
	/**
	 * Checks if all elements in the matrix are false.
	 * This is used to know if the petri has the type of arcs described by the matrix semantics.
	 * @param matrix specifies the kind of arcs
	 * @return True if the net has inhibition arcs.
	 */
	protected boolean isMatrixNonZero(Boolean[][] matrix){
		// if the matrix is null or if all elements are zeros
		// the net does not have the type of arcs described by the matrix semantics
		try{
			// this trivial comparison is to throw a NullPointerException if matrix is null
			matrix.equals(matrix);
			boolean allZeros = true;
			for( Boolean[] row : matrix ){
				// if hashset size is 1 all elements are equal
				allZeros &= !row[0] &&
						new HashSet<Boolean>(Arrays.asList(row)).size() == 1;
				if(!allZeros){
					return true;
				}
			}
			return !allZeros;
		} catch (NullPointerException e){
			return false;
		}
	}
	
	
}
