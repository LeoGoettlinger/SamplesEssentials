package net.coreprotect.listener.player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.Map.Entry;
import net.coreprotect.config.Config;
import net.coreprotect.config.ConfigHandler;
import net.coreprotect.consumer.Queue;
import net.coreprotect.utility.EntityUtils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.AbstractArrow;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.CrossbowMeta;

public final class ProjectileLaunchListener extends Queue implements Listener {
   public static Set<Material> BOWS = new HashSet<>(Arrays.asList(Material.BOW, Material.CROSSBOW));

   public static void playerLaunchProjectile(Location location, String user, ItemStack itemStack, int amount, int delay, int offset, int action) {
      if (Config.getConfig(location.getWorld()).ITEM_DROPS && itemStack != null) {
         String loggingItemId = user.toLowerCase(Locale.ROOT) + "." + location.getBlockX() + "." + location.getBlockY() + "." + location.getBlockZ();
         int itemId = getItemId(loggingItemId);
         itemStack = itemStack.clone();
         if (amount > 0) {
            itemStack.setAmount(amount);
         }

         if (action == 7) {
            List<ItemStack> list = ConfigHandler.itemsShot.getOrDefault(loggingItemId, new ArrayList<>());
            list.add(itemStack);
            ConfigHandler.itemsShot.put(loggingItemId, list);
         } else {
            List<ItemStack> list = ConfigHandler.itemsThrown.getOrDefault(loggingItemId, new ArrayList<>());
            list.add(itemStack);
            ConfigHandler.itemsThrown.put(loggingItemId, list);
         }

         int time = (int)(System.currentTimeMillis() / 1000L) + delay;
         Queue.queueItemTransaction(user, location.clone(), time, offset, itemId);
      }
   }

   @EventHandler(
      priority = EventPriority.MONITOR,
      ignoreCancelled = true
   )
   protected void onProjectileLaunch(ProjectileLaunchEvent event) {
      Location location = event.getEntity().getLocation();
      String key = location.getWorld().getName() + "-" + location.getBlockX() + "-" + location.getBlockY() + "-" + location.getBlockZ();
      Iterator<Entry<String, Object[]>> it = ConfigHandler.entityBlockMapper.entrySet().iterator();

      while (it.hasNext()) {
         Entry<String, Object[]> pair = it.next();
         String name = pair.getKey();
         Object[] data = pair.getValue();
         ItemStack itemStack = (ItemStack)data[3];
         Material entityMaterial = EntityUtils.getEntityMaterial(event.getEntityType());
         boolean isBow = BOWS.contains(itemStack.getType());
         if ((data[1].equals(key) || data[2].equals(key))
            && (entityMaterial == itemStack.getType() || itemStack.getType() == Material.LINGERING_POTION && entityMaterial == Material.SPLASH_POTION || isBow)
            )
          {
            boolean thrownItem = itemStack.getType() != Material.FIREWORK_ROCKET && !isBow;
            if (isBow) {
               if (itemStack.getType() == Material.CROSSBOW) {
                  CrossbowMeta meta = (CrossbowMeta)itemStack.getItemMeta();
                  Iterator var13 = meta.getChargedProjectiles().iterator();
                  if (var13.hasNext()) {
                     ItemStack item = (ItemStack)var13.next();
                     itemStack = item;
                  }
               } else if (event.getEntity() instanceof AbstractArrow) {
                  itemStack = PlayerPickupArrowListener.getArrowType((AbstractArrow)event.getEntity());
               }

               if (itemStack == null || BOWS.contains(itemStack.getType())) {
                  return;
               }
            }

            playerLaunchProjectile(location, name, itemStack, 1, 1, 0, thrownItem ? 6 : 7);
            it.remove();
         }
      }
   }
}
