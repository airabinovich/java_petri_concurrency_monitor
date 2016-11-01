package org.lac.javapetriengine.petrinets.components;

public class Arc {
	
	/**
	 * Different types of arc available for petri nets
	 */
	public enum ArcType {
		/** Normal arc. Connects a place to a transition or vice versa.
		 * Its weight is the amount of token drained from or given to a place
		 */
		NORMAL,
		/** Inhibitor arc. Connects a place to a transition but not the other way.
		 * Its weight must be one. When the source place has any tokens, it disables the target transition
		 */
		INHIBITOR,
		/** Reset arc. Connects a place to a transition but not the other way.
		 * When the source place has any token, it enables the target transition.
		 * When the target transition is fired, the arc drains all tokens from source place.
		 * A transition that has a reset arc, must not have any other arc as input.
		 */
		RESET,
		/** Reader arc. Connects a place to a transition but not the other way.
		 * As the standard arc, a source place needs an amount of token equal
		 * or greater than the arc's weight to enable the target transition, but
		 * in this type of arc firing the transition doesn't drain any tokens from the place
		 */
		READ;
		
		private static final String INHIBITOR_STR = "inhibitor";
		private static final String RESET_STR = "reset";
		private static final String READER_STR = "test";
		
		/**
		 * Given a pre-defined string present in PNML files, matches and returns
		 * the corresponding ArcType
		 * @param str The ArcType name taken from PNML file
		 * @return An ArcType enum value matching param str
		 */
		public static ArcType fromString (String str){
			if (str.equalsIgnoreCase(INHIBITOR_STR)){
				return INHIBITOR;
			}
			if (str.equalsIgnoreCase(RESET_STR)){
				return RESET;
			}
			if (str.equalsIgnoreCase(READER_STR)){
				return READ;
			}
			return NORMAL;
		}
		
	};
	
	private String id;
	private PetriNode source;
	private PetriNode target;
	private Integer weight;
	/** Type of arc from {@link ArcType} */
	private ArcType type;
	
	/**
	 * @param _id Unique id to identify the arc
	 * @param _source PetriNode object for arc's source
	 * @param _target PetriNode object for arc's target
	 * @param _weight A positive non-zero integer for the arc's weight
	 * @throws IllegalArgumentException if the specified weight is invalid or if any field is null
	 * @see PetriNode
	 */
	public Arc(String _id, PetriNode _source, PetriNode _target, Integer _weight) throws IllegalArgumentException {
		this(_id, _source, _target, _weight, ArcType.NORMAL);
	}
	
	/**
	 * @param _id Unique id to identify the arc
	 * @param _source PetriNode object for arc's source
	 * @param _target PetriNode object for arc's target
	 * @param _weight A positive non-zero integer for the arc's weight. If _type is {@link ArcType#INHIBITOR}, _weight must be one
	 * @param _type An {@link ArcType} type
	 * @throws IllegalArgumentException if the specified weight is invalid or if any field is null
	 * @see PetriNode 
	 */
	public Arc(String _id, PetriNode _source, PetriNode _target, Integer _weight, ArcType _type) throws IllegalArgumentException{
		if( _id == null || _source == null || _target == null || _weight == null || _type == null){
			throw new IllegalArgumentException("Invalid null parameter recieved");
		}
		if(!isValidWeightForArc(_weight, _type)){
			throw new IllegalArgumentException("Arc weight cannot be less that 1");
		}
		this.id = _id;
		this.source = _source;
		this.target = _target;
		this.weight = _weight;
		this.type = _type;
	}

	/**
	 * @return the arc's weight
	 */
	public Integer getWeight() {
		return weight;
	}

	/**
	 * @return the arc's source
	 */
	public PetriNode getSource() {
		return source;
	}

	/**
	 * @return the arc's target
	 */
	public PetriNode getTarget() {
		return target;
	}

	/**
	 * @return the arc's id
	 */
	public String getId() {
		return id;
	}
	
	/**
	 * @return the arc type
	 */
	public ArcType getType(){
		return type;
	}
	
	/**
	 * Checks if _weight is valid for an arc of type _type
	 * @param _weight an Integer for the weight to test
	 * @param _type the arc type to test
	 * @return True if the weight is valid for the arc type
	 */
	private boolean isValidWeightForArc(Integer _weight, ArcType _type){
		if(type == ArcType.INHIBITOR){
			return _weight == 1;
		}
		
		return _weight > 0;
	}

}
