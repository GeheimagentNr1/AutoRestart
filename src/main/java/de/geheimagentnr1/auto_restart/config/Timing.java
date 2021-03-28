package de.geheimagentnr1.auto_restart.config;

import java.util.Optional;


public class Timing {
	
	
	private final long value;
	
	private final TimeUnit timeUnit;
	
	private Timing( long _value, TimeUnit _timeUnit ) {
		
		value = _value;
		timeUnit = _timeUnit;
	}
	
	//package-private
	static Timing build( long value, TimeUnit timeUnit ) {
		
		return new Timing( value, timeUnit );
	}
	
	//package-private
	static Optional<Timing> parse( String timingString ) {
		
		String valueString = timingString.substring( 0, timingString.length() - 1 );
		String timeUnitString = timingString.substring( timingString.length() - 1 );
		
		try {
			long value = Long.parseLong( valueString );
			Optional<TimeUnit> timeUnit = TimeUnit.parse( timeUnitString );
			
			if( value < 0 ) {
				return Optional.empty();
			}
			return timeUnit.map( unit -> new Timing( value, unit ) );
		} catch( NumberFormatException exception ) {
			return Optional.empty();
		}
	}
	
	public long getSeconds() {
		
		switch( timeUnit ) {
			case SECONDS:
				return value;
			case MINUTES:
				return value * 60;
			case HOURS:
				return value * 3600;
			default:
				return 0;
		}
	}
	
	public String getDisplayString() {
		
		return String.format( "%d %s", value, timeUnit.getText( value ) );
	}
	
	@Override
	public String toString() {
		
		return value + timeUnit.getSign();
	}
}
