package Petri;

public class Transition extends PetriNode{
	
	private Label label;
	private TimeSpan interval;
	
	private String guardName;
	private boolean guardEnablingValue;
	
	/**
	 * Constructor for transitions with time and guard
	 * @param _id The transition id
	 * @param _label The transition label object
	 * @param _index The transition index. It must match the petri's column which correspond to this transition 
	 * @param _interval TimeSpan object for the timed transition interval
	 * @param _guardName name for the variable used as guard for this transition. Can be set to null
	 * @param _guardEnablingValue value that enabled the guard
	 * @throws IllegalArgumentException When _index is negative
	 */
	public Transition(String _id, Label _label, int _index, TimeSpan _interval, String _guardName, boolean _guardEnablingValue) throws IllegalArgumentException{
		super(_id, _index);
		this.label = _label;
		this.interval = _interval;
		this.guardName = _guardName == null ? "" : _guardName;
		this.guardEnablingValue = _guardEnablingValue;
	}
	
	/**
	 * Constructor for transitions with guard
	 * @param _id The transition id
	 * @param _label The transition label object
	 * @param _index The transition index. It must match the petri's column which correspond to this transition 
	 * @param _guardName name for the variable used as guard for this transition. Can be set to null
	 * @param _guardEnablingValue value that enabled the guard
	 * @throws IllegalArgumentException When _index is negative
	 */
	public Transition(String _id, Label _label, int _index, String _guardName, boolean _guardEnablingValue) throws IllegalArgumentException{
		this(_id, _label, _index, null, _guardName, _guardEnablingValue);
	}
	
	/**
	 * This constructor is intended for timed transitions only. If this transition is not timed use {@link Transition#Transition(String, Label, int)}
	 * @param _id The transition id
	 * @param _label The transition label object
	 * @param _index The transition index. It must match the petri's column which correspond to this transition 
	 * @param _interval TimeSpan object for the timed transition interval
	 * @throws IllegalArgumentException When _index is negative
	 */
	public Transition(String _id, Label _label, int _index, TimeSpan _interval) throws IllegalArgumentException{
		this(_id, _label, _index, _interval, null, false);
	}
	
	/**
	 * Simple constructor for Transition without time nor guard
	 * @param _id The transition id
	 * @param _label The transition label object
	 * @param _index The transition index. It must match the petri's column which correspond to this transition 
	 * @throws IllegalArgumentException When _index is negative
	 */
	public Transition(String _id, Label _label, int _index) throws IllegalArgumentException{
		this(_id, _label, _index, (TimeSpan) null, null, false);
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
	
	/**
	 * @return the name for this transition's guard. Empty string if none
	 */
	public String getGuardName() {
		return guardName;
	}
	
	/**
	 * @return true if the guard is set by low level
	 */
	public boolean getGuardEnablingValue() {
		return guardEnablingValue;
	}
	
	/**
	 * @return true if the transition has a guard
	 */
	public boolean hasGuard() {
		return !guardName.isEmpty();
	}
}
