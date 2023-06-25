package de.geheimagentnr1.auto_restart.elements.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import de.geheimagentnr1.auto_restart.util.ServerRestarter;
import de.geheimagentnr1.minecraft_forge_api.elements.commands.CommandInterface;
import lombok.RequiredArgsConstructor;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;


@SuppressWarnings( "SameReturnValue" )
@RequiredArgsConstructor
public class RestartCommand implements CommandInterface {
	
	
	@NotNull
	private final ServerRestarter serverRestarter;
	
	@NotNull
	@Override
	public LiteralArgumentBuilder<CommandSourceStack> build() {
		
		return Commands.literal( "restart" )
			.requires( source -> source.hasPermission( 4 ) )
			.executes( this::restart );
	}
	
	private int restart( @NotNull CommandContext<CommandSourceStack> context ) {
		
		CommandSourceStack source = context.getSource();
		source.sendSuccess( () -> Component.literal( "Restarting the server" ), true );
		serverRestarter.restart( source.getServer() );
		return Command.SINGLE_SUCCESS;
	}
}
