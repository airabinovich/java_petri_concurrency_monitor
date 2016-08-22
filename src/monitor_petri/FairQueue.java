package monitor_petri;

import java.util.concurrent.Semaphore;

import Petri.Transition;

public class FairQueue extends Thread implements VarCondQueue{
	
	private Semaphore sem;
	
	public FairQueue(Transition t){
		if(t.getLabel().isAutomatic()){
			sem = null;
		}
		else{
			sem = new Semaphore(0,true);
		}
	}
	
	public void sleep() {
		// TODO Auto-generated method stub
		try {
			sem.acquire();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void wakeUp() {
		// TODO Auto-generated method stub
		sem.release();
	}

	public boolean isEmpty() {
		// TODO Auto-generated method stub
		return sem.hasQueuedThreads();
	}
}
