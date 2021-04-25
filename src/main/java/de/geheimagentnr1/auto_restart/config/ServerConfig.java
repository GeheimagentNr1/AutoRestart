package de.geheimagentnr1.auto_restart.config;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.ModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;


public class ServerConfig {
	
	
	private static final Logger LOGGER = LogManager.getLogger( ServerConfig.class );
	
	private static final String MOD_NAME = ModLoadingContext.get().getActiveContainer().getModInfo().getDisplayName();
	
	private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
	
	public static final ForgeConfigSpec CONFIG;
	
	private static final ForgeConfigSpec.BooleanValue USES_EXTERNAL_RESTART_SCRIPT;
	
	private static final ForgeConfigSpec.ConfigValue<String> RESTART_COMMAND;
	
	private static final ForgeConfigSpec.BooleanValue AUTO_RESTART_ENABLED;
	
	private static final ForgeConfigSpec.ConfigValue<List<String>> AUTO_RESTART_TIMES;
	
	private static final ForgeConfigSpec.BooleanValue AUTO_RESTART_ON_CRASH;
	
	private static final ForgeConfigSpec.ConfigValue<List<String>> AUTO_RESTART_WARNING_TIMES;
	
	private static final ForgeConfigSpec.BooleanValue ON_EMPTY_RESTART_ENABLED;
	
	private static final ForgeConfigSpec.ConfigValue<String> ON_EMPTY_RESTART_DELAY;
	
	private static final ForgeConfigSpec.BooleanValue LOW_TPS_RESTART_ENABLED;
	
	private static final ForgeConfigSpec.DoubleValue LOW_TPS_RESTART_MINIMUM_TPS_LEVEL;
	
	private static final ForgeConfigSpec.ConfigValue<String> LOW_TPS_RESTART_DELAY;
	
	private static final ArrayList<AutoRestartTime> autoRestartTimes = new ArrayList<>();
	
	private static final ArrayList<Timing> autoRestartWarningTimes = new ArrayList<>();
	
	private static Timing onEmptyRestartDelay;
	
	private static Timing lowTpsRestartDelay;
	
	static {
		
		BUILDER.comment( "Options for restarting:" )
			.push( "restart" );
		USES_EXTERNAL_RESTART_SCRIPT = BUILDER.comment( "Is the server started by an external restart script?" )
			.define( "use_external_restart_script", false );
		RESTART_COMMAND = BUILDER.comment( String.format(
			"Command that is executed on Server stopped to restart the server. Only called if %s is false.",
			USES_EXTERNAL_RESTART_SCRIPT.getPath()
		) ).define( "restart_command", "" );
		BUILDER.pop();
		BUILDER.comment( "Option for auto restarting:" )
			.push( "auto_restart" );
		AUTO_RESTART_ENABLED = BUILDER.comment( "Should the Server do automatic restarts?" )
			.define( "enabled", false );
		AUTO_RESTART_TIMES =
			BUILDER.comment( "Times in 24 hour format on with the server will automaticaly restarts." )
				.define(
					"times",
					new ArrayList<>( Arrays.asList(
						AutoRestartTime.build( 14, 0 ).toString(),
						AutoRestartTime.build( 16, 32 ).toString()
					) )
				);
		AUTO_RESTART_ON_CRASH = BUILDER.comment( "Should the server be automatically restarted when it crashes." )
			.define( "on_crash", false );
		AUTO_RESTART_WARNING_TIMES = BUILDER.comment( String.format(
			"Times before an auto restart of the server, a restart warning should be shown.%n" +
				"Examples:%n" +
				" - 5s - For a message 5 seconds before a restart%n" +
				" - 7m - For a message 7 minutes before a restart%n" +
				" - 2h - For a message 2 hours before a restart"
		) ).define(
			"warning_times",
			new ArrayList<>( Arrays.asList(
				Timing.build( 5, TimeUnit.SECONDS ).toString(),
				Timing.build( 4, TimeUnit.SECONDS ).toString(),
				Timing.build( 3, TimeUnit.SECONDS ).toString(),
				Timing.build( 2, TimeUnit.SECONDS ).toString(),
				Timing.build( 1, TimeUnit.SECONDS ).toString()
			) )
		);
		BUILDER.pop();
		BUILDER.comment( "Options for restart, if the server is empty" )
			.push( "on_empty_restart" );
		ON_EMPTY_RESTART_ENABLED = BUILDER.comment( "Should the server restart, if no players are online?" )
			.define( "enabled", false );
		ON_EMPTY_RESTART_DELAY = BUILDER.comment( String.format(
			"Delay after the server should restart, if it is empty.%n" +
				"Examples:%n" +
				" - 5s - For a delay 5 seconds%n" +
				" - 7m - For a delay 7 minutes%n" +
				" - 2h - For a delay 2 hours"
		) ).define(
			"delay",
			Timing.build( 10, TimeUnit.MINUTES ).toString(),
			object -> {
				if( object instanceof String ) {
					return Timing.parse( (String)object ).isPresent();
				}
				return false;
			}
		);
		BUILDER.comment( "Options for restart, if the tps of server or its dimensions are " )
			.push( "low_tps_restart" );
		LOW_TPS_RESTART_ENABLED = BUILDER.comment(
			"Should the server restart, if it is below a tps level for a certain time?"
		).define( "enabled", false );
		LOW_TPS_RESTART_MINIMUM_TPS_LEVEL = BUILDER.comment(
			"TPS level below which the server is restarted, if it lasts for a certain time."
		).defineInRange( "minium_tps_level", 0.0, 0.0, 20.0 );
		LOW_TPS_RESTART_DELAY = BUILDER.comment( String.format(
			"Delay, that the server must be below the defined TPS level, in order for it to be restarted.%n" +
				"Examples:%n" + " - 5s - For a delay 5 seconds%n" + " - 7m - For a delay 7 minutes%n" +
				" - 2h - For a delay 2 hours"
		) ).define(
			"delay",
			Timing.build( 1, TimeUnit.MINUTES ).toString(),
			object -> {
				if( object instanceof String ) {
					return Timing.parse( (String)object ).isPresent();
				}
				return false;
			}
		);
		BUILDER.pop();
		BUILDER.pop();
		CONFIG = BUILDER.build();
	}
	
	public static void handleConfigChange() {
		
		printConfig();
		loadAutoRestartTimes();
		loadAutoRestartWarningTimes();
		loadOnEmptyRestartDelay();
		loadLowTpsRestartDelay();
	}
	
	private static void printConfig() {
		
		LOGGER.info( "Loading \"{}\" Server Config", MOD_NAME );
		LOGGER.info( "{} = {}", USES_EXTERNAL_RESTART_SCRIPT.getPath(), USES_EXTERNAL_RESTART_SCRIPT.get() );
		LOGGER.info( "{} = {}", RESTART_COMMAND.getPath(), RESTART_COMMAND.get() );
		LOGGER.info( "{} = {}", AUTO_RESTART_ENABLED.getPath(), AUTO_RESTART_ENABLED.get() );
		LOGGER.info( "{} = {}", AUTO_RESTART_TIMES.getPath(), AUTO_RESTART_TIMES.get() );
		LOGGER.info( "{} = {}", AUTO_RESTART_ON_CRASH.getPath(), AUTO_RESTART_ON_CRASH.get() );
		LOGGER.info( "{} = {}", AUTO_RESTART_WARNING_TIMES.getPath(), AUTO_RESTART_WARNING_TIMES.get() );
		LOGGER.info( "{} = {}", ON_EMPTY_RESTART_ENABLED.getPath(), ON_EMPTY_RESTART_ENABLED.get() );
		LOGGER.info( "{} = {}", ON_EMPTY_RESTART_DELAY.getPath(), ON_EMPTY_RESTART_DELAY.get() );
		LOGGER.info( "\"{}\" Server Config loaded", MOD_NAME );
	}
	
	private static synchronized void loadAutoRestartTimes() {
		
		autoRestartTimes.clear();
		List<String> autoRestartTimeStrings = AUTO_RESTART_TIMES.get();
		for( int i = 0; i < autoRestartTimeStrings.size(); i++ ) {
			Optional<AutoRestartTime> autoRestartTime = AutoRestartTime.parse( autoRestartTimeStrings.get( i ) );
			if( autoRestartTime.isPresent() ) {
				autoRestartTimes.add( autoRestartTime.get() );
			} else {
				LOGGER.warn(
					"{}: Removed invalid {} from auto restart times.",
					MOD_NAME,
					autoRestartTimeStrings.get( i )
				);
				autoRestartTimeStrings.remove( i );
				i--;
			}
		}
	}
	
	private static synchronized void loadAutoRestartWarningTimes() {
		
		autoRestartWarningTimes.clear();
		List<String> autoRestartWarningTimeStrings = AUTO_RESTART_WARNING_TIMES.get();
		for( int i = 0; i < autoRestartWarningTimeStrings.size(); i++ ) {
			Optional<Timing> autoRestartWarningTime = Timing.parse( autoRestartWarningTimeStrings.get( i ) );
			if( autoRestartWarningTime.isPresent() ) {
				autoRestartWarningTimes.add( autoRestartWarningTime.get() );
			} else {
				LOGGER.warn( String.format(
					"%s: Removed invalid %s from auto restart warning times.",
					MOD_NAME,
					autoRestartWarningTime
				) );
				autoRestartWarningTimeStrings.remove( i );
				i--;
			}
		}
	}
	
	private static void loadOnEmptyRestartDelay() {
		
		try {
			onEmptyRestartDelay = Timing.parse( ON_EMPTY_RESTART_DELAY.get() )
				.orElseThrow( () -> new IllegalStateException( String.format(
					"%s: Invalid on empty restart delay",
					MOD_NAME
				) ) );
		} catch( Throwable throwable ) {
			throw new IllegalStateException( throwable );
		}
	}
	
	private static void loadLowTpsRestartDelay() {
		
		try {
			lowTpsRestartDelay = Timing.parse( LOW_TPS_RESTART_DELAY.get() )
				.orElseThrow( () -> new IllegalStateException( String.format(
					"%s: Invalid low tps restart delay",
					MOD_NAME
				) ) );
		} catch( Throwable throwable ) {
			throw new IllegalStateException( throwable );
		}
	}
	
	public static boolean usesExternalRestartScript() {
		
		return USES_EXTERNAL_RESTART_SCRIPT.get();
	}
	
	public static String getRestartCommand() {
		
		return RESTART_COMMAND.get();
	}
	
	public static boolean isAutoRestartEnabled() {
		
		if( getAutoRestartTimes().isEmpty() ) {
			AUTO_RESTART_ENABLED.set( false );
		}
		return AUTO_RESTART_ENABLED.get();
	}
	
	public static synchronized List<AutoRestartTime> getAutoRestartTimes() {
		
		return autoRestartTimes;
	}
	
	public static boolean shouldAutoRestartOnCrash() {
		
		return AUTO_RESTART_ON_CRASH.get();
	}
	
	public static synchronized List<Timing> getAutoRestartWarningTimes() {
		
		return autoRestartWarningTimes;
	}
	
	public static boolean getOnEmptyRestartEnabled() {
		
		return ON_EMPTY_RESTART_ENABLED.get();
	}
	
	public static Timing getOnEmptyRestartDelay() {
		
		return onEmptyRestartDelay;
	}
	
	public static boolean isLowTpsRestartEnabled() {
		
		return LOW_TPS_RESTART_ENABLED.get();
	}
	
	public static double getLowTpsRestartMinimumTpsLevel() {
		
		return LOW_TPS_RESTART_MINIMUM_TPS_LEVEL.get();
	}
	
	public static Timing getLowTpsRestartDelay() {
		
		return lowTpsRestartDelay;
	}
}
