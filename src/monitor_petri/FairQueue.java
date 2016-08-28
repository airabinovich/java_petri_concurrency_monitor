package monitor_petri;

import java.util.concurrent.Semaphore;

public class FairQueue implements VarCondQueue{
	
	private Semaphore sem;
	
	/**
	 * A fair sleeping queue for threads. 
	 * A call to {@link #sleep() sleep} sends the calling thread to sleep until any threads calls {@link #wakeUp() wakeUp}
	 */
	public FairQueue(){
		sem = new Semaphore(0,true);
	}
	
	public void sleep(){
		try {
			sem.acquire();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} 
	}

	public void wakeUp() {
		if(!isEmpty()){
			sem.release();
		}
	}

	
	public boolean isEmpty() {
		return !sem.hasQueuedThreads();
	}

	@Override
	public int getSize() {
		return sem.getQueueLength();
	}
}
