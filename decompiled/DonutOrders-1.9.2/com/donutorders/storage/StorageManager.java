package com.donutorders.storage;

import com.donutorders.DonutOrders;
import com.donutorders.libs.hikari.HikariConfig;
import com.donutorders.libs.hikari.HikariDataSource;
import com.donutorders.model.Order;
import com.donutorders.model.OrderStatus;
import com.donutorders.scheduler.FoliaScheduler;
import com.donutorders.util.ItemUtils;
import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;

public class StorageManager {
   private static final Logger LOG = DonutOrders.getInstance().getLogger();
   private final HikariDataSource dataSource;
   private final boolean mysql;
   private final int syncIntervalSeconds;
   private final AtomicLong lastSyncedAt = new AtomicLong(0L);
   private final ConcurrentHashMap<UUID, Order> orderCache = new ConcurrentHashMap<>();
   private final ConcurrentHashMap<UUID, List<UUID>> playerOrderIndex = new ConcurrentHashMap<>();

   public StorageManager(File dataFolder) throws SQLException {
      FileConfiguration config = DonutOrders.getInstance().getConfig();
      String dbType = config.getString("database.type", "sqlite");
      this.mysql = "mysql".equalsIgnoreCase(dbType);
      this.syncIntervalSeconds = config.getInt("database.sync-interval-seconds", 5);
      HikariConfig cfg = new HikariConfig();
      if (this.mysql) {
         String host = config.getString("database.mysql.host", "localhost");
         int port = config.getInt("database.mysql.port", 3306);
         String database = config.getString("database.mysql.database", "donutorders");
         String username = config.getString("database.mysql.username", "donutorders");
         String password = config.getString("database.mysql.password", "");
         int poolSize = config.getInt("database.mysql.pool-size", 10);
         cfg.setDriverClassName("com.donutorders.libs.mysql.cj.jdbc.Driver");
         cfg.setJdbcUrl("jdbc:mysql://" + host + ":" + port + "/" + database + "?useSSL=false&allowPublicKeyRetrieval=true&characterEncoding=utf8");
         cfg.setUsername(username);
         cfg.setPassword(password);
         cfg.setMaximumPoolSize(Math.max(1, poolSize));
         cfg.setMinimumIdle(Math.min(2, poolSize));
         cfg.setConnectionTimeout(30000L);
         cfg.setPoolName("DonutOrders-MySQL");
      } else {
         File dbFile = new File(dataFolder, config.getString("database.filename", "donutorders.db"));
         cfg.setDriverClassName("org.sqlite.JDBC");
         cfg.setJdbcUrl("jdbc:sqlite:" + dbFile.getAbsolutePath());
         cfg.setMaximumPoolSize(1);
         cfg.setMinimumIdle(1);
         cfg.setConnectionTimeout(30000L);
         cfg.setIdleTimeout(0L);
         cfg.setMaxLifetime(0L);
         cfg.setPoolName("DonutOrders-SQLite");
         cfg.setConnectionInitSql("PRAGMA journal_mode=WAL; PRAGMA busy_timeout=30000; PRAGMA synchronous=NORMAL;");
      }

      this.dataSource = new HikariDataSource(cfg);
      this.createTables();
   }

   private void createTables() throws SQLException {
      try (
         Connection conn = this.dataSource.getConnection();
         Statement stmt = conn.createStatement();
      ) {
         if (this.mysql) {
            stmt.executeUpdate(
               "CREATE TABLE IF NOT EXISTS orders (  order_id        VARCHAR(36) PRIMARY KEY,  buyer_uuid      VARCHAR(36) NOT NULL,  buyer_name      VARCHAR(64) NOT NULL,  item_data       TEXT NOT NULL,  amount_req      INT NOT NULL,  amount_filled   INT NOT NULL,  price_per_item  DOUBLE NOT NULL,  remaining_funds DOUBLE NOT NULL,  created_at      BIGINT NOT NULL,  expires_at      BIGINT NOT NULL,  status          VARCHAR(16) NOT NULL,  claimed_at      BIGINT DEFAULT 0,  claimed_by      VARCHAR(36) DEFAULT NULL,  updated_at      BIGINT NOT NULL DEFAULT 0)"
            );
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS order_stash (  order_id   VARCHAR(36) PRIMARY KEY,  stash_data TEXT NOT NULL)");
         } else {
            stmt.executeUpdate(
               "CREATE TABLE IF NOT EXISTS orders (  order_id        TEXT PRIMARY KEY,  buyer_uuid      TEXT NOT NULL,  buyer_name      TEXT NOT NULL,  item_data       TEXT NOT NULL,  amount_req      INTEGER NOT NULL,  amount_filled   INTEGER NOT NULL,  price_per_item  REAL NOT NULL,  remaining_funds REAL NOT NULL,  created_at      INTEGER NOT NULL,  expires_at      INTEGER NOT NULL,  status          TEXT NOT NULL,  claimed_at      INTEGER DEFAULT 0,  claimed_by      TEXT DEFAULT NULL,  updated_at      INTEGER NOT NULL DEFAULT 0)"
            );
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS order_stash (  order_id   TEXT PRIMARY KEY,  stash_data TEXT NOT NULL)");
         }

         try {
            stmt.executeUpdate("ALTER TABLE orders ADD COLUMN claimed_at " + (this.mysql ? "BIGINT DEFAULT 0" : "INTEGER DEFAULT 0"));
         } catch (SQLException var9) {
         }

         try {
            stmt.executeUpdate("ALTER TABLE orders ADD COLUMN claimed_by " + (this.mysql ? "VARCHAR(36) DEFAULT NULL" : "TEXT DEFAULT NULL"));
         } catch (SQLException var8) {
         }

         try {
            stmt.executeUpdate("ALTER TABLE orders ADD COLUMN updated_at " + (this.mysql ? "BIGINT NOT NULL DEFAULT 0" : "INTEGER NOT NULL DEFAULT 0"));
         } catch (SQLException var7) {
         }
      }
   }

   public void startSyncTask() {
      if (this.mysql && this.syncIntervalSeconds > 0) {
         long periodTicks = this.syncIntervalSeconds * 20L;
         FoliaScheduler.runGlobalRepeating(this::pollRemoteChanges, periodTicks, periodTicks);
         LOG.info("[DonutOrders] MySQL cross-server sync enabled (every " + this.syncIntervalSeconds + "s).");
      }
   }

   private void pollRemoteChanges() {
      FoliaScheduler.runAsync(() -> {
         long since = this.lastSyncedAt.get();
         String sql = "SELECT * FROM orders WHERE updated_at > ? ORDER BY updated_at ASC";

         try (
            Connection conn = this.dataSource.getConnection();
            PreparedStatement ps = conn.prepareStatement("SELECT * FROM orders WHERE updated_at > ? ORDER BY updated_at ASC");
         ) {
            ps.setLong(1, since);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
               Order order = this.rowToOrder(rs);
               if (order != null) {
                  this.mergeRemoteOrder(order);
                  this.lastSyncedAt.updateAndGet(prev -> Math.max(prev, order.getUpdatedAt()));
               }
            }
         } catch (SQLException var12) {
            LOG.log(Level.WARNING, "Failed to poll remote order changes", (Throwable)var12);
         }
      });
   }

   public void refreshOrderFromDb(UUID orderId, Consumer<Order> callback) {
      FoliaScheduler.runAsync(() -> callback.accept(this.refreshOrderFromDbSync(orderId)));
   }

   public Order refreshOrderFromDbSync(UUID orderId) {
      String sql = "SELECT * FROM orders WHERE order_id=?";

      try {
         Order var7;
         try (
            Connection conn = this.dataSource.getConnection();
            PreparedStatement ps = conn.prepareStatement("SELECT * FROM orders WHERE order_id=?");
         ) {
            ps.setString(1, orderId.toString());
            ResultSet rs = ps.executeQuery();
            if (!rs.next()) {
               return this.orderCache.get(orderId);
            }

            Order fromDb = this.rowToOrder(rs);
            if (fromDb == null) {
               return this.orderCache.get(orderId);
            }

            this.mergeRemoteOrder(fromDb);
            this.lastSyncedAt.updateAndGet(prev -> Math.max(prev, fromDb.getUpdatedAt()));
            var7 = this.orderCache.get(orderId);
         }

         return var7;
      } catch (SQLException var12) {
         LOG.log(Level.WARNING, "Failed to refresh order " + orderId + " from database", (Throwable)var12);
         return this.orderCache.get(orderId);
      }
   }

   private void mergeRemoteOrder(Order fromDb) {
      this.orderCache.compute(fromDb.getOrderId(), (id, cached) -> {
         if (cached == null) {
            this.putToPlayerIndex(fromDb);
            return fromDb;
         } else if (fromDb.getUpdatedAt() >= cached.getUpdatedAt()) {
            cached.applyRemoteState(fromDb);
            return (Order)cached;
         } else {
            return (Order)cached;
         }
      });
   }

   public void loadAll(Runnable onDone) {
      Runnable finish = () -> {
         try {
            onDone.run();
         } catch (Throwable var2x) {
            LOG.log(Level.SEVERE, "Order load completion callback failed", var2x);
         }
      };

      try {
         FoliaScheduler.runAsync(() -> {
            this.loadAllIntoCache();
            FoliaScheduler.runGlobal(finish);
         });
      } catch (RuntimeException var4) {
         LOG.log(Level.WARNING, "Async scheduler unavailable during startup; loading orders synchronously", (Throwable)var4);
         this.loadAllIntoCache();
         FoliaScheduler.runGlobal(finish);
      }
   }

   private void loadAllIntoCache() {
      try (
         Connection conn = this.dataSource.getConnection();
         PreparedStatement ps = conn.prepareStatement("SELECT * FROM orders");
      ) {
         ResultSet rs = ps.executeQuery();

         while (rs.next()) {
            Order order = this.rowToOrder(rs);
            if (order != null) {
               this.putToCache(order);
               this.lastSyncedAt.updateAndGet(prev -> Math.max(prev, order.getUpdatedAt()));
            }
         }
      } catch (SQLException var9) {
         LOG.log(Level.SEVERE, "Failed to load orders from database", (Throwable)var9);
      }
   }

   public void saveOrder(Order order, Runnable callback) {
      order.setUpdatedAt(System.currentTimeMillis());
      this.putToCache(order);
      this.lastSyncedAt.updateAndGet(prev -> Math.max(prev, order.getUpdatedAt()));
      FoliaScheduler.runAsync(
         () -> {
            String sql = this.mysql
               ? "INSERT INTO orders (order_id, buyer_uuid, buyer_name, item_data, amount_req,  amount_filled, price_per_item, remaining_funds, created_at,  expires_at, status, claimed_at, claimed_by, updated_at) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?) ON DUPLICATE KEY UPDATE buyer_uuid=VALUES(buyer_uuid), buyer_name=VALUES(buyer_name), item_data=VALUES(item_data), amount_req=VALUES(amount_req), amount_filled=VALUES(amount_filled), price_per_item=VALUES(price_per_item), remaining_funds=VALUES(remaining_funds), created_at=VALUES(created_at), expires_at=VALUES(expires_at), status=VALUES(status), claimed_at=VALUES(claimed_at), claimed_by=VALUES(claimed_by), updated_at=VALUES(updated_at)"
               : "INSERT OR REPLACE INTO orders (order_id, buyer_uuid, buyer_name, item_data, amount_req,  amount_filled, price_per_item, remaining_funds, created_at,  expires_at, status, claimed_at, claimed_by, updated_at) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?)";

            try (
               Connection conn = this.dataSource.getConnection();
               PreparedStatement ps = conn.prepareStatement(sql);
            ) {
               this.bindOrder(ps, order);
               ps.executeUpdate();
            } catch (SQLException var12) {
               LOG.log(Level.SEVERE, "Failed to save order " + order.getOrderId(), (Throwable)var12);
            }

            if (callback != null) {
               callback.run();
            }
         }
      );
   }

   public void updateOrder(Order order, Runnable callback) {
      this.updateOrderOptimistic(order, null, null, success -> {
         if (callback != null) {
            callback.run();
         }
      });
   }

   public void updateOrderOptimistic(Order order, Integer expectedAmountFilled, OrderStatus expectedStatus, Consumer<Boolean> callback) {
      order.setUpdatedAt(System.currentTimeMillis());
      this.putToCache(order);
      this.lastSyncedAt.updateAndGet(prev -> Math.max(prev, order.getUpdatedAt()));
      FoliaScheduler.runAsync(
         () -> {
            boolean success = false;
            StringBuilder sql = new StringBuilder(
               "UPDATE orders SET amount_filled=?, remaining_funds=?, status=?, claimed_at=?, claimed_by=?, updated_at=? WHERE order_id=?"
            );
            if (expectedAmountFilled != null) {
               sql.append(" AND amount_filled=?");
            }

            if (expectedStatus != null) {
               sql.append(" AND status=?");
            }

            try (
               Connection conn = this.dataSource.getConnection();
               PreparedStatement ps = conn.prepareStatement(sql.toString());
            ) {
               int idx = 1;
               ps.setInt(idx++, order.getAmountFulfilled());
               ps.setDouble(idx++, order.getRemainingFunds());
               ps.setString(idx++, order.getStatus().name());
               ps.setLong(idx++, order.getClaimedAt());
               ps.setString(idx++, order.getClaimedBy() != null ? order.getClaimedBy().toString() : null);
               ps.setLong(idx++, order.getUpdatedAt());
               ps.setString(idx++, order.getOrderId().toString());
               if (expectedAmountFilled != null) {
                  ps.setInt(idx++, expectedAmountFilled);
               }

               if (expectedStatus != null) {
                  ps.setString(idx++, expectedStatus.name());
               }

               success = ps.executeUpdate() > 0;
            } catch (SQLException var15) {
               LOG.log(Level.SEVERE, "Failed to update order " + order.getOrderId(), (Throwable)var15);
            }

            if (callback != null) {
               callback.accept(success);
            }
         }
      );
   }

   public void saveStash(UUID orderId, ItemStack[] stash, Runnable callback) {
      String data = ItemUtils.serializeItemArray(stash);
      FoliaScheduler.runAsync(
         () -> {
            String sql = this.mysql
               ? "INSERT INTO order_stash (order_id, stash_data) VALUES (?,?) ON DUPLICATE KEY UPDATE stash_data=VALUES(stash_data)"
               : "INSERT OR REPLACE INTO order_stash (order_id, stash_data) VALUES (?,?)";

            try (
               Connection conn = this.dataSource.getConnection();
               PreparedStatement ps = conn.prepareStatement(sql);
            ) {
               ps.setString(1, orderId.toString());
               ps.setString(2, data);
               ps.executeUpdate();
            } catch (SQLException var13) {
               LOG.log(Level.SEVERE, "Failed to save stash for " + orderId, (Throwable)var13);
            }

            if (callback != null) {
               callback.run();
            }
         }
      );
   }

   public void loadStash(UUID orderId, Consumer<ItemStack[]> callback) {
      FoliaScheduler.runAsync(() -> {
         ItemStack[] result = new ItemStack[54];
         String sql = "SELECT stash_data FROM order_stash WHERE order_id=?";

         try (
            Connection conn = this.dataSource.getConnection();
            PreparedStatement ps = conn.prepareStatement("SELECT stash_data FROM order_stash WHERE order_id=?");
         ) {
            ps.setString(1, orderId.toString());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
               result = ItemUtils.deserializeItemArray(rs.getString("stash_data"), 54);
            }
         } catch (SQLException var13) {
            LOG.log(Level.SEVERE, "Failed to load stash for " + orderId, (Throwable)var13);
         }

         callback.accept(result);
      });
   }

   public void clearStash(UUID orderId, Runnable callback) {
      FoliaScheduler.runAsync(() -> {
         try (
            Connection conn = this.dataSource.getConnection();
            PreparedStatement ps = conn.prepareStatement("DELETE FROM order_stash WHERE order_id=?");
         ) {
            ps.setString(1, orderId.toString());
            ps.executeUpdate();
         } catch (SQLException var11) {
            LOG.log(Level.SEVERE, "Failed to clear stash for " + orderId, (Throwable)var11);
         }

         if (callback != null) {
            callback.run();
         }
      });
   }

   public Order getOrder(UUID orderId) {
      return this.orderCache.get(orderId);
   }

   public List<Order> getPlayerOrders(UUID playerUUID) {
      List<UUID> ids = this.playerOrderIndex.getOrDefault(playerUUID, Collections.emptyList());
      List<Order> result = new ArrayList<>(ids.size());

      for (UUID id : ids) {
         Order o = this.orderCache.get(id);
         if (o != null) {
            result.add(o);
         }
      }

      return result;
   }

   public List<Order> getAllActiveOrders() {
      List<Order> active = new ArrayList<>();

      for (Order o : this.orderCache.values()) {
         if (o.getStatus() == OrderStatus.ACTIVE) {
            active.add(o);
         }
      }

      active.sort(Comparator.comparingLong(Order::getCreatedAt));
      return active;
   }

   public Collection<Order> getAllOrders() {
      return Collections.unmodifiableCollection(this.orderCache.values());
   }

   public boolean isMysql() {
      return this.mysql;
   }

   public void close() {
      if (this.dataSource != null && !this.dataSource.isClosed()) {
         this.dataSource.close();
      }
   }

   private void putToCache(Order order) {
      this.orderCache.put(order.getOrderId(), order);
      this.putToPlayerIndex(order);
   }

   private void putToPlayerIndex(Order order) {
      this.playerOrderIndex.compute(order.getBuyerUUID(), (k, existing) -> {
         if (existing == null) {
            List<UUID> list = new ArrayList<>();
            list.add(order.getOrderId());
            return list;
         } else if (!existing.contains(order.getOrderId())) {
            List<UUID> list = new ArrayList<>(existing);
            list.add(order.getOrderId());
            return list;
         } else {
            return existing;
         }
      });
   }

   private void bindOrder(PreparedStatement ps, Order o) throws SQLException {
      ps.setString(1, o.getOrderId().toString());
      ps.setString(2, o.getBuyerUUID().toString());
      ps.setString(3, o.getBuyerName());
      ps.setString(4, ItemUtils.serializeItem(o.getItemTemplate()));
      ps.setInt(5, o.getAmountRequested());
      ps.setInt(6, o.getAmountFulfilled());
      ps.setDouble(7, o.getPricePerItem());
      ps.setDouble(8, o.getRemainingFunds());
      ps.setLong(9, o.getCreatedAt());
      ps.setLong(10, o.getExpiresAt());
      ps.setString(11, o.getStatus().name());
      ps.setLong(12, o.getClaimedAt());
      ps.setString(13, o.getClaimedBy() != null ? o.getClaimedBy().toString() : null);
      ps.setLong(14, o.getUpdatedAt());
   }

   private Order rowToOrder(ResultSet rs) {
      try {
         UUID orderId = UUID.fromString(rs.getString("order_id"));
         UUID buyerUUID = UUID.fromString(rs.getString("buyer_uuid"));
         String buyerName = rs.getString("buyer_name");
         ItemStack template = ItemUtils.deserializeItem(rs.getString("item_data"));
         if (template == null) {
            template = new ItemStack(Material.PAPER);
         }

         int amountReq = rs.getInt("amount_req");
         int amountFilled = rs.getInt("amount_filled");
         double price = rs.getDouble("price_per_item");
         double funds = rs.getDouble("remaining_funds");
         long createdAt = rs.getLong("created_at");
         long expiresAt = rs.getLong("expires_at");
         OrderStatus status = OrderStatus.valueOf(rs.getString("status"));
         long claimedAt = rs.getLong("claimed_at");
         String claimedByStr = rs.getString("claimed_by");
         UUID claimedBy = claimedByStr != null && !claimedByStr.isEmpty() ? UUID.fromString(claimedByStr) : null;
         long updatedAt = 0L;

         try {
            updatedAt = rs.getLong("updated_at");
         } catch (SQLException var24) {
            updatedAt = createdAt;
         }

         return new Order(
            orderId, buyerUUID, buyerName, template, amountReq, amountFilled, price, funds, createdAt, expiresAt, status, claimedAt, claimedBy, updatedAt
         );
      } catch (Exception var25) {
         LOG.log(Level.WARNING, "Failed to parse order row", (Throwable)var25);
         return null;
      }
   }
}
