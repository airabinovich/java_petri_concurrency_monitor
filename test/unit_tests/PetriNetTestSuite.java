package unit_tests;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import Petri.PNMLreader;
import Petri.PetriNet;
import Petri.PetriNetFactory;
import Petri.Place;
import Petri.Transition;

public class PetriNetTestSuite {

	private static PetriNetFactory factory;
	private PetriNet testedNet;

	/**
	 * @throws java.lang.Exception
	 */
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		PNMLreader reader = new PNMLreader("test/unit_tests/testResources/readerWriter.pnml");
		factory = new PetriNetFactory(reader);
	}

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		testedNet = factory.makePetriNet("PT");
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
		testedNet = null;
	}

	/**
	 * Test method for {@link Petri.PetriNet#fire(Petri.Transition)}.
	 */
	@Test
	public void testFireTransitionShouldReturnFalseIfTransitionIsNotEnabled() {
		// When I fire t2 (not enabled) should return false
		Transition[] transitions = testedNet.getTransitions();
		Transition t2 = transitions[2];
		
		Assert.assertFalse(testedNet.isEnabled(t2));
		Assert.assertFalse(testedNet.fire(t2));
	}
	
	/**
	 * Test method for {@link Petri.PetriNet#fire(Petri.Transition)}.
	 */
	@Test
	public void testFireTransitionShouldReturnTrueIfTransitionIsEnabled() {
		// When I fire t0 (enabled) should return true
		Transition[] transitions = testedNet.getTransitions();
		Transition t0 = transitions[0];
		
		Assert.assertTrue(testedNet.isEnabled(t0));
		Assert.assertTrue(testedNet.fire(t0));
	}

	/**
	 * Test method for {@link Petri.PetriNet#fire(Petri.Transition)}.
	 */
	@Test
	public void testFireTransitionShouldChangeMarkingWhenSucceeded() {
		// When I fire t0 a token is taken from p0
		// And 5 tokens are taken from p4
		// And a token is put into p1
		Transition[] transitions = testedNet.getTransitions();
		Place[] places = testedNet.getPlaces();
		Integer[] previousMarking = testedNet.getCurrentMarking().clone();
		
		Transition t0 = transitions[0];
		Place p0 = places[0];
		Place p1 = places[1];
		Place p4 = places[4];
		
		testedNet.fire(t0);
		
		Integer[] newMarking = testedNet.getCurrentMarking();
		
		Assert.assertEquals(previousMarking[p0.getIndex()] - 1, newMarking[p0.getIndex()].intValue());
		Assert.assertEquals(previousMarking[p4.getIndex()] - 5, newMarking[p4.getIndex()].intValue());
		Assert.assertEquals(previousMarking[p1.getIndex()] + 1, newMarking[p1.getIndex()].intValue());
	}
	
	/**
	 * Test method for {@link Petri.PetriNet#fire(Petri.Transition)}.
	 */
	@Test
	public void testFireTransitionShouldUpdateEnabledTransitions() {
		// When I fire t0 (enabled) then t0 is disabled
		// And t1 is disabled
		// And t2 is enabled
		Transition[] transitions = testedNet.getTransitions();
		Transition t0 = transitions[0];
		Transition t1 = transitions[1];
		Transition t2 = transitions[2];
		
		Assert.assertTrue(testedNet.isEnabled(t0));
		
		testedNet.fire(t0);
		
		Assert.assertFalse(testedNet.isEnabled(t0));
		Assert.assertFalse(testedNet.isEnabled(t1));
		Assert.assertTrue(testedNet.isEnabled(t2));
	}

	/**
	 * Test method for {@link Petri.PetriNet#isEnabled(Petri.Transition)}.
	 */
	@Test
	public void testIsEnabledTransitionShouldReturnTrueIfEnabled() {
		// t0 starts as enabled
		Transition[] transitions = testedNet.getTransitions();
		Transition t0 = transitions[0];
		
		Assert.assertTrue(testedNet.isEnabled(t0));
	}
	
	/**
	 * Test method for {@link Petri.PetriNet#isEnabled(Petri.Transition)}.
	 */
	@Test
	public void testIsEnabledTransitionShouldReturnFalseIfNotEnabled() {
		// t2 starts as disabled
		Transition[] transitions = testedNet.getTransitions();
		Transition t2 = transitions[2];
		
		Assert.assertFalse(testedNet.isEnabled(t2));
	}

}
