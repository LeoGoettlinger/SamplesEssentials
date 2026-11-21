package me.chancesd.pvpmanager.storage;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import me.chancesd.pvpmanager.PvPManager;
import me.chancesd.pvpmanager.player.CombatPlayer;
import me.chancesd.pvpmanager.sdutils.database.Database;
import me.chancesd.pvpmanager.sdutils.database.DatabaseConfigBuilder;
import me.chancesd.pvpmanager.sdutils.database.DatabaseFactory;
import me.chancesd.pvpmanager.sdutils.database.Table;
import me.chancesd.pvpmanager.sdutils.utils.Log;
import me.chancesd.pvpmanager.storage.converter.DisplayNameConverter;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.java.JavaPlugin;

public class SQLStorage implements Storage {
   private Table usersTable;
   private Table worldsTable;
   private final JavaPlugin plugin;
   private final ConfigurationSection dbConfigSection;
   private final Database database;

   public SQLStorage(PvPManager plugin) {
      this(plugin, null);
   }

   public SQLStorage(PvPManager plugin, DatabaseConfigBuilder.DatabaseType dbType) {
      this.plugin = plugin;
      this.dbConfigSection = plugin.getConfig().getConfigurationSection("Database");
      File sqliteFile = new File(plugin.getDataFolder(), "database.db");
      DatabaseConfigBuilder config;
      if (dbType == null) {
         config = new DatabaseConfigBuilder(this.dbConfigSection, sqliteFile);
      } else {
         config = new DatabaseConfigBuilder(this.dbConfigSection, sqliteFile, dbType);
      }

      this.database = this.setupDatabase(config);
   }

   private Database setupDatabase(DatabaseConfigBuilder config) {
      Database db = new DatabaseFactory(this.plugin).registerConverter(new DisplayNameConverter()).getDatabase(config);
      this.usersTable = new Table(
         "pmr_users",
         "uuid CHAR(36) NOT NULL PRIMARY KEY, name VARCHAR(16), displayname VARCHAR(255), kills INT UNSIGNED DEFAULT 0, deaths INT UNSIGNED DEFAULT 0, pvpstatus BOOLEAN DEFAULT 1, toggletime BIGINT DEFAULT 0, newbie BOOLEAN DEFAULT 0, newbie_timeleft BIGINT DEFAULT 0, last_seen BIGINT DEFAULT 0"
      );
      db.registerTable(this.usersTable);
      this.worldsTable = new Table(
         "pmr_worlds", "uuid CHAR(36) NOT NULL, name VARCHAR(255) NOT NULL PRIMARY KEY, pvp BOOLEAN DEFAULT 1, forcepvp VARCHAR(16) DEFAULT 'NONE'"
      );
      db.registerTable(this.worldsTable);
      Log.infoColor(ChatColor.GREEN + "Connected to " + ChatColor.AQUA + config.getType() + ChatColor.GREEN + " database successfully");
      Log.infoColor(ChatColor.GREEN + "Players stored: " + ChatColor.GOLD + db.executeCountQuery(this.usersTable, ""));
      return db;
   }

   @Override
   public boolean userExists(UUID uuid) {
      return this.database.contains(this.usersTable, "uuid", uuid.toString());
   }

   @Override
   public Map<String, Object> getUserData(UUID uuid) {
      return this.database.getRow(this.usersTable, "uuid", uuid.toString());
   }

   @Override
   public void removeUserData(UUID uuid) {
      this.database.remove(this.usersTable, "uuid", uuid.toString());
   }

   @Override
   public boolean saveUserData(UUID uuid, Map<String, Object> userData) {
      return this.userExists(uuid)
         ? this.database.updateValues(this.usersTable, "uuid", uuid.toString(), userData.keySet(), userData.values())
         : this.database.insertColumns(this.usersTable, userData.keySet(), userData.values());
   }

   @Override
   public List<Map<String, Object>> getAllUserData() {
      return this.database.getAllRows(this.usersTable);
   }

   @Override
   public void saveUserDataBatch(Collection<CombatPlayer> players) {
      if (!players.isEmpty()) {
         List<String> columns = new ArrayList<>(players.iterator().next().exportPlayerData().toMap().keySet());
         Map<Object, Collection<Object>> indexToValues = new HashMap<>();

         for (CombatPlayer player : players) {
            indexToValues.put(player.getUUID().toString(), player.exportPlayerData().toMap().values());
         }

         this.database.updateValuesBatch(this.usersTable, "uuid", columns, indexToValues);
      }
   }

   @Override
   public void increment(String field, UUID uuid) {
      this.database.update(this.usersTable, "uuid", field, uuid, field, "+1");
   }

   @Override
   public Map<String, Object> getWorldData(String name) {
      return this.database.getRow(this.worldsTable, "name", name);
   }

   @Override
   public boolean saveWorldData(String name, Map<String, Object> worldData) {
      Map<String, Object> data = this.getWorldData(name);
      return data.isEmpty()
         ? this.database.insertColumns(this.worldsTable, worldData.keySet(), worldData.values())
         : this.database.updateValues(this.worldsTable, "name", name, worldData.keySet(), worldData.values());
   }

   @Override
   public void shutdown() {
      this.database.close();
      Log.infoColor(ChatColor.RED + "Closed " + this.getDatabaseType() + " storage");
   }

   @Override
   public DatabaseConfigBuilder.DatabaseType getDatabaseType() {
      return this.database.getDatabaseType();
   }
}
