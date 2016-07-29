package monitor_petri;

public class Transicion{
		
	private String id;
	private Etiqueta etiqueta;
	
	public Transicion(String i, Etiqueta e){
		etiqueta = e;
		id = i;
	}
	public Etiqueta getEtiqueta() {
		return etiqueta;
	}
	public void setEtiqueta(Etiqueta etiqueta) {
		this.etiqueta = etiqueta;
	}
	public String getId() {
		return id;
	}
	public String setId(String id) {
		return this.id = id;
	}
	
}
