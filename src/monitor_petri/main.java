package monitor_petri;

import org.javatuples.Septet;;

public class main {

	public static void main(String[] args) {

		String pnmlFile = "./src/inhibitorBasic.pnml";
		RdPBuilder petriNetBuilder;
		RdP petriNet;
		try {
			petriNetBuilder = new RdPBuilder(pnmlFile);
			Septet<Plaza[], Transicion[], Arco[], Integer[], Integer[][], Integer[][], Integer[][]> petriNetObjects = 
					petriNetBuilder.buildPetriNetObjects();
			petriNet = new RdP(
					petriNetObjects.getValue0(),	//Places
					petriNetObjects.getValue1(),	//Transitions
					petriNetObjects.getValue2(),	//Arcs
					petriNetObjects.getValue3(),	//Initial Marking
					petriNetObjects.getValue4(),	//PreI Matrix
					petriNetObjects.getValue5(),	//PosI Matrix
					petriNetObjects.getValue6());	//I Matrix
			
			//Politica politica = new Politica();
			//GestorMonitor monitor = new GestorMonitor(red, politica);
			System.out.println("FIN");
		} catch (SecurityException e) {
			System.out.println("Excecution error: " + e.getMessage());
			e.printStackTrace();
		}
		
		
	}
}
