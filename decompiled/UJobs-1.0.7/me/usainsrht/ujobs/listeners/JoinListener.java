package me.usainsrht.ujobs.listeners;

import java.util.UUID;
import me.usainsrht.ujobs.UJobsPlugin;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class JoinListener implements Listener {
   UJobsPlugin plugin;

   public JoinListener(UJobsPlugin plugin) {
      this.plugin = plugin;
   }

   @EventHandler
   public void onJoin(PlayerJoinEvent e) {
      Player player = e.getPlayer();
      UUID uuid = player.getUniqueId();
      this.plugin.getStorage().load(uuid);
   }
}
