package Petri;

public class Transition extends PetriNode{
	
	private Label label;
	private TimeSpan interval;
	
	/**
	 * This constructor is intended por timed transitions only. If this transition is not timed use {@link Transition#Transition(String, Label, int)}
	 * @param _id The transition id
	 * @param _label The transition label object
	 * @param _i The transition index. It must match the petri's column which correspond to this transition 
	 * @param _interval TimeSpan object for the timed transition interval
	 * @throws IllegalArgumentException When _i is negative
	 */
	public Transition(String _id, Label _label, int _i, TimeSpan _interval) throws IllegalArgumentException{
		super(_id, _i);
		this.label = _label;
		this.interval = _interval;
	}
	
	/**
	 * 
	 * @param _id The transition id
	 * @param _label The transition label object
	 * @param _i The transition index. It must match the petri's column which correspond to this transition 
	 * @throws IllegalArgumentException When _i is negative
	 */
	public Transition(String _id, Label _label, int _i) throws IllegalArgumentException{
		this(_id, _label, _i, (TimeSpan) null);
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
