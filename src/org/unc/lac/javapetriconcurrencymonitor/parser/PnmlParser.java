package org.unc.lac.javapetriconcurrencymonitor.parser;

import java.io.File;
import java.io.FileNotFoundException;

import org.javatuples.Triplet;
import org.unc.lac.javapetriconcurrencymonitor.errors.DuplicatedIdError;
import org.unc.lac.javapetriconcurrencymonitor.errors.DuplicatedNameError;
import org.unc.lac.javapetriconcurrencymonitor.exceptions.BadPnmlFormatException;
import org.unc.lac.javapetriconcurrencymonitor.petrinets.components.Arc;
import org.unc.lac.javapetriconcurrencymonitor.petrinets.components.Place;
import org.unc.lac.javapetriconcurrencymonitor.petrinets.components.Transition;

/**
 * Any class that implements this interface should be able t
 */
public abstract class PnmlParser {
	
	protected File pnmlFile;
	
	public PnmlParser(String pnmlPath) throws FileNotFoundException, SecurityException, NullPointerException {
		pnmlFile = new File(pnmlPath);
		
		if(!pnmlFile.exists()){
			throw new FileNotFoundException("File " + pnmlPath + " not found");
		}
		else if(!pnmlFile.canRead()){
			throw new SecurityException("Security Error while trying to read from file " + pnmlPath);
		}
	}
	
	/**
	 * parses PNML file and returns all petri components embedded
	 * @return a Triplet containing places, transitions and arcs inside the PNML
	 * @throws BadPnmlFormatException
	 * @throws DuplicatedNameError If two or more places or transitions share names (empty names are not a problem)
	 * @throws DuplicatedIdError If two or more places or transitions share ids
	 */
	public abstract Triplet<Place[], Transition[], Arc[]> parseFileAndGetPetriComponents()
			throws BadPnmlFormatException, DuplicatedNameError, DuplicatedIdError;

}
