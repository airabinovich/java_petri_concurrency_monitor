package Petri;

import org.javatuples.Septet;

import Petri.PetriNet.PetriNetBuilder;
import unit_tests.PetriNetBuilderTestSuite;

public class main {

	public static void main(String[] args) {
		String pnmlFile = "resources/petriNets/test.pnml";
		PetriNetBuilder petriNetBuilder = new PetriNetBuilder(pnmlFile);
		PetriNet petriNet;
		try {
			petriNetBuilder = new PetriNetBuilder(pnmlFile);
			
			//Septet<Place[], Transition[], Arc[], Integer[], Integer[][], Integer[][], Integer[][]> petriNetObjects = 
			//		petriNetBuilder.buildPetriNet();
			//petriNet = new PetriNet(
			//		petriNetObjects.getValue0(),	//Places
			//		petriNetObjects.getValue1(),	//Transitions
			//		petriNetObjects.getValue2(),	//Arcs
			//		petriNetObjects.getValue3(),	//Initial Marking
			//		petriNetObjects.getValue4(),	//PreI Matrix
			//		petriNetObjects.getValue5(),	//PosI Matrix
			//		petriNetObjects.getValue6());	//I Matrix
			
					petriNet = petriNetBuilder.buildPetriNet();
		
			//Politica politica = new Politica();
			//GestorMonitor monitor = new GestorMonitor(red, politica);
			System.out.println("FIN");
		} catch (SecurityException e) {
			System.out.println("Excecution error: " + e.getMessage());
			e.printStackTrace();
		} catch (Exception e){
			System.out.println("Excecution error: " + e.getMessage());
			e.printStackTrace();
		}
		
		
	}
}