package github.nighter.smartspawner.spawner.config;

import github.nighter.smartspawner.SmartSpawner;
import github.nighter.smartspawner.spawner.lootgen.loot.EntityLootConfig;
import github.nighter.smartspawner.spawner.lootgen.loot.LootItem;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;

public class ItemSpawnerSettingsConfig {
   private static final String RESOURCE = "spawner_items.yml";
   private static final String LEGACY_RESOURCE = "item_spawners_settings.yml";
   private static final String TYPE_KEY = "item";
   private final SmartSpawner plugin;
   private final File configFile;
   private final Map<String, ItemSpawnerSettingsConfig.ItemDefinition> definitionsByName = new LinkedHashMap<>();
   private final Map<String, String> aliasToName = new HashMap<>();
   private final Map<Material, ItemSpawnerSettingsConfig.ItemDefinition> baseDefinitions = new EnumMap<>(Material.class);
   private final Map<String, String> sectionPaths = new HashMap<>();

   public ItemSpawnerSettingsConfig(SmartSpawner plugin) {
      this.plugin = plugin;
      this.configFile = new File(plugin.getDataFolder(), "spawner_items.yml");
   }

   public void load() {
      if (!this.configFile.exists()) {
         this.saveDefaultConfig();
         SupersededConfigNotice.warn(this.plugin, "spawner_items.yml", "item_spawners_settings.yml");
      }

      YamlConfiguration config = YamlConfiguration.loadConfiguration(this.configFile);
      if (SpawnerFileLayout.isLegacy(config, "item")) {
         SpawnerFileLayout.backup(this.configFile, this.plugin.getLogger());
         SpawnerFileLayout.convertLegacy(config, "item", ItemSpawnerSettingsConfig::baseIdOf);

         try {
            config.save(this.configFile);
         } catch (IOException var3) {
            this.plugin.getLogger().severe("Failed to save converted spawner_items.yml: " + var3.getMessage());
         }
      }

      this.parseConfig(config);
   }

   public void reload() {
      this.load();
   }

   private void saveDefaultConfig() {
      try {
         InputStream inputStream = this.plugin.getResource("spawner_items.yml");
         if (inputStream == null) {
            return;
         }

         String line;
         try (
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(this.configFile), StandardCharsets.UTF_8));
         ) {
            while ((line = reader.readLine()) != null) {
               writer.write(line);
               writer.newLine();
            }
         }
      } catch (IOException var10) {
         this.plugin.getLogger().severe("Failed to create default spawner_items.yml: " + var10.getMessage());
      }
   }

   private static Material materialOf(String raw) {
      String name = SpawnerConfigName.normalize(raw).toUpperCase(Locale.ROOT);
      if (name.isEmpty()) {
         return null;
      } else {
         Material material = Material.getMaterial(name);
         return material != null && material.isItem() && !material.isAir() ? material : null;
      }
   }

   private static String baseIdOf(String raw) {
      Material material = materialOf(raw);
      return material == null ? null : baseId(material);
   }

   private static String baseId(Material material) {
      return material.name().toLowerCase(Locale.ROOT);
   }

   private void parseConfig(FileConfiguration config) {
      SpawnerNameNotices.reset();
      this.definitionsByName.clear();
      this.aliasToName.clear();
      this.baseDefinitions.clear();
      this.sectionPaths.clear();

      for (String key : config.getKeys(false)) {
         if (!key.equals("custom_spawners")) {
            ConfigurationSection section = config.getConfigurationSection(key);
            if (section != null) {
               Material material = materialOf(key);
               if (material == null) {
                  this.plugin.getLogger().warning("'" + key + "' in spawner_items.yml is not an item name. Custom spawners go under custom_spawners.");
               } else {
                  String id = baseId(material);
                  if (this.definitionsByName.containsKey(id)) {
                     this.plugin.getLogger().warning("'" + key + "' in spawner_items.yml repeats the " + id + " spawner and is ignored.");
                  } else {
                     this.register(this.parseEntry(id, material, section, true, null), key);
                  }
               }
            }
         }
      }

      ConfigurationSection customs = config.getConfigurationSection("custom_spawners");
      if (customs != null) {
         for (String keyx : customs.getKeys(false)) {
            ConfigurationSection section = customs.getConfigurationSection(keyx);
            if (section != null) {
               String id = SpawnerConfigName.normalize(keyx);
               String label = "custom spawner '" + keyx + "' in spawner_items.yml";
               if (id.isEmpty() || materialOf(id) != null) {
                  this.plugin.getLogger().warning(label + " uses an item name. Give it a name of its own.");
               } else if (this.definitionsByName.containsKey(id)) {
                  this.plugin.getLogger().warning(label + " repeats another spawner's name and is ignored.");
               } else {
                  Material material = materialOf(section.getString("item", ""));
                  if (material == null) {
                     this.plugin.getLogger().warning(label + " needs a valid 'item'.");
                  } else {
                     this.register(this.parseEntry(id, material, section, false, this.baseDefinitions.get(material)), "custom_spawners." + keyx);
                  }
               }
            }
         }
      }

      for (ItemSpawnerSettingsConfig.ItemDefinition definition : this.definitionsByName.values()) {
         for (String alias : definition.aliases()) {
            String name = SpawnerConfigName.normalize(alias);
            if (!name.isEmpty() && !this.definitionsByName.containsKey(name) && !this.aliasToName.containsKey(name)) {
               this.aliasToName.put(name, definition.name());
            } else {
               this.plugin
                  .getLogger()
                  .warning("Alias '" + alias + "' of " + definition.name() + " in spawner_items.yml is already a spawner name and is ignored.");
            }
         }
      }
   }

   private void register(ItemSpawnerSettingsConfig.ItemDefinition definition, String sectionPath) {
      this.definitionsByName.put(definition.name(), definition);
      this.sectionPaths.put(definition.name(), sectionPath);
      if (definition.base()) {
         this.baseDefinitions.put(definition.material(), definition);
      }
   }

   private ItemSpawnerSettingsConfig.ItemDefinition parseEntry(
      String id, Material material, ConfigurationSection section, boolean base, ItemSpawnerSettingsConfig.ItemDefinition inherited
   ) {
      String label = id + " in spawner_items.yml";
      int experience = !section.contains("experience") && inherited != null ? inherited.lootConfig().experience() : section.getInt("experience", 0);
      EntityLootConfig lootConfig = new EntityLootConfig(experience, this.parseLoot(section, id));
      SpawnerHead head = SpawnerHead.parse(section, material, label, this.plugin.getLogger());
      if (head == null) {
         head = inherited != null ? inherited.head() : new SpawnerHead(material, null);
      }

      ItemStack displayItem = !section.contains("nbt_data") && inherited != null ? inherited.displayItem() : this.parseDisplayItem(section, label);
      String displayName = section.getString("display_name");
      if (displayName != null && displayName.isBlank()) {
         displayName = null;
      }

      return new ItemSpawnerSettingsConfig.ItemDefinition(
         id, material, base, displayName, lootConfig, head, displayItem, List.copyOf(section.getStringList("aliases"))
      );
   }

   private List<LootItem> parseLoot(ConfigurationSection section, String id) {
      List<LootItem> items = new ArrayList<>();
      ConfigurationSection lootSection = section.getConfigurationSection("loot");
      if (lootSection == null) {
         return items;
      } else {
         for (String itemKey : lootSection.getKeys(false)) {
            ConfigurationSection lootItemSection = lootSection.getConfigurationSection(itemKey);
            if (lootItemSection != null) {
               LootItem lootItem = LootEntryParser.parse(
                  lootItemSection, itemKey, this.plugin.getItemPriceManager(), this.plugin.getLogger(), "item spawner " + id
               );
               if (lootItem != null) {
                  items.add(lootItem);
               }
            }
         }

         return items;
      }
   }

   private ItemStack parseDisplayItem(ConfigurationSection section, String label) {
      String rawItem = section.getString("nbt_data");
      if (rawItem != null && !rawItem.isBlank()) {
         try {
            return ConfiguredItemParser.parse(rawItem).asQuantity(1);
         } catch (IllegalArgumentException var5) {
            this.plugin.getLogger().warning("Invalid nbt_data for " + label + ": " + var5.getMessage());
            return null;
         }
      } else {
         return null;
      }
   }

   public ItemSpawnerSettingsConfig.ItemDefinition getDefinition(String name) {
      return this.definitionsByName.get(SpawnerConfigName.normalize(name));
   }

   public ItemSpawnerSettingsConfig.ItemDefinition resolve(String name) {
      String normalized = SpawnerConfigName.normalize(name);
      if (normalized.isEmpty()) {
         return null;
      } else {
         ItemSpawnerSettingsConfig.ItemDefinition definition = this.definitionsByName.get(normalized);
         if (definition != null) {
            return definition;
         } else {
            String aliased = this.aliasToName.get(normalized);
            if (aliased != null) {
               return this.definitionsByName.get(aliased);
            } else {
               String legacy = SpawnerFileLayout.legacyBaseId(normalized);
               return legacy == null ? null : this.definitionsByName.get(legacy);
            }
         }
      }
   }

   public ItemSpawnerSettingsConfig.ItemDefinition getBaseDefinition(Material material) {
      return material == null ? null : this.baseDefinitions.get(material);
   }

   public ItemSpawnerSettingsConfig.ItemDefinition resolveOrBase(String name, Material material) {
      ItemSpawnerSettingsConfig.ItemDefinition definition = this.resolve(name);
      return definition != null && definition.material() == material ? definition : this.getBaseDefinition(material);
   }

   public Set<String> getDefinitionNames() {
      return Collections.unmodifiableSet(this.definitionsByName.keySet());
   }

   public String getSectionPath(String name) {
      return this.sectionPaths.get(SpawnerConfigName.normalize(name));
   }

   public EntityLootConfig getLootConfig(Material material) {
      ItemSpawnerSettingsConfig.ItemDefinition definition = this.getBaseDefinition(material);
      return definition == null ? null : definition.lootConfig();
   }

   public SpawnerHead getBaseHead(Material material) {
      ItemSpawnerSettingsConfig.ItemDefinition definition = this.getBaseDefinition(material);
      return definition != null ? definition.head() : new SpawnerHead(material, null);
   }

   public ItemStack getDisplayItem(String configName, Material fallback) {
      ItemSpawnerSettingsConfig.ItemDefinition definition = this.resolveOrBase(configName, fallback);
      if (definition == null) {
         return new ItemStack(fallback, 1);
      } else if (definition.displayItem() != null) {
         return definition.displayItem().clone();
      } else {
         for (LootItem lootItem : definition.lootConfig().getAllItems()) {
            if (lootItem.template() != null) {
               return lootItem.template().asQuantity(1);
            }
         }

         return new ItemStack(definition.material(), 1);
      }
   }

   public record ItemDefinition(
      String name,
      Material material,
      boolean base,
      String displayName,
      EntityLootConfig lootConfig,
      SpawnerHead head,
      ItemStack displayItem,
      List<String> aliases
   ) {
   }
}
