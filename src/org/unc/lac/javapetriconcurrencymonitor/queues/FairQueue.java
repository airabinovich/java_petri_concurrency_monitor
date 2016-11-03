package org.unc.lac.javapetriconcurrencymonitor.queues;

import org.unc.lac.javapetriconcurrencymonitor.utils.PriorityBinaryLock;
import org.unc.lac.javapetriconcurrencymonitor.utils.PriorityBinaryLock.LockPriority;

public class FairQueue implements VarCondQueue{
	
	private PriorityBinaryLock lock;
	
	/**
	 * A fair sleeping queue for threads. 
	 * A call to {@link #sleep() sleep} sends the calling thread to sleep until any threads calls {@link #wakeUp() wakeUp}
	 */
	public FairQueue(){
		lock = new PriorityBinaryLock();
		// take the lock so no permissions are available
		// and when a thread calls sleep() (or sleepWithHighPriority())
		// it can't acquire the lock and continue, and must sleep
		lock.lock();
	}
	
	public void sleep(){
		lock.lock();
	}
	
	public void sleepWithHighPriority() {
		lock.lock(LockPriority.HIGH);
	}

	public void wakeUp() {
		if(!isEmpty()){
			lock.unlock();
		}
	}

	
	public boolean isEmpty() {
		return !lock.hasQueuedThreads();
	}

	@Override
	public int getSize() {
		return (int) (lock.getHighPriorityQueueLength() + lock.getLowPriorityQueueLength());
	}
	
	public int getHighPriorityThreadsSleeping(){
		return (int) (lock.getHighPriorityQueueLength());
	}
	
	public int getLowPriorityThreadsSleeping(){
		return (int) (lock.getLowPriorityQueueLength());
	}

}
