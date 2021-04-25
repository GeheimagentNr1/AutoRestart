package de.geheimagentnr1.auto_restart.elements.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import de.geheimagentnr1.auto_restart.util.ServerRestarter;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.util.text.StringTextComponent;


@SuppressWarnings( "SameReturnValue" )
public class RestartCommand {
	
	
	public static void register( CommandDispatcher<CommandSource> dispatcher ) {
		
		dispatcher.register( Commands.literal( "restart" )
			.requires( source -> source.hasPermission( 4 ) )
			.executes( RestartCommand::restart ) );
	}
	
	private static int restart( CommandContext<CommandSource> context ) {
		
		CommandSource source = context.getSource();
		source.sendSuccess( new StringTextComponent( "Restarting the server" ), true );
		ServerRestarter.restart( source.getServer() );
		return Command.SINGLE_SUCCESS;
	}
}
