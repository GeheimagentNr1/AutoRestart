package de.geheimagentnr1.auto_restart.util;

import de.geheimagentnr1.auto_restart.AutoRestart;
import de.geheimagentnr1.auto_restart.config.ServerConfig;
import de.geheimagentnr1.minecraft_forge_api.events.ForgeEventHandlerInterface;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;


@Log4j2
@RequiredArgsConstructor
public class ServerRestarter implements ForgeEventHandlerInterface {
	
	
	@NotNull
	private final ServerConfig serverConfig;
	
	private boolean shouldDoRestart = false;
	
	public synchronized void restart( @NotNull MinecraftServer server ) {
		
		shouldDoRestart = true;
		createRestartFile();
		server.halt( false );
	}
	
	private void restartServer() {
		
		if( !serverConfig.usesExternalRestartScript() ) {
			String restartCommand = serverConfig.getRestartCommand();
			if( StringUtils.isBlank( restartCommand ) ) {
				log.info(
					"Restart failed, because of empty restart command. Check restart.restart_command in the config"
				);
			} else {
				log.info( "Restart Server" );
				ProcessBuilder builder = new ProcessBuilder( restartCommand );
				try {
					builder.start();
				} catch( IOException exception ) {
					log.error( "Restart failed for restart command \"{}\"", restartCommand, exception );
				}
			}
		}
	}
	
	private void createExceptionFile() {
		
		saveToFile( StopType.EXCEPTION );
	}
	
	private void createStopFile() {
		
		saveToFile( StopType.STOP );
	}
	
	private void createRestartFile() {
		
		saveToFile( StopType.RESTART );
	}
	
	private void saveToFile( StopType type ) {
		
		FileWriter fileWriter = null;
		try {
			log.info( "Saving restart status \"{}\" to file", type );
			File file = new File( "." + File.separator + AutoRestart.MODID + File.separator + "restart" );
			if( file.exists() || file.getParentFile().mkdirs() && file.createNewFile() ) {
				fileWriter = new FileWriter( file );
				fileWriter.write( String.valueOf( type.ordinal() - 1 ) );
				fileWriter.flush();
			} else {
				log.error( "Restart File could not be created" );
			}
		} catch( IOException exception ) {
			log.error( "FileWriter failed", exception );
		} finally {
			if( fileWriter != null ) {
				try {
					fileWriter.close();
				} catch( IOException exception ) {
					log.error( "FileWriter failed to close", exception );
				}
			}
		}
	}
	
	private synchronized boolean shouldDoRestart() {
		
		return shouldDoRestart;
	}
	
	@SubscribeEvent
	@Override
	public void handleServerStartingEvent( @NotNull ServerStartingEvent event ) {
		
		createExceptionFile();
	}
	
	@SubscribeEvent
	@Override
	public void handleServerStoppedEvent( @NotNull ServerStoppedEvent event ) {
		
		if( shouldDoRestart() ) {
			restartServer();
		} else {
			if( event.getServer().isRunning() ) {
				if( serverConfig.shouldAutoRestartOnCrash() ) {
					restartServer();
				}
			} else {
				createStopFile();
			}
		}
	}
}
