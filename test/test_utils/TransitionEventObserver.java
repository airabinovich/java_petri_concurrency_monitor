package test_utils;

import java.util.ArrayList;

import rx.Observer;

/**
 * An observer that receive and store string events
 * Made for testing purposes
 *
 */
public class TransitionEventObserver implements Observer<String> {

	/**
	 * A buffer for the recieved events
	 */
	private ArrayList<String> eventsRecieved;
	
	/**
	 * 
	 */
	public TransitionEventObserver() {
		super();
		eventsRecieved = new ArrayList<String>();
	}

	/**
	 * @see rx.Observer#onCompleted()
	 */
	@Override
	public void onCompleted() {
		eventsRecieved.add("COMPLETED");
	}

	/**
	 * @see rx.Observer#onError(java.lang.Throwable)
	 */
	@Override
	public void onError(Throwable t) {
		eventsRecieved.add("ERROR: " + t.getMessage() + " of type " + t.getClass().getName());
	}

	/**
	 * @see rx.Observer#onNext(java.lang.Object)
	 */
	@Override
	public void onNext(String event) {
		eventsRecieved.add(event);
	}
	
	/**
	 * Getter for the events buffer
	 * @return the events recieved
	 */
	public ArrayList<String> getEvents(){
		return eventsRecieved;
	}

}
