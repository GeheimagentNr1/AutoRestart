package de.geheimagentnr1.auto_restart.elements.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import de.geheimagentnr1.auto_restart.util.ServerRestarter;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.util.text.StringTextComponent;


public class RestartCommand {
	
	
	@SuppressWarnings( "SameReturnValue" )
	public static void register( CommandDispatcher<CommandSource> dispatcher ) {
		
		dispatcher.register( Commands.literal( "restart" ).requires( source -> source.hasPermissionLevel( 4 ) )
			.executes( context -> {
				CommandSource source = context.getSource();
				source.sendFeedback( new StringTextComponent( "Restarting the server" ), true );
				ServerRestarter.restart();
				return Command.SINGLE_SUCCESS;
			} ) );
	}
}
