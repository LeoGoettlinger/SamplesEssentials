package github.nighter.smartspawner.commands.editloot;

import github.nighter.smartspawner.SmartSpawner;
import github.nighter.smartspawner.spawner.config.ConfiguredItemParser;
import github.nighter.smartspawner.spawner.config.SpawnerNames;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import lombok.Generated;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;

public class LootEditorService {
   private final SmartSpawner plugin;
   private final Map<LootEditorTarget, YamlConfiguration> snapshots = new EnumMap<>(LootEditorTarget.class);

   public LootEditorService(SmartSpawner plugin) {
      this.plugin = plugin;
      this.reload();
   }

   private File fileOf(LootEditorTarget target) {
      return new File(this.plugin.getDataFolder(), target.getFileName());
   }

   public synchronized void reload() {
      for (LootEditorTarget target : LootEditorTarget.values()) {
         this.snapshots.put(target, YamlConfiguration.loadConfiguration(this.fileOf(target)));
      }
   }

   private synchronized YamlConfiguration snapshot(LootEditorTarget target) {
      return this.snapshots.get(target);
   }

   public synchronized LootEditorService.EntryRef findEntry(String name) {
      if (name != null && !name.isBlank()) {
         SpawnerNames.Resolved resolved = SpawnerNames.resolve(this.plugin, name.trim());
         if (resolved == null) {
            return null;
         } else if (resolved.mob() != null) {
            String path = this.plugin.getSpawnerSettingsConfig().getSectionPath(resolved.mob().name());
            return path == null ? null : new LootEditorService.EntryRef(LootEditorTarget.SMART_SPAWNER, path);
         } else {
            String path = this.plugin.getItemSpawnerSettingsConfig().getSectionPath(resolved.item().name());
            return path == null ? null : new LootEditorService.EntryRef(LootEditorTarget.ITEM_SPAWNER, path);
         }
      } else {
         return null;
      }
   }

   public synchronized List<String> listAllEntryNames() {
      return new ArrayList<>(SpawnerNames.suggestionNames(this.plugin));
   }

   public synchronized boolean hasEntry(LootEditorTarget target, String key) {
      return this.snapshot(target).isConfigurationSection(key);
   }

   public synchronized List<String> listLootKeys(LootEditorTarget target, String key) {
      ConfigurationSection loot = this.snapshot(target).getConfigurationSection(key + ".loot");
      return (List<String>)(loot == null ? List.of() : new ArrayList<>(loot.getKeys(false)));
   }

   public synchronized LootEditorService.LootView readLoot(LootEditorTarget target, String key, String lootKey) {
      ConfigurationSection section = this.snapshot(target).getConfigurationSection(key + ".loot." + lootKey);
      return this.readLootSection(lootKey, section);
   }

   public synchronized List<LootEditorService.LootView> readLootList(LootEditorTarget target, String key) {
      ConfigurationSection loot = this.snapshot(target).getConfigurationSection(key + ".loot");
      if (loot == null) {
         return List.of();
      } else {
         List<LootEditorService.LootView> result = new ArrayList<>();

         for (String lootKey : loot.getKeys(false)) {
            LootEditorService.LootView view = this.readLootSection(lootKey, loot.getConfigurationSection(lootKey));
            if (view != null) {
               result.add(view);
            }
         }

         return result;
      }
   }

   private LootEditorService.LootView readLootSection(String lootKey, ConfigurationSection section) {
      if (section == null) {
         return null;
      } else {
         String rawItem = section.getString("item");

         ItemStack preview;
         try {
            preview = rawItem == null ? null : ConfiguredItemParser.parse(rawItem);
         } catch (IllegalArgumentException var7) {
            preview = null;
         }

         if (rawItem == null) {
            rawItem = "";
         }

         int[] amount = parseRange(section.getString("amount", "1-1"), 1, 1);
         int[] durability = section.contains("durability") ? parseRange(section.getString("durability"), 0, 0) : null;
         return new LootEditorService.LootView(
            lootKey,
            rawItem,
            preview,
            amount[0],
            amount[1],
            section.getDouble("chance", 100.0),
            durability == null ? null : durability[0],
            durability == null ? null : durability[1]
         );
      }
   }

   public void setLootAmount(LootEditorTarget target, String key, String lootKey, int min, int max) {
      int low = Math.max(0, Math.min(min, max));
      int high = Math.max(low, Math.max(min, max));
      this.mutate(target, config -> config.set(key + ".loot." + lootKey + ".amount", low == high ? String.valueOf(low) : low + "-" + high));
   }

   public void setLootChance(LootEditorTarget target, String key, String lootKey, double chance) {
      this.mutate(target, config -> config.set(key + ".loot." + lootKey + ".chance", round(clamp(chance, 0.0, 100.0))));
   }

   public void setLootDurability(LootEditorTarget target, String key, String lootKey, Integer min, Integer max) {
      this.mutate(target, config -> {
         String path = key + ".loot." + lootKey + ".durability";
         if (min != null && max != null) {
            int low = Math.max(0, Math.min(min, max));
            int high = Math.max(low, Math.max(min, max));
            config.set(path, low == high ? String.valueOf(low) : low + "-" + high);
         } else {
            config.set(path, null);
         }
      });
   }

   public void setLootItem(LootEditorTarget target, String key, String lootKey, ItemStack item) {
      String value = this.describesItselfFully(item) ? item.getType().name() : ConfiguredItemParser.toNbtValue(item);
      this.mutate(target, config -> config.set(key + ".loot." + lootKey + ".item", value));
   }

   public String addLoot(LootEditorTarget target, String key, ItemStack item, int min, int max, double chance, Integer durabilityMin, Integer durabilityMax) {
      String label = this.uniqueLootLabel(target, key);
      String value = this.describesItselfFully(item) ? item.getType().name() : ConfiguredItemParser.toNbtValue(item);
      int low = Math.max(0, Math.min(min, max));
      int high = Math.max(low, Math.max(min, max));
      this.mutate(target, config -> {
         String path = key + ".loot." + label;
         config.set(path + ".item", value);
         config.set(path + ".amount", low == high ? String.valueOf(low) : low + "-" + high);
         config.set(path + ".chance", round(clamp(chance, 0.0, 100.0)));
         if (durabilityMin != null && durabilityMax != null) {
            int dLow = Math.max(0, Math.min(durabilityMin, durabilityMax));
            int dHigh = Math.max(dLow, Math.max(durabilityMin, durabilityMax));
            config.set(path + ".durability", dLow == dHigh ? String.valueOf(dLow) : dLow + "-" + dHigh);
         }
      });
      return label;
   }

   public void removeLoot(LootEditorTarget target, String key, String lootKey) {
      this.mutate(target, config -> config.set(key + ".loot." + lootKey, null));
   }

   private synchronized void mutate(LootEditorTarget target, LootEditorService.Mutation mutation) {
      File file = this.fileOf(target);
      YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
      mutation.apply(config);

      try {
         config.save(file);
      } catch (IOException var6) {
         this.plugin.getLogger().severe("Could not save " + target.getFileName() + ": " + var6.getMessage());
         return;
      }

      this.snapshots.put(target, config);
      this.applyReload(target);
   }

   private void applyReload(LootEditorTarget target) {
      if (target == LootEditorTarget.SMART_SPAWNER) {
         this.plugin.getSpawnerSettingsConfig().reload();
      } else {
         this.plugin.getItemSpawnerSettingsConfig().reload();
      }

      this.plugin.getSpawnerManager().reloadSpawnerDropsAndConfigs();
      if (this.plugin.getSpawnerItemFactory() != null) {
         this.plugin.getSpawnerItemFactory().clearAllCaches();
      }
   }

   private boolean describesItselfFully(ItemStack item) {
      return !item.hasItemMeta() || item.getItemMeta() == null || item.getItemMeta().equals(new ItemStack(item.getType()).getItemMeta());
   }

   private String uniqueLootLabel(LootEditorTarget target, String key) {
      List<String> taken = this.listLootKeys(target, key);

      for (int i = 1; i <= taken.size() + 1; i++) {
         String candidate = String.valueOf(i);
         if (!taken.contains(candidate)) {
            return candidate;
         }
      }

      return String.valueOf(taken.size() + 1);
   }

   public static int[] parseRange(String raw, int fallbackMin, int fallbackMax) {
      String value = raw == null ? "" : raw.trim();

      try {
         int separator = value.indexOf(45, 1);
         if (separator < 0) {
            int single = Integer.parseInt(value);
            return new int[]{single, single};
         } else {
            return new int[]{Integer.parseInt(value.substring(0, separator).trim()), Integer.parseInt(value.substring(separator + 1).trim())};
         }
      } catch (NumberFormatException var6) {
         return new int[]{fallbackMin, fallbackMax};
      }
   }

   private static double clamp(double value, double min, double max) {
      return Math.max(min, Math.min(max, value));
   }

   private static double round(double value) {
      return Math.round(value * 10.0) / 10.0;
   }

   public static final class EntryRef {
      private final LootEditorTarget target;
      private final String entryKey;

      public EntryRef(LootEditorTarget target, String entryKey) {
         this.target = target;
         this.entryKey = entryKey;
      }

      @Generated
      public LootEditorTarget getTarget() {
         return this.target;
      }

      @Generated
      public String getEntryKey() {
         return this.entryKey;
      }
   }

   public record LootView(
      String key, String rawItem, ItemStack preview, int minAmount, int maxAmount, double chance, Integer minDurability, Integer maxDurability
   ) {
      public boolean isBroken() {
         return this.preview == null;
      }

      public String amountLabel() {
         return this.minAmount == this.maxAmount ? String.valueOf(this.minAmount) : this.minAmount + "-" + this.maxAmount;
      }

      public String durabilityLabel() {
         if (this.minDurability != null && this.maxDurability != null) {
            return this.minDurability.equals(this.maxDurability) ? String.valueOf(this.minDurability) : this.minDurability + "-" + this.maxDurability;
         } else {
            return null;
         }
      }
   }

   private interface Mutation {
      void apply(YamlConfiguration var1);
   }
}
