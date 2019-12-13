package de.geheimagentnr1.auto_restart.handlers;

import de.geheimagentnr1.auto_restart.config.ModConfig;
import de.geheimagentnr1.auto_restart.elements.commands.RestartCommand;
import de.geheimagentnr1.auto_restart.util.ServerRestarter;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


@SuppressWarnings( "unused" )
@Mod.EventBusSubscriber( bus = Mod.EventBusSubscriber.Bus.FORGE )
public class ForgeRegistryEventHandler {
	
	
	private static final Logger LOGGER = LogManager.getLogger();
	
	@SubscribeEvent
	public static void handlerServerStartEvent( FMLServerStartingEvent event ) {
		
		if( event.getServer().isDedicatedServer() ) {
			ServerRestarter.createStopFile();
			ModConfig.load();
			RestartCommand.register( event.getCommandDispatcher() );
		}
	}
}
