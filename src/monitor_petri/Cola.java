package monitor_petri;

import java.util.ArrayList;

public class Cola {
	
	private ArrayList<Thread> cola = new ArrayList<Thread>();
	
	public ArrayList<Thread> quienes_estan(){
		return cola;
	}
	
	public Cola(){
		
	}
	
	public void release(int t){
		// TODO: revisar si realmente estoy despertando a ese thread
		cola.get(t).notify();
	}
	
	public void wakeUp(Thread t){
		int index = cola.indexOf(t);
		release(index);
	}
	
	public void release_disparos(){
		
	}
	
	private void goToSleep(Thread t){
		cola.add(t);
		try {
			t.wait();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
