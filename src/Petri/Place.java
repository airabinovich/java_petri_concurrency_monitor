package Petri;

public class Place {
	
	private int index;
	private String id;
	private int marking;

	public Place(String _id, int _m, int _i) throws IllegalArgumentException{
		this.id = _id;
		setMarking(_m);
		this.index = _i;
	}
	
	public Place(final Place p){
		this.id = new String(p.getId());
		this.marking = p.getMarking();
		this.index = p.getIndex();
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
	/**
	 * @return the place's id
	 */
	public String getId() {
		return id;
	}

	/**
	 * @return the place's index
	 */
	public int getIndex() {
		return index;
	}

}
