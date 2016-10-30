package Petri;

public class TimeSpan {
	
	/** Timestamp when the timespan begins */
	private long timeBegin;
	/** Timestamp when the timespan ends */ 
	private long timeEnd;
	/** Timestamp when the transition was enabled */
	private long enableTime;
	/** This is true when a thread is sleeping in sleep method */
	private boolean sleeping;
	
	/**
	 * @param timeB Increment to add to enabling time to set begin time
	 * @param timeE Increment to add to enabling time to set end time
	 */
	public TimeSpan(long timeB, long timeE){
		this.timeBegin = timeB;
		this.timeEnd = timeE;
		this.enableTime = -1;
	}
	
	/**
	 * Set the enabling time. This is intended to be used when the associated transition is enabled
	 * @param time timestamp in miliseconds when the enabling occurs
	 */
	public void setEnableTime(long time){
		enableTime = time;
	}
	
	/**
	 * @return the enabling time
	 */
	public long getEnableTime(){
		return this.enableTime;
	}
	
	public long getTimeBegin(){
		return this.timeBegin;
	}
	
	public long getTimeEnd(){
		return this.timeEnd;
	}
	
	/**
	 * @param time timestamp in miliseconds to figure out whether it's inside the span
	 * @return true if time is inside the span
	 */
	public boolean inTimeSpan(long time){
		if(time >= enableTime + timeBegin){
			if(timeEnd == Long.MAX_VALUE){
				return true;
			}
			else if (time <= enableTime + timeEnd){
				return true;
			}
		}
		return false;
	}
	
	/**
	 * @param time timestamp in miliseconds to figure out whether it's before the span
	 * @return true if time is befire the span
	 */
	public boolean isBeforeTimeSpan(long time){
		return time < enableTime + timeBegin;
	}
	
	/**
	 * Set the begining and ending increments to add to the enabling time in order to set the span
	 * @param timeB set time in miliseconds used to generate the span begin time
	 * @param timeE set time in miliseconds used to generate the span end time
	 * @throws IllegalArgumentException
	 */
	public void setTimeSpan(int timeB, int timeE){
		if(timeB >= 0 && timeE >= 0 && (timeB < timeE)){
			this.timeBegin = timeB;
			this.timeEnd = timeE;
		}
		else{
			throw new IllegalArgumentException("The interval time must not have a negative value");
		}
	}
	
	/**
	 * Locks the calling thread for the given time
	 * @param time time to lock the calling thread
	 */
	public void sleep(long time){
		this.sleeping = true;
		try {
			Thread.sleep(time);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		this.sleeping = false;
	}
	
	/**
	 * 
	 * @return true if a thread is locked in {@link #sleep(long time)} method
	 */
	public boolean anySleeping(){
		return sleeping;
	}
}
