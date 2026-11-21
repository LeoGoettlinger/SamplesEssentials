package net.coreprotect.listener.player;

import java.util.List;
import net.coreprotect.config.Config;
import net.coreprotect.consumer.Queue;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;

public final class PlayerDeathListener extends Queue implements Listener {
   @EventHandler(
      priority = EventPriority.MONITOR,
      ignoreCancelled = true
   )
   protected void onPlayerDeath(PlayerDeathEvent event) {
      if (!event.getKeepInventory()) {
         Entity entity = event.getEntity();
         if (entity instanceof Player) {
            Location location = entity.getLocation();
            if (Config.getConfig(location.getWorld()).ITEM_DROPS) {
               String user = ((Player)entity).getName();
               List<ItemStack> items = event.getDrops();
               if (items != null && items.size() != 0) {
                  for (ItemStack itemStack : items) {
                     PlayerDropItemListener.playerDropItem(location, user, itemStack);
                  }
               }
            }
         }
      }
   }
}
