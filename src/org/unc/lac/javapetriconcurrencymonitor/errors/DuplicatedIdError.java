package org.unc.lac.javapetriconcurrencymonitor.errors;

public class DuplicatedIdError extends Error {

	private static final long serialVersionUID = -597629369661776041L;

	public DuplicatedIdError() {
		super("Duplicated id not allowed for places nor transitions");
	}

	public DuplicatedIdError(String message) {
		super(message);
	}

	public DuplicatedIdError(Throwable cause) {
		super(cause);
	}

	public DuplicatedIdError(String message, Throwable cause) {
		super(message, cause);
	}

	public DuplicatedIdError(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
