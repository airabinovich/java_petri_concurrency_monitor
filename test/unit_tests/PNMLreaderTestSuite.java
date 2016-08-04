package unit_tests;

import static org.junit.Assert.*;

import java.io.FileNotFoundException;
import java.util.HashMap;

import org.javatuples.Triplet;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import monitor_petri.Arco;
import monitor_petri.Etiqueta;
import monitor_petri.PNMLreader;
import monitor_petri.Plaza;
import monitor_petri.Transicion;

public class PNMLreaderTestSuite {

	private static String readerWriterPath = "resources/petriNets/readerWriter.pnml";
	private static String readerWriterPathNonPNML = "resources/petriNets/readerWriter.ndr";
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void ParseFileAndGetPetriObjectsShouldGetAllPlacesCorrectly() {
		PNMLreader reader = null;
		try {
			reader = new PNMLreader(readerWriterPath);

			Triplet<Plaza[], Transicion[], Arco[]> petriObjects = reader.parseFileAndGetPetriObjects();
			
			final int expectedPlaceAmount = 5;
			final int[] expectedMarking = { 1, 0, 5, 0, 5 };
			final int[] expectedIndexes = { 0, 1, 2, 3, 4 };
			Plaza[] obtainedPlaces = petriObjects.getValue0();
			
			assertEquals(expectedPlaceAmount, obtainedPlaces.length);
			
			for(int i = 0; i < obtainedPlaces.length; i++){
				assertEquals(expectedIndexes[i], obtainedPlaces[i].getIndice());
				assertEquals(expectedMarking[i], obtainedPlaces[i].getMarcado());
			}
		} catch (FileNotFoundException | SecurityException e) {
			fail("Could not open file " + readerWriterPath);
		}
	}
	
	@Test
	public void ParseFileAndGetPetriObjectsShouldGetAllTransitionsCorrectly() {
		PNMLreader reader = null;
		try {
			reader = new PNMLreader(readerWriterPath);
			
			Triplet<Plaza[], Transicion[], Arco[]> petriObjects = reader.parseFileAndGetPetriObjects();
			
			final int expectedTransitionsAmount = 4;
			final int[] expectedIndexes = { 0, 1, 2, 3 };
			final Etiqueta[] expectedLabels = { new Etiqueta(false, false), new Etiqueta(false, false), new Etiqueta(true, true), new Etiqueta(true, true), };
			Transicion[] obtainedTransitions = petriObjects.getValue1();
			
			assertEquals(expectedTransitionsAmount, obtainedTransitions.length);
			
			for(int i = 0; i < obtainedTransitions.length; i++){
				assertEquals(expectedIndexes[i], obtainedTransitions[i].getIndice());
				assertEquals(expectedLabels[i].isAutomatica(), obtainedTransitions[i].getEtiqueta().isAutomatica());
				assertEquals(expectedLabels[i].isInformada(), obtainedTransitions[i].getEtiqueta().isInformada());
			}
		} catch (FileNotFoundException | SecurityException e) {
			fail("Could not open file " + readerWriterPath);
		}
	}
	
	@Test
	public void ParseFileAndGetPetriObjectsShouldGetAllArcsCorrectly() {
		PNMLreader reader = null;
		try {
			reader = new PNMLreader(readerWriterPath);
			
			Triplet<Plaza[], Transicion[], Arco[]> petriObjects = reader.parseFileAndGetPetriObjects();
			
			final int expectedArcsAmount = 12;
			HashMap<String, Arco> expectedArcs = new HashMap<String, Arco>();
			expectedArcs.put("e-3DC6-6BDC2-11", new Arco("e-3DC6-6BDC2-11", "p-3DC6-6BDB5-6", "t-3DC6-6BDBA-8", 1));
			expectedArcs.put("e-3DC6-6BDC3-12", new Arco("e-3DC6-6BDC3-12", "p-3DC6-6BDAF-3", "t-3DC6-6BDBD-9", 1));
			expectedArcs.put("e-3DC6-6BDC4-13", new Arco("e-3DC6-6BDC4-13", "t-3DC6-6BDB6-7", "p-3DC6-6BDAF-3", 1));
			expectedArcs.put("e-3DC6-6BDC4-14", new Arco("e-3DC6-6BDC4-14", "p-3DC6-6BDA9-2", "t-3DC6-6BDB6-7", 1));
			expectedArcs.put("e-3DC6-6BDC6-15", new Arco("e-3DC6-6BDC6-15", "t-3DC6-6BDBF-10", "p-3DC6-6BDB5-6", 1));
			expectedArcs.put("e-3DC6-6BDC7-16", new Arco("e-3DC6-6BDC7-16", "t-3DC6-6BDBF-10", "p-3DC6-6BDB0-4", 1));
			expectedArcs.put("e-3DC6-6BDD7-17", new Arco("e-3DC6-6BDD7-17", "p-3DC6-6BDB2-5", "t-3DC6-6BDBF-10", 1));
			expectedArcs.put("e-3DC6-6BDD8-18", new Arco("e-3DC6-6BDD8-18", "t-3DC6-6BDBA-8", "p-3DC6-6BDB2-5", 1));
			expectedArcs.put("e-3DC6-6BDD8-19", new Arco("e-3DC6-6BDD8-19", "p-3DC6-6BDB0-4", "t-3DC6-6BDBA-8", 1));
			expectedArcs.put("e-3DC6-6BDD9-20", new Arco("e-3DC6-6BDD9-20", "t-3DC6-6BDBD-9", "p-3DC6-6BDB5-6", 5));
			expectedArcs.put("e-3DC6-6BDDA-21", new Arco("e-3DC6-6BDDA-21", "p-3DC6-6BDB5-6", "t-3DC6-6BDB6-7", 5));
			expectedArcs.put("e-3DC6-6BDDC-22", new Arco("e-3DC6-6BDDC-22", "t-3DC6-6BDBD-9", "p-3DC6-6BDA9-2", 1));
			
			Arco[] obtainedArcs = petriObjects.getValue2();
			
			assertEquals(expectedArcsAmount, obtainedArcs.length);
			
			for(Arco arc : obtainedArcs){
				try{
					Arco expectedArc = expectedArcs.get(arc.getId());
					assertEquals(expectedArc.getId_source(), arc.getId_source());
					assertEquals(expectedArc.getId_target(), arc.getId_target());
					assertEquals(expectedArc.getWeight(), arc.getWeight());
				} catch (IndexOutOfBoundsException ex){
					fail("An unexpected arc was recieved");
				}
			}
		} catch (FileNotFoundException | SecurityException e) {
			fail("Could not open file " + readerWriterPath);
		}
	}
	
	@Test
	public void PNMLreaderShouldThrowFileNotFoundExceptionWhenFileDoesNotExist() {
		try {
			PNMLreader reader = new PNMLreader("nonExistingFile");
			fail("Exception should be thrown before this point");
		} catch (Exception e) {
			assertEquals(e.getClass().getSimpleName(), "FileNotFoundException");
		}
	}
	
	@Test
	public void ParseFileAndGetPetriObjectsShouldThrowExceptionWhenFileIsNotPNML() {
		try {
			PNMLreader reader = new PNMLreader(readerWriterPathNonPNML);
			assertEquals(null, reader.parseFileAndGetPetriObjects());
		} catch (Exception e) {
			assertEquals(e.getClass().getSimpleName(), "SAXParseException");
		}
	}

}
