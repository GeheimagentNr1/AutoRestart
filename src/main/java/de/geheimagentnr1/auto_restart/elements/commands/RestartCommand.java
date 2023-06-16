package de.geheimagentnr1.auto_restart.elements.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import de.geheimagentnr1.auto_restart.util.ServerRestarter;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;


@SuppressWarnings( "SameReturnValue" )
public class RestartCommand {
	
	
	public static void register( CommandDispatcher<CommandSourceStack> dispatcher ) {
		
		dispatcher.register( Commands.literal( "restart" )
			.requires( source -> source.hasPermission( 4 ) )
			.executes( RestartCommand::restart ) );
	}
	
	private static int restart( CommandContext<CommandSourceStack> context ) {
		
		CommandSourceStack source = context.getSource();
		source.sendSuccess( () -> Component.literal( "Restarting the server" ), true );
		ServerRestarter.restart( source.getServer() );
		return Command.SINGLE_SUCCESS;
	}
}
