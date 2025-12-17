package de.geheimagentnr1.auto_restart.elements.gametests;

import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder( "auto_restart" )
@PrefixGameTestTemplate( false )
public class AutoRestartGameTests {

    @GameTest( template = "floor_3x3x3", templateNamespace = "neoforge" )
    public void modLoadsSuccessfully( GameTestHelper helper ) {

        // Simple test to verify the mod loads correctly
        // This test passes immediately since we just need to verify the mod is loaded
        helper.succeed();
    }
}
