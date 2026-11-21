package github.nighter.smartspawner.spawner.data.legacy;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionType;

public final class LegacyInventoryCodec {
   private LegacyInventoryCodec() {
   }

   public static List<String> parseJsonArray(String jsonData) {
      List<String> entries = new ArrayList<>();
      if (jsonData != null && !jsonData.isEmpty()) {
         if (jsonData.startsWith("[") && jsonData.endsWith("]")) {
            String content = jsonData.substring(1, jsonData.length() - 1);
            if (content.isEmpty()) {
               return entries;
            } else {
               StringBuilder current = new StringBuilder();
               boolean inQuotes = false;
               boolean escaped = false;

               for (char c : content.toCharArray()) {
                  if (escaped) {
                     current.append(c);
                     escaped = false;
                  } else if (c == '\\') {
                     escaped = true;
                  } else if (c == '"') {
                     inQuotes = !inQuotes;
                  } else if (c != ',' || inQuotes) {
                     current.append(c);
                  } else if (!current.isEmpty()) {
                     entries.add(current.toString());
                     current = new StringBuilder();
                  }
               }

               if (!current.isEmpty()) {
                  entries.add(current.toString());
               }

               return entries;
            }
         } else {
            return entries;
         }
      } else {
         return entries;
      }
   }

   public static Map<ItemStack, Long> deserialize(List<String> data) {
      Map<ItemStack, Long> result = new LinkedHashMap<>();
      if (data != null && !data.isEmpty()) {
         for (String entry : data) {
            if (entry != null && !entry.isEmpty()) {
               try {
                  if (entry.startsWith("TIPPED_ARROW#")) {
                     readTippedArrows(entry, result);
                  } else if (entry.contains(";")) {
                     readDamagedItems(entry, result);
                  } else {
                     readPlainItem(entry, result);
                  }
               } catch (IndexOutOfBoundsException | IllegalArgumentException var5) {
               }
            }
         }

         return result;
      } else {
         return result;
      }
   }

   private static void readTippedArrows(String entry, Map<ItemStack, Long> result) {
      String[] potionEntries = entry.substring("TIPPED_ARROW#".length()).split(",");

      for (String potionEntry : potionEntries) {
         String[] parts = potionEntry.split(":");
         if (parts.length >= 2) {
            long count = Long.parseLong(parts[1]);
            if (count > 0L) {
               ItemStack arrow = new ItemStack(Material.TIPPED_ARROW);
               if (arrow.getItemMeta() instanceof PotionMeta meta) {
                  PotionType potionType;
                  try {
                     potionType = PotionType.valueOf(parts[0]);
                  } catch (IllegalArgumentException var14) {
                     potionType = PotionType.WATER;
                  }

                  meta.setBasePotionType(potionType);
                  arrow.setItemMeta(meta);
               }

               result.merge(arrow, count, Long::sum);
            }
         }
      }
   }

   private static void readDamagedItems(String entry, Map<ItemStack, Long> result) {
      String[] parts = entry.split(";");
      if (parts.length >= 2) {
         Material material = Material.valueOf(parts[0]);

         for (String damageCount : parts[1].split(",")) {
            String[] pair = damageCount.split(":");
            if (pair.length >= 2) {
               int damage = Integer.parseInt(pair[0]);
               long count = Long.parseLong(pair[1]);
               if (count > 0L) {
                  ItemStack item = new ItemStack(material);
                  if (item.getItemMeta() instanceof Damageable damageable) {
                     damageable.setDamage(damage);
                     item.setItemMeta(damageable);
                  }

                  result.merge(item, count, Long::sum);
               }
            }
         }
      }
   }

   private static void readPlainItem(String entry, Map<ItemStack, Long> result) {
      String[] parts = entry.split(":");
      if (parts.length >= 2) {
         Material material = Material.valueOf(parts[0]);
         long count = Long.parseLong(parts[1]);
         if (count > 0L) {
            result.merge(new ItemStack(material), count, Long::sum);
         }
      }
   }
}
