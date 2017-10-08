package org.lac.javapetriconcurrencymonitor.test.utils;

import java.util.ArrayList;
import java.util.List;

import rx.Observer;

/**
 * An observer who receives and stores string events.
 * Made for testing purposes
 *
 */
public class TransitionEventObserver implements Observer<String> {

	/**
	 * A buffer for the recieved events
	 */
	private List<String> eventsRecieved;
	
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
	public List<String> getEvents(){
		return eventsRecieved;
	}

}
