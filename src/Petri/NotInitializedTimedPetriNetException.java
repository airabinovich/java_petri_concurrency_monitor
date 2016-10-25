package Petri;

public class NotInitializedTimedPetriNetException extends Exception {
	
	private static final long serialVersionUID = 5988729711337034401L;

	public NotInitializedTimedPetriNetException(){
		super("The Timed Petri Net is not initialized");
	}
	
	public NotInitializedTimedPetriNetException(String message){
		super(message);
	}
	
	public NotInitializedTimedPetriNetException(Throwable cause) {
		super(cause);
	}

	public NotInitializedTimedPetriNetException(String message, Throwable cause) {
		super(message, cause);
	}
	
	public NotInitializedTimedPetriNetException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}
}