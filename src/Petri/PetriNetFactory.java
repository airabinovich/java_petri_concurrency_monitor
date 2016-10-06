package Petri;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;

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
		 * @throws CannotCreatePetriNetError If any a non supported arc type is given,
		 * or if a transition that has a reset arc as input has another arc as input
		 */
		public PetriNet makePetriNet(petriNetType type) throws CannotCreatePetriNetError{
			
			Quartet<Place[], Transition[], Arc[], Integer[]> petriObjects = PNML2PNObjects();
			Quintet<Integer[][], Integer[][], Integer[][], Integer[][], Integer[][]> petriMatrices = 
					petriNetObjectsToMatrices(petriObjects.getValue0(), petriObjects.getValue1(), petriObjects.getValue2());
			
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
		 * @throws CannotCreatePetriNetError If an error occurs during parsing
		 */
		protected Quartet<Place[], Transition[], Arc[], Integer[]> PNML2PNObjects() throws CannotCreatePetriNetError{
			try{
				Triplet<Place[], Transition[], Arc[]> ret = reader.parseFileAndGetPetriObjects();
			
				return ret.add(getMarkingFromPlaces(ret.getValue0()));
			} catch (BadPNMLFormatException e){
				throw new CannotCreatePetriNetError("Error creating petriNet due to PNML error " + e.getClass().getSimpleName()
						+ ". Message: " + e.getMessage());
			}
		}
		
		/**
		 * makes and returns petri net matrices from its objects
		 * @param places petri net's places
		 * @param transitions petri net's transitions
		 * @param arcs petri net's arcs
		 * @return a 5-tuple containing (Pre matrix, Post matrix, Incidence matrix, Inhibition matrix, Reset matrix)
		 * @throws CannotCreatePetriNetError If any a non supported arc type is given,
		 * or if a transition that has a reset arc as input has another arc as input
		 * @see Petri.Arc.ArcType
		 */
		protected Quintet<Integer[][], Integer[][], Integer[][], Integer[][], Integer[][]> petriNetObjectsToMatrices(
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
			
			for(Arc arc : arcs){
				PetriNode source = arc.getSource();
				PetriNode target = arc.getTarget();
				ArcType type = arc.getType();
				switch(type){
				case NORMAL:
					if(source.getClass().getSimpleName().equals("Place")){
						// arc goes from place to transition, let's fill the pre-incidence matrix
						pre[source.getIndex()][target.getIndex()] = arc.getWeight();
					}
					else {
						// arc goes from transition to place, let's fill the post-incidence matrix
						pos[target.getIndex()][source.getIndex()] = arc.getWeight();
					}
					break;
				case INHIBITOR:
					// source has to be a place and target a transition
					inhibition[source.getIndex()][target.getIndex()] = 1;
					break;
				case RESET:
					resetMatrix[source.getIndex()][target.getIndex()] = 1;
					break;
				case READ:
				default:
					throw new CannotCreatePetriNetError("Arc " + type + " not supported");
				}
			}
			
			
			// Now let's check if any transition that has a reset arc as input also has any other input arc
			// That is an illegal condition
			
			Arc[] resetArcs = Arrays.stream(arcs)
					.filter((Arc a) -> a.getType() == ArcType.RESET)
					.toArray((int size) -> new Arc[size]);
			for(Arc resetArc : resetArcs){
				int placeIndex = resetArc.getSource().getIndex();
				int transitionIndex = resetArc.getTarget().getIndex();
				for(int i = 0; i < placesAmount; i++){
					boolean anotherResetArcEntersTransition = i != placeIndex && resetMatrix[i][transitionIndex] > 0;
					boolean inhibitionArcEntersTransition = inhibition[i][transitionIndex] > 0;
					boolean normalArcEntersTransition = pre[i][transitionIndex] > 0;
					if(normalArcEntersTransition || inhibitionArcEntersTransition || anotherResetArcEntersTransition){
						throw new CannotCreatePetriNetError(
								"Cannot have another input arcs in transition " + resetArc.getTarget().getName() + ", id: " + transitionIndex + ", because there is a reset arc.");
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