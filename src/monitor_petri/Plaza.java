package monitor_petri;

public class Plaza {
	
	private int indice;
	private String id;
	private int marcado;

	public Plaza(String _id, int _m, int _i){
		this.id = _id;
		this.marcado = _m;
		this.indice = _i;
	}
	
	public int getMarcado() {
		return marcado;
	}

	public void setMarcado(int marcado) {
		this.marcado = marcado;
	}
	
	public String getId() {
		return id;
	}
	public String setId(String id) {
		return this.id = id;
	}

	/**
	 * @return the indice
	 */
	public int getIndice() {
		return indice;
	}

}
