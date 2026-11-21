package me.usainsrht.ujobs.listeners.job_actions;

import me.usainsrht.ujobs.managers.JobManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;

public class AnvilMergeListener implements Listener {
   JobManager jobManager;

   public AnvilMergeListener(JobManager jobManager) {
      this.jobManager = jobManager;
   }

   @EventHandler(
      priority = EventPriority.MONITOR,
      ignoreCancelled = true
   )
   public void onAnvilMerge(InventoryClickEvent e) {
      if (e.getInventory().getType() == InventoryType.ANVIL) {
         if (e.getSlot() == 2) {
            if (e.getWhoClicked() instanceof Player player) {
               if (!this.jobManager.shouldIgnore(player)) {
                  ;
               }
            }
         }
      }
   }
}
