package monitor_petri;

import java.util.ArrayList;

public class Etiqueta {
	
	private boolean automatica;
	private boolean informada;
	
	public Etiqueta(boolean au, boolean inf){
		automatica = au;
		informada = inf;
	}

	public boolean isAutomatica() {
		return automatica;
	}

	public void setAutomatica(boolean automatica) {
		this.automatica = automatica;
	}

	public boolean isInformada() {
		return informada;
	}

	public void setInformada(boolean informada) {
		this.informada = informada;
	}

}
