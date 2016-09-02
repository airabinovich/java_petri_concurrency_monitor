package unit_tests;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import Petri.TimedPetriNet;
import Petri.Transition;
import Petri.TimedPetriNet.TimedPetriNetBuilder;
import monitor_petri.FirstInLinePolicy;
import monitor_petri.MonitorManager;
import monitor_petri.TransitionsPolicy;

public class MonitorManagerTimeTestSuite {

	MonitorManager monitor;
	TimedPetriNet petri;
	static TransitionsPolicy policy;
	static TimedPetriNetBuilder builder;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		builder = new TimedPetriNetBuilder("./test/unit_tests/testResources/timedPetri.pnml");
		policy = new FirstInLinePolicy();
	}

	@Before
	public void setUp() throws Exception {
		petri = builder.buildPetriNet();
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

}
