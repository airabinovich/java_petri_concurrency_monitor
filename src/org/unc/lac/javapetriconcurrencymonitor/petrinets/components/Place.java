package org.unc.lac.javapetriconcurrencymonitor.petrinets.components;

public class Place extends PetriNode{
	
	private int marking;

	/**
	 * Constructs a place object mathcing arguments if valid
	 * @param _id A unique id to identify univoquely the place
	 * @param _m Initial marking
	 * @param _i Index for the petri matrices
	 * @param _name User-defined name. Empty not allowed
	 * @throws IllegalArgumentException if marking is negative or if name is invalid
	 */
	public Place(String _id, int _m, int _i, String _name) throws IllegalArgumentException{
		super(_id, _i, _name);
		setMarking(_m);
	}
	
	/**
	 * Constructs a copy of the given place
	 * @param p A place object to copy
	 */
	public Place(final Place p){
		this(p.getId(), p.getMarking(), p.getIndex(), p.getName());
	}
	
	/**
	 * @return the place's current marking
	 */
	public int getMarking() {
		return marking;
	}
	
	/**
	 * Sets a new marking to the place. If new marking is less than 0 throws IllegalArgumentException
	 * @param _marking the new marking o set
	 * @throws IllegalArgumentException
	 */
	public void setMarking(int _marking) throws IllegalArgumentException {
		if (_marking < 0){
			throw new IllegalArgumentException("Negative marking is not allowed");
		}
		this.marking = _marking;
	}

}
