package de.geheimagentnr1.auto_restart;

import de.geheimagentnr1.auto_restart.config.ModConfig;
import de.geheimagentnr1.auto_restart.tasks.AutoRestartTask;
import de.geheimagentnr1.auto_restart.util.ServerRestarter;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.server.FMLServerStartedEvent;
import net.minecraftforge.fml.event.server.FMLServerStoppedEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.Timer;


@Mod( AutoRestart.MODID )
public class AutoRestart {
	
	
	public final static String MODID = "auto_restart";
	
	private static final Logger LOGGER = LogManager.getLogger();
	
	public AutoRestart() {
		
		MinecraftForge.EVENT_BUS.addListener( this::handleServerStarted );
		MinecraftForge.EVENT_BUS.addListener( this::handleServerStopped );
	}
	
	private void handleServerStarted( FMLServerStartedEvent event ) {
		
		if( event.getServer().isDedicatedServer() && ModConfig.shouldAutoRestart() ) {
			new Timer( true ).scheduleAtFixedRate( new AutoRestartTask( event.getServer() ), 0, 1000 * 60 );
		}
	}
	
	private void handleServerStopped( FMLServerStoppedEvent event ) {
		
		if( event.getServer().isDedicatedServer() ) {
			if( ServerRestarter.shouldDoRestart() ) {
				if( !ModConfig.usesRestartScript() ) {
					LOGGER.info( "Restart Server" );
					ProcessBuilder builder = new ProcessBuilder( ModConfig.getRestartCommand() );
					try {
						builder.start();
					} catch( IOException exception ) {
						LOGGER.error( "Auto Restart could not be done.", exception );
					}
				}
			} else {
				if( !event.getServer().isServerRunning() ) {
					ServerRestarter.createStopFile();
				}
			}
		}
	}
}
