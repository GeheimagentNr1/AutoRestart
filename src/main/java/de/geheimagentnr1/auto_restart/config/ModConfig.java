package de.geheimagentnr1.auto_restart.config;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.electronwill.nightconfig.core.io.WritingMode;
import de.geheimagentnr1.auto_restart.AutoRestart;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.loading.FMLPaths;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class ModConfig {
	
	
	private static final Logger LOGGER = LogManager.getLogger();
	
	private static final String mod_name = "Auto Restart";
	
	private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
	
	private static final ForgeConfigSpec CONFIG;
	
	private static final ForgeConfigSpec.BooleanValue AUTO_RESTART;
	
	private static final ForgeConfigSpec.ConfigValue<List<String>> RESTART_TIMES;
	
	private static final ForgeConfigSpec.ConfigValue<String> RESTART_MESSAGE;
	
	private static final ForgeConfigSpec.BooleanValue USES_RESTART_SCRIPT;
	
	private static final ForgeConfigSpec.BooleanValue RESTART_ON_CRASH;
	
	private static final ForgeConfigSpec.ConfigValue<String> RESTART_COMMAND;
	
	private static final ArrayList<RestartTime> restartTimes = new ArrayList<>();
	
	static {
		
		AUTO_RESTART = BUILDER.comment( "Should the Server do automatic restarts?" ).define( "auto_restart", false );
		RESTART_TIMES = BUILDER.comment( "Times in 24 hour format on with the server will automaticaly restarts." )
			.define( "restart_times", Arrays.asList( "14:00", "16:32" ), o -> {
				if( o == null ) {
					return false;
				}
				@SuppressWarnings( "unchecked" )
				List<String> time_list = (List<String>)o;
				for( String time : time_list ) {
					try {
						timeStringToRestartTime( time );
					} catch( Exception exception ) {
						LogManager.getLogger().error( "Invalid Time", exception );
						return false;
					}
				}
				return true;
			} );
		RESTART_MESSAGE = BUILDER.comment( "Message that is shown on auto restart." )
			.define( "restart_message", "Server will auto restart." );
		USES_RESTART_SCRIPT = BUILDER.comment( "Is the server started by an external restart script?" )
			.define( "uses_restart_script", false );
		RESTART_ON_CRASH = BUILDER.comment( "Should the server be automatically restarted when it crashes." )
			.define( "restart_on_crash", false );
		RESTART_COMMAND = BUILDER.comment( "Command that is executed on Server stopped to restart the server. " +
			"Only called if " + USES_RESTART_SCRIPT.getPath() + " is false." ).define( "restart_command", "" );
		
		CONFIG = BUILDER.build();
	}
	
	public static void load() {
		
		CommentedFileConfig configData = CommentedFileConfig.builder( FMLPaths.CONFIGDIR.get().resolve(
			AutoRestart.MODID + ".toml" ) ).sync().autosave().writingMode( WritingMode.REPLACE ).build();
		
		LOGGER.info( "Loading \"{}\" Config", mod_name );
		configData.load();
		CONFIG.setConfig( configData );
		loadRestartTimes();
		LOGGER.info( "{} = {}", AUTO_RESTART.getPath(), AUTO_RESTART.get() );
		LOGGER.info( "{} = {}", RESTART_TIMES.getPath(), RESTART_TIMES.get() );
		LOGGER.info( "{} = {}", RESTART_MESSAGE.getPath(), RESTART_MESSAGE.get() );
		LOGGER.info( "{} = {}", USES_RESTART_SCRIPT.getPath(), USES_RESTART_SCRIPT.get() );
		LOGGER.info( "{} = {}", RESTART_COMMAND.getPath(), RESTART_COMMAND.get() );
		LOGGER.info( "\"{}\" Config loaded", mod_name );
	}
	
	private static void loadRestartTimes() {
		
		for( String restartTime : RESTART_TIMES.get() ) {
			restartTimes.add( timeStringToRestartTime( restartTime ) );
		}
	}
	
	private static RestartTime timeStringToRestartTime( String time ) {
		
		String[] timeElements = time.split( ":" );
		if( timeElements.length != 2 ) {
			throw new IllegalStateException( "Illegal time format" );
		}
		int hour;
		int min;
		try {
			hour = Integer.parseInt( timeElements[0] );
			min = Integer.parseInt( timeElements[1] );
		} catch( NumberFormatException exception ) {
			throw new IllegalStateException( "Illegal time format", exception );
		}
		if( hour < 0 || hour > 23 || min < 0 || min > 59 ) {
			throw new IllegalStateException( "Illegal time format" );
		}
		return new RestartTime( hour, min );
	}
	
	public static boolean shouldAutoRestart() {
		
		if( getRestartTimes().isEmpty() ) {
			AUTO_RESTART.set( false );
		}
		return AUTO_RESTART.get();
	}
	
	public static ArrayList<RestartTime> getRestartTimes() {
		
		return restartTimes;
	}
	
	public static String getRestartMessage() {
		
		return RESTART_MESSAGE.get();
	}
	
	public static boolean usesRestartScript() {
		
		return USES_RESTART_SCRIPT.get();
	}
	
	public static boolean shouldRestartOnCrash() {
		
		return RESTART_ON_CRASH.get();
	}
	
	public static String getRestartCommand() {
		
		return RESTART_COMMAND.get();
	}
}
