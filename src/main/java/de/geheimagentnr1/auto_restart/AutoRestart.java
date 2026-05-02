package de.geheimagentnr1.auto_restart;

import de.geheimagentnr1.auto_restart.config.ServerConfig;
import de.geheimagentnr1.auto_restart.elements.commands.RestartCommand;
import de.geheimagentnr1.auto_restart.task.AutoRestartTask;
import de.geheimagentnr1.auto_restart.util.ServerRestarter;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import org.jetbrains.annotations.NotNull;


@Mod( value = AutoRestart.MODID, dist = Dist.DEDICATED_SERVER )
public class AutoRestart {
	
	
	@NotNull
	public static final String MODID = "auto_restart";
	
	public AutoRestart( ModContainer modContainer ) {
		
		ServerConfig serverConfig = new ServerConfig();
		modContainer.registerConfig( ModConfig.Type.SERVER, serverConfig.getSpec() );

		ServerRestarter serverRestarter = new ServerRestarter( serverConfig );
		NeoForge.EVENT_BUS.register( serverRestarter );

		RestartCommand restartCommand = new RestartCommand( serverRestarter );
		NeoForge.EVENT_BUS.addListener( ( RegisterCommandsEvent event ) ->
			event.getDispatcher().register( restartCommand.build() )
		);

		AutoRestartTask autoRestartTask = new AutoRestartTask( serverConfig, serverRestarter );
		NeoForge.EVENT_BUS.register( autoRestartTask );
		autoRestartTask.schedule();
	}
}
