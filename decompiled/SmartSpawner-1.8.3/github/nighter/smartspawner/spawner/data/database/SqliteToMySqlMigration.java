package github.nighter.smartspawner.spawner.data.database;

import github.nighter.smartspawner.SmartSpawner;
import github.nighter.smartspawner.spawner.data.legacy.LegacyInventoryCodec;
import github.nighter.smartspawner.spawner.data.storage.SpawnerInventoryCodec;
import github.nighter.smartspawner.spawner.data.storage.StorageMode;
import github.nighter.smartspawner.spawner.properties.ItemSignature;
import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.inventory.ItemStack;

public class SqliteToMySqlMigration {
   private final SmartSpawner plugin;
   private final Logger logger;
   private final DatabaseManager mysqlManager;
   private static final String MIGRATED_FILE_SUFFIX = ".migrated";
   private static final String INSERT_SQL_MYSQL = "INSERT INTO %s (\n    spawner_id, world, loc_x, loc_y, loc_z, chunk_x, chunk_z,\n    entity_type, itemspawner_type, stack_size, max_stack_size,\n    active, stop, activation_range, delay, last_spawn_time, min_mobs, max_mobs,\n    max_loot_slots, is_at_capacity, total_items, exp, max_stored_exp,\n    last_interacted_player, preferred_sort_item, filtered_items, storage_items, config_name\n) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)\nON DUPLICATE KEY UPDATE\n    world = VALUES(world),\n    loc_x = VALUES(loc_x),\n    loc_y = VALUES(loc_y),\n    loc_z = VALUES(loc_z),\n    chunk_x = VALUES(chunk_x),\n    chunk_z = VALUES(chunk_z),\n    entity_type = VALUES(entity_type),\n    itemspawner_type = VALUES(itemspawner_type),\n    stack_size = VALUES(stack_size),\n    max_stack_size = VALUES(max_stack_size),\n    active = VALUES(active),\n    stop = VALUES(stop),\n    activation_range = VALUES(activation_range),\n    delay = VALUES(delay),\n    last_spawn_time = VALUES(last_spawn_time),\n    min_mobs = VALUES(min_mobs),\n    max_mobs = VALUES(max_mobs),\n    max_loot_slots = VALUES(max_loot_slots),\n    is_at_capacity = VALUES(is_at_capacity),\n    total_items = VALUES(total_items),\n    exp = VALUES(exp),\n    max_stored_exp = VALUES(max_stored_exp),\n    last_interacted_player = VALUES(last_interacted_player),\n    preferred_sort_item = VALUES(preferred_sort_item),\n    filtered_items = VALUES(filtered_items),\n    storage_items = VALUES(storage_items),\n    config_name = VALUES(config_name)\n";
   private static final String LEGACY_TABLE_SPAWNERS = "smart_spawners";
   private static final String TABLE_PLACEHOLDER = "{table}";
   private static final String SELECT_COMMON_COLUMNS = "spawner_id, world, loc_x, loc_y, loc_z,\nentity_type, itemspawner_type, stack_size, max_stack_size,\nactive, stop, activation_range, delay, last_spawn_time, min_mobs, max_mobs,\nmax_loot_slots, is_at_capacity, exp, max_stored_exp,\nlast_interacted_player, preferred_sort_item, filtered_items\n";
   private static final String SELECT_COMMON_COLUMNS_LEGACY = "spawner_id, world_name AS world, loc_x, loc_y, loc_z,\nentity_type, item_spawner_material AS itemspawner_type,\nstack_size, max_stack_size,\nspawner_active AS active, spawner_stop AS stop,\nspawner_range AS activation_range, spawn_delay AS delay,\nlast_spawn_time, min_mobs, max_mobs,\nmax_spawner_loot_slots AS max_loot_slots, is_at_capacity,\nspawner_exp AS exp, max_stored_exp,\nlast_interacted_player, preferred_sort_item, filtered_items\n";
   private static final String SELECT_ALL_SQLITE = "SELECT spawner_id, world, loc_x, loc_y, loc_z,\nentity_type, itemspawner_type, stack_size, max_stack_size,\nactive, stop, activation_range, delay, last_spawn_time, min_mobs, max_mobs,\nmax_loot_slots, is_at_capacity, exp, max_stored_exp,\nlast_interacted_player, preferred_sort_item, filtered_items\n, storage_items, total_items, %s FROM {table}";
   private static final String SELECT_ALL_SQLITE_LEGACY = "SELECT spawner_id, world_name AS world, loc_x, loc_y, loc_z,\nentity_type, item_spawner_material AS itemspawner_type,\nstack_size, max_stack_size,\nspawner_active AS active, spawner_stop AS stop,\nspawner_range AS activation_range, spawn_delay AS delay,\nlast_spawn_time, min_mobs, max_mobs,\nmax_spawner_loot_slots AS max_loot_slots, is_at_capacity,\nspawner_exp AS exp, max_stored_exp,\nlast_interacted_player, preferred_sort_item, filtered_items\n, inventory_data, NULL AS config_name FROM {table}";

   public SqliteToMySqlMigration(SmartSpawner plugin, DatabaseManager mysqlManager) {
      this.plugin = plugin;
      this.logger = plugin.getLogger();
      this.mysqlManager = mysqlManager;
   }

   public boolean needsMigration() {
      if (this.mysqlManager.getStorageMode() != StorageMode.MYSQL) {
         return false;
      } else {
         String sqliteFileName = this.plugin.getConfig().getString("database.sqlite-file", "spawners.db");
         File sqliteFile = new File(this.plugin.getDataFolder(), sqliteFileName);
         if (!sqliteFile.exists()) {
            return false;
         } else {
            File migratedFile = new File(this.plugin.getDataFolder(), sqliteFileName + ".migrated");
            return migratedFile.exists() ? false : this.hasSqliteData(sqliteFile);
         }
      }
   }

   private boolean hasSqliteData(File sqliteFile) {
      String jdbcUrl = "jdbc:sqlite:" + sqliteFile.getAbsolutePath();

      try {
         boolean var16;
         try (Connection conn = DriverManager.getConnection(jdbcUrl)) {
            String table = this.resolveSourceTable(conn);
            if (table != null) {
               try (
                  Statement stmt = conn.createStatement();
                  ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM " + table);
               ) {
                  if (rs.next()) {
                     return rs.getInt(1) > 0;
                  }

                  return false;
               }
            }

            var16 = false;
         }

         return var16;
      } catch (SQLException var15) {
         return false;
      }
   }

   private String resolveSourceTable(Connection conn) throws SQLException {
      for (String candidate : new String[]{this.mysqlManager.getTableSpawners(), this.mysqlManager.getTableSingleServer(), "smart_spawners"}) {
         if (this.sqliteTableExists(conn, candidate)) {
            return candidate;
         }
      }

      return null;
   }

   private boolean sqliteTableExists(Connection conn, String tableName) throws SQLException {
      boolean var5;
      try (PreparedStatement stmt = conn.prepareStatement("SELECT name FROM sqlite_master WHERE type = 'table' AND name = ?")) {
         stmt.setString(1, tableName);

         try (ResultSet rs = stmt.executeQuery()) {
            var5 = rs.next();
         }
      }

      return var5;
   }

   private boolean sqliteColumnExists(Connection conn, String tableName, String columnName) throws SQLException {
      try (
         Statement stmt = conn.createStatement();
         ResultSet rs = stmt.executeQuery("PRAGMA table_info(" + tableName + ")");
      ) {
         while (rs.next()) {
            if (columnName.equalsIgnoreCase(rs.getString("name"))) {
               return true;
            }
         }
      }

      return false;
   }

   public boolean migrate() {
      this.logger.info("Starting SQLite to MySQL migration...");
      String sqliteFileName = this.plugin.getConfig().getString("database.sqlite-file", "spawners.db");
      File sqliteFile = new File(this.plugin.getDataFolder(), sqliteFileName);
      if (!sqliteFile.exists()) {
         this.logger.info("No SQLite file found, skipping migration.");
         return true;
      } else {
         String sqliteJdbcUrl = "jdbc:sqlite:" + sqliteFile.getAbsolutePath();
         int totalSpawners = 0;
         int migratedCount = 0;
         int failedCount = 0;

         try {
            boolean var35;
            try (Connection sqliteConn = DriverManager.getConnection(sqliteJdbcUrl)) {
               String sourceTable = this.resolveSourceTable(sqliteConn);
               if (sourceTable == null) {
                  this.logger.info("SQLite file has no spawner table, skipping migration.");
                  return true;
               }

               boolean hasItemBlob = this.sqliteColumnExists(sqliteConn, sourceTable, "storage_items");
               boolean hasConfigName = this.sqliteColumnExists(sqliteConn, sourceTable, "config_name");
               String selectSql = hasItemBlob
                  ? "SELECT spawner_id, world, loc_x, loc_y, loc_z,\nentity_type, itemspawner_type, stack_size, max_stack_size,\nactive, stop, activation_range, delay, last_spawn_time, min_mobs, max_mobs,\nmax_loot_slots, is_at_capacity, exp, max_stored_exp,\nlast_interacted_player, preferred_sort_item, filtered_items\n, storage_items, total_items, %s FROM {table}"
                     .formatted(hasConfigName ? "config_name" : "NULL AS config_name")
                     .replace("{table}", sourceTable)
                  : "SELECT spawner_id, world_name AS world, loc_x, loc_y, loc_z,\nentity_type, item_spawner_material AS itemspawner_type,\nstack_size, max_stack_size,\nspawner_active AS active, spawner_stop AS stop,\nspawner_range AS activation_range, spawn_delay AS delay,\nlast_spawn_time, min_mobs, max_mobs,\nmax_spawner_loot_slots AS max_loot_slots, is_at_capacity,\nspawner_exp AS exp, max_stored_exp,\nlast_interacted_player, preferred_sort_item, filtered_items\n, inventory_data, NULL AS config_name FROM {table}"
                     .replace("{table}", sourceTable);

               try (
                  Connection mysqlConn = this.mysqlManager.getConnection();
                  PreparedStatement selectStmt = sqliteConn.prepareStatement(selectSql);
                  PreparedStatement insertStmt = mysqlConn.prepareStatement(
                     "INSERT INTO %s (\n    spawner_id, world, loc_x, loc_y, loc_z, chunk_x, chunk_z,\n    entity_type, itemspawner_type, stack_size, max_stack_size,\n    active, stop, activation_range, delay, last_spawn_time, min_mobs, max_mobs,\n    max_loot_slots, is_at_capacity, total_items, exp, max_stored_exp,\n    last_interacted_player, preferred_sort_item, filtered_items, storage_items, config_name\n) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)\nON DUPLICATE KEY UPDATE\n    world = VALUES(world),\n    loc_x = VALUES(loc_x),\n    loc_y = VALUES(loc_y),\n    loc_z = VALUES(loc_z),\n    chunk_x = VALUES(chunk_x),\n    chunk_z = VALUES(chunk_z),\n    entity_type = VALUES(entity_type),\n    itemspawner_type = VALUES(itemspawner_type),\n    stack_size = VALUES(stack_size),\n    max_stack_size = VALUES(max_stack_size),\n    active = VALUES(active),\n    stop = VALUES(stop),\n    activation_range = VALUES(activation_range),\n    delay = VALUES(delay),\n    last_spawn_time = VALUES(last_spawn_time),\n    min_mobs = VALUES(min_mobs),\n    max_mobs = VALUES(max_mobs),\n    max_loot_slots = VALUES(max_loot_slots),\n    is_at_capacity = VALUES(is_at_capacity),\n    total_items = VALUES(total_items),\n    exp = VALUES(exp),\n    max_stored_exp = VALUES(max_stored_exp),\n    last_interacted_player = VALUES(last_interacted_player),\n    preferred_sort_item = VALUES(preferred_sort_item),\n    filtered_items = VALUES(filtered_items),\n    storage_items = VALUES(storage_items),\n    config_name = VALUES(config_name)\n"
                        .formatted(this.mysqlManager.getTableSpawners())
                  );
               ) {
                  mysqlConn.setAutoCommit(false);

                  try (ResultSet rs = selectStmt.executeQuery()) {
                     int batchCount = 0;
                     int BATCH_SIZE = 100;

                     while (rs.next()) {
                        totalSpawners++;

                        try {
                           int locX = rs.getInt("loc_x");
                           int locZ = rs.getInt("loc_z");
                           insertStmt.setString(1, rs.getString("spawner_id"));
                           insertStmt.setString(2, rs.getString("world"));
                           insertStmt.setInt(3, locX);
                           insertStmt.setInt(4, rs.getInt("loc_y"));
                           insertStmt.setInt(5, locZ);
                           insertStmt.setInt(6, locX >> 4);
                           insertStmt.setInt(7, locZ >> 4);
                           insertStmt.setString(8, rs.getString("entity_type"));
                           insertStmt.setString(9, rs.getString("itemspawner_type"));
                           insertStmt.setInt(10, rs.getInt("stack_size"));
                           insertStmt.setInt(11, rs.getInt("max_stack_size"));
                           insertStmt.setBoolean(12, rs.getBoolean("active"));
                           insertStmt.setBoolean(13, rs.getBoolean("stop"));
                           insertStmt.setInt(14, rs.getInt("activation_range"));
                           insertStmt.setLong(15, rs.getLong("delay"));
                           insertStmt.setLong(16, rs.getLong("last_spawn_time"));
                           insertStmt.setInt(17, rs.getInt("min_mobs"));
                           insertStmt.setInt(18, rs.getInt("max_mobs"));
                           insertStmt.setInt(19, rs.getInt("max_loot_slots"));
                           insertStmt.setBoolean(20, rs.getBoolean("is_at_capacity"));
                           if (hasItemBlob) {
                              insertStmt.setLong(21, rs.getLong("total_items"));
                              insertStmt.setBytes(27, rs.getBytes("storage_items"));
                           } else {
                              Map<ItemSignature, Long> items = this.readLegacyInventory(rs.getString("inventory_data"));
                              insertStmt.setLong(21, SpawnerInventoryCodec.totalItems(items));
                              insertStmt.setBytes(27, SpawnerInventoryCodec.encode(items));
                           }

                           insertStmt.setLong(22, rs.getLong("exp"));
                           insertStmt.setLong(23, rs.getLong("max_stored_exp"));
                           insertStmt.setString(24, rs.getString("last_interacted_player"));
                           insertStmt.setString(25, rs.getString("preferred_sort_item"));
                           insertStmt.setString(26, rs.getString("filtered_items"));
                           insertStmt.setString(28, rs.getString("config_name"));
                           insertStmt.addBatch();
                           batchCount++;
                           migratedCount++;
                           if (batchCount >= 100) {
                              insertStmt.executeBatch();
                              mysqlConn.commit();
                              batchCount = 0;
                              this.logger.info("Migrated " + migratedCount + " spawners...");
                           }
                        } catch (Exception var26) {
                           this.logger.log(Level.WARNING, "Failed to migrate spawner: " + rs.getString("spawner_id"), (Throwable)var26);
                           failedCount++;
                        }
                     }

                     if (batchCount > 0) {
                        insertStmt.executeBatch();
                        mysqlConn.commit();
                     }
                  }

                  this.logger.info("Migration completed. Total: " + totalSpawners + ", Migrated: " + migratedCount + ", Failed: " + failedCount);
                  if (failedCount == 0) {
                     File migratedFile = new File(this.plugin.getDataFolder(), sqliteFileName + ".migrated");
                     if (sqliteFile.renameTo(migratedFile)) {
                        this.logger.info("SQLite file renamed to " + sqliteFileName + ".migrated");
                     } else {
                        this.logger.warning("Failed to rename SQLite file. Manual cleanup may be required.");
                     }
                  }

                  var35 = failedCount == 0;
               }
            }

            return var35;
         } catch (SQLException var32) {
            this.logger.log(Level.SEVERE, "Database error during SQLite to MySQL migration", (Throwable)var32);
            return false;
         }
      }
   }

   private Map<ItemSignature, Long> readLegacyInventory(String inventoryData) {
      if (inventoryData != null && !inventoryData.isEmpty()) {
         Map<ItemStack, Long> legacyItems = LegacyInventoryCodec.deserialize(LegacyInventoryCodec.parseJsonArray(inventoryData));
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
