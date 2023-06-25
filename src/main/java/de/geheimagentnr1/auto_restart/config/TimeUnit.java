package de.geheimagentnr1.auto_restart.config;

import org.jetbrains.annotations.NotNull;

import java.util.Locale;
import java.util.Optional;


public enum TimeUnit {
	HOURS,
	MINUTES,
	SECONDS;
	
	@NotNull
	private final String plural;
	
	@NotNull
	private final String singular;
	
	@NotNull
	private final String sign;
	
	//private
	TimeUnit() {
		
		plural = name().toLowerCase( Locale.ENGLISH );
		singular = plural.substring( 0, plural.length() - 1 );
		sign = singular.substring( 0, 1 );
	}
	
	//package-private
	@NotNull
	static Optional<TimeUnit> parse( @NotNull String timeUnitString ) {
		
		for( TimeUnit timeUnit : values() ) {
			if( timeUnit.getSign().equals( timeUnitString ) ) {
				return Optional.of( timeUnit );
			}
		}
		return Optional.empty();
	}
	
	@NotNull
	String getText( long value ) {
		
		return value == 1 ? singular : plural;
	}
	
	@NotNull
	String getSign() {
		
		return sign;
	}
}
