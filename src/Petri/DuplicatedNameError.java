package Petri;

public class DuplicatedNameError extends Error {

	private static final long serialVersionUID = -625306281548494799L;

	public DuplicatedNameError() {
		super("Duplicated name not allowed for places nor transitions");
	}

	public DuplicatedNameError(String message) {
		super(message);
	}

	public DuplicatedNameError(Throwable cause) {
		super(cause);
	}

	public DuplicatedNameError(String message, Throwable cause) {
		super(message, cause);
	}

	public DuplicatedNameError(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
