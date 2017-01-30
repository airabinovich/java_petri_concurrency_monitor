package org.unc.lac.javapetriconcurrencymonitor.petrinets;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Optional;

import org.unc.lac.javapetriconcurrencymonitor.exceptions.NotInitializedPetriNetException;
import org.unc.lac.javapetriconcurrencymonitor.exceptions.PetriNetException;
import org.unc.lac.javapetriconcurrencymonitor.petrinets.components.Arc;
import org.unc.lac.javapetriconcurrencymonitor.petrinets.components.Label;
import org.unc.lac.javapetriconcurrencymonitor.petrinets.components.PetriNode;
import org.unc.lac.javapetriconcurrencymonitor.petrinets.components.Place;
import org.unc.lac.javapetriconcurrencymonitor.petrinets.components.Transition;

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
	protected boolean[] enabledTransitions;
	
	/** Inhibition arcs pre-incidence matrix */
	protected Boolean[][] inhibitionMatrix;
	/** Reset arcs pre-incidence matrix */
	protected Boolean[][] resetMatrix;
	/** Reader arcs pre-incidence matrix */
	protected Integer[][] readerMatrix;
	
	protected boolean hasInhibitionArcs;
	protected boolean hasResetArcs;
	protected boolean hasReaderArcs;
	
	
	protected boolean initializedPetriNet;
	
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
	 * @param _inhibitionMatrix Pre-Incidence matrix for inhibition arcs only. If no inhibition arcs, null is accepted.
	 * @param _resetMatrix Pre-Incidence matrix for reset arcs only. If no reset arcs, null is accepted.
	 * @param _readerMatrix Pre-Incidence matrix for reader arcs only. If no reader arcs, null is accepted.
	 */
	protected PetriNet(Place[] _places, Transition[] _transitions, Arc[] _arcs,
			Integer[] _initialMarking, Integer[][] _preI, Integer[][] _posI, Integer[][] _I,
			Boolean[][] _inhibitionMatrix, Boolean[][] _resetMatrix, Integer[][] _readerMatrix){
		this.places = _places;
		this.transitions = _transitions;
		
		// this sorting allows using indexes to access these arrays and avoid searching for an index
		Arrays.sort(_transitions, (Transition t0, Transition t1) -> t0.getIndex() - t1.getIndex());
		Arrays.sort(_places, (Place p0, Place p1) -> p0.getIndex() - p1.getIndex());
		
		computeAutomaticAndInformed();
		fillGuardsMap();
		
		this.arcs = _arcs;
		this.initialMarking = _initialMarking.clone();
		this.currentMarking = _initialMarking;
		this.pre = _preI;
		this.post = _posI;
		this.inc = _I;
		this.inhibitionMatrix = _inhibitionMatrix;
		this.resetMatrix = _resetMatrix;
		this.readerMatrix = _readerMatrix;
		hasInhibitionArcs = isMatrixNonZero(inhibitionMatrix);
		hasResetArcs = isMatrixNonZero(resetMatrix);
		hasReaderArcs = isMatrixNonZero(readerMatrix);
	}
	
	/**
	 * Compute all enabled transitions according to each particular net's requirements
	 * @return An array where true means that transition is enabled
	 */
	protected abstract boolean[] computeEnabledTransitions();
	
	/**
	 * Initialize the petri net and computes enabled transitions for the first time.
	 * This method must be called before being ready to fire a transition.
	 * @see PetriNet#computeEnabledTransitions()
	 */
	public void initializePetriNet(){
		enabledTransitions = computeEnabledTransitions();
		initializedPetriNet = true;
	}
	
	private void computeAutomaticAndInformed() {
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
	 * Fires the transition whose index is given as argument if it's enabled and updates current marking.
	 * @param transitionIndex Transition's index to be fired.
	 * @return true if t was fired.
	 * @throws IllegalArgumentException If transitionIndex is negative or greater than the last transition index.
	 * @throws PetriNetException If an error regarding the petri occurs, for instance if the net hasn't been initialized before calling this method.
	 */
	public PetriNetFireOutcome fire(int transitionIndex) throws IllegalArgumentException, PetriNetException{
		if(transitionIndex < 0 || transitionIndex > transitions.length){
			throw new IllegalArgumentException("Invalid transition index: " + transitionIndex);
		}
		return fire(transitions[transitionIndex]);
	}

	/**
	 * Fires the transition given as argument if it's enabled and updates current marking.
	 * @param transition Transition to be fired.
	 * @return true if transition was fired.
	 * @throws IllegalArgumentException If transition is null or if it doesn't match any transition index
	 * @throws NotInitializedPetriNetException If the net hasn't been initialized before calling this method
	 * @throws PetriNetException If an error regarding the petri occurs, for instance if the net hasn't been initialized before calling this method.
	 */
	public synchronized PetriNetFireOutcome fire(final Transition transition) throws IllegalArgumentException, NotInitializedPetriNetException, PetriNetException {
		// m_(i+1) = m_i + I*d
		// when d is a vector where every element is 0 but the nth which is 1
		// it's equivalent to pick nth column from Incidence matrix (I) 
		// and add it to the current marking (m_i)
		// and if there is a reset arc, all tokens from its source place are taken.
		if(transition == null){
			throw new IllegalArgumentException("Null Transition passed as argument");
		}
		if(!initializedPetriNet){
			throw new NotInitializedPetriNetException();
		}
		
		int transitionIndex = transition.getIndex();
		
		if(transitionIndex < 0 || transitionIndex > transitions.length){
			throw new IllegalArgumentException("Index " + transitionIndex + " doesn't match any transition's index in this petri net");
		}
		
		if(!isEnabled(transition)){
			return PetriNetFireOutcome.NOT_ENABLED;
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
		
		enabledTransitions = computeEnabledTransitions();
		
		return PetriNetFireOutcome.SUCCESS;
	}
	
	/**
	 * gets the transitions array and evaluates each one if is enabled or not.
	 * @return a boolean array that contains if each transition is enabled or not (true or false)
	 */
	public boolean[] getEnabledTransitions(){
		return enabledTransitions;
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
	 * @param placeName The name of the place to find.
	 * @throws IllegalArgumentException If no place matches placeName
	 * @return A copy of the place whose name is placeName
	 */
	public Place getPlace(final String placeName) throws IllegalArgumentException{
		return getPetriNode(placeName, Place.class);
	}
	
	/**
	 * @return the transitions
	 */
	public Transition[] getTransitions() {
		return transitions;
	}
	
	/**
	 * Looks for a transition whose name matches transitionName and returns it.
	 * If it doesn't exist, {@link IllegalArgumentException} is thrown
	 * @param transitionName The name of the transition to look for
	 * @return The tansition found
	 * @throws IllegalArgumentException if transitionName doesn't match any transition
	 */
	public Transition getTransition(final String transitionName) throws IllegalArgumentException{
		return getPetriNode(transitionName, Transition.class);
	}
	
	/**
	 * Looks for a {@link Place} or {@link Transition} that matches petriNodeName name.
	 * The second parameter is the class required for the call query. This can be either {@link Transition}.class or {@link Place}.class.
	 * @param petriNodeName The name of the Place or Transition to look for.
	 * @param _class The class to use as return type. i.e. {@link Transition} or {@link Place}.
	 * @return A place or transition matching the given name.
	 * @throws IllegalArgumentException If the given name is null, the given class is not {@link Transition} nor {@link Place} or if there isn't a match for the name.
	 */
	@SuppressWarnings("unchecked")
	private <E extends PetriNode> E getPetriNode(String petriNodeName, Class<E> _class) throws IllegalArgumentException{
		E[] arrayToFilter = null;
		if(petriNodeName == null){
			throw new IllegalArgumentException("Null name not supported");
		}
		
		if(_class == Transition.class){
			arrayToFilter = (E[]) transitions;
		}
		else if (_class == Place.class){
			arrayToFilter = (E[]) places;
		}
		else {
			throw new IllegalArgumentException("Method not supported for class " + _class.getName());
		}
		
		Optional<E> filteredPetriNode = Arrays.stream(arrayToFilter)
				.filter((E element) -> element.getName().equals(petriNodeName))
				.findFirst();
		
		// if there is a matching argument return it, else throw an exception
		return filteredPetriNode.orElseThrow(() -> new IllegalArgumentException("No " + _class.getSimpleName().toLowerCase() + " matches the name " + petriNodeName));
		
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
	 * @return True if the petri net is initialized
	 */
	public boolean isInitialized(){
		return initializedPetriNet;
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
		for(int i=0; i<places.length ; i++){
			if (pre[i][transitionIndex] > currentMarking[i]){
				return false;
			}
		}
		if(t.hasGuard()){
			String guardName = t.getGuardName();
			Boolean guardValue = guards.get(guardName);
			if(!guardValue.equals(t.getGuardEnablingValue())){
				return false;
			}
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
		if(hasReaderArcs){
			for(int i=0; i<places.length ; i++){
				if (readerMatrix[i][transitionIndex] > currentMarking[i]){
					return false;
				}
			}
		}
		return true;
		
	}
	
	/**
	 * Adds a new guard to the petriNet or updates a guard's value.
	 * Intended only for internal using. Use {@link org.unc.lac.javapetriconcurrencymonitor.monitor.PetriMonitor#setGuard(String, boolean)} instead 
	 * @param key the guard name
	 * @param value the new value
	 * @return True when succeeded
	 */
	public synchronized boolean addGuard(String key, Boolean value) {
		boolean success = guards.put(key, value) != null;
		
		enabledTransitions = computeEnabledTransitions();
		
		return success;
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
	 * @return True if the matrix doesn't have all entries as false.
	 */
	protected boolean isMatrixNonZero(Boolean[][] matrix){
		// if the matrix is null or if all elements are zeros
		// the net does not have the type of arcs described by the matrix semantics
		try{
			// this trivial comparison is to throw a NullPointerException if matrix is null
			matrix.equals(matrix);
			boolean allFalse = true;
			for( Boolean[] row : matrix ){
				// if hashset size is 1 all elements are equal
				allFalse &= !row[0] &&
						new HashSet<Boolean>(Arrays.asList(row)).size() == 1;
				if(!allFalse){
					return true;
				}
			}
			return !allFalse;
		} catch (NullPointerException e){
			return false;
		}
	}
	
	/**
	 * Checks if all elements in the matrix are zero.
	 * This is used to know if the petri has the type of arcs described by the matrix semantics.
	 * @param matrix specifies the kind of arcs
	 * @return True if the matrix is not all zeros.
	 */
	protected boolean isMatrixNonZero(Integer[][] matrix){
		// if the matrix is null or if all elements are zeros
		// the net does not have the type of arcs described by the matrix semantics
		try{
			// this trivial comparison is to throw a NullPointerException if matrix is null
			matrix.equals(matrix);
			boolean allZeros = true;
			for( Integer[] row : matrix ){
				// if hashset size is 1 all elements are equal
				allZeros &= row[0] == 0 &&
						new HashSet<Integer>(Arrays.asList(row)).size() == 1;
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
