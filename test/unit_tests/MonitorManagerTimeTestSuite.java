package unit_tests;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import Petri.PetriNet;
import Petri.PetriNetFactory;
import Petri.PetriNetFactory.petriNetType;
import Petri.Transition;
import monitor_petri.FirstInLinePolicy;
import monitor_petri.MonitorManager;
import monitor_petri.TransitionsPolicy;

public class MonitorManagerTimeTestSuite {

	MonitorManager monitor;
	PetriNet petri;
	static TransitionsPolicy policy;
	static PetriNetFactory factory;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		factory = new PetriNetFactory("./test/unit_tests/testResources/timedPetri.pnml");
		policy = new FirstInLinePolicy();
	}

	@Before
	public void setUp() throws Exception {
		petri = factory.makePetriNet(petriNetType.TIMED);
		monitor = new MonitorManager(petri, policy);
	}

	@After
	public void tearDown() throws Exception {
	}
	
	@Test
	public void testAThreadGoesToSleepWhenComeBeforeTimeSpan() {
		Transition t0 = petri.getTransitions()[0];
		
		Thread worker = new Thread(() -> {
			monitor.fireTransition(t0);
		});
		worker.start();
		
		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		assertEquals(true, t0.getTimeSpan().anySleeping());
	}
	
	@Test
	public void testAThreadGoesToVardCondQueueWhenAnotherThreadIsSleepingIntoTransition() {
		Transition t0 = petri.getTransitions()[0];
		Integer[] initialMarking = petri.getInitialMarking();
		
		Thread worker = new Thread(() -> {
			monitor.fireTransition(t0);
		});
		Thread worker2 = new Thread(() -> {
			monitor.fireTransition(t0);
		});
		worker.start();
		
		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		worker2.start();
		
		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
			
		assertEquals(true, t0.getTimeSpan().anySleeping());
		assertEquals(true, monitor.getQueuesState()[0]);
		assertArrayEquals(initialMarking, petri.getCurrentMarking());
	}
	
	@Test
	public void testAThreadGoesToSleepWhenComeBeforeTimeSpanAndThenWakeUpAndFireTransition() {
		Transition t0 = petri.getTransitions()[0];

		Thread worker = new Thread(() -> {
			monitor.fireTransition(t0);
		});
		worker.start();
		
		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		assertEquals(true, t0.getTimeSpan().anySleeping());
		
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		Integer[] expectedMarking = {0, 0, 1, 1};
		
		assertArrayEquals(expectedMarking, petri.getCurrentMarking());
	}

	/**
	 * <li> Given t0 and t3 are enabled by the same place p0 </li>
	 * <li> And t0 is timed [a,b] </li>
	 * <li> And t0 has not reached its time span </li>
	 * <li> When th0 tries to fire t0 </li>
	 * <li> And th0 sleeps on its own waiting for t0's time span</li>
	 * <li> And th1 fires t3 disabling t0 </li>
	 * <li> And th0 wakes up and tries to fire t0</li>
	 * <li> Then th0 fails firing t0</li>
	 * <li> And th0 goes to sleep in t0's varcond queue</li>
	 */
	@Test
	public void threadShouldSleepInVarcondQueueWhenTransitionGetsDisabledWhileSleepingByItself() {
		
		Transition t0 = petri.getTransitions()[0];
		Transition t3 = petri.getTransitions()[3];
		
		Assert.assertTrue(petri.isEnabled(t0));
		Assert.assertTrue(petri.isEnabled(t3));
		
		Assert.assertFalse(t0.getTimeSpan().anySleeping());
		
		Thread th0 = new Thread(() -> {
			monitor.fireTransition(t0);
		});
		th0.start();
		
		try {
			// let's give th0 a little time to try to fire t0
			Thread.sleep(50);
		} catch (InterruptedException e) {
			Assert.fail("Main thread interrupted. Message: " + e.getMessage());
		}
		
		Assert.assertFalse(t0.getTimeSpan().inTimeSpan(System.currentTimeMillis()));
		Assert.assertTrue(t0.getTimeSpan().anySleeping());
		
		Assert.assertTrue(petri.isEnabled(t0));
		Assert.assertTrue(petri.isEnabled(t3));
		
		monitor.fireTransition(t3);
		
		Assert.assertFalse(petri.isEnabled(t0));
		
		boolean[] expectedQueuesState = {false, false, false, false};
		Assert.assertArrayEquals(expectedQueuesState, monitor.getQueuesState());
		
		try {
			// let's give th0 some time to actually fire t0
			// It shouldn't, but if the functionality is broken and we don't wait here
			// it may appear as it's working properly when not
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			Assert.fail("Main thread interrupted. Message: " + e.getMessage());
		}
		
		expectedQueuesState[0] = true;
		Assert.assertArrayEquals(expectedQueuesState, monitor.getQueuesState());
	}

}
