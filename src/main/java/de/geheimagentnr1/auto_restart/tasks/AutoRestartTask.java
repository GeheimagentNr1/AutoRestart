package de.geheimagentnr1.auto_restart.tasks;

import de.geheimagentnr1.auto_restart.config.Timing;
import de.geheimagentnr1.auto_restart.config.ServerConfig;
import de.geheimagentnr1.auto_restart.config.AutoRestartTime;
import de.geheimagentnr1.auto_restart.util.ServerRestarter;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextFormatting;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.TimerTask;


public class AutoRestartTask extends TimerTask {
	
	
	private static final Logger LOGGER = LogManager.getLogger();
	
	private final MinecraftServer server;
	
	private static LocalDateTime empty_time;
	
	public AutoRestartTask( MinecraftServer _server ) {
		
		server = _server;
	}
	
	/**
	 * The action to be performed by this timer task.
	 */
	@Override
	public void run() {
		
		if( ServerConfig.isAutoRestartEnabled() ) {
			LocalDateTime time = LocalDateTime.now();
			if( ServerConfig.isRestartOnEmptyEnabled() && empty_time != null ) {
				if( Duration.between( empty_time, time ).getSeconds() >=
					ServerConfig.getRestartOnEmptyDelay().getSeconds() ){
					LOGGER.info( "Auto restarting Server on empty server" );
					ServerRestarter.restart();
					return;
				}
			}
			for( AutoRestartTime autoRestartTime : ServerConfig.getAutoRestartTimes() ) {
				Duration difference = autoRestartTime.getDifferenceTo( time );
				for( Timing warning_time : ServerConfig.getAutoRestartWarningTimes() ) {
					if( difference.getSeconds() == warning_time.getSeconds() ) {
						server.getPlayerList().sendMessage(
							new StringTextComponent( String.format(
								"Restarting in %s...",
								warning_time.getDisplayString()
							) ).setStyle( new Style().setColor( TextFormatting.YELLOW ) ),
							true
						);
					}
				}
				if( autoRestartTime.getHour() == time.getHour() && autoRestartTime.getMinute() == time.getMinute() ) {
					LOGGER.info( "Auto restarting Server on auto restarting time" );
					ServerRestarter.restart();
					return;
				}
			}
		}
	}
	
	public static void resetEmptyTime() {
		
		empty_time = null;
		LOGGER.info( "Empty server timer stopped" );
	}
	
	public static void setEmptyTime() {
		
		empty_time = LocalDateTime.now();
		LOGGER.info( "Empty server timer started" );
	}
}
