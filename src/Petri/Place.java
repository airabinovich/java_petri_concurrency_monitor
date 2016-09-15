package Petri;

public class Place extends PetriNode{
	
	private int marking;

	public Place(String _id, int _m, int _i) throws IllegalArgumentException{
		super(_id, _i);
		setMarking(_m);
	}
	
	public Place(final Place p){
		super(new String(p.getId()), p.getIndex());
		this.marking = p.getMarking();
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
