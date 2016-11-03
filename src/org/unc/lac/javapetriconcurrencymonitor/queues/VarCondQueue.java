package org.unc.lac.javapetriconcurrencymonitor.queues;

public interface VarCondQueue{

	/**
	 * Sends the calling thread to sleep
	 */
	public void sleep();
	
	/**
	 * Sends the calling thread to sleep with the guarantee
	 * that a {@link #wakeUp()} call will signal it before any other
	 * thread who called {@link #sleep()}
	 */
	public void sleepWithHighPriority();
	
	/**
	 * Wakes a sleeping thread up if there is any.
	 * The woken thread is the one that has been waiting the longer
	 */
	public void wakeUp();
	
	/**
	 * @return whether the queue has any sleeping thread
	 */
	public boolean isEmpty();
	
	/**
	 * @return the amount of threads sleeping in queue 
	 */
	public int getSize();

}
