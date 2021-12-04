package de.geheimagentnr1.auto_restart;

import de.geheimagentnr1.auto_restart.config.ServerConfig;
import net.minecraftforge.fml.IExtensionPoint;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.network.NetworkConstants;


@SuppressWarnings( "UtilityClassWithPublicConstructor" )
@Mod( AutoRestart.MODID )
public class AutoRestart {
	
	
	public static final String MODID = "auto_restart";
	
	public AutoRestart() {
		
		ModLoadingContext.get().registerConfig( ModConfig.Type.SERVER, ServerConfig.CONFIG );
		ModLoadingContext.get().registerExtensionPoint(
			IExtensionPoint.DisplayTest.class,
			() -> new IExtensionPoint.DisplayTest(
				() -> NetworkConstants.IGNORESERVERONLY,
				( remote, isServer ) -> true
			)
		);
	}
}
