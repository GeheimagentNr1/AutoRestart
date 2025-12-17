package de.geheimagentnr1.auto_restart.config;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;


class TimingTest {
	
	
	@Test
	void build_createsTimingCorrectly() {
		
		Timing timing = Timing.build( 5, TimeUnit.MINUTES );
		
		assertEquals( 300, timing.getSeconds() );
		assertEquals( "5m", timing.toString() );
	}
	
	@ParameterizedTest
	@CsvSource( {
		"10s, 10",
		"5m, 300",
		"2h, 7200",
		"1h, 3600",
		"60s, 60",
		"60m, 3600"
	} )
	void parse_validTiming_returnsCorrectSeconds( String input, long expectedSeconds ) {
		
		Optional<Timing> result = Timing.parse( input );
		
		assertTrue( result.isPresent() );
		assertEquals( expectedSeconds, result.get().getSeconds() );
	}
	
	@ParameterizedTest
	@ValueSource( strings = { "abc", "10x", "-5m", "5", "m5" } )
	void parse_invalidTiming_returnsEmpty( String invalidInput ) {
		
		Optional<Timing> result = Timing.parse( invalidInput );
		
		assertTrue( result.isEmpty() );
	}
	
	@Test
	void getSeconds_hours_convertsCorrectly() {
		
		Timing timing = Timing.build( 3, TimeUnit.HOURS );
		
		assertEquals( 10800, timing.getSeconds() );
	}
	
	@Test
	void getSeconds_minutes_convertsCorrectly() {
		
		Timing timing = Timing.build( 45, TimeUnit.MINUTES );
		
		assertEquals( 2700, timing.getSeconds() );
	}
	
	@Test
	void getSeconds_seconds_returnsValue() {
		
		Timing timing = Timing.build( 90, TimeUnit.SECONDS );
		
		assertEquals( 90, timing.getSeconds() );
	}
	
	@ParameterizedTest
	@CsvSource( {
		"1, HOURS, 1 hour",
		"2, HOURS, 2 hours",
		"1, MINUTES, 1 minute",
		"30, MINUTES, 30 minutes",
		"1, SECONDS, 1 second",
		"45, SECONDS, 45 seconds"
	} )
	void getDisplayString_returnsFormattedString( long value, TimeUnit unit, String expected ) {
		
		Timing timing = Timing.build( value, unit );
		
		assertEquals( expected, timing.getDisplayString() );
	}
	
	@Test
	void toString_returnsCompactFormat() {
		
		assertEquals( "5h", Timing.build( 5, TimeUnit.HOURS ).toString() );
		assertEquals( "30m", Timing.build( 30, TimeUnit.MINUTES ).toString() );
		assertEquals( "15s", Timing.build( 15, TimeUnit.SECONDS ).toString() );
	}
	
	@Test
	void parse_zeroValue_isValid() {
		
		Optional<Timing> result = Timing.parse( "0s" );
		
		assertTrue( result.isPresent() );
		assertEquals( 0, result.get().getSeconds() );
	}
	
	@Test
	void parse_largeValue_isValid() {
		
		Optional<Timing> result = Timing.parse( "9999h" );
		
		assertTrue( result.isPresent() );
		assertEquals( 9999 * 3600, result.get().getSeconds() );
	}
}
