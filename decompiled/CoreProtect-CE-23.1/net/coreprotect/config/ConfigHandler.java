package net.coreprotect.config;

import java.io.File;
import java.io.RandomAccessFile;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.coreprotect.bukkit.BukkitAdapter;
import net.coreprotect.consumer.Queue;
import net.coreprotect.database.Database;
import net.coreprotect.database.statement.UserStatement;
import net.coreprotect.hikari.HikariConfig;
import net.coreprotect.hikari.HikariDataSource;
import net.coreprotect.language.Phrase;
import net.coreprotect.listener.ListenerHandler;
import net.coreprotect.model.BlockGroup;
import net.coreprotect.paper.PaperAdapter;
import net.coreprotect.patch.Patch;
import net.coreprotect.spigot.SpigotAdapter;
import net.coreprotect.utility.Chat;
import net.coreprotect.utility.Color;
import net.coreprotect.utility.SystemUtils;
import net.coreprotect.utility.VersionUtils;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import oshi.hardware.CentralProcessor;

public class ConfigHandler extends Queue {
   public static int SERVER_VERSION = 0;
   public static final int EDITION_VERSION = 2;
   public static final String EDITION_BRANCH = VersionUtils.getBranch();
   public static final String EDITION_NAME = VersionUtils.getPluginName();
   public static final String COMMUNITY_EDITION = "Community Edition";
   public static final String JAVA_VERSION = "11.0";
   public static final String MINECRAFT_VERSION = "1.16";
   public static final String PATCH_VERSION = "23.1";
   public static final String LATEST_VERSION = "1.21.11";
   public static String path = "plugins/CoreProtect/";
   public static String sqlite = "database.db";
   public static String host = "127.0.0.1";
   public static int port = 3306;
   public static String database = "database";
   public static String username = "root";
   public static String password = "";
   public static String prefix = "co_";
   public static String prefixConfig = "co_";
   public static int maximumPoolSize = 10;
   public static HikariDataSource hikariDataSource = null;
   public static final CentralProcessor processorInfo = SystemUtils.getProcessorInfo();
   public static final boolean isSpigot = VersionUtils.isSpigot();
   public static final boolean isPaper = VersionUtils.isPaper();
   public static final boolean isFolia = VersionUtils.isFolia();
   public static volatile boolean serverRunning = false;
   public static volatile boolean converterRunning = false;
   public static volatile boolean purgeRunning = false;
   public static volatile boolean migrationRunning = false;
   public static volatile boolean pauseConsumer = false;
   public static volatile boolean worldeditEnabled = false;
   public static volatile boolean databaseReachable = true;
   public static volatile int worldId = 0;
   public static volatile int materialId = 0;
   public static volatile int blockdataId = 0;
   public static volatile int entityId = 0;
   public static volatile int artId = 0;
   public static Map<String, Integer> worlds = syncMap();
   public static Map<Integer, String> worldsReversed = syncMap();
   public static Map<String, Integer> materials = syncMap();
   public static Map<Integer, String> materialsReversed = syncMap();
   public static Map<String, Integer> blockdata = syncMap();
   public static Map<Integer, String> blockdataReversed = syncMap();
   public static Map<String, Integer> entities = syncMap();
   public static Map<Integer, String> entitiesReversed = syncMap();
   public static Map<String, Integer> art = syncMap();
   public static Map<Integer, String> artReversed = syncMap();
   public static Map<String, int[]> rollbackHash = syncMap();
   public static Map<String, Boolean> inspecting = syncMap();
   public static Map<String, Boolean> blacklist = syncMap();
   public static Map<String, Integer> loggingChest = syncMap();
   public static Map<String, Integer> loggingItem = syncMap();
   public static ConcurrentHashMap<String, List<Object>> transactingChest = new ConcurrentHashMap<>();
   public static ConcurrentHashMap<String, List<ItemStack[]>> oldContainer = new ConcurrentHashMap<>();
   public static ConcurrentHashMap<String, List<ItemStack>> itemsPickup = new ConcurrentHashMap<>();
   public static ConcurrentHashMap<String, List<ItemStack>> itemsDrop = new ConcurrentHashMap<>();
   public static ConcurrentHashMap<String, List<ItemStack>> itemsThrown = new ConcurrentHashMap<>();
   public static ConcurrentHashMap<String, List<ItemStack>> itemsShot = new ConcurrentHashMap<>();
   public static ConcurrentHashMap<String, List<ItemStack>> itemsBreak = new ConcurrentHashMap<>();
   public static ConcurrentHashMap<String, List<ItemStack>> itemsDestroy = new ConcurrentHashMap<>();
   public static ConcurrentHashMap<String, List<ItemStack>> itemsCreate = new ConcurrentHashMap<>();
   public static ConcurrentHashMap<String, List<ItemStack>> itemsSell = new ConcurrentHashMap<>();
   public static ConcurrentHashMap<String, List<ItemStack>> itemsBuy = new ConcurrentHashMap<>();
   public static ConcurrentHashMap<String, Object[]> hopperAbort = new ConcurrentHashMap<>();
   public static ConcurrentHashMap<String, Object[]> hopperSuccess = new ConcurrentHashMap<>();
   public static ConcurrentHashMap<String, ConcurrentHashMap<String, Long>> dispenserNoChange = new ConcurrentHashMap<>();
   public static ConcurrentHashMap<String, Object[]> dispenserPending = new ConcurrentHashMap<>();
   public static Map<String, List<ItemStack[]>> forceContainer = syncMap();
   public static Map<String, Integer> lookupType = syncMap();
   public static Map<String, Object[]> lookupThrottle = syncMap();
   public static Map<String, Object[]> teleportThrottle = syncMap();
   public static Map<String, Integer> lookupPage = syncMap();
   public static Map<String, String> lookupCommand = syncMap();
   public static Map<String, List<Object>> lookupBlist = syncMap();
   public static Map<String, Map<Object, Boolean>> lookupElist = syncMap();
   public static Map<String, List<String>> lookupEUserlist = syncMap();
   public static Map<String, List<String>> lookupUlist = syncMap();
   public static Map<String, List<Integer>> lookupAlist = syncMap();
   public static Map<String, Integer[]> lookupRadius = syncMap();
   public static Map<String, String> lookupTime = syncMap();
   public static Map<String, Long[]> lookupRows = syncMap();
   public static Map<String, String> uuidCache = syncMap();
   public static Map<String, String> uuidCacheReversed = syncMap();
   public static Map<String, Integer> playerIdCache = syncMap();
   public static Map<Integer, String> playerIdCacheReversed = syncMap();
   public static Map<String, List<Object>> lastRollback = syncMap();
   public static Map<String, Boolean> activeRollbacks = syncMap();
   public static Map<String, Object[]> entityBlockMapper = new ConcurrentHashMap<>();
   public static ConcurrentHashMap<Long, Long> populatedChunks = new ConcurrentHashMap<>();
   public static ConcurrentHashMap<String, String> language = new ConcurrentHashMap<>();
   public static List<String> databaseTables = new ArrayList<>();

   private static <K, V> Map<K, V> syncMap() {
      return Collections.synchronizedMap(new HashMap<>());
   }

   public static void checkPlayers(Connection connection) {
      playerIdCache.clear();
      playerIdCacheReversed.clear();

      for (Player player : Bukkit.getServer().getOnlinePlayers()) {
         if (playerIdCache.get(player.getName().toLowerCase(Locale.ROOT)) == null) {
            UserStatement.loadId(connection, player.getName(), player.getUniqueId().toString());
         }
      }
   }

   private static void loadBlacklist() {
      try {
         ConfigHandler.blacklist.clear();
         String blacklist = path + "blacklist.txt";
         boolean exists = new File(blacklist).exists();
         if (exists) {
            RandomAccessFile blfile = new RandomAccessFile(blacklist, "rw");
            long blc = blfile.length();
            if (blc > 0L) {
               while (blfile.getFilePointer() < blfile.length()) {
                  String blacklistUser = blfile.readLine().replaceAll(" ", "").toLowerCase(Locale.ROOT);
                  if (blacklistUser.length() > 0) {
                     ConfigHandler.blacklist.put(blacklistUser, true);
                  }
               }
            }

            blfile.close();
         }
      } catch (Exception var6) {
         var6.printStackTrace();
      }
   }

   private static void loadConfig() {
      try {
         Config.init();
         ConfigFile.init("language.yml");
         ConfigFile.init(".language");
         if (!Config.getGlobal().MYSQL) {
            prefixConfig = Config.getGlobal().PREFIX;
            Config.getGlobal().PREFIX = "co_";
         }

         host = Config.getGlobal().MYSQL_HOST;
         port = Config.getGlobal().MYSQL_PORT;
         database = Config.getGlobal().MYSQL_DATABASE;
         username = Config.getGlobal().MYSQL_USERNAME;
         password = Config.getGlobal().MYSQL_PASSWORD;
         maximumPoolSize = Config.getGlobal().MAXIMUM_POOL_SIZE;
         prefix = Config.getGlobal().PREFIX;
         loadBlacklist();
      } catch (Exception var1) {
         var1.printStackTrace();
      }
   }

   public static void loadDatabase() {
      Database.closeConnection();
      if (!Config.getGlobal().MYSQL) {
         try {
            File tempFile = File.createTempFile("CoreProtect_" + System.currentTimeMillis(), ".tmp");
            tempFile.setExecutable(true);
            boolean canExecute = false;

            try {
               canExecute = tempFile.canExecute();
            } catch (Exception var5) {
            }

            if (!canExecute) {
               File tempFolder = new File("cache");
               boolean exists = tempFolder.exists();
               if (!exists) {
                  tempFolder.mkdir();
               }

               System.setProperty("java.io.tmpdir", "cache");
            }

            tempFile.delete();
            Class.forName("org.sqlite.JDBC");
         } catch (Exception var6) {
            var6.printStackTrace();
         }
      } else {
         HikariConfig config = new HikariConfig();

         try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            config.setDriverClassName("com.mysql.cj.jdbc.Driver");
         } catch (Exception var4) {
            config.setDriverClassName("com.mysql.jdbc.Driver");
         }

         config.setJdbcUrl("jdbc:mysql://" + host + ":" + port + "/" + database);
         config.setUsername(username);
         config.setPassword(password);
         config.setMaximumPoolSize(maximumPoolSize);
         config.setMaxLifetime(60000L);
         config.addDataSourceProperty("characterEncoding", "UTF-8");
         config.addDataSourceProperty("connectionTimeout", "10000");
         config.addDataSourceProperty("cachePrepStmts", "true");
         config.addDataSourceProperty("prepStmtCacheSize", "250");
         config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
         config.addDataSourceProperty("useServerPrepStmts", "true");
         config.addDataSourceProperty("useLocalSessionState", "true");
         config.addDataSourceProperty("rewriteBatchedStatements", "true");
         config.addDataSourceProperty("cacheServerConfiguration", "true");
         config.addDataSourceProperty("maintainTimeStats", "false");
         config.addDataSourceProperty("allowPublicKeyRetrieval", "true");
         config.addDataSourceProperty("useSSL", Config.getGlobal().ENABLE_SSL);
         hikariDataSource = new HikariDataSource(config);
      }

      Database.createDatabaseTables(prefix, false, null, Config.getGlobal().MYSQL, false);
   }

   public static void loadMaterials(Statement statement) {
      try {
         String query = "SELECT id,material FROM " + prefix + "material_map";
         ResultSet rs = statement.executeQuery(query);
         materials.clear();
         materialsReversed.clear();
         materialId = 0;

         while (rs.next()) {
            int id = rs.getInt("id");
            String material = rs.getString("material");
            materials.put(material, id);
            materialsReversed.put(id, material);
            if (id > materialId) {
               materialId = id;
            }
         }

         rs.close();
      } catch (Exception var5) {
         var5.printStackTrace();
      }
   }

   public static void loadBlockdata(Statement statement) {
      try {
         String query = "SELECT id,data FROM " + prefix + "blockdata_map";
         ResultSet rs = statement.executeQuery(query);
         blockdata.clear();
         blockdataReversed.clear();
         blockdataId = 0;

         while (rs.next()) {
            int id = rs.getInt("id");
            String data = rs.getString("data");
            blockdata.put(data, id);
            blockdataReversed.put(id, data);
            if (id > blockdataId) {
               blockdataId = id;
            }
         }

         rs.close();
      } catch (Exception var5) {
         var5.printStackTrace();
      }
   }

   public static void loadArt(Statement statement) {
      try {
         String query = "SELECT id,art FROM " + prefix + "art_map";
         ResultSet rs = statement.executeQuery(query);
         ConfigHandler.art.clear();
         artReversed.clear();
         artId = 0;

         while (rs.next()) {
            int id = rs.getInt("id");
            String art = rs.getString("art");
            ConfigHandler.art.put(art, id);
            artReversed.put(id, art);
            if (id > artId) {
               artId = id;
            }
         }

         rs.close();
      } catch (Exception var5) {
         var5.printStackTrace();
      }
   }

   public static void loadEntities(Statement statement) {
      try {
         String query = "SELECT id,entity FROM " + prefix + "entity_map";
         ResultSet rs = statement.executeQuery(query);
         entities.clear();
         entitiesReversed.clear();
         entityId = 0;

         while (rs.next()) {
            int id = rs.getInt("id");
            String entity = rs.getString("entity");
            entities.put(entity, id);
            entitiesReversed.put(id, entity);
            if (id > entityId) {
               entityId = id;
            }
         }

         rs.close();
      } catch (Exception var5) {
         var5.printStackTrace();
      }
   }

   public static void loadTypes(Statement statement) {
      loadMaterials(statement);
      loadBlockdata(statement);
      loadArt(statement);
      loadEntities(statement);
   }

   public static int reloadAndGetId(ConfigHandler.CacheType type, String name) {
      if (Config.getGlobal().DATABASE_LOCK) {
         return -1;
      } else {
         try (Connection connection = Database.getConnection(true)) {
            if (connection != null) {
               Statement statement = connection.createStatement();
               switch (type) {
                  case MATERIALS:
                     loadMaterials(statement);
                     statement.close();
                     return materials.getOrDefault(name, -1);
                  case BLOCKDATA:
                     loadBlockdata(statement);
                     statement.close();
                     return blockdata.getOrDefault(name, -1);
                  case ART:
                     loadArt(statement);
                     statement.close();
                     return art.getOrDefault(name, -1);
                  case ENTITIES:
                     loadEntities(statement);
                     statement.close();
                     return entities.getOrDefault(name, -1);
                  case WORLDS:
                     loadWorlds(statement);
                     statement.close();
                     return worlds.getOrDefault(name, -1);
                  default:
                     statement.close();
                     return -1;
               }
            } else {
               return -1;
            }
         } catch (Exception var7) {
            var7.printStackTrace();
            return -1;
         }
      }
   }

   public static void loadWorlds(Statement statement) {
      try {
         String query = "SELECT id,world FROM " + prefix + "world";
         ResultSet rs = statement.executeQuery(query);
         ConfigHandler.worlds.clear();
         worldsReversed.clear();
         worldId = 0;

         while (rs.next()) {
            int id = rs.getInt("id");
            String world = rs.getString("world");
            ConfigHandler.worlds.put(world, id);
            worldsReversed.put(id, world);
            if (id > worldId) {
               worldId = id;
            }
         }

         rs.close();

         for (World world : Bukkit.getServer().getWorlds()) {
            String worldname = world.getName();
            if (ConfigHandler.worlds.get(worldname) == null) {
               int id = worldId + 1;
               ConfigHandler.worlds.put(worldname, id);
               worldsReversed.put(id, worldname);
               worldId = id;
               Queue.queueWorldInsert(id, worldname);
            }
         }
      } catch (Exception var8) {
         var8.printStackTrace();
      }
   }

   private static boolean checkDatabaseLock(Statement statement) {
      try {
         if (Config.getGlobal().DATABASE_LOCK) {
            boolean locked = true;
            boolean lockMessage = false;
            int unixtimestamp = (int)(System.currentTimeMillis() / 1000L);
            int waitTime = unixtimestamp + 15;

            while (locked) {
               locked = false;
               unixtimestamp = (int)(System.currentTimeMillis() / 1000L);
               int checkTime = unixtimestamp - 15;
               String query = "SELECT * FROM " + prefix + "database_lock WHERE rowid='1' AND status='1' AND time >= '" + checkTime + "' LIMIT 1";

               ResultSet rs;
               for (rs = statement.executeQuery(query); rs.next(); locked = true) {
                  if (unixtimestamp >= waitTime) {
                     Chat.sendConsoleMessage(Color.RED + "[CoreProtect] " + Phrase.build(Phrase.DATABASE_LOCKED_2));
                     Chat.sendConsoleMessage(Color.GREY + "[CoreProtect] " + Phrase.build(Phrase.DATABASE_LOCKED_3));
                     Chat.sendConsoleMessage(Color.GREY + "[CoreProtect] " + Phrase.build(Phrase.DATABASE_LOCKED_4));
                     return false;
                  }

                  if (!lockMessage) {
                     Chat.sendConsoleMessage("[CoreProtect] " + Phrase.build(Phrase.DATABASE_LOCKED_1));
                     lockMessage = true;
                  }

                  Thread.sleep(1000L);
               }

               rs.close();
            }
         }
      } catch (Exception var8) {
         var8.printStackTrace();
      }

      return true;
   }

   public static boolean performInitialization(boolean startup) {
      try {
         BukkitAdapter.loadAdapter();
         SpigotAdapter.loadAdapter();
         PaperAdapter.loadAdapter();
         BlockGroup.initialize();
         loadConfig();
         loadDatabase();
         if (startup) {
            ListenerHandler.registerNetworking();
         }
      } catch (Exception var7) {
         var7.printStackTrace();
      }

      try {
         boolean var5;
         try (Connection connection = Database.getConnection(true, 0)) {
            Statement statement = connection.createStatement();
            checkPlayers(connection);
            loadWorlds(statement);
            loadTypes(statement);
            if (VersionUtils.checkWorldEdit()) {
               PluginManager pluginManager = Bukkit.getServer().getPluginManager();
               Plugin worldEditPlugin = pluginManager.getPlugin("WorldEdit");
               if (worldEditPlugin != null && worldEditPlugin.isEnabled()) {
                  VersionUtils.loadWorldEdit();
               }
            } else if (worldeditEnabled) {
               VersionUtils.unloadWorldEdit();
            }

            serverRunning = true;
            boolean validVersion = Patch.versionCheck(statement);
            boolean databaseLock = true;
            if (startup) {
               databaseLock = checkDatabaseLock(statement);
            }

            statement.close();
            var5 = validVersion && databaseLock;
         }

         return var5;
      } catch (Exception var9) {
         var9.printStackTrace();
         return false;
      }
   }

   public static void performDisable() {
      try {
         Database.closeConnection();
         ListenerHandler.unregisterNetworking();
      } catch (Exception var1) {
         var1.printStackTrace();
      }
   }

   public static enum CacheType {
      MATERIALS,
      BLOCKDATA,
      ART,
      ENTITIES,
      WORLDS;
   }
}
