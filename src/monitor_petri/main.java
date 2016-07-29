package monitor_petri;

import java.io.FileNotFoundException;

public class main {

	public static void main(String[] args) {

		String pnmlFile = "./src/inhibitorBasic.pnml";
		PNMLreader pnmlReader;
		try {
			pnmlReader = new PNMLreader(pnmlFile);
			pnmlReader.read_file();
			//RdP red = new RdP();
			//Politica politica = new Politica();
			//GestorMonitor monitor = new GestorMonitor(red, politica);
			System.out.println("FIN");
		} catch (FileNotFoundException | SecurityException e) {
			System.out.println("Excecution error: " + e.getMessage());
			e.printStackTrace();
		}
		
		
	}
}
