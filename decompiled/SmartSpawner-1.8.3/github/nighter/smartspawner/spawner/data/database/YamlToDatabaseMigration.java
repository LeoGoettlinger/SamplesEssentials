package github.nighter.smartspawner.spawner.data.database;

import github.nighter.smartspawner.SmartSpawner;
import github.nighter.smartspawner.spawner.config.SpawnerConfigName;
import github.nighter.smartspawner.spawner.data.legacy.LegacyInventoryCodec;
import github.nighter.smartspawner.spawner.data.storage.SpawnerInventoryCodec;
import github.nighter.smartspawner.spawner.data.storage.StorageMode;
import github.nighter.smartspawner.spawner.properties.ItemSignature;
import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;

public class YamlToDatabaseMigration {
   private final SmartSpawner plugin;
   private final Logger logger;
   private final DatabaseManager databaseManager;
   private final String serverName;
   private static final String YAML_FILE_NAME = "spawners_data.yml";
   private static final String MIGRATED_FILE_SUFFIX = ".migrated";
   private static final String INSERT_SQL_MYSQL = "INSERT INTO %s (\n    spawner_id, world, loc_x, loc_y, loc_z, chunk_x, chunk_z,\n    entity_type, itemspawner_type, stack_size, max_stack_size,\n    active, stop, activation_range, delay, last_spawn_time, min_mobs, max_mobs,\n    max_loot_slots, is_at_capacity, total_items, exp, max_stored_exp,\n    last_interacted_player, preferred_sort_item, filtered_items, storage_items, config_name\n) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)\nON DUPLICATE KEY UPDATE\n    world = VALUES(world),\n    loc_x = VALUES(loc_x),\n    loc_y = VALUES(loc_y),\n    loc_z = VALUES(loc_z),\n    chunk_x = VALUES(chunk_x),\n    chunk_z = VALUES(chunk_z),\n    entity_type = VALUES(entity_type),\n    itemspawner_type = VALUES(itemspawner_type),\n    stack_size = VALUES(stack_size),\n    max_stack_size = VALUES(max_stack_size),\n    active = VALUES(active),\n    stop = VALUES(stop),\n    activation_range = VALUES(activation_range),\n    delay = VALUES(delay),\n    last_spawn_time = VALUES(last_spawn_time),\n    min_mobs = VALUES(min_mobs),\n    max_mobs = VALUES(max_mobs),\n    max_loot_slots = VALUES(max_loot_slots),\n    is_at_capacity = VALUES(is_at_capacity),\n    total_items = VALUES(total_items),\n    exp = VALUES(exp),\n    max_stored_exp = VALUES(max_stored_exp),\n    last_interacted_player = VALUES(last_interacted_player),\n    preferred_sort_item = VALUES(preferred_sort_item),\n    filtered_items = VALUES(filtered_items),\n    storage_items = VALUES(storage_items),\n    config_name = VALUES(config_name)\n";
   private static final String INSERT_SQL_SQLITE = "INSERT INTO %s (\n    spawner_id, world, loc_x, loc_y, loc_z, chunk_x, chunk_z,\n    entity_type, itemspawner_type, stack_size, max_stack_size,\n    active, stop, activation_range, delay, last_spawn_time, min_mobs, max_mobs,\n    max_loot_slots, is_at_capacity, total_items, exp, max_stored_exp,\n    last_interacted_player, preferred_sort_item, filtered_items, storage_items, config_name\n) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)\nON CONFLICT(spawner_id) DO UPDATE SET\n    world = excluded.world,\n    loc_x = excluded.loc_x,\n    loc_y = excluded.loc_y,\n    loc_z = excluded.loc_z,\n    chunk_x = excluded.chunk_x,\n    chunk_z = excluded.chunk_z,\n    entity_type = excluded.entity_type,\n    itemspawner_type = excluded.itemspawner_type,\n    stack_size = excluded.stack_size,\n    max_stack_size = excluded.max_stack_size,\n    active = excluded.active,\n    stop = excluded.stop,\n    activation_range = excluded.activation_range,\n    delay = excluded.delay,\n    last_spawn_time = excluded.last_spawn_time,\n    min_mobs = excluded.min_mobs,\n    max_mobs = excluded.max_mobs,\n    max_loot_slots = excluded.max_loot_slots,\n    is_at_capacity = excluded.is_at_capacity,\n    total_items = excluded.total_items,\n    exp = excluded.exp,\n    max_stored_exp = excluded.max_stored_exp,\n    last_interacted_player = excluded.last_interacted_player,\n    preferred_sort_item = excluded.preferred_sort_item,\n    filtered_items = excluded.filtered_items,\n    storage_items = excluded.storage_items,\n    config_name = excluded.config_name\n";

   public YamlToDatabaseMigration(SmartSpawner plugin, DatabaseManager databaseManager) {
      this.plugin = plugin;
      this.logger = plugin.getLogger();
      this.databaseManager = databaseManager;
      this.serverName = databaseManager.getServerName();
   }

   public boolean needsMigration() {
      File yamlFile = new File(this.plugin.getDataFolder(), "spawners_data.yml");
      if (!yamlFile.exists()) {
         return false;
      } else {
         File migratedFile = new File(this.plugin.getDataFolder(), "spawners_data.yml.migrated");
         if (migratedFile.exists()) {
            return false;
         } else {
            FileConfiguration yamlData = YamlConfiguration.loadConfiguration(yamlFile);
            ConfigurationSection spawnersSection = yamlData.getConfigurationSection("spawners");
            return spawnersSection != null && !spawnersSection.getKeys(false).isEmpty();
         }
      }
   }

   public boolean migrate() {
      this.logger.info("Starting YAML to database migration...");
      File yamlFile = new File(this.plugin.getDataFolder(), "spawners_data.yml");
      if (!yamlFile.exists()) {
         this.logger.info("No YAML file found, skipping migration.");
         return true;
      } else {
         FileConfiguration yamlData = YamlConfiguration.loadConfiguration(yamlFile);
         ConfigurationSection spawnersSection = yamlData.getConfigurationSection("spawners");
         if (spawnersSection != null && !spawnersSection.getKeys(false).isEmpty()) {
            int totalSpawners = spawnersSection.getKeys(false).size();
            int migratedCount = 0;
            int failedCount = 0;
            this.logger.info("Found " + totalSpawners + " spawners to migrate.");
            String insertSql = (this.databaseManager.getStorageMode() == StorageMode.SQLITE
                  ? "INSERT INTO %s (\n    spawner_id, world, loc_x, loc_y, loc_z, chunk_x, chunk_z,\n    entity_type, itemspawner_type, stack_size, max_stack_size,\n    active, stop, activation_range, delay, last_spawn_time, min_mobs, max_mobs,\n    max_loot_slots, is_at_capacity, total_items, exp, max_stored_exp,\n    last_interacted_player, preferred_sort_item, filtered_items, storage_items, config_name\n) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)\nON CONFLICT(spawner_id) DO UPDATE SET\n    world = excluded.world,\n    loc_x = excluded.loc_x,\n    loc_y = excluded.loc_y,\n    loc_z = excluded.loc_z,\n    chunk_x = excluded.chunk_x,\n    chunk_z = excluded.chunk_z,\n    entity_type = excluded.entity_type,\n    itemspawner_type = excluded.itemspawner_type,\n    stack_size = excluded.stack_size,\n    max_stack_size = excluded.max_stack_size,\n    active = excluded.active,\n    stop = excluded.stop,\n    activation_range = excluded.activation_range,\n    delay = excluded.delay,\n    last_spawn_time = excluded.last_spawn_time,\n    min_mobs = excluded.min_mobs,\n    max_mobs = excluded.max_mobs,\n    max_loot_slots = excluded.max_loot_slots,\n    is_at_capacity = excluded.is_at_capacity,\n    total_items = excluded.total_items,\n    exp = excluded.exp,\n    max_stored_exp = excluded.max_stored_exp,\n    last_interacted_player = excluded.last_interacted_player,\n    preferred_sort_item = excluded.preferred_sort_item,\n    filtered_items = excluded.filtered_items,\n    storage_items = excluded.storage_items,\n    config_name = excluded.config_name\n"
                  : "INSERT INTO %s (\n    spawner_id, world, loc_x, loc_y, loc_z, chunk_x, chunk_z,\n    entity_type, itemspawner_type, stack_size, max_stack_size,\n    active, stop, activation_range, delay, last_spawn_time, min_mobs, max_mobs,\n    max_loot_slots, is_at_capacity, total_items, exp, max_stored_exp,\n    last_interacted_player, preferred_sort_item, filtered_items, storage_items, config_name\n) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)\nON DUPLICATE KEY UPDATE\n    world = VALUES(world),\n    loc_x = VALUES(loc_x),\n    loc_y = VALUES(loc_y),\n    loc_z = VALUES(loc_z),\n    chunk_x = VALUES(chunk_x),\n    chunk_z = VALUES(chunk_z),\n    entity_type = VALUES(entity_type),\n    itemspawner_type = VALUES(itemspawner_type),\n    stack_size = VALUES(stack_size),\n    max_stack_size = VALUES(max_stack_size),\n    active = VALUES(active),\n    stop = VALUES(stop),\n    activation_range = VALUES(activation_range),\n    delay = VALUES(delay),\n    last_spawn_time = VALUES(last_spawn_time),\n    min_mobs = VALUES(min_mobs),\n    max_mobs = VALUES(max_mobs),\n    max_loot_slots = VALUES(max_loot_slots),\n    is_at_capacity = VALUES(is_at_capacity),\n    total_items = VALUES(total_items),\n    exp = VALUES(exp),\n    max_stored_exp = VALUES(max_stored_exp),\n    last_interacted_player = VALUES(last_interacted_player),\n    preferred_sort_item = VALUES(preferred_sort_item),\n    filtered_items = VALUES(filtered_items),\n    storage_items = VALUES(storage_items),\n    config_name = VALUES(config_name)\n")
               .formatted(this.databaseManager.getTableSpawners());

            try {
               boolean var22;
               try (
                  Connection conn = this.databaseManager.getConnection();
                  PreparedStatement stmt = conn.prepareStatement(insertSql);
               ) {
                  conn.setAutoCommit(false);
                  int batchCount = 0;
                  int BATCH_SIZE = 100;

                  for (String spawnerId : spawnersSection.getKeys(false)) {
                     try {
                        if (this.migrateSpawner(stmt, yamlData, spawnerId)) {
                           stmt.addBatch();
                           batchCount++;
                           migratedCount++;
                           if (batchCount >= 100) {
                              stmt.executeBatch();
                              conn.commit();
                              batchCount = 0;
                              this.logger.info("Migrated " + migratedCount + "/" + totalSpawners + " spawners...");
                           }
                        } else {
                           failedCount++;
                        }
                     } catch (Exception var17) {
                        this.logger.log(Level.WARNING, "Failed to migrate spawner " + spawnerId, (Throwable)var17);
                        failedCount++;
                     }
                  }

                  if (batchCount > 0) {
                     stmt.executeBatch();
                     conn.commit();
                  }

                  this.logger.info("Migration completed. Migrated: " + migratedCount + ", Failed: " + failedCount);
                  if (failedCount == 0 || migratedCount > 0) {
                     File migratedFile = new File(this.plugin.getDataFolder(), "spawners_data.yml.migrated");
                     if (yamlFile.renameTo(migratedFile)) {
                        this.logger.info("YAML file renamed to spawners_data.yml.migrated");
                     } else {
                        this.logger.warning("Failed to rename YAML file. Manual cleanup may be required.");
                     }
                  }

                  var22 = failedCount == 0;
               }

               return var22;
            } catch (SQLException var20) {
               this.logger.log(Level.SEVERE, "Database error during migration", (Throwable)var20);
               return false;
            }
         } else {
            this.logger.info("No spawners found in YAML file, skipping migration.");
            return true;
         }
      }
   }

   private boolean migrateSpawner(PreparedStatement stmt, FileConfiguration yamlData, String spawnerId) throws SQLException {
      String path = "spawners." + spawnerId;
      String locationString = yamlData.getString(path + ".location");
      if (locationString == null) {
         this.logger.warning("No location for spawner " + spawnerId + ", skipping.");
         return false;
      } else {
         String[] locParts = locationString.split(",");
         if (locParts.length != 4) {
            this.logger.warning("Invalid location format for spawner " + spawnerId + ", skipping.");
            return false;
         } else {
            String worldName = locParts[0];

            int locX;
            int locY;
            int locZ;
            try {
               locX = Integer.parseInt(locParts[1]);
               locY = Integer.parseInt(locParts[2]);
               locZ = Integer.parseInt(locParts[3]);
            } catch (NumberFormatException var39) {
               this.logger.warning("Invalid location coordinates for spawner " + spawnerId + ", skipping.");
               return false;
            }

            String entityTypeString = yamlData.getString(path + ".entityType");
            if (entityTypeString == null) {
               this.logger.warning("No entity type for spawner " + spawnerId + ", skipping.");
               return false;
            } else {
               EntityType entityType;
               try {
                  entityType = EntityType.valueOf(entityTypeString);
               } catch (IllegalArgumentException var38) {
                  this.logger.warning("Invalid entity type for spawner " + spawnerId + ": " + entityTypeString + ", skipping.");
                  return false;
               }

               String itemSpawnerMaterial = yamlData.getString(path + ".itemSpawnerMaterial");
               String settingsString = yamlData.getString(path + ".settings");
               int spawnerExp = 0;
               boolean spawnerActive = true;
               int spawnerRange = 16;
               boolean spawnerStop = true;
               long spawnDelay = 500L;
               int maxSpawnerLootSlots = 45;
               int maxStoredExp = 1000;
               int minMobs = 1;
               int maxMobs = 4;
               int stackSize = 1;
               int maxStackSize = 1000;
               long lastSpawnTime = 0L;
               boolean isAtCapacity = false;
               if (settingsString != null) {
                  String[] settings = settingsString.split(",");
                  int version = yamlData.getInt("data_version", 1);

                  try {
                     if (version >= 3 && settings.length >= 13) {
                        spawnerExp = Integer.parseInt(settings[0]);
                        spawnerActive = Boolean.parseBoolean(settings[1]);
                        spawnerRange = Integer.parseInt(settings[2]);
                        spawnerStop = Boolean.parseBoolean(settings[3]);
                        spawnDelay = Long.parseLong(settings[4]);
                        maxSpawnerLootSlots = Integer.parseInt(settings[5]);
                        maxStoredExp = Integer.parseInt(settings[6]);
                        minMobs = Integer.parseInt(settings[7]);
                        maxMobs = Integer.parseInt(settings[8]);
                        stackSize = Integer.parseInt(settings[9]);
                        maxStackSize = Integer.parseInt(settings[10]);
                        lastSpawnTime = Long.parseLong(settings[11]);
                        isAtCapacity = Boolean.parseBoolean(settings[12]);
                     } else if (settings.length >= 11) {
                        spawnerExp = Integer.parseInt(settings[0]);
                        spawnerActive = Boolean.parseBoolean(settings[1]);
                        spawnerRange = Integer.parseInt(settings[2]);
                        spawnerStop = Boolean.parseBoolean(settings[3]);
                        spawnDelay = Long.parseLong(settings[4]);
                        maxSpawnerLootSlots = Integer.parseInt(settings[5]);
                        maxStoredExp = Integer.parseInt(settings[6]);
                        minMobs = Integer.parseInt(settings[7]);
                        maxMobs = Integer.parseInt(settings[8]);
                        stackSize = Integer.parseInt(settings[9]);
                        lastSpawnTime = Long.parseLong(settings[10]);
                     }
                  } catch (NumberFormatException var40) {
                     this.logger.warning("Invalid settings format for spawner " + spawnerId + ", using defaults.");
                  }
               }

               String filteredItemsStr = yamlData.getString(path + ".filteredItems");
               String preferredSortItemStr = yamlData.getString(path + ".preferredSortItem");
               String lastInteractedPlayer = yamlData.getString(path + ".lastInteractedPlayer");
               List<String> inventoryData = yamlData.getStringList(path + ".inventory");
               Map<ItemSignature, Long> items = this.readLegacyInventory(inventoryData);

               byte[] itemsBlob;
               try {
                  itemsBlob = SpawnerInventoryCodec.encode(items);
               } catch (IOException var37) {
                  this.logger.log(Level.WARNING, "Could not encode inventory for spawner " + spawnerId + ", it will be migrated empty", (Throwable)var37);
                  itemsBlob = null;
                  items = Map.of();
               }

               stmt.setString(1, spawnerId);
               stmt.setString(2, worldName);
               stmt.setInt(3, locX);
               stmt.setInt(4, locY);
               stmt.setInt(5, locZ);
               stmt.setInt(6, locX >> 4);
               stmt.setInt(7, locZ >> 4);
               stmt.setString(8, entityType.name());
               stmt.setString(9, itemSpawnerMaterial);
               stmt.setInt(10, stackSize);
               stmt.setInt(11, maxStackSize);
               stmt.setBoolean(12, spawnerActive);
               stmt.setBoolean(13, spawnerStop);
               stmt.setInt(14, spawnerRange);
               stmt.setLong(15, spawnDelay);
               stmt.setLong(16, lastSpawnTime);
               stmt.setInt(17, minMobs);
               stmt.setInt(18, maxMobs);
               stmt.setInt(19, maxSpawnerLootSlots);
               stmt.setBoolean(20, isAtCapacity);
               stmt.setLong(21, SpawnerInventoryCodec.totalItems(items));
               stmt.setInt(22, spawnerExp);
               stmt.setInt(23, maxStoredExp);
               stmt.setString(24, lastInteractedPlayer);
               stmt.setString(25, preferredSortItemStr);
               stmt.setString(26, filteredItemsStr);
               stmt.setBytes(27, itemsBlob);
               stmt.setString(28, SpawnerConfigName.defaultName(itemSpawnerMaterial != null ? itemSpawnerMaterial : entityType.name()));
               return true;
            }
         }
      }
   }

   private Map<ItemSignature, Long> readLegacyInventory(List<String> inventoryData) {
      if (inventoryData != null && !inventoryData.isEmpty()) {
         Map<ItemStack, Long> legacyItems = LegacyInventoryCodec.deserialize(inventoryData);
         if (legacyItems.isEmpty()) {
            return Map.of();
         } else {
            Map<ItemSignature, Long> items = new LinkedHashMap<>(Math.max(16, legacyItems.size() * 2));

            for (Entry<ItemStack, Long> entry : legacyItems.entrySet()) {
               items.merge(new ItemSignature(entry.getKey()), entry.getValue(), Long::sum);
            }

            return items;
         }
      } else {
         return Map.of();
      }
   }
}
