package github.nighter.smartspawner.spawner.data.database;

import github.nighter.smartspawner.Scheduler;
import github.nighter.smartspawner.SmartSpawner;
import github.nighter.smartspawner.commands.list.gui.CrossServerSpawnerData;
import github.nighter.smartspawner.spawner.config.SpawnerDisplayConfigurator;
import github.nighter.smartspawner.spawner.data.storage.SpawnerInventoryCodec;
import github.nighter.smartspawner.spawner.data.storage.SpawnerStorage;
import github.nighter.smartspawner.spawner.data.storage.StorageMode;
import github.nighter.smartspawner.spawner.properties.ItemSignature;
import github.nighter.smartspawner.spawner.properties.SpawnerData;
import github.nighter.smartspawner.spawner.properties.VirtualInventory;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;

public class SpawnerDatabaseHandler implements SpawnerStorage {
   private static final String DEFAULT_AUTOSAVE_INTERVAL = "3m";
   private static final long DEFAULT_AUTOSAVE_TICKS = 3600L;
   private static final long MIN_AUTOSAVE_TICKS = 600L;
   private final SmartSpawner plugin;
   private final Logger logger;
   private final DatabaseManager databaseManager;
   private final String serverName;
   private final Set<String> dirtySpawners = ConcurrentHashMap.newKeySet();
   private final Set<String> deletedSpawners = ConcurrentHashMap.newKeySet();
   private volatile boolean isSaving = false;
   private Scheduler.Task saveTask = null;
   private final Map<String, String> locationCache = new ConcurrentHashMap<>();
   private static final String SELECT_COLUMNS = "spawner_id, world, loc_x, loc_y, loc_z,\nentity_type, itemspawner_type, config_name, stack_size, max_stack_size,\nactive, stop, activation_range, delay, last_spawn_time, min_mobs, max_mobs,\nmax_loot_slots, is_at_capacity, exp, max_stored_exp,\nlast_interacted_player, preferred_sort_item, filtered_items, storage_items\n";
   private static final String UPSERT_SQL_MYSQL = "INSERT INTO %s (\n    spawner_id, world, loc_x, loc_y, loc_z, chunk_x, chunk_z,\n    entity_type, itemspawner_type, stack_size, max_stack_size,\n    active, stop, activation_range, delay, last_spawn_time, min_mobs, max_mobs,\n    max_loot_slots, is_at_capacity, total_items, exp, max_stored_exp,\n    last_interacted_player, preferred_sort_item, filtered_items, storage_items, config_name\n) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)\nON DUPLICATE KEY UPDATE\n    world = VALUES(world),\n    loc_x = VALUES(loc_x),\n    loc_y = VALUES(loc_y),\n    loc_z = VALUES(loc_z),\n    chunk_x = VALUES(chunk_x),\n    chunk_z = VALUES(chunk_z),\n    entity_type = VALUES(entity_type),\n    itemspawner_type = VALUES(itemspawner_type),\n    stack_size = VALUES(stack_size),\n    max_stack_size = VALUES(max_stack_size),\n    active = VALUES(active),\n    stop = VALUES(stop),\n    activation_range = VALUES(activation_range),\n    delay = VALUES(delay),\n    last_spawn_time = VALUES(last_spawn_time),\n    min_mobs = VALUES(min_mobs),\n    max_mobs = VALUES(max_mobs),\n    max_loot_slots = VALUES(max_loot_slots),\n    is_at_capacity = VALUES(is_at_capacity),\n    total_items = VALUES(total_items),\n    exp = VALUES(exp),\n    max_stored_exp = VALUES(max_stored_exp),\n    last_interacted_player = VALUES(last_interacted_player),\n    preferred_sort_item = VALUES(preferred_sort_item),\n    filtered_items = VALUES(filtered_items),\n    storage_items = VALUES(storage_items),\n    config_name = VALUES(config_name)\n";
   private static final String UPSERT_SQL_SQLITE = "INSERT INTO %s (\n    spawner_id, world, loc_x, loc_y, loc_z, chunk_x, chunk_z,\n    entity_type, itemspawner_type, stack_size, max_stack_size,\n    active, stop, activation_range, delay, last_spawn_time, min_mobs, max_mobs,\n    max_loot_slots, is_at_capacity, total_items, exp, max_stored_exp,\n    last_interacted_player, preferred_sort_item, filtered_items, storage_items, config_name\n) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)\nON CONFLICT(spawner_id) DO UPDATE SET\n    world = excluded.world,\n    loc_x = excluded.loc_x,\n    loc_y = excluded.loc_y,\n    loc_z = excluded.loc_z,\n    chunk_x = excluded.chunk_x,\n    chunk_z = excluded.chunk_z,\n    entity_type = excluded.entity_type,\n    itemspawner_type = excluded.itemspawner_type,\n    stack_size = excluded.stack_size,\n    max_stack_size = excluded.max_stack_size,\n    active = excluded.active,\n    stop = excluded.stop,\n    activation_range = excluded.activation_range,\n    delay = excluded.delay,\n    last_spawn_time = excluded.last_spawn_time,\n    min_mobs = excluded.min_mobs,\n    max_mobs = excluded.max_mobs,\n    max_loot_slots = excluded.max_loot_slots,\n    is_at_capacity = excluded.is_at_capacity,\n    total_items = excluded.total_items,\n    exp = excluded.exp,\n    max_stored_exp = excluded.max_stored_exp,\n    last_interacted_player = excluded.last_interacted_player,\n    preferred_sort_item = excluded.preferred_sort_item,\n    filtered_items = excluded.filtered_items,\n    storage_items = excluded.storage_items,\n    config_name = excluded.config_name\n";
   private static final String CROSS_SERVER_COLUMNS = "spawner_id, world, loc_x, loc_y, loc_z,\nentity_type, stack_size, stop, last_interacted_player,\nexp, total_items\n";
   private final String tableSpawners;
   private final String selectAllSql;
   private final String selectOneSql;
   private final String selectLocationSql;
   private final String deleteSql;
   private final String deleteLocationConflictSql;

   public SpawnerDatabaseHandler(SmartSpawner plugin, DatabaseManager databaseManager) {
      this.plugin = plugin;
      this.logger = plugin.getLogger();
      this.databaseManager = databaseManager;
      this.serverName = databaseManager.getServerName();
      this.tableSpawners = databaseManager.getTableSpawners();
      this.selectAllSql = "SELECT spawner_id, world, loc_x, loc_y, loc_z,\nentity_type, itemspawner_type, config_name, stack_size, max_stack_size,\nactive, stop, activation_range, delay, last_spawn_time, min_mobs, max_mobs,\nmax_loot_slots, is_at_capacity, exp, max_stored_exp,\nlast_interacted_player, preferred_sort_item, filtered_items, storage_items\n FROM "
         + this.tableSpawners
         + ";";
      this.selectOneSql = "SELECT spawner_id, world, loc_x, loc_y, loc_z,\nentity_type, itemspawner_type, config_name, stack_size, max_stack_size,\nactive, stop, activation_range, delay, last_spawn_time, min_mobs, max_mobs,\nmax_loot_slots, is_at_capacity, exp, max_stored_exp,\nlast_interacted_player, preferred_sort_item, filtered_items, storage_items\n FROM "
         + this.tableSpawners
         + " WHERE spawner_id = ?";
      this.selectLocationSql = "SELECT world, loc_x, loc_y, loc_z FROM " + this.tableSpawners + " WHERE spawner_id = ?";
      this.deleteSql = "DELETE FROM " + this.tableSpawners + " WHERE spawner_id = ?";
      this.deleteLocationConflictSql = "DELETE FROM " + this.tableSpawners + " WHERE world = ? AND loc_x = ? AND loc_y = ? AND loc_z = ? AND spawner_id <> ?";
   }

   @Override
   public boolean initialize() {
      if (!this.databaseManager.isActive()) {
         this.logger.severe("Database manager is not active, cannot initialize SpawnerDatabaseHandler");
         return false;
      } else {
         this.startSaveTask();
         return true;
      }
   }

   private void startSaveTask() {
      long intervalTicks = Math.max(600L, this.configuredAutosaveTicks());
      if (this.saveTask != null) {
         this.saveTask.cancel();
         this.saveTask = null;
      }

      this.saveTask = Scheduler.runTaskTimerAsync(() -> this.flushChanges(), intervalTicks, intervalTicks);
   }

   private long configuredAutosaveTicks() {
      String configured = this.plugin.getConfig().getString("database.autosave-interval", "3m");
      long ticks = this.plugin.getTimeFormatter().parseTimeToTicks(configured, -1L);
      if (ticks > 0L) {
         return ticks;
      } else {
         this.logger.warning("Could not read database.autosave-interval ('" + configured + "'), using 3m instead.");
         return 3600L;
      }
   }

   @Override
   public void reloadSettings() {
      this.startSaveTask();
   }

   @Override
   public void markSpawnerModified(String spawnerId) {
      if (spawnerId != null) {
         this.dirtySpawners.add(spawnerId);
         this.deletedSpawners.remove(spawnerId);
      }
   }

   @Override
   public void markSpawnerDeleted(String spawnerId) {
      if (spawnerId != null) {
         this.deletedSpawners.add(spawnerId);
         this.dirtySpawners.remove(spawnerId);
         this.locationCache.remove(spawnerId);
      }
   }

   @Override
   public void queueSpawnerForSaving(String spawnerId) {
      this.markSpawnerModified(spawnerId);
   }

   @Override
   public void flushChanges() {
      if (!this.dirtySpawners.isEmpty() || !this.deletedSpawners.isEmpty()) {
         if (!this.isSaving) {
            this.isSaving = true;
            Scheduler.runTaskAsync(() -> {
               try {
                  if (!this.deletedSpawners.isEmpty()) {
                     Set<String> toDelete = new HashSet<>(this.deletedSpawners);
                     this.deletedSpawners.removeAll(toDelete);
                     this.deleteSpawnerBatch(toDelete);
                  }

                  if (!this.dirtySpawners.isEmpty()) {
                     Set<String> toUpdate = new HashSet<>(this.dirtySpawners);
                     this.dirtySpawners.removeAll(toUpdate);
                     this.saveSpawnerBatch(toUpdate);
                  }
               } catch (Exception var5) {
                  this.logger.log(Level.SEVERE, "Error during database flush", (Throwable)var5);
               } finally {
                  this.isSaving = false;
               }
            });
         }
      }
   }

   private void saveSpawnerBatch(Set<String> spawnerIds) {
      if (!spawnerIds.isEmpty()) {
         String upsertSql = (this.databaseManager.getStorageMode() == StorageMode.SQLITE
               ? "INSERT INTO %s (\n    spawner_id, world, loc_x, loc_y, loc_z, chunk_x, chunk_z,\n    entity_type, itemspawner_type, stack_size, max_stack_size,\n    active, stop, activation_range, delay, last_spawn_time, min_mobs, max_mobs,\n    max_loot_slots, is_at_capacity, total_items, exp, max_stored_exp,\n    last_interacted_player, preferred_sort_item, filtered_items, storage_items, config_name\n) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)\nON CONFLICT(spawner_id) DO UPDATE SET\n    world = excluded.world,\n    loc_x = excluded.loc_x,\n    loc_y = excluded.loc_y,\n    loc_z = excluded.loc_z,\n    chunk_x = excluded.chunk_x,\n    chunk_z = excluded.chunk_z,\n    entity_type = excluded.entity_type,\n    itemspawner_type = excluded.itemspawner_type,\n    stack_size = excluded.stack_size,\n    max_stack_size = excluded.max_stack_size,\n    active = excluded.active,\n    stop = excluded.stop,\n    activation_range = excluded.activation_range,\n    delay = excluded.delay,\n    last_spawn_time = excluded.last_spawn_time,\n    min_mobs = excluded.min_mobs,\n    max_mobs = excluded.max_mobs,\n    max_loot_slots = excluded.max_loot_slots,\n    is_at_capacity = excluded.is_at_capacity,\n    total_items = excluded.total_items,\n    exp = excluded.exp,\n    max_stored_exp = excluded.max_stored_exp,\n    last_interacted_player = excluded.last_interacted_player,\n    preferred_sort_item = excluded.preferred_sort_item,\n    filtered_items = excluded.filtered_items,\n    storage_items = excluded.storage_items,\n    config_name = excluded.config_name\n"
               : "INSERT INTO %s (\n    spawner_id, world, loc_x, loc_y, loc_z, chunk_x, chunk_z,\n    entity_type, itemspawner_type, stack_size, max_stack_size,\n    active, stop, activation_range, delay, last_spawn_time, min_mobs, max_mobs,\n    max_loot_slots, is_at_capacity, total_items, exp, max_stored_exp,\n    last_interacted_player, preferred_sort_item, filtered_items, storage_items, config_name\n) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)\nON DUPLICATE KEY UPDATE\n    world = VALUES(world),\n    loc_x = VALUES(loc_x),\n    loc_y = VALUES(loc_y),\n    loc_z = VALUES(loc_z),\n    chunk_x = VALUES(chunk_x),\n    chunk_z = VALUES(chunk_z),\n    entity_type = VALUES(entity_type),\n    itemspawner_type = VALUES(itemspawner_type),\n    stack_size = VALUES(stack_size),\n    max_stack_size = VALUES(max_stack_size),\n    active = VALUES(active),\n    stop = VALUES(stop),\n    activation_range = VALUES(activation_range),\n    delay = VALUES(delay),\n    last_spawn_time = VALUES(last_spawn_time),\n    min_mobs = VALUES(min_mobs),\n    max_mobs = VALUES(max_mobs),\n    max_loot_slots = VALUES(max_loot_slots),\n    is_at_capacity = VALUES(is_at_capacity),\n    total_items = VALUES(total_items),\n    exp = VALUES(exp),\n    max_stored_exp = VALUES(max_stored_exp),\n    last_interacted_player = VALUES(last_interacted_player),\n    preferred_sort_item = VALUES(preferred_sort_item),\n    filtered_items = VALUES(filtered_items),\n    storage_items = VALUES(storage_items),\n    config_name = VALUES(config_name)\n")
            .formatted(this.tableSpawners);

         try (
            Connection conn = this.databaseManager.getConnection();
            PreparedStatement stmt = conn.prepareStatement(upsertSql);
         ) {
            conn.setAutoCommit(false);

            for (String spawnerId : spawnerIds) {
               SpawnerData spawner = this.plugin.getSpawnerManager().getSpawnerById(spawnerId);
               if (spawner != null && this.setSpawnerParameters(stmt, spawner)) {
                  stmt.addBatch();
               }
            }

            stmt.executeBatch();
            conn.commit();
         } catch (SQLException var12) {
            this.logger.log(Level.WARNING, "Spawner batch save failed, retrying row by row", (Throwable)var12);
            this.saveSpawnersIndividually(spawnerIds);
         }
      }
   }

   private void saveSpawnersIndividually(Set<String> spawnerIds) {
      String upsertSql = (this.databaseManager.getStorageMode() == StorageMode.SQLITE
            ? "INSERT INTO %s (\n    spawner_id, world, loc_x, loc_y, loc_z, chunk_x, chunk_z,\n    entity_type, itemspawner_type, stack_size, max_stack_size,\n    active, stop, activation_range, delay, last_spawn_time, min_mobs, max_mobs,\n    max_loot_slots, is_at_capacity, total_items, exp, max_stored_exp,\n    last_interacted_player, preferred_sort_item, filtered_items, storage_items, config_name\n) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)\nON CONFLICT(spawner_id) DO UPDATE SET\n    world = excluded.world,\n    loc_x = excluded.loc_x,\n    loc_y = excluded.loc_y,\n    loc_z = excluded.loc_z,\n    chunk_x = excluded.chunk_x,\n    chunk_z = excluded.chunk_z,\n    entity_type = excluded.entity_type,\n    itemspawner_type = excluded.itemspawner_type,\n    stack_size = excluded.stack_size,\n    max_stack_size = excluded.max_stack_size,\n    active = excluded.active,\n    stop = excluded.stop,\n    activation_range = excluded.activation_range,\n    delay = excluded.delay,\n    last_spawn_time = excluded.last_spawn_time,\n    min_mobs = excluded.min_mobs,\n    max_mobs = excluded.max_mobs,\n    max_loot_slots = excluded.max_loot_slots,\n    is_at_capacity = excluded.is_at_capacity,\n    total_items = excluded.total_items,\n    exp = excluded.exp,\n    max_stored_exp = excluded.max_stored_exp,\n    last_interacted_player = excluded.last_interacted_player,\n    preferred_sort_item = excluded.preferred_sort_item,\n    filtered_items = excluded.filtered_items,\n    storage_items = excluded.storage_items,\n    config_name = excluded.config_name\n"
            : "INSERT INTO %s (\n    spawner_id, world, loc_x, loc_y, loc_z, chunk_x, chunk_z,\n    entity_type, itemspawner_type, stack_size, max_stack_size,\n    active, stop, activation_range, delay, last_spawn_time, min_mobs, max_mobs,\n    max_loot_slots, is_at_capacity, total_items, exp, max_stored_exp,\n    last_interacted_player, preferred_sort_item, filtered_items, storage_items, config_name\n) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)\nON DUPLICATE KEY UPDATE\n    world = VALUES(world),\n    loc_x = VALUES(loc_x),\n    loc_y = VALUES(loc_y),\n    loc_z = VALUES(loc_z),\n    chunk_x = VALUES(chunk_x),\n    chunk_z = VALUES(chunk_z),\n    entity_type = VALUES(entity_type),\n    itemspawner_type = VALUES(itemspawner_type),\n    stack_size = VALUES(stack_size),\n    max_stack_size = VALUES(max_stack_size),\n    active = VALUES(active),\n    stop = VALUES(stop),\n    activation_range = VALUES(activation_range),\n    delay = VALUES(delay),\n    last_spawn_time = VALUES(last_spawn_time),\n    min_mobs = VALUES(min_mobs),\n    max_mobs = VALUES(max_mobs),\n    max_loot_slots = VALUES(max_loot_slots),\n    is_at_capacity = VALUES(is_at_capacity),\n    total_items = VALUES(total_items),\n    exp = VALUES(exp),\n    max_stored_exp = VALUES(max_stored_exp),\n    last_interacted_player = VALUES(last_interacted_player),\n    preferred_sort_item = VALUES(preferred_sort_item),\n    filtered_items = VALUES(filtered_items),\n    storage_items = VALUES(storage_items),\n    config_name = VALUES(config_name)\n")
         .formatted(this.tableSpawners);

      try (
         Connection conn = this.databaseManager.getConnection();
         PreparedStatement clearStmt = conn.prepareStatement(this.deleteLocationConflictSql);
         PreparedStatement stmt = conn.prepareStatement(upsertSql);
      ) {
         conn.setAutoCommit(false);

         for (String spawnerId : spawnerIds) {
            SpawnerData spawner = this.plugin.getSpawnerManager().getSpawnerById(spawnerId);
            if (spawner != null) {
               try {
                  Location loc = spawner.getSpawnerLocation();
                  clearStmt.setString(1, loc.getWorld().getName());
                  clearStmt.setInt(2, loc.getBlockX());
                  clearStmt.setInt(3, loc.getBlockY());
                  clearStmt.setInt(4, loc.getBlockZ());
                  clearStmt.setString(5, spawnerId);
                  clearStmt.executeUpdate();
                  if (this.setSpawnerParameters(stmt, spawner)) {
                     stmt.executeUpdate();
                  }

                  conn.commit();
               } catch (SQLException var15) {
                  this.logger.log(Level.SEVERE, "Failed to save spawner " + spawnerId + ", re-queuing for the next flush", (Throwable)var15);

                  try {
                     conn.rollback();
                  } catch (SQLException var14) {
                  }

                  this.dirtySpawners.add(spawnerId);
               }
            }
         }
      } catch (SQLException var19) {
         this.logger.log(Level.SEVERE, "Error saving spawners individually to database", (Throwable)var19);
         this.dirtySpawners.addAll(spawnerIds);
      }
   }

   private void deleteSpawnerBatch(Set<String> spawnerIds) {
      if (!spawnerIds.isEmpty()) {
         try (
            Connection conn = this.databaseManager.getConnection();
            PreparedStatement stmt = conn.prepareStatement(this.deleteSql);
         ) {
            conn.setAutoCommit(false);

            for (String spawnerId : spawnerIds) {
               stmt.setString(1, spawnerId);
               stmt.addBatch();
            }

            stmt.executeBatch();
            conn.commit();
         } catch (SQLException var10) {
            this.logger.log(Level.SEVERE, "Error deleting spawner batch from database", (Throwable)var10);
            this.deletedSpawners.addAll(spawnerIds);
         }
      }
   }

   private boolean setSpawnerParameters(PreparedStatement stmt, SpawnerData spawner) throws SQLException {
      Location loc = spawner.getSpawnerLocation();
      VirtualInventory virtualInv = spawner.getVirtualInventory();
      byte[] items;
      long totalItems;
      if (virtualInv == null) {
         items = null;
         totalItems = 0L;
      } else {
         Map<ItemSignature, Long> consolidated = virtualInv.getConsolidatedItems();

         try {
            items = SpawnerInventoryCodec.encode(consolidated);
         } catch (IOException var10) {
            this.logger
               .log(
                  Level.WARNING,
                  "Could not encode inventory for spawner " + spawner.getSpawnerId() + ", skipping this save so the stored items are not lost",
                  (Throwable)var10
               );
            this.dirtySpawners.add(spawner.getSpawnerId());
            return false;
         }

         totalItems = SpawnerInventoryCodec.totalItems(consolidated);
      }

      stmt.setString(1, spawner.getSpawnerId());
      stmt.setString(2, loc.getWorld().getName());
      stmt.setInt(3, loc.getBlockX());
      stmt.setInt(4, loc.getBlockY());
      stmt.setInt(5, loc.getBlockZ());
      stmt.setInt(6, loc.getBlockX() >> 4);
      stmt.setInt(7, loc.getBlockZ() >> 4);
      stmt.setString(8, spawner.getEntityType().name());
      stmt.setString(9, spawner.isItemSpawner() ? spawner.getSpawnedItemMaterial().name() : null);
      stmt.setInt(10, spawner.getStackSize());
      stmt.setInt(11, spawner.getMaxStackSize());
      stmt.setBoolean(12, spawner.getSpawnerActive());
      stmt.setBoolean(13, spawner.getSpawnerStop().get());
      stmt.setInt(14, spawner.getSpawnerRange());
      stmt.setLong(15, spawner.getSpawnDelay());
      stmt.setLong(16, spawner.getLastSpawnTime());
      stmt.setInt(17, spawner.getMinMobs());
      stmt.setInt(18, spawner.getMaxMobs());
      stmt.setInt(19, spawner.getMaxSpawnerLootSlots());
      stmt.setBoolean(20, spawner.getIsAtCapacity());
      stmt.setLong(21, totalItems);
      stmt.setLong(22, Math.max(0L, spawner.getSpawnerExp()));
      stmt.setLong(23, spawner.getMaxStoredExp());
      stmt.setString(24, spawner.getLastInteractedPlayer());
      stmt.setString(25, spawner.getPreferredSortItem() != null ? spawner.getPreferredSortItem().name() : null);
      stmt.setString(26, this.serializeFilteredItems(spawner.getFilteredItems()));
      stmt.setBytes(27, items);
      stmt.setString(28, spawner.getConfigName());
      return true;
   }

   @Override
   public Map<String, SpawnerData> loadAllSpawnersRaw() {
      Map<String, SpawnerData> loadedSpawners = new HashMap<>();

      try (
         Connection conn = this.databaseManager.getConnection();
         PreparedStatement stmt = conn.prepareStatement(this.selectAllSql);
         ResultSet rs = stmt.executeQuery();
      ) {
         while (rs.next()) {
            String spawnerId = rs.getString("spawner_id");

            try {
               SpawnerData spawner = this.loadSpawnerFromResultSet(rs);
               loadedSpawners.put(spawnerId, spawner);
               if (spawner == null) {
                  String worldName = rs.getString("world");
                  int x = rs.getInt("loc_x");
                  int y = rs.getInt("loc_y");
                  int z = rs.getInt("loc_z");
                  this.locationCache.put(spawnerId, String.format("%s,%d,%d,%d", worldName, x, y, z));
               }
            } catch (Exception var14) {
               loadedSpawners.put(spawnerId, null);
            }
         }
      } catch (SQLException var18) {
         this.logger.log(Level.SEVERE, "Error loading spawners from database", (Throwable)var18);
      }

      return loadedSpawners;
   }

   @Override
   public SpawnerData loadSpecificSpawner(String spawnerId) {
      try (
         Connection conn = this.databaseManager.getConnection();
         PreparedStatement stmt = conn.prepareStatement(this.selectOneSql);
      ) {
         stmt.setString(1, spawnerId);

         try (ResultSet rs = stmt.executeQuery()) {
            return rs.next() ? this.loadSpawnerFromResultSet(rs) : null;
         }
      } catch (SQLException var13) {
         this.logger.log(Level.SEVERE, "Error loading spawner " + spawnerId + " from database", (Throwable)var13);
         return null;
      }
   }

   @Override
   public String getRawLocationString(String spawnerId) {
      String cached = this.locationCache.get(spawnerId);
      if (cached != null) {
         return cached;
      } else {
         try (
            Connection conn = this.databaseManager.getConnection();
            PreparedStatement stmt = conn.prepareStatement(this.selectLocationSql);
         ) {
            stmt.setString(1, spawnerId);

            try (ResultSet rs = stmt.executeQuery()) {
               if (rs.next()) {
                  String worldName = rs.getString("world");
                  int x = rs.getInt("loc_x");
                  int y = rs.getInt("loc_y");
                  int z = rs.getInt("loc_z");
                  String location = String.format("%s,%d,%d,%d", worldName, x, y, z);
                  this.locationCache.put(spawnerId, location);
                  return location;
               } else {
                  return null;
               }
            }
         } catch (SQLException var18) {
            this.logger.log(Level.SEVERE, "Error getting location for spawner " + spawnerId, (Throwable)var18);
            return null;
         }
      }
   }

   private SpawnerData loadSpawnerFromResultSet(ResultSet rs) throws SQLException {
      String spawnerId = rs.getString("spawner_id");
      String worldName = rs.getString("world");
      int x = rs.getInt("loc_x");
      int y = rs.getInt("loc_y");
      int z = rs.getInt("loc_z");
      World world = Bukkit.getWorld(worldName);
      if (world == null) {
         return null;
      } else {
         Location location = new Location(world, x, y, z);
         String entityTypeStr = rs.getString("entity_type");

         EntityType entityType;
         try {
            entityType = EntityType.valueOf(entityTypeStr);
         } catch (IllegalArgumentException var23) {
            this.logger.severe("Invalid entity type for spawner " + spawnerId + ": " + entityTypeStr);
            return null;
         }

         String itemMaterialStr = rs.getString("itemspawner_type");
         String configName = rs.getString("config_name");
         SpawnerData spawner;
         if (entityType == EntityType.ITEM && itemMaterialStr != null) {
            try {
               Material itemMaterial = Material.valueOf(itemMaterialStr);
               spawner = new SpawnerData(spawnerId, location, itemMaterial, configName, this.plugin);
            } catch (IllegalArgumentException var22) {
               this.logger.severe("Invalid item spawner material for spawner " + spawnerId + ": " + itemMaterialStr);
               return null;
            }
         } else {
            spawner = new SpawnerData(spawnerId, location, entityType, configName, this.plugin);
         }

         spawner.setSpawnerExpData(rs.getLong("exp"));
         spawner.setSpawnerActive(rs.getBoolean("active"));
         spawner.setSpawnerRange(rs.getInt("activation_range"));
         spawner.getSpawnerStop().set(rs.getBoolean("stop"));
         spawner.setSpawnDelay(Math.max(1L, rs.getLong("delay")));
         spawner.setMaxSpawnerLootSlots(rs.getInt("max_loot_slots"));
         spawner.setMaxStoredExp(rs.getLong("max_stored_exp"));
         spawner.setMinMobs(rs.getInt("min_mobs"));
         spawner.setMaxMobs(rs.getInt("max_mobs"));
         spawner.setMaxStackSize(rs.getInt("max_stack_size"));
         spawner.setStackSize(rs.getInt("stack_size"), false);
         spawner.setLastSpawnTime(rs.getLong("last_spawn_time"));
         spawner.setIsAtCapacity(rs.getBoolean("is_at_capacity"));
         spawner.setLastInteractedPlayer(rs.getString("last_interacted_player"));
         String preferredSortItemStr = rs.getString("preferred_sort_item");
         if (preferredSortItemStr != null && !preferredSortItemStr.isEmpty()) {
            try {
               Material preferredSortItem = Material.valueOf(preferredSortItemStr);
               spawner.setPreferredSortItem(preferredSortItem);
            } catch (IllegalArgumentException var21) {
               this.logger.warning("Invalid preferred sort item for spawner " + spawnerId + ": " + preferredSortItemStr);
            }
         }

         String filteredItemsStr = rs.getString("filtered_items");
         if (filteredItemsStr != null && !filteredItemsStr.isEmpty()) {
            this.deserializeFilteredItems(filteredItemsStr, spawner.getFilteredItems());
         }

         byte[] itemsBlob = rs.getBytes("storage_items");
         VirtualInventory virtualInv = new VirtualInventory(spawner.getMaxSpawnerLootSlots());
         if (itemsBlob != null && itemsBlob.length > 0) {
            try {
               Map<ItemStack, Long> items = SpawnerInventoryCodec.decode(itemsBlob);

               for (Entry<ItemStack, Long> entry : items.entrySet()) {
                  virtualInv.addConsolidatedItem(entry.getKey(), entry.getValue());
               }
            } catch (Exception var24) {
               this.logger.warning("Error loading inventory for spawner " + spawnerId + ": " + var24.getMessage());
            }
         }

         spawner.setVirtualInventory(virtualInv);
         spawner.markSellValueDirty();
         if (spawner.getPreferredSortItem() != null) {
            virtualInv.sortItems(spawner.getPreferredSortItem());
         }

         Scheduler.runLocationTask(location, () -> {
            Block block = location.getBlock();
            if (block.getType() == Material.SPAWNER && block.getState(false) instanceof CreatureSpawner cs) {
               if (spawner.isItemSpawner()) {
                  SpawnerDisplayConfigurator.applyItem(this.plugin, cs, spawner.getConfigName(), spawner.getSpawnedItemMaterial());
               } else {
                  SpawnerDisplayConfigurator.applyMob(this.plugin, cs, spawner.getConfigName(), spawner.getEntityType());
               }

               cs.update(true, false);
            }
         });
         return spawner;
      }
   }

   @Override
   public void shutdown() {
      if (this.saveTask != null) {
         this.saveTask.cancel();
         this.saveTask = null;
      }

      if (!this.dirtySpawners.isEmpty() || !this.deletedSpawners.isEmpty()) {
         try {
            this.isSaving = true;
            this.logger.info("Saving " + this.dirtySpawners.size() + " spawners to database on shutdown...");
            if (!this.deletedSpawners.isEmpty()) {
               this.deleteSpawnerBatch(new HashSet<>(this.deletedSpawners));
            }

            if (!this.dirtySpawners.isEmpty()) {
               this.saveSpawnerBatch(new HashSet<>(this.dirtySpawners));
            }

            this.dirtySpawners.clear();
            this.deletedSpawners.clear();
            this.logger.info("Database shutdown save completed.");
         } catch (Exception var5) {
            this.logger.log(Level.SEVERE, "Error during database shutdown flush", (Throwable)var5);
         } finally {
            this.isSaving = false;
         }
      }

      this.locationCache.clear();
   }

   private String serializeFilteredItems(Set<Material> filteredItems) {
      return filteredItems != null && !filteredItems.isEmpty() ? filteredItems.stream().map(Enum::name).collect(Collectors.joining(",")) : null;
   }

   private void deserializeFilteredItems(String data, Set<Material> filteredItems) {
      if (data != null && !data.isEmpty()) {
         String[] materialNames = data.split(",");

         for (String materialName : materialNames) {
            try {
               Material material = Material.valueOf(materialName.trim());
               filteredItems.add(material);
            } catch (IllegalArgumentException var9) {
               this.logger.warning("Invalid material in filtered items: " + materialName);
            }
         }
      }
   }

   public String getServerName() {
      return this.serverName;
   }

   private String tableFor(String targetServer) {
      try {
         return this.databaseManager.listServerTables().getOrDefault(targetServer, this.tableSpawners);
      } catch (SQLException var3) {
         this.logger.log(Level.WARNING, "Could not list the spawner tables, using this server's", (Throwable)var3);
         return this.tableSpawners;
      }
   }

   public void getDistinctServerNamesAsync(Consumer<List<String>> callback) {
      Scheduler.runTaskAsync(() -> {
         List<String> servers = new ArrayList<>();

         try {
            servers.addAll(this.databaseManager.listServerTables().keySet());
            Collections.sort(servers);
         } catch (SQLException var4) {
            this.logger.log(Level.SEVERE, "Error listing the spawner tables in the database", (Throwable)var4);
         }

         Scheduler.runTask(() -> callback.accept(servers));
      });
   }

   public void getWorldsForServerAsync(String targetServer, Consumer<Map<String, SpawnerDatabaseHandler.WorldSpawnerStats>> callback) {
      Scheduler.runTaskAsync(() -> {
         Map<String, SpawnerDatabaseHandler.WorldSpawnerStats> worlds = new LinkedHashMap<>();
         String table = this.tableFor(targetServer);
         String sql = "SELECT world, COUNT(*) AS total, COALESCE(SUM(stack_size), 0) AS total_stacked FROM " + table + " GROUP BY world ORDER BY world";

         try (
            Connection conn = this.databaseManager.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();
         ) {
            while (rs.next()) {
               worlds.put(rs.getString("world"), new SpawnerDatabaseHandler.WorldSpawnerStats(rs.getInt("total"), rs.getInt("total_stacked")));
            }
         } catch (SQLException var17) {
            this.logger.log(Level.SEVERE, "Error fetching worlds for server " + targetServer, (Throwable)var17);
         }

         Scheduler.runTask(() -> callback.accept(worlds));
      });
   }

   public void getTotalStacksForWorldAsync(String targetServer, String worldName, Consumer<Integer> callback) {
      Scheduler.runTaskAsync(() -> {
         int total = 0;
         String sql = "SELECT SUM(stack_size) as total FROM " + this.tableFor(targetServer) + " WHERE world = ?";

         try (
            Connection conn = this.databaseManager.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql);
         ) {
            stmt.setString(1, worldName);

            try (ResultSet rs = stmt.executeQuery()) {
               if (rs.next()) {
                  total = rs.getInt("total");
               }
            }
         } catch (SQLException var17) {
            this.logger.log(Level.SEVERE, "Error fetching stack total for " + targetServer + "/" + worldName, (Throwable)var17);
         }

         int finalTotal = total;
         Scheduler.runTask(() -> callback.accept(finalTotal));
      });
   }

   public void getCrossServerSpawnersAsync(String targetServer, String worldName, Consumer<List<CrossServerSpawnerData>> callback) {
      Scheduler.runTaskAsync(
         () -> {
            List<CrossServerSpawnerData> spawners = new ArrayList<>();
            String sql = "SELECT spawner_id, world, loc_x, loc_y, loc_z,\nentity_type, stack_size, stop, last_interacted_player,\nexp, total_items\n FROM "
               + this.tableFor(targetServer)
               + " WHERE world = ? ORDER BY stack_size DESC";

            try (
               Connection conn = this.databaseManager.getConnection();
               PreparedStatement stmt = conn.prepareStatement(sql);
            ) {
               stmt.setString(1, worldName);

               try (ResultSet rs = stmt.executeQuery()) {
                  while (rs.next()) {
                     String spawnerId = rs.getString("spawner_id");
                     String world = rs.getString("world");
                     int x = rs.getInt("loc_x");
                     int y = rs.getInt("loc_y");
                     int z = rs.getInt("loc_z");

                     EntityType entityType;
                     try {
                        entityType = EntityType.valueOf(rs.getString("entity_type"));
                     } catch (IllegalArgumentException var26) {
                        entityType = EntityType.PIG;
                     }

                     int stackSize = rs.getInt("stack_size");
                     boolean active = !rs.getBoolean("stop");
                     String lastPlayer = rs.getString("last_interacted_player");
                     long storedExp = rs.getLong("exp");
                     long totalItems = rs.getLong("total_items");
                     spawners.add(
                        new CrossServerSpawnerData(spawnerId, targetServer, world, x, y, z, entityType, stackSize, active, lastPlayer, storedExp, totalItems)
                     );
                  }
               }
            } catch (SQLException var30) {
               this.logger.log(Level.SEVERE, "Error fetching spawners for " + targetServer + "/" + worldName, (Throwable)var30);
            }

            Scheduler.runTask(() -> callback.accept(spawners));
         }
      );
   }

   public void getSpawnerCountForServerAsync(String targetServer, Consumer<Integer> callback) {
      Scheduler.runTaskAsync(() -> {
         int count = 0;
         String sql = "SELECT COUNT(*) as count FROM " + this.tableFor(targetServer);

         try (
            Connection conn = this.databaseManager.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();
         ) {
            if (rs.next()) {
               count = rs.getInt("count");
            }
         } catch (SQLException var16) {
            this.logger.log(Level.SEVERE, "Error fetching spawner count for " + targetServer, (Throwable)var16);
         }

         int finalCount = count;
         Scheduler.runTask(() -> callback.accept(finalCount));
      });
   }

   public void getCrossServerSpawnersAsync(String targetServer, String worldName, String filter, String sort, Consumer<List<CrossServerSpawnerData>> callback) {
      Scheduler.runTaskAsync(
         () -> {
            List<CrossServerSpawnerData> spawners = new ArrayList<>();
            StringBuilder sql = new StringBuilder(
               "SELECT spawner_id, world, loc_x, loc_y, loc_z,\nentity_type, stack_size, stop, last_interacted_player,\nexp, total_items\n FROM "
                  + this.tableFor(targetServer)
                  + " WHERE world = ?"
            );
            if ("ACTIVE".equalsIgnoreCase(filter)) {
               sql.append(" AND stop = FALSE");
            } else if ("INACTIVE".equalsIgnoreCase(filter)) {
               sql.append(" AND stop = TRUE");
            }

            if ("STACK_SIZE_ASC".equalsIgnoreCase(sort)) {
               sql.append(" ORDER BY stack_size ASC");
            } else if ("STACK_SIZE_DESC".equalsIgnoreCase(sort)) {
               sql.append(" ORDER BY stack_size DESC");
            } else {
               sql.append(" ORDER BY spawner_id ASC");
            }

            try (
               Connection conn = this.databaseManager.getConnection();
               PreparedStatement stmt = conn.prepareStatement(sql.toString());
            ) {
               stmt.setString(1, worldName);

               try (ResultSet rs = stmt.executeQuery()) {
                  while (rs.next()) {
                     String spawnerId = rs.getString("spawner_id");
                     String world = rs.getString("world");
                     int x = rs.getInt("loc_x");
                     int y = rs.getInt("loc_y");
                     int z = rs.getInt("loc_z");

                     EntityType entityType;
                     try {
                        entityType = EntityType.valueOf(rs.getString("entity_type"));
                     } catch (IllegalArgumentException var28) {
                        entityType = EntityType.PIG;
                     }

                     int stackSize = rs.getInt("stack_size");
                     boolean active = !rs.getBoolean("stop");
                     String lastPlayer = rs.getString("last_interacted_player");
                     long storedExp = rs.getLong("exp");
                     long totalItems = rs.getLong("total_items");
                     spawners.add(
                        new CrossServerSpawnerData(spawnerId, targetServer, world, x, y, z, entityType, stackSize, active, lastPlayer, storedExp, totalItems)
                     );
                  }
               }
            } catch (SQLException var32) {
               this.logger.log(Level.SEVERE, "Error fetching spawners for " + targetServer + "/" + worldName, (Throwable)var32);
            }

            Scheduler.runTask(() -> callback.accept(spawners));
         }
      );
   }

   public record WorldSpawnerStats(int total, int totalStacked) {
   }
}
