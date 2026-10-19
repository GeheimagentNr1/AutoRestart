package de.geheimagentnr1.auto_restart.config;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;


class LegacyServerConfigMigrationTest {
	
	
	private static final String FILE = "mod-server.toml";
	
	@TempDir
	Path gameDir;
	
	private void write( Path file, String text ) throws IOException {
		
		Files.createDirectories( file.getParent() );
		Files.writeString( file, text );
	}
	
	@Test
	void copiesTheOldWorldConfigOnce() throws IOException {
		
		write( gameDir.resolve( "server.properties" ), "level-name=world\n" );
		write( gameDir.resolve( "world/serverconfig/" + FILE ), "old" );
		Optional<Path> copied = LegacyServerConfigMigration.migrate( gameDir, FILE );
		assertEquals( Optional.of( gameDir.resolve( "world/syncedconfig/" + FILE ) ), copied );
		assertEquals( "old", Files.readString( gameDir.resolve( "world/syncedconfig/" + FILE ) ) );
		assertTrue( Files.exists( gameDir.resolve( "world/serverconfig/" + FILE ) ) );
		assertEquals( Optional.empty(), LegacyServerConfigMigration.migrate( gameDir, FILE ) );
	}
	
	@Test
	void neverOverwritesASyncedConfig() throws IOException {
		
		write( gameDir.resolve( "server.properties" ), "level-name=world\n" );
		write( gameDir.resolve( "world/serverconfig/" + FILE ), "old" );
		write( gameDir.resolve( "world/syncedconfig/" + FILE ), "new" );
		assertEquals( Optional.empty(), LegacyServerConfigMigration.migrate( gameDir, FILE ) );
		assertEquals( "new", Files.readString( gameDir.resolve( "world/syncedconfig/" + FILE ) ) );
	}
	
	@Test
	void usesTheLevelNameOfTheServer() throws IOException {
		
		write( gameDir.resolve( "server.properties" ), "level-name=survival\n" );
		write( gameDir.resolve( "survival/serverconfig/" + FILE ), "old" );
		assertTrue( LegacyServerConfigMigration.migrate( gameDir, FILE ).isPresent() );
		assertTrue( Files.exists( gameDir.resolve( "survival/syncedconfig/" + FILE ) ) );
	}
	
	@Test
	void doesNothingWithoutServerProperties() throws IOException {
		
		write( gameDir.resolve( "world/serverconfig/" + FILE ), "old" );
		assertEquals( Optional.empty(), LegacyServerConfigMigration.migrate( gameDir, FILE ) );
		assertFalse( Files.exists( gameDir.resolve( "world/syncedconfig" ) ) );
	}
}
