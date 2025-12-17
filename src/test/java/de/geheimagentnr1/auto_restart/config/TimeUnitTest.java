package de.geheimagentnr1.auto_restart.config;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;


class TimeUnitTest {
	
	
	@Test
	void parse_validHourSign_returnsHours() {
		
		Optional<TimeUnit> result = TimeUnit.parse( "h" );
		
		assertTrue( result.isPresent() );
		assertEquals( TimeUnit.HOURS, result.get() );
	}
	
	@Test
	void parse_validMinuteSign_returnsMinutes() {
		
		Optional<TimeUnit> result = TimeUnit.parse( "m" );
		
		assertTrue( result.isPresent() );
		assertEquals( TimeUnit.MINUTES, result.get() );
	}
	
	@Test
	void parse_validSecondSign_returnsSeconds() {
		
		Optional<TimeUnit> result = TimeUnit.parse( "s" );
		
		assertTrue( result.isPresent() );
		assertEquals( TimeUnit.SECONDS, result.get() );
	}
	
	@ParameterizedTest
	@ValueSource( strings = { "x", "H", "M", "S", "", "hour", "minute", "second" } )
	void parse_invalidSign_returnsEmpty( String invalidSign ) {
		
		Optional<TimeUnit> result = TimeUnit.parse( invalidSign );
		
		assertTrue( result.isEmpty() );
	}
	
	@ParameterizedTest
	@CsvSource( {
		"HOURS, 1, hour",
		"HOURS, 2, hours",
		"MINUTES, 1, minute",
		"MINUTES, 5, minutes",
		"SECONDS, 1, second",
		"SECONDS, 30, seconds"
	} )
	void getText_returnsCorrectSingularOrPlural( TimeUnit unit, long value, String expected ) {
		
		String result = unit.getText( value );
		
		assertEquals( expected, result );
	}
	
	@Test
	void getSign_returnsCorrectSigns() {
		
		assertEquals( "h", TimeUnit.HOURS.getSign() );
		assertEquals( "m", TimeUnit.MINUTES.getSign() );
		assertEquals( "s", TimeUnit.SECONDS.getSign() );
	}
}
