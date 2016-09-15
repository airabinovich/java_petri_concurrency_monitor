package Petri;

public class Label {
	
	private boolean automatic;
	private boolean informed;
	
	public Label(boolean au, boolean inf){
		automatic = au;
		informed = inf;
	}

	public boolean isAutomatic() {
		return automatic;
	}

	public boolean isInformed() {
		return informed;
	}
	
	@Override
	public String toString(){
		String isInformed = informed ? "I" : "N";
		String isAutomatic = automatic ? "A" : "D";
		
		return "<" + isAutomatic + "," + isInformed + ">";
	}

}
