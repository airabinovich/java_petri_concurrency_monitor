package monitor_petri;

import java.io.FileNotFoundException;
import java.util.ArrayList;

import org.javatuples.Quartet;
import org.javatuples.Triplet;
import org.javatuples.Septet;

public class RdPBuilder {
	
	private PNMLreader reader;
	
	public RdPBuilder(String pathToPNML){
		try {
			reader = new PNMLreader(pathToPNML);
		} catch (FileNotFoundException | SecurityException e) {
			e.printStackTrace();
		}
	}
	
	public Septet<Plaza[], Transicion[], Arco[], Integer[], Integer[][], Integer[][], Integer[][]> buildPetriNetObjects(){
		
		Quartet<Plaza[], Transicion[], Arco[], Integer[]> petriObjects = PNML2PNObjects();
		return petriObjects.add(rdpObjetcts2Matrixes(petriObjects.getValue0(), petriObjects.getValue1(), petriObjects.getValue2()));
	}
	
	/**
	 * extracts petri net info from PNML file given as argument and returns a 4-tuple containing
	 * places, transitions, arcs and initial marking
	 * @param pnmlFile
	 * @return
	 */
	private Quartet<Plaza[], Transicion[], Arco[], Integer[]> PNML2PNObjects(){
		//Aca se obtiene el Triplet desde el objeto que lee el xml, entonces devuelve las plazas, transiciones y arcos
		Triplet<Plaza[], Transicion[], Arco[]> ret = reader.parseFileAndGetPetriObjects();
		
		//A eso hay que sumarte el marcado inicial, generando un Quartet
		return ret.add(getMarkingFromPlaces(ret.getValue0()));
	}
	
	/**
	 * makes and returns petri net matrixes from its objects
	 * @param plazas petri net's places
	 * @param transiciones petri net's transitions
	 * @param arcos petri net's arcs
	 * @return a 3-tuple containing <PreI matrix, PosI matrix, I matrix>
	 */
	private Triplet<Integer[][], Integer[][], Integer[][]> rdpObjetcts2Matrixes(Plaza[] plazas, Transicion[] transiciones, Arco[] arcos){
		final int placesAmount = plazas.length;
		final int transitionsAmount = transiciones.length;
		Integer[][] pre = new Integer[placesAmount][transitionsAmount];
		Integer[][] pos = new Integer[placesAmount][transitionsAmount];
		Integer[][] inc = new Integer[placesAmount][transitionsAmount];
		
		for(int i = 0; i < placesAmount; i++){
			for(int j = 0; j < transitionsAmount; j++){
				// since not all fields will be filled in the following loops
				// and since Integer default value is null
				// I need to fill the matrixes with zeros
				pre[i][j] = 0;
				pos[i][j] = 0;
				inc[i][j] = 0;
			}
		}
		
		for( Arco arc : arcos){
			String arcSource = arc.getId_source();
			String arcTarget = arc.getId_target();
			boolean arcDone = false;
			for(int i = 0; i < placesAmount ; i++){
				if(arcDone){ break;}
				String placeId = plazas[i].getId();
				if(arcSource.equals(placeId)){
					for(int j = 0; j < transitionsAmount; j++){
						String transitionId = transiciones[j].getId();
						if(arcTarget.equals(transitionId)){
							// I don't use the place not transition index
							// because there might not be all of them
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
				String transitionId = transiciones[j].getId();
				if(arcSource.equals(transitionId)){
					for(int i = 0; i < placesAmount; i++){
						String placeId = plazas[i].getId();
						if(arcTarget.equals(placeId)){
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
	 * gets every marking from places array and returns an array containing them
	 * @param places the places to check
	 * @return place's marking
	 */
	private Integer[] getMarkingFromPlaces(Plaza[] places){
		ArrayList<Integer> initialMarking = new ArrayList<Integer>(places.length);
		for(Plaza place : places){
			initialMarking.add(place.getMarcado());
		}
		Integer[] ret = new Integer[initialMarking.size()];
		return initialMarking.toArray(ret);
	}
	
}
