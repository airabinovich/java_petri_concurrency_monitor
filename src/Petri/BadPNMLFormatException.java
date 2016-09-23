package Petri;

public class BadPNMLFormatException extends Exception {

	private static final long serialVersionUID = -7574500084315352706L;

	public BadPNMLFormatException() {
		super("Bad PNML format");
	}

	public BadPNMLFormatException(String message) {
		super(message);
	}

	public BadPNMLFormatException(Throwable cause) {
		super(cause);
	}

	public BadPNMLFormatException(String message, Throwable cause) {
		super(message, cause);
	}

	public BadPNMLFormatException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
