package unit_tests;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import Petri.Place;

public class PlaceTestSuite {
	
	private static final int INITIAL_MARKING = 5;
	private static final int INDEX = 0;
	
	Place place;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
		place = new Place("id", INITIAL_MARKING, INDEX, "name");
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testSetMarkingShouldThrowExceptionWhenMarkingIsNegative() {
		try{
			int newMarking = -5;
			place.setMarking(newMarking);
			Assert.fail("An exception should've been thrown before this point");
		}catch (IllegalArgumentException e){
			Assert.assertEquals("IllegalArgumentException", e.getClass().getSimpleName());
		}
	}
	
	@Test
	public void testSetMarkingShouldUpdateMarkingUsingPositiveValue() {
		try{
			int newMarking = 3;
			place.setMarking(newMarking);
			Assert.assertEquals(newMarking, place.getMarking());
		} catch (Exception e){
			Assert.fail("Exception got: " + e.getMessage());
		}
	}

	@Test
	public void testSetIndexShouldFailWhenIndexIsNegative() {
		try{
			int newIndex = -5;
			place.setMarking(newIndex);
			Assert.fail("An exception should've been thrown before this point");
		}catch (IllegalArgumentException e){
			Assert.assertEquals("IllegalArgumentException", e.getClass().getSimpleName());
		}
	}
	
	@Test
	public void testSetIndexShouldUpdateIndexUsingPositiveValue() {
		try{
			int newIndex = 3;
			place.setIndex(newIndex);
			Assert.assertEquals(newIndex, place.getIndex());
		} catch (Exception e){
			Assert.fail("Exception got: " + e.getMessage());
		}
	}

}
