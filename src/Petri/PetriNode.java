package Petri;

/**
 * Since a PetriNet is a bipartite graph, a PetriNode is a node connectable by an arc.
 * Therefore PetriNode must be extended by Place and Transition.
 * This class generalizes a few methods needed by a a node of that graph.
 */
public abstract class PetriNode {
	
	protected String id;
	protected int index;
	
	public PetriNode(String _id, int _index) throws IllegalArgumentException{
		this.id = _id;
		setIndex(_index);
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
