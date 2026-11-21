package io.github.retrooper.packetevents.bukkit;

import com.github.retrooper.packetevents.PacketEvents;
import io.github.retrooper.packetevents.injector.SpigotChannelInjector;
import io.github.retrooper.packetevents.injector.handlers.PacketEventsEncoder;
import io.github.retrooper.packetevents.util.SpigotReflectionUtil;
import io.netty.channel.Channel;
import io.papermc.paper.connection.PlayerConfigurationConnection;
import io.papermc.paper.event.connection.PlayerConnectionValidateLoginEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.ApiStatus.Internal;
import org.jspecify.annotations.NullMarked;

@NullMarked
@Internal
public class InternalPaperJoinListener implements Listener {
   private final InternalBukkitListener delegate;

   public InternalPaperJoinListener(Plugin plugin) {
      this.delegate = new InternalBukkitListener(plugin);
   }

   private void setChannelFreeze(Channel channel, boolean freeze) {
      channel.eventLoop().execute(() -> {
         try {
            SpigotChannelInjector injector = (SpigotChannelInjector)PacketEvents.getAPI().getInjector();
            PacketEventsEncoder encoder = injector.getEncoder(channel);
            if (encoder != null) {
               encoder.setHold(channel, freeze);
            }
         } catch (Exception var4) {
            throw new RuntimeException(var4);
         }
      });
   }

   @EventHandler(
      priority = EventPriority.MONITOR
   )
   public void onLogin(PlayerConnectionValidateLoginEvent event) {
      if (event.isAllowed()) {
         if (event.getConnection() instanceof PlayerConfigurationConnection) {
            Channel channel = (Channel)SpigotReflectionUtil.getChannelFromPaperConnection(event.getConnection());
            this.setChannelFreeze(channel, true);
         }
      }
   }

   @EventHandler(
      priority = EventPriority.LOWEST
   )
   public void onJoin(PlayerJoinEvent event) {
      this.delegate.onPostJoin(event.getPlayer());
      Channel channel = (Channel)SpigotReflectionUtil.getChannel(event.getPlayer());
      if (channel != null) {
         this.setChannelFreeze(channel, false);
      }
   }
}
