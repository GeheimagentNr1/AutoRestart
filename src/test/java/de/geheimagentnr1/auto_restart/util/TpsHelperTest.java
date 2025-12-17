package de.geheimagentnr1.auto_restart.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;


class TpsHelperTest {
	
	
	@Test
	void calculateTps_perfectTps_returns20() {
		
		// 50ms per tick = 20 TPS (50ms * 1_000_000 = 50_000_000 nanoseconds)
		long[] tickTimes = new long[100];
		for( int i = 0; i < tickTimes.length; i++ ) {
			tickTimes[i] = 50_000_000L;
		}
		
		double tps = TpsHelper.calculateTps( tickTimes );
		
		assertEquals( 20.0, tps, 0.001 );
	}
	
	@Test
	void calculateTps_slowServer_returnsLowerTps() {
		
		// 100ms per tick = 10 TPS
		long[] tickTimes = new long[100];
		for( int i = 0; i < tickTimes.length; i++ ) {
			tickTimes[i] = 100_000_000L;
		}
		
		double tps = TpsHelper.calculateTps( tickTimes );
		
		assertEquals( 10.0, tps, 0.001 );
	}
	
	@Test
	void calculateTps_veryFastServer_cappedAt20() {
		
		// 10ms per tick would be 100 TPS, but should be capped at 20
		long[] tickTimes = new long[100];
		for( int i = 0; i < tickTimes.length; i++ ) {
			tickTimes[i] = 10_000_000L;
		}
		
		double tps = TpsHelper.calculateTps( tickTimes );
		
		assertEquals( 20.0, tps, 0.001 );
	}
	
	@Test
	void calculateTps_mixedTickTimes_returnsAverage() {
		
		// Mix of 50ms and 100ms ticks, average = 75ms = ~13.33 TPS
		long[] tickTimes = new long[100];
		for( int i = 0; i < tickTimes.length; i++ ) {
			tickTimes[i] = ( i % 2 == 0 ) ? 50_000_000L : 100_000_000L;
		}
		
		double tps = TpsHelper.calculateTps( tickTimes );
		
		assertEquals( 13.333, tps, 0.01 );
	}
}
