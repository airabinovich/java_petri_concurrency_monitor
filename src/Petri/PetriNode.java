package Petri;

/**
 * Since a PetriNet is a bipartite graph, a PetriNode is a node connectable by an arc.
 * Therefore PetriNode must be extended by Place and Transition.
 * This class generalizes a few methods needed by a a node of that graph.
 */
public abstract class PetriNode {
	
	protected String id;
	protected String name;
	protected int index;
	
	public PetriNode(String _id, int _index, String _name) throws IllegalArgumentException{
		if(_name == null || _name.isEmpty()){
			throw new IllegalArgumentException("Invalid " + this.getClass().getSimpleName() + " name: " + _name );
		}
		this.id = _id;
		setIndex(_index);
		this.name = _name;
	}
	
	/** 
	 * @return Id used to identify univocally the node
	 */
	public String getId(){
		return this.id;
	};
	
	/** 
	 * @return Index used either as row or column index for the petri matrixes
	 */
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
	
	/**
	 * @return Get the custom name given by the petri net creator
	 */
	public String getName(){
		return this.name;
	}

}
