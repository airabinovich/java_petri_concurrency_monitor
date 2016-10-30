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

import Petri.NotInitializedPetriNetException;
import Petri.PetriNetFactory;
import Petri.TimedPetriNet;
import Petri.PetriNetFactory.petriNetType;
import Petri.Transition;
import monitor_petri.FirstInLinePolicy;
import monitor_petri.IllegalTransitionFiringError;
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
	private static final String TIMED_PETRI_NET_02 = TEST_PETRI_FOLDER + "timedPetri02.pnml";
	private static final String PETRI_FOR_INITIALIZATION_TIME = TEST_PETRI_FOLDER + "timedPetriForInitializationTime.pnml";
	private static final String PETRI_FOR_INITIALIZATION_TIME2 = TEST_PETRI_FOLDER + "timedPetriForInitializationTime2.pnml";

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
		timedPetriNet.initializePetriNet();
		
		Transition t0 = timedPetriNet.getTransitions()[0];
		
		Thread worker = new Thread(() -> {
			try {
				monitor.fireTransition(t0);
			} catch (Exception e) {
				Assert.fail("Exception thrown in test execution");
			}
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
		timedPetriNet.initializePetriNet();
		
		Transition t0 = timedPetriNet.getTransitions()[0];
		Integer[] initialMarking = timedPetriNet.getInitialMarking();
		
		long t0BeginTime = t0.getTimeSpan().getTimeBegin();
		
		Thread worker1 = new Thread(() -> {
			try {
				monitor.fireTransition(t0);
			} catch (Exception e) {
				Assert.fail("Exception thrown in test execution");
			}
		});
		Thread worker2 = new Thread(() -> {
			try {
				monitor.fireTransition(t0);
			} catch (Exception e) {
				Assert.fail("Exception thrown in test execution");
			}
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
		timedPetriNet.initializePetriNet();
		
		Transition t0 = timedPetriNet.getTransitions()[0];

		long t0BeginTime = t0.getTimeSpan().getTimeBegin();
		
		Thread worker = new Thread(() -> {
			try {
				monitor.fireTransition(t0);
			} catch (Exception e) {
				Assert.fail("Exception thrown in test execution");
			}
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
		timedPetriNet.initializePetriNet();
		
		Transition t0 = timedPetriNet.getTransitions()[0];
		Transition t3 = timedPetriNet.getTransitions()[3];
		
		Assert.assertTrue(timedPetriNet.isEnabled(t0));
		Assert.assertTrue(timedPetriNet.isEnabled(t3));
		
		Assert.assertFalse(t0.getTimeSpan().anySleeping());
		
		Thread th0 = new Thread(() -> {
			try {
				monitor.fireTransition(t0);
			} catch (Exception e) {
				Assert.fail("Exception thrown in test execution");
			}
		});
		th0.start();
		
		try {
			// let's give th0 a little time to try to fire t0
			Thread.sleep(50);
		} catch (InterruptedException e) {
			Assert.fail("Main thread interrupted. Message: " + e.getMessage());
		}
		
		boolean insideTimeSpan = t0.getTimeSpan().inTimeSpan(System.currentTimeMillis());
		
		Assert.assertFalse(insideTimeSpan);
		Assert.assertTrue(t0.getTimeSpan().anySleeping());
		
		Assert.assertTrue(timedPetriNet.isEnabled(t0));
		Assert.assertTrue(timedPetriNet.isEnabled(t3));
		
		try {
			monitor.fireTransition(t3);
		} catch (IllegalTransitionFiringError | NotInitializedPetriNetException e1) {
			Assert.fail("Exception thrown in test execution");
		}
		
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
		timedPetriNet.initializePetriNet();
		
		Transition t0 = timedPetriNet.getTransitions()[0];
		Assert.assertTrue(timedPetriNet.isEnabled(t0));
		
		long t0BeginTime = t0.getTimeSpan().getTimeBegin();
		
		TransitionEventObserver obs = new TransitionEventObserver();
		monitor.subscribeToTransition(t0, obs);
		
		ArrayList<String> events = obs.getEvents();
		
		Thread th0 = new Thread(() -> {
			try {
				monitor.fireTransition(t0, true);
			} catch (Exception e) {
				Assert.fail("Exception thrown in test execution");
			}
		});
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
		timedPetriNet.initializePetriNet();
		
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
		
		Thread th0 = new Thread(() -> {
			try {
				monitor.fireTransition(t0, true);
			} catch (Exception e) {
				Assert.fail("Exception thrown in test execution");
			}
		});
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
	 * <li> Then th0 fires t0 successfully </li>
	 */
	@Test
	public void MonitorShouldRestartTheEnablingTimesAndHasNoRaceTimeCondition(){
		
		setUpMonitor(PETRI_FOR_INITIALIZATION_TIME);
		
		Transition t0 = timedPetriNet.getTransitions()[0];
		
		Integer[] expectedMarking = {1, 0};
		Assert.assertArrayEquals(expectedMarking, timedPetriNet.getCurrentMarking());
		
		Thread th0 = new Thread(() -> {
			try {
				monitor.fireTransition(t0);
			} catch (Exception e) {
				Assert.fail("Exception thrown in test execution");
			}
		});
		
		try {
			Thread.sleep(200);
		} catch (InterruptedException e) {
			Assert.fail("Interrupted thread: " + e.getMessage());
		}
		timedPetriNet.initializePetriNet();
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
	
	/**
	 * <li> Given t0 is enabled </li>
	 * <li> And t0 is timed with timespan [a,b], a>0, b>a </li>
	 * <li> And some time passes after initialization </li>
	 * <li> When thread th0 tries to fire t0 </li>
	 * <li> Then a NotInitializedTimedPetriNetException is thrown </li>
	 */
	@Test
	public void MonitorShouldThrowAnExceptionWhenThreadTriesToFireBeforeInitializePetriNet(){
		
		setUpMonitor(PETRI_FOR_INITIALIZATION_TIME);
		
		Transition t0 = timedPetriNet.getTransitions()[0];
		
		Integer[] expectedMarking = {1, 0};
		Assert.assertArrayEquals(expectedMarking, timedPetriNet.getCurrentMarking());
		
		Thread th0 = new Thread(() -> {
			try {
				monitor.fireTransition(t0);
			} catch (Exception e) {
				Assert.assertEquals(NotInitializedPetriNetException.class, e.getClass());
			}
		});
		
		th0.start();	
		
		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
			Assert.fail("Interrupted thread: " + e.getMessage());
		}
		
		Assert.assertArrayEquals(expectedMarking, timedPetriNet.getCurrentMarking());
	}
	
	/**
	 * <li> Given p0 feeds t0 and p2 feeds t1 </li>
	 * <li> And t0 and t1 are enabled </li>
	 * <li> And t0 is timed with timespan [a,b], a>0, b>a </li>
	 * <li> And t1 is not timed </li>
	 * <li> And some time passes after initialization </li>
	 * <li> When th2 tries to fire t0 </li>
	 * <li> Then NotInitializedPetriNetException is thrown </li>
	 * <li> And th0 tries to fire t1 </li>
	 * <li> And a NotInitializedTimedPetriNetException is thrown </li>
	 */
	@Test
	public void MonitorShouldNotInitializePetriNetAfterFiringANonTimedTransition(){
		
		setUpMonitor(PETRI_FOR_INITIALIZATION_TIME2);
		
		Transition t0 = timedPetriNet.getTransitions()[0];
		Transition t1 = timedPetriNet.getTransitions()[1];
		
		Integer[] expectedMarking = {1, 0, 1};
		Assert.assertArrayEquals(expectedMarking, timedPetriNet.getCurrentMarking());
		
		Thread th0 = new Thread(() -> {
			try {
				monitor.fireTransition(t1);
			} catch (Exception e) {
				Assert.assertEquals(NotInitializedPetriNetException.class, e.getClass());
			}
		});
		
		Thread th1 = new Thread(() -> {
			try {
				monitor.fireTransition(t0);
			} catch (Exception e) {
				Assert.assertEquals(NotInitializedPetriNetException.class, e.getClass());
			}
		});
		
		th0.start();
		
		th1.start();
		
		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
			Assert.fail("Interrupted thread: " + e.getMessage());
		}
		
		Assert.assertArrayEquals(expectedMarking, timedPetriNet.getCurrentMarking());
	}
	
	/**
	* <li> Given t0 is timed [a,b], a > 0, b > a </li>
	* <li> And t0 is enabled at time ti </li>
	* <li> And t2 is not timed and always enabled </li>
	* <li> And observer obs is listening for t0's and t2's events </li>
	* <li> When thread th0 tries to fire t0 at time tj, ti < tj < (ti+a) </li>
	* <li> And th0 sleeps on its own waiting for t0's timespan </li>
	* <li> And 500 threads try to fire t2 before th1 wakes up</li>
	* <li> Then th0 wakes up before all threads finished their work </li>
	* <li> And th0 fires t0 right as it wakes up </li>
	* <li> And in obs's event list, t0's firing is not the last </li>
	*/
	@Test
	public void threadWhoSleptWaitingForTimeSpanShouldHavePriorityOverIncomingThreads(){
		
		setUpMonitor(TIMED_PETRI_NET_02);
		
		Transition t0 = timedPetriNet.getTransitions()[0];
		Transition t2 = timedPetriNet.getTransitions()[2];
		
		Thread th0 = new Thread(() -> {
			try {
				monitor.fireTransition(t0);
			} catch (IllegalTransitionFiringError | NotInitializedPetriNetException e) {
				Assert.fail("Exception thrown in test execution");
			}
		});
		
		ArrayList<Thread> workers = new ArrayList<Thread>();
		for(int i = 0; i < 500; i++){
			Thread worker = new Thread(() -> {
				try {
					monitor.fireTransition(t2);
				} catch (IllegalTransitionFiringError | NotInitializedPetriNetException e) {
					Assert.fail("Exception thrown in test execution");
				}
			});
			workers.add(worker);
		}
		
		TransitionEventObserver obs = new TransitionEventObserver();
		monitor.subscribeToTransition(t0, obs);
		monitor.subscribeToTransition(t2, obs);
		
		ArrayList<String> events = obs.getEvents();
		
		timedPetriNet.initializePetriNet();
		
		th0.start();
		
		try {
			Thread.sleep(10);
		} catch (InterruptedException e) {
			Assert.fail("Interrupted thread: " + e.getMessage());
		}
		
		Assert.assertTrue(t0.getTimeSpan().anySleeping());
		
		Assert.assertTrue(events.isEmpty());
		
		for(Thread worker : workers){
			worker.start();
		}
		
		try {
			Thread.sleep(200);
		} catch (InterruptedException e) {
			Assert.fail("Interrupted thread: " + e.getMessage());
		}
		
		String t0ID = t0.getId();
		int t0EventIndex = -1;
		
		for(int i = 0; i < events.size(); i++){
			try {
				String obtainedId = jsonParser.readTree(events.get(i)).get(ID).asText();
				if(obtainedId.equals(t0ID)){
					t0EventIndex = i;
					break;
				}
			} catch (IOException e) {
				Assert.fail("Event is not in JSON format");
			}
		}
		
		Assert.assertNotEquals(-1, t0EventIndex);
		Assert.assertTrue(t0EventIndex < events.size() - 1);
		Assert.assertTrue(events.size() > 1);
	}

	/**
	* <li> Given t0 is timed [a,b], a > 0, b > a </li>
	* <li> And t0 is enabled at time ti </li>
	* <li> And t0 is fed by p0 </li>
	* <li> And p0 is fed by t3 </li>
	* <li> And p0 feeds t1 </li>
	* <li> And p0 has one token </li>
	* <li> And observer obs is listening for t0's events </li>
	* <li> When thread th0 tries to fire t0 at time tj, ti < tj < (ti+a) </li>
	* <li> And th0 sleeps on its own waiting for t0's timespan </li>
	* <li> And 10 threads try to fire t0 </li>
	* <li> And all of them sleep in t0's queue </li>
	* <li> And I fire t1 before th0 wakes up </li>
	* <li> And t0 gets disabled </li>
	* <li> Then th0 wakes up</li>
	* <li> And th0 goes to sleep in t0's queue </li>
	* <li> And I fire t3 </li>
	* <li> And t0 gets enabled </li>
	* <li> And th0 wakes up and fires t0 </li>
	* <li> And obs got only one message with t0's ID </li>
	* <li> And th0's final state is TERMINATED </li>
	* <li> And all of the other threads' states are WAITING </li>
	
	*/
	@Test
	public void threadWhoSleptWaitingForTimeSpanAndTransitionGotDisabledShouldHavePriorityOverIncomingThreads(){
		
		setUpMonitor(TIMED_PETRI_NET_02);
		
		Transition t0 = timedPetriNet.getTransitions()[0];
		Transition t1 = timedPetriNet.getTransitions()[1];
		Transition t3 = timedPetriNet.getTransitions()[3];
		
		Thread th0 = new Thread(() -> {
			try {
				Thread.currentThread().setName("th0");
				monitor.fireTransition(t0);
			} catch (IllegalTransitionFiringError | NotInitializedPetriNetException e) {
				Assert.fail("Exception thrown in test execution");
			}
		});
		
		ArrayList<Thread> workers = new ArrayList<Thread>();
		for(int i = 0; i < 10; i++){
			Thread worker = new Thread(() -> {
				try {
					monitor.fireTransition(t0);
				} catch (IllegalTransitionFiringError | NotInitializedPetriNetException e) {
					Assert.fail("Exception thrown in test execution");
				}
			});
			workers.add(worker);
		}
		
		TransitionEventObserver obs = new TransitionEventObserver();
		monitor.subscribeToTransition(t0, obs);
		
		ArrayList<String> events = obs.getEvents();
		
		timedPetriNet.initializePetriNet();
		
		th0.start();
		
		try {
			Thread.sleep(10);
		} catch (InterruptedException e) {
			Assert.fail("Interrupted thread: " + e.getMessage());
		}
		
		Assert.assertTrue(t0.getTimeSpan().anySleeping());
		
		for(Thread worker : workers){
			worker.start();
		}
		
		try {
			monitor.fireTransition(t1);
		} catch (IllegalTransitionFiringError | NotInitializedPetriNetException e) {
			Assert.fail("Exception thrown in test execution");
		}
		
		Assert.assertTrue(events.isEmpty());
		Assert.assertFalse(timedPetriNet.isEnabled(t0));
		
		try {
			Thread.sleep(200);
		} catch (InterruptedException e) {
			Assert.fail("Interrupted thread: " + e.getMessage());
		}
		
		try {
			monitor.fireTransition(t3);
		} catch (IllegalTransitionFiringError | NotInitializedPetriNetException e) {
			Assert.fail("Exception thrown in test execution");
		}
		
		Assert.assertTrue(events.isEmpty());
		Assert.assertTrue(timedPetriNet.isEnabled(t0));
		
		try {
			Thread.sleep(200);
		} catch (InterruptedException e) {
			Assert.fail("Interrupted thread: " + e.getMessage());
		}
		
		Assert.assertEquals(1, events.size());
		
		try {
			String obtainedId = jsonParser.readTree(events.get(0)).get(ID).asText();
			Assert.assertEquals(t0.getId(), obtainedId);
		} catch (IOException e) {
			Assert.fail("Event is not in JSON format");
		}
		
		Assert.assertEquals(Thread.State.TERMINATED, th0.getState());
		
		for(Thread worker : workers){
			Assert.assertEquals(Thread.State.WAITING, worker.getState());
		}
	}
}
