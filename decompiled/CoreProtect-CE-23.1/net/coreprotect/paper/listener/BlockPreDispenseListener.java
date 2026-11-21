package net.coreprotect.paper.listener;

import io.papermc.paper.event.block.BlockPreDispenseEvent;
import java.util.concurrent.ConcurrentHashMap;
import net.coreprotect.config.Config;
import net.coreprotect.config.ConfigHandler;
import net.coreprotect.consumer.Queue;
import net.coreprotect.listener.player.InventoryChangeListener;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.Dispenser;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

public final class BlockPreDispenseListener extends Queue implements Listener {
   public static boolean useBlockPreDispenseEvent = true;
   public static boolean useForDroppers = false;
   private static final long CACHE_EXPIRY_TIME = 5000L;

   @EventHandler(
      priority = EventPriority.MONITOR,
      ignoreCancelled = true
   )
   public void onBlockPreDispense(BlockPreDispenseEvent event) {
      Block block = event.getBlock();
      World world = block.getWorld();
      if (Config.getConfig(world).BLOCK_PLACE) {
         BlockData blockData = block.getBlockData();
         if (blockData instanceof Dispenser) {
            if (!useForDroppers && block.getType() == Material.DROPPER) {
               useForDroppers = true;
            }

            ItemStack item = event.getItemStack();
            if (item == null) {
               return;
            }

            String locationKey = block.getWorld().getUID().toString() + "." + block.getX() + "." + block.getY() + "." + block.getZ();
            String eventKey = event.getSlot() + "." + item.getType().name() + ":" + item.getAmount();
            if (item.hasItemMeta()) {
               try {
                  eventKey = eventKey + ":" + item.getItemMeta().hashCode();
               } catch (Exception var14) {
               }
            }

            ConcurrentHashMap<String, Long> locationMap = ConfigHandler.dispenserNoChange.computeIfAbsent(locationKey, k -> new ConcurrentHashMap<>());
            Long lastNoChangeTime = locationMap.get(eventKey);
            long currentTime = System.currentTimeMillis();
            if (lastNoChangeTime != null && currentTime - lastNoChangeTime < 5000L) {
               locationMap.put(eventKey, currentTime);
               return;
            }

            ConfigHandler.dispenserNoChange.remove(locationKey);
            ConfigHandler.dispenserPending.put(locationKey, new Object[]{eventKey, currentTime, event.getSlot(), item.clone()});
            String user = "#dispenser";
            ItemStack[] inventory = ((InventoryHolder)block.getState()).getInventory().getStorageContents();
            InventoryChangeListener.inventoryTransaction(user, block.getLocation(), inventory);
         }
      }
   }
}
