package monitor_petri;

public class Transicion{
	
	private int indice;
	private String id;
	private Etiqueta etiqueta;
	
	public Transicion(String _id, Etiqueta _e, int _i){
		this.etiqueta = _e;
		this.id = _id;
		this.indice = _i;
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
	
	/**
	 * @return the index
	 */
	public int getIndice() {
		return indice;
	}
}
