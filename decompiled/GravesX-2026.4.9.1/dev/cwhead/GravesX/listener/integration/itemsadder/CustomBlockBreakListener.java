package dev.cwhead.GravesX.listener.integration.itemsadder;

import com.ranull.graves.Graves;
import com.ranull.graves.type.Grave;
import dev.lone.itemsadder.api.Events.CustomBlockBreakEvent;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class CustomBlockBreakListener implements Listener {
   private final Graves plugin;

   public CustomBlockBreakListener(Graves plugin) {
      this.plugin = plugin;
   }

   @EventHandler(
      priority = EventPriority.HIGHEST,
      ignoreCancelled = true
   )
   public void onBlockBreakIA(CustomBlockBreakEvent event) {
      Player player = event.getPlayer();
      if (!this.plugin.getConfigManager().getConfigSection("grave.break", player).getBoolean("grave.break")) {
         Block block = event.getBlock();
         Grave grave = this.plugin.getBlockManager().getGraveFromBlock(block);
         String blockId = event.getNamespacedID();
         if (grave != null
            && this.plugin.getConfigManager().getConfigSection("itemsadder.block.enabled", player).getBoolean("itemsadder.block.enabled")
            && blockId.equals(this.plugin.getConfigManager().getConfigSection("itemsadder.block.name", player).getString("itemsadder.block.name"))) {
            event.setCancelled(true);
         }
      }
   }
}
