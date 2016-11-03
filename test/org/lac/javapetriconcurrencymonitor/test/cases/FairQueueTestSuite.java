package org.lac.javapetriconcurrencymonitor.test.cases;

import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.lac.javapetriconcurrencymonitor.test.utils.DummyTask;
import org.unc.lac.javapetriconcurrencymonitor.queues.FairQueue;

public class FairQueueTestSuite {
	
	List<DummyTask> threads;
	FairQueue queue;
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
		queue = new FairQueue();
		threads = new ArrayList<DummyTask>();
		for(int i = 0; i < 5 ; i++){
			DummyTask task = new DummyTask(queue);
			task.start();
			threads.add(task);
		}
	}

	@After
	public void tearDown() throws Exception {
		for(DummyTask task : threads){
			task.setTerminate(true);
		}
		threads = null;
	}

	@Test
	public void testSleep() {
		
		Assert.assertTrue(queue.isEmpty());
		
		for(Thread t : threads){
			((DummyTask)(t)).setGet_in_queue(true);
		}
		
		try {
			Thread.sleep(200);
		} catch (InterruptedException e) {}
		
		Assert.assertFalse(queue.isEmpty());
	}

	@Test
	public void testWakeUp() {
		Assert.assertTrue(queue.isEmpty());
		
		for(Thread t : threads){
			((DummyTask)(t)).setGet_in_queue(true);
		}
		
		try {
			Thread.sleep(200);
		} catch (InterruptedException e) {}
		
		Assert.assertFalse(queue.isEmpty());
		
		for(int i = threads.size() ; i >= 0 ; i--){
			Assert.assertEquals(i, queue.getSize());
			queue.wakeUp();
			try {
				// let's give the semaphore a little while to release the thread
				Thread.sleep(10);
			} catch (InterruptedException e) {}
		}
		
		try {
			Thread.sleep(200);
		} catch (InterruptedException e) {}
		
		Assert.assertTrue(queue.isEmpty());
		
	}
	
	/**
	 * <li> Given 3 threads go to sleep with low priority </li>
	 * <li> And 2 threads go to sleep with high priority </li>
	 * <li> When I wake up 2 threads </li>
	 * <li> Then no high priority threads are sleeping </li>
	 * <li> And 3 low priority threads are sleeping </li>
	 */
	@Test
	public void threadSleepingWithHighPriorityIsWakenBeforeLowPriorityThread(){
		Assert.assertTrue(queue.isEmpty());
		
		for(int i = 0; i < 3; i++){
			threads.get(i).setGet_in_queue(true);
		}
		
		for(int i = 3; i < 5; i++){
			threads.get(i).setGet_in_high_priority_queue(true);
		}
		
		try {
			Thread.sleep(200);
		} catch (InterruptedException e) {
			Assert.fail("Main thread interrupted during test execution");
		}
		
		Assert.assertEquals(3, queue.getLowPriorityThreadsSleeping());
		Assert.assertEquals(2, queue.getHighPriorityThreadsSleeping());
		
		queue.wakeUp();
		
		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
			Assert.fail("Main thread interrupted during test execution");
		}
		
		queue.wakeUp();
		
		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
			Assert.fail("Main thread interrupted during test execution");
		}
		
		Assert.assertEquals(3, queue.getLowPriorityThreadsSleeping());
		Assert.assertEquals(0, queue.getHighPriorityThreadsSleeping());
		
	}

}
