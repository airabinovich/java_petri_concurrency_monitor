package unit_tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.javatuples.Triplet;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;

import Petri.Arc;
import Petri.Label;
import Petri.PNMLreader;
import Petri.PetriNet;
import Petri.Place;
import Petri.Transition;
import Petri.PetriNet.PetriNetBuilder;

public class PetriNetBuilderTestSuite {

	private static PNMLreader reader;
	private static Triplet<Place[], Transition[], Arc[]> mockPetriObjects;
	private static Integer[] expectedMarking;
	private static Integer[][] expectedPre =  { {2, 0}, {1, 0}, {0, 1} };
	private static Integer[][] expectedPos = { {0, 2}, {0, 1}, {1, 0} };
	private static Integer[][] expectedInc = { {-2, 2}, {-1, 1}, {1, -1} };
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		final Place[] mockPlaces = {
			new Place("p0", 2, 0),
			new Place("p1", 1, 1),
			new Place("p2", 0, 2)
		};
		final Transition[] mockTransitions = {
			new Transition("t0", new Label(false, false), 0),
			new Transition("t1", new Label(false, false), 1)
		};
		final Arc[] mockArcs = {
			new Arc("a0", "p0", "t0", 2),
			new Arc("a1", "p1", "t0", 1),
			new Arc("a2", "t0", "p2", 1),
			new Arc("a3", "p2", "t1", 1),
			new Arc("a4", "t1", "p0", 2),
			new Arc("a5", "t1", "p1", 1)
		};
		mockPetriObjects = new Triplet<Place[], Transition[], Arc[]>(mockPlaces, mockTransitions, mockArcs);
		
		reader = Mockito.mock(PNMLreader.class);
		Mockito.when(reader.parseFileAndGetPetriObjects()).thenReturn(mockPetriObjects);
		
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
	public void petriNetBuilderShouldThrowExceptionWhenNullPNMLreaderIsPassed() {
		try{
			@SuppressWarnings("unused")
			PetriNetBuilder builder = new PetriNetBuilder((PNMLreader)null);
			fail("Exception should've be thrown before this point");
		} catch(Exception e){
			assertEquals(e.getClass().getSimpleName(), "NullPointerException");
		}
	}
	
	@Test
	public void petriNetBuilderShouldThrowExceptionWhenNullStringIsPassed() {
		try{
			@SuppressWarnings("unused")
			PetriNetBuilder builder = new PetriNetBuilder((String)null);
			fail("Exception should've be thrown before this point");
		} catch(Exception e){
			assertEquals(e.getClass().getSimpleName(), "NullPointerException");
		}
	}
	
	@Test
	public void petriNetBuilderShouldReturnSamePetriObjectsGottenFromPNMLreader(){
		try{
			PetriNet petriNet = new PetriNetBuilder(PetriNetBuilderTestSuite.reader).buildPetriNet();
			
			Place[] places = petriNet.getPlaces();
			Transition[] transitions = petriNet.getTransitions();
			Arc[] arcs = petriNet.getArcs();
			
			Assert.assertArrayEquals(mockPetriObjects.getValue0(), places);
			Assert.assertArrayEquals(mockPetriObjects.getValue1(), transitions);
			Assert.assertArrayEquals(mockPetriObjects.getValue2(), arcs);
			
		} catch(Exception e){
			fail("No exception should've been thrown");
		}
	}
	
	@Test
	public void petriNetBuilderShouldReturnCorrectInitialMarking(){
		try{
			PetriNet petriNet = new PetriNetBuilder(PetriNetBuilderTestSuite.reader).buildPetriNet();
			
			Integer[] obtainedMarking = petriNet.getInitialMarking();
			Assert.assertArrayEquals(expectedMarking, obtainedMarking);
			
		} catch(Exception e){
			fail("No exception should've been thrown");
		}
	}
	
	@Test
	public void petriNetBuilderShouldReturnCorrectCurrentMarking(){
		try{
			PetriNet petriNet = new PetriNetBuilder(PetriNetBuilderTestSuite.reader).buildPetriNet();
			
			Integer[] obtainedMarking = petriNet.getCurrentMarking();
			Assert.assertArrayEquals(expectedMarking, obtainedMarking);
			
		} catch(Exception e){
			fail("No exception should've been thrown");
		}
	}
	
	@Test
	public void petriNetBuilderShouldReturnCorrectPetriMatrixes(){
		try{
			PetriNet petriNet = new PetriNetBuilder(PetriNetBuilderTestSuite.reader).buildPetriNet();
			
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
	public void petriNetBuilderShouldReturnPlacesInOrder(){
		try{
			PetriNet petriNet = new PetriNetBuilder(PetriNetBuilderTestSuite.reader).buildPetriNet();
			
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
	public void petriNetBuilderShouldReturnTransitionsInOrder(){
		try{
			PetriNet petriNet = new PetriNetBuilder(PetriNetBuilderTestSuite.reader).buildPetriNet();
			
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

}
