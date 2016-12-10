package org.lac.javapetriconcurrencymonitor.test.cases;

import java.io.FileNotFoundException;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.unc.lac.javapetriconcurrencymonitor.exceptions.PetriNetException;
import org.unc.lac.javapetriconcurrencymonitor.parser.PnmlParser;
import org.unc.lac.javapetriconcurrencymonitor.petrinets.PetriNet;
import org.unc.lac.javapetriconcurrencymonitor.petrinets.PetriNetFireOutcome;
import org.unc.lac.javapetriconcurrencymonitor.petrinets.components.Place;
import org.unc.lac.javapetriconcurrencymonitor.petrinets.components.Transition;
import org.unc.lac.javapetriconcurrencymonitor.petrinets.factory.PetriNetFactory;
import org.unc.lac.javapetriconcurrencymonitor.petrinets.factory.PetriNetFactory.petriNetType;

public class PetriNetTestSuite {

	private static final String TEST_PETRI_FOLDER = "test/org/lac/javapetriconcurrencymonitor/test/resources/";
	private static final String READER_WRITER= TEST_PETRI_FOLDER + "readerWriter.pnml";
	private static final String PETRI_WITH_GUARD_01 = TEST_PETRI_FOLDER + "petriWithGuard01.pnml";
	private static final String PETRI_WITH_INHIBITOR_01 = TEST_PETRI_FOLDER + "petriWithInhibitor01.pnml";
	private static final String PETRI_WITH_RESET_01 = TEST_PETRI_FOLDER + "petriWithReset01.pnml";
	private static final String PETRI_WITH_RESET_02 = TEST_PETRI_FOLDER + "petriWithReset02.pnml";
	private static final String PETRI_WITH_RESET_03 = TEST_PETRI_FOLDER + "petriWithReset03.pnml";
	private static final String PETRI_WITH_READER_01 = TEST_PETRI_FOLDER + "petriWithReader01.pnml";
	private static final String TIMED_PETRI_NET = TEST_PETRI_FOLDER + "timedPetri.pnml";
	private static final String TIMED_PETRI_NET_02 = TEST_PETRI_FOLDER + "timedPetri02.pnml";
	
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
		readFileAndMakePetriNet(PNMLFile, petriNetType.PLACE_TRANSITION);
	}
	
	private void readFileAndMakePetriNet(String PNMLFile, petriNetType type) throws FileNotFoundException, SecurityException, NullPointerException{
		PnmlParser reader = new PnmlParser(PNMLFile);
		factory = new PetriNetFactory(reader);
		petriNet = factory.makePetriNet(type);
	}
	/**
	 * Test method for {@link org.unc.lac.javapetriconcurrencymonitor.petrinets.PetriNet#fire(org.unc.lac.javapetriconcurrencymonitor.petrinets.components.Transition)}.
	 */
	@Test
	public void testFireTransitionShouldReturnFalseIfTransitionIsNotEnabled() {
		try{
			readFileAndMakePetriNet(READER_WRITER);
			
			petriNet.initializePetriNet();
			
			// When I fire t2 (not enabled) should return false
			Transition[] transitions = petriNet.getTransitions();
			Transition t2 = transitions[2];
			
			Assert.assertFalse(petriNet.isEnabled(t2));
			try {
				Assert.assertEquals(PetriNetFireOutcome.NOT_ENABLED, petriNet.fire(t2));
			} catch (PetriNetException e) {
				Assert.fail("Exception should've not been thrown. Cause:" + e.getCause());
			}
		} catch (FileNotFoundException | SecurityException | NullPointerException e){
			Assert.fail("Could not open or parse file " + READER_WRITER);
		} catch (IllegalArgumentException e) {
			Assert.fail(e.getMessage());
		}
	}
	
	/**
	 * Test method for {@link org.unc.lac.javapetriconcurrencymonitor.petrinets.PetriNet#fire(org.unc.lac.javapetriconcurrencymonitor.petrinets.components.Transition)}.
	 */
	@Test
	public void testFireTransitionShouldReturnTrueIfTransitionIsEnabled() {
		try{
			readFileAndMakePetriNet(READER_WRITER);
			
			petriNet.initializePetriNet();
			
			// When I fire t0 (enabled) should return true
			Transition[] transitions = petriNet.getTransitions();
			Transition t0 = transitions[0];
			
			Assert.assertTrue(petriNet.isEnabled(t0));
			try {
				Assert.assertEquals(PetriNetFireOutcome.SUCCESS, petriNet.fire(t0));
			} catch (PetriNetException e) {
				Assert.fail("Exception should've not been thrown. Cause:" + e.getCause());
			}
		} catch (FileNotFoundException | SecurityException | NullPointerException e){
			Assert.fail("Could not open or parse file " + READER_WRITER);
		} catch (IllegalArgumentException e) {
			Assert.fail(e.getMessage());
		}
	}

	/**
	 * Test method for {@link org.unc.lac.javapetriconcurrencymonitor.petrinets.PetriNet#fire(org.unc.lac.javapetriconcurrencymonitor.petrinets.components.Transition)}.
	 */
	@Test
	public void testFireTransitionShouldChangeMarkingWhenSucceeded() {
		try{
			readFileAndMakePetriNet(READER_WRITER);
			
			petriNet.initializePetriNet();
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
			
			try {
				petriNet.fire(t0);
			} catch (PetriNetException e) {
				Assert.fail("Exception should've not been thrown. Cause:" + e.getCause());
			}
			
			Integer[] newMarking = petriNet.getCurrentMarking();
			
			Assert.assertEquals(previousMarking[p0.getIndex()] - 1, newMarking[p0.getIndex()].intValue());
			Assert.assertEquals(previousMarking[p4.getIndex()] - 5, newMarking[p4.getIndex()].intValue());
			Assert.assertEquals(previousMarking[p1.getIndex()] + 1, newMarking[p1.getIndex()].intValue());
		} catch (FileNotFoundException | SecurityException | NullPointerException e){
			Assert.fail("Could not open or parse file " + READER_WRITER);
		} catch (IllegalArgumentException e) {
			Assert.fail(e.getMessage());
		}
	}
	
	/**
	 * Test method for {@link org.unc.lac.javapetriconcurrencymonitor.petrinets.PetriNet#fire(org.unc.lac.javapetriconcurrencymonitor.petrinets.components.Transition)}.
	 */
	@Test
	public void testFireTransitionShouldUpdateEnabledTransitions() {
		try{
			readFileAndMakePetriNet(READER_WRITER);
			
			petriNet.initializePetriNet();
			// When I fire t0 (enabled) then t0 is disabled
			// And t1 is disabled
			// And t2 is enabled
			Transition[] transitions = petriNet.getTransitions();
			Transition t0 = transitions[0];
			Transition t1 = transitions[1];
			Transition t2 = transitions[2];
			
			Assert.assertTrue(petriNet.isEnabled(t0));
			
			try {
				petriNet.fire(t0);
			} catch (PetriNetException e) {
				Assert.fail("Exception should've not been thrown. Cause:" + e.getCause());
			}
			
			Assert.assertFalse(petriNet.isEnabled(t0));
			Assert.assertFalse(petriNet.isEnabled(t1));
			Assert.assertTrue(petriNet.isEnabled(t2));
		} catch (FileNotFoundException | SecurityException | NullPointerException e){
			Assert.fail("Could not open or parse file " + READER_WRITER);
		} catch (IllegalArgumentException e) {
			Assert.fail(e.getMessage());
		}
	}

	/**
	 * Test method for {@link org.unc.lac.javapetriconcurrencymonitor.petrinets.PetriNet#isEnabled(org.unc.lac.javapetriconcurrencymonitor.petrinets.components.Transition)}.
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
	 * Test method for {@link org.unc.lac.javapetriconcurrencymonitor.petrinets.PetriNet#isEnabled(org.unc.lac.javapetriconcurrencymonitor.petrinets.components.Transition)}.
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
			
			petriNet.initializePetriNet();
			
			Transition t1 = petriNet.getTransitions()[1];
			
			Integer[] expectedMarking = {Integer.valueOf(2), Integer.valueOf(0), Integer.valueOf(0)};
			Assert.assertArrayEquals(expectedMarking, petriNet.getCurrentMarking());
			
			petriNet.addGuard("test", true);
			
			try {
				Assert.assertEquals(PetriNetFireOutcome.NOT_ENABLED, petriNet.fire(t1));
			} catch (PetriNetException e) {
				Assert.fail("Exception should've not been thrown. Cause:" + e.getCause());
			}
			
			Assert.assertArrayEquals(expectedMarking, petriNet.getCurrentMarking());
			
		} catch (FileNotFoundException | SecurityException | NullPointerException e){
			Assert.fail("Could not open or parse file " + PETRI_WITH_GUARD_01);
		} catch (IllegalArgumentException e) {
			Assert.fail(e.getMessage());
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
			
			petriNet.initializePetriNet();
			
			Transition t0 = petriNet.getTransitions()[0];
			
			Integer[] expectedMarking = {Integer.valueOf(2), Integer.valueOf(0), Integer.valueOf(0)};
			Assert.assertArrayEquals(expectedMarking, petriNet.getCurrentMarking());
			
			petriNet.addGuard("test", true);
			
			try {
				Assert.assertEquals(PetriNetFireOutcome.SUCCESS, petriNet.fire(t0));
			} catch (PetriNetException e) {
				Assert.fail("Exception should've not been thrown. Cause:" + e.getCause());
			}
			
			expectedMarking[0] = 1;
			expectedMarking[1] = 1;
			Assert.assertArrayEquals(expectedMarking, petriNet.getCurrentMarking());
			
		} catch (FileNotFoundException | SecurityException | NullPointerException e){
			Assert.fail("Could not open or parse file " + PETRI_WITH_GUARD_01);
		} catch (IllegalArgumentException e) {
			Assert.fail(e.getMessage());
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
			
			petriNet.initializePetriNet();
			
			Transition t0 = petriNet.getTransitions()[0];
			
			Integer[] expectedMarking = {Integer.valueOf(2), Integer.valueOf(0), Integer.valueOf(0)};
			Assert.assertArrayEquals(expectedMarking, petriNet.getCurrentMarking());
			
			petriNet.addGuard("test", false);
			
			try {
				Assert.assertEquals(PetriNetFireOutcome.NOT_ENABLED, petriNet.fire(t0));
			} catch (PetriNetException e) {
				Assert.fail("Exception should've not been thrown. Cause:" + e.getCause());
			}
			
			Assert.assertArrayEquals(expectedMarking, petriNet.getCurrentMarking());
			
		} catch (FileNotFoundException | SecurityException | NullPointerException e){
			Assert.fail("Could not open or parse file " + PETRI_WITH_GUARD_01);
		} catch (IllegalArgumentException e) {
			Assert.fail(e.getMessage());
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
			
			petriNet.initializePetriNet();
			
			Transition t1 = petriNet.getTransitions()[1];
			
			Integer[] expectedMarking = {Integer.valueOf(2), Integer.valueOf(0), Integer.valueOf(0)};
			Assert.assertArrayEquals(expectedMarking, petriNet.getCurrentMarking());
			
			petriNet.addGuard("test", false);
			
			try {
				Assert.assertEquals(PetriNetFireOutcome.SUCCESS, petriNet.fire(t1));
			} catch (PetriNetException e) {
				Assert.fail("Exception should've not been thrown. Cause:" + e.getCause());
			}
			
			expectedMarking[0] = 1;
			expectedMarking[2] = 1;
			Assert.assertArrayEquals(expectedMarking, petriNet.getCurrentMarking());
			
		} catch (FileNotFoundException | SecurityException | NullPointerException e){
			Assert.fail("Could not open or parse file " + PETRI_WITH_GUARD_01);
		} catch (IllegalArgumentException e) {
			Assert.fail(e.getMessage());
		}
	}

	/**
	 * <li> Given p0 has two tokens </li>
	 * <li> And t0 feeds p2 </li>
	 * <li> And p2 has no tokens </li>
	 * <li> And t2 is fed by p0 and inhibited by p2 </li>
	 * <li> And t2 is enabled </li>
	 * <li> When I fire t0 </li>
	 * <li> And a token goes to p2 </li>
	 * <li> Then t2 is disabled</li> 
	 */
	@Test
	public void TransitionGetsDisabledByInhibitorArc(){
		try{
			readFileAndMakePetriNet(PETRI_WITH_INHIBITOR_01);
			
			petriNet.initializePetriNet();
			
			Transition t0 = petriNet.getTransitions()[0];
			Transition t2 = petriNet.getTransitions()[2];
			
			Integer[] expectedMarking = {Integer.valueOf(2) , Integer.valueOf(0), Integer.valueOf(0)};
			Assert.assertArrayEquals(expectedMarking, petriNet.getCurrentMarking());
			
			Assert.assertTrue(petriNet.isEnabled(t2));
			
			try {
				petriNet.fire(t0);
			} catch (PetriNetException e) {
				Assert.fail("Exception should've not been thrown. Cause:" + e.getCause());
			}
			
			expectedMarking[2] = 1;
			Assert.assertArrayEquals(expectedMarking, petriNet.getCurrentMarking());
			
			Assert.assertFalse(petriNet.isEnabled(t2));
			
		} catch (FileNotFoundException | SecurityException | NullPointerException e){
			Assert.fail("Could not open or parse file " + PETRI_WITH_INHIBITOR_01);
		} catch (IllegalArgumentException e) {
			Assert.fail(e.getMessage());
		}
	}
	
	/**
	 * <li> Given p0 has two tokens </li>
	 * <li> And t0 feeds p2 </li>
	 * <li> And p2 has no tokens </li>
	 * <li> And t2 is fed by p0 and inhibited by p2 </li>
	 * <li> And t2 is enabled </li>
	 * <li> And I fire t0 </li>
	 * <li> And a token goes to p2 </li>
	 * <li> When I try to fire t2 </li>
	 * <li> Then t2 is not fired </li>
	 */
	@Test
	public void FireTransitionDisabledByInhibitorArcShouldReturnFalse(){
		try{
			readFileAndMakePetriNet(PETRI_WITH_INHIBITOR_01);
			
			petriNet.initializePetriNet();
			
			Transition t0 = petriNet.getTransitions()[0];
			Transition t2 = petriNet.getTransitions()[2];
			
			Integer[] expectedMarking = {Integer.valueOf(2) , Integer.valueOf(0), Integer.valueOf(0)};
			Assert.assertArrayEquals(expectedMarking, petriNet.getCurrentMarking());
			
			Assert.assertTrue(petriNet.isEnabled(t2));
			
			try {
				petriNet.fire(t0);
			} catch (PetriNetException e) {
				Assert.fail("Exception should've not been thrown. Cause:" + e.getCause());
			}
			
			expectedMarking[2] = 1;
			Assert.assertArrayEquals(expectedMarking, petriNet.getCurrentMarking());
			
			try {
				Assert.assertEquals(PetriNetFireOutcome.NOT_ENABLED, petriNet.fire(t2));
			} catch (PetriNetException e) {
				Assert.fail("Exception should've not been thrown. Cause:" + e.getCause());
			}
			
		} catch (FileNotFoundException | SecurityException | NullPointerException e){
			Assert.fail("Could not open or parse file " + PETRI_WITH_INHIBITOR_01);
		} catch (IllegalArgumentException e) {
			Assert.fail(e.getMessage());
		}
	}
	
	/**
	 * <li> Given p0 has two tokens </li>
	 * <li> And p2 has no tokens </li>
	 * <li> And t2 is fed by p0 and inhibited by p2 </li>
	 * <li> And t2 is enabled </li>
	 * <li> When I try to fire t2 </li>
	 * <li> Then t2 is fired successfully </li>
	 * <li> And a token is taken from p0 </li>
	 * <li> And a token is put into p1</li>
	 */
	@Test
	public void FireTransitionNotDisabledByInhibitorArcShouldReturnTrue(){
		try{
			readFileAndMakePetriNet(PETRI_WITH_INHIBITOR_01);
			
			petriNet.initializePetriNet();
			
			Transition t2 = petriNet.getTransitions()[2];
			
			Integer[] expectedMarking = {Integer.valueOf(2) , Integer.valueOf(0), Integer.valueOf(0)};
			Assert.assertArrayEquals(expectedMarking, petriNet.getCurrentMarking());
			
			Assert.assertTrue(petriNet.isEnabled(t2));
			
			try {
				Assert.assertEquals(PetriNetFireOutcome.SUCCESS, petriNet.fire(t2));
			} catch (PetriNetException e) {
				Assert.fail("Exception should've not been thrown. Cause:" + e.getCause());
			}
			
			expectedMarking[0] = 1;
			expectedMarking[1] = 1;
			Assert.assertArrayEquals(expectedMarking, petriNet.getCurrentMarking());
			
			
		} catch (FileNotFoundException | SecurityException | NullPointerException e){
			Assert.fail("Could not open or parse file " + PETRI_WITH_INHIBITOR_01);
		} catch (IllegalArgumentException e) {
			Assert.fail(e.getMessage());
		}
	}
	
	/**
	 * <li> Given p3 has four tokens </li>
	 * <li> And p4 has no tokens </li>
	 * <li> And t3 is fed by p3 with a reset arc </li>
	 * <li> and t3 feeds p4 with normal arc </li>
	 * <li> And t3 is enabled </li>
	 * <li> When I try to fire t3 </li>
	 * <li> Then t3 is fired successfully </li>
	 * <li> And all tokens (four) are taken from p3 </li>
	 * <li> And a token is put into p4</li>
	 */
	@Test
	public void FireTransitionWithResetArcShouldEmptySourcePlace(){
		try{
			readFileAndMakePetriNet(PETRI_WITH_RESET_01);
			
			petriNet.initializePetriNet();
			
			Transition t3 = petriNet.getTransitions()[3];
			
			Integer[] expectedMarking = {Integer.valueOf(1) , Integer.valueOf(1), Integer.valueOf(1), Integer.valueOf(4), Integer.valueOf(0)};
			Assert.assertArrayEquals(expectedMarking, petriNet.getCurrentMarking());
			
			Assert.assertTrue(petriNet.isEnabled(t3));
			
			try {
				Assert.assertEquals(PetriNetFireOutcome.SUCCESS ,petriNet.fire(t3));
			} catch (PetriNetException e) {
				Assert.fail("Exception should've not been thrown. Cause:" + e.getCause());
			}
			
			expectedMarking[3] = 0;
			expectedMarking[4] = 1;
			Assert.assertArrayEquals(expectedMarking, petriNet.getCurrentMarking());
			Assert.assertEquals(expectedMarking[3].intValue(), petriNet.getPlaces()[3].getMarking());
			Assert.assertEquals(expectedMarking[4].intValue(), petriNet.getPlaces()[4].getMarking());
			
		} catch (FileNotFoundException | SecurityException | NullPointerException e){
			Assert.fail("Could not open or parse file " + PETRI_WITH_RESET_01);
		} catch (IllegalArgumentException e) {
			Assert.fail(e.getMessage());
		}
	}
	
	/**
	 * <li> Given p3 and p4 have no tokens </li>
	 * <li> And t3 is fed by p3 with a reset arc </li>
	 * <li> and t3 feeds p4 with normal arc </li>
	 * <li> And t3 is not enabled </li>
	 * <li> When I try to fire t3 </li>
	 * <li> Then t3 is not fired successfully </li>
	 * <li> And the current marking does not change </li>
	 */
	@Test
	public void FireTransitionWithResetArcShouldNotBeEnabledWhenSourcePlaceHasNoMarking(){
		try{
			readFileAndMakePetriNet(PETRI_WITH_RESET_02);
			
			petriNet.initializePetriNet();
			
			Transition t3 = petriNet.getTransitions()[3];
			
			Integer[] expectedMarking = {Integer.valueOf(1) , Integer.valueOf(1), Integer.valueOf(1), Integer.valueOf(0), Integer.valueOf(0)};
			Assert.assertArrayEquals(expectedMarking, petriNet.getCurrentMarking());
			
			Assert.assertFalse(petriNet.isEnabled(t3));
			
			try {
				Assert.assertEquals(PetriNetFireOutcome.NOT_ENABLED, petriNet.fire(t3));
			} catch (PetriNetException e) {
				Assert.fail("Exception should've not been thrown. Cause:" + e.getCause());
			}
			
			Assert.assertArrayEquals(expectedMarking, petriNet.getCurrentMarking());
			
		} catch (FileNotFoundException | SecurityException | NullPointerException e){
			Assert.fail("Could not open or parse file " + PETRI_WITH_RESET_02);
		} catch (IllegalArgumentException e) {
			Assert.fail(e.getMessage());
		}
	}
	
	/**
	 * <li> Given p0 has four tokens </li>
	 * <li> And p1, p2 and p3 have no tokens </li>
	 * <li> And t0 is fed by p0 with a reset arc </li>
	 * <li> and t3 feeds p1, p2 and p3 with normal arcs </li>
	 * <li> And t3 is enabled </li>
	 * <li> When I try to fire t3 </li>
	 * <li> Then t3 is fired successfully </li>
	 * <li> And all tokens (four) are taken from p0 </li>
	 * <li> And a token is put into p1, p2 and p3</li>
	 */
	@Test
	public void FireTransitionWithResetArcAndOtherOutputArcsShouldFireSuccesfully(){
		try{
			readFileAndMakePetriNet(PETRI_WITH_RESET_03);
			
			petriNet.initializePetriNet();
			
			Transition t0 = petriNet.getTransitions()[0];
			
			Integer[] expectedMarking = {Integer.valueOf(4) , Integer.valueOf(0), Integer.valueOf(0), Integer.valueOf(0)};
			Assert.assertArrayEquals(expectedMarking, petriNet.getCurrentMarking());
			
			Assert.assertTrue(petriNet.isEnabled(t0));
			
			try {
				Assert.assertEquals(PetriNetFireOutcome.SUCCESS, petriNet.fire(t0));
			} catch (PetriNetException e) {
				Assert.fail("Exception should've not been thrown. Cause:" + e.getCause());
			}
			
			expectedMarking[0] = 0;
			expectedMarking[1] = 1;
			expectedMarking[2] = 1;
			expectedMarking[3] = 1;
			Assert.assertArrayEquals(expectedMarking, petriNet.getCurrentMarking());
			
		} catch (FileNotFoundException | SecurityException | NullPointerException e){
			Assert.fail("Could not open or parse file " + PETRI_WITH_RESET_03);
		} catch (IllegalArgumentException e) {
			Assert.fail(e.getMessage());
		}
	}
	
	/**
	 * <li> Given p0 feeds t2 with a normal arc with weight 1 </li>
	 * <li> And p2 feeds t2 with a reader arc with weight 1 </li>
	 * <li> And p0 has 2 tokens </li>
	 * <li> And p2 has no tokens </li>
	 * <li> When I fire t2 </li>
	 * <li> Then the fire returns {@link PetriNetFireOutcome#NOT_ENABLED} </li>
	 * <li> And no tokens are drained from p0 </li>
	 */
	@Test
	public void FireTransitionWithReaderArcAndNoEnoughTokensShouldReturnNotEnabled(){
		try{
			readFileAndMakePetriNet(PETRI_WITH_READER_01);
			
			petriNet.initializePetriNet();
			
			Transition t2 = petriNet.getTransitions()[2];
			
			Integer[] expectedMarking = { 2, 0, 0 };
			
			Assert.assertArrayEquals(expectedMarking, petriNet.getCurrentMarking());
			
			try {
				Assert.assertEquals(PetriNetFireOutcome.NOT_ENABLED, petriNet.fire(t2));
			} catch (PetriNetException e) {
				Assert.fail("Exception should've not been thrown. Cause:" + e.getCause());
			}
			
			Assert.assertArrayEquals(expectedMarking, petriNet.getCurrentMarking());
			
		} catch (FileNotFoundException | SecurityException | NullPointerException e){
			Assert.fail("Could not open or parse file " + PETRI_WITH_READER_01);
		} catch (IllegalArgumentException e) {
			Assert.fail(e.getMessage());
		}
	}
	
	/**
	 * <li> Given p0 feeds t2 with a normal arc with weight 1 </li>
	 * <li> And p2 feeds t2 with a reader arc with weight 1 </li>
	 * <li> And t2 feeds p1 </li>
	 * <li> And p0 has 2 tokens </li>
	 * <li> And p1 has no tokens </li>
	 * <li> And p2 has 1 token </li>
	 * <li> When I fire t2 </li>
	 * <li> Then the fire returns {@link PetriNetFireOutcome#SUCCESS} </li>
	 * <li> And one token is drained from p0 </li>
	 * <li> And one token is put into p1 </li>
	 * <li> And no tokens are drained from p2 </li>
	 */
	@Test
	public void FireTransitionWithReaderArcShouldDrainTokensFromAllPlacesExceptTheOneWithTheReaderArc(){
		try{
			readFileAndMakePetriNet(PETRI_WITH_READER_01);
			
			petriNet.initializePetriNet();
			
			Transition t0 = petriNet.getTransitions()[0];
			try {
				Assert.assertEquals(PetriNetFireOutcome.SUCCESS, petriNet.fire(t0));
			} catch (PetriNetException e) {
				Assert.fail("Exception should've not been thrown. Cause:" + e.getCause());
			}
			
			Transition t2 = petriNet.getTransitions()[2];
			
			Integer[] expectedMarking = { 2, 0, 1 };
			
			Assert.assertArrayEquals(expectedMarking, petriNet.getCurrentMarking());
			
			try {
				Assert.assertEquals(PetriNetFireOutcome.SUCCESS, petriNet.fire(t2));
			} catch (PetriNetException e) {
				Assert.fail("Exception should've not been thrown. Cause:" + e.getCause());
			}
			
			expectedMarking[0] = 1;
			expectedMarking[1] = 1;
			
			Assert.assertArrayEquals(expectedMarking, petriNet.getCurrentMarking());
			
		} catch (FileNotFoundException | SecurityException | NullPointerException e){
			Assert.fail("Could not open or parse file " + PETRI_WITH_READER_01);
		} catch (IllegalArgumentException e) {
			Assert.fail(e.getMessage());
		}
	}
	
	/**
	 * <li> Given t0 is timed [500, 2000] </li>
	 * <li> When I fire t0 before 500ms since initialization </li>
	 * <li> Then {@link PetriNetFireOutcome#TIMED_BEFORE_TIMESPAN} is returned </li>
	 */
	@Test
	public void testFireTimedTransitionBeforeTimespanShouldReturnError(){
		try{
			readFileAndMakePetriNet(TIMED_PETRI_NET, petriNetType.TIMED);
			
			petriNet.initializePetriNet();
			
			Transition t0 = petriNet.getTransitions()[0];
			try {
				Assert.assertEquals(PetriNetFireOutcome.TIMED_BEFORE_TIMESPAN, petriNet.fire(t0));
			} catch (Exception e) {
				Assert.fail("Exception thrown during test execution. " + e.getMessage());
			}
			
		} catch (FileNotFoundException | SecurityException | NullPointerException e){
			Assert.fail("Could not open or parse file " + PETRI_WITH_READER_01);
		} catch (IllegalArgumentException e) {
			Assert.fail(e.getMessage());
		}
		
		Assert.assertTrue(true);
	}
	
	/**
	 * <li> Given t0 is timed [50, 100] </li>
	 * <li> When I fire t0 after 100ms since initialization </li>
	 * <li> Then {@link PetriNetFireOutcome#TIMED_AFTER_TIMESPAN} is returned </li>
	 */
	@Test
	public void testFireTimedTransitionAfterTimespanShouldReturnError(){
		try{
			readFileAndMakePetriNet(TIMED_PETRI_NET_02, petriNetType.TIMED);
			
			petriNet.initializePetriNet();
			
			try {
				Thread.sleep(101);
			} catch (InterruptedException e1) {
				Assert.fail("Main thread interrupted during test execution");
			}
			
			Transition t0 = petriNet.getTransitions()[0];
			try {
				Assert.assertEquals(PetriNetFireOutcome.TIMED_AFTER_TIMESPAN, petriNet.fire(t0));
			} catch (Exception e) {
				Assert.fail("Exception thrown during test execution. " + e.getMessage());
			}
			
		} catch (FileNotFoundException | SecurityException | NullPointerException e){
			Assert.fail("Could not open or parse file " + PETRI_WITH_READER_01);
		} catch (IllegalArgumentException e) {
			Assert.fail(e.getMessage());
		}
	}
	
	/**
	 * <li> Given p4 has 5 tokens </li>
	 * <li> When I try to get p4 by name </li>
	 * <li> And ask its tokens amount </li>
	 * <li> Then A copy of p4 is returned into p4_aux </li>
	 * <li> And p4_aux has 5 tokens </li>
	 */
	@Test
	public void testIfAPlaceCouldBeGetByName(){
		try{
			readFileAndMakePetriNet(READER_WRITER);
			
			petriNet.initializePetriNet();
			
			Place p4_aux = petriNet.getPlace("p4");
			Assert.assertEquals(5, p4_aux.getMarking());
		} catch (Exception e){
			Assert.fail("Could not open or parse file " + READER_WRITER);
		}
	}
	
	/**
	 * <li> Given petriNet has no place named  "ghost"</li>
	 * <li> When I try to get ghost by name </li>
	 * <li> Then IllegalArgumentException is thrown </li>
	 */
	@Test
	public void testIfAIllegalArgumentExceptionIsThrownGettingAPlaceThatDoesntExist(){
		try{
			readFileAndMakePetriNet(READER_WRITER);
			
			petriNet.initializePetriNet();
			@SuppressWarnings("unused")
			Place ghost = new Place(petriNet.getPlace("ghost"));
			Assert.fail("The exception should be thrown before");
		} catch (Exception e) {
			Assert.assertEquals(IllegalArgumentException.class, e.getClass());
		}
	}
}
