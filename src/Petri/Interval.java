package Petri;

public class Interval {
	
	private int timeBegin;
	//if timeEnd has -1 means infinity
	private int timeEnd;
	
	public Interval(int timeB, int timeE){
		this.timeBegin = timeB;
		this.timeEnd = timeE;
	}
	
	public int getTimeBegin(){
		return this.timeBegin;
	}
	
	public int getTimeEnd(){
		return this.timeEnd;
	}
}
