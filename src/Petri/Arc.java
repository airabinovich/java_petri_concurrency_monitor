package Petri;

public class Arc {
	
	private String id;
	private String id_source;
	private String id_target;
	private Integer weight;
	
	public Arc(String _id, String _id_source, String _id_target, Integer _weight) throws IllegalArgumentException {
		if(_weight < 1){
			throw new IllegalArgumentException("Arc weight cannot be less that 1");
		}
		this.id = _id;
		this.id_source = _id_source;
		this.id_target = _id_target;
		this.weight = _weight;
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

}
