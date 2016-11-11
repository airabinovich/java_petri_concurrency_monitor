package org.unc.lac.javapetriconcurrencymonitor.exceptions;

/**
 * Exception used to signal an attempt to fire a timed transition after its timespan has occurred.
 */
public class FiringAfterTimespanException extends Exception {

	private static final long serialVersionUID = -5507033074544187737L;

	public FiringAfterTimespanException() {
		super("Firing a timed transition after its timespan was attempted");
	}

	public FiringAfterTimespanException(String message) {
		super(message);
	}

	public FiringAfterTimespanException(Throwable cause) {
		super(cause);
	}

	public FiringAfterTimespanException(String message, Throwable cause) {
		super(message, cause);
	}

	public FiringAfterTimespanException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
