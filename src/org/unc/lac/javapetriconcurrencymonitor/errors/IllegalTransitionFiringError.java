package org.unc.lac.javapetriconcurrencymonitor.errors;

/**
 * This error should be thrown when an illegal transition firing
 * has tries to occur and the application cannot recover from this
 * i.e: A thread tried to fire an automatic transition
 *
 */
public class IllegalTransitionFiringError extends Error {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4560536609740420888L;

	private final static String defaultMessage= "An illegal transition firing has tried to occur";
	
	public IllegalTransitionFiringError() {
		super(defaultMessage);
	}
	
	/**
	 * @param message message to be thrown with the error
	 */
	public IllegalTransitionFiringError(String message) {
		super(message);
	}

}
