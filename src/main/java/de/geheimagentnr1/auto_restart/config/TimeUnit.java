package de.geheimagentnr1.auto_restart.config;

import java.util.Locale;
import java.util.Optional;


public enum TimeUnit {
	HOURS,
	MINUTES,
	SECONDS;
	
	private final String plural;
	
	private final String singular;
	
	private final String sign;
	
	//private
	TimeUnit() {
		
		plural = name().toLowerCase( Locale.ENGLISH );
		singular = plural.substring( 0, plural.length() - 1 );
		sign = singular.substring( 0, 1 );
	}
	
	//package-private
	static Optional<TimeUnit> parse( String timeUnitString ) {
		
		for( TimeUnit timeUnit : values() ) {
			if( timeUnit.getSign().equals( timeUnitString ) ) {
				return Optional.of( timeUnit );
			}
		}
		return Optional.empty();
	}
	
	public String getText( long value ) {
		
		return value == 1 ? singular : plural;
	}
	
	public String getSign() {
		
		return sign;
	}
}
