package de.geheimagentnr1.auto_restart.config;

import de.geheimagentnr1.auto_restart.AutoRestart;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import net.neoforged.neoforge.common.ModConfigSpec;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;


@Log4j2
public class ServerConfig {
	
	
	@Getter
	@NotNull
	private final ModConfigSpec spec;
	
	@NotNull
	private final ModConfigSpec.BooleanValue usesExternalRestartScript;
	
	@NotNull
	private final ModConfigSpec.ConfigValue<String> restartCommand;
	
	@NotNull
	private final ModConfigSpec.BooleanValue autoRestartEnabled;
	
	@NotNull
	private final ModConfigSpec.ConfigValue<List<? extends String>> autoRestartTimesValue;
	
	@NotNull
	private final ModConfigSpec.BooleanValue autoRestartOnCrash;
	
	@NotNull
	private final ModConfigSpec.ConfigValue<List<? extends String>> autoRestartWarningTimesValue;
	
	@NotNull
	private final ModConfigSpec.BooleanValue onEmptyRestartEnabled;
	
	@NotNull
	private final ModConfigSpec.ConfigValue<String> onEmptyRestartDelayValue;
	
	@NotNull
	private final ModConfigSpec.BooleanValue lowTpsRestartEnabled;
	
	@NotNull
	private final ModConfigSpec.DoubleValue lowTpsRestartMinimumTpsLevel;
	
	@NotNull
	private final ModConfigSpec.ConfigValue<String> lowTpsRestartDelayValue;
	
	@NotNull
	private final ArrayList<AutoRestartTime> autoRestartTimes = new ArrayList<>();
	
	@NotNull
	private final ArrayList<Timing> autoRestartWarningTimes = new ArrayList<>();
	
	private Timing onEmptyRestartDelay;
	
	private Timing lowTpsRestartDelay;
	
	public ServerConfig() {
		
		ModConfigSpec.Builder builder = new ModConfigSpec.Builder();
		
		builder.comment( "Options for restarting:" ).push( "restart" );
		usesExternalRestartScript = builder
			.comment( "Is the server started by an external restart script?" )
			.define( "use_external_restart_script", false );
		restartCommand = builder
			.comment( "Command that is executed on Server stopped to restart the server. Only called if \"use_external_restart_script\" is false." )
			.define( "restart_command", "" );
		builder.pop();
		
		builder.comment( "Option for auto restarting:" ).push( "auto_restart" );
		autoRestartEnabled = builder
			.comment( "Should the Server do automatic restarts?" )
			.define( "enabled", false );
		autoRestartTimesValue = builder
			.comment( "Times in 24-hour format on which the server will automatically restart" )
			.defineListAllowEmpty(
				"times",
				() -> new ArrayList<>( Arrays.asList(
					AutoRestartTime.build( 14, 0 ).toString(),
					AutoRestartTime.build( 16, 32 ).toString()
				) ),
				() -> "",
				object -> object instanceof String && AutoRestartTime.parse( (String) object ).isPresent()
			);
		autoRestartOnCrash = builder
			.comment( "Should the server be automatically restarted when it crashes." )
			.define( "on_crash", false );
		autoRestartWarningTimesValue = builder
			.comment(
				"Times before an auto restart of the server, a restart warning should be shown.",
				"Examples:",
				" - 5s - For a message 5 seconds before a restart",
				" - 7m - For a message 7 minutes before a restart",
				" - 2h - For a message 2 hours before a restart"
			)
			.defineListAllowEmpty(
				"warning_times",
				() -> new ArrayList<>( Arrays.asList(
					Timing.build( 5, TimeUnit.SECONDS ).toString(),
					Timing.build( 4, TimeUnit.SECONDS ).toString(),
					Timing.build( 3, TimeUnit.SECONDS ).toString(),
					Timing.build( 2, TimeUnit.SECONDS ).toString(),
					Timing.build( 1, TimeUnit.SECONDS ).toString()
				) ),
				() -> "",
				object -> object instanceof String && Timing.parse( (String) object ).isPresent()
			);
		builder.pop();
		
		builder.comment( "Options for restart, if the server is empty:" ).push( "on_empty_restart" );
		onEmptyRestartEnabled = builder
			.comment( "Should the server restart, if no players are online?" )
			.define( "enabled", false );
		onEmptyRestartDelayValue = builder
			.comment(
				"Delay after the server should restart, if it is empty.",
				"Examples:",
				" - 5s - For a delay 5 seconds",
				" - 7m - For a delay 7 minutes",
				" - 2h - For a delay 2 hours"
			)
			.define( "delay", Timing.build( 10, TimeUnit.MINUTES ).toString(),
				object -> object instanceof String && Timing.parse( (String) object ).isPresent()
			);
		builder.pop();
		
		builder.comment( "Options for restart, if the tps of server or its dimensions are low:" ).push( "low_tps_restart" );
		lowTpsRestartEnabled = builder
			.comment( "Should the server restart, if it is below a tps level for a specified time?" )
			.define( "enabled", false );
		lowTpsRestartMinimumTpsLevel = builder
			.comment( "TPS level below which the server is restarted, if it lasts for a specified time." )
			.defineInRange( "minium_tps_level", 0.0, 0.0, 20.0 );
		lowTpsRestartDelayValue = builder
			.comment(
				"Delay, that the server must be below the defined TPS level, in order for it to be restarted.",
				"Examples:",
				" - 5s - For a delay 5 seconds",
				" - 7m - For a delay 7 minutes",
				" - 2h - For a delay 2 hours"
			)
			.define( "delay", Timing.build( 1, TimeUnit.MINUTES ).toString(),
				object -> object instanceof String && Timing.parse( (String) object ).isPresent()
			);
		builder.pop();
		
		spec = builder.build();
	}
	
	private void loadAutoRestartTimes() {
		
		autoRestartTimes.clear();
		List<? extends String> autoRestartTimeStrings = autoRestartTimesValue.get();
		for( String autoRestartTimeString : autoRestartTimeStrings ) {
			Optional<AutoRestartTime> autoRestartTime = AutoRestartTime.parse( autoRestartTimeString );
			if( autoRestartTime.isPresent() ) {
				autoRestartTimes.add( autoRestartTime.get() );
			} else {
				log.warn(
					"{}: Removed invalid {} from auto restart times.",
					AutoRestart.MODID,
					autoRestartTimeString
				);
			}
		}
	}
	
	private void loadAutoRestartWarningTimes() {
		
		autoRestartWarningTimes.clear();
		List<? extends String> autoRestartWarningTimeStrings = autoRestartWarningTimesValue.get();
		for( String autoRestartWarningTimeString : autoRestartWarningTimeStrings ) {
			Optional<Timing> autoRestartWarningTime = Timing.parse( autoRestartWarningTimeString );
			if( autoRestartWarningTime.isPresent() ) {
				autoRestartWarningTimes.add( autoRestartWarningTime.get() );
			} else {
				log.warn(
					"{}: Removed invalid {} from auto restart warning times.",
					AutoRestart.MODID,
					autoRestartWarningTimeString
				);
			}
		}
	}
	
	private void loadOnEmptyRestartDelay() {
		
		onEmptyRestartDelay = Timing.parse( onEmptyRestartDelayValue.get() )
			.orElseThrow( () -> new IllegalStateException( String.format(
				"%s: Invalid on empty restart delay",
				AutoRestart.MODID
			) ) );
	}
	
	private void loadLowTpsRestartDelay() {
		
		lowTpsRestartDelay = Timing.parse( lowTpsRestartDelayValue.get() )
			.orElseThrow( () -> new IllegalStateException( String.format(
				"%s: Invalid low tps restart delay",
				AutoRestart.MODID
			) ) );
	}
	
	private void ensureLoaded() {
		
		if( autoRestartTimes.isEmpty() && autoRestartWarningTimes.isEmpty() &&
			onEmptyRestartDelay == null && lowTpsRestartDelay == null ) {
			loadAutoRestartTimes();
			loadAutoRestartWarningTimes();
			loadOnEmptyRestartDelay();
			loadLowTpsRestartDelay();
		}
	}
	
	public boolean usesExternalRestartScript() {
		
		return usesExternalRestartScript.get();
	}
	
	@NotNull
	public String getRestartCommand() {
		
		return restartCommand.get();
	}
	
	public boolean isAutoRestartEnabled() {
		
		ensureLoaded();
		if( getAutoRestartTimes().isEmpty() ) {
			autoRestartEnabled.set( false );
		}
		return autoRestartEnabled.get();
	}
	
	@NotNull
	public synchronized List<AutoRestartTime> getAutoRestartTimes() {
		
		ensureLoaded();
		return autoRestartTimes;
	}
	
	public boolean shouldAutoRestartOnCrash() {
		
		return autoRestartOnCrash.get();
	}
	
	@NotNull
	public synchronized List<Timing> getAutoRestartWarningTimes() {
		
		ensureLoaded();
		return autoRestartWarningTimes;
	}
	
	public boolean getOnEmptyRestartEnabled() {
		
		return onEmptyRestartEnabled.get();
	}
	
	@NotNull
	public Timing getOnEmptyRestartDelay() {
		
		ensureLoaded();
		return onEmptyRestartDelay;
	}
	
	public boolean isLowTpsRestartEnabled() {
		
		return lowTpsRestartEnabled.get();
	}
	
	public double getLowTpsRestartMinimumTpsLevel() {
		
		return lowTpsRestartMinimumTpsLevel.get();
	}
	
	@NotNull
	public Timing getLowTpsRestartDelay() {
		
		ensureLoaded();
		return lowTpsRestartDelay;
	}
}
