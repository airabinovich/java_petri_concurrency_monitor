package org.unc.lac.javapetriconcurrencymonitor.exceptions;

/**
 * Generic petri net related exception. All petri net related exceptions have to extend this class.
 */
public abstract class PetriNetException extends Exception {

	private static final long serialVersionUID = 5765724762433220529L;

	public PetriNetException(){
		super("Exception regarding petri net");
	}
	
	public PetriNetException(String message){
		super(message);
	}
	
	public PetriNetException(Throwable cause) {
		super(cause);
	}

	public PetriNetException(String message, Throwable cause) {
		super(message, cause);
	}
	
	public PetriNetException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
