package Petri;

public class Interval {
	
	private int timeBegin;
	//if timeEnd has -1 means infinity
	private int timeEnd;
	//closure could be an int containing:
	//0 if closure is "open"
	//1 if closure is "open-closed"
	//2 if closure is "closed-open"
	//3 if closure is "closed"
	private int closure;
	
	public Interval(int timeB, int timeE, int closure){
		this.timeBegin = timeB;
		this.timeEnd = timeE;
		this.closure = closure;	
	}
	
	public int getTimeBegin(){
		return this.timeBegin;
	}
	
	public int getTimeEnd(){
		return this.timeEnd;
	}
	
	public int getClosure(){
		return this.closure;
	}
}
