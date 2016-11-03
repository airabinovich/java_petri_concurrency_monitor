package org.unc.lac.javapetriconcurrencymonitor.exceptions;

public class BadPnmlFormatException extends Exception {

	private static final long serialVersionUID = -7574500084315352706L;

	public BadPnmlFormatException() {
		super("Bad PNML format");
	}

	public BadPnmlFormatException(String message) {
		super(message);
	}

	public BadPnmlFormatException(Throwable cause) {
		super(cause);
	}

	public BadPnmlFormatException(String message, Throwable cause) {
		super(message, cause);
	}

	public BadPnmlFormatException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
