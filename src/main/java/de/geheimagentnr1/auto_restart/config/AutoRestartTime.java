package de.geheimagentnr1.auto_restart.config;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Optional;


public class AutoRestartTime {
	
	
	private final int hour;
	
	private final int minute;
	
	private AutoRestartTime( int _hour, int _minute ) {
		
		hour = _hour;
		minute = _minute;
	}
	
	//package-private
	static AutoRestartTime build( int hour, int minute ) {
		
		return new AutoRestartTime( hour, minute );
	}
	
	//package-private
	static Optional<AutoRestartTime> parse( String time ) {
		
		String[] timeElements = time.split( ":" );
		if( timeElements.length != 2 ) {
			return Optional.empty();
		}
		try {
			int hour = Integer.parseInt( timeElements[0] );
			int min = Integer.parseInt( timeElements[1] );
			
			if( hour < 0 || hour > 23 || min < 0 || min > 59 ) {
				return Optional.empty();
			}
			return Optional.of( new AutoRestartTime( hour, min ) );
		} catch( NumberFormatException exception ) {
			return Optional.empty();
		}
	}
	
	public Duration getDifferenceTo( LocalDateTime time ) {
		
		LocalDate autoRestartDate = time.toLocalDate();
		LocalTime autoRestartTime = LocalTime.of( getHour(), getMinute() );
		if( time.toLocalTime().isAfter( autoRestartTime ) ) {
			autoRestartDate = autoRestartDate.plusDays( 1 );
		}
		return Duration.between( time, LocalDateTime.of( autoRestartDate, autoRestartTime ) );
	}
	
	public int getHour() {
		
		return hour;
	}
	
	public int getMinute() {
		
		return minute;
	}
	
	@Override
	public String toString() {
		
		return String.format( "%02d:%02d", hour, minute );
	}
}
