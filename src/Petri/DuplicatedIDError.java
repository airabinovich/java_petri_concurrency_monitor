package Petri;

public class DuplicatedIDError extends Error {

	private static final long serialVersionUID = -597629369661776041L;

	public DuplicatedIDError() {
		super("Duplicated id not allowed for places nor transitions");
	}

	public DuplicatedIDError(String message) {
		super(message);
	}

	public DuplicatedIDError(Throwable cause) {
		super(cause);
	}

	public DuplicatedIDError(String message, Throwable cause) {
		super(message, cause);
	}

	public DuplicatedIDError(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
