package de.geheimagentnr1.auto_restart.handlers;

import de.geheimagentnr1.auto_restart.AutoRestart;
import de.geheimagentnr1.auto_restart.config.ServerConfig;
import de.geheimagentnr1.auto_restart.elements.commands.RestartCommand;
import de.geheimagentnr1.auto_restart.task.AutoRestartTask;
import de.geheimagentnr1.auto_restart.util.ServerRestarter;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.server.ServerLifecycleHooks;

import java.util.Timer;


@Mod.EventBusSubscriber(
	modid = AutoRestart.MODID,
	bus = Mod.EventBusSubscriber.Bus.FORGE,
	value = Dist.DEDICATED_SERVER
)
public class ForgeEventHandler {
	
	
	@SubscribeEvent
	public static void handlerServerStartingEvent( ServerStartingEvent event ) {
		
		ServerRestarter.createExceptionFile();
	}
	
	@SubscribeEvent
	public static void handlerRegisterCommandsEvent( RegisterCommandsEvent event ) {
		
		RestartCommand.register( event.getDispatcher() );
	}
	
	@SubscribeEvent
	public static void handleServerStartedEvent( ServerStartedEvent event ) {
		
		new Timer( true ).scheduleAtFixedRate( new AutoRestartTask( event.getServer() ), 60 * 1000, 1000 );
	}
	
	@SubscribeEvent
	public static void handlePlayerLoggedInEvent( PlayerEvent.PlayerLoggedInEvent event ) {
		
		AutoRestartTask.resetEmptyTime();
	}
	
	@SubscribeEvent
	public static void handlePlayerLoggedOutEvent( PlayerEvent.PlayerLoggedOutEvent event ) {
		
		if( ServerLifecycleHooks.getCurrentServer().getPlayerList().getPlayers().size() <= 1 ) {
			AutoRestartTask.setEmptyTime();
		}
	}
	
	@SubscribeEvent
	public static void handleServerStoppedEvent( ServerStoppedEvent event ) {
		
		if( ServerRestarter.shouldDoRestart() ) {
			ServerRestarter.restartServer();
		} else {
			if( event.getServer().isRunning() ) {
				if( ServerConfig.shouldAutoRestartOnCrash() ) {
					ServerRestarter.restartServer();
				}
			} else {
				ServerRestarter.createStopFile();
			}
		}
	}
}
