package org.unc.lac.javapetriconcurrencymonitor.exceptions;

/**
 * Exception used to signal an attempt to fire a timed transition before its timespan has occurred.
 */
public class FiringBeforeTimespanException extends PetriNetException {

	private static final long serialVersionUID = 7870586631036384922L;

	public FiringBeforeTimespanException() {
		super("Firing a timed transition before its timespan was attempted");
	}

	public FiringBeforeTimespanException(String message) {
		super(message);
	}

	public FiringBeforeTimespanException(Throwable cause) {
		super(cause);
	}

	public FiringBeforeTimespanException(String message, Throwable cause) {
		super(message, cause);
	}

	public FiringBeforeTimespanException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
