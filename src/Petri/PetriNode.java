package Petri;

/**
 * A PetriNode is a point connectable by an arc, thus it must be implemented by Place and Transition
 * This interface generalizes a few methods needed by a point that has to be an origin or destination for an arc
 */
public abstract class PetriNode {
	
	protected String id;
	protected int index;
	
	public PetriNode(String _id, int _index){
		this.id = _id;
		this.index = _index;
	}
	
	/** Id used to identify univocally the node */
	public String getId(){
		return this.id;
	};
	
	/** Index used either as row or column index for the petri matrix */
	public int getIndex(){
		return this.index;
	}
	
	/**
	 * Set the id for this PetriNode.
	 * This should be used before creating the petri matrices and during PNML parsing
	 * to avoid mismatching index and column/row when some index is missing in the PNML
	 * @param _index the new index. Must be positive
	 * @throws IllegalArgumentException when _index is negative
	 */
	public void setIndex(int _index) throws IllegalArgumentException {
		if(_index < 0){
			throw new IllegalArgumentException("index cannot be negative");
		}
		
		this.index = _index;
	}

}
