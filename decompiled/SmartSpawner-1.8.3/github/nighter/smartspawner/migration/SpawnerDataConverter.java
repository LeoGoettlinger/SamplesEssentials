package github.nighter.smartspawner.migration;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import github.nighter.smartspawner.SmartSpawner;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.Map.Entry;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class SpawnerDataConverter {
   private final SmartSpawner plugin;
   private final FileConfiguration oldConfig;
   private final FileConfiguration newConfig;
   private static final Gson gson = new Gson();

   public SpawnerDataConverter(SmartSpawner plugin, FileConfiguration oldConfig, FileConfiguration newConfig) {
      this.plugin = plugin;
      this.oldConfig = oldConfig;
      this.newConfig = newConfig;
   }

   public void convertData() {
      ConfigurationSection spawnersSection = this.oldConfig.getConfigurationSection("spawners");
      if (spawnersSection != null) {
         for (String spawnerId : spawnersSection.getKeys(false)) {
            try {
               this.convertSpawner(spawnerId);
            } catch (Exception var5) {
               this.plugin.getLogger().severe("Failed to convert spawner " + spawnerId);
               var5.printStackTrace();
            }
         }
      }
   }

   private void convertSpawner(String spawnerId) {
      String oldPath = "spawners." + spawnerId;

      try {
         String worldName = this.oldConfig.getString(oldPath + ".world");
         int x = this.oldConfig.getInt(oldPath + ".x");
         int y = this.oldConfig.getInt(oldPath + ".y");
         int z = this.oldConfig.getInt(oldPath + ".z");
         String settings = String.format(
            "%d,%b,%d,%b,%d,%d,%d,%d,%d,%d,%d,%b",
            this.oldConfig.getInt(oldPath + ".spawnerExp"),
            this.oldConfig.getBoolean(oldPath + ".spawnerActive"),
            this.oldConfig.getInt(oldPath + ".spawnerRange"),
            this.oldConfig.getBoolean(oldPath + ".spawnerStop"),
            this.oldConfig.getInt(oldPath + ".spawnDelay"),
            this.oldConfig.getInt(oldPath + ".maxSpawnerLootSlots"),
            this.oldConfig.getInt(oldPath + ".maxStoredExp"),
            this.oldConfig.getInt(oldPath + ".minMobs"),
            this.oldConfig.getInt(oldPath + ".maxMobs"),
            this.oldConfig.getInt(oldPath + ".stackSize"),
            this.oldConfig.getLong(oldPath + ".lastSpawnTime"),
            this.oldConfig.getBoolean(oldPath + ".allowEquipmentItems")
         );
         List<String> newInventoryFormat = new ArrayList<>();
         ConfigurationSection invSection = this.oldConfig.getConfigurationSection(oldPath + ".virtualInventory");
         if (invSection != null) {
            List<String> serializedItems = invSection.getStringList("items");
            Map<String, Map<Integer, Integer>> durabilityItems = new HashMap<>();
            Map<String, Integer> regularItems = new HashMap<>();

            for (String serialized : serializedItems) {
               try {
                  String[] parts = serialized.split(":", 2);
                  if (parts.length == 2) {
                     ItemStack item = itemStackFromJson(parts[1]);
                     if (item != null) {
                        if (item.getType() == Material.TIPPED_ARROW) {
                           ItemMeta meta = item.getItemMeta();
                           if (meta instanceof PotionMeta && ((PotionMeta)meta).hasCustomEffects()) {
                              PotionEffect effect = (PotionEffect)((PotionMeta)meta).getCustomEffects().get(0);
                              String itemKey = String.format("TIPPED_ARROW#%s;%d;%d", effect.getType().getName(), effect.getDuration(), effect.getAmplifier());
                              regularItems.merge(itemKey, item.getAmount(), Integer::sum);
                           } else {
                              regularItems.merge("ARROW", item.getAmount(), Integer::sum);
                           }
                        } else if (this.isDestructibleItem(item.getType())) {
                           String itemType = item.getType().name();
                           durabilityItems.computeIfAbsent(itemType, k -> new TreeMap<>())
                              .merge(Integer.valueOf(item.getDurability()), item.getAmount(), Integer::sum);
                        } else {
                           regularItems.merge(item.getType().name(), item.getAmount(), Integer::sum);
                        }
                     }
                  }
               } catch (Exception var20) {
                  this.plugin.getLogger().warning("Failed to convert item in spawner " + spawnerId + ": " + var20.getMessage());
               }
            }

            for (Entry<String, Integer> entry : regularItems.entrySet()) {
               newInventoryFormat.add(entry.getKey() + ":" + entry.getValue());
            }

            for (Entry<String, Map<Integer, Integer>> itemEntry : durabilityItems.entrySet()) {
               StringBuilder itemString = new StringBuilder(itemEntry.getKey());
               if (!itemEntry.getValue().isEmpty()) {
                  itemString.append(";");
                  boolean first = true;

                  for (Entry<Integer, Integer> durabilityEntry : itemEntry.getValue().entrySet()) {
                     if (!first) {
                        itemString.append(",");
                     }

                     itemString.append(durabilityEntry.getKey()).append(":").append(durabilityEntry.getValue());
                     first = false;
                  }
               }

               newInventoryFormat.add(itemString.toString());
            }
         }

         String spawnerPath = "spawners." + spawnerId;
         this.newConfig.set(spawnerPath + ".location", String.format("%s,%d,%d,%d", worldName, x, y, z));
         this.newConfig.set(spawnerPath + ".entityType", this.oldConfig.getString(oldPath + ".entityType"));
         this.newConfig.set(spawnerPath + ".settings", settings);
         this.newConfig.set(spawnerPath + ".inventory", newInventoryFormat);
      } catch (Exception var21) {
         this.plugin.getLogger().severe("Failed to convert spawner " + spawnerId + ": " + var21.getMessage());
         throw var21;
      }
   }

   private boolean isDestructibleItem(Material material) {
      String name = material.name();
      return name.endsWith("_SWORD")
         || name.endsWith("_PICKAXE")
         || name.endsWith("_AXE")
         || name.endsWith("_SPADE")
         || name.endsWith("_HOE")
         || name.equals("BOW")
         || name.equals("FISHING_ROD")
         || name.equals("FLINT_AND_STEEL")
         || name.equals("SHEARS")
         || name.equals("SHIELD")
         || name.equals("ELYTRA")
         || name.equals("TRIDENT")
         || name.equals("CROSSBOW")
         || name.startsWith("LEATHER_")
         || name.startsWith("CHAINMAIL_")
         || name.startsWith("IRON_")
         || name.startsWith("GOLDEN_")
         || name.startsWith("DIAMOND_")
         || name.startsWith("NETHERITE_");
   }

   public static ItemStack itemStackFromJson(String data) {
      JsonObject json = (JsonObject)gson.fromJson(data, JsonObject.class);
      ItemStack item = new ItemStack(Material.valueOf(json.get("type").getAsString()), json.get("amount").getAsInt(), (short)json.get("durability").getAsInt());
      ItemMeta meta = item.getItemMeta();
      if (meta != null) {
         if (json.has("displayName")) {
            meta.setDisplayName(json.get("displayName").getAsString());
         }

         if (json.has("lore")) {
            List<String> lore = new ArrayList<>();

            for (JsonElement element : json.getAsJsonArray("lore")) {
               lore.add(element.getAsString());
            }

            meta.setLore(lore);
         }

         if (json.has("enchantments")) {
            JsonObject enchants = json.getAsJsonObject("enchantments");

            for (Entry<String, JsonElement> entry : enchants.entrySet()) {
               Enchantment enchantment = Enchantment.getByName(entry.getKey());
               if (enchantment != null) {
                  meta.addEnchant(enchantment, entry.getValue().getAsInt(), true);
               }
            }
         }

         if (meta instanceof PotionMeta && json.has("potionData")) {
            PotionMeta potionMeta = (PotionMeta)meta;
            JsonObject potionData = json.getAsJsonObject("potionData");
            if (potionData.has("customEffects")) {
               for (JsonElement element : potionData.getAsJsonArray("customEffects")) {
                  JsonObject effectObj = element.getAsJsonObject();
                  PotionEffectType type = PotionEffectType.getByName(effectObj.get("type").getAsString());
                  if (type != null) {
                     PotionEffect effect = new PotionEffect(
                        type,
                        effectObj.get("duration").getAsInt(),
                        effectObj.get("amplifier").getAsInt(),
                        effectObj.get("ambient").getAsBoolean(),
                        effectObj.get("particles").getAsBoolean(),
                        effectObj.get("icon").getAsBoolean()
                     );
                     potionMeta.addCustomEffect(effect, true);
                  }
               }
            }
         }

         item.setItemMeta(meta);
      }

      return item;
   }
}
