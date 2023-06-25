package de.geheimagentnr1.auto_restart.elements.commands;

import de.geheimagentnr1.auto_restart.util.ServerRestarter;
import de.geheimagentnr1.minecraft_forge_api.elements.commands.CommandInterface;
import de.geheimagentnr1.minecraft_forge_api.elements.commands.CommandsRegisterFactory;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.util.List;


@RequiredArgsConstructor
public class ModCommandsRegisterFactory extends CommandsRegisterFactory {
	
	
	@NotNull
	private final ServerRestarter serverRestarter;
	
	@NotNull
	@Override
	public List<CommandInterface> commands() {
		
		return List.of(
			new RestartCommand( serverRestarter )
		);
	}
}
