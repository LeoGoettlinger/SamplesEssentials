package github.nighter.smartspawner.spawner.config;

import github.nighter.smartspawner.SmartSpawner;
import github.nighter.smartspawner.spawner.lootgen.loot.EntityLootConfig;
import github.nighter.smartspawner.spawner.lootgen.loot.LootItem;
import github.nighter.smartspawner.updates.YamlMigrator;
import java.io.File;
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
import org.bukkit.entity.EntitySnapshot;
import org.bukkit.entity.EntityType;

public class SpawnerSettingsConfig {
   private static final String RESOURCE = "spawner_mobs.yml";
   private static final String LEGACY_RESOURCE = "spawners_settings.yml";
   private static final String TYPE_KEY = "entity";
   private static final Material FALLBACK_HEAD = Material.SPAWNER;
   private final SmartSpawner plugin;
   private final File configFile;
   private final Map<String, SpawnerSettingsConfig.MobDefinition> definitionsByName = new LinkedHashMap<>();
   private final Map<String, String> aliasToName = new HashMap<>();
   private final Map<EntityType, SpawnerSettingsConfig.MobDefinition> baseDefinitions = new EnumMap<>(EntityType.class);
   private final Map<String, String> sectionPaths = new HashMap<>();

   public SpawnerSettingsConfig(SmartSpawner plugin) {
      this.plugin = plugin;
      this.configFile = new File(plugin.getDataFolder(), "spawner_mobs.yml");
   }

   public void load() {
      boolean firstRun = !this.configFile.exists();
      if (!firstRun && SpawnerFileLayout.isLegacy(YamlConfiguration.loadConfiguration(this.configFile), "entity")) {
         SpawnerFileLayout.backup(this.configFile, this.plugin.getLogger());
      }

      YamlMigrator.migrate(
         this.configFile,
         this.plugin.getResource("spawner_mobs.yml"),
         List.of(),
         (user, defaults) -> SpawnerFileLayout.convertLegacy(user, "entity", SpawnerSettingsConfig::baseIdOf),
         true,
         YamlMigrator.OwnedSection.curated((defaults, path) -> path.endsWith(".loot") || path.equals("custom_spawners")),
         this.plugin.getLogger()
      );
      if (firstRun) {
         SupersededConfigNotice.warn(this.plugin, "spawner_mobs.yml", "spawners_settings.yml");
      }

      this.parseConfig(YamlConfiguration.loadConfiguration(this.configFile));
   }

   public void reload() {
      this.load();
   }

   private static EntityType entityTypeOf(String raw) {
      String name = SpawnerConfigName.normalize(raw).toUpperCase(Locale.ROOT);
      if (name.isEmpty()) {
         return null;
      } else {
         try {
            EntityType type = EntityType.valueOf(name);
            return type == EntityType.UNKNOWN ? null : type;
         } catch (IllegalArgumentException var3) {
            return null;
         }
      }
   }

   private static String baseIdOf(String raw) {
      EntityType type = entityTypeOf(raw);
      return type == null ? null : baseId(type);
   }

   private static String baseId(EntityType type) {
      return type.name().toLowerCase(Locale.ROOT);
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
               EntityType type = entityTypeOf(key);
               if (type == null) {
                  this.plugin.getLogger().warning("'" + key + "' in spawner_mobs.yml is not a mob name. Custom spawners go under custom_spawners.");
               } else {
                  String id = baseId(type);
                  if (this.definitionsByName.containsKey(id)) {
                     this.plugin.getLogger().warning("'" + key + "' in spawner_mobs.yml repeats the " + id + " spawner and is ignored.");
                  } else {
                     this.register(this.parseEntry(id, type, section, true, null), key);
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
               String label = "custom spawner '" + keyx + "' in spawner_mobs.yml";
               if (id.isEmpty() || entityTypeOf(id) != null) {
                  this.plugin.getLogger().warning(label + " uses a mob name. Give it a name of its own.");
               } else if (this.definitionsByName.containsKey(id)) {
                  this.plugin.getLogger().warning(label + " repeats another spawner's name and is ignored.");
               } else {
                  EntityType type = entityTypeOf(section.getString("entity", ""));
                  if (type == null) {
                     this.plugin.getLogger().warning(label + " needs a valid 'entity' mob.");
                  } else {
                     this.register(this.parseEntry(id, type, section, false, this.baseDefinitions.get(type)), "custom_spawners." + keyx);
                  }
               }
            }
         }
      }

      for (SpawnerSettingsConfig.MobDefinition definition : this.definitionsByName.values()) {
         for (String alias : definition.aliases()) {
            String name = SpawnerConfigName.normalize(alias);
            if (!name.isEmpty() && !this.definitionsByName.containsKey(name) && !this.aliasToName.containsKey(name)) {
               this.aliasToName.put(name, definition.name());
            } else {
               this.plugin
                  .getLogger()
                  .warning("Alias '" + alias + "' of " + definition.name() + " in spawner_mobs.yml is already a spawner name and is ignored.");
            }
         }
      }
   }

   private void register(SpawnerSettingsConfig.MobDefinition definition, String sectionPath) {
      this.definitionsByName.put(definition.name(), definition);
      this.sectionPaths.put(definition.name(), sectionPath);
      if (definition.base()) {
         this.baseDefinitions.put(definition.entityType(), definition);
      }
   }

   private SpawnerSettingsConfig.MobDefinition parseEntry(
      String id, EntityType type, ConfigurationSection section, boolean base, SpawnerSettingsConfig.MobDefinition inherited
   ) {
      String label = id + " in spawner_mobs.yml";
      int experience = !section.contains("experience") && inherited != null ? inherited.lootConfig().experience() : section.getInt("experience", 0);
      EntityLootConfig lootConfig = new EntityLootConfig(experience, this.parseLoot(section, id));
      SpawnerHead head = SpawnerHead.parse(section, FALLBACK_HEAD, label, this.plugin.getLogger());
      if (head == null && inherited != null) {
         head = inherited.head();
      }

      Double dropChance = inherited == null ? null : inherited.dropChance();
      if (section.contains("drop_chance")) {
         dropChance = section.getDouble("drop_chance", 100.0);
         if (dropChance < 0.0 || dropChance > 100.0) {
            this.plugin.getLogger().warning("Invalid drop_chance for " + label + ". It must be 0.0 to 100.0; using 100.0");
            dropChance = 100.0;
         }
      }

      EntitySnapshot snapshot = !section.contains("nbt_data") && inherited != null ? inherited.snapshot() : this.parseEntityDisplay(type, section, label);
      String displayName = section.getString("display_name");
      if (displayName != null && displayName.isBlank()) {
         displayName = null;
      }

      return new SpawnerSettingsConfig.MobDefinition(
         id, type, base, displayName, lootConfig, head, dropChance, snapshot, List.copyOf(section.getStringList("aliases"))
      );
   }

   private List<LootItem> parseLoot(ConfigurationSection section, String id) {
      List<LootItem> items = new ArrayList<>();
      ConfigurationSection lootSection = section.getConfigurationSection("loot");
      if (lootSection == null) {
         return items;
      } else {
         for (String itemKey : lootSection.getKeys(false)) {
            ConfigurationSection itemSection = lootSection.getConfigurationSection(itemKey);
            if (itemSection != null) {
               LootItem lootItem = LootEntryParser.parse(itemSection, itemKey, this.plugin.getItemPriceManager(), this.plugin.getLogger(), "spawner " + id);
               if (lootItem != null) {
                  items.add(lootItem);
               }
            }
         }

         return items;
      }
   }

   private EntitySnapshot parseEntityDisplay(EntityType entityType, ConfigurationSection section, String label) {
      String nbt = section.getString("nbt_data");
      if (nbt != null && !nbt.isBlank()) {
         String trimmed = nbt.trim();
         if (trimmed.length() >= 2 && trimmed.charAt(0) == '{' && trimmed.charAt(trimmed.length() - 1) == '}') {
            String body = trimmed.substring(1, trimmed.length() - 1).trim();
            String fullNbt = "{id:\"" + entityType.getKey().asString() + "\"" + (body.isEmpty() ? "" : "," + body) + "}";

            try {
               EntitySnapshot snapshot = this.plugin.getServer().getEntityFactory().createEntitySnapshot(fullNbt);
               if (snapshot.getEntityType() == entityType) {
                  return snapshot;
               }
            } catch (IllegalArgumentException var9) {
               this.plugin.getLogger().warning("Invalid nbt_data for " + label + ": " + var9.getMessage());
            }

            return null;
         } else {
            this.plugin.getLogger().warning("Invalid nbt_data for " + label + ": expected an SNBT compound");
            return null;
         }
      } else {
         return null;
      }
   }

   public SpawnerSettingsConfig.MobDefinition getDefinition(String name) {
      return this.definitionsByName.get(SpawnerConfigName.normalize(name));
   }

   public SpawnerSettingsConfig.MobDefinition resolve(String name) {
      String normalized = SpawnerConfigName.normalize(name);
      if (normalized.isEmpty()) {
         return null;
      } else {
         SpawnerSettingsConfig.MobDefinition definition = this.definitionsByName.get(normalized);
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

   public SpawnerSettingsConfig.MobDefinition getBaseDefinition(EntityType type) {
      return type == null ? null : this.baseDefinitions.get(type);
   }

   public SpawnerSettingsConfig.MobDefinition resolveOrBase(String name, EntityType type) {
      SpawnerSettingsConfig.MobDefinition definition = this.resolve(name);
      return definition != null && definition.entityType() == type ? definition : this.getBaseDefinition(type);
   }

   public Set<String> getDefinitionNames() {
      return Collections.unmodifiableSet(this.definitionsByName.keySet());
   }

   public String getSectionPath(String name) {
      return this.sectionPaths.get(SpawnerConfigName.normalize(name));
   }

   public EntityLootConfig getLootConfig(EntityType entityType) {
      SpawnerSettingsConfig.MobDefinition definition = this.getBaseDefinition(entityType);
      return definition == null ? null : definition.lootConfig();
   }

   public SpawnerHead getBaseHead(EntityType entityType) {
      SpawnerSettingsConfig.MobDefinition definition = this.getBaseDefinition(entityType);
      return definition != null && definition.head() != null ? definition.head() : new SpawnerHead(FALLBACK_HEAD, null);
   }

   public double getSpawnerDropChance(String configName, EntityType fallback) {
      SpawnerSettingsConfig.MobDefinition definition = this.resolveOrBase(configName, fallback);
      return definition != null && definition.dropChance() != null ? definition.dropChance() : 100.0;
   }

   public boolean hasSpawnerDropChance(String configName, EntityType fallback) {
      SpawnerSettingsConfig.MobDefinition definition = this.resolveOrBase(configName, fallback);
      return definition != null && definition.dropChance() != null;
   }

   public EntitySnapshot getEntityDisplaySnapshot(String configName, EntityType fallback) {
      SpawnerSettingsConfig.MobDefinition definition = this.resolveOrBase(configName, fallback);
      return definition == null ? null : definition.snapshot();
   }

   public record MobDefinition(
      String name,
      EntityType entityType,
      boolean base,
      String displayName,
      EntityLootConfig lootConfig,
      SpawnerHead head,
      Double dropChance,
      EntitySnapshot snapshot,
      List<String> aliases
   ) {
   }
}
