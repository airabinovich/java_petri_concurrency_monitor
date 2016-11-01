package org.lac.javapetriengine.exceptions;

public class NotInitializedPetriNetException extends Exception {

	private static final long serialVersionUID = -7078076102280257966L;

	public NotInitializedPetriNetException(){
		super("The Petri Net is not initialized");
	}
	
	public NotInitializedPetriNetException(String message){
		super(message);
	}
	
	public NotInitializedPetriNetException(Throwable cause) {
		super(cause);
	}

	public NotInitializedPetriNetException(String message, Throwable cause) {
		super(message, cause);
	}
	
	public NotInitializedPetriNetException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}
}