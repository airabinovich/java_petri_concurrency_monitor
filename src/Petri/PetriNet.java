package Petri;

import java.util.Arrays;
import java.util.HashMap;

public abstract class PetriNet {
	
	protected Place[] places;
	protected Transition[] transitions;
	protected Arc[] arcs;
	protected Integer[][] pre;
	protected Integer[][] post;
	protected Integer[][] inc;
	protected Integer[] currentMarking;
	protected Integer[] initialMarking;
	protected boolean[] automaticTransitions;
	protected boolean[] informedTransitions;
	
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
	 * Fires the transition t if it's enabled and updates current marking
	 * @param t Transition to be fired
	 * @return true if t was fired
	 */
	public boolean fire(Transition t) {
		return fire(t.getIndex());
	}
	
	/**
	 * Fires the transition whose index if transitionIndex if it's enabled and updates current marking
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
	 * @return whether the transition is enabled or not
	 */
	public boolean isEnabled(Transition t){
		int transitionIndex = t.getIndex();
		boolean enabled = true;
		for(int i=0; i<places.length ; i++){
			if (pre[i][transitionIndex] > currentMarking[i]){
				enabled = false;
				break;
			}
		}
		if(t.hasGuard()){
			String guardName = t.getGuardName();
			enabled &= guards.get(guardName).equals(t.getGuardEnablingValue());
		}
		return enabled;
		
	}
	
	/**
	 * Adds a new guard to the petriNet or updates a guard's value
	 * @param key the guard name
	 * @param value the new value
	 */
	public synchronized void addGuard(String key, Boolean value) {
		guards.put(key, value);
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
	
}
