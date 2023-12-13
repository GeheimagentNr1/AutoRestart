package de.geheimagentnr1.auto_restart.task;

import de.geheimagentnr1.auto_restart.config.AutoRestartTime;
import de.geheimagentnr1.auto_restart.config.ServerConfig;
import de.geheimagentnr1.auto_restart.config.Timing;
import de.geheimagentnr1.auto_restart.util.ServerRestarter;
import de.geheimagentnr1.auto_restart.util.TpsHelper;
import de.geheimagentnr1.minecraft_forge_api.events.ForgeEventHandlerInterface;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.server.ServerLifecycleHooks;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Timer;
import java.util.TimerTask;


@Log4j2
@RequiredArgsConstructor
public class AutoRestartTask extends TimerTask implements ForgeEventHandlerInterface {
	
	
	@NotNull
	private final ServerConfig serverConfig;
	
	@NotNull
	private final ServerRestarter serverRestarter;
	
	@Nullable
	private MinecraftServer server;
	
	private boolean isRestartRunning = false;
	
	@Nullable
	private LocalDateTime empty_time;
	
	private long tpsProblemDuration = 0;
	
	private boolean notScheduled = true;
	
	/**
	 * The action to be performed by this timer task.
	 */
	@Override
	public void run() {
		
		if( server == null && isRestartRunning ) {
			return;
		}
		LocalDateTime current_time = LocalDateTime.now();
		if( serverConfig.getOnEmptyRestartEnabled() && empty_time != null ) {
			if( Duration.between( empty_time, current_time ).getSeconds() >=
				serverConfig.getOnEmptyRestartDelay().getSeconds() ) {
				log.info( "Auto restarting Server on empty server" );
				restart();
				return;
			}
		}
		if( serverConfig.isLowTpsRestartEnabled() ) {
			boolean foundTpsProblem = false;
			long[] serverTickTimes = server.tickTimesNanos;
			if( TpsHelper.calculateTps( serverTickTimes ) < serverConfig.getLowTpsRestartMinimumTpsLevel() ) {
				tpsProblemDuration++;
				foundTpsProblem = true;
			}
			if( !foundTpsProblem ) {
				for( ServerLevel serverLevel : server.getAllLevels() ) {
					long[] tickTimes = server.getTickTime( serverLevel.dimension() );
					if( tickTimes != null ) {
						if( TpsHelper.calculateTps( tickTimes ) < serverConfig.getLowTpsRestartMinimumTpsLevel() ) {
							tpsProblemDuration++;
							foundTpsProblem = true;
							break;
						}
					}
				}
			}
			if( foundTpsProblem ) {
				if( tpsProblemDuration >= serverConfig.getLowTpsRestartDelay().getSeconds() ) {
					log.info( "Auto restarting Server on low tps" );
					restart();
					return;
				}
			} else {
				tpsProblemDuration = 0;
			}
		}
		if( serverConfig.isAutoRestartEnabled() ) {
			for( AutoRestartTime autoRestartTime : serverConfig.getAutoRestartTimes() ) {
				Duration difference = autoRestartTime.getDifferenceTo( current_time );
				for( Timing warning_time : serverConfig.getAutoRestartWarningTimes() ) {
					if( difference.getSeconds() == warning_time.getSeconds() ) {
						server.getPlayerList().broadcastSystemMessage(
							Component.literal( String.format(
								"Restarting in %s...",
								warning_time.getDisplayString()
							) ).setStyle( Style.EMPTY.withColor( ChatFormatting.YELLOW ) ),
							false
						);
					}
				}
				if( autoRestartTime.getHour() == current_time.getHour() &&
					autoRestartTime.getMinute() == current_time.getMinute() ) {
					log.info( "Auto restarting Server on auto restarting time" );
					restart();
					return;
				}
			}
		}
	}
	
	private void restart() {
		
		if( server == null ) {
			throw new IllegalStateException( "MinecraftServer is not initialize. Restart failed." );
		}
		serverRestarter.restart( server );
		isRestartRunning = true;
	}
	
	private void resetEmptyTime() {
		
		empty_time = null;
		log.info( "Empty server timer stopped" );
	}
	
	private void setEmptyTime() {
		
		empty_time = LocalDateTime.now();
		log.info( "Empty server timer started" );
	}
	
	@SubscribeEvent
	@Override
	public void handleServerStartedEvent( @NotNull ServerStartedEvent event ) {
		
		server = event.getServer();
	}
	
	@SubscribeEvent
	@Override
	public void handlePlayerLoggedInEvent( @NotNull PlayerEvent.PlayerLoggedInEvent event ) {
		
		resetEmptyTime();
	}
	
	@SubscribeEvent
	@Override
	public void handlePlayerLoggedOutEvent( @NotNull PlayerEvent.PlayerLoggedOutEvent event ) {
		
		if( ServerLifecycleHooks.getCurrentServer().getPlayerList().getPlayers().size() <= 1 ) {
			setEmptyTime();
		}
	}
	
	public void schedule() {
		
		if( notScheduled ) {
			new Timer( true ).scheduleAtFixedRate( this, 60 * 1000, 1000 );
			notScheduled = false;
		}
	}
}
