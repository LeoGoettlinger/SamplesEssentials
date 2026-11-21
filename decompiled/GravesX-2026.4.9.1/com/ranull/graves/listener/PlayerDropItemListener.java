package com.ranull.graves.listener;

import com.ranull.graves.Graves;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.inventory.ItemStack;

public class PlayerDropItemListener implements Listener {
   private final Graves plugin;

   public PlayerDropItemListener(Graves plugin) {
      this.plugin = plugin;
   }

   @EventHandler(
      priority = EventPriority.LOWEST,
      ignoreCancelled = true
   )
   public void onPlayerDropItem(PlayerDropItemEvent event) {
      ItemStack stack = event.getItemDrop().getItemStack();
      if (this.isGraveItem(stack)) {
         event.setCancelled(true);
         event.getItemDrop().remove();
      }
   }

   private boolean isGraveItem(ItemStack itemStack) {
      return itemStack != null && this.plugin.getEntityManager().getGraveUUIDFromItemStack(itemStack) != null;
   }
}
