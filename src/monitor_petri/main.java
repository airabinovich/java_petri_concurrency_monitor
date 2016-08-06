package monitor_petri;

import Petri.PetriNet;
import Petri.PetriNet.PetriNetBuilder;

public class main {

	public static void main(String[] args) {
		String pnmlFile = "resources/petriNets/readerWriter.pnml";
		PetriNet petriNet;
		try {
			petriNet = new PetriNetBuilder(pnmlFile).buildPetriNet();
			
			Integer[][] pre = petriNet.getPre();
			Integer[][] post = petriNet.getPost();
			Integer[][] inc = petriNet.getInc();
			
			System.out.println("Pre:");
			for(Integer[] row : pre ){
				for(Integer elem : row){
					System.out.print(elem.toString() + "\t");
				}
				System.out.println();
			}
			System.out.println();
			
			System.out.println("Post:");
			for(Integer[] row : post ){
				for(Integer elem : row){
					System.out.print(elem.toString() + "\t");
				}
				System.out.println();
			}
			System.out.println();
			
			System.out.println("Inc:");
			for(Integer[] row : inc ){
				for(Integer elem : row){
					System.out.print(elem.toString() + "\t");
				}
				System.out.println();
			}
			System.out.println();
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
