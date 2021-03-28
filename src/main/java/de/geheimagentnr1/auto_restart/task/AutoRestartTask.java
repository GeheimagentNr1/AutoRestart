package de.geheimagentnr1.auto_restart.task;

import de.geheimagentnr1.auto_restart.config.AutoRestartTime;
import de.geheimagentnr1.auto_restart.config.ServerConfig;
import de.geheimagentnr1.auto_restart.config.Timing;
import de.geheimagentnr1.auto_restart.util.TpsHelper;
import de.geheimagentnr1.auto_restart.util.ServerRestarter;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Util;
import net.minecraft.util.text.ChatType;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.server.ServerWorld;
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
			long[] serverTickTimes = server.tickTimeArray;
			if( TpsHelper.calculateTps( serverTickTimes ) < ServerConfig.getLowTpsRestartMinimumTpsLevel() ) {
				tpsProblemDuration++;
				foundTpsProblem = true;
			}
			if( !foundTpsProblem ) {
				for( ServerWorld serverWorld : server.getWorlds() ) {
					long[] tickTimes = server.getTickTime( serverWorld.func_234923_W_() );
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
						server.getPlayerList().func_232641_a_(
							new StringTextComponent( String.format(
								"Restarting in %s...",
								warning_time.getDisplayString()
							) ).func_230530_a_( Style.field_240709_b_.func_240712_a_( TextFormatting.YELLOW ) ),
							ChatType.SYSTEM,
							Util.field_240973_b_
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
