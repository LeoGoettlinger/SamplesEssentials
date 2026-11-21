package com.ranull.graves.listener;

import com.ranull.graves.Graves;
import com.ranull.graves.integration.MiniMessage;
import com.ranull.graves.type.Grave;
import com.ranull.graves.util.StringUtil;
import java.util.UUID;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

public class PlayerTeleportListener implements Listener {
   private final Graves plugin;

   public PlayerTeleportListener(Graves plugin) {
      this.plugin = plugin;
   }

   @EventHandler(
      priority = EventPriority.HIGHEST,
      ignoreCancelled = true
   )
   public void onPlayerTeleport(PlayerTeleportEvent event) {
      Player player = event.getPlayer();
      Location newLocation = event.getTo();
      if (newLocation != null) {
         if (this.plugin.getGraveManager().isNearGrave(newLocation, player)) {
            this.removeSpecificCompassNearGrave(player, newLocation);
         }
      }
   }

   private void removeSpecificCompassNearGrave(Player player, Location location) {
      PlayerInventory inventory = player.getInventory();
      ItemStack[] items = inventory.getContents();
      if (items != null && location.getWorld() != null) {
         Material recoveryCompass;
         try {
            recoveryCompass = Material.valueOf(String.valueOf(this.plugin.getVersionManager().getMaterialForVersion("RECOVERY_COMPASS")));
         } catch (IllegalArgumentException var17) {
            return;
         }

         for (ItemStack item : items) {
            if (item != null && item.hasItemMeta() && item.getType() == recoveryCompass) {
               ItemMeta itemMeta = item.getItemMeta();
               if (itemMeta != null && itemMeta.hasDisplayName()) {
                  UUID graveUUID = this.getGraveUUIDFromItemStack(item);
                  if (graveUUID != null) {
                     Grave grave = this.plugin.getCacheManager().getGrave(graveUUID);
                     if (grave != null) {
                        try {
                           Location graveLocation = this.plugin.getGraveManager().getGraveLocation(location, grave);
                           if (graveLocation != null && location.getWorld().equals(graveLocation.getWorld()) && location.distance(graveLocation) <= 15.0) {
                              String configured = this.plugin.getConfigManager().getConfigSection("compass.name", grave).getString("compass.name");
                              if (configured != null) {
                                 String compassName = StringUtil.parseString("&f" + configured, grave, this.plugin);
                                 if (this.plugin.getIntegrationManager().hasMiniMessage()) {
                                    compassName = MiniMessage.parseString(compassName);
                                 }

                                 if (itemMeta.getDisplayName().equals(compassName)) {
                                    inventory.remove(item);
                                 }
                              }
                           }
                        } catch (NullPointerException | IllegalArgumentException var16) {
                        }
                     }
                  }
               }
            }
         }
      }
   }

   private UUID getGraveUUIDFromItemStack(ItemStack itemStack) {
      if (!itemStack.hasItemMeta()) {
         return null;
      } else {
         ItemMeta meta = itemStack.getItemMeta();
         if (meta == null) {
            return null;
         } else {
            String uuidString = (String)meta.getPersistentDataContainer().get(new NamespacedKey(this.plugin, "graveUUID"), PersistentDataType.STRING);
            return uuidString != null ? UUID.fromString(uuidString) : null;
         }
      }
   }
}
