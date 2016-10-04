package Petri;

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
		/** Reader arc. Connects a place to a transition but not the other way.
		 * As the standard arc, a source place has no have an amount of token equal
		 * or greater than the arc's weight to enable the target transition, but
		 * in this type of arc the transition fire doesn't drain any tokens from the place
		 */
		READ;
		
		private static final String INHIBITOR_STR = "inhibitor";
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
			if (str.equalsIgnoreCase(READER_STR)){
				return READ;
			}
			return NORMAL;
		}
		
	};
	
	private String id;
	private String id_source;
	private String id_target;
	private Integer weight;
	/** Type of arc from {@link ArcType} */
	private ArcType type;
	
	/**
	 * @param _id Unique id to identify the arc
	 * @param _id_source Unique id matching the arc's source
	 * @param _id_target Unique id matching the arc's target
	 * @param _weight A positive non-zero integer for the arc's weight
	 * @throws IllegalArgumentException if the specified weight is less than one or if any field is null
	 */
	public Arc(String _id, String _id_source, String _id_target, Integer _weight) throws IllegalArgumentException {
		this(_id, _id_source, _id_target, _weight, ArcType.NORMAL);
	}
	
	/**
	 * @param _id Unique id to identify the arc
	 * @param _id_source Unique id matching the arc's source
	 * @param _id_target Unique id matching the arc's target
	 * @param _weight A positive non-zero integer for the arc's weight. If _type is {@link ArcType#INHIBITOR}, _weight must be one
	 * @param _type An {@link ArcType} type
	 * @throws IllegalArgumentException if the specified weight is invalid or if any field is null 
	 */
	public Arc(String _id, String _id_source, String _id_target, Integer _weight, ArcType _type) throws IllegalArgumentException{
		if( _id == null || _id_source == null || _id_target == null || _weight == null || _type == null){
			throw new IllegalArgumentException("Invalid null parameter recieved");
		}
		if(!isValidWeightForArc(_weight, _type)){
			throw new IllegalArgumentException("Arc weight cannot be less that 1");
		}
		this.id = _id;
		this.id_source = _id_source;
		this.id_target = _id_target;
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
	 * @return the arc's source's id
	 */
	public String getId_source() {
		return id_source;
	}

	/**
	 * @return the arc's target's id
	 */
	public String getId_target() {
		return id_target;
	}

	/**
	 * @return the arc's id
	 */
	public String getId() {
		return id;
	}
	
	public ArcType getType(){
		return type;
	}
	
	private boolean isValidWeightForArc(Integer _weight, ArcType _type){
		if(type == ArcType.INHIBITOR){
			return _weight == 1;
		}
		
		return _weight > 0;
	}

}
