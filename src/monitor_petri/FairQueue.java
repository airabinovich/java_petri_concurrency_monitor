package monitor_petri;

import monitor_petri.PriorityBinaryLock.LockPriority;

public class FairQueue implements VarCondQueue{
	
	private PriorityBinaryLock lock;
	
	/**
	 * A fair sleeping queue for threads. 
	 * A call to {@link #sleep() sleep} sends the calling thread to sleep until any threads calls {@link #wakeUp() wakeUp}
	 */
	public FairQueue(){
		lock = new PriorityBinaryLock();
		// take the lock so no thread calling sleep can actually take it and continue
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
	
	public int getHighPriorityThreadsSLeeping(){
		return (int) (lock.getHighPriorityQueueLength());
	}
	
	public int getLowPriorityThreadsSLeeping(){
		return (int) (lock.getLowPriorityQueueLength());
	}

}
