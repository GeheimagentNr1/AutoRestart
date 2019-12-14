package de.geheimagentnr1.auto_restart;

import de.geheimagentnr1.auto_restart.config.ModConfig;
import de.geheimagentnr1.auto_restart.tasks.AutoRestartTask;
import de.geheimagentnr1.auto_restart.util.ServerRestarter;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.server.FMLServerStartedEvent;
import net.minecraftforge.fml.event.server.FMLServerStoppedEvent;

import java.util.Timer;


@Mod( AutoRestart.MODID )
public class AutoRestart {
	
	
	public final static String MODID = "auto_restart";
	
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
				ServerRestarter.restartServer();
			} else {
				if( event.getServer().isServerRunning() ) {
					if( ModConfig.shouldRestartOnCrash() ) {
						ServerRestarter.restartServer();
					}
				} else {
					ServerRestarter.createStopFile();
				}
			}
		}
	}
}
