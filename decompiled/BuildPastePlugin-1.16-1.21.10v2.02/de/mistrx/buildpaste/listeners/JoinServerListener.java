package de.mistrx.buildpaste.listeners;

import de.mistrx.buildpaste.util.Variables;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class JoinServerListener implements Listener {
   @EventHandler
   public static void onPlayerJoin(PlayerJoinEvent event) {
      Player player = event.getPlayer();
      Variables.CreatePlayerData(player);
      if (player.isOp()) {
         ;
      }
   }
}
