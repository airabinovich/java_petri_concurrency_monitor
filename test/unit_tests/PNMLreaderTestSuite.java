package unit_tests;

import static org.junit.Assert.*;

import java.io.FileNotFoundException;
import java.util.HashMap;

import org.javatuples.Triplet;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import Petri.Arc;
import Petri.Label;
import Petri.PNMLreader;
import Petri.Place;
import Petri.Transition;

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

			Triplet<Place[], Transition[], Arc[]> petriObjects = reader.parseFileAndGetPetriObjects();
			
			final int expectedPlaceAmount = 5;
			final int[] expectedMarking = { 1, 0, 5, 0, 5 };
			final int[] expectedIndexes = { 0, 1, 2, 3, 4 };
			Place[] obtainedPlaces = petriObjects.getValue0();
			
			assertEquals(expectedPlaceAmount, obtainedPlaces.length);
			
			for(int i = 0; i < obtainedPlaces.length; i++){
				assertEquals(expectedIndexes[i], obtainedPlaces[i].getIndex());
				assertEquals(expectedMarking[i], obtainedPlaces[i].getMarking());
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
			
			Triplet<Place[], Transition[], Arc[]> petriObjects = reader.parseFileAndGetPetriObjects();
			
			final int expectedTransitionsAmount = 4;
			final int[] expectedIndexes = { 0, 1, 2, 3 };
			final Label[] expectedLabels = { new Label(false, false), new Label(false, false), new Label(true, true), new Label(true, true), };
			Transition[] obtainedTransitions = petriObjects.getValue1();
			
			assertEquals(expectedTransitionsAmount, obtainedTransitions.length);
			
			for(int i = 0; i < obtainedTransitions.length; i++){
				assertEquals(expectedIndexes[i], obtainedTransitions[i].getIndex());
				assertEquals(expectedLabels[i].isAutomatic(), obtainedTransitions[i].getLabel().isAutomatic());
				assertEquals(expectedLabels[i].isInformed(), obtainedTransitions[i].getLabel().isInformed());
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
			
			Triplet<Place[], Transition[], Arc[]> petriObjects = reader.parseFileAndGetPetriObjects();
			
			final int expectedArcsAmount = 12;
			HashMap<String, Arc> expectedArcs = new HashMap<String, Arc>();
			expectedArcs.put("e-3DC6-6BDC2-11", new Arc("e-3DC6-6BDC2-11", "p-3DC6-6BDB5-6", "t-3DC6-6BDBA-8", 1));
			expectedArcs.put("e-3DC6-6BDC3-12", new Arc("e-3DC6-6BDC3-12", "p-3DC6-6BDAF-3", "t-3DC6-6BDBD-9", 1));
			expectedArcs.put("e-3DC6-6BDC4-13", new Arc("e-3DC6-6BDC4-13", "t-3DC6-6BDB6-7", "p-3DC6-6BDAF-3", 1));
			expectedArcs.put("e-3DC6-6BDC4-14", new Arc("e-3DC6-6BDC4-14", "p-3DC6-6BDA9-2", "t-3DC6-6BDB6-7", 1));
			expectedArcs.put("e-3DC6-6BDC6-15", new Arc("e-3DC6-6BDC6-15", "t-3DC6-6BDBF-10", "p-3DC6-6BDB5-6", 1));
			expectedArcs.put("e-3DC6-6BDC7-16", new Arc("e-3DC6-6BDC7-16", "t-3DC6-6BDBF-10", "p-3DC6-6BDB0-4", 1));
			expectedArcs.put("e-3DC6-6BDD7-17", new Arc("e-3DC6-6BDD7-17", "p-3DC6-6BDB2-5", "t-3DC6-6BDBF-10", 1));
			expectedArcs.put("e-3DC6-6BDD8-18", new Arc("e-3DC6-6BDD8-18", "t-3DC6-6BDBA-8", "p-3DC6-6BDB2-5", 1));
			expectedArcs.put("e-3DC6-6BDD8-19", new Arc("e-3DC6-6BDD8-19", "p-3DC6-6BDB0-4", "t-3DC6-6BDBA-8", 1));
			expectedArcs.put("e-3DC6-6BDD9-20", new Arc("e-3DC6-6BDD9-20", "t-3DC6-6BDBD-9", "p-3DC6-6BDB5-6", 5));
			expectedArcs.put("e-3DC6-6BDDA-21", new Arc("e-3DC6-6BDDA-21", "p-3DC6-6BDB5-6", "t-3DC6-6BDB6-7", 5));
			expectedArcs.put("e-3DC6-6BDDC-22", new Arc("e-3DC6-6BDDC-22", "t-3DC6-6BDBD-9", "p-3DC6-6BDA9-2", 1));
			
			Arc[] obtainedArcs = petriObjects.getValue2();
			
			assertEquals(expectedArcsAmount, obtainedArcs.length);
			
			for(Arc arc : obtainedArcs){
				try{
					Arc expectedArc = expectedArcs.get(arc.getId());
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
			@SuppressWarnings("unused")
			PNMLreader reader = new PNMLreader("nonExistingFile");
			fail("Exception should've be thrown before this point");
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
