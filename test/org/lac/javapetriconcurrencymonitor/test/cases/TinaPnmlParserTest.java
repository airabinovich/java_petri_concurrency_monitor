package org.lac.javapetriconcurrencymonitor.test.cases;

import static org.junit.Assert.*;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import org.javatuples.Triplet;
import org.junit.Test;
import org.unc.lac.javapetriconcurrencymonitor.errors.DuplicatedIdError;
import org.unc.lac.javapetriconcurrencymonitor.errors.DuplicatedNameError;
import org.unc.lac.javapetriconcurrencymonitor.exceptions.BadPnmlFormatException;
import org.unc.lac.javapetriconcurrencymonitor.parser.TinaPnmlParser;
import org.unc.lac.javapetriconcurrencymonitor.petrinets.components.Arc;
import org.unc.lac.javapetriconcurrencymonitor.petrinets.components.Label;
import org.unc.lac.javapetriconcurrencymonitor.petrinets.components.Place;
import org.unc.lac.javapetriconcurrencymonitor.petrinets.components.Transition;
import org.unc.lac.javapetriconcurrencymonitor.petrinets.components.Arc.ArcType;

public class TinaPnmlParserTest {

	private static final String TEST_PETRI_FOLDER = "test/org/lac/javapetriconcurrencymonitor/test/resources/";
	private static final String READER_WRITER= TEST_PETRI_FOLDER + "readerWriter.pnml";
	private static final String READER_WRITER_NON_PNML = TEST_PETRI_FOLDER + "readerWriter.ndr";
	private static final String TIMED_PETRI_NET = TEST_PETRI_FOLDER + "timedPetriForReader.pnml";
	private static final String PETRI_WITH_GUARD_01 = TEST_PETRI_FOLDER + "petriWithGuard01.pnml";
	private static final String PETRI_WITH_GUARD_BAD_FORMAT_01 = TEST_PETRI_FOLDER + "petriWithGuardBadFormat01.pnml";
	private static final String PETRI_WITH_INHIBITOR_01 = TEST_PETRI_FOLDER + "petriWithInhibitor01.pnml";
	private static final String PETRI_WITH_READER_01 = TEST_PETRI_FOLDER + "petriWithReader01.pnml";
	private static final String PETRI_WITH_CUSTOM_NAMES = TEST_PETRI_FOLDER + "petriWithCustomNames.pnml";
	private static final String PETRI_WITH_DUPLICATED_NAMES_PLACE = TEST_PETRI_FOLDER + "petriWithDuplicatedNamesPlace.pnml";
	private static final String PETRI_WITH_DUPLICATED_NAMES_TRANSITION = TEST_PETRI_FOLDER + "petriWithDuplicatedNamesTransition.pnml";
	private static final String PETRI_WITH_DUPLICATED_IDS_PLACE = TEST_PETRI_FOLDER + "petriWithDuplicatedIdsPlace.pnml";
	private static final String PETRI_WITH_DUPLICATED_IDS_TRANSITION = TEST_PETRI_FOLDER + "petriWithDuplicatedIdsTransition.pnml";
	

	@Test
	public void testParseFileAndGetPetriComponentsShouldGetAllPlacesCorrectly() {
		TinaPnmlParser reader = null;
		try {
			reader = new TinaPnmlParser(READER_WRITER);

			Triplet<Place[], Transition[], Arc[]> petriComponents = reader.parseFileAndGetPetriComponents();
			
			final int expectedPlaceAmount = 5;
			final int[] expectedMarking = { 1, 0, 5, 0, 5 };
			final int[] expectedIndexes = { 0, 1, 2, 3, 4 };
			Place[] obtainedPlaces = petriComponents.getValue0();
			
			assertEquals(expectedPlaceAmount, obtainedPlaces.length);
			
			for(int i = 0; i < obtainedPlaces.length; i++){
				assertEquals(expectedIndexes[i], obtainedPlaces[i].getIndex());
				assertEquals(expectedMarking[i], obtainedPlaces[i].getMarking());
			}
		} catch (FileNotFoundException | SecurityException e) {
			fail("Could not open file " + READER_WRITER);
		} catch (BadPnmlFormatException e) {
			fail(e.getMessage());
		}
	}
	
	@Test
	public void testParseFileAndGetPetriComponentsShouldGetAllTransitionsCorrectly() {
		TinaPnmlParser reader = null;
		try {
			reader = new TinaPnmlParser(READER_WRITER);
			
			Triplet<Place[], Transition[], Arc[]> petriComponents = reader.parseFileAndGetPetriComponents();
			
			final int expectedTransitionsAmount = 4;
			final int[] expectedIndexes = { 0, 1, 2, 3 };
			final Label[] expectedLabels = { new Label(false, false), new Label(false, false), new Label(true, true), new Label(true, true), };
			Transition[] obtainedTransitions = petriComponents.getValue1();
			
			assertEquals(expectedTransitionsAmount, obtainedTransitions.length);
			
			for(int i = 0; i < obtainedTransitions.length; i++){
				assertEquals(expectedIndexes[i], obtainedTransitions[i].getIndex());
				assertEquals(expectedLabels[i].isAutomatic(), obtainedTransitions[i].getLabel().isAutomatic());
				assertEquals(expectedLabels[i].isInformed(), obtainedTransitions[i].getLabel().isInformed());
			}
		} catch (FileNotFoundException | SecurityException e) {
			fail("Could not open file " + READER_WRITER);
		} catch (BadPnmlFormatException e) {
			fail(e.getMessage());
		}
	}
	
	@Test
	public void testParseFileAndGetPetriComponentsShouldGetAllArcsCorrectly() {
		TinaPnmlParser reader = null;
		try {
			reader = new TinaPnmlParser(READER_WRITER);
			
			Triplet<Place[], Transition[], Arc[]> petriComponents = reader.parseFileAndGetPetriComponents();
			
			final int expectedArcsAmount = 12;
			HashMap<String, Arc> expectedArcs = new HashMap<String, Arc>();
			Place p0 = new Place("p-3DC6-6BDA9-2", 1, 0, "p0");
			Place p1 = new Place("p-3DC6-6BDAF-3", 0, 1, "p1");
			Place p2 = new Place("p-3DC6-6BDB0-4", 5, 2, "p2");
			Place p3 = new Place("p-3DC6-6BDB2-5", 0, 3, "p3");
			Place p4 = new Place("p-3DC6-6BDB5-6", 5, 4, "p4");
			Transition t0 = new Transition("t-3DC6-6BDB6-7", new Label(false, false), 0, "t0");
			Transition t1 = new Transition("t-3DC6-6BDBA-8", new Label(false, false), 1, "t1");
			Transition t2 = new Transition("t-3DC6-6BDBD-9", new Label(true, true), 2, "t2");
			Transition t3 = new Transition("t-3DC6-6BDBF-10", new Label(true, true), 3, "t3");
			expectedArcs.put("e-3DC6-6BDC2-11", new Arc("e-3DC6-6BDC2-11", p4, t1, 1));
			expectedArcs.put("e-3DC6-6BDC3-12", new Arc("e-3DC6-6BDC3-12", p1, t2, 1));
			expectedArcs.put("e-3DC6-6BDC4-13", new Arc("e-3DC6-6BDC4-13", t0, p1, 1));
			expectedArcs.put("e-3DC6-6BDC4-14", new Arc("e-3DC6-6BDC4-14", p0, t0, 1));
			expectedArcs.put("e-3DC6-6BDC6-15", new Arc("e-3DC6-6BDC6-15", t3, p4, 1));
			expectedArcs.put("e-3DC6-6BDC7-16", new Arc("e-3DC6-6BDC7-16", t3, p2, 1));
			expectedArcs.put("e-3DC6-6BDD7-17", new Arc("e-3DC6-6BDD7-17", p3, t3, 1));
			expectedArcs.put("e-3DC6-6BDD8-18", new Arc("e-3DC6-6BDD8-18", t1, p3, 1));
			expectedArcs.put("e-3DC6-6BDD8-19", new Arc("e-3DC6-6BDD8-19", p2, t1, 1));
			expectedArcs.put("e-3DC6-6BDD9-20", new Arc("e-3DC6-6BDD9-20", t2, p4, 5));
			expectedArcs.put("e-3DC6-6BDDA-21", new Arc("e-3DC6-6BDDA-21", p4, t0, 5));
			expectedArcs.put("e-3DC6-6BDDC-22", new Arc("e-3DC6-6BDDC-22", t2, p0, 1));
			
			Arc[] obtainedArcs = petriComponents.getValue2();
			
			assertEquals(expectedArcsAmount, obtainedArcs.length);
			
			for(Arc arc : obtainedArcs){
				try{
					Arc expectedArc = expectedArcs.get(arc.getId());
					assertEquals(expectedArc.getSource().getId(), arc.getSource().getId());
					assertEquals(expectedArc.getTarget().getId(), arc.getTarget().getId());
					assertEquals(expectedArc.getWeight(), arc.getWeight());
				} catch (IndexOutOfBoundsException ex){
					fail("An unexpected arc was recieved");
				}
			}
		} catch (FileNotFoundException | SecurityException e) {
			fail("Could not open file " + READER_WRITER);
		} catch (BadPnmlFormatException e) {
			fail(e.getMessage());
		}
	}
	
	@Test
	public void PNMLreaderShouldThrowFileNotFoundExceptionWhenFileDoesNotExist() {
		try {
			@SuppressWarnings("unused")
			TinaPnmlParser reader = new TinaPnmlParser("nonExistingFile");
			fail("Exception should've be thrown before this point");
		} catch (Exception e) {
			assertEquals(e.getClass().getSimpleName(), "FileNotFoundException");
		}
	}
	
	@Test
	public void ParseFileAndGetPetriComponentsShouldThrowExceptionWhenFileIsNotPNML() {
		try {
			TinaPnmlParser reader = new TinaPnmlParser(READER_WRITER_NON_PNML);
			assertEquals(null, reader.parseFileAndGetPetriComponents());
		} catch (Exception e) {
			assertEquals(e.getClass().getSimpleName(), "SAXParseException");
		} 
	}
	
	@Test
	public void ParseFileAndGetTimeTransitionsCorrectly() {
		long max = Long.MAX_VALUE;
		long min = 1;
		try {
			TinaPnmlParser reader = new TinaPnmlParser(TIMED_PETRI_NET);
			
			Triplet<Place[], Transition[], Arc[]> petriComponents = reader.parseFileAndGetPetriComponents();
			Transition[] transitions = petriComponents.getValue1();
			
			double[] expectedTimes = {1+min, 4-min, 1, 5, 2, max-min};
			
			assertEquals(expectedTimes[0], transitions[0].getTimeSpan().getTimespanBeginning(), 0);
			assertEquals(expectedTimes[1], transitions[0].getTimeSpan().getTimespanEnding(), 0);
			assertEquals(expectedTimes[2], transitions[1].getTimeSpan().getTimespanBeginning(), 0);
			assertEquals(expectedTimes[3], transitions[1].getTimeSpan().getTimespanEnding(), 0);
			assertEquals(expectedTimes[4], transitions[2].getTimeSpan().getTimespanBeginning(), 0);
			assertEquals(expectedTimes[5], transitions[2].getTimeSpan().getTimespanEnding(), 0);
			
		} catch (FileNotFoundException | SecurityException e) {
			fail("Could not open file " + READER_WRITER);
		} catch (BadPnmlFormatException e) {
			fail(e.getMessage());
		}
	}

	/**
	 * <li> Given t0 has a label with guard "test" </li>
	 * <li> And t1 has a label with same guard as t0 but negated </li>
	 * <li> When the file is parsed </li>
	 * <li> And t0 and t1 are generated as transition objects </li>
	 * <li> Then t0 has a guard, name "test", enabled by true </li>
	 * <li> And t1 has a guard, name "test", enabled by false </li>
	 */
	@Test
	public void testParseFileAndGetTransitionsWithGuard() {
		try {
			TinaPnmlParser reader = new TinaPnmlParser(PETRI_WITH_GUARD_01);
			
			Triplet<Place[], Transition[], Arc[]> petriComponents = reader.parseFileAndGetPetriComponents();
			Transition[] transitions = petriComponents.getValue1();
			
			Transition t0 = transitions[0];
			Transition t1 = transitions[1];
			
			final String expectedGuardName = "test";
			
			assertEquals(expectedGuardName, t0.getGuardName());
			assertTrue(t0.getGuardEnablingValue());
			
			assertEquals(expectedGuardName, t1.getGuardName());
			assertFalse(t1.getGuardEnablingValue());
			
		} catch (FileNotFoundException | SecurityException e) {
			fail("Could not open file " + PETRI_WITH_GUARD_01);
		} catch (BadPnmlFormatException e) {
			fail(e.getMessage());
		}
	}
	
	/**
	 * <li> Given t0 has a label with guard written in bad format </li>
	 * <li> When the file is parsed </li>
	 * <li> Then BadPNMLFormatException should be thrown </li>
	 * @see org.unc.lac.javapetriconcurrencymonitor.exceptions.BadPnmlFormatException
	 */
	@Test
	public void testGuardWithBadFormatShouldThrowException() {
		try {
			TinaPnmlParser reader = new TinaPnmlParser(PETRI_WITH_GUARD_BAD_FORMAT_01);
			
			Triplet<Place[], Transition[], Arc[]> petriComponents = reader.parseFileAndGetPetriComponents();
			
			fail("An exception should've been thrown");
			
		} catch (FileNotFoundException | SecurityException e) {
			fail("Could not open file " + PETRI_WITH_GUARD_BAD_FORMAT_01);
		} catch (Exception e) {
			assertEquals(BadPnmlFormatException.class, e.getClass());
		}
	}
	
	/**
	 * <li> Given t0 feeds p2 and has no restriction places </li>
	 * <li> And t1 drains p2 </li>
	 * <li> And p0 has two tokens </li>
	 * <li> And t2 is fed by p0 and inhibited by p2 </li>
	 * <li> When the file is parsed </li>
	 * <li> And the arcs are build </li>
	 * <li> Then there must be one inhibiter arc from p2 to t2</li>
	 */
	@Test
	public void testPnmlParserShouldRecognizeInhibiterArcs() {
		try {
			TinaPnmlParser reader = new TinaPnmlParser(PETRI_WITH_INHIBITOR_01);
			
			Triplet<Place[], Transition[], Arc[]> petriComponents = reader.parseFileAndGetPetriComponents();
			
			Place p2 = petriComponents.getValue0()[2];
			Transition t2 = petriComponents.getValue1()[2];
			
			// get all matching arcs filtering the array as a stream by source id and target id. This should be just one
			Arc[] matchingArcs = Arrays.stream(petriComponents.getValue2())
					.filter((Arc a) -> a.getSource().getId().equals(p2.getId()) &&  a.getTarget().getId().equals(t2.getId()))
					.toArray((size) -> new Arc[size]);
			
			assertEquals(1, matchingArcs.length);
			
			Arc arc = matchingArcs[0];
			
			assertEquals(ArcType.INHIBITOR, arc.getType());
			
		} catch (FileNotFoundException | SecurityException e) {
			fail("Could not open file " + PETRI_WITH_INHIBITOR_01);
		} catch (Exception e) {
			fail("Exception thrown: " + e.getMessage());
		}
	}

	/**
	 * <li> Given the PNML file contains 4 places with custom names "input 01", "input 02", "product 01" and "product 02" </li>
	 * <li> And the file contains 4 transitions with custom names "build 02 from input 01", "build 01", "build 02 from input 02" and "input arrives"  </li>
	 * <li> When I parse the file </li>
	 * <li> And I get the petri components </li>
	 * <li> Then all transition names must be in the transitions array </li>
	 * <li> And no extra name has to be in the transition array </li>
	 * <li> Then all transition names must be in the places array </li>
	 * <li> And no extra name has to be in the places array </li>
	 */
	@Test
	public void testParsePetriWithCustomNamesShouldStoreCustomNamesInPlacesAndTransitions(){
		try{
			TinaPnmlParser reader = new TinaPnmlParser(PETRI_WITH_CUSTOM_NAMES);
			
			String[] expectedTransitionNames = {
				"build 02 from input 01",
				"build 01",
				"build 02 from input 02",
				"input arrives"
			};
			
			String[] expectedPlaceNames = {
				"input 01",
				"input 02",
				"product 01",
				"product 02"
			};
			
			Triplet<Place[], Transition[], Arc[]> petriComponents = reader.parseFileAndGetPetriComponents();
			
			Place[] places = petriComponents.getValue0();
			Transition[] transitions = petriComponents.getValue1();
			
			ArrayList<String> placesNames = new ArrayList<String>();
			Arrays.stream(places)
				.map((Place p) -> p.getName())
				.forEach(placesNames::add);
			
			ArrayList<String> transitionsNames = new ArrayList<String>();
			Arrays.stream(transitions)
				.map((Transition t) -> t.getName())
				.forEach(transitionsNames::add);
			
			assertEquals(4, transitions.length);
			assertTrue(placesNames.containsAll(Arrays.asList(expectedPlaceNames)));
			
			assertEquals(4, places.length);
			assertTrue(transitionsNames.containsAll(Arrays.asList(expectedTransitionNames)));
			
		} catch (Exception e) {
			fail("Exception thrown during test: " + e.getMessage());
		}
	}
	
	/**
	 * <li> Given file contains 4 places and 4 transitions </li>
	 * <li> When I parse the file </li>
	 * <li> And I get the petri components </li>
	 * <li> Then places indexes have to go from 0 to 3 </li>
	 * <li> And transitions indexes have to go from 0 to 3 </li>
	 */
	@Test
	public void testParserShouldGenerateConsecutiveIndexed(){
		try{
			TinaPnmlParser reader = new TinaPnmlParser(PETRI_WITH_CUSTOM_NAMES);
			
			Triplet<Place[], Transition[], Arc[]> petriComponents = reader.parseFileAndGetPetriComponents();
			
			Place[] places = petriComponents.getValue0();
			Transition[] transitions = petriComponents.getValue1();
			
			int expectedLength = 4;
			
			assertEquals(expectedLength, places.length);
			assertEquals(expectedLength, transitions.length);
			
			for( int patternIndex = 0; patternIndex < expectedLength; patternIndex++){
				assertEquals(patternIndex, places[patternIndex].getIndex());
				assertEquals(patternIndex, transitions[patternIndex].getIndex());
			}
			
		} catch (Exception e) {
			fail("Exception thrown during test: " + e.getMessage());
		}
	}
	
	/**
	 * <li> Given there are two places whose name is "p0" </li>
	 * <li> When I parse the file </li>
	 * <li> Then DuplicatedNameError should be thrown </li>
	 */
	@Test
	public void testParserShouldThrowErrorWhenPlaceNamesAreDuplicated(){
		try{
			TinaPnmlParser reader = new TinaPnmlParser(PETRI_WITH_DUPLICATED_NAMES_PLACE);
			
			reader.parseFileAndGetPetriComponents();
			
			fail("An error should've been thrown before this point");
		} catch (Error e){
			assertEquals(DuplicatedNameError.class, e.getClass());
		} catch (Exception e) {
			fail("Exception thrown during test: " + e.getMessage());
		}
	}
	
	/**
	 * <li> Given there are two transitions whose name is "t0" </li>
	 * <li> When I parse the file </li>
	 * <li> Then DuplicatedNameError should be thrown </li>
	 */
	@Test
	public void testParserShouldThrowErrorWhenTransitionNamesAreDuplicated(){
		try{
			TinaPnmlParser reader = new TinaPnmlParser(PETRI_WITH_DUPLICATED_NAMES_TRANSITION);
			
			reader.parseFileAndGetPetriComponents();
			
			fail("An error should've been thrown before this point");
		} catch (Error e){
			assertEquals(DuplicatedNameError.class, e.getClass());
		} catch (Exception e) {
			fail("Exception thrown during test: " + e.getMessage());
		}
	}
	
	/**
	 * <li> Given p0 and p1 have the same ID </li>
	 * <li> When I parse the file </li>
	 * <li> Then DuplicatedIdError should be thrown </li>
	 */
	@Test
	public void testParserShouldThrowErrorWhenPlaceIdsAreDuplicated(){
		try{
			TinaPnmlParser reader = new TinaPnmlParser(PETRI_WITH_DUPLICATED_IDS_PLACE);
			
			reader.parseFileAndGetPetriComponents();
			
			fail("An error should've been thrown before this point");
		} catch (Error e){
			assertEquals(DuplicatedIdError.class, e.getClass());
		} catch (Exception e) {
			fail("Exception thrown during test: " + e.getMessage());
		}
	}
	
	/**
	 * <li> Given t0 and t1 have the same ID </li>
	 * <li> When I parse the file </li>
	 * <li> Then DuplicatedIdError should be thrown </li>
	 */
	@Test
	public void testParserShouldThrowErrorWhenTransitionIdsAreDuplicated(){
		try{
			TinaPnmlParser reader = new TinaPnmlParser(PETRI_WITH_DUPLICATED_IDS_TRANSITION);
			
			reader.parseFileAndGetPetriComponents();
			
			fail("An error should've been thrown before this point");
		} catch (Error e){
			assertEquals(DuplicatedIdError.class, e.getClass());
		} catch (Exception e) {
			fail("Exception thrown during test: " + e.getMessage());
		}
	}
	
	/**
	 * <li> Given p2 feeds t2 with a reader arc </li>
	 * <li> When the file is parsed </li>
	 * <li> And the arcs are built </li>
	 * <li> Then there must be one reader arc from p2 to t2</li>
	 */
	@Test
	public void testPnmlParserShouldRecognizeReaderArcs() {
		try {
			TinaPnmlParser reader = new TinaPnmlParser(PETRI_WITH_READER_01);
			
			Triplet<Place[], Transition[], Arc[]> petriComponents = reader.parseFileAndGetPetriComponents();
			
			Place p2 = petriComponents.getValue0()[2];
			Transition t2 = petriComponents.getValue1()[2];
			
			// get all matching arcs filtering the array as a stream by source id and target id. This should be just one
			Arc[] matchingArcs = Arrays.stream(petriComponents.getValue2())
					.filter((Arc a) -> a.getSource().getId().equals(p2.getId()) &&  a.getTarget().getId().equals(t2.getId()))
					.toArray((size) -> new Arc[size]);
			
			assertEquals(1, matchingArcs.length);
			
			Arc arc = matchingArcs[0];
			
			assertEquals(ArcType.READ, arc.getType());
			
		} catch (FileNotFoundException | SecurityException e) {
			fail("Could not open file " + PETRI_WITH_INHIBITOR_01);
		} catch (Exception e) {
			fail("Exception thrown: " + e.getMessage());
		}
	}
}
