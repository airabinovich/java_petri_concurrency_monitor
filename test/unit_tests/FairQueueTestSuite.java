package unit_tests;

import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import monitor_petri.FairQueue;
import test_utils.DummyTask;

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

}
