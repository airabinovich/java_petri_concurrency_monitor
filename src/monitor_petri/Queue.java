package monitor_petri;

import java.util.ArrayList;

public class Queue {
	
	private ArrayList<Thread> queue = new ArrayList<Thread>();
	
	public ArrayList<Thread> whoAreIn(){
		return queue;
	}
	
	public Queue(){
		
	}
	
	public void release(int t){
		// TODO: revisar si realmente estoy despertando a ese thread
		queue.get(t).notify();
	}
	
	public void wakeUp(Thread t){
		int index = queue.indexOf(t);
		release(index);
	}
	
	public void release_disparos(){
		
	}
	
	private void goToSleep(Thread t){
		queue.add(t);
		try {
			t.wait();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
