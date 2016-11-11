package org.lac.javapetriconcurrencymonitor.test.utils;

import org.unc.lac.javapetriconcurrencymonitor.queues.VarCondQueue;

public class DummyTask extends Thread{
		
	private boolean get_in_queue;
	private boolean get_in_high_priority_queue;
	private boolean terminate;
	VarCondQueue queue;
	
	public DummyTask(VarCondQueue q){
		queue = q;
		get_in_queue = false;
		get_in_high_priority_queue = false;
	}
	
	public synchronized void setGet_in_queue(boolean _get_in_queue){
		get_in_queue = _get_in_queue;
	}
	
	public synchronized void setGet_in_high_priority_queue(boolean _get_in_high_priority_queue){
		get_in_high_priority_queue = _get_in_high_priority_queue;
	}
	
	public synchronized void setTerminate(boolean _terminate){
		terminate = _terminate;
	}
	
	@Override
	public void run() {
		while(!terminate){
			try {
				if(get_in_high_priority_queue){
					queue.sleepWithHighPriority();
					setGet_in_high_priority_queue(false);
				}
				else if(get_in_queue){
					queue.sleep();
					setGet_in_queue(false);
				}
				Thread.sleep(10);
			} catch (InterruptedException e) {
			}
		}
		setGet_in_queue(false);
		setGet_in_high_priority_queue(false);
		setTerminate(false);
	}
}
