package dev.demora.demoralegendaryweapons.listeners;

import dev.demora.demoralegendaryweapons.DemoraLegendaryWeapons;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scheduler.BukkitRunnable;

public class LanguageSelectionListener implements Listener {
   private final DemoraLegendaryWeapons plugin;

   public LanguageSelectionListener(DemoraLegendaryWeapons plugin) {
      this.plugin = plugin;
   }

   @EventHandler
   public void onPlayerJoin(PlayerJoinEvent event) {
      final Player player = event.getPlayer();
      if (player.isOp()) {
         if (this.plugin.getLanguageManager().isLanguageNotSet()) {
            (new BukkitRunnable() {
               public void run() {
                  if (player.isOnline()) {
                     LanguageSelectionListener.this.plugin.getLanguageManager().addPlayerNeedingSelection(player.getUniqueId());
                     LanguageSelectionListener.this.plugin.getLanguageManager().showLanguageSelection(player);
                  }
               }
            }).runTaskLater(this.plugin, 40L);
         }
      }
   }
}
