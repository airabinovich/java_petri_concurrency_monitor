package Petri;

import java.io.FileNotFoundException;
import java.util.ArrayList;

import org.javatuples.Quartet;
import org.javatuples.Triplet;

	/**
	 * PetriNet factory. Gets info from PNML file. Calls makePetriNet to get a PetriNet object
	 *
	 */
	public class PetriNetFactory {
		
		protected PNMLreader reader;
		
		/**
		 * Types accepted by {@link PetriNetFactory#makePetriNet(petriNetType)}
		 */
		public static enum petriNetType {PT, TIMED};
		
		public PetriNetFactory(String pathToPNML) throws NullPointerException{
			try {
				this.reader = new PNMLreader(pathToPNML);
			} catch (FileNotFoundException | SecurityException e) {
				e.printStackTrace();
			}
		}
		
		public PetriNetFactory(PNMLreader _reader) throws NullPointerException{
			if(_reader == null){
				throw new NullPointerException("Invalid reader argument");
			}
			this.reader = _reader;
		}
		
		/**
		 * makes and returns the petri described in the PNML file passed to the factory
		 * @return PetriNet object containing info described in PNML file
		 * @param petriNetType petri net type from enum type {@link petriNetType}
		 * @throws CannotCreatePetriNetError
		 */
		public PetriNet makePetriNet(petriNetType type) throws CannotCreatePetriNetError{
			
			Quartet<Place[], Transition[], Arc[], Integer[]> petriObjects = PNML2PNObjects();
			Triplet<Integer[][], Integer[][], Integer[][]> petriMatrices = 
					rdpObjects2Matrices(petriObjects.getValue0(), petriObjects.getValue1(), petriObjects.getValue2());
			
			switch(type){
			case PT:
				return new PTPetriNet(petriObjects.getValue0(), petriObjects.getValue1(), petriObjects.getValue2(), petriObjects.getValue3(),
						petriMatrices.getValue0(), petriMatrices.getValue1(), petriMatrices.getValue2());
			case TIMED:
				return new TimedPetriNet(petriObjects.getValue0(), petriObjects.getValue1(), petriObjects.getValue2(), petriObjects.getValue3(),
						petriMatrices.getValue0(), petriMatrices.getValue1(), petriMatrices.getValue2());
			default:
				return null;
			}
		}
		/**
		 * extracts petri net info from PNML file given as argument and returns a 4-tuple containing
		 * places, transitions, arcs and initial marking
		 * @return a 4-tuple containig (places, transitions, arcs, initial marking)
		 * @throws CannotCreatePetriNetError
		 */
		protected Quartet<Place[], Transition[], Arc[], Integer[]> PNML2PNObjects() throws CannotCreatePetriNetError{
			try{
				Triplet<Place[], Transition[], Arc[]> ret = reader.parseFileAndGetPetriObjects();
			
				return ret.add(getMarkingFromPlaces(ret.getValue0()));
			} catch (BadPNMLFormatException e){
				throw new CannotCreatePetriNetError("Error creating petriNet due to PNML error. "
						+ e.getMessage());
			}
		}
		
		/**
		 * makes and returns petri net matrices from its objects
		 * @param places petri net's places
		 * @param transitions petri net's transitions
		 * @param arcs petri net's arcs
		 * @return a 3-tuple containing (Pre matrix, Post matrix, Incidence matrix)
		 */
		protected Triplet<Integer[][], Integer[][], Integer[][]> rdpObjects2Matrices(Place[] places, Transition[] transitions, Arc[] arcs){
			final int placesAmount = places.length;
			final int transitionsAmount = transitions.length;
			Integer[][] pre = new Integer[placesAmount][transitionsAmount];
			Integer[][] pos = new Integer[placesAmount][transitionsAmount];
			Integer[][] inc = new Integer[placesAmount][transitionsAmount];
			
			for(int i = 0; i < placesAmount; i++){
				for(int j = 0; j < transitionsAmount; j++){
					// Since Integer default value is null
					// I need to fill the matrixes with zeros
					pre[i][j] = 0;
					pos[i][j] = 0;
					inc[i][j] = 0;
				}
			}
			
			for( Arc arc : arcs){
				String arcSource = arc.getId_source();
				String arcTarget = arc.getId_target();
				boolean arcDone = false;
				for(int i = 0; i < placesAmount ; i++){
					if(arcDone){ break;}
					if(arcSource.equals(places[i].getId())){
						for(int j = 0; j < transitionsAmount; j++){
							if(arcTarget.equals(transitions[j].getId())){
								// We don't use the place nor transition index here because there might be some index missing or repeated
								// and that could cause and error
								// e.g: t2 doesn't exist and t5 is the last but it will be on position 4 instead of 5
								pre[i][j] = arc.getWeight();
								arcDone = true;
								break;
							}
						}
					}
				}
				if(arcDone){
					// if I already filled a pre matrix field, I won't fill any pos matrix field with this arc's info
					continue;
				}
				for(int j = 0; j < transitionsAmount; j++){
					if(arcDone){ break;	}
					if(arcSource.equals(transitions[j].getId())){
						for(int i = 0; i < placesAmount; i++){
							if(arcTarget.equals(places[i].getId())){
								pos[i][j] = arc.getWeight();
								arcDone = true;
								break;
							}
						}
					}
				}
			}
			
			// now we have both matrixes pre and pos, let's get inc = pos - pre
			for(int i = 0; i < placesAmount; i++){
				for(int j = 0; j < transitionsAmount; j++){
					inc[i][j] = pos[i][j] - pre[i][j];
				}
			}
			
			return new Triplet<Integer[][], Integer[][], Integer[][]>(pre, pos, inc);
		}
		
		/**
		 * gets initial marking from places array and returns an array containing the markings
		 * @param places the places to check
		 * @return place's initial marking
		 */
		protected Integer[] getMarkingFromPlaces(Place[] places){
			ArrayList<Integer> initialMarking = new ArrayList<Integer>(places.length);
			for(Place place : places){
				initialMarking.add(place.getMarking());
			}
			Integer[] ret = new Integer[initialMarking.size()];
			return initialMarking.toArray(ret);
		}
		
	}