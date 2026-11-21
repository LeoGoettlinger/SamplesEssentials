package com.ranull.graves.listener;

import com.ranull.graves.Graves;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;

public class PlayerDeathListener implements Listener {
   private final Graves plugin;

   public PlayerDeathListener(Graves plugin) {
      this.plugin = plugin;
   }

   @EventHandler(
      priority = EventPriority.MONITOR,
      ignoreCancelled = true
   )
   public void onPlayerDeath(PlayerDeathEvent event) {
      List<ItemStack> drops = event.getDrops();
      List<ItemStack> toRemove = new ArrayList<>();

      for (ItemStack stack : drops) {
         if (this.isCompassToRemove(event, stack)) {
            toRemove.add(stack);
         }
      }

      drops.removeAll(toRemove);
      this.cacheRemainingItems(event, drops);
   }

   private boolean isCompassToRemove(PlayerDeathEvent event, ItemStack itemStack) {
      if (itemStack == null) {
         return false;
      } else {
         String typeName = itemStack.getType().name();
         boolean isAnyCompass = typeName.contains("COMPASS");
         return isAnyCompass
            && this.plugin.getEntityManager().getGraveUUIDFromItemStack(itemStack) != null
            && this.plugin.getConfigManager().getConfigSection("compass.destroy", event.getEntity()).getBoolean("compass.destroy");
      }
   }

   private void cacheRemainingItems(PlayerDeathEvent event, List<ItemStack> items) {
      this.plugin.getCacheManager().getRemovedItemStackMap().put(event.getEntity().getUniqueId(), new ArrayList<>(items));
   }
}
