package de.geheimagentnr1.auto_restart.util;

import de.geheimagentnr1.auto_restart.AutoRestart;
import de.geheimagentnr1.auto_restart.config.ModConfig;
import de.geheimagentnr1.auto_restart.tasks.ShutdownTask;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.StringTextComponent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Timer;


public class ServerRestarter {
	
	
	private static final Logger LOGGER = LogManager.getLogger();
	
	private volatile static boolean shouldDoRestart = false;
	
	public static void restart( MinecraftServer server, boolean auto ) {
		
		shouldDoRestart = true;
		createRestartFile();
		if( auto ) {
			server.getPlayerList().sendMessage( new StringTextComponent( ModConfig.getRestartMessage() ), true );
		} else {
			server.getPlayerList().sendMessage( new StringTextComponent( "The Server is getting restarted." ), true );
		}
		new Timer( true ).scheduleAtFixedRate( new ShutdownTask( server ), 0, 1000 );
	}
	
	public static void createExceptionFile() {
		
		saveToFile( StopType.EXCEPTION );
	}
	
	public static void createStopFile() {
		
		saveToFile( StopType.STOP );
	}
	
	private static void createRestartFile() {
		
		saveToFile( StopType.RESTART );
	}
	
	private static void saveToFile( StopType type ) {
		
		FileWriter fileWriter = null;
		try {
			File file = new File( "." + File.separator + AutoRestart.MODID + File.separator +
				"restart" );
			if( file.exists() || file.getParentFile().mkdirs() && file.createNewFile() ) {
				fileWriter = new FileWriter( file );
				fileWriter.write( String.valueOf( type.ordinal() - 1 ) );
				fileWriter.flush();
			} else {
				LOGGER.error( "Restart File could not be created" );
			}
		} catch( IOException exception ) {
			LOGGER.error( "FileWriter failed", exception );
		} finally {
			if( fileWriter != null ) {
				try {
					fileWriter.close();
				} catch( IOException exception ) {
					LOGGER.error( "FileWriter failed to close", exception );
				}
			}
		}
	}
	
	public static boolean shouldDoRestart() {
		
		return shouldDoRestart;
	}
}
