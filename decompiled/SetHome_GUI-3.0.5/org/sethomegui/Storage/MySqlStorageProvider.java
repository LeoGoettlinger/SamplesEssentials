package org.sethomegui.Storage;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.sethomegui.SetHomeGUI;
import org.sethomegui.SetHomeGUI.libs.block.implementation.Section;
import org.sethomegui.SetHomeGUI.libs.hikari.HikariConfig;
import org.sethomegui.SetHomeGUI.libs.hikari.HikariDataSource;
import org.sethomegui.SetHomeGUI.libs.mysql.cj.jdbc.Driver;

public class MySqlStorageProvider implements StorageProvider {
   private final SetHomeGUI plugin;
   private final Section config;
   private final String prefix;
   private HikariDataSource dataSource;

   public MySqlStorageProvider(SetHomeGUI plugin, Section config) {
      this.plugin = plugin;
      this.config = config;
      this.prefix = this.sanitizePrefix(config.getString("table-prefix", "sethomegui_"));
   }

   private String sanitizePrefix(String raw) {
      if (raw == null) {
         return "sethomegui_";
      } else {
         String cleaned = raw.replaceAll("[^A-Za-z0-9_]", "");
         return cleaned.isEmpty() ? "sethomegui_" : cleaned;
      }
   }

   @Override
   public String getName() {
      return "MySQL/MariaDB ("
         + this.config.getString("host", "localhost")
         + ":"
         + this.config.getInt("port", 3306)
         + "/"
         + this.config.getString("database", "sethomegui")
         + ")";
   }

   @Override
   public void initialize() throws Exception {
      DriverManager.registerDriver(new Driver());
      String host = this.config.getString("host", "localhost");
      int port = this.config.getInt("port", 3306);
      String database = this.config.getString("database", "sethomegui");
      StringBuilder url = new StringBuilder("jdbc:mysql://").append(host).append(":").append(port).append("/").append(database);
      Section properties = this.config.getSection("properties");
      if (properties != null && !properties.getKeys().isEmpty()) {
         boolean first = true;

         for (Object keyObj : properties.getKeys()) {
            String key = String.valueOf(keyObj);
            url.append(first ? "?" : "&").append(key).append("=").append(properties.getString(key, ""));
            first = false;
         }
      }

      HikariConfig hikari = new HikariConfig();
      hikari.setPoolName("SetHomeGUI-MySQL");
      hikari.setJdbcUrl(url.toString());
      hikari.setDriverClassName(Driver.class.getName());
      hikari.setUsername(this.config.getString("username", "root"));
      hikari.setPassword(this.config.getString("password", ""));
      hikari.setMaximumPoolSize(Math.max(1, this.config.getInt("pool-size", 10)));
      hikari.setConnectionTimeout(Math.max(1000L, this.config.getLong("connection-timeout", 10000L)));
      hikari.setInitializationFailTimeout(-1L);
      this.dataSource = new HikariDataSource(hikari);

      try (Connection connection = this.dataSource.getConnection()) {
         this.createSchema(connection);
      }
   }

   private void createSchema(Connection connection) throws SQLException {
      try (Statement statement = connection.createStatement()) {
         statement.executeUpdate(
            "CREATE TABLE IF NOT EXISTS `"
               + this.prefix
               + "players` (`uuid` CHAR(36) NOT NULL,`extra_data` MEDIUMTEXT NULL,`updated_at` BIGINT NOT NULL,PRIMARY KEY (`uuid`)) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci"
         );
         statement.executeUpdate(
            "CREATE TABLE IF NOT EXISTS `"
               + this.prefix
               + "homes` (`id` INT NOT NULL AUTO_INCREMENT,`owner_uuid` CHAR(36) NOT NULL,`home_name` VARCHAR(128) NOT NULL,`ordinal` INT NOT NULL DEFAULT 0,`world` VARCHAR(128) NOT NULL,`x` DOUBLE NOT NULL,`y` DOUBLE NOT NULL,`z` DOUBLE NOT NULL,`yaw` DOUBLE NOT NULL DEFAULT 0,`pitch` DOUBLE NOT NULL DEFAULT 0,`icon` VARCHAR(128) NULL,`access_mode` VARCHAR(32) NOT NULL DEFAULT 'nobody',PRIMARY KEY (`id`),UNIQUE KEY `uq_owner_home` (`owner_uuid`, `home_name`),KEY `idx_owner` (`owner_uuid`)) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci"
         );
         statement.executeUpdate(
            "CREATE TABLE IF NOT EXISTS `"
               + this.prefix
               + "home_access` (`home_id` INT NOT NULL,`friend_uuid` CHAR(36) NOT NULL,PRIMARY KEY (`home_id`, `friend_uuid`),CONSTRAINT `fk_access_home` FOREIGN KEY (`home_id`) REFERENCES `"
               + this.prefix
               + "homes` (`id`) ON DELETE CASCADE) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci"
         );
         statement.executeUpdate(
            "CREATE TABLE IF NOT EXISTS `"
               + this.prefix
               + "friends` (`player_uuid` CHAR(36) NOT NULL,`friend_uuid` CHAR(36) NOT NULL,PRIMARY KEY (`player_uuid`, `friend_uuid`),KEY `idx_player` (`player_uuid`)) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci"
         );
      }
   }

   @Override
   public void shutdown() {
      if (this.dataSource != null && !this.dataSource.isClosed()) {
         this.dataSource.close();
      }
   }

   @Override
   public PlayerRecord load(UUID uuid) throws SQLException {
      String id = uuid.toString();
      PlayerRecord record = new PlayerRecord(uuid);
      boolean exists = false;

      try (Connection connection = this.dataSource.getConnection()) {
         try (PreparedStatement ps = connection.prepareStatement("SELECT `extra_data` FROM `" + this.prefix + "players` WHERE `uuid` = ?")) {
            ps.setString(1, id);

            try (ResultSet rs = ps.executeQuery()) {
               if (rs.next()) {
                  exists = true;
                  record.setExtraData(rs.getString("extra_data"));
               }
            }
         }

         List<HomeEntry> homes = new ArrayList<>();

         try (PreparedStatement ps = connection.prepareStatement(
               "SELECT `id`,`home_name`,`world`,`x`,`y`,`z`,`yaw`,`pitch`,`icon`,`access_mode` FROM `"
                  + this.prefix
                  + "homes` WHERE `owner_uuid` = ? ORDER BY `ordinal` ASC, `id` ASC"
            )) {
            ps.setString(1, id);

            try (ResultSet rsx = ps.executeQuery()) {
               while (rsx.next()) {
                  exists = true;
                  HomeEntry entry = new HomeEntry(rsx.getString("home_name"));
                  entry.setWorld(rsx.getString("world"));
                  entry.setX(rsx.getDouble("x"));
                  entry.setY(rsx.getDouble("y"));
                  entry.setZ(rsx.getDouble("z"));
                  entry.setYaw(rsx.getDouble("yaw"));
                  entry.setPitch(rsx.getDouble("pitch"));
                  entry.setIcon(rsx.getString("icon"));
                  entry.setAccess(rsx.getString("access_mode"));
                  entry.setAllowedFriends(this.loadAllowedFriends(connection, rsx.getInt("id")));
                  homes.add(entry);
               }
            }
         }

         record.setHomes(homes);
         List<String> friends = new ArrayList<>();

         try (PreparedStatement ps = connection.prepareStatement("SELECT `friend_uuid` FROM `" + this.prefix + "friends` WHERE `player_uuid` = ?")) {
            ps.setString(1, id);

            try (ResultSet rsx = ps.executeQuery()) {
               while (rsx.next()) {
                  exists = true;
                  friends.add(rsx.getString("friend_uuid"));
               }
            }
         }

         record.setFriends(friends);
      }

      return exists ? record : null;
   }

   private List<String> loadAllowedFriends(Connection connection, int homeId) throws SQLException {
      List<String> allowed = new ArrayList<>();

      try (PreparedStatement ps = connection.prepareStatement("SELECT `friend_uuid` FROM `" + this.prefix + "home_access` WHERE `home_id` = ?")) {
         ps.setInt(1, homeId);

         try (ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
               allowed.add(rs.getString("friend_uuid"));
            }
         }
      }

      return allowed;
   }

   @Override
   public void save(UUID uuid, PlayerRecord record) throws SQLException {
      String id = uuid.toString();

      try (Connection connection = this.dataSource.getConnection()) {
         boolean previousAutoCommit = connection.getAutoCommit();
         connection.setAutoCommit(false);

         try {
            try (PreparedStatement ps = connection.prepareStatement(
                  "INSERT INTO `"
                     + this.prefix
                     + "players` (`uuid`,`extra_data`,`updated_at`) VALUES (?,?,?) ON DUPLICATE KEY UPDATE `extra_data` = VALUES(`extra_data`), `updated_at` = VALUES(`updated_at`)"
               )) {
               ps.setString(1, id);
               ps.setString(2, record.getExtraData());
               ps.setLong(3, System.currentTimeMillis());
               ps.executeUpdate();
            }

            try (PreparedStatement ps = connection.prepareStatement("DELETE FROM `" + this.prefix + "homes` WHERE `owner_uuid` = ?")) {
               ps.setString(1, id);
               ps.executeUpdate();
            }

            int ordinal = 0;

            for (HomeEntry home : record.getHomes()) {
               int homeId = this.insertHome(connection, id, home, ordinal++);
               if (homeId > 0 && !home.getAllowedFriends().isEmpty()) {
                  this.insertAllowedFriends(connection, homeId, home.getAllowedFriends());
               }
            }

            try (PreparedStatement ps = connection.prepareStatement("DELETE FROM `" + this.prefix + "friends` WHERE `player_uuid` = ?")) {
               ps.setString(1, id);
               ps.executeUpdate();
            }

            if (!record.getFriends().isEmpty()) {
               try (PreparedStatement ps = connection.prepareStatement("INSERT INTO `" + this.prefix + "friends` (`player_uuid`,`friend_uuid`) VALUES (?,?)")) {
                  for (String friend : record.getFriends()) {
                     ps.setString(1, id);
                     ps.setString(2, friend);
                     ps.addBatch();
                  }

                  ps.executeBatch();
               }
            }

            connection.commit();
         } catch (SQLException var32) {
            connection.rollback();
            throw var32;
         } finally {
            connection.setAutoCommit(previousAutoCommit);
         }
      }
   }

   private int insertHome(Connection connection, String ownerUuid, HomeEntry home, int ordinal) throws SQLException {
      int var7;
      try (PreparedStatement ps = connection.prepareStatement(
            "INSERT INTO `"
               + this.prefix
               + "homes` (`owner_uuid`,`home_name`,`ordinal`,`world`,`x`,`y`,`z`,`yaw`,`pitch`,`icon`,`access_mode`) VALUES (?,?,?,?,?,?,?,?,?,?,?)",
            1
         )) {
         ps.setString(1, ownerUuid);
         ps.setString(2, home.getName());
         ps.setInt(3, ordinal);
         ps.setString(4, home.getWorld());
         ps.setDouble(5, home.getX());
         ps.setDouble(6, home.getY());
         ps.setDouble(7, home.getZ());
         ps.setDouble(8, home.getYaw());
         ps.setDouble(9, home.getPitch());
         ps.setString(10, home.getIcon());
         ps.setString(11, home.getAccess());
         ps.executeUpdate();

         try (ResultSet keys = ps.getGeneratedKeys()) {
            var7 = keys.next() ? keys.getInt(1) : -1;
         }
      }

      return var7;
   }

   private void insertAllowedFriends(Connection connection, int homeId, List<String> allowed) throws SQLException {
      try (PreparedStatement ps = connection.prepareStatement("INSERT IGNORE INTO `" + this.prefix + "home_access` (`home_id`,`friend_uuid`) VALUES (?,?)")) {
         for (String friend : allowed) {
            ps.setInt(1, homeId);
            ps.setString(2, friend);
            ps.addBatch();
         }

         ps.executeBatch();
      }
   }

   @Override
   public void delete(UUID uuid) throws SQLException {
      String id = uuid.toString();

      try (Connection connection = this.dataSource.getConnection()) {
         this.executeDelete(connection, "DELETE FROM `" + this.prefix + "homes` WHERE `owner_uuid` = ?", id);
         this.executeDelete(connection, "DELETE FROM `" + this.prefix + "friends` WHERE `player_uuid` = ?", id);
         this.executeDelete(connection, "DELETE FROM `" + this.prefix + "players` WHERE `uuid` = ?", id);
      }
   }

   private void executeDelete(Connection connection, String sql, String id) throws SQLException {
      try (PreparedStatement ps = connection.prepareStatement(sql)) {
         ps.setString(1, id);
         ps.executeUpdate();
      }
   }

   @Override
   public Map<UUID, Integer> countHomesByPlayer() throws SQLException {
      Map<UUID, Integer> counts = new HashMap<>();

      try (
         Connection connection = this.dataSource.getConnection();
         PreparedStatement ps = connection.prepareStatement("SELECT `owner_uuid`, COUNT(*) AS `total` FROM `" + this.prefix + "homes` GROUP BY `owner_uuid`");
         ResultSet rs = ps.executeQuery();
      ) {
         while (rs.next()) {
            try {
               counts.put(UUID.fromString(rs.getString("owner_uuid")), rs.getInt("total"));
            } catch (IllegalArgumentException var10) {
            }
         }
      }

      return counts;
   }

   @Override
   public Set<UUID> listPlayers() throws SQLException {
      Set<UUID> players = new LinkedHashSet<>();

      try (
         Connection connection = this.dataSource.getConnection();
         PreparedStatement ps = connection.prepareStatement("SELECT `uuid` FROM `" + this.prefix + "players`");
         ResultSet rs = ps.executeQuery();
      ) {
         while (rs.next()) {
            try {
               players.add(UUID.fromString(rs.getString("uuid")));
            } catch (IllegalArgumentException var10) {
               this.plugin.getLogger().warning("UUID invalido encontrado en la base de datos: " + rs.getString("uuid"));
            }
         }
      }

      return players;
   }
}
