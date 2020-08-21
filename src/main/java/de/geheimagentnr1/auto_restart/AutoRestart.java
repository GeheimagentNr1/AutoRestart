package de.geheimagentnr1.auto_restart;

import de.geheimagentnr1.auto_restart.config.MainConfig;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;


@SuppressWarnings( "UtilityClassWithPublicConstructor" )
@Mod( AutoRestart.MODID )
public class AutoRestart {
	
	
	public static final String MODID = "auto_restart";
	
	public AutoRestart() {
		
		ModLoadingContext.get().registerConfig( ModConfig.Type.COMMON, MainConfig.CONFIG, MODID + ".toml" );
	}
}
