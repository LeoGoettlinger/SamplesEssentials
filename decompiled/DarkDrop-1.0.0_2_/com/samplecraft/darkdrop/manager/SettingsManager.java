package com.samplecraft.darkdrop.manager;

import com.samplecraft.darkdrop.DarkDrop;
import com.samplecraft.darkdrop.model.BadLootType;
import com.samplecraft.darkdrop.model.DropSettings;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.Map.Entry;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;

public class SettingsManager {
   private final DarkDrop plugin;
   private final File file;
   private final Map<UUID, DropSettings> cache = new HashMap<>();

   public SettingsManager(DarkDrop plugin) {
      this.plugin = plugin;
      this.file = new File(plugin.getDataFolder(), "settings.yml");
      if (!this.file.exists()) {
         try {
            plugin.getDataFolder().mkdirs();
            this.file.createNewFile();
         } catch (IOException var3) {
            plugin.getLogger().warning("Konnte settings.yml nicht erstellen: " + var3.getMessage());
         }
      }

      this.loadAll();
   }

   public synchronized DropSettings getOrCreate(UUID playerId) {
      return this.cache.computeIfAbsent(playerId, id -> new DropSettings());
   }

   public synchronized void put(UUID playerId, DropSettings settings) {
      this.cache.put(playerId, settings);
   }

   public synchronized void loadAll() {
      this.cache.clear();
      if (this.file.exists()) {
         YamlConfiguration yaml = YamlConfiguration.loadConfiguration(this.file);

         for (String key : yaml.getKeys(false)) {
            try {
               UUID id = UUID.fromString(key);
               DropSettings settings = new DropSettings();
               ConfigurationSection sec = yaml.getConfigurationSection(key);
               if (sec != null) {
                  settings.setGoodChance(sec.getDouble("good-chance", 50.0));
                  settings.setBadChance(sec.getDouble("bad-chance", 50.0));
                  settings.setGoodAmount(sec.getInt("good-amount", 1));
                  settings.setBadAmount(sec.getInt("bad-amount", 1));
                  String badName = sec.getString("bad-loot-type", BadLootType.LIGHTNING.name());

                  try {
                     settings.setBadLootType(BadLootType.valueOf(badName));
                  } catch (IllegalArgumentException var13) {
                     settings.setBadLootType(BadLootType.LIGHTNING);
                  }

                  List<?> raw = sec.getList("good-loot-items");
                  List<ItemStack> items = new ArrayList<>();
                  if (raw != null) {
                     for (Object o : raw) {
                        if (o instanceof ItemStack stack && !stack.getType().isAir()) {
                           items.add(stack.clone());
                        }
                     }
                  }

                  settings.setGoodLootItems(items);
                  List<String> commands = sec.getStringList("good-loot-commands");
                  settings.setGoodLootCommands(new ArrayList<>(commands));
                  settings.setUseCommandsAsGoodLoot(sec.getBoolean("use-commands-as-good-loot", false));
                  this.cache.put(id, settings);
               }
            } catch (IllegalArgumentException var14) {
            }
         }
      }
   }

   public synchronized void saveAll() {
      YamlConfiguration yaml = new YamlConfiguration();

      for (Entry<UUID, DropSettings> entry : this.cache.entrySet()) {
         DropSettings s = entry.getValue();
         String base = entry.getKey().toString();
         yaml.set(base + ".good-chance", s.getGoodChance());
         yaml.set(base + ".bad-chance", s.getBadChance());
         yaml.set(base + ".good-amount", s.getGoodAmount());
         yaml.set(base + ".bad-amount", s.getBadAmount());
         yaml.set(base + ".bad-loot-type", s.getBadLootType().name());
         yaml.set(base + ".good-loot-items", s.getGoodLootItems());
         yaml.set(base + ".good-loot-commands", s.getGoodLootCommands());
         yaml.set(base + ".use-commands-as-good-loot", s.isUseCommandsAsGoodLoot());
      }

      try {
         yaml.save(this.file);
      } catch (IOException var6) {
         this.plugin.getLogger().warning("Konnte settings.yml nicht speichern: " + var6.getMessage());
      }
   }
}
