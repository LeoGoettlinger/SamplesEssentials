package github.nighter.smartspawner.spawner.config;

import github.nighter.smartspawner.hooks.economy.ItemPriceManager;
import github.nighter.smartspawner.spawner.lootgen.loot.LootItem;
import java.util.logging.Logger;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;

final class LootEntryParser {
   private LootEntryParser() {
   }

   static LootItem parse(ConfigurationSection section, String key, ItemPriceManager priceManager, Logger logger, String context) {
      String configuredItem = section.getString("item");
      if (configuredItem != null && !configuredItem.isBlank()) {
         ItemStack template;
         try {
            template = ConfiguredItemParser.parse(configuredItem);
         } catch (IllegalArgumentException var17) {
            logger.warning("Skipping loot entry '" + key + "' for " + context + ": " + var17.getMessage());
            return null;
         }

         try {
            int[] amounts = parseRange(section.getString("amount", "1-1"));
            int minAmount = Math.max(0, amounts[0]);
            int maxAmount = Math.max(minAmount, amounts[1]);
            double chance = section.getDouble("chance", 100.0);
            if (chance < 0.0 || chance > 100.0) {
               logger.warning("Loot entry '" + key + "' for " + context + " has chance " + chance + ", which is outside 0 to 100. Using 100.");
               chance = 100.0;
            }

            Integer minDurability = null;
            Integer maxDurability = null;
            if (section.contains("durability")) {
               int[] durability = parseRange(section.getString("durability"));
               if (durability[0] == durability[1]) {
                  applyDamage(template, durability[0]);
               } else {
                  minDurability = Math.min(durability[0], durability[1]);
                  maxDurability = Math.max(durability[0], durability[1]);
               }
            }

            Material material = template.getType();
            double sellPrice = priceManager != null ? priceManager.getPrice(material) : 0.0;
            return new LootItem(template, material, minAmount, maxAmount, chance, minDurability, maxDurability, sellPrice);
         } catch (NumberFormatException var18) {
            logger.warning("Skipping loot entry '" + key + "' for " + context + ": amount and durability must be a number or a 'min-max' range");
            return null;
         }
      } else {
         logger.warning("Skipping loot entry '" + key + "' for " + context + ": it has no 'item:' line naming what should drop.");
         return null;
      }
   }

   private static int[] parseRange(String raw) {
      String value = raw == null ? "" : raw.trim();
      int separator = value.indexOf(45, 1);
      if (separator < 0) {
         int single = Integer.parseInt(value);
         return new int[]{single, single};
      } else {
         return new int[]{Integer.parseInt(value.substring(0, separator).trim()), Integer.parseInt(value.substring(separator + 1).trim())};
      }
   }

   private static void applyDamage(ItemStack item, int damage) {
      if (item.getItemMeta() instanceof Damageable damageable) {
         damageable.setDamage(damage);
         item.setItemMeta(damageable);
      }
   }
}
