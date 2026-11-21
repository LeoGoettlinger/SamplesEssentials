package io.github.retrooper.packetevents.bukkit;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.PacketEventsAPI;
import com.github.retrooper.packetevents.netty.channel.ChannelHelper;
import com.github.retrooper.packetevents.protocol.player.User;
import com.github.retrooper.packetevents.util.FakeChannelUtil;
import io.github.retrooper.packetevents.injector.SpigotChannelInjector;
import io.github.retrooper.packetevents.manager.player.PlayerManagerImpl;
import io.github.retrooper.packetevents.util.folia.FoliaScheduler;
import java.lang.ref.WeakReference;
import java.util.Map;
import java.util.UUID;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerLoginEvent.Result;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.ApiStatus.Internal;
import org.jspecify.annotations.NullMarked;

@NullMarked
@Internal
public class InternalBukkitListener implements Listener {
   static final String KICK_MESSAGE = "PacketEvents failed to inject into a channel";
   private final Plugin plugin;

   public InternalBukkitListener(Plugin plugin) {
      this.plugin = plugin;
   }

   @EventHandler(
      priority = EventPriority.MONITOR
   )
   public void onLogin(PlayerLoginEvent event) {
      if (event.getResult() == Result.ALLOWED) {
         this.onPreJoin(event.getPlayer());
      }
   }

   @EventHandler(
      priority = EventPriority.LOWEST
   )
   public void onJoin(PlayerJoinEvent event) {
      this.onPostJoin(event.getPlayer());
   }

   void onPreJoin(Player player) {
      PacketEventsAPI<?> api = PacketEvents.getAPI();
      Map<UUID, WeakReference<Player>> map = ((PlayerManagerImpl)api.getPlayerManager()).joiningPlayers;
      map.put(player.getUniqueId(), new WeakReference<>(player));
   }

   void onPostJoin(Player player) {
      PacketEventsAPI<?> api = PacketEvents.getAPI();
      User user = api.getPlayerManager().getUser(player);
      if (user != null) {
         SpigotChannelInjector injector = (SpigotChannelInjector)PacketEvents.getAPI().getInjector();
         injector.setPlayer(user.getChannel(), player);
         ((PlayerManagerImpl)api.getPlayerManager()).joiningPlayers.remove(player.getUniqueId());
      } else {
         ((PlayerManagerImpl)api.getPlayerManager()).joiningPlayers.remove(player.getUniqueId());
         Object channel = api.getPlayerManager().getChannel(player);
         if ((channel == null || !FakeChannelUtil.isFakeChannel(channel)) && (!api.isTerminated() || api.getSettings().isKickIfTerminated())) {
            FoliaScheduler.getEntityScheduler().runDelayed(player, this.plugin, __ -> {
               if (channel != null ? ChannelHelper.isOpen(channel) : player.isOnline()) {
                  player.kickPlayer("PacketEvents failed to inject into a channel");
               }
            }, null, 0L);
         }
      }
   }
}
