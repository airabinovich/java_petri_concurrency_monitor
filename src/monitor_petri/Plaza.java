package monitor_petri;

public class Plaza {
	
	private String id;
	private int marcado;

	public Plaza(String i, int m){
		id = i;
		marcado = m;
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

}
