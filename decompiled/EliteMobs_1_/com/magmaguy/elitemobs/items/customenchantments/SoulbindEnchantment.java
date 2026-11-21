package com.magmaguy.elitemobs.items.customenchantments;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.config.enchantments.EnchantmentsConfig;
import com.magmaguy.elitemobs.config.enchantments.premade.SoulbindConfig;
import com.magmaguy.elitemobs.easyminecraftgoals.internal.FakeText;
import com.magmaguy.elitemobs.items.EliteItemLore;
import com.magmaguy.elitemobs.magmacore.util.ChatColorConverter;
import com.magmaguy.elitemobs.utils.VisualDisplay;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;

public final class SoulbindEnchantment {
   public static final NamespacedKey PRESTIGE_KEY = new NamespacedKey(MetadataHandler.PLUGIN, "prestige");
   public static String key = "soulbind";
   public static final NamespacedKey SOULBIND_KEY = new NamespacedKey(MetadataHandler.PLUGIN, key);
   public static boolean isEnabled;

   private SoulbindEnchantment() {
   }

   private static boolean soulbindEnabled() {
      isEnabled = EnchantmentsConfig.getEnchantment(key + ".yml").isEnabled();
      return isEnabled;
   }

   public static ItemStack addEnchantment(ItemStack itemStack, Player player) {
      if (!soulbindEnabled()) {
         return null;
      } else if (itemStack != null && player != null) {
         ItemMeta itemMeta = itemStack.getItemMeta();
         itemMeta.getPersistentDataContainer().set(SOULBIND_KEY, PersistentDataType.STRING, player.getUniqueId().toString());
         setPrestigeLevel(itemMeta, 0);
         itemStack.setItemMeta(itemMeta);
         new EliteItemLore(itemStack, true);
         return itemStack;
      } else {
         return null;
      }
   }

   public static void removeEnchantment(ItemStack itemStack) {
      if (itemStack != null) {
         ItemMeta itemMeta = itemStack.getItemMeta();
         itemMeta.getPersistentDataContainer().remove(SOULBIND_KEY);
         itemStack.setItemMeta(itemMeta);
         new EliteItemLore(itemStack, false);
      }
   }

   public static void addPhysicalDisplay(final Item item, final Player player) {
      if (player != null) {
         (new BukkitRunnable() {
               public void run() {
                  if (item != null && item.isValid()) {
                     final FakeText fakeText = VisualDisplay.generateFakeText(
                        item.getLocation().add(0.0, 0.5, 0.0),
                        ChatColorConverter.convert(
                           SoulbindConfig.hologramStrings.replace("$playerName", player.getName()).replace("$player", player.getDisplayName())
                        ),
                        30.0
                     );
                     if (fakeText != null) {
                        (new BukkitRunnable() {
                           Location lastLocation = item.getLocation();
                           int counter = 0;

                           public void run() {
                              this.counter++;
                              if (this.counter <= 6000 && item.isValid()) {
                                 Location currentLocation = item.getLocation();
                                 if (!this.lastLocation.equals(currentLocation)) {
                                    fakeText.teleport(currentLocation.clone().add(0.0, 0.5, 0.0));
                                    this.lastLocation = currentLocation;
                                 }
                              } else {
                                 this.cancel();
                                 fakeText.remove();
                              }
                           }
                        }).runTaskTimer(MetadataHandler.PLUGIN, 1L, 1L);
                     }
                  }
               }
            })
            .runTaskLater(MetadataHandler.PLUGIN, 60L);
      }
   }

   public static boolean isValidSoulbindUser(ItemMeta itemMeta, Player player) {
      if (!soulbindEnabled()) {
         return true;
      } else if (player.hasPermission("elitemobs.soulbind.bypass")) {
         return true;
      } else if (itemMeta == null) {
         return true;
      } else {
         return !itemMeta.getPersistentDataContainer().has(SOULBIND_KEY, PersistentDataType.STRING)
            ? true
            : ((String)itemMeta.getPersistentDataContainer().get(new NamespacedKey(MetadataHandler.PLUGIN, key), PersistentDataType.STRING))
               .equals(player.getUniqueId().toString());
      }
   }

   public static boolean isSoulboundItem(ItemMeta itemMeta) {
      if (!soulbindEnabled()) {
         return false;
      } else {
         return itemMeta == null ? false : !itemMeta.getPersistentDataContainer().has(SOULBIND_KEY, PersistentDataType.STRING);
      }
   }

   public static Player getSoulboundPlayer(ItemMeta itemMeta) {
      if (!itemMeta.getPersistentDataContainer().has(SOULBIND_KEY, PersistentDataType.STRING)) {
         return null;
      } else {
         try {
            UUID uuid = UUID.fromString(
               (String)itemMeta.getPersistentDataContainer().get(new NamespacedKey(MetadataHandler.PLUGIN, key), PersistentDataType.STRING)
            );
            return Bukkit.getPlayer(uuid);
         } catch (Exception var2) {
            return null;
         }
      }
   }

   public static boolean itemHasSoulbindEnchantment(ItemMeta itemMeta) {
      return itemMeta.getPersistentDataContainer().has(SOULBIND_KEY, PersistentDataType.STRING);
   }

   private static void setPrestigeLevel(ItemMeta itemMeta, int prestigeLevel) {
      itemMeta.getPersistentDataContainer().set(PRESTIGE_KEY, PersistentDataType.INTEGER, prestigeLevel);
   }

   public static int getPrestigeLevel(ItemMeta itemMeta) {
      if (!itemMeta.getPersistentDataContainer().has(SOULBIND_KEY, PersistentDataType.STRING)) {
         return 0;
      } else {
         int prestige = 0;
         if (itemMeta.getPersistentDataContainer().get(PRESTIGE_KEY, PersistentDataType.INTEGER) != null) {
            prestige = (Integer)itemMeta.getPersistentDataContainer().get(PRESTIGE_KEY, PersistentDataType.INTEGER);
         }

         return prestige;
      }
   }

   public static class SoulbindEnchantmentEvents implements Listener {
      @EventHandler(
         priority = EventPriority.LOWEST
      )
      public void onPickup(PlayerPickupItemEvent event) {
         if (!SoulbindEnchantment.isValidSoulbindUser(event.getItem().getItemStack().getItemMeta(), event.getPlayer())) {
            event.setCancelled(true);
         }
      }
   }
}
