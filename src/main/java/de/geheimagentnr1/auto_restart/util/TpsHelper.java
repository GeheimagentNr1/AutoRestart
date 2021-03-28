package de.geheimagentnr1.auto_restart.util;

public class TpsHelper {
	
	
	public static double calculateTps( long[] tickTimes ) {
		
		double tickTime = mean( tickTimes ) / 1000000.0;
		return Math.min( 1000.0 / tickTime, 20 );
	}
	
	private static long mean( long[] values ) {
		
		long sum = 0L;
		for( long v : values ) {
			sum += v;
		}
		return sum / values.length;
	}
}
