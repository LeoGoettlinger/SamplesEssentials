package com.blaxk.spawnelytra.bedrock;

import com.blaxk.spawnelytra.Main;
import com.blaxk.spawnelytra.util.SchedulerUtil;
import io.papermc.paper.event.player.PlayerTrackEntityEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class TempElytraTrackListener implements Listener {
   private final Main plugin;
   private final TempElytraManager manager;

   public TempElytraTrackListener(Main plugin, TempElytraManager manager) {
      this.plugin = plugin;
      this.manager = manager;
   }

   @EventHandler(
      ignoreCancelled = true
   )
   public void onTrack(PlayerTrackEntityEvent event) {
      if (event.getEntity() instanceof Player wearer && this.manager.hasDisguise(wearer)) {
         Player viewer = event.getPlayer();
         SchedulerUtil.runAtEntityLater(this.plugin, viewer, 2L, () -> {
            if (viewer.isOnline() && wearer.isOnline()) {
               this.manager.sendDisguise(wearer, viewer);
            }
         });
      }
   }
}
