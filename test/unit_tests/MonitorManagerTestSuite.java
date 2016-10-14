package unit_tests;


import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

import Petri.PetriNet;
import Petri.PetriNetFactory;
import Petri.PetriNetFactory.petriNetType;
import Petri.Transition;
import monitor_petri.FirstInLinePolicy;
import monitor_petri.MonitorManager;
import monitor_petri.TransitionsPolicy;
import rx.Subscription;
import test_utils.TransitionEventObserver;

public class MonitorManagerTestSuite {
	
	MonitorManager monitor;
	PetriNet petri;
	static TransitionsPolicy policy;
	static PetriNetFactory factory;
	
	static ObjectMapper jsonParser;
	
	private static final String TEST_PETRI_FOLDER = "test/unit_tests/testResources/";
	private static final String MONITOR_TEST_01_PETRI = TEST_PETRI_FOLDER + "monitorTest01.pnml";
	private static final String MONITOR_TEST_02_PETRI = TEST_PETRI_FOLDER + "monitorTest02.pnml";
	private static final String MONITOR_TEST_03_PETRI = TEST_PETRI_FOLDER + "monitorTest03.pnml";
	private static final String PETRI_WITH_GUARD_01 = TEST_PETRI_FOLDER + "petriWithGuard01.pnml";
	private static final String PETRI_WITH_GUARD_02 = TEST_PETRI_FOLDER + "petriWithGuard02.pnml";
	private static final String PETRI_WITH_INHIBITOR_01 = TEST_PETRI_FOLDER + "petriWithInhibitor01.pnml";
	
	private static final String ID = "id";

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
	 * Creates builder, petri and monitor from given PNML
	 * @param PNML path to the PNML file
	 */
	private void setUpMonitor(String PNML){
		factory = new PetriNetFactory(PNML);
		petri = factory.makePetriNet(petriNetType.PT);
		monitor = new MonitorManager(petri, policy);
	}

	/**
	 * <li>Given no thread is sleeping for any transition</li>
	 * <li>And I know that the initial marking is {1, 0, 0, 0}</li>
	 * <li>And I know only t0 is enabled</li>
	 * <li>When I fire t0</li>
	 * <li>Then t1 and t2 are enabled (not testable from this scope)</li>
	 * <li>And t1 is fired for being automatic</li>
	 * <li>And the final marking is {0, 0, 1, 1}</li>
	 * <li>And t2 is enabled</li>
	 */
	@Test
	public void testFireTransitionWhenNoThreadIsSleeping() {
		
		setUpMonitor(MONITOR_TEST_01_PETRI);
		
		Integer[] expectedInitialMarking = {1, 0, 0, 0};
		Assert.assertArrayEquals(expectedInitialMarking , this.petri.getCurrentMarking());
		
		Transition t0 = petri.getTransitions()[0];
		Transition t1 = petri.getTransitions()[1];
		
		TransitionEventObserver obs = new TransitionEventObserver();
		monitor.subscribeToTransition(t1, obs);
		
		monitor.fireTransition(t0);
		
		// this means that t1 emmited an event when it was fired
		try {
			String obtainedId = jsonParser.readTree(obs.getEvents().get(0)).get(ID).asText();
			Assert.assertEquals(t1.getId(), obtainedId);
		} catch (IOException e) {
			Assert.fail("Event is not in JSON format");
		}
		
		Integer[] expectedMarkingAfterT0 = {0, 0, 1, 1};
		Assert.assertArrayEquals(expectedMarkingAfterT0 , this.petri.getCurrentMarking());
		
		boolean[] expectedEnabled = {false, false, true};
		Assert.assertArrayEquals(expectedEnabled, petri.getEnabledTransitions());
	}
	
	/**
	 * <li>Given no thread is sleeping for any transition</li>
	 * <li>And I know that the initial marking is {1, 0, 0, 0}</li>
	 * <li>And I know only t0 is enabled</li>
	 * <li>When I try to fire t2 using a worker thread</li>
	 * <li>And I wait for t2 to go to sleep</li>
	 * <li>And I fire t0</li>
	 * <li>Then t1 and t2 get enabled</li>
	 * <li>And t1 is fired for being automatic</li>
	 * <li>And t2 is fired because a worker thread was waiting for it to enable</li>
	 * <li>And the final marking is {0, 0, 0, 2}</li>
	 * <li>And no transition is enabled</li>
	 */
	@Test
	public void testFireTransitionWhenAThreadIsSleepingInT2() {
		
		setUpMonitor(MONITOR_TEST_01_PETRI);
		
		Integer[] expectedInitialMarking = {1, 0, 0, 0};
		Assert.assertArrayEquals(expectedInitialMarking , this.petri.getCurrentMarking());
		
		final Transition t0 = petri.getTransitions()[0];
		final Transition t1 = petri.getTransitions()[1];
		final Transition t2 = petri.getTransitions()[2];
		
		Thread worker = new Thread(() -> {
			monitor.fireTransition(t2);
		});
		worker.start();
		
		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		Assert.assertArrayEquals(expectedInitialMarking , petri.getCurrentMarking());
		
		TransitionEventObserver obs = new TransitionEventObserver();
		monitor.subscribeToTransition(t1, obs);
		monitor.subscribeToTransition(t2, obs);
		
		monitor.fireTransition(t0);
		
		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		ArrayList<String> events = obs.getEvents();
		
		Assert.assertEquals(2, events.size());
		try {
			String[] expectedIds = { t1.getId(), t2.getId() };
			ArrayList<String> obtainedIds = new ArrayList<String>();
			obtainedIds.add(jsonParser.readTree(events.get(0)).get(ID).asText());
			obtainedIds.add(jsonParser.readTree(events.get(1)).get(ID).asText());
			Assert.assertTrue(obtainedIds.containsAll(Arrays.asList(expectedIds)));
		} catch (IOException e) {
			Assert.fail("Event is not in JSON format");
		}
		
		try {
			boolean finishedWaiting = false;
			int retries = 10;
			while(!finishedWaiting && retries > 0){
				for( String str : obs.getEvents() ){
					finishedWaiting = str.endsWith(t2.getId());
				}
				retries--;
				Thread.sleep(10);
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		events = obs.getEvents();
		
		Assert.assertEquals(2, events.size());
		try {
			String obtainedId = jsonParser.readTree(events.get(1)).get(ID).asText();
			Assert.assertEquals(t2.getId(), obtainedId);
		} catch (IOException e) {
			Assert.fail("Event is not in JSON format");
		}
		
		Integer[] expectedMarkingAfterT0 = {0, 0, 0, 2};
		Assert.assertArrayEquals(expectedMarkingAfterT0 , petri.getCurrentMarking());
		
		boolean[] expectedEnabled = {false, false, false};
		Assert.assertArrayEquals(expectedEnabled, petri.getEnabledTransitions());
	}
	
	@Test
	public void testFireTransitionShouldThrowErrorWhenFiringAnAutomaticTransition() {
		try{
			setUpMonitor(MONITOR_TEST_01_PETRI);
			Transition t1 = petri.getTransitions()[1];
			monitor.fireTransition(t1);
			Assert.fail("An IllegalTransitionFiringError should've been thrown before this point");
		} catch (Error err){
			Assert.assertEquals("IllegalTransitionFiringError", err.getClass().getSimpleName());
		}
	}
	
	/**
	 * Given I know t1 is automatic and informed
	 * And obs is an observer subscript to t1 events
	 * When I fire t0
	 * Then t0 is fired
	 * And t1 is enabled
	 * And t1 is fired
	 * And an obs recieves an event with t1's ID
	 */
	@Test
	public void FireInformedTransitionShouldSendAnEvent(){
		
		setUpMonitor(MONITOR_TEST_01_PETRI);
		
		TransitionEventObserver obs = new TransitionEventObserver();
		
		Transition t0 = petri.getTransitions()[0];
		Transition t1 = petri.getTransitions()[1];
		
		monitor.subscribeToTransition(t1, obs);
		
		monitor.fireTransition(t0);
		
		ArrayList<String> events = obs.getEvents();
		
		Assert.assertEquals(1, events.size());
		try {
			String obtainedId = jsonParser.readTree(events.get(0)).get(ID).asText();
			Assert.assertEquals(t1.getId(), obtainedId);
		} catch (IOException e) {
			Assert.fail("Event is not in JSON format");
		}
		
	}
	
	/**
	 * Given I know t0 is not informed
	 * When I try to subscribe obs to t0
	 * Then an IllegalArgumentException is thrown
	 */
	@Test
	public void SubscribeToNotInformedTransitionShouldThrowException(){
		try{
			setUpMonitor(MONITOR_TEST_01_PETRI);
			
			TransitionEventObserver obs = new TransitionEventObserver();
			
			Transition t0 = petri.getTransitions()[0];
			
			monitor.subscribeToTransition(t0, obs);
			
			Assert.fail("An exception should've been thrown before this point");	
		} catch (Exception e){
			Assert.assertEquals("IllegalArgumentException", e.getClass().getSimpleName());
		}
	}
	
	
	/**
	 * Given I have a policy object but no petri
	 * When I try to create a monitor without petri
	 * Then an IllegalArgumentException is thrown 
	 */
	@Test
	public void CreatingMonitorWithoutPetriShouldThrowException(){
		try{
			MonitorManager aMonitor = new MonitorManager(null, policy);
			Assert.fail("An exception should've been thrown before this point");
		} catch (Exception e){
			Assert.assertEquals("IllegalArgumentException", e.getClass().getSimpleName());
		}
	}
	
	/**
	 * Given I have a petri object but no policy
	 * When I try to create a monitor without policy
	 * Then an IllegalArgumentException is thrown 
	 */
	@Test
	public void CreatingMonitorWithoutPolicyShouldThrowException(){
		try{
			MonitorManager aMonitor = new MonitorManager(petri, null);
			Assert.fail("An exception should've been thrown before this point");
		} catch (Exception e){
			Assert.assertEquals("IllegalArgumentException", e.getClass().getSimpleName());
		}
	}
	
	/**
	 * Given I know t0 is Informed and Fired
	 * And I'm registered to t0 events
	 * And t0 is enabled
	 * When I fire t0
	 * Then I get an event from the subscription
	 */
	@Test
	public void MonitorShouldSendEventWhenInformedTransitionIsFired(){
		setUpMonitor(MONITOR_TEST_02_PETRI);
		
		boolean[] expectedMarking = {true};
		Assert.assertArrayEquals(expectedMarking, petri.getEnabledTransitions());
		
		Transition t0 = petri.getTransitions()[0];
		
		TransitionEventObserver obs = new TransitionEventObserver();
		monitor.subscribeToTransition(t0, obs);
		
		Assert.assertTrue(obs.getEvents().isEmpty());
		
		monitor.fireTransition(t0);
		
		ArrayList<String> events = obs.getEvents();
		
		Assert.assertEquals(1, events.size());
		
		try {
			String obtainedId = jsonParser.readTree(events.get(0)).get(ID).asText();
			Assert.assertEquals(t0.getId(), obtainedId);
		} catch (IOException e) {
			Assert.fail("Event is not in JSON format");
		}
		
		
	}
	
	/**
	 * Given I know t0 is Informed and Fired
	 * And I'm registered to t0 events
	 * And t0 is enabled
	 * When I fire t0
	 * And I get an event from the subscription
	 * And I unsubscribe from t0 events
	 * And I fire t0
	 * Then I don't get any events
	 */
	@Test
	public void MonitorShouldNoLongerSendEventsAfterUnsubsciption(){
		setUpMonitor(MONITOR_TEST_02_PETRI);
		
		boolean[] expectedMarking = {true};
		Assert.assertArrayEquals(expectedMarking, petri.getEnabledTransitions());
		
		Transition t0 = petri.getTransitions()[0];
		
		TransitionEventObserver obs = new TransitionEventObserver();
		Subscription sub = monitor.subscribeToTransition(t0, obs);
		
		Assert.assertTrue(obs.getEvents().isEmpty());
		
		monitor.fireTransition(t0);
		
		ArrayList<String> events = obs.getEvents();
		
		Assert.assertEquals(1, events.size());
		try {
			String obtainedId = jsonParser.readTree(events.get(0)).get(ID).asText();
			Assert.assertEquals(t0.getId(), obtainedId);
		} catch (IOException e) {
			Assert.fail("Event is not in JSON format");
		}
		
		sub.unsubscribe();
		
		Assert.assertTrue(sub.isUnsubscribed());
		
		monitor.fireTransition(t0);
		
		Assert.assertEquals(1, events.size());
	}
	
	/**
	 * Given I know t0 and t1 are Informed and Fired
	 * And obs0 is registered only to t0 events
	 * And obs1 is registered only to t1 events
	 * And t0 is enabled
	 * When I fire t0
	 * And t1 gets enabled
	 * And I fire t1
	 * Then obs0 gets an event from t0's firing
	 * And obs1 gets an event from t0's firing
	 */
	@Test
	public void MonitorShouldSendEventsOnlyToSubscribedTransition(){
		setUpMonitor(MONITOR_TEST_03_PETRI);
		
		boolean[] expectedMarking = {true, false};
		Assert.assertArrayEquals(expectedMarking, petri.getEnabledTransitions());
		
		Transition t0 = petri.getTransitions()[0];
		Transition t1 = petri.getTransitions()[1];
		
		TransitionEventObserver obs0 = new TransitionEventObserver();
		monitor.subscribeToTransition(t0, obs0);
		Assert.assertTrue(obs0.getEvents().isEmpty());
		
		TransitionEventObserver obs1 = new TransitionEventObserver();
		monitor.subscribeToTransition(t1, obs1);
		
		monitor.fireTransition(t0);
		
		expectedMarking[0] = false;
		expectedMarking[1] = true;
		Assert.assertArrayEquals(expectedMarking, petri.getEnabledTransitions());
		
		monitor.fireTransition(t1);
		
		ArrayList<String> events0 = obs0.getEvents();
		ArrayList<String> events1 = obs1.getEvents();
		
		
		Assert.assertEquals(1, events0.size());
		Assert.assertEquals(1, events1.size());
		try {
			String obtainedId0 = jsonParser.readTree(events0.get(0)).get(ID).asText();
			String obtainedId1 = jsonParser.readTree(events1.get(0)).get(ID).asText();
			Assert.assertEquals(t0.getId(), obtainedId0);
			Assert.assertEquals(t1.getId(), obtainedId1);
		} catch (IOException e) {
			Assert.fail("Event is not in JSON format");
		}	
	}
	
	/**
	 * <li> Given t0 and t1 are fed by p0 </li>
	 * <li> And p0 has 2 tokens </li>
	 * <li> And arcs joining p0 to t0 and t1 have weight 1 </li>
	 * <li> And t0 has a guard "test" which expects true to fire </li>
	 * <li> And t1 has a guard "test" which expects false to fire </li>
	 * <li> And t0 feeds place p1 which starts empty </li>
	 * <li> And t1 feeds place p2 which starts empty</li>
	 * <li> And both t0 and t1 are fired and informed </li>
	 * <li> When I set "test" to true </li>
	 * <li> And obs registers to t0's and t1's events </li>
	 * <li> And th0 fires t0 </li>
	 * <li> And th1 fires t1 </li>
	 * <li> Then t0 should be fired successfully </li>
	 * <li> And t1 should not be fired </li>
	 * <li> And p0 has one token </li>
	 * <li> And p1 has one token </li>
	 * <li> And p2 has no tokens </li>
	 * <li> And obs gets one event with id matching t0's </li>
	 */
	@Test
	public void MonitorShouldFireTransitionOnlyIfGuardAllows(){
		setUpMonitor(PETRI_WITH_GUARD_01);
		
		Transition t0 = petri.getTransitions()[0];
		Transition t1 = petri.getTransitions()[1];
		
		TransitionEventObserver obs = new TransitionEventObserver();
		monitor.subscribeToTransition(t0, obs);
		monitor.subscribeToTransition(t1, obs);
		Assert.assertTrue(obs.getEvents().isEmpty());
		
		Integer[] expectedMarking = {Integer.valueOf(2), Integer.valueOf(0), Integer.valueOf(0)};
		Assert.assertArrayEquals(expectedMarking, petri.getCurrentMarking());
		
		petri.addGuard("test", true);
		
		Thread th0 = new Thread(() -> monitor.fireTransition(t0));
		th0.start();
		
		Thread th1 = new Thread(() -> monitor.fireTransition(t1));
		th1.start();
		
		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		expectedMarking[0] = 1;
		expectedMarking[1] = 1;
		expectedMarking[2] = 0;
		Assert.assertArrayEquals(expectedMarking, petri.getCurrentMarking());
		
		ArrayList<String> events = obs.getEvents();
		
		Assert.assertEquals(1, events.size());
		
		try {
			String obtainedId = jsonParser.readTree(events.get(0)).get(ID).asText();
			Assert.assertEquals(t0.getId(), obtainedId);
		} catch (IOException e) {
			Assert.fail("Event is not in JSON format");
		}
	}

	/**
	 * <li> Given t0 is enabled </li>
	 * <li> And t0 has a guard "test" which expects true to fire </li>
	 * <li> And t0 is fired and informed </li>
	 * <li> When I set "test" to false </li>
	 * <li> And obs registers to t0's events </li>
	 * <li> And th0 fires t0 </li>
	 * <li> And t0 is not fired </li>
	 * <li> And th0 sleeps in t0's varcond queue </li>
	 * <li> And I set "test" to true </li>
	 * <li> Then th0 wakes up </li>
	 * <li> And t0 is fired </li>
	 * <li> And obs gets one event with id matching t0's </li>
	 */
	@Test
	public void ThreadSleepingDueToGuardShouldWakeUpWhenGuardAllows(){
		
		setUpMonitor(PETRI_WITH_GUARD_01);
		
		Transition t0 = petri.getTransitions()[0];
		
		// setting this guard here is just to enable t0
		monitor.setGuard("test", true);
		Assert.assertTrue(petri.isEnabled(t0));
		
		monitor.setGuard("test", false);
		
		TransitionEventObserver obs = new TransitionEventObserver();
		monitor.subscribeToTransition(t0, obs);
		
		Thread th0 = new Thread(() -> monitor.fireTransition(t0));
		th0.start();
		
		try {
			// let's give th0 some time to try to fire t0
			Thread.sleep(100);
		} catch (InterruptedException e1) {
			Assert.fail("Error sleeping main thread");
		}
		
		Assert.assertTrue(obs.getEvents().isEmpty());
		
		monitor.setGuard("test", true);
		
		try {
			// let's give th0 some time to try to fire t0
			Thread.sleep(100);
		} catch (InterruptedException e1) {
			Assert.fail("Error sleeping main thread");
		}
		
		ArrayList<String> events = obs.getEvents();
		Assert.assertEquals(1, events.size());
		
		try {
			String obtainedId = jsonParser.readTree(events.get(0)).get(ID).asText();
			Assert.assertEquals(t0.getId(), obtainedId);
		} catch (IOException e) {
			Assert.fail("Event is not in JSON format");
		}
		
	}

	/**
	 * <li> Given t0 is enabled </li>
	 * <li> And t0 is fired and informed </li>
	 * <li> And t2 is automatic and informed </li>
	 * <li> And t2 has a guard "test" which expects false to fire </li>
	 * <li> When I set "test" to true </li>
	 * <li> And obs registers to t0's and t2's events </li>
	 * <li> And th0 fires t0 </li>
	 * <li> And t0 is fired successfully </li>
	 * <li> And t2 is not fired due to "test" guard </li>
	 * <li> And obs gets one event with id matching t0's </li>
	 * <li> And I set "test" to false </li>
	 * <li> Then t2 gets enabled </li>
	 * <li> And t2 is fired automatically </li>
	 * <li> And obs gets two event with ids matching t0's and t2's </li>
	 */
	@Test
	public void AutomaticTransitionDisabledDueToGuardShuldBeFiredWenGuardAllows(){
		
		setUpMonitor(PETRI_WITH_GUARD_02);
		
		Transition t0 = petri.getTransitions()[0];
		Transition t2 = petri.getTransitions()[2];
		
		// setting this guard here is just to enable t0
		monitor.setGuard("test", true);
		Assert.assertTrue(petri.isEnabled(t0));
		
		TransitionEventObserver obs = new TransitionEventObserver();
		monitor.subscribeToTransition(t0, obs);
		monitor.subscribeToTransition(t2, obs);
		
		Thread th0 = new Thread(() -> monitor.fireTransition(t0));
		th0.start();
		
		try {
			// let's give th0 some time to try to fire t0
			Thread.sleep(100);
		} catch (InterruptedException e1) {
			Assert.fail("Error sleeping main thread");
		}
		
		ArrayList<String> events = obs.getEvents();
		
		Assert.assertEquals(1, events.size());
		try {
			String obtainedId = jsonParser.readTree(events.get(0)).get(ID).asText();
			Assert.assertEquals(t0.getId(), obtainedId);
		} catch (IOException e) {
			Assert.fail("Event is not in JSON format");
		}
		
		monitor.setGuard("test", false);
		
		try {
			// let's give some time for t2 to get fired automatically
			Thread.sleep(100);
		} catch (InterruptedException e1) {
			Assert.fail("Error sleeping main thread");
		}
		
		events = obs.getEvents();
		
		Assert.assertEquals(2, events.size());
		try {
			String obtainedId = jsonParser.readTree(events.get(1)).get(ID).asText();
			Assert.assertEquals(t2.getId(), obtainedId);
		} catch (IOException e) {
			Assert.fail("Event is not in JSON format");
		}
	}
	
	/**
	 * <li> Given t0 feeds p2 </li>
	 * <li> And t1 drains p2 </li>
	 * <li> And t0 has been fired </li>
	 * <li> And p2 has one token </li>
	 * <li> And p0 has two tokens </li>
	 * <li> And t2 is fed by p0 and inhibited by p2 </li>
	 * <li> When th0 fires t2 </li>
	 * <li> And t2 is not fired </li>
	 * <li> And th1 fires t1 </li>
	 * <li> And t1 is successfully fired </li>
	 * <li> Then th0 wakes up</li>
	 * <li> And t2 is successfully fired </li> 
	 */
	@Test
	public void ThreadSleepingIntransitionDisabledByInhibitionShouldWakeUpWhenInhibitionDissapears(){
		
		setUpMonitor(PETRI_WITH_INHIBITOR_01);
		
		Transition[] transitions = petri.getTransitions();
		Transition t0 = transitions[0];
		Transition t1 = transitions[1];
		Transition t2 = transitions[2];
		
		TransitionEventObserver obs = new TransitionEventObserver();
		monitor.subscribeToTransition(t1, obs);
		monitor.subscribeToTransition(t2, obs);
		
		new Thread(() -> monitor.fireTransition(t0)).start();
		
		try{
			Thread.sleep(100);
		} catch(InterruptedException e){
			Assert.fail("Interrupted thread: " + e.getMessage());
		}
		
		Integer[] expectedMarking = {Integer.valueOf(2), Integer.valueOf(0), Integer.valueOf(1)};
		Assert.assertArrayEquals(expectedMarking, petri.getCurrentMarking());
		
		// initial condition generate and check here finishes here
		
		Thread th0 = new Thread(() -> monitor.fireTransition(t2));
		th0.start();
		
		try{
			Thread.sleep(100);
		} catch(InterruptedException e){
			Assert.fail("Interrupted thread: " + e.getMessage());
		}
		
		ArrayList<String> events = obs.getEvents();
		
		Assert.assertTrue(events.isEmpty());
		
		Thread th1 = new Thread(() -> monitor.fireTransition(t1));
		th1.start();
		
		try{
			Thread.sleep(100);
		} catch(InterruptedException e){
			Assert.fail("Interrupted thread: " + e.getMessage());
		}
		
		Assert.assertFalse(events.isEmpty());
		try {
			String obtainedId0 = jsonParser.readTree(events.get(0)).get(ID).asText();
			Assert.assertEquals(t1.getId(), obtainedId0);
			String obtainedId1 = jsonParser.readTree(events.get(1)).get(ID).asText();
			Assert.assertEquals(t2.getId(), obtainedId1);
		} catch (IOException e) {
			Assert.fail("Event is not in JSON format");
		}
	}
	
	/**
	 * <li> Given t1 is disabled </li>
	 * <li> When th0 perennial fires t1 </li>
	 * <li> Then th0 doesn't go to sleep </li>
	 */
	@Test
	public void PerennialFiringShouldNotSendAThreadToSleepWhenTransitionIsDisabled(){
		
		setUpMonitor(MONITOR_TEST_03_PETRI);
		
		Transition t1 = petri.getTransitions()[1];
		
		Thread th0 = new Thread(() -> monitor.fireTransition(t1, true));
		th0.start();
		
		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
			Assert.fail("Interrupted thread: " + e.getMessage());
		}
		
		Assert.assertFalse(monitor.getQueuesState()[1]);
	}
	
	/**
	 * <li> Given t1 is disabled </li>
	 * <li> And p1 feeds t1 </li>
	 * <li> And t1 feeds p0 </li>
	 * <li> And p0 has one token </li>
	 * <li> And p1 has no tokens </li>
	 * <li> And obs subscribes to t1's events </li>
	 * <li> When th0 perennial fires t1 </li>
	 * <li> Then obs gets no events </li>
	 * <li> And the marking didn't change </li>
	 */
	@Test
	public void PerennialFiringShouldNotFireTransitionWhenTransitionIsDisabled(){
		
		setUpMonitor(MONITOR_TEST_03_PETRI);
		
		Transition t1 = petri.getTransitions()[1];
		
		Integer[] expectedMarking = {1, 0};
		Assert.assertArrayEquals(expectedMarking, petri.getCurrentMarking());
		
		TransitionEventObserver obs = new TransitionEventObserver();
		monitor.subscribeToTransition(t1, obs);
		
		ArrayList<String> events = obs.getEvents();
		
		Assert.assertTrue(events.isEmpty());
		
		Thread th0 = new Thread(() -> monitor.fireTransition(t1, true));
		th0.start();
		
		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
			Assert.fail("Interrupted thread: " + e.getMessage());
		}
		
		Assert.assertTrue(events.isEmpty());
		
		Assert.assertArrayEquals(expectedMarking, petri.getCurrentMarking());
	}
	
	/**
	 * <li> Given t0 is enabled </li>
	 * <li> And p0 feeds t0 </li>
	 * <li> And t0 feeds p1 </li>
	 * <li> And p0 has one token </li>
	 * <li> And p1 has no tokens </li>
	 * <li> And obs subscribes to t0's events </li>
	 * <li> When th0 perennial fires t0 </li>
	 * <li> Then obs gets one events matching t0's id </li>
	 * <li> And p0 has no tokens </li>
	 * <li> And p1 has one token </li>
	 */
	@Test
	public void PerennialFiringShouldFireTransitionWhenTransitionIsEnabled(){
		
		setUpMonitor(MONITOR_TEST_03_PETRI);
		
		Transition t0 = petri.getTransitions()[0];
		
		Integer[] expectedMarking = {1, 0};
		Assert.assertArrayEquals(expectedMarking, petri.getCurrentMarking());
		
		TransitionEventObserver obs = new TransitionEventObserver();
		monitor.subscribeToTransition(t0, obs);
		
		ArrayList<String> events = obs.getEvents();
		
		Assert.assertTrue(events.isEmpty());
		
		Thread th0 = new Thread(() -> monitor.fireTransition(t0, true));
		th0.start();
		
		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
			Assert.fail("Interrupted thread: " + e.getMessage());
		}
		
		Assert.assertFalse(events.isEmpty());
		try {
			String obtainedId0 = jsonParser.readTree(events.get(0)).get(ID).asText();
			Assert.assertEquals(t0.getId(), obtainedId0);
		} catch (IOException e) {
			Assert.fail("Event is not in JSON format");
		}
		
		expectedMarking[0] = 0;
		expectedMarking[1] = 1;
		Assert.assertArrayEquals(expectedMarking, petri.getCurrentMarking());
		
	}
}
