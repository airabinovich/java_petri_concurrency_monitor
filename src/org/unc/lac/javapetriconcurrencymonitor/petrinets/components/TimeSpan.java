package org.unc.lac.javapetriconcurrencymonitor.petrinets.components;

public class TimeSpan {
	
	/** Time increment when the timespan begins since {@link #enablingTime} */
	private long timespanBeginning;
	/** time increment when the timespan ends since {@link #enablingTime}*/ 
	private long timespanEnding;
	/** Timestamp when the transition was enabled */
	private long enablingTime;
	
	/**
	 * @param _timespanBeginning Increment to add to enabling time to set begin time
	 * @param _timespanEnding Increment to add to enabling time to set end time
	 */
	public TimeSpan(long _timespanBeginning, long _timespanEnding){
		this.timespanBeginning = _timespanBeginning;
		this.timespanEnding = _timespanEnding;
		this.enablingTime = -1;
	}
	
	/**
	 * Sets the enabling time. This is intended to be used when the associated transition is enabled
	 * @param time timestamp in miliseconds when the enabling occurs
	 */
	public void setEnablingTime(long time){
		enablingTime = time;
	}
	
	/**
	 * @return the enabling time
	 */
	public long getEnablingTime(){
		return this.enablingTime;
	}
	
	public long getTimespanBeginning(){
		return this.timespanBeginning;
	}
	
	public long getTimespanEnding(){
		return this.timespanEnding;
	}
	
	/**
	 * @param time timestamp in miliseconds to figure out whether it's inside the span
	 * @return true if time is inside the span
	 */
	public boolean inTimeSpan(long time){
		if(enablingTime < 0){
			// the transition hasn't been enablred yet
			return false;
		}
		if(time >= enablingTime + timespanBeginning){
			if(timespanEnding == Long.MAX_VALUE){
				return true;
			}
			else if (time <= enablingTime + timespanEnding){
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
		return time < enablingTime + timespanBeginning;
	}
	
	/**
	 * Set the begining and ending increments to add to the enabling time in order to set the span
	 * @param timeB set time in miliseconds used to generate the span begin time
	 * @param timeE set time in miliseconds used to generate the span end time
	 * @throws IllegalArgumentException
	 */
	public void setTimeSpan(int timeB, int timeE){
		if(timeB >= 0 && timeE >= 0 && (timeB < timeE)){
			this.timespanBeginning = timeB;
			this.timespanEnding = timeE;
		}
		else{
			throw new IllegalArgumentException("The interval time must not have a negative value");
		}
	}
}
