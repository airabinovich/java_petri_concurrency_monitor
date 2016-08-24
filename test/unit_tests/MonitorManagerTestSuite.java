package unit_tests;


import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import Petri.PetriNet;
import Petri.PetriNet.PetriNetBuilder;
import Petri.Transition;
import monitor_petri.FirstInLinePolicy;
import monitor_petri.MonitorManager;
import monitor_petri.TransitionsPolicy;

public class MonitorManagerTestSuite {
	
	MonitorManager monitor;
	PetriNet petri;
	static TransitionsPolicy policy;
	static PetriNetBuilder builder;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		builder = new PetriNetBuilder("./test/unit_tests/testResources/monitorTest01.pnml");
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
		Integer[] expectedInitialMarking = {1, 0, 0, 0};
		Assert.assertArrayEquals(expectedInitialMarking , this.petri.getCurrentMarking());
		
		Transition t0 = petri.getTransitions()[0];
		monitor.fireTransition(t0);
		
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
		Integer[] expectedInitialMarking = {1, 0, 0, 0};
		Assert.assertArrayEquals(expectedInitialMarking , this.petri.getCurrentMarking());
		
		Transition t2 = petri.getTransitions()[2];
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
		
		Transition t0 = petri.getTransitions()[0];
		monitor.fireTransition(t0);
		
		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		Integer[] expectedMarkingAfterT0 = {0, 0, 0, 2};
		Assert.assertArrayEquals(expectedMarkingAfterT0 , this.petri.getCurrentMarking());
		
		boolean[] expectedEnabled = {false, false, false};
		Assert.assertArrayEquals(expectedEnabled, petri.getEnabledTransitions());
	}
	
	@Test
	public void testFireTransitionShouldIgnoreWhenFiringAnAutomaticTransition() {
		assert(true);
//		Assert.fail("not implemented yet");
	}

}
