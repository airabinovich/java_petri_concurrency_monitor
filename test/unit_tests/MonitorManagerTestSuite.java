package unit_tests;


import java.io.IOException;
import java.util.ArrayList;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

import Petri.PetriNet;
import Petri.PetriNet.PetriNetBuilder;
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
	static PetriNetBuilder builder;
	
	static ObjectMapper jsonParser;
	
	private static final String MonitorTest01Petri = "test/unit_tests/testResources/monitorTest01.pnml";
	private static final String MonitorTest02Petri = "test/unit_tests/testResources/monitorTest02.pnml";
	private static final String MonitorTest03Petri = "test/unit_tests/testResources/monitorTest03.pnml";
	
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
		builder = new PetriNetBuilder(PNML);
		petri = builder.buildPetriNet();
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
		
		setUpMonitor(MonitorTest01Petri);
		
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
	 * <li>Then t1 and t2 are enabled (not testable from this scope)</li>
	 * <li>And t1 is fired for being automatic</li>
	 * <li>And t2 is fired because a worker thread was waiting for it to enable</li>
	 * <li>And the final marking is {0, 0, 0, 2}</li>
	 * <li>And no transition is enabled</li>
	 */
	@Test
	public void testFireTransitionWhenAThreadIsSleepingInT2() {
		
		setUpMonitor(MonitorTest01Petri);
		
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
		
		Assert.assertArrayEquals(expectedInitialMarking , this.petri.getCurrentMarking());
		
		TransitionEventObserver obs = new TransitionEventObserver();
		monitor.subscribeToTransition(t1, obs);
		monitor.subscribeToTransition(t2, obs);
		
		monitor.fireTransition(t0);
		
		ArrayList<String> events = obs.getEvents();
		
		Assert.assertEquals(1, events.size());
		try {
			String obtainedId = jsonParser.readTree(events.get(0)).get(ID).asText();
			Assert.assertEquals(t1.getId(), obtainedId);
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
		Assert.assertArrayEquals(expectedMarkingAfterT0 , this.petri.getCurrentMarking());
		
		boolean[] expectedEnabled = {false, false, false};
		Assert.assertArrayEquals(expectedEnabled, petri.getEnabledTransitions());
	}
	
	@Test
	public void testFireTransitionShouldThrowErrorWhenFiringAnAutomaticTransition() {
		try{
			setUpMonitor(MonitorTest01Petri);
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
		
		setUpMonitor(MonitorTest01Petri);
		
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
			setUpMonitor(MonitorTest01Petri);
			
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
		setUpMonitor(MonitorTest02Petri);
		
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
		setUpMonitor(MonitorTest02Petri);
		
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
		setUpMonitor(MonitorTest03Petri);
		
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
	

}
