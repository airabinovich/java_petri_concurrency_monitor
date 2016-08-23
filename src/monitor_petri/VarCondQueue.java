package monitor_petri;

public interface VarCondQueue{

	/**
	 * Sends the calling thread to sleep
	 */
	public void sleep();
	
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
