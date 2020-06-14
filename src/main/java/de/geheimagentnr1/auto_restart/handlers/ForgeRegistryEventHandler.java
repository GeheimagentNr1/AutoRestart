package de.geheimagentnr1.auto_restart.handlers;

import de.geheimagentnr1.auto_restart.config.ModConfig;
import de.geheimagentnr1.auto_restart.elements.commands.RestartCommand;
import de.geheimagentnr1.auto_restart.tasks.AutoRestartTask;
import de.geheimagentnr1.auto_restart.util.ServerRestarter;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.server.FMLServerStartedEvent;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;
import net.minecraftforge.fml.event.server.FMLServerStoppedEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Timer;


@SuppressWarnings( "unused" )
@Mod.EventBusSubscriber( bus = Mod.EventBusSubscriber.Bus.FORGE )
public class ForgeRegistryEventHandler {
	
	
	private static final Logger LOGGER = LogManager.getLogger();
	
	@SubscribeEvent
	public static void handlerServerStartEvent( FMLServerStartingEvent event ) {
		
		if( event.getServer().isDedicatedServer() ) {
			ServerRestarter.createExceptionFile();
			ModConfig.load();
			RestartCommand.register( event.getCommandDispatcher() );
		}
	}
	
	@SubscribeEvent
	public static void handleServerStarted( FMLServerStartedEvent event ) {
		
		if( event.getServer().isDedicatedServer() && ModConfig.shouldAutoRestart() ) {
			new Timer( true ).scheduleAtFixedRate( new AutoRestartTask( event.getServer() ), 0, 1000 * 60 );
		}
	}
	
	@SubscribeEvent
	public static void handleServerStopped( FMLServerStoppedEvent event ) {
		
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
