package de.geheimagentnr1.auto_restart.tasks;

import de.geheimagentnr1.auto_restart.config.MainConfig;
import de.geheimagentnr1.auto_restart.config.RestartTime;
import de.geheimagentnr1.auto_restart.util.ServerRestarter;
import net.minecraft.server.MinecraftServer;

import java.time.LocalDateTime;
import java.util.TimerTask;


public class AutoRestartTask extends TimerTask {
	
	
	private final MinecraftServer server;
	
	public AutoRestartTask( MinecraftServer _server ) {
		
		server = _server;
	}
	
	/**
	 * The action to be performed by this timer task.
	 */
	@Override
	public void run() {
		
		if( MainConfig.shouldAutoRestart() ) {
			LocalDateTime time = LocalDateTime.now();
			for( RestartTime restartTime : MainConfig.getRestartTimes() ) {
				if( restartTime.getHour() == time.getHour() && restartTime.getMinute() == time.getMinute() ) {
					ServerRestarter.restart( server, true );
					break;
				}
			}
		}
	}
}
