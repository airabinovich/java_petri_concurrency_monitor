package org.lac.javapetriengine.utils;

import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.LockSupport;

/**
 * A lock with two priority levels.
 * There is only one permit available, thus the 'binary' in the class' name.
 * Each lock() blocks if necessary until the lock is available, and then takes it.
 * Each unlock() releases the lock, potentially waking a blocking acquirer.
 * 
 * The special feature in this lock is that a {@link #lock(LockPriority)} call may be with high or low priority,
 * and a {@link #unlock()} call prioritizes a high priority caller.
 * That is, a thread sleeping with high priority will be released before a low priority one.
 * 
 * Among threads of the same priority level, the lock's queue behaves as a FIFO queue.
 *
 * @see LockPriority
 */
public class PriorityBinaryLock {

	/** Atomic boolean object to take register of the lock and guarantee no concurrence errors may allow two threads to take it */
	private final AtomicBoolean locked = new AtomicBoolean(false);
	
	/** 
	 * The container to organize the sleeping threads.
	 * This is ordered by priority and by arrival time among same priority level threads
	 */
	private final PriorityBlockingQueue<PrioritizedThread> queue = new PriorityBlockingQueue<PrioritizedThread>();
	
	/** A counter for low priority sleeping threads */
	private AtomicLong lowPriorityThreadsSleeping = new AtomicLong(0);
	/** A counter for high priority sleeping threads */
	private AtomicLong highPriorityThreadsSleeping = new AtomicLong(0); 
	
	/**
	 * Available priorities for {@link PriorityBinaryLock}
	 */
	public enum LockPriority{
		HIGH,
		LOW
	}
	
	/**
	 * Creates a PriorityBinaryLock.
	 */
	public PriorityBinaryLock() {
	}
	
	/**
	 * Tries to lock this PriorityLock with low priority.
	 * Takes the lock if available and returns immediately,
	 * else the current thread becomes disabled for thread scheduling purposes and lies dormant until
	 * some other thread invokes the {@link #unlock()} method for this lock
	 * and the current thread is next in line.
	 */
	public void lock(){
		lock(LockPriority.LOW);
	}
	
	/**
	 * Tries to lock this PriorityLock with the specified priority.
	 * Takes the lock if available and returns immediately,
	 * else the current thread becomes disabled for thread scheduling purposes and lies dormant until
	 * some other thread invokes the {@link #unlock()} method for this lock
	 * and the current thread is next in line.
	 */
	public void lock(LockPriority priority){

		PrioritizedThread entry = new PrioritizedThread(Thread.currentThread(), priority);
		queue.add(entry);

		switch(priority){
		case HIGH:
			highPriorityThreadsSleeping.incrementAndGet();
			break;
		case LOW:
			lowPriorityThreadsSleeping.incrementAndGet();
			break;
		}
		// Block while not first in queue or cannot acquire lock
		while(queue.peek() != entry || !locked.compareAndSet(false, true)){

			LockSupport.park(this); // disable the current thread with the semaphore as permit
			// this is inside a while just in case. It's recommended
		}
		
		// once the lock is taken by the current thread, remove it from the queue
		queue.remove(entry);
		
		switch(priority){
		case HIGH:
			highPriorityThreadsSleeping.decrementAndGet();
			break;
		case LOW:
			lowPriorityThreadsSleeping.decrementAndGet();
			break;
		}
	}
	
	/**
	 * Releases this lock.
	 * If there's any thread waiting for it, wake it up.
	 * Which thread is to be woken is {@link #queue}'s responsibility.
	 */
	public void unlock() {
		locked.set(false);
		// wakes the first thread in priority order if any
		try{
			LockSupport.unpark(queue.peek().getThread());
		} catch (NullPointerException e){
			// queue was empty, no problem
		}
		// if the queue is empty, no thread tried to acquire the lock
		// while the current thread had the lock
	}
	

	
	/**
	 * Returns the length of the low priority queue
	 * @return the number of threads waiting for this lock with low priority
	 */
	public long getLowPriorityQueueLength(){
		return lowPriorityThreadsSleeping.get();
	}
	/**
	 * Returns the length of the high priority queue
	 * @return the number of threads waiting for this lock with high priority
	 */
	public long getHighPriorityQueueLength(){
		return highPriorityThreadsSleeping.get();
	}
	
	/**
	 * Tests the lock and informs if it's taken
	 * @return True if the lock is taken
	 */
	public boolean isLocked(){
		return locked.get();
	}
	
	/**
	 * @return True if there are threads waiting to take this lock
	 */
	public boolean hasQueuedThreads(){
		return (highPriorityThreadsSleeping.get() != 0L) ||
				(lowPriorityThreadsSleeping.get() != 0L);
	}

	/**
	 * A container for threads to be compared.
	 * The comparison is to be made first using {@link LockPriority} and only then by creation time.
	 * The natural order is that high priority threads are on top of low,
	 * and among same priority level threads, older threads come first.
	 */
	private class PrioritizedThread implements Comparable<PrioritizedThread> {

		/** The contained Thread */
		private Thread thread;
		/** The thread calling priority */
		private LockPriority priority;
		/** Object's creation time */
		private long timestamp;
		
		/**
		 * Constructs the {@link PrioritizedThread} object wrapping the given thread associated with the given priority.
		 * Also a timestamp is generated at construction time for comparison.
		 * @param _thread The thread to wrap.
		 * @param _priority The priority of the thread.
		 * @throws NullPointerException If thread or priority is null
		 */
		public PrioritizedThread(Thread _thread, LockPriority _priority) throws NullPointerException {
			if(_thread == null || _priority == null){
				throw new NullPointerException("No null thread nor priority allowed");
			}
			
			thread = _thread;
			priority = _priority;
			timestamp = System.currentTimeMillis();
			
		}
		
		/**
		 * @return The thread priority
		 */
		public final LockPriority getPriority(){
			return priority;
		}
		
		/**
		 * The format of this timestamp is the format of {@link System#currentTimeMillis()}
		 * @return Object's creation time
		 */
		public final long getTimestamp(){
			return timestamp;
		}
		
		/**
		 * @return The wrapped thread
		 */
		public final Thread getThread(){
			return thread;
		}
		
		@Override
		public int compareTo(PrioritizedThread _prioritizedThread) {
			int priorityComparison = priority.compareTo(_prioritizedThread.getPriority());
			
			if(priorityComparison < 0){
				// this thread has higher priority
				return -1;
			}
			
			if(priorityComparison > 0){
				// the other thread has higher priority
				return 1;
			}
			
			//both threads have same priority, return the one with the lower timestamp
			return (timestamp - _prioritizedThread.getTimestamp()) < 0 ? -1 : 1;
		}
		
	}
}
