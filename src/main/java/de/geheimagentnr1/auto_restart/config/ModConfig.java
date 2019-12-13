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
	
	
	private final static Logger LOGGER = LogManager.getLogger();
	
	private final static String mod_name = "Auto Restart";
	
	private final static ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
	
	private final static ForgeConfigSpec CONFIG;
	
	private final static ForgeConfigSpec.BooleanValue AUTO_START;
	
	private final static ForgeConfigSpec.ConfigValue<List<String>> RESTART_TIMES;
	
	private final static ForgeConfigSpec.ConfigValue<String> RESTART_MESSAGE;
	
	private final static ForgeConfigSpec.BooleanValue USES_RESTART_SCRIPT;
	
	private final static ForgeConfigSpec.ConfigValue<String> RESTART_COMMAND;
	
	private final static ArrayList<RestartTime> restartTimes = new ArrayList<>();
	
	static {
		
		AUTO_START = BUILDER.comment( "Should the Server do Autorestarts?" ).define( "auto_restart", false );
		RESTART_TIMES = BUILDER.comment( "Times in 24 hour format on with the server will automaticaly restarts." )
			.define( "restart_times", Arrays.asList( "14:00", "16:32" ), o -> {
				@SuppressWarnings( "unchecked" )
				List<String> time_list = (List<String>)o;
				for( String time : time_list) {
					try {
						timeStringToRestartTime( time );
					} catch( Exception exception ) {
						LogManager.getLogger().error( "Invalid Time", exception );
						return false;
					}
				}
				return true;
			} );
		RESTART_MESSAGE = BUILDER.comment( "Message that ist shown on auto restart." )
			.define( "restart_message", "Server will auto restart." );
		USES_RESTART_SCRIPT = BUILDER.comment( "Is the server started by a restart script." )
			.define( "uses_restart_script", false );
		RESTART_COMMAND = BUILDER.comment( "Command that is execute on Server stopped to restart the server. " +
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
		LOGGER.info( "{} = {}", AUTO_START.getPath(), AUTO_START.get() );
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
			AUTO_START.set( false );
		}
		return AUTO_START.get();
	}
	
	public static ArrayList<RestartTime> getRestartTimes() {
		
		return restartTimes;
	}
	
	public static String getRestartMessage() {
		
		return RESTART_MESSAGE.get();
	}
	
	public static String getRestartCommand() {
		
		return RESTART_COMMAND.get();
	}
	
	public static boolean usesRestartScript() {
		
		return USES_RESTART_SCRIPT.get();
	}
}
