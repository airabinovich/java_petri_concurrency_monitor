package unit_tests;

import java.io.FileNotFoundException;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import Petri.PNMLreader;
import Petri.PetriNet;
import Petri.PetriNetFactory;
import Petri.PetriNetFactory.petriNetType;
import Petri.Place;
import Petri.Transition;

public class PetriNetTestSuite {

	private static final String TEST_PETRI_FOLDER = "test/unit_tests/testResources/";
	private static final String READER_WRITER= TEST_PETRI_FOLDER + "readerWriter.pnml";
	private static final String PETRI_WITH_GUARD_01 = TEST_PETRI_FOLDER + "petriWithGuard01.pnml";
	
	private static PetriNetFactory factory;
	private PetriNet petriNet;

	/**
	 * @throws java.lang.Exception
	 */
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
		petriNet = null;
	}

	private void readFileAndMakePetriNet(String PNMLFile) throws FileNotFoundException, SecurityException, NullPointerException{
		PNMLreader reader = new PNMLreader(PNMLFile);
		factory = new PetriNetFactory(reader);
		petriNet = factory.makePetriNet(petriNetType.PT);
	}
	/**
	 * Test method for {@link Petri.PetriNet#fire(Petri.Transition)}.
	 */
	@Test
	public void testFireTransitionShouldReturnFalseIfTransitionIsNotEnabled() {
		try{
			readFileAndMakePetriNet(READER_WRITER);
			// When I fire t2 (not enabled) should return false
			Transition[] transitions = petriNet.getTransitions();
			Transition t2 = transitions[2];
			
			Assert.assertFalse(petriNet.isEnabled(t2));
			Assert.assertFalse(petriNet.fire(t2));
		} catch (Exception e){
			Assert.fail("Could not open or parse file " + READER_WRITER);
		}
	}
	
	/**
	 * Test method for {@link Petri.PetriNet#fire(Petri.Transition)}.
	 */
	@Test
	public void testFireTransitionShouldReturnTrueIfTransitionIsEnabled() {
		try{
			readFileAndMakePetriNet(READER_WRITER);
			// When I fire t0 (enabled) should return true
			Transition[] transitions = petriNet.getTransitions();
			Transition t0 = transitions[0];
			
			Assert.assertTrue(petriNet.isEnabled(t0));
			Assert.assertTrue(petriNet.fire(t0));
		} catch (Exception e){
			Assert.fail("Could not open or parse file " + READER_WRITER);
		}
	}

	/**
	 * Test method for {@link Petri.PetriNet#fire(Petri.Transition)}.
	 */
	@Test
	public void testFireTransitionShouldChangeMarkingWhenSucceeded() {
		try{
			readFileAndMakePetriNet(READER_WRITER);
			// When I fire t0 a token is taken from p0
			// And 5 tokens are taken from p4
			// And a token is put into p1
			Transition[] transitions = petriNet.getTransitions();
			Place[] places = petriNet.getPlaces();
			Integer[] previousMarking = petriNet.getCurrentMarking().clone();
			
			Transition t0 = transitions[0];
			Place p0 = places[0];
			Place p1 = places[1];
			Place p4 = places[4];
			
			petriNet.fire(t0);
			
			Integer[] newMarking = petriNet.getCurrentMarking();
			
			Assert.assertEquals(previousMarking[p0.getIndex()] - 1, newMarking[p0.getIndex()].intValue());
			Assert.assertEquals(previousMarking[p4.getIndex()] - 5, newMarking[p4.getIndex()].intValue());
			Assert.assertEquals(previousMarking[p1.getIndex()] + 1, newMarking[p1.getIndex()].intValue());
		} catch (Exception e){
			Assert.fail("Could not open or parse file " + READER_WRITER);
		}
	}
	
	/**
	 * Test method for {@link Petri.PetriNet#fire(Petri.Transition)}.
	 */
	@Test
	public void testFireTransitionShouldUpdateEnabledTransitions() {
		try{
			readFileAndMakePetriNet(READER_WRITER);
			// When I fire t0 (enabled) then t0 is disabled
			// And t1 is disabled
			// And t2 is enabled
			Transition[] transitions = petriNet.getTransitions();
			Transition t0 = transitions[0];
			Transition t1 = transitions[1];
			Transition t2 = transitions[2];
			
			Assert.assertTrue(petriNet.isEnabled(t0));
			
			petriNet.fire(t0);
			
			Assert.assertFalse(petriNet.isEnabled(t0));
			Assert.assertFalse(petriNet.isEnabled(t1));
			Assert.assertTrue(petriNet.isEnabled(t2));
		} catch (Exception e){
			Assert.fail("Could not open or parse file " + READER_WRITER);
		}
	}

	/**
	 * Test method for {@link Petri.PetriNet#isEnabled(Petri.Transition)}.
	 */
	@Test
	public void testIsEnabledTransitionShouldReturnTrueIfEnabled() {
		try{
			readFileAndMakePetriNet(READER_WRITER);
			// t0 starts as enabled
			Transition[] transitions = petriNet.getTransitions();
			Transition t0 = transitions[0];
			
			Assert.assertTrue(petriNet.isEnabled(t0));
		} catch (Exception e){
			Assert.fail("Could not open or parse file " + READER_WRITER);
		}
	}
	
	/**
	 * Test method for {@link Petri.PetriNet#isEnabled(Petri.Transition)}.
	 */
	@Test
	public void testIsEnabledTransitionShouldReturnFalseIfNotEnabled() {
		try{
			readFileAndMakePetriNet(READER_WRITER);
			// t2 starts as disabled
			Transition[] transitions = petriNet.getTransitions();
			Transition t2 = transitions[2];
			
			Assert.assertFalse(petriNet.isEnabled(t2));
		} catch (Exception e){
			Assert.fail("Could not open or parse file " + READER_WRITER);
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
	 * <li> When I set "test" to true </li>
	 * <li> And I fire t1 </li>
	 * <li> Then t1 should not be fired </li>
	 * <li> And p0 still has 2 tokens </li>
	 * <li> And p2 has no tokens </li>
	 */
	@Test
	public void FireTransitionDisabledByTrueGuardShouldReturnFalse() {
		try{
			readFileAndMakePetriNet(PETRI_WITH_GUARD_01);
			
			Transition t1 = petriNet.getTransitions()[1];
			
			Integer[] expectedMarking = {Integer.valueOf(2), Integer.valueOf(0), Integer.valueOf(0)};
			Assert.assertArrayEquals(expectedMarking, petriNet.getCurrentMarking());
			
			petriNet.addGuard("test", true);
			
			Assert.assertFalse(petriNet.fire(t1));
			
			Assert.assertArrayEquals(expectedMarking, petriNet.getCurrentMarking());
			
		} catch (Exception e){
			Assert.fail("Could not open or parse file " + READER_WRITER);
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
	 * <li> When I set "test" to true </li>
	 * <li> And I fire t0 </li>
	 * <li> Then t0 should be fired successfully </li>
	 * <li> And p0 has one token </li>
	 * <li> And p1 has one token </li>
	 */
	@Test
	public void FireTransitionEnabledByTrueGuardShouldReturnTrue() {
		try{
			readFileAndMakePetriNet(PETRI_WITH_GUARD_01);
			
			Transition t0 = petriNet.getTransitions()[0];
			
			Integer[] expectedMarking = {Integer.valueOf(2), Integer.valueOf(0), Integer.valueOf(0)};
			Assert.assertArrayEquals(expectedMarking, petriNet.getCurrentMarking());
			
			petriNet.addGuard("test", true);
			
			Assert.assertTrue(petriNet.fire(t0));
			
			expectedMarking[0] = 1;
			expectedMarking[1] = 1;
			Assert.assertArrayEquals(expectedMarking, petriNet.getCurrentMarking());
			
		} catch (Exception e){
			Assert.fail("Could not open or parse file " + READER_WRITER);
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
	 * <li> When I set "test" to false </li>
	 * <li> And I fire t0 </li>
	 * <li> Then t0 should not be fired </li>
	 * <li> And p0 still has 2 tokens </li>
	 * <li> And p1 has no tokens </li>
	 */
	@Test
	public void FireTransitionDisabledByFalseGuardShouldReturnFalse() {
		try{
			readFileAndMakePetriNet(PETRI_WITH_GUARD_01);
			
			Transition t0 = petriNet.getTransitions()[0];
			
			Integer[] expectedMarking = {Integer.valueOf(2), Integer.valueOf(0), Integer.valueOf(0)};
			Assert.assertArrayEquals(expectedMarking, petriNet.getCurrentMarking());
			
			petriNet.addGuard("test", false);
			
			Assert.assertFalse(petriNet.fire(t0));
			
			Assert.assertArrayEquals(expectedMarking, petriNet.getCurrentMarking());
			
		} catch (Exception e){
			Assert.fail("Could not open or parse file " + READER_WRITER);
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
	 * <li> When I set "test" to false </li>
	 * <li> And I fire t1 </li>
	 * <li> Then t1 should be fired successfully </li>
	 * <li> And p0 has one token </li>
	 * <li> And p2 has one token </li>
	 */
	@Test
	public void FireTransitionEnabledByFalseTrueGuardShouldReturnTrue() {
		try{
			readFileAndMakePetriNet(PETRI_WITH_GUARD_01);
			
			Transition t1 = petriNet.getTransitions()[1];
			
			Integer[] expectedMarking = {Integer.valueOf(2), Integer.valueOf(0), Integer.valueOf(0)};
			Assert.assertArrayEquals(expectedMarking, petriNet.getCurrentMarking());
			
			petriNet.addGuard("test", false);
			
			Assert.assertTrue(petriNet.fire(t1));
			
			expectedMarking[0] = 1;
			expectedMarking[2] = 1;
			Assert.assertArrayEquals(expectedMarking, petriNet.getCurrentMarking());
			
		} catch (Exception e){
			Assert.fail("Could not open or parse file " + READER_WRITER);
		}
	}

}
