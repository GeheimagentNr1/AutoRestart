package de.geheimagentnr1.auto_restart.config;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.Properties;


// NeoForge 26.3 loads the per-world config from <world>/syncedconfig instead of <world>/serverconfig. Before the server
// loads its configs, an old file of a dedicated server is copied once, so existing settings are kept (the old file stays).
// Not possible in singleplayer: the world is not known yet when the mod is loaded.
public final class LegacyServerConfigMigration {
	
	
	private LegacyServerConfigMigration() {
	
	}
	
	// Returns the copied file, or empty when there was nothing to copy
	@NotNull
	public static Optional<Path> migrate( @NotNull Path gameDir, @NotNull String fileName ) throws IOException {
		
		Path serverProperties = gameDir.resolve( "server.properties" );
		if( !Files.isRegularFile( serverProperties ) ) {
			return Optional.empty();
		}
		Properties properties = new Properties();
		try( Reader reader = Files.newBufferedReader( serverProperties ) ) {
			properties.load( reader );
		}
		Path world = gameDir.resolve( properties.getProperty( "level-name", "world" ) );
		Path source = world.resolve( "serverconfig" ).resolve( fileName );
		Path target = world.resolve( "syncedconfig" ).resolve( fileName );
		if( !Files.isRegularFile( source ) || Files.exists( target ) ) {
			return Optional.empty();
		}
		Files.createDirectories( target.getParent() );
		Files.copy( source, target );
		return Optional.of( target );
	}
}
