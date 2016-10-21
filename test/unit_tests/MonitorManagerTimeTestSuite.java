package unit_tests;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.ArrayList;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

import Petri.PetriNet;
import Petri.PetriNetFactory;
import Petri.TimedPetriNet;
import Petri.PetriNetFactory.petriNetType;
import Petri.Transition;
import monitor_petri.FirstInLinePolicy;
import monitor_petri.MonitorManager;
import monitor_petri.TransitionsPolicy;
import test_utils.TransitionEventObserver;

public class MonitorManagerTimeTestSuite {

	MonitorManager monitor;
	TimedPetriNet timedPetriNet;
	static ObjectMapper jsonParser;
	static TransitionsPolicy policy;
	static PetriNetFactory factory;
	
	private static final String ID = "id";
	private static final String TEST_PETRI_FOLDER = "test/unit_tests/testResources/";
	private static final String TIMED_PETRI_NET = TEST_PETRI_FOLDER + "timedPetri.pnml";
	private static final String PETRI_FOR_INITIALIZATION_TIME = TEST_PETRI_FOLDER + "timedPetriForInitializationTime.pnml";

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		policy = new FirstInLinePolicy();
		jsonParser = new ObjectMapper();
	}

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}
	
	/**
	 * Creates factory, timed petri and monitor from given PNML
	 * @param PNML Path to the PNML file
	 * @param type The petri type to create
	 */
	private void setUpMonitor(String PNML){
		factory = new PetriNetFactory(PNML);
		timedPetriNet = (TimedPetriNet) factory.makePetriNet(petriNetType.TIMED);
		monitor = new MonitorManager(timedPetriNet, policy);
		
	}
	
	/**
	 * <li> Given t0 fed by p0 with an arc with weight 1 </li>
	 * <li> And p0 has one token</li>
	 * <li> And t0 is timed [a,b], a>0, b>a </li>
	 * <li> When thread worker fires t0 at time ti < a (before timespan) </li>
	 * <li> Then worker goes to sleep by itself </li>
	 */
	@Test
	public void testAThreadGoesToSleepWhenComeBeforeTimeSpan() {
		
		setUpMonitor(TIMED_PETRI_NET);
		timedPetriNet.startTimes();
		
		Transition t0 = timedPetriNet.getTransitions()[0];
		
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
	
	/**
	 * <li> Given t0 fed by p0 with an arc with weight 1 </li>
	 * <li> And p0 has one token</li>
	 * <li> And t0 is timed [a,b], a>0, b>a </li>
	 * <li> When thread worker1 tries to fires t0 at time ti < a (before timespan) </li>
	 * <li> And thread worker2 tries to fires t0 at time tj, where ti < tj < a (before timespan as well) </li>
	 * <li> Then worker1 goes to sleep by itself </li>
	 * <li> And worker2 goes to sleep into varCond queue </li>
	 */
	@Test
	public void testAThreadGoesToVardCondQueueWhenAnotherThreadIsSleepingIntoTransition() {
		
		setUpMonitor(TIMED_PETRI_NET);
		timedPetriNet.startTimes();
		
		Transition t0 = timedPetriNet.getTransitions()[0];
		Integer[] initialMarking = timedPetriNet.getInitialMarking();
		
		long t0BeginTime = t0.getTimeSpan().getTimeBegin();
		
		Thread worker1 = new Thread(() -> {
			monitor.fireTransition(t0);
		});
		Thread worker2 = new Thread(() -> {
			monitor.fireTransition(t0);
		});
		worker1.start();
		
		try {
			Thread.sleep(t0BeginTime / 10);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		worker2.start();
		
		try {
			Thread.sleep(t0BeginTime / 10);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
			
		assertEquals(true, t0.getTimeSpan().anySleeping());
		assertEquals(true, monitor.getQueuesState()[0]);
		assertArrayEquals(initialMarking, timedPetriNet.getCurrentMarking());
	}
	
	/**
	 * <li> Given t0 fed by p0 with an arc with weight 1 </li>
	 * <li> And p0 has one token</li>
	 * <li> And t0 is timed [a,b], a>0, b>a </li>
	 * <li> When thread worker tries to fires t0 at time ti < a (before timespan) </li>
	 * <li> Then worker goes to sleep by itself </li>
	 * <li> And worker wakes up after the time "a - ti" and fires the transition succefully</li>
	 */
	@Test
	public void testAThreadGoesToSleepWhenComeBeforeTimeSpanAndThenWakeUpAndFireTransition() {
		
		setUpMonitor(TIMED_PETRI_NET);
		timedPetriNet.startTimes();
		
		Transition t0 = timedPetriNet.getTransitions()[0];

		long t0BeginTime = t0.getTimeSpan().getTimeBegin();
		
		Thread worker = new Thread(() -> {
			monitor.fireTransition(t0);
		});
		worker.start();
		
		try {
			Thread.sleep(t0BeginTime / 10);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		assertEquals(true, t0.getTimeSpan().anySleeping());
		
		try {
			Thread.sleep(t0BeginTime);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		Integer[] expectedMarking = {0, 0, 1, 1};
		
		assertArrayEquals(expectedMarking, timedPetriNet.getCurrentMarking());
	}

	/**
	 * <li> Given t0 and t3 are enabled by the same place p0 </li>
	 * <li> And t0 is timed [a,b], a>0, b>a </li>
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
		
		setUpMonitor(TIMED_PETRI_NET);
		timedPetriNet.startTimes();
		
		Transition t0 = timedPetriNet.getTransitions()[0];
		Transition t3 = timedPetriNet.getTransitions()[3];
		
		Assert.assertTrue(timedPetriNet.isEnabled(t0));
		Assert.assertTrue(timedPetriNet.isEnabled(t3));
		
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
		
		Assert.assertTrue(timedPetriNet.isEnabled(t0));
		Assert.assertTrue(timedPetriNet.isEnabled(t3));
		
		monitor.fireTransition(t3);
		
		Assert.assertFalse(timedPetriNet.isEnabled(t0));
		
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
	
	/**
	 * <li> Given t0 gets enabled at time ti </li>
	 * <li> And t0 is timed [a,b], a>0, b>a </li>
	 * <li> And t0 has not reached its time span </li>
	 * <li> And obs is subscript to t0's events </li>
	 * <li> When th0 tries to perennial fire t0 </li>
	 * <li> Then th0 sleeps on its own waiting for t0's time span </li>
	 * <li> And th0 wakes up at time (ti + a) </li>
	 * <li> And th0 fires t0 </li>
	 * <li> And obs gets one event matching t0's id </li>
	 */
	@Test
	public void threadPerennialFiringATransitionBeforeItsTimeSpanShouldSleepOnItsOwnAndThenFire(){
		
		setUpMonitor(TIMED_PETRI_NET);
		timedPetriNet.startTimes();
		
		Transition t0 = timedPetriNet.getTransitions()[0];
		Assert.assertTrue(timedPetriNet.isEnabled(t0));
		
		long t0BeginTime = t0.getTimeSpan().getTimeBegin();
		
		TransitionEventObserver obs = new TransitionEventObserver();
		monitor.subscribeToTransition(t0, obs);
		
		ArrayList<String> events = obs.getEvents();
		
		Thread th0 = new Thread(() -> monitor.fireTransition(t0, true));
		th0.start();
		
		try {
			// let's give th0 a little time to try to fire t0
			Thread.sleep(t0BeginTime / 10);
		} catch (InterruptedException e) {
			Assert.fail("Main thread interrupted. Message: " + e.getMessage());
		}
		
		Assert.assertTrue(events.isEmpty());
		
		//let's make sure th0 isn't sleeping in t0's queue
		Assert.assertFalse(monitor.getQueuesState()[0]);

		try {
			// let's give th0 enough time to wake up and fire t0
			Thread.sleep(t0BeginTime);
		} catch (InterruptedException e) {
			Assert.fail("Main thread interrupted. Message: " + e.getMessage());
		}
		
		Assert.assertFalse(events.isEmpty());
		try {
			String obtainedId = jsonParser.readTree(events.get(0)).get(ID).asText();
			Assert.assertEquals(t0.getId(), obtainedId);
		} catch (IOException e) {
			Assert.fail("Event is not in JSON format");
		}
	}
	
	/**
	 * <li> Given t0 gets enabled at time ti </li>
	 * <li> And t0 is timed [a,b], a>0, b>a </li>
	 * <li> And t0 has past its time span </li>
	 * <li> And obs is subscript to t0's events </li>
	 * <li> When th0 tries to perennial fire t0 </li>
	 * <li> Then th0 doesn't go to sleep in t0's queue </li>
	 * <li> And th0 wakes up at time (ti + a) </li>
	 * <li> And obs gets no events </li>
	 */
	@Test
	public void threadPerennialFiringATransitionAfterItsTimeSpanShouldNotSleepInQueue(){
		
		setUpMonitor(TIMED_PETRI_NET);
		timedPetriNet.startTimes();
		
		Transition t0 = timedPetriNet.getTransitions()[0];
		Assert.assertTrue(timedPetriNet.isEnabled(t0));
		
		long t0BeginTime = t0.getTimeSpan().getTimeBegin();
		long t0EndTime = t0.getTimeSpan().getTimeEnd();
		
		TransitionEventObserver obs = new TransitionEventObserver();
		monitor.subscribeToTransition(t0, obs);
		
		ArrayList<String> events = obs.getEvents();
		
		try {
			// let's wait for t0 to get past its time span
			Thread.sleep(t0BeginTime + t0EndTime + 100);
		} catch (InterruptedException e) {
			Assert.fail("Main thread interrupted. Message: " + e.getMessage());
		}
		
		Thread th0 = new Thread(() -> monitor.fireTransition(t0, true));
		th0.start();
		
		try {
			// let's give th0 a little time to try to fire t0
			Thread.sleep(t0BeginTime / 10);
		} catch (InterruptedException e) {
			Assert.fail("Main thread interrupted. Message: " + e.getMessage());
		}
		
		Assert.assertTrue(events.isEmpty());
		
		//let's make sure th0 isn't sleeping in t0's queue
		Assert.assertFalse(monitor.getQueuesState()[0]);
		
	}
	
	/**
	 * <li> Given t0 is enabled </li>
	 * <li> And t0 is timed with timespan [a,b], a>0, b>a </li>
	 * <li> And some time passes after initialization </li>
	 * <li> And the timespans are initialized </li>
	 * <li> When thread th0 tries to fire t0 </li>
	 * <li> Then th0 fires t0 succefully </li>
	 */
	@Test
	public void MonitorShouldRestartTheEnablingTimesAndHasNoRaceTimeCondition(){
		
		setUpMonitor(PETRI_FOR_INITIALIZATION_TIME);
		
		Transition t0 = timedPetriNet.getTransitions()[0];
		
		Integer[] expectedMarking = {1, 0};
		Assert.assertArrayEquals(expectedMarking, timedPetriNet.getCurrentMarking());
		
		Thread th0 = new Thread(() -> monitor.fireTransition(t0));
		
		try {
			Thread.sleep(200);
		} catch (InterruptedException e) {
			Assert.fail("Interrupted thread: " + e.getMessage());
		}
		timedPetriNet.startTimes();
		th0.start();
		
		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
			Assert.fail("Interrupted thread: " + e.getMessage());
		}
		
		expectedMarking[0] = 0;
		expectedMarking[1] = 1;	
		Assert.assertArrayEquals(expectedMarking, timedPetriNet.getCurrentMarking());
	}
}
