package de.geheimagentnr1.auto_restart.task;

import de.geheimagentnr1.auto_restart.config.AutoRestartTime;
import de.geheimagentnr1.auto_restart.config.ServerConfig;
import de.geheimagentnr1.auto_restart.config.Timing;
import de.geheimagentnr1.auto_restart.util.ServerRestarter;
import de.geheimagentnr1.auto_restart.util.TpsHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.TimerTask;


public class AutoRestartTask extends TimerTask {
	
	
	private static final Logger LOGGER = LogManager.getLogger( AutoRestartTask.class );
	
	private final MinecraftServer server;
	
	private boolean isRestartRunning = false;
	
	private static LocalDateTime empty_time;
	
	private static long tpsProblemDuration = 0;
	
	public AutoRestartTask( MinecraftServer _server ) {
		
		server = _server;
	}
	
	/**
	 * The action to be performed by this timer task.
	 */
	@Override
	public void run() {
		
		if( isRestartRunning ) {
			return;
		}
		LocalDateTime current_time = LocalDateTime.now();
		if( ServerConfig.getOnEmptyRestartEnabled() && empty_time != null ) {
			if( Duration.between( empty_time, current_time ).getSeconds() >=
				ServerConfig.getOnEmptyRestartDelay().getSeconds() ) {
				LOGGER.info( "Auto restarting Server on empty server" );
				restart();
				return;
			}
		}
		if( ServerConfig.isLowTpsRestartEnabled() ) {
			boolean foundTpsProblem = false;
			long[] serverTickTimes = server.tickTimes;
			if( TpsHelper.calculateTps( serverTickTimes ) < ServerConfig.getLowTpsRestartMinimumTpsLevel() ) {
				tpsProblemDuration++;
				foundTpsProblem = true;
			}
			if( !foundTpsProblem ) {
				for( ServerLevel serverLevel : server.getAllLevels() ) {
					long[] tickTimes = server.getTickTime( serverLevel.dimension() );
					if( tickTimes != null ) {
						if( TpsHelper.calculateTps( tickTimes ) < ServerConfig.getLowTpsRestartMinimumTpsLevel() ) {
							tpsProblemDuration++;
							foundTpsProblem = true;
							break;
						}
					}
				}
			}
			if( foundTpsProblem ) {
				if( tpsProblemDuration >= ServerConfig.getLowTpsRestartDelay().getSeconds() ) {
					LOGGER.info( "Auto restarting Server on low tps" );
					restart();
					return;
				}
			} else {
				tpsProblemDuration = 0;
			}
		}
		if( ServerConfig.isAutoRestartEnabled() ) {
			for( AutoRestartTime autoRestartTime : ServerConfig.getAutoRestartTimes() ) {
				Duration difference = autoRestartTime.getDifferenceTo( current_time );
				for( Timing warning_time : ServerConfig.getAutoRestartWarningTimes() ) {
					if( difference.getSeconds() == warning_time.getSeconds() ) {
						server.getPlayerList().broadcastSystemMessage(
							Component.literal( String.format(
								"Restarting in %s...",
								warning_time.getDisplayString()
							) ).setStyle( Style.EMPTY.withColor( ChatFormatting.YELLOW ) ),
							ChatType.SYSTEM
						);
					}
				}
				if( autoRestartTime.getHour() == current_time.getHour() &&
					autoRestartTime.getMinute() == current_time.getMinute() ) {
					LOGGER.info( "Auto restarting Server on auto restarting time" );
					restart();
					return;
				}
			}
		}
	}
	
	private void restart() {
		
		ServerRestarter.restart( server );
		isRestartRunning = true;
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
