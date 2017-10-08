package org.lac.javapetriconcurrencymonitor.test.cases;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.FileNotFoundException;

import org.javatuples.Triplet;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;
import org.unc.lac.javapetriconcurrencymonitor.parser.PnmlParser;
import org.unc.lac.javapetriconcurrencymonitor.parser.TinaPnmlParser;
import org.unc.lac.javapetriconcurrencymonitor.petrinets.PetriNet;
import org.unc.lac.javapetriconcurrencymonitor.petrinets.components.Arc;
import org.unc.lac.javapetriconcurrencymonitor.petrinets.components.Label;
import org.unc.lac.javapetriconcurrencymonitor.petrinets.components.Place;
import org.unc.lac.javapetriconcurrencymonitor.petrinets.components.TimeSpan;
import org.unc.lac.javapetriconcurrencymonitor.petrinets.components.Transition;
import org.unc.lac.javapetriconcurrencymonitor.petrinets.factory.PetriNetFactory;
import org.unc.lac.javapetriconcurrencymonitor.petrinets.factory.PetriNetFactory.petriNetType;

public class PetriNetFactoryTestSuite {

	private static PnmlParser parser;
	private static Triplet<Place[], Transition[], Arc[]> mockPetriComponents;
	private static Integer[] expectedMarking;
	private static Integer[][] expectedPre =  { {2, 0}, {1, 0}, {0, 1} };
	private static Integer[][] expectedPos = { {0, 2}, {0, 1}, {1, 0} };
	private static Integer[][] expectedInc = { {-2, 2}, {-1, 1}, {1, -1} };
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		Place p0 = new Place("p0", 2, 0, "p0");
		Place p1 = new Place("p1", 1, 1, "p1");
		Place p2 = new Place("p2", 0, 2, "p2");
		final Place[] mockPlaces = { p0, p1, p2 };
		
		Transition t0 = new Transition("t0", new Label(false, false), 0, new TimeSpan(0,0), "t0");
		Transition t1 = new Transition("t1", new Label(false, false), 1, new TimeSpan(0,0), "t1");
		final Transition[] mockTransitions = { t0, t1 };
		final Arc[] mockArcs = {
			new Arc("a0", p0, t0, 2),
			new Arc("a1", p1, t0, 1),
			new Arc("a2", t0, p2, 1),
			new Arc("a3", p2, t1, 1),
			new Arc("a4", t1, p0, 2),
			new Arc("a5", t1, p1, 1)
		};
		mockPetriComponents = new Triplet<Place[], Transition[], Arc[]>(mockPlaces, mockTransitions, mockArcs);
		
		parser = Mockito.mock(PnmlParser.class);
		Mockito.when(parser.parseFileAndGetPetriComponents()).thenReturn(mockPetriComponents);
		
		expectedMarking = new Integer[mockPlaces.length];
		for(int i = 0; i < mockPlaces.length; i++){
			expectedMarking[i] = mockPlaces[i].getMarking();
		}
	}

	@Before
	public void setUp() throws Exception {
		
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void petriNetFactoryShouldThrowExceptionWhenNullPNMLreaderIsPassed() {
		try{
			@SuppressWarnings("unused")
			PetriNetFactory factory = new PetriNetFactory((PnmlParser)null);
			fail("Exception should've be thrown before this point");
		} catch(Exception e){
			assertEquals(e.getClass().getSimpleName(), "NullPointerException");
		}
	}
	
	@Test
	public void petriNetFactoryShouldThrowExceptionWhenNullStringIsPassed() {
		try{
			@SuppressWarnings("unused")
			PetriNetFactory factory = new PetriNetFactory((String)null);
			fail("Exception should've be thrown before this point");
		} catch(Exception e){
			assertEquals(e.getClass().getSimpleName(), "NullPointerException");
		}
	}
	
	@Test
	public void petriNetFactoryShouldReturnSamePetriComponentsGottenFromPNMLreader(){
		try{
			PetriNet petriNet = new PetriNetFactory(PetriNetFactoryTestSuite.parser).makePetriNet(petriNetType.PLACE_TRANSITION);
			
			Place[] places = petriNet.getPlaces();
			Transition[] transitions = petriNet.getTransitions();
			Arc[] arcs = petriNet.getArcs();
			
			Assert.assertArrayEquals(mockPetriComponents.getValue0(), places);
			Assert.assertArrayEquals(mockPetriComponents.getValue1(), transitions);
			Assert.assertArrayEquals(mockPetriComponents.getValue2(), arcs);
			
		} catch(Exception e){
			fail("No exception should've been thrown");
		}
	}
	
	@Test
	public void petriNetFactoryShouldReturnCorrectInitialMarking(){
		try{
			PetriNet petriNet = new PetriNetFactory(PetriNetFactoryTestSuite.parser).makePetriNet(petriNetType.PLACE_TRANSITION);
			
			Integer[] obtainedMarking = petriNet.getInitialMarking();
			Assert.assertArrayEquals(expectedMarking, obtainedMarking);
			
		} catch(Exception e){
			fail("No exception should've been thrown");
		}
	}
	
	@Test
	public void petriNetFactoryShouldReturnCorrectCurrentMarking(){
		try{
			PetriNet petriNet = new PetriNetFactory(PetriNetFactoryTestSuite.parser).makePetriNet(petriNetType.PLACE_TRANSITION);
			
			Integer[] obtainedMarking = petriNet.getCurrentMarking();
			Assert.assertArrayEquals(expectedMarking, obtainedMarking);
			
		} catch(Exception e){
			fail("No exception should've been thrown");
		}
	}
	
	@Test
	public void petriNetFactoryShouldReturnCorrectPetriMatrixes(){
		try{
			PetriNet petriNet = new PetriNetFactory(PetriNetFactoryTestSuite.parser).makePetriNet(petriNetType.PLACE_TRANSITION);
			
			Integer[][] obtainedPre = petriNet.getPre();
			Integer[][] obtainedPos = petriNet.getPost();
			Integer[][] obtainedInc = petriNet.getInc();
			
			for(int i = 0; i < obtainedInc.length; i++){
				Assert.assertArrayEquals(obtainedPre[i], expectedPre[i]);
				Assert.assertArrayEquals(obtainedPos[i], expectedPos[i]);
				Assert.assertArrayEquals(obtainedInc[i], expectedInc[i]);
			}
			
		} catch(Exception e){
			fail("No exception should've been thrown");
		}
	}
	
	@Test
	public void petriNetFactoryShouldReturnPlacesInOrder(){
		try{
			PetriNet petriNet = new PetriNetFactory(PetriNetFactoryTestSuite.parser).makePetriNet(petriNetType.PLACE_TRANSITION);
			
			Place[] places = petriNet.getPlaces();
			int patternIndex = 0;
			for( Place p : places){
				Assert.assertEquals(p.getIndex(), patternIndex);
				patternIndex++;
			}
		} catch(Exception e){
			fail("No exception should've been thrown");
		}
	}
	
	@Test
	public void petriNetFactoryShouldReturnTransitionsInOrder(){
		try{
			PetriNet petriNet = new PetriNetFactory(PetriNetFactoryTestSuite.parser).makePetriNet(petriNetType.PLACE_TRANSITION);
			
			Transition[] transitions = petriNet.getTransitions();
			int patternIndex = 0;
			for( Transition t : transitions){
				Assert.assertEquals(t.getIndex(), patternIndex);
				patternIndex++;
			}
		} catch(Exception e){
			fail("No exception should've been thrown");
		}
	}
	
	/**
	 * <li> Given t1 is fed by p0 with an inhibitor arc </li>
	 * <li> And t1 is fed by p1 with a reset arc </li>
	 * <li> When the Petri Net Factory tries to create petri net</li>
	 * <li> Then it throws an error</li>
	 */
	@Test
	public void petriNetFactoryShouldThrowErrorWhenTransitionWithInputResetArcHasInhibitorInput() {
		try{
			String PNMLFile = "petriWithResetAndOtherWrongArcs01.pnml";
			PnmlParser reader = new TinaPnmlParser(PNMLFile);
			new PetriNetFactory(reader).makePetriNet(petriNetType.PLACE_TRANSITION);
			fail("Error should've be thrown before this point");
		} catch(Error e){
			assertEquals("CannotCreatePetriNetError", e.getClass().getSimpleName());
		} catch (FileNotFoundException | SecurityException | NullPointerException e) {
			fail("Incorrect exception recieved: " + e.getClass().getSimpleName());
		}
	}
	
	/**
	 * <li> Given t0 is fed by p0 with a reset arc </li>
	 * <li> And t1 is fed by p1 with a normal arc </li>
	 * <li> When the Petri Net Factory tries to create petri net</li>
	 * <li> Then it throws an error</li>
	 */
	@Test
	public void petriNetFactoryShouldThrowErrorWhenTransitionWithInputResetArcHasNormalInput() {
		try{
			String PNMLFile = "petriWithResetAndOtherWrongArcs02.pnml";
			PnmlParser reader = new TinaPnmlParser(PNMLFile);
			new PetriNetFactory(reader).makePetriNet(petriNetType.PLACE_TRANSITION);
			fail("Error should've be thrown before this point");
		} catch(Error e){
			assertEquals("CannotCreatePetriNetError", e.getClass().getSimpleName());
		} catch (FileNotFoundException | SecurityException | NullPointerException e) {
			fail("Incorrect exception recieved: " + e.getClass().getSimpleName());
		}
	}
}
