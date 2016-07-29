package monitor_petri;

public class RdP {
	
	private Plaza[] plazas;
	private Transicion[] transiciones;
	private Arco[] arcos;
	private Transicion[] sensibilizada;
	private Integer[][] preI;
	private Integer[][] posI;
	private Integer[][] I;
	private Integer[] marca;
	private Integer[] marca_inicial;
	
	public RdP(Plaza[] _places, Transicion[] _transitions, Arco[] _arcs,
			Integer[] _initialMarking, Integer[][] _preI, Integer[][] _posI, Integer[][] _I){
		this.plazas = _places;
		this.transiciones = _transitions;
		this.arcos = _arcs;
		this.marca_inicial = _initialMarking.clone();
		this.marca = _initialMarking;
		this.preI = _preI;
		this.posI = _posI;
		this.I = _I;
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
