package com.Sw3ys.bigenderchest.storage;

import com.Sw3ys.bigenderchest.BiggerEnderChest;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Base64;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;

public class MySqlChestStore implements ChestStore {
   private static final String DRIVER_CLASS = "com.mysql.cj.jdbc.Driver";
   private final BiggerEnderChest plugin;
   private final String host;
   private final int port;
   private final String database;
   private final String username;
   private final String password;
   private final boolean useSsl;
   private final String table;
   private final String url;
   private Driver driver;
   private final Properties props = new Properties();
   private ExecutorService io;
   private volatile boolean running;
   private Connection connection;
   private final Map<UUID, ChestStore.Stored> cache = new ConcurrentHashMap<>();

   public MySqlChestStore(BiggerEnderChest plugin) {
      this.plugin = plugin;
      ConfigurationSection cfg = plugin.getConfig().getConfigurationSection("mysql");
      if (cfg == null) {
         cfg = plugin.getConfig().createSection("mysql");
      }

      this.host = cfg.getString("host", "127.0.0.1");
      this.port = cfg.getInt("port", 3306);
      this.database = cfg.getString("database", "minecraft");
      this.username = cfg.getString("username", "root");
      this.password = cfg.getString("password", "");
      this.useSsl = cfg.getBoolean("useSSL", false);
      this.table = cfg.getString("table", "bigenderchest_chests").replaceAll("[^a-zA-Z0-9_]", "");
      this.url = "jdbc:mysql://"
         + this.host
         + ":"
         + this.port
         + "/"
         + this.database
         + "?useSSL="
         + this.useSsl
         + "&allowPublicKeyRetrieval="
         + !this.useSsl
         + "&characterEncoding=utf8&serverTimezone=UTC&autoReconnect=true&connectTimeout=5000";
      this.props.setProperty("user", this.username);
      this.props.setProperty("password", this.password);
   }

   @Override
   public void prepare() {
      try {
         Class<?> driverClass = Class.forName("com.mysql.cj.jdbc.Driver");
         this.driver = (Driver)driverClass.getDeclaredConstructor().newInstance();
      } catch (Exception var7) {
         throw new IllegalStateException("Bundled MySQL driver could not be loaded: " + var7.getMessage(), var7);
      }

      try (
         Connection c = this.driver.connect(this.url, this.props);
         Statement st = c.createStatement();
      ) {
         st.executeUpdate(this.createTableSql());
      } catch (SQLException var10) {
         throw new IllegalStateException(
            "Could not connect to MySQL at " + this.host + ":" + this.port + "/" + this.database + " (" + var10.getMessage() + ")", var10
         );
      }

      this.running = true;
      this.io = Executors.newSingleThreadExecutor(r -> {
         Thread t = new Thread(r, "BiggerEnderChest-MySQL");
         t.setDaemon(true);
         return t;
      });
      this.plugin.getLogger().info("MySQL storage ready at " + this.host + ":" + this.port + "/" + this.database + " (table " + this.table + ").");
   }

   private String createTableSql() {
      return "CREATE TABLE IF NOT EXISTS `"
         + this.table
         + "` (uuid CHAR(36) NOT NULL PRIMARY KEY, size INT NOT NULL, pending TINYINT(1) NOT NULL DEFAULT 0, items MEDIUMTEXT) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;";
   }

   private Connection conn() throws SQLException {
      if (this.connection == null || this.connection.isClosed() || !this.connection.isValid(2)) {
         if (this.connection != null) {
            try {
               this.connection.close();
            } catch (SQLException var2) {
            }
         }

         this.connection = this.driver.connect(this.url, this.props);
      }

      return this.connection;
   }

   @Override
   public void close() {
      this.running = false;
      if (this.io != null) {
         this.io.shutdown();

         try {
            if (!this.io.awaitTermination(3L, TimeUnit.SECONDS)) {
               this.io.shutdownNow();
            }
         } catch (InterruptedException var3) {
            this.io.shutdownNow();
            Thread.currentThread().interrupt();
         }
      }

      if (this.connection != null) {
         try {
            this.connection.close();
         } catch (SQLException var2) {
         }
      }
   }

   @Override
   public boolean isAsync() {
      return true;
   }

   @Override
   public boolean isAuthoritative() {
      return true;
   }

   @Override
   public boolean exists(UUID uuid) {
      return this.cache.containsKey(uuid);
   }

   @Override
   public ChestStore.Stored read(UUID uuid) {
      ChestStore.Stored cached = this.cache.get(uuid);
      return cached == null ? null : new ChestStore.Stored(cached.size, cached.pending, this.cloneItems(cached.items));
   }

   @Override
   public int getSavedSize(UUID uuid) {
      ChestStore.Stored cached = this.cache.get(uuid);
      return cached == null ? 27 : cached.size;
   }

   @Override
   public void loadAsync(UUID uuid, Consumer<Boolean> done) {
      this.io.submit(() -> done.accept(this.fetchIntoCache(uuid)));
   }

   private boolean fetchIntoCache(UUID uuid) {
      String sql = "SELECT size, pending, items FROM `" + this.table + "` WHERE uuid = ?";

      try {
         boolean var8;
         try (PreparedStatement ps = this.conn().prepareStatement(sql)) {
            ps.setString(1, uuid.toString());

            try (ResultSet rs = ps.executeQuery()) {
               if (!rs.next()) {
                  return false;
               }

               int size = Math.max(27, rs.getInt("size"));
               boolean pending = rs.getInt("pending") != 0;
               ItemStack[] items = this.decodeItems(rs.getString("items"), size);
               this.cache.put(uuid, new ChestStore.Stored(size, pending, items));
               var8 = true;
            }
         }

         return var8;
      } catch (SQLException var13) {
         this.plugin.getLogger().warning("MySQL load failed for " + uuid + ": " + var13.getMessage());
         return false;
      }
   }

   @Override
   public void write(UUID uuid, int size, boolean pending, ItemStack[] items) {
      this.cache.put(uuid, new ChestStore.Stored(size, pending, this.cloneItems(items)));
      String payload = this.encodeItems(size, items);
      this.io.submit(() -> this.upsert(uuid, size, pending, payload));
   }

   private void upsert(UUID uuid, int size, boolean pending, String items) {
      String sql = "INSERT INTO `"
         + this.table
         + "` (uuid, size, pending, items) VALUES (?, ?, ?, ?) ON DUPLICATE KEY UPDATE size = VALUES(size), pending = VALUES(pending), items = VALUES(items)";

      try (PreparedStatement ps = this.conn().prepareStatement(sql)) {
         ps.setString(1, uuid.toString());
         ps.setInt(2, size);
         ps.setInt(3, pending ? 1 : 0);
         ps.setString(4, items);
         ps.executeUpdate();
      } catch (SQLException var11) {
         this.plugin.getLogger().warning("MySQL save failed for " + uuid + ": " + var11.getMessage());
      }
   }

   @Override
   public void unload(UUID uuid) {
      this.cache.remove(uuid);
   }

   @Override
   public boolean importWrite(UUID uuid, int size, ItemStack[] items) {
      if (this.cache.containsKey(uuid)) {
         return false;
      } else {
         String payload = this.encodeItems(size, items);
         this.io.submit(() -> this.upsert(uuid, size, false, payload));
         return true;
      }
   }

   private String encodeItems(int size, ItemStack[] items) {
      StringBuilder sb = new StringBuilder();

      for (int i = 0; i < size; i++) {
         if (i > 0) {
            sb.append('\n');
         }

         ItemStack item = items != null && i < items.length ? items[i] : null;
         if (item != null) {
            sb.append(Base64.getEncoder().encodeToString(item.serializeAsBytes()));
         }
      }

      return sb.toString();
   }

   private ItemStack[] decodeItems(String data, int size) {
      ItemStack[] items = new ItemStack[size];
      if (data != null && !data.isEmpty()) {
         String[] lines = data.split("\n", -1);

         for (int i = 0; i < size && i < lines.length; i++) {
            String encoded = lines[i];
            if (encoded != null && !encoded.isEmpty()) {
               try {
                  items[i] = ItemStack.deserializeBytes(Base64.getDecoder().decode(encoded));
               } catch (Exception var8) {
                  items[i] = null;
               }
            }
         }

         return items;
      } else {
         return items;
      }
   }

   private ItemStack[] cloneItems(ItemStack[] items) {
      if (items == null) {
         return new ItemStack[0];
      } else {
         ItemStack[] copy = new ItemStack[items.length];

         for (int i = 0; i < items.length; i++) {
            copy[i] = items[i] == null ? null : items[i].clone();
         }

         return copy;
      }
   }
}
