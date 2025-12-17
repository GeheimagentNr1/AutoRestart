package de.geheimagentnr1.auto_restart.elements.gametests;

import de.geheimagentnr1.auto_restart.AutoRestart;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;


@GameTestHolder( AutoRestart.MODID )
@PrefixGameTestTemplate( false )
public class AutoRestartGameTests {
	
	
	@GameTest( template = "floor_3x3x3" )
	public void serverIsRunning( GameTestHelper helper ) {
		
		// Verify that the server is running and the mod is loaded
		if( helper.getLevel().getServer() != null ) {
			helper.succeed();
		}
	}
}
