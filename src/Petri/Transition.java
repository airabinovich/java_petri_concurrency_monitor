package Petri;

public class Transition extends PetriNode{
	
	private Label label;
	private TimeSpan interval;
	
	public Transition(String _id, Label _label, int _i, TimeSpan _interval){
		super(_id, _i);
		this.label = _label;
		this.interval = _interval;
	}
	
	/**
	 * @return the transition's label
	 */
	public Label getLabel() {
		return label;
	}
	
	/**
	 * @return the transition's interval
	 */
	public TimeSpan getTimeSpan() {
		return interval;
	}
}
