package de.geheimagentnr1.auto_restart.config;

public class RestartTime {
	
	
	private final int hour;
	
	private final int minute;
	
	//package-private
	RestartTime( int _hour, int _minute ) {
		
		hour = _hour;
		minute = _minute;
	}
	
	public int getHour() {
		
		return hour;
	}
	
	public int getMinute() {
		
		return minute;
	}
}
