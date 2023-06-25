package de.geheimagentnr1.auto_restart;

import de.geheimagentnr1.auto_restart.config.ServerConfig;
import de.geheimagentnr1.auto_restart.elements.commands.ModCommandsRegisterFactory;
import de.geheimagentnr1.auto_restart.task.AutoRestartTask;
import de.geheimagentnr1.auto_restart.util.ServerRestarter;
import de.geheimagentnr1.minecraft_forge_api.AbstractMod;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.NotNull;


@Mod( AutoRestart.MODID )
public class AutoRestart extends AbstractMod {
	
	
	@NotNull
	public static final String MODID = "auto_restart";
	
	@NotNull
	@Override
	public String getModId() {
		
		return MODID;
	}
	
	@Override
	protected void initMod() {
		
		DistExecutor.safeRunWhenOn(
			Dist.DEDICATED_SERVER,
			() -> () -> {
				ServerConfig serverConfig = registerConfig( ServerConfig::new );
				ServerRestarter serverRestarter = registerEventHandler( new ServerRestarter( serverConfig ) );
				registerEventHandler( new ModCommandsRegisterFactory( serverRestarter ) );
				AutoRestartTask autoRestartTask = registerEventHandler( new AutoRestartTask(
					serverConfig,
					serverRestarter
				) );
				autoRestartTask.schedule();
			}
		);
		
	}
}
