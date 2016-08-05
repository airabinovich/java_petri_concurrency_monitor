package unit_tests;

import static org.junit.Assert.*;

import org.javatuples.Septet;
import org.javatuples.Triplet;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;

import monitor_petri.Arco;
import monitor_petri.Etiqueta;
import monitor_petri.PNMLreader;
import monitor_petri.Plaza;
import monitor_petri.RdPBuilder;
import monitor_petri.Transicion;

public class RdPBuilderTestSuite {

	private static PNMLreader reader;
	private static Triplet<Plaza[], Transicion[], Arco[]> mockPetriObjects;
	private static Integer[] expectedMarking;
	private static Integer[][] expectedPre =  { {2, 0}, {1, 0}, {0, 1} };
	private static Integer[][] expectedPos = { {0, 2}, {0, 1}, {1, 0} };
	private static Integer[][] expectedInc = { {-2, 2}, {-1, 1}, {1, -1} };
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		final Plaza[] mockPlaces = {
			new Plaza("p0", 2, 0),
			new Plaza("p1", 1, 1),
			new Plaza("p2", 0, 2)
		};
		final Transicion[] mockTransitions = {
			new Transicion("t0", new Etiqueta(false, false), 0),
			new Transicion("t1", new Etiqueta(false, false), 1)
		};
		final Arco[] mockArcs = {
			new Arco("a0", "p0", "t0", 2),
			new Arco("a1", "p1", "t0", 1),
			new Arco("a2", "t0", "p2", 1),
			new Arco("a3", "p2", "t1", 1),
			new Arco("a4", "t1", "p0", 2),
			new Arco("a5", "t1", "p1", 1)
		};
		mockPetriObjects = new Triplet<Plaza[], Transicion[], Arco[]>(mockPlaces, mockTransitions, mockArcs);
		
		reader = Mockito.mock(PNMLreader.class);
		Mockito.when(reader.parseFileAndGetPetriObjects()).thenReturn(mockPetriObjects);
		
		expectedMarking = new Integer[mockPlaces.length];
		for(int i = 0; i < mockPlaces.length; i++){
			expectedMarking[i] = mockPlaces[i].getMarcado();
		}
	}

	@Before
	public void setUp() throws Exception {
		
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void RdPBuilderShouldThrowExceptionWhenNullPNMLreaderIsPassed() {
		try{
			@SuppressWarnings("unused")
			RdPBuilder builder = new RdPBuilder((PNMLreader)null);
			fail("Exception should've be thrown before this point");
		} catch(Exception e){
			assertEquals(e.getClass().getSimpleName(), "NullPointerException");
		}
	}
	
	@Test
	public void RdPBuilderShouldThrowExceptionWhenNullStringIsPassed() {
		try{
			@SuppressWarnings("unused")
			RdPBuilder builder = new RdPBuilder((String)null);
			fail("Exception should've be thrown before this point");
		} catch(Exception e){
			assertEquals(e.getClass().getSimpleName(), "NullPointerException");
		}
	}
	
	@Test
	public void RdPBuilderShouldReturnSamePetriObjectsGottenFromPNMLreader(){
		try{
			RdPBuilder builder = new RdPBuilder(RdPBuilderTestSuite.reader);
			Septet<Plaza[], Transicion[], Arco[], Integer[], Integer[][], Integer[][], Integer[][]> petriObjects = builder.buildPetriNetObjects();
			
			Assert.assertArrayEquals(mockPetriObjects.getValue0(), petriObjects.getValue0());
			Assert.assertArrayEquals(mockPetriObjects.getValue1(), petriObjects.getValue1());
			Assert.assertArrayEquals(mockPetriObjects.getValue2(), petriObjects.getValue2());
			
		} catch(Exception e){
			fail("No exception should've been thrown");
		}
	}
	
	@Test
	public void RdPBuilderShouldReturnCorrectInitialMarking(){
		try{
			RdPBuilder builder = new RdPBuilder(RdPBuilderTestSuite.reader);
			Septet<Plaza[], Transicion[], Arco[], Integer[], Integer[][], Integer[][], Integer[][]> petriObjects = builder.buildPetriNetObjects();
			
			Integer[] obtainedMarking = petriObjects.getValue3();
			Assert.assertArrayEquals(expectedMarking, obtainedMarking);
			
		} catch(Exception e){
			fail("No exception should've been thrown");
		}
	}
	
	@Test
	public void RdPBuilderShouldReturnCorrectPetriMatrixes(){
		try{
			RdPBuilder builder = new RdPBuilder(RdPBuilderTestSuite.reader);
			Septet<Plaza[], Transicion[], Arco[], Integer[], Integer[][], Integer[][], Integer[][]> petriObjects = builder.buildPetriNetObjects();
			
			Integer[][] obtainedPre = petriObjects.getValue4();
			Integer[][] obtainedPos = petriObjects.getValue5();
			Integer[][] obtainedInc = petriObjects.getValue6();
			
			for(int i = 0; i < obtainedInc.length; i++){
				Assert.assertArrayEquals(obtainedPre[i], expectedPre[i]);
				Assert.assertArrayEquals(obtainedPos[i], expectedPos[i]);
				Assert.assertArrayEquals(obtainedInc[i], expectedInc[i]);
			}
			
		} catch(Exception e){
			fail("No exception should've been thrown");
		}
	}

}
