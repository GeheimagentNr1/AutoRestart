package de.geheimagentnr1.auto_restart.tasks;

import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.StringTextComponent;

import java.util.TimerTask;


public class ShutdownTask extends TimerTask {
	
	private final MinecraftServer server;
	
	private int count = 5;
	
	public ShutdownTask( MinecraftServer _server ) {
		
		server = _server;
	}
	
	/**
	 * The action to be performed by this timer task.
	 */
	@Override
	public void run() {
		
		if( count > 0 ) {
			server.getPlayerList().sendMessage( new StringTextComponent( "Restarting in " + count + "..." ), true );
		} else {
			if( count == 0 ) {
				server.initiateShutdown( false );
			}
		}
		count--;
	}
}
