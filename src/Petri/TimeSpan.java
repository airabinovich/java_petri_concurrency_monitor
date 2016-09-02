package Petri;

public class TimeSpan {
	
	private double timeBegin;
	//if timeEnd has -1 means infinity
	private double timeEnd;
	private long enableTime;
	private boolean sleeping;
	
	public TimeSpan(double timeB, double timeE){
		this.timeBegin = timeB;
		this.timeEnd = timeE;
	}
	
	public void setEnableTime(long time){
		enableTime = time;
	}
	
	public double getTimeBegin(){
		return this.timeBegin;
	}
	
	public double getTimeEnd(){
		return this.timeEnd;
	}
	
	public boolean inTimeSpan(long time){
		return (time >= enableTime + timeBegin) && (time <= enableTime + timeEnd);
	}
	
	public boolean beforeWindow(long time){
		return time < enableTime + timeBegin;
	}
	
	public void setTimeSpan(int timeB, int timeE){
		if(timeB >= 0 && (timeE >= 0 || timeE == -1)){
			this.timeBegin = timeB;
			this.timeEnd = timeE;
		}
		else{
			throw new IllegalArgumentException("The interval time must not has a negative value");
		}
	}
	
	public void sleep(double time){
		this.sleeping = true;
		try {
			Thread.sleep((long)time);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		this.sleeping = false;
	}
	
	public boolean anySleeping(){
		return sleeping;
	}
}
