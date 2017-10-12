package org.lac.javapetriconcurrencymonitor.test.cases;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.unc.lac.javapetriconcurrencymonitor.petrinets.components.Arc;
import org.unc.lac.javapetriconcurrencymonitor.petrinets.components.Arc.ArcType;
import org.unc.lac.javapetriconcurrencymonitor.petrinets.components.Label;
import org.unc.lac.javapetriconcurrencymonitor.petrinets.components.Place;
import org.unc.lac.javapetriconcurrencymonitor.petrinets.components.Transition;

public class ArcTestSuite {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	/**
	 * When I create an arc with Type {@link ArcType#INHIBITOR} and weight 2
	 * Then {@link IllegalArgumentException} is thrown
	 */
	@Test(expected = IllegalArgumentException.class)
	public void inhibitorArcShouldNotBeAbleToHaveWeightGreaterThanOne() {
		Transition t = new Transition("1", new Label(false, false), 1, "tr");
		Place p = new Place("1", 0, 1, "pl");
		new Arc("1", p, t, 2, ArcType.INHIBITOR);
	}
	
	/**
	 * When I create an arc with weight -1
	 * Then {@link IllegalArgumentException} is thrown
	 */
	@Test(expected = IllegalArgumentException.class)
	public void arcShouldNotBeAbleToHaveNegativeValue() {
		Transition t = new Transition("1", new Label(false, false), 1, "tr");
		Place p = new Place("1", 0, 1, "pl");
		new Arc("1", p, t, -1);
	}
	
	/**
	 * When I create an arc with weight 0
	 * Then {@link IllegalArgumentException} is thrown
	 */
	@Test(expected = IllegalArgumentException.class)
	public void arcShouldNotHaveZeroWeight() {
		Transition t = new Transition("1", new Label(false, false), 1, "tr");
		Place p = new Place("1", 0, 1, "pl");
		new Arc("1", p, t, 0);
	}

}
