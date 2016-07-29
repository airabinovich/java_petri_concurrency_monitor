package monitor_petri;

import java.io.File;
import org.javatuples.Quartet;
import org.javatuples.Triplet;
import org.javatuples.Septet;

public class RdPBuilder {
	
	private Plaza[] plazas;
	private Transicion[] transiciones;
	private Arco[] arcos;
	
	public RdPBuilder(){}
	
	public Septet<Plaza[], Transicion[], Arco[], Integer[], Integer[][], Integer[][], Integer[][]> buildNet(File xml){
		
		return PNML2rdpObjects(xml).add(rdpObjetcts2Matrixes(plazas, transiciones, arcos));
	}
	
	/**
	 * extracts petri net info from PNML file given as argument and returns a 4-tuple containing
	 * places, transitions, arcs and initial marking
	 * @param pnmlFile
	 * @return
	 */
	private Quartet<Plaza[], Transicion[], Arco[], Integer[]> PNML2rdpObjects(File pnmlFile){
		//Aca se obtiene el Triplet desde el objeto que lee el xml, entonces devuelve las plazas, transiciones y arcos
		//A eso hay que sumarte el marcado inicial, generando un Quartet
		return null;
	}
	
	private Triplet<Integer[][], Integer[][], Integer[][]> rdpObjetcts2Matrixes(Plaza[] plazas, Transicion[] transiciones, Arco[] arcos){
		Integer[][] pre = null, pos = null, inc = null;		
		return new Triplet<Integer[][], Integer[][], Integer[][]>(pre, pos, inc);
	}
}
