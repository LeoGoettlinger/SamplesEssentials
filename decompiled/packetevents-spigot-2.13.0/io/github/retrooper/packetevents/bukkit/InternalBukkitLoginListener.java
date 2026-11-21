package io.github.retrooper.packetevents.bukkit;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.PacketEventsAPI;
import com.github.retrooper.packetevents.protocol.player.User;
import com.github.retrooper.packetevents.util.FakeChannelUtil;
import io.github.retrooper.packetevents.injector.SpigotChannelInjector;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerLoginEvent.Result;
import org.jetbrains.annotations.ApiStatus.Internal;
import org.jspecify.annotations.NullMarked;

@NullMarked
@Internal
public class InternalBukkitLoginListener implements Listener {
   @EventHandler(
      priority = EventPriority.LOWEST
   )
   public void onLogin(PlayerLoginEvent event) {
      PacketEventsAPI<?> api = PacketEvents.getAPI();
      User user = api.getPlayerManager().getUser(event.getPlayer());
      if (user != null) {
         SpigotChannelInjector injector = (SpigotChannelInjector)api.getInjector();
         injector.updatePlayer(user, event.getPlayer());
      } else {
         Object channel = api.getPlayerManager().getChannel(event.getPlayer());
         if ((channel == null || !FakeChannelUtil.isFakeChannel(channel)) && (!api.isTerminated() || api.getSettings().isKickIfTerminated())) {
            event.disallow(Result.KICK_OTHER, "PacketEvents failed to inject into a channel");
         }
      }
   }
}
