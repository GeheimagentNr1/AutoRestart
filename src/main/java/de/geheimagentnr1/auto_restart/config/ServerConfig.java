package de.geheimagentnr1.auto_restart.config;

import de.geheimagentnr1.minecraft_forge_api.AbstractMod;
import de.geheimagentnr1.minecraft_forge_api.config.AbstractConfig;
import lombok.extern.log4j.Log4j2;
import net.minecraftforge.fml.config.ModConfig;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;


@Log4j2
public class ServerConfig extends AbstractConfig {
	
	
	@NotNull
	private static final String RESTART_KEY = "restart";
	
	@NotNull
	private static final List<String> USES_EXTERNAL_RESTART_SCRIPT_KEY = List.of(
		RESTART_KEY,
		"use_external_restart_script"
	);
	
	@NotNull
	private static final List<String> RESTART_COMMAND_KEY = List.of( RESTART_KEY, "restart_command" );
	
	@NotNull
	private static final String AUTO_RESTART_KEY = "auto_restart";
	
	@NotNull
	private static final List<String> AUTO_RESTART_ENABLED_KEY = List.of( AUTO_RESTART_KEY, "enabled" );
	
	@NotNull
	private static final List<String> AUTO_RESTART_TIMES_KEY = List.of( AUTO_RESTART_KEY, "times" );
	
	@NotNull
	private static final List<String> AUTO_RESTART_ON_CRASH_KEY = List.of( AUTO_RESTART_KEY, "on_crash" );
	
	@NotNull
	private static final List<String> AUTO_RESTART_WARNING_TIMES_KEY = List.of( AUTO_RESTART_KEY, "warning_times" );
	
	@NotNull
	private static final String ON_EMPTY_RESTART_KEY = "on_empty_restart";
	
	@NotNull
	private static final List<String> ON_EMPTY_RESTART_ENABLED_KEY = List.of( ON_EMPTY_RESTART_KEY, "enabled" );
	
	@NotNull
	private static final List<String> ON_EMPTY_RESTART_DELAY_KEY = List.of( ON_EMPTY_RESTART_KEY, "delay" );
	
	@NotNull
	private static final String LOW_TPS_RESTART_KEY = "low_tps_restart";
	
	@NotNull
	private static final List<String> LOW_TPS_RESTART_ENABLED_KEY = List.of( LOW_TPS_RESTART_KEY, "enabled" );
	
	@NotNull
	private static final List<String> LOW_TPS_RESTART_MINIMUM_TPS_LEVEL_KEY = List.of(
		LOW_TPS_RESTART_KEY,
		"minium_tps_level"
	);
	
	@NotNull
	private static final List<String> LOW_TPS_RESTART_DELAY_KEY = List.of( LOW_TPS_RESTART_KEY, "delay" );
	
	@NotNull
	private final ArrayList<AutoRestartTime> autoRestartTimes = new ArrayList<>();
	
	@NotNull
	private final ArrayList<Timing> autoRestartWarningTimes = new ArrayList<>();
	
	private Timing onEmptyRestartDelay;
	
	private Timing lowTpsRestartDelay;
	
	public ServerConfig( @NotNull AbstractMod _abstractMod ) {
		
		super( _abstractMod );
	}
	
	@NotNull
	@Override
	public ModConfig.Type type() {
		
		return ModConfig.Type.SERVER;
	}
	
	@Override
	public boolean isEarlyLoad() {
		
		return false;
	}
	
	@Override
	protected void registerConfigValues() {
		
		push( "Options for restarting:", RESTART_KEY );
		registerConfigValue(
			"Is the server started by an external restart script?",
			USES_EXTERNAL_RESTART_SCRIPT_KEY,
			false
		);
		registerConfigValue(
			String.format(
				"Command that is executed on Server stopped to restart the server. Only called if \"%s\" is false.",
				USES_EXTERNAL_RESTART_SCRIPT_KEY
			),
			RESTART_COMMAND_KEY,
			""
		);
		pop();
		push( "Option for auto restarting:", AUTO_RESTART_KEY );
		registerConfigValue( "Should the Server do automatic restarts?", AUTO_RESTART_ENABLED_KEY, false );
		registerConfigValue(
			"Times in 24-hour format on which the server will automatically restart",
			AUTO_RESTART_TIMES_KEY,
			new ArrayList<>( Arrays.asList(
				AutoRestartTime.build( 14, 0 ).toString(),
				AutoRestartTime.build( 16, 32 ).toString()
			) )
		);
		registerConfigValue(
			"Should the server be automatically restarted when it crashes.",
			AUTO_RESTART_ON_CRASH_KEY,
			false
		);
		registerConfigValue(
			List.of(
				"Times before an auto restart of the server, a restart warning should be shown.",
				"Examples:",
				" - 5s - For a message 5 seconds before a restart",
				" - 7m - For a message 7 minutes before a restart",
				" - 2h - For a message 2 hours before a restart"
			),
			AUTO_RESTART_WARNING_TIMES_KEY,
			new ArrayList<>( Arrays.asList(
				Timing.build( 5, TimeUnit.SECONDS ).toString(),
				Timing.build( 4, TimeUnit.SECONDS ).toString(),
				Timing.build( 3, TimeUnit.SECONDS ).toString(),
				Timing.build( 2, TimeUnit.SECONDS ).toString(),
				Timing.build( 1, TimeUnit.SECONDS ).toString()
			) )
		);
		pop();
		push( "Options for restart, if the server is empty:", ON_EMPTY_RESTART_KEY );
		registerConfigValue(
			"Should the server restart, if no players are online?",
			ON_EMPTY_RESTART_ENABLED_KEY,
			false
		);
		registerConfigValue(
			List.of(
				"Delay after the server should restart, if it is empty.",
				"Examples:",
				" - 5s - For a delay 5 seconds",
				" - 7m - For a delay 7 minutes",
				" - 2h - For a delay 2 hours"
			),
			ON_EMPTY_RESTART_DELAY_KEY,
			( builder, path ) -> builder.define(
				path,
				Timing.build( 10, TimeUnit.MINUTES ).toString(),
				object -> {
					if( object instanceof String ) {
						return Timing.parse( (String)object ).isPresent();
					}
					return false;
				}
			)
		);
		pop();
		push( "Options for restart, if the tps of server or its dimensions are low:", LOW_TPS_RESTART_KEY );
		registerConfigValue(
			"Should the server restart, if it is below a tps level for a specified time?",
			LOW_TPS_RESTART_ENABLED_KEY,
			false
		);
		registerConfigValue(
			"TPS level below which the server is restarted, if it lasts for a specified time.",
			LOW_TPS_RESTART_MINIMUM_TPS_LEVEL_KEY,
			( builder, path ) -> builder.defineInRange( path, 0.0, 0.0, 20.0 )
		);
		registerConfigValue(
			List.of(
				"Delay, that the server must be below the defined TPS level, in order for it to be restarted.",
				"Examples:",
				" - 5s - For a delay 5 seconds",
				" - 7m - For a delay 7 minutes",
				" - 2h - For a delay 2 hours"
			),
			LOW_TPS_RESTART_DELAY_KEY,
			( builder, path ) -> builder.define(
				path,
				Timing.build( 1, TimeUnit.MINUTES ).toString(),
				object -> {
					if( object instanceof String ) {
						return Timing.parse( (String)object ).isPresent();
					}
					return false;
				}
			
			)
		);
		pop();
	}
	
	@Override
	protected void handleConfigChanging() {
		
		loadAutoRestartTimes();
		loadAutoRestartWarningTimes();
		loadOnEmptyRestartDelay();
		loadLowTpsRestartDelay();
	}
	
	private synchronized void loadAutoRestartTimes() {
		
		autoRestartTimes.clear();
		List<String> autoRestartTimeStrings = getAutoRestartTimesValue();
		for( int i = 0; i < autoRestartTimeStrings.size(); i++ ) {
			Optional<AutoRestartTime> autoRestartTime = AutoRestartTime.parse( autoRestartTimeStrings.get( i ) );
			if( autoRestartTime.isPresent() ) {
				autoRestartTimes.add( autoRestartTime.get() );
			} else {
				log.warn(
					"{}: Removed invalid {} from auto restart times.",
					abstractMod.getModName(),
					autoRestartTimeStrings.get( i )
				);
				autoRestartTimeStrings.remove( i );
				i--;
			}
		}
	}
	
	private synchronized void loadAutoRestartWarningTimes() {
		
		autoRestartWarningTimes.clear();
		List<String> autoRestartWarningTimeStrings = getAutoRestartWarningTimesValue();
		for( int i = 0; i < autoRestartWarningTimeStrings.size(); i++ ) {
			Optional<Timing> autoRestartWarningTime = Timing.parse( autoRestartWarningTimeStrings.get( i ) );
			if( autoRestartWarningTime.isPresent() ) {
				autoRestartWarningTimes.add( autoRestartWarningTime.get() );
			} else {
				log.warn( String.format(
					"%s: Removed invalid %s from auto restart warning times.",
					abstractMod.getModName(),
					autoRestartWarningTime
				) );
				autoRestartWarningTimeStrings.remove( i );
				i--;
			}
		}
	}
	
	private void loadOnEmptyRestartDelay() {
		
		try {
			onEmptyRestartDelay = Timing.parse( getOnEmptyRestartDelayValue() )
				.orElseThrow( () -> new IllegalStateException( String.format(
					"%s: Invalid on empty restart delay",
					abstractMod.getModName()
				) ) );
		} catch( Throwable throwable ) {
			throw new IllegalStateException( throwable );
		}
	}
	
	private void loadLowTpsRestartDelay() {
		
		try {
			lowTpsRestartDelay = Timing.parse( getLowTpsRestartDelayValue() )
				.orElseThrow( () -> new IllegalStateException( String.format(
					"%s: Invalid low tps restart delay",
					abstractMod.getModName()
				) ) );
		} catch( Throwable throwable ) {
			throw new IllegalStateException( throwable );
		}
	}
	
	public boolean usesExternalRestartScript() {
		
		return getValue( Boolean.class, USES_EXTERNAL_RESTART_SCRIPT_KEY );
	}
	
	@NotNull
	public String getRestartCommand() {
		
		return getValue( String.class, RESTART_COMMAND_KEY );
	}
	
	public boolean isAutoRestartEnabled() {
		
		if( getAutoRestartTimes().isEmpty() ) {
			setValue( Boolean.class, AUTO_RESTART_ENABLED_KEY, false );
		}
		return getValue( Boolean.class, AUTO_RESTART_ENABLED_KEY );
	}
	
	@NotNull
	private List<String> getAutoRestartTimesValue() {
		
		return getListValue( String.class, AUTO_RESTART_TIMES_KEY );
	}
	
	@NotNull
	public synchronized List<AutoRestartTime> getAutoRestartTimes() {
		
		return autoRestartTimes;
	}
	
	public boolean shouldAutoRestartOnCrash() {
		
		return getValue( Boolean.class, AUTO_RESTART_ON_CRASH_KEY );
	}
	
	@NotNull
	private List<String> getAutoRestartWarningTimesValue() {
		
		return getListValue( String.class, AUTO_RESTART_WARNING_TIMES_KEY );
	}
	
	@NotNull
	public synchronized List<Timing> getAutoRestartWarningTimes() {
		
		return autoRestartWarningTimes;
	}
	
	public boolean getOnEmptyRestartEnabled() {
		
		return getValue( Boolean.class, ON_EMPTY_RESTART_ENABLED_KEY );
	}
	
	@NotNull
	private String getOnEmptyRestartDelayValue() {
		
		return getValue( String.class, ON_EMPTY_RESTART_DELAY_KEY );
	}
	
	@NotNull
	public Timing getOnEmptyRestartDelay() {
		
		return onEmptyRestartDelay;
	}
	
	public boolean isLowTpsRestartEnabled() {
		
		return getValue( Boolean.class, LOW_TPS_RESTART_ENABLED_KEY );
	}
	
	public double getLowTpsRestartMinimumTpsLevel() {
		
		return getValue( Double.class, LOW_TPS_RESTART_MINIMUM_TPS_LEVEL_KEY );
	}
	
	@NotNull
	private String getLowTpsRestartDelayValue() {
		
		return getValue( String.class, LOW_TPS_RESTART_DELAY_KEY );
	}
	
	@NotNull
	public Timing getLowTpsRestartDelay() {
		
		return lowTpsRestartDelay;
	}
}
