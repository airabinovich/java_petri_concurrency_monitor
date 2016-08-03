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
		int placesAmount = plazas.length;
		int transitionsAmount = transiciones.length;
		Integer[][] pre = new Integer[placesAmount][transitionsAmount];
		Integer[][] pos = new Integer[placesAmount][transitionsAmount];
		Integer[][] inc = new Integer[placesAmount][transitionsAmount];
		
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
