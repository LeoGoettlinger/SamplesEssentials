package io.github.retrooper.packetevents.bukkit;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.PacketEventsAPI;
import com.github.retrooper.packetevents.event.UserLoginEvent;
import com.github.retrooper.packetevents.protocol.player.User;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class InternalGlobalBukkitListener implements Listener {
   @EventHandler
   public void onPlayerJoin(PlayerJoinEvent event) {
      PacketEventsAPI<?> api = PacketEvents.getAPI();
      User user = api.getPlayerManager().getUser(event.getPlayer());
      PacketEvents.getAPI().getEventManager().callEvent(new UserLoginEvent(user, event.getPlayer()));
   }
}
