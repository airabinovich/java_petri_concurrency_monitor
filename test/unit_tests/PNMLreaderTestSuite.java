package unit_tests;

import static org.junit.Assert.*;

import java.io.FileNotFoundException;

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
			
			int[] expectedMarking = { 1, 0, 5, 0, 5 };
			int[] expectedIndexes = { 0, 1, 2, 3, 4 };
			Plaza[] obtainedPlaces = petriObjects.getValue0();
			
			assertEquals(5, obtainedPlaces.length);
			
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
			
			int[] expectedIndexes = { 0, 1, 2, 3 };
			Etiqueta[] expectedLabels = { new Etiqueta(false, false), new Etiqueta(false, false), new Etiqueta(true, true), new Etiqueta(true, true), };
			Transicion[] obtainedTransitions = petriObjects.getValue1();
			
			assertEquals(4, obtainedTransitions.length);
			
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
