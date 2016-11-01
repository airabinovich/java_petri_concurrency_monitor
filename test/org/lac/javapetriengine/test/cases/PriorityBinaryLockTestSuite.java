package org.lac.javapetriengine.test.cases;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.lac.javapetriengine.utils.PriorityBinaryLock;
import org.lac.javapetriengine.utils.PriorityBinaryLock.LockPriority;

public class PriorityBinaryLockTestSuite {

	/**
	 * <li> Given lock l0 gets locked </li>
	 * <li> And l0's low priority queue is empty </li>
	 * <li> When thread th0 tries to lock l0 </li>
	 * <li> Then th0 sleeps in l0's low priority queue </li>
	 * <li> And l0's low priority queue has one thread sleeping </li>
	 */
	@Test
	public void testAcquireTakenLockShouldSleepInLowPriorityQueue() {
		
		PriorityBinaryLock l0 = new PriorityBinaryLock();
		
		l0.lock();
		
		try {
			Thread.sleep(100);
		} catch (InterruptedException e1) {
			Assert.fail("Thread interrupted during test execution");
		}
		
		Assert.assertEquals(0, l0.getLowPriorityQueueLength());
		
		Thread th0 = new Thread(() -> {
			l0.lock();
			Assert.fail("This thread should not reach this point");
		});
		
		th0.start();
		
		try {
			Thread.sleep(50);
		} catch (InterruptedException e) {
			Assert.fail("Main thread interrupted");
		}
		
		Assert.assertEquals(1, l0.getLowPriorityQueueLength());
	}
	
	/**
	 * <li> Given lock l0 gets locked </li>
	 * <li> And l0's low priority queue is empty </li>
	 * <li> When thread th0 tries to lock l0 with low priority </li>
	 * <li> Then th0 sleeps in l0's low priority queue </li>
	 * <li> And l0's low priority queue has one thread sleeping </li>
	 */
	@Test
	public void testAcquireTakenLockWithLowPriorityShouldSleepInLowPriorityQueue() {
		
		PriorityBinaryLock l0 = new PriorityBinaryLock();
		
		l0.lock();
		
		try {
			Thread.sleep(100);
		} catch (InterruptedException e1) {
			Assert.fail("Thread interrupted during test execution");
		}
		
		Assert.assertEquals(0, l0.getLowPriorityQueueLength());
		
		Thread th0 = new Thread(() -> {
			l0.lock(LockPriority.LOW);
			Assert.fail("This thread should not reach this point");
		});
		
		th0.start();
		
		try {
			Thread.sleep(50);
		} catch (InterruptedException e) {
			Assert.fail("Main thread interrupted");
		}
		
		Assert.assertEquals(1, l0.getLowPriorityQueueLength());
	}
	
	/**
	 * <li> Given lock l0 gets locked </li>
	 * <li> And l0's low priority queue is empty </li>
	 * <li> When thread th0 tries to lock l0 with high priority </li>
	 * <li> Then th0 sleeps in l0's high priority queue </li>
	 * <li> And l0's high priority queue has one thread sleeping </li>
	 */
	@Test
	public void testAcquireTakenLockWithHighPriorityShouldSleepInHighPriorityQueue() {
		
		PriorityBinaryLock l0 = new PriorityBinaryLock();
		
		l0.lock();
		
		try {
			Thread.sleep(100);
		} catch (InterruptedException e1) {
			Assert.fail("Thread interrupted during test execution");
		}
		
		Assert.assertEquals(0, l0.getHighPriorityQueueLength());
		
		Thread th0 = new Thread(() -> {
			l0.lock(LockPriority.HIGH);
			Assert.fail("This thread should not reach this point");
		});
		
		th0.start();
		
		try {
			Thread.sleep(50);
		} catch (InterruptedException e) {
			Assert.fail("Main thread interrupted");
		}
		
		Assert.assertEquals(1, l0.getHighPriorityQueueLength());
	}
	
	/**
	 * <li> Given l0 is locked </li>
	 * <li> And l0's low priority queue is empty </li>
	 * <li> And l0's high priority queue is empty </li>
	 * <li> When thread th0 tries to lock l0 with high priority </li>
	 * <li> And th0 goes to sleep in high priority queue </li>
	 * <li> And thread th1 tries to lock l0 with low priority </li>
	 * <li> And th1 goes to sleep in low priority queue </li>
	 * <li> And I unlock l0 </li>
	 * <li> Then th0 wakes up and executes its task </li>
	 * <li> And th1 never wakes up </li>
	 * <li> And l0's low priority queue has one thread sleeping </li>
	 * <li> And l0's high priority queue has no threads sleeping </li>
	 */
	@Test
	public void testReleaseLockShouldWakeUpThreadSleepingInHighPriorityQueueFirst() {
		
		PriorityBinaryLock l0 = new PriorityBinaryLock();
				
		l0.lock();
		
		Assert.assertEquals(0, l0.getLowPriorityQueueLength());
		Assert.assertEquals(0, l0.getHighPriorityQueueLength());
		
		FutureTask<Boolean> ft0 = new FutureTask<Boolean>(() -> {
			l0.lock(LockPriority.HIGH);
			return true;
		});
		Thread th0 = new Thread(ft0);
		th0.start();
		
		FutureTask<Boolean> ft1 = new FutureTask<Boolean>(() -> {
			l0.lock(LockPriority.LOW);
			return true;
		});
		Thread th1 = new Thread(ft1);
		th1.start();
		
		try {
			Thread.sleep(50);
		} catch (InterruptedException e) {
			Assert.fail("Main thread interrupted");
		}
		
		l0.unlock();
		
		try {
			Thread.sleep(50);
		} catch (InterruptedException e) {
			Assert.fail("Main thread interrupted");
		}

		try{
			ft1.get(1, TimeUnit.MILLISECONDS);
			Assert.fail("Should not get to this point");
		} catch (TimeoutException e){
			//nothing wrong, just timed out because it's locked
		} catch (Exception e){
			Assert.fail("Unexpected exception thrown");
		}
		try {
			Assert.assertTrue(ft0.get());
		} catch (InterruptedException | ExecutionException e) {
			Assert.fail("Thread interrupted during test execution");
		}
	}
	
	/**
	 * <li> Given l0 is locked </li>
	 * <li> And l0's low priority queue is empty </li>
	 * <li> And l0's high priority queue is empty </li>
	 * <li> When thread th0 tries to lock l0 with low priority </li>
	 * <li> And th0 goes to sleep in low priority queue </li>
	 * <li> And I unlock l0 </li>
	 * <li> Then th0 wakes up and executes its task </li>
	 * <li> And l0's low priority queue has no threads sleeping </li>
	 * <li> And l0's high priority queue has no threads sleeping </li>
	 */
	@Test
	public void testReleaseLockShouldWakeUpThreadSleepingInLowPriorityQueueIfHighIsEmpty(){
		
		PriorityBinaryLock l0 = new PriorityBinaryLock();
		
		l0.lock();
		
		Assert.assertEquals(0, l0.getLowPriorityQueueLength());
		Assert.assertEquals(0, l0.getHighPriorityQueueLength());
		
		Thread th0 = new Thread(() -> {
			l0.lock(LockPriority.LOW);
		});
		th0.start();
		
		try {
			Thread.sleep(50);
		} catch (InterruptedException e) {
			Assert.fail("Main thread interrupted");
		}
		
		Assert.assertEquals(1, l0.getLowPriorityQueueLength());
		Assert.assertEquals(0, l0.getHighPriorityQueueLength());
		
		l0.unlock();
		
		try {
			Thread.sleep(50);
		} catch (InterruptedException e) {
			Assert.fail("Main thread interrupted");
		}
		
		Assert.assertEquals(0, l0.getLowPriorityQueueLength());
		Assert.assertEquals(0, l0.getHighPriorityQueueLength());
	}

	/**
	 * <li> Given l0 is locked </li>
	 * <li> And l0's low priority queue is empty </li>
	 * <li> And l0's high priority queue is empty </li>
	 * <li> When thread th0 tries to lock l0 with high priority </li>
	 * <li> And th0 goes to sleep in high priority queue </li>
	 * <li> And I unlock l0 </li>
	 * <li> Then th0 wakes up and executes its task </li>
	 * <li> And l0's low priority queue has no threads sleeping </li>
	 * <li> And l0's high priority queue has no threads sleeping </li>
	 */
	@Test
	public void testReleaseLockShouldWakeUpThreadSleepingInHighPriorityQueueIfHighIsEmpty(){
		
		PriorityBinaryLock l0 = new PriorityBinaryLock();
		
		l0.lock();
		
		Assert.assertEquals(0, l0.getLowPriorityQueueLength());
		Assert.assertEquals(0, l0.getHighPriorityQueueLength());
		
		Thread th0 = new Thread(() -> {
			l0.lock(LockPriority.HIGH);
		});
		th0.start();
		
		try {
			Thread.sleep(50);
		} catch (InterruptedException e) {
			Assert.fail("Main thread interrupted");
		}
		
		Assert.assertEquals(0, l0.getLowPriorityQueueLength());
		Assert.assertEquals(1, l0.getHighPriorityQueueLength());
		
		l0.unlock();
		
		try {
			Thread.sleep(50);
		} catch (InterruptedException e) {
			Assert.fail("Main thread interrupted");
		}
		
		Assert.assertEquals(0, l0.getLowPriorityQueueLength());
		Assert.assertEquals(0, l0.getHighPriorityQueueLength());
	}
	
	/**
	 * <li> Given l0 gets locked </li>
	 * <li> And l0's low priority queue is empty </li>
	 * <li> And l0's high priority queue is empty </li>
	 * <li> When 50 threads try to take l0 with low priority </li>
	 * <li> And 50 threads try to take l0 with high priority </li>
	 * <li> And l0's low priority queue has 50 threads </li>
	 * <li> And l0's high priority queue has 50 threads </li>
	 * <li> And l0 is unlocked 50 times </li>
	 * <li> Then l0's low priority queue has 50 threads </li>
	 * <li> And l0's high priority queue is empty </li>
	 */
	@Test
	public void testMultipleUnlocksShouldWakeAllHighPriorityThreadsFirst(){
		
		PriorityBinaryLock l0 = new PriorityBinaryLock();
		
		l0.lock();
		
		Assert.assertEquals(0, l0.getLowPriorityQueueLength());
		Assert.assertEquals(0, l0.getHighPriorityQueueLength());
		
		boolean isHighPriority = true;
		for(int i = 0; i < 100; i++){
			if(isHighPriority){
				new Thread(() -> l0.lock(LockPriority.HIGH)).start();
			}
			else{
				new Thread(
						() -> {
							l0.lock(LockPriority.LOW);
							Assert.fail("Should've not reached this point");
						}
						)
						.start();
			}
			isHighPriority = !isHighPriority;
		}
		
		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
			Assert.fail("Main thread interrupted");
		}
		
		Assert.assertEquals(50, l0.getLowPriorityQueueLength());
		Assert.assertEquals(50, l0.getHighPriorityQueueLength());
		
		for(int i = 0; i < 50; i++){
			l0.unlock();
			try {
				// give the waken thread a little time to actually get out of the lock
				Thread.sleep(1);
			} catch (InterruptedException e) {
				Assert.fail("Main thread interrupted");
			}
		}
		
		Assert.assertEquals(50, l0.getLowPriorityQueueLength());
		Assert.assertEquals(0, l0.getHighPriorityQueueLength());
	}
	
	/**
	 * <li> Given lock l0 is not taken </li>
	 * <li> When I try to unlock l0 </li>
	 * <li> Then l0 remains unlocked </li>
	 * <li> And no exception is thrown </li>
	 */
	@Test
	public void testUnlockingNonTakenLockShouldHaveNoEffect(){
		
		PriorityBinaryLock l0 = new PriorityBinaryLock();
		
		Assert.assertFalse(l0.isLocked());
		
		try{
			l0.unlock();
			
			Assert.assertFalse(l0.isLocked());
		} catch (Exception e) {
			Assert.fail("No exception should've been thrown: " + e.getMessage());
		}
	}
}
