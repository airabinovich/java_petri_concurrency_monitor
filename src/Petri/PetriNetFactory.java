package Petri;

import java.io.FileNotFoundException;
import java.util.ArrayList;

import org.javatuples.Quartet;
import org.javatuples.Quintet;
import org.javatuples.Triplet;

import Petri.Arc.ArcType;

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
		 * @throws CannotCreatePetriNetError If the parsed info has inconsistent data.
		 */
		public PetriNet makePetriNet(petriNetType type) throws CannotCreatePetriNetError{
			
			Quartet<Place[], Transition[], Arc[], Integer[]> petriObjects = PNML2PNObjects();
			Quintet<Integer[][], Integer[][], Integer[][], Integer[][], Integer[][]> petriMatrices = 
					rdpObjects2Matrices(petriObjects.getValue0(), petriObjects.getValue1(), petriObjects.getValue2());
			
			switch(type){
			case PT:
				return new PTPetriNet(petriObjects.getValue0(), petriObjects.getValue1(), petriObjects.getValue2(), petriObjects.getValue3(),
						petriMatrices.getValue0(), petriMatrices.getValue1(), petriMatrices.getValue2(), petriMatrices.getValue3(), petriMatrices.getValue4());
			case TIMED:
				return new TimedPetriNet(petriObjects.getValue0(), petriObjects.getValue1(), petriObjects.getValue2(), petriObjects.getValue3(),
						petriMatrices.getValue0(), petriMatrices.getValue1(), petriMatrices.getValue2(), petriMatrices.getValue3(), petriMatrices.getValue4());
			default:
				throw new CannotCreatePetriNetError("Cannot create petri net from unknown type " + type);
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
		 * @return a 5-tuple containing (Pre matrix, Post matrix, Incidence matrix, Inhibition matrix, Reset matrix)
		 * @throws CannotCreatePetriNetError If a non-standard arc goes from transition to place 
		 * @see Petri.Arc.ArcType
		 */
		protected Quintet<Integer[][], Integer[][], Integer[][], Integer[][], Integer[][]> rdpObjects2Matrices(
				Place[] places, Transition[] transitions, Arc[] arcs) throws CannotCreatePetriNetError{
			final int placesAmount = places.length;
			final int transitionsAmount = transitions.length;
			Integer[][] pre = new Integer[placesAmount][transitionsAmount];
			Integer[][] pos = new Integer[placesAmount][transitionsAmount];
			Integer[][] inc = new Integer[placesAmount][transitionsAmount];
			Integer[][] inhibition = new Integer[placesAmount][transitionsAmount];
			Integer[][] resetMatrix = new Integer[placesAmount][transitionsAmount];
			
			for(int i = 0; i < placesAmount; i++){
				for(int j = 0; j < transitionsAmount; j++){
					// Since Integer default value is null
					// I need to fill the matrixes with zeros
					pre[i][j] = 0;
					pos[i][j] = 0;
					inc[i][j] = 0;
					inhibition[i][j] = 0;
					resetMatrix[i][j] = 0;
				}
			}
			
			for( Arc arc : arcs){
				String arcSource = arc.getId_source();
				String arcTarget = arc.getId_target();
				ArcType type = arc.getType();
				boolean arcDone = false;
				for(int i = 0; i < placesAmount ; i++){
					if(arcDone){ break;}
					if(arcSource.equals(places[i].getId())){
						for(int j = 0; j < transitionsAmount; j++){
							if(arcTarget.equals(transitions[j].getId())){
								// We don't use the place nor transition index here because there might be some index missing or repeated
								// and that could cause and error
								// e.g: t2 doesn't exist and t5 is the last but it will be on position 4 instead of 5
								if(type == ArcType.NORMAL){
									pre[i][j] = arc.getWeight();
								} else if (type == ArcType.INHIBITOR){
									// for inhibitor arcs weight is ignored
									inhibition[i][j] = 1;
								} else if (type == ArcType.RESET){
									// for reset matrix arcs weight is ignored
									resetMatrix[i][j] = 1;
								}
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
						if(type != ArcType.NORMAL){
							throw new CannotCreatePetriNetError(type + " arc cannot go from transition to place");
						}
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
			
			for(int i = 0; i < placesAmount; i++){
				for(int j = 0; j < transitionsAmount; j++){
					boolean resetArcEnters = resetMatrix[i][j] > 0;
					if (resetArcEnters){
						for(int k = 0; k < placesAmount; k++){
							boolean anotherResetArcEntersTransition = k != j && resetMatrix[k][j] > 0;
							boolean inhibitionArcEntersTransition = inhibition[k][j] > 0;
							boolean normalArcEntersTransition = pre[k][j] > 0;
							if(normalArcEntersTransition || inhibitionArcEntersTransition || anotherResetArcEntersTransition){
								throw new CannotCreatePetriNetError(
										"Cannot have another input arcs in transition " + j + " because there is a reset arc.");
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
			
			return new Quintet<Integer[][], Integer[][], Integer[][], Integer[][], Integer[][]>(pre, pos, inc, inhibition, resetMatrix);
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