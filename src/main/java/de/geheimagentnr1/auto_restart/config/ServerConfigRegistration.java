package de.geheimagentnr1.auto_restart.config;

import com.mojang.logging.LogUtils;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.config.IConfigSpec;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.loading.FMLPaths;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.io.IOException;


// NeoForge 26.3 renamed the config type SERVER to SYNCED (default file name "-synced"): the old file name keeps the
// existing config files, and an old world config is copied to the new world config folder once
public final class ServerConfigRegistration {
	
	
	@NotNull
	private static final Logger LOGGER = LogUtils.getLogger();
	
	private ServerConfigRegistration() {
	
	}
	
	public static void register( @NotNull ModContainer modContainer, @NotNull IConfigSpec spec ) {
		
		String fileName = modContainer.getModId() + "-server.toml";
		try {
			LegacyServerConfigMigration.migrate( FMLPaths.GAMEDIR.get(), fileName ).ifPresent( file ->
				LOGGER.info( "Copied the world config to {} (NeoForge 26.3 reads world configs from syncedconfig)", file )
			);
		} catch( IOException exception ) {
			LOGGER.error( "Could not copy the world config {} to syncedconfig", fileName, exception );
		}
		modContainer.registerConfig( ModConfig.Type.SYNCED, spec, fileName );
	}
}
