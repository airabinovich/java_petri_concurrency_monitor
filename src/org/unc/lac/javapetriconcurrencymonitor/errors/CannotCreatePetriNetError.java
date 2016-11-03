package org.unc.lac.javapetriconcurrencymonitor.errors;

/**
 * This error is intended to be thrown when {@link org.unc.lac.javapetriconcurrencymonitor.petrinets.factory.PetriNetFactory} cannot create a PetriNet object
 */
public class CannotCreatePetriNetError extends Error {

	private static final long serialVersionUID = 6784383886670822817L;

	public CannotCreatePetriNetError() {
		super("Error creating petriNet");
	}

	public CannotCreatePetriNetError(String message) {
		super(message);
	}

	public CannotCreatePetriNetError(Throwable cause) {
		super(cause);
	}

	public CannotCreatePetriNetError(String message, Throwable cause) {
		super(message, cause);
	}

	public CannotCreatePetriNetError(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
