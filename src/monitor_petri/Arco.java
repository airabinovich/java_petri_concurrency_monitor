package monitor_petri;

public class Arco {
	
	private String id;
	private String id_origen;
	private String id_destino;
	
	public Arco(String i, String id_o, String id_d){
		id = i;
		id_origen = id_o;
		id_destino = id_d;
	}
}
