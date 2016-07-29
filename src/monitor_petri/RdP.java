package monitor_petri;

public class RdP {
	
	private Plaza[] plazas;
	private Transicion[] transiciones;
	private Arco[] arcos;
	private Transicion[] sensibilizada;
	private Integer[] marca;
	private Integer[] marca_inicial;
	
	public RdP(){
		
	}
	
	public boolean disparo(Transicion t) {
		int i;
		for( i = 0; i< transiciones.length; i++){
			if(transiciones[i].getId() == t.getId()){
				break;
			}
		}
		return disparo(i);
	}
	
	public boolean disparo(int transicion){
		
		return true;
	}
	
	public Transicion[] sensibilizadas(){
		return this.sensibilizada;
	}
	
	//No sabemos que hace todavia
	public void disparar_guarda(int ti, boolean to) {
	}
	
	//No sabemos que hace todavia (tampoco)
	public void set_guarda(boolean p, int i){
		
	}
}
