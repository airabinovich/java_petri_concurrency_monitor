package Petri;

public class Transition{
	
	private int index;
	private String id;
	private Label label;
	
	public Transition(String _id, Label _label, int _i){
		this.label = _label;
		this.id = _id;
		this.index = _i;
	}
	
	/**
	 * @return the transition's label
	 */
	public Label getLabel() {
		return label;
	}
	
	/**
	 * @return the transition's id
	 */
	public String getId() {
		return id;
	}
	
	
	/**
	 * @return the transition's index
	 */
	public int getIndex() {
		return index;
	}
}
