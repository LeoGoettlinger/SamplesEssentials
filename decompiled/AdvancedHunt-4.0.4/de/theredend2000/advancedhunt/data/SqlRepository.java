package de.theredend2000.advancedhunt.data;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import de.theredend2000.advancedhunt.lib.hikari.HikariConfig;
import de.theredend2000.advancedhunt.lib.hikari.HikariDataSource;
import de.theredend2000.advancedhunt.model.ActRule;
import de.theredend2000.advancedhunt.model.Collection;
import de.theredend2000.advancedhunt.model.PlaceItem;
import de.theredend2000.advancedhunt.model.PlayerData;
import de.theredend2000.advancedhunt.model.Reward;
import de.theredend2000.advancedhunt.model.RewardPreset;
import de.theredend2000.advancedhunt.model.RewardPresetType;
import de.theredend2000.advancedhunt.model.Treasure;
import de.theredend2000.advancedhunt.model.TreasureCore;
import java.io.File;
import java.lang.reflect.Type;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.logging.Level;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.plugin.java.JavaPlugin;

public class SqlRepository implements DataRepository {
   private static final Type REWARD_LIST_TYPE = (new TypeToken<List<Reward>>() {}).getType();
   private HikariDataSource dataSource;
   private final JavaPlugin plugin;
   private final String host;
   private final String database;
   private final String username;
   private final String password;
   private final int port;
   private final boolean useSqlite;
   private final Gson gson;
   private volatile ExecutorService sqliteExecutor;
   private volatile Executor asyncExecutor;
   private final Map<Integer, Consumer<Connection>> schemaMigrations = new HashMap<>();
   private int cachedSchemaVersion = -1;
   private static final int MAX_RETRIES = 3;
   private static final long INITIAL_BACKOFF_MS = 50L;

   private void safeRollback(Connection conn, Exception originalException) {
      if (conn != null) {
         try {
            conn.rollback();
         } catch (SQLException var4) {
            originalException.addSuppressed(var4);
            this.plugin.getLogger().log(Level.SEVERE, "Failed to rollback transaction", (Throwable)var4);
         }
      }
   }

   private void safeRestoreAutoCommit(Connection conn, boolean originalAutoCommit) {
      if (conn != null) {
         try {
            conn.setAutoCommit(originalAutoCommit);
         } catch (SQLException var4) {
            this.plugin.getLogger().log(Level.WARNING, "Failed to restore autocommit mode", (Throwable)var4);
         }
      }
   }

   private <T> T executeWithRetry(SqlRepository.SqlOperation<T> operation) throws SQLException {
      SQLException lastException = null;

      for (int attempt = 0; attempt < 3; attempt++) {
         try {
            return operation.execute();
         } catch (SQLException var7) {
            if (!this.useSqlite || var7.getMessage() == null || !var7.getMessage().contains("SQLITE_BUSY")) {
               throw var7;
            }

            lastException = var7;

            try {
               Thread.sleep(50L * (1L << attempt));
            } catch (InterruptedException var6) {
               Thread.currentThread().interrupt();
               throw var7;
            }
         }
      }

      throw lastException;
   }

   public SqlRepository(JavaPlugin plugin, String host, int port, String database, String username, String password, boolean useSqlite) {
      this.plugin = plugin;
      this.host = host;
      this.port = port;
      this.database = database;
      this.username = username;
      this.password = password;
      this.useSqlite = useSqlite;
      this.gson = new Gson();
      if (useSqlite) {
         this.ensureSqliteExecutor();
      } else {
         this.sqliteExecutor = null;
         this.asyncExecutor = null;
      }

      this.registerMigrations();
   }

   private synchronized void ensureSqliteExecutor() {
      if (this.useSqlite) {
         ExecutorService current = this.sqliteExecutor;
         if (current == null || current.isShutdown() || current.isTerminated()) {
            ThreadFactory threadFactory = runnable -> {
               Thread thread = new Thread(runnable, "AdvancedHunt-SQLite");
               thread.setDaemon(true);
               return thread;
            };
            this.sqliteExecutor = Executors.newSingleThreadExecutor(threadFactory);
            this.asyncExecutor = this.sqliteExecutor;
         }
      }
   }

   private <T> CompletableFuture<T> supplyAsync(Supplier<T> supplier) {
      return this.asyncExecutor == null ? CompletableFuture.supplyAsync(supplier) : CompletableFuture.supplyAsync(supplier, this.asyncExecutor);
   }

   private CompletableFuture<Void> runAsync(Runnable runnable) {
      return this.asyncExecutor == null ? CompletableFuture.runAsync(runnable) : CompletableFuture.runAsync(runnable, this.asyncExecutor);
   }

   private void registerMigrations() {
      this.schemaMigrations.put(1, this::createPerformanceIndexes);
      this.schemaMigrations.put(2, this::addHideAfterFoundColumn);
   }

   @Override
   public void init() {
      if (this.useSqlite) {
         this.ensureSqliteExecutor();
      }

      this.cachedSchemaVersion = -1;
      HikariConfig config = new HikariConfig();
      if (this.useSqlite) {
         File dataFolder = this.plugin.getDataFolder();
         if (!dataFolder.exists()) {
            dataFolder.mkdirs();
         }

         File dbFile = new File(dataFolder, "database.db");
         config.setJdbcUrl("jdbc:sqlite:" + dbFile.getAbsolutePath());
         config.setMaximumPoolSize(1);
         config.setMinimumIdle(1);
         config.setConnectionInitSql("PRAGMA foreign_keys=ON; PRAGMA journal_mode=WAL; PRAGMA synchronous=NORMAL; PRAGMA busy_timeout=5000;");
      } else {
         config.setJdbcUrl("jdbc:mysql://" + this.host + ":" + this.port + "/" + this.database);
         config.setUsername(this.username);
         config.setPassword(this.password);
         config.setMaximumPoolSize(10);
      }

      this.dataSource = new HikariDataSource(config);
      this.createTables();
      this.upgradeSchema();
   }

   @Override
   public void reload() {
      this.shutdown();
      this.init();
   }

   private void createTables() {
      try {
         Connection conn = this.dataSource.getConnection();

         try {
            Statement stmt = conn.createStatement();

            try {
               stmt.execute("CREATE TABLE IF NOT EXISTS ah_schema_version (version INT PRIMARY KEY)");
               stmt.execute(
                  "CREATE TABLE IF NOT EXISTS ah_collections (id VARCHAR(36) PRIMARY KEY, name VARCHAR(64) UNIQUE, enabled BOOLEAN, reset_cron VARCHAR(64), active_start VARCHAR(32), active_end VARCHAR(32), progress_reset_cron VARCHAR(64), single_player_find BOOLEAN, rewards TEXT, default_treasure_reward_preset_id VARCHAR(36), hide_when_not_available BOOLEAN DEFAULT FALSE)"
               );
               stmt.execute(
                  "CREATE TABLE IF NOT EXISTS ah_reward_presets (id VARCHAR(36) PRIMARY KEY, type VARCHAR(16) NOT NULL, name VARCHAR(64) NOT NULL, rewards TEXT)"
               );
               stmt.execute(
                  "CREATE TABLE IF NOT EXISTS ah_act_rules (id VARCHAR(36) PRIMARY KEY, collection_id VARCHAR(36) NOT NULL, name VARCHAR(64), date_range VARCHAR(64), duration VARCHAR(16), cron_expression VARCHAR(64), enabled BOOLEAN, FOREIGN KEY (collection_id) REFERENCES ah_collections(id) ON DELETE CASCADE)"
               );
               stmt.execute(
                  "CREATE TABLE IF NOT EXISTS ah_place_items (id VARCHAR(36) PRIMARY KEY, grp VARCHAR(64) NOT NULL, name VARCHAR(64) NOT NULL, item TEXT)"
               );
               stmt.execute("CREATE TABLE IF NOT EXISTS ah_place_preset_groups (grp VARCHAR(64) PRIMARY KEY)");
               stmt.execute(
                  "CREATE TABLE IF NOT EXISTS ah_treasures (id VARCHAR(36) PRIMARY KEY, collection_id VARCHAR(36), world VARCHAR(64), x INT, y INT, z INT, rewards TEXT, block_data TEXT, material VARCHAR(64), block_state TEXT)"
               );
               stmt.execute(
                  "CREATE TABLE IF NOT EXISTS ah_player_found (player_uuid VARCHAR(36), treasure_id VARCHAR(36), PRIMARY KEY (player_uuid, treasure_id))"
               );
               stmt.execute("CREATE TABLE IF NOT EXISTS ah_players (uuid VARCHAR(36) PRIMARY KEY)");
            } catch (Throwable var7) {
               if (stmt != null) {
                  try {
                     stmt.close();
                  } catch (Throwable var6) {
                     var7.addSuppressed(var6);
                  }
               }

               throw var7;
            }

            if (stmt != null) {
               stmt.close();
            }
         } catch (Throwable var8) {
            if (conn != null) {
               try {
                  conn.close();
               } catch (Throwable var5) {
                  var8.addSuppressed(var5);
               }
            }

            throw var8;
         }

         if (conn != null) {
            conn.close();
         }
      } catch (SQLException var9) {
         this.plugin.getLogger().severe("Failed to initialize database: " + var9.getMessage());
      }
   }

   @Override
   public int getSchemaVersion() {
      if (this.cachedSchemaVersion != -1) {
         return this.cachedSchemaVersion;
      } else {
         try {
            Connection conn = this.dataSource.getConnection();

            int var4;
            label116: {
               try {
                  Statement stmt;
                  label108: {
                     stmt = conn.createStatement();

                     try {
                        ResultSet rs = stmt.executeQuery("SELECT version FROM ah_schema_version");

                        label87: {
                           try {
                              if (rs.next()) {
                                 this.cachedSchemaVersion = rs.getInt("version");
                                 var4 = this.cachedSchemaVersion;
                                 break label87;
                              }
                           } catch (Throwable var9) {
                              if (rs != null) {
                                 try {
                                    rs.close();
                                 } catch (Throwable var8) {
                                    var9.addSuppressed(var8);
                                 }
                              }

                              throw var9;
                           }

                           if (rs != null) {
                              rs.close();
                           }
                           break label108;
                        }

                        if (rs != null) {
                           rs.close();
                        }
                     } catch (Throwable var10) {
                        if (stmt != null) {
                           try {
                              stmt.close();
                           } catch (Throwable var7) {
                              var10.addSuppressed(var7);
                           }
                        }

                        throw var10;
                     }

                     if (stmt != null) {
                        stmt.close();
                     }
                     break label116;
                  }

                  if (stmt != null) {
                     stmt.close();
                  }
               } catch (Throwable var11) {
                  if (conn != null) {
                     try {
                        conn.close();
                     } catch (Throwable var6) {
                        var11.addSuppressed(var6);
                     }
                  }

                  throw var11;
               }

               if (conn != null) {
                  conn.close();
               }

               return 0;
            }

            if (conn != null) {
               conn.close();
            }

            return var4;
         } catch (SQLException var12) {
            this.plugin.getLogger().severe("Failed to get schema version: " + var12.getMessage());
            return 0;
         }
      }
   }

   @Override
   public void upgradeSchema() {
      try {
         Connection conn = this.dataSource.getConnection();

         try {
            int currentVersion = 0;
            Statement stmt = conn.createStatement();

            try {
               ResultSet rs = stmt.executeQuery("SELECT version FROM ah_schema_version");

               try {
                  if (rs.next()) {
                     currentVersion = rs.getInt("version");
                  } else {
                     Statement insertStmt = conn.createStatement();

                     try {
                        insertStmt.execute("INSERT INTO ah_schema_version (version) VALUES (0)");
                     } catch (Throwable var14) {
                        if (insertStmt != null) {
                           try {
                              insertStmt.close();
                           } catch (Throwable var12) {
                              var14.addSuppressed(var12);
                           }
                        }

                        throw var14;
                     }

                     if (insertStmt != null) {
                        insertStmt.close();
                     }
                  }
               } catch (Throwable var15) {
                  if (rs != null) {
                     try {
                        rs.close();
                     } catch (Throwable var11) {
                        var15.addSuppressed(var11);
                     }
                  }

                  throw var15;
               }

               if (rs != null) {
                  rs.close();
               }
            } catch (Throwable var16) {
               if (stmt != null) {
                  try {
                     stmt.close();
                  } catch (Throwable var10) {
                     var16.addSuppressed(var10);
                  }
               }

               throw var16;
            }

            if (stmt != null) {
               stmt.close();
            }

            int latestVersion = this.schemaMigrations.keySet().stream().mapToInt(v -> v).max().orElse(0);

            for (int i = currentVersion + 1; i <= latestVersion; this.cachedSchemaVersion = i++) {
               if (this.schemaMigrations.containsKey(i)) {
                  try {
                     this.schemaMigrations.get(i).accept(conn);
                     this.plugin.getLogger().info("Upgraded database schema to version " + i);
                  } catch (Exception var17) {
                     this.plugin.getLogger().severe("Failed to apply schema migration for version " + i);
                     break;
                  }
               }

               PreparedStatement ps = conn.prepareStatement("UPDATE ah_schema_version SET version = ?");

               try {
                  ps.setInt(1, i);
                  ps.executeUpdate();
               } catch (Throwable var13) {
                  if (ps != null) {
                     try {
                        ps.close();
                     } catch (Throwable var9) {
                        var13.addSuppressed(var9);
                     }
                  }

                  throw var13;
               }

               if (ps != null) {
                  ps.close();
               }
            }
         } catch (Throwable var18) {
            if (conn != null) {
               try {
                  conn.close();
               } catch (Throwable var8) {
                  var18.addSuppressed(var8);
               }
            }

            throw var18;
         }

         if (conn != null) {
            conn.close();
         }
      } catch (SQLException var19) {
         this.plugin.getLogger().severe("Failed to upgrade schema: " + var19.getMessage());
      }
   }

   private void createPerformanceIndexes(Connection conn) {
      String[] indexes = this.useSqlite
         ? new String[]{
            "CREATE INDEX IF NOT EXISTS idx_player_found_player ON ah_player_found(player_uuid)",
            "CREATE INDEX IF NOT EXISTS idx_player_found_treasure ON ah_player_found(treasure_id)",
            "CREATE INDEX IF NOT EXISTS idx_treasures_collection ON ah_treasures(collection_id)",
            "CREATE INDEX IF NOT EXISTS idx_leaderboard_query ON ah_treasures(collection_id, id)"
         }
         : new String[]{
            "CREATE INDEX idx_player_found_player ON ah_player_found(player_uuid)",
            "CREATE INDEX idx_player_found_treasure ON ah_player_found(treasure_id)",
            "CREATE INDEX idx_treasures_collection ON ah_treasures(collection_id)",
            "CREATE INDEX idx_leaderboard_query ON ah_treasures(collection_id, id)"
         };

      for (String indexSql : indexes) {
         try {
            Statement stmt = conn.createStatement();

            try {
               stmt.execute(indexSql);
               this.plugin.getLogger().info("Created database index for performance optimization");
            } catch (Throwable var11) {
               if (stmt != null) {
                  try {
                     stmt.close();
                  } catch (Throwable var10) {
                     var11.addSuppressed(var10);
                  }
               }

               throw var11;
            }

            if (stmt != null) {
               stmt.close();
            }
         } catch (SQLException var12) {
            if (!var12.getMessage().contains("already exists") && !var12.getMessage().contains("Duplicate")) {
               this.plugin.getLogger().warning("Failed to create index: " + var12.getMessage());
            }
         }
      }
   }

   private void addHideAfterFoundColumn(Connection conn) {
      try {
         Statement stmt = conn.createStatement();

         try {
            stmt.execute("ALTER TABLE ah_collections ADD COLUMN hide_after_found BOOLEAN DEFAULT FALSE");
         } catch (Throwable var6) {
            if (stmt != null) {
               try {
                  stmt.close();
               } catch (Throwable var5) {
                  var6.addSuppressed(var5);
               }
            }

            throw var6;
         }

         if (stmt != null) {
            stmt.close();
         }
      } catch (SQLException var7) {
         if (!var7.getMessage().contains("duplicate") && !var7.getMessage().contains("already exists") && !var7.getMessage().contains("Duplicate")) {
            this.plugin.getLogger().warning("Failed to add hide_after_found column: " + var7.getMessage());
         }
      }
   }

   private List<ActRule> loadActRules(Connection conn, UUID collectionId) throws SQLException {
      List<ActRule> rules = new ArrayList<>();
      PreparedStatement ps = conn.prepareStatement("SELECT * FROM ah_act_rules WHERE collection_id = ?");

      try {
         ps.setString(1, collectionId.toString());
         ResultSet rs = ps.executeQuery();

         while (rs.next()) {
            UUID ruleId = UUID.fromString(rs.getString("id"));
            String name = rs.getString("name");
            ActRule rule = new ActRule(ruleId, collectionId, name);
            rule.setDateRange(rs.getString("date_range"));
            rule.setDuration(rs.getString("duration"));
            rule.setCronExpression(rs.getString("cron_expression"));
            rule.setEnabled(rs.getBoolean("enabled"));
            rules.add(rule);
         }
      } catch (Throwable var10) {
         if (ps != null) {
            try {
               ps.close();
            } catch (Throwable var9) {
               var10.addSuppressed(var9);
            }
         }

         throw var10;
      }

      if (ps != null) {
         ps.close();
      }

      return rules;
   }

   @Override
   public void shutdown() {
      if (this.dataSource != null) {
         this.dataSource.close();
         this.dataSource = null;
      }

      if (this.sqliteExecutor != null) {
         this.sqliteExecutor.shutdown();

         try {
            if (!this.sqliteExecutor.awaitTermination(5L, TimeUnit.SECONDS)) {
               this.sqliteExecutor.shutdownNow();
            }
         } catch (InterruptedException var2) {
            this.sqliteExecutor.shutdownNow();
            Thread.currentThread().interrupt();
         }
      }
   }

   @Override
   public CompletableFuture<PlayerData> loadPlayerData(UUID playerUuid) {
      return this.supplyAsync(() -> {
         PlayerData data = new PlayerData(playerUuid);

         try {
            Connection conn = this.dataSource.getConnection();

            try {
               PreparedStatement ps = conn.prepareStatement("SELECT treasure_id FROM ah_player_found WHERE player_uuid = ?");

               try {
                  ps.setString(1, playerUuid.toString());
                  ResultSet rs = ps.executeQuery();

                  while (rs.next()) {
                     data.addFoundTreasure(UUID.fromString(rs.getString("treasure_id")));
                  }
               } catch (Throwable var9) {
                  if (ps != null) {
                     try {
                        ps.close();
                     } catch (Throwable var8) {
                        var9.addSuppressed(var8);
                     }
                  }

                  throw var9;
               }

               if (ps != null) {
                  ps.close();
               }
            } catch (Throwable var10) {
               if (conn != null) {
                  try {
                     conn.close();
                  } catch (Throwable var7) {
                     var10.addSuppressed(var7);
                  }
               }

               throw var10;
            }

            if (conn != null) {
               conn.close();
            }
         } catch (SQLException var11) {
            this.plugin.getLogger().severe("Failed to load player data for " + playerUuid + ": " + var11.getMessage());
         }

         return data;
      });
   }

   @Override
   public CompletableFuture<Void> savePlayerData(PlayerData playerData) {
      return this.runAsync(
         () -> {
            try {
               Connection conn = this.dataSource.getConnection();

               try {
                  String ensurePlayerSql = this.useSqlite
                     ? "INSERT OR IGNORE INTO ah_players (uuid) VALUES (?)"
                     : "INSERT IGNORE INTO ah_players (uuid) VALUES (?)";
                  PreparedStatement ps = conn.prepareStatement(ensurePlayerSql);

                  try {
                     ps.setString(1, playerData.getPlayerUuid().toString());
                     ps.executeUpdate();
                  } catch (Throwable var11) {
                     if (ps != null) {
                        try {
                           ps.close();
                        } catch (Throwable var10) {
                           var11.addSuppressed(var10);
                        }
                     }

                     throw var11;
                  }

                  if (ps != null) {
                     ps.close();
                  }

                  String insertSql = this.useSqlite
                     ? "INSERT OR IGNORE INTO ah_player_found (player_uuid, treasure_id) VALUES (?, ?)"
                     : "INSERT IGNORE INTO ah_player_found (player_uuid, treasure_id) VALUES (?, ?)";
                  PreparedStatement psx = conn.prepareStatement(insertSql);

                  try {
                     for (UUID treasureId : playerData.getFoundTreasures()) {
                        psx.setString(1, playerData.getPlayerUuid().toString());
                        psx.setString(2, treasureId.toString());
                        psx.addBatch();
                     }

                     psx.executeBatch();
                  } catch (Throwable var12) {
                     if (psx != null) {
                        try {
                           psx.close();
                        } catch (Throwable var9) {
                           var12.addSuppressed(var9);
                        }
                     }

                     throw var12;
                  }

                  if (psx != null) {
                     psx.close();
                  }
               } catch (Throwable var13) {
                  if (conn != null) {
                     try {
                        conn.close();
                     } catch (Throwable var8) {
                        var13.addSuppressed(var8);
                     }
                  }

                  throw var13;
               }

               if (conn != null) {
                  conn.close();
               }
            } catch (SQLException var14) {
               this.plugin.getLogger().severe("Failed to save player data for " + playerData.getPlayerUuid() + ": " + var14.getMessage());
            }
         }
      );
   }

   @Override
   public CompletableFuture<Void> savePlayerDataBatch(List<PlayerData> playerDataList) {
      return this.runAsync(
         () -> {
            if (!playerDataList.isEmpty()) {
               try {
                  Connection conn = this.dataSource.getConnection();

                  try {
                     boolean originalAutoCommit = conn.getAutoCommit();
                     conn.setAutoCommit(false);

                     try {
                        this.executeWithRetry(
                           () -> {
                              String ensurePlayerSql = this.useSqlite
                                 ? "INSERT OR IGNORE INTO ah_players (uuid) VALUES (?)"
                                 : "INSERT IGNORE INTO ah_players (uuid) VALUES (?)";
                              PreparedStatement ps = conn.prepareStatement(ensurePlayerSql);

                              try {
                                 for (PlayerData pd : playerDataList) {
                                    ps.setString(1, pd.getPlayerUuid().toString());
                                    ps.addBatch();
                                 }

                                 ps.executeBatch();
                              } catch (Throwable var14x) {
                                 if (ps != null) {
                                    try {
                                       ps.close();
                                    } catch (Throwable var12x) {
                                       var14x.addSuppressed(var12x);
                                    }
                                 }

                                 throw var14x;
                              }

                              if (ps != null) {
                                 ps.close();
                              }

                              String insertSql = this.useSqlite
                                 ? "INSERT OR IGNORE INTO ah_player_found (player_uuid, treasure_id) VALUES (?, ?)"
                                 : "INSERT IGNORE INTO ah_player_found (player_uuid, treasure_id) VALUES (?, ?)";
                              PreparedStatement psx = conn.prepareStatement(insertSql);

                              try {
                                 for (PlayerData pd : playerDataList) {
                                    String uuidStr = pd.getPlayerUuid().toString();

                                    for (UUID treasureId : pd.getFoundTreasures()) {
                                       psx.setString(1, uuidStr);
                                       psx.setString(2, treasureId.toString());
                                       psx.addBatch();
                                    }
                                 }

                                 psx.executeBatch();
                              } catch (Throwable var13x) {
                                 if (psx != null) {
                                    try {
                                       psx.close();
                                    } catch (Throwable var11x) {
                                       var13x.addSuppressed(var11x);
                                    }
                                 }

                                 throw var13x;
                              }

                              if (psx != null) {
                                 psx.close();
                              }

                              conn.commit();
                              return null;
                           }
                        );
                     } catch (SQLException var12) {
                        this.safeRollback(conn, var12);
                        throw var12;
                     } finally {
                        this.safeRestoreAutoCommit(conn, originalAutoCommit);
                     }
                  } catch (Throwable var14) {
                     if (conn != null) {
                        try {
                           conn.close();
                        } catch (Throwable var11) {
                           var14.addSuppressed(var11);
                        }
                     }

                     throw var14;
                  }

                  if (conn != null) {
                     conn.close();
                  }
               } catch (SQLException var15) {
                  this.plugin.getLogger().severe("Failed to batch save player data: " + var15.getMessage());
               }
            }
         }
      );
   }

   @Override
   public CompletableFuture<List<Treasure>> loadTreasures() {
      return this.supplyAsync(() -> {
         List<Treasure> treasures = new ArrayList<>();

         try {
            Connection conn = this.dataSource.getConnection();

            try {
               PreparedStatement ps = conn.prepareStatement("SELECT * FROM ah_treasures");

               try {
                  ResultSet rs = ps.executeQuery();

                  while (rs.next()) {
                     UUID id = UUID.fromString(rs.getString("id"));
                     UUID collectionId = UUID.fromString(rs.getString("collection_id"));
                     String world = rs.getString("world");
                     int x = rs.getInt("x");
                     int y = rs.getInt("y");
                     int z = rs.getInt("z");
                     Location loc = new Location(Bukkit.getWorld(world), x, y, z);
                     String rewardsJson = rs.getString("rewards");
                     List<Reward> rewards = (List<Reward>)this.gson.fromJson(rewardsJson, REWARD_LIST_TYPE);
                     String nbtData = rs.getString("block_data");
                     String material = rs.getString("material");
                     String blockState = rs.getString("block_state");
                     treasures.add(new Treasure(id, collectionId, loc, rewards, nbtData, material, blockState));
                  }
               } catch (Throwable var19) {
                  if (ps != null) {
                     try {
                        ps.close();
                     } catch (Throwable var18) {
                        var19.addSuppressed(var18);
                     }
                  }

                  throw var19;
               }

               if (ps != null) {
                  ps.close();
               }
            } catch (Throwable var20) {
               if (conn != null) {
                  try {
                     conn.close();
                  } catch (Throwable var17) {
                     var20.addSuppressed(var17);
                  }
               }

               throw var20;
            }

            if (conn != null) {
               conn.close();
            }
         } catch (SQLException var21) {
            this.plugin.getLogger().severe("Failed to load treasures: " + var21.getMessage());
         }

         return treasures;
      });
   }

   @Override
   public CompletableFuture<List<TreasureCore>> loadTreasureCores() {
      return this.supplyAsync(() -> {
         List<TreasureCore> cores = new ArrayList<>();

         try {
            Connection conn = this.dataSource.getConnection();

            try {
               PreparedStatement ps = conn.prepareStatement("SELECT id, collection_id, world, x, y, z, material, block_state FROM ah_treasures");

               try {
                  ResultSet rs = ps.executeQuery();

                  while (rs.next()) {
                     UUID id = UUID.fromString(rs.getString("id"));
                     UUID collectionId = UUID.fromString(rs.getString("collection_id"));
                     String world = rs.getString("world");
                     int x = rs.getInt("x");
                     int y = rs.getInt("y");
                     int z = rs.getInt("z");
                     Location loc = new Location(Bukkit.getWorld(world), x, y, z);
                     String material = rs.getString("material");
                     String blockState = rs.getString("block_state");
                     cores.add(new TreasureCore(id, collectionId, loc, material, blockState));
                  }
               } catch (Throwable var16) {
                  if (ps != null) {
                     try {
                        ps.close();
                     } catch (Throwable var15) {
                        var16.addSuppressed(var15);
                     }
                  }

                  throw var16;
               }

               if (ps != null) {
                  ps.close();
               }
            } catch (Throwable var17) {
               if (conn != null) {
                  try {
                     conn.close();
                  } catch (Throwable var14) {
                     var17.addSuppressed(var14);
                  }
               }

               throw var17;
            }

            if (conn != null) {
               conn.close();
            }
         } catch (SQLException var18) {
            this.plugin.getLogger().severe("Failed to load treasure cores: " + var18.getMessage());
         }

         return cores;
      });
   }

   @Override
   public CompletableFuture<List<UUID>> getAllTreasureUUIDs() {
      return this.supplyAsync(() -> {
         List<UUID> ids = new ArrayList<>();

         try {
            Connection conn = this.dataSource.getConnection();

            try {
               PreparedStatement ps = conn.prepareStatement("SELECT id FROM ah_treasures");

               try {
                  ResultSet rs = ps.executeQuery();

                  while (rs.next()) {
                     try {
                        ids.add(UUID.fromString(rs.getString("id")));
                     } catch (IllegalArgumentException var8) {
                     }
                  }
               } catch (Throwable var9) {
                  if (ps != null) {
                     try {
                        ps.close();
                     } catch (Throwable var7) {
                        var9.addSuppressed(var7);
                     }
                  }

                  throw var9;
               }

               if (ps != null) {
                  ps.close();
               }
            } catch (Throwable var10) {
               if (conn != null) {
                  try {
                     conn.close();
                  } catch (Throwable var6) {
                     var10.addSuppressed(var6);
                  }
               }

               throw var10;
            }

            if (conn != null) {
               conn.close();
            }
         } catch (SQLException var11) {
            this.plugin.getLogger().severe("Failed to get treasure IDs by collection: " + var11.getMessage());
         }

         return ids;
      });
   }

   @Override
   public CompletableFuture<Void> saveTreasure(Treasure treasure) {
      return this.runAsync(
         () -> {
            try {
               Connection conn = this.dataSource.getConnection();

               try {
                  PreparedStatement ps = conn.prepareStatement(
                     "REPLACE INTO ah_treasures (id, collection_id, world, x, y, z, rewards, block_data, material, block_state) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)"
                  );

                  try {
                     ps.setString(1, treasure.getId().toString());
                     ps.setString(2, treasure.getCollectionId().toString());
                     ps.setString(3, treasure.getLocation().getWorld().getName());
                     ps.setInt(4, treasure.getLocation().getBlockX());
                     ps.setInt(5, treasure.getLocation().getBlockY());
                     ps.setInt(6, treasure.getLocation().getBlockZ());
                     ps.setString(7, this.gson.toJson(treasure.getRewards()));
                     ps.setString(8, treasure.getNbtData());
                     ps.setString(9, treasure.getMaterial());
                     ps.setString(10, treasure.getBlockState());
                     ps.executeUpdate();
                  } catch (Throwable var8) {
                     if (ps != null) {
                        try {
                           ps.close();
                        } catch (Throwable var7) {
                           var8.addSuppressed(var7);
                        }
                     }

                     throw var8;
                  }

                  if (ps != null) {
                     ps.close();
                  }
               } catch (Throwable var9) {
                  if (conn != null) {
                     try {
                        conn.close();
                     } catch (Throwable var6) {
                        var9.addSuppressed(var6);
                     }
                  }

                  throw var9;
               }

               if (conn != null) {
                  conn.close();
               }
            } catch (SQLException var10) {
               this.plugin.getLogger().severe("Failed to save treasure: " + var10.getMessage());
            }
         }
      );
   }

   @Override
   public CompletableFuture<Void> saveTreasuresBatch(List<Treasure> treasures) {
      return this.runAsync(
         () -> {
            if (treasures != null && !treasures.isEmpty()) {
               try {
                  Connection conn = this.dataSource.getConnection();

                  try {
                     PreparedStatement ps = conn.prepareStatement(
                        "REPLACE INTO ah_treasures (id, collection_id, world, x, y, z, rewards, block_data, material, block_state) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)"
                     );

                     try {
                        boolean originalAutoCommit = conn.getAutoCommit();
                        conn.setAutoCommit(false);

                        try {
                           this.executeWithRetry(() -> {
                              for (Treasure treasure : treasures) {
                                 ps.setString(1, treasure.getId().toString());
                                 ps.setString(2, treasure.getCollectionId().toString());
                                 ps.setString(3, treasure.getLocation().getWorld().getName());
                                 ps.setInt(4, treasure.getLocation().getBlockX());
                                 ps.setInt(5, treasure.getLocation().getBlockY());
                                 ps.setInt(6, treasure.getLocation().getBlockZ());
                                 ps.setString(7, this.gson.toJson(treasure.getRewards()));
                                 ps.setString(8, treasure.getNbtData());
                                 ps.setString(9, treasure.getMaterial());
                                 ps.setString(10, treasure.getBlockState());
                                 ps.addBatch();
                              }

                              ps.executeBatch();
                              conn.commit();
                              return null;
                           });
                        } catch (SQLException var16) {
                           this.safeRollback(conn, var16);
                           throw var16;
                        } finally {
                           this.safeRestoreAutoCommit(conn, originalAutoCommit);
                        }
                     } catch (Throwable var18) {
                        if (ps != null) {
                           try {
                              ps.close();
                           } catch (Throwable var15) {
                              var18.addSuppressed(var15);
                           }
                        }

                        throw var18;
                     }

                     if (ps != null) {
                        ps.close();
                     }
                  } catch (Throwable var19) {
                     if (conn != null) {
                        try {
                           conn.close();
                        } catch (Throwable var14) {
                           var19.addSuppressed(var14);
                        }
                     }

                     throw var19;
                  }

                  if (conn != null) {
                     conn.close();
                  }
               } catch (SQLException var20) {
                  this.plugin.getLogger().severe("Failed to save treasures batch: " + var20.getMessage());
               }
            }
         }
      );
   }

   @Override
   public CompletableFuture<Void> deleteTreasure(UUID treasureId) {
      return this.runAsync(() -> {
         try {
            Connection conn = this.dataSource.getConnection();

            try {
               PreparedStatement ps = conn.prepareStatement("DELETE FROM ah_treasures WHERE id = ?");

               try {
                  ps.setString(1, treasureId.toString());
                  ps.executeUpdate();
               } catch (Throwable var8) {
                  if (ps != null) {
                     try {
                        ps.close();
                     } catch (Throwable var7) {
                        var8.addSuppressed(var7);
                     }
                  }

                  throw var8;
               }

               if (ps != null) {
                  ps.close();
               }
            } catch (Throwable var9) {
               if (conn != null) {
                  try {
                     conn.close();
                  } catch (Throwable var6) {
                     var9.addSuppressed(var6);
                  }
               }

               throw var9;
            }

            if (conn != null) {
               conn.close();
            }
         } catch (SQLException var10) {
            this.plugin.getLogger().severe("Failed to delete treasure: " + var10.getMessage());
         }
      });
   }

   @Override
   public CompletableFuture<Integer> deleteTreasuresInCollection(UUID collectionId) {
      return this.supplyAsync(() -> {
         try {
            Connection conn = this.dataSource.getConnection();

            Integer var4;
            try {
               PreparedStatement ps = conn.prepareStatement("DELETE FROM ah_treasures WHERE collection_id = ?");

               try {
                  ps.setString(1, collectionId.toString());
                  var4 = ps.executeUpdate();
               } catch (Throwable var8) {
                  if (ps != null) {
                     try {
                        ps.close();
                     } catch (Throwable var7) {
                        var8.addSuppressed(var7);
                     }
                  }

                  throw var8;
               }

               if (ps != null) {
                  ps.close();
               }
            } catch (Throwable var9) {
               if (conn != null) {
                  try {
                     conn.close();
                  } catch (Throwable var6) {
                     var9.addSuppressed(var6);
                  }
               }

               throw var9;
            }

            if (conn != null) {
               conn.close();
            }

            return var4;
         } catch (SQLException var10) {
            this.plugin.getLogger().severe("Failed to delete treasures by collection ID: " + var10.getMessage());
            return 0;
         }
      });
   }

   @Override
   public CompletableFuture<Treasure> loadTreasure(UUID treasureId) {
      return this.supplyAsync(() -> {
         try {
            Connection conn = this.dataSource.getConnection();

            Treasure var17;
            label79: {
               try {
                  PreparedStatement ps = conn.prepareStatement("SELECT * FROM ah_treasures WHERE id = ?");

                  label74: {
                     try {
                        ps.setString(1, treasureId.toString());
                        ResultSet rs = ps.executeQuery();
                        if (!rs.next()) {
                           break label74;
                        }

                        UUID id = UUID.fromString(rs.getString("id"));
                        UUID collectionId = UUID.fromString(rs.getString("collection_id"));
                        String world = rs.getString("world");
                        int x = rs.getInt("x");
                        int y = rs.getInt("y");
                        int z = rs.getInt("z");
                        Location loc = new Location(Bukkit.getWorld(world), x, y, z);
                        String rewardsJson = rs.getString("rewards");
                        List<Reward> rewards = (List<Reward>)this.gson.fromJson(rewardsJson, REWARD_LIST_TYPE);
                        String nbtData = rs.getString("block_data");
                        String material = rs.getString("material");
                        String blockState = rs.getString("block_state");
                        var17 = new Treasure(id, collectionId, loc, rewards, nbtData, material, blockState);
                     } catch (Throwable var20) {
                        if (ps != null) {
                           try {
                              ps.close();
                           } catch (Throwable var19) {
                              var20.addSuppressed(var19);
                           }
                        }

                        throw var20;
                     }

                     if (ps != null) {
                        ps.close();
                     }
                     break label79;
                  }

                  if (ps != null) {
                     ps.close();
                  }
               } catch (Throwable var21) {
                  if (conn != null) {
                     try {
                        conn.close();
                     } catch (Throwable var18) {
                        var21.addSuppressed(var18);
                     }
                  }

                  throw var21;
               }

               if (conn != null) {
                  conn.close();
               }

               return null;
            }

            if (conn != null) {
               conn.close();
            }

            return var17;
         } catch (SQLException var22) {
            this.plugin.getLogger().severe("Failed to load treasure: " + var22.getMessage());
            return null;
         }
      });
   }

   @Override
   public CompletableFuture<Void> updateTreasureRewards(UUID treasureId, List<Reward> rewards) {
      return this.runAsync(() -> {
         try {
            Connection conn = this.dataSource.getConnection();

            try {
               PreparedStatement ps = conn.prepareStatement("UPDATE ah_treasures SET rewards = ? WHERE id = ?");

               try {
                  ps.setString(1, this.gson.toJson(rewards));
                  ps.setString(2, treasureId.toString());
                  ps.executeUpdate();
               } catch (Throwable var9) {
                  if (ps != null) {
                     try {
                        ps.close();
                     } catch (Throwable var8) {
                        var9.addSuppressed(var8);
                     }
                  }

                  throw var9;
               }

               if (ps != null) {
                  ps.close();
               }
            } catch (Throwable var10) {
               if (conn != null) {
                  try {
                     conn.close();
                  } catch (Throwable var7) {
                     var10.addSuppressed(var7);
                  }
               }

               throw var10;
            }

            if (conn != null) {
               conn.close();
            }
         } catch (SQLException var11) {
            this.plugin.getLogger().severe("Failed to update treasure rewards: " + var11.getMessage());
         }
      });
   }

   @Override
   public CompletableFuture<Void> updateTreasureRewardsBatch(List<UUID> treasureIds, List<Reward> rewards) {
      return this.runAsync(() -> {
         if (treasureIds != null && !treasureIds.isEmpty()) {
            String rewardsJson = this.gson.toJson(rewards);

            try {
               Connection conn = this.dataSource.getConnection();

               try {
                  PreparedStatement ps = conn.prepareStatement("UPDATE ah_treasures SET rewards = ? WHERE id = ?");

                  try {
                     boolean oldAutoCommit = conn.getAutoCommit();
                     conn.setAutoCommit(false);

                     try {
                        this.executeWithRetry(() -> {
                           for (UUID treasureId : treasureIds) {
                              ps.setString(1, rewardsJson);
                              ps.setString(2, treasureId.toString());
                              ps.addBatch();
                           }

                           ps.executeBatch();
                           conn.commit();
                           return null;
                        });
                     } catch (SQLException var18) {
                        this.safeRollback(conn, var18);
                        throw var18;
                     } finally {
                        this.safeRestoreAutoCommit(conn, oldAutoCommit);
                     }
                  } catch (Throwable var20) {
                     if (ps != null) {
                        try {
                           ps.close();
                        } catch (Throwable var17) {
                           var20.addSuppressed(var17);
                        }
                     }

                     throw var20;
                  }

                  if (ps != null) {
                     ps.close();
                  }
               } catch (Throwable var21) {
                  if (conn != null) {
                     try {
                        conn.close();
                     } catch (Throwable var16) {
                        var21.addSuppressed(var16);
                     }
                  }

                  throw var21;
               }

               if (conn != null) {
                  conn.close();
               }
            } catch (SQLException var22) {
               this.plugin.getLogger().severe("Failed to update treasure rewards batch: " + var22.getMessage());
            }
         }
      });
   }

   @Override
   public CompletableFuture<List<Collection>> loadCollections() {
      return this.supplyAsync(() -> {
         List<Collection> collections = new ArrayList<>();

         try {
            Connection conn = this.dataSource.getConnection();

            try {
               PreparedStatement ps = conn.prepareStatement("SELECT * FROM ah_collections");

               try {
                  ResultSet rs = ps.executeQuery();

                  while (rs.next()) {
                     UUID id = UUID.fromString(rs.getString("id"));
                     String name = rs.getString("name");
                     boolean enabled = rs.getBoolean("enabled");
                     Collection c = new Collection(id, name, enabled);
                     c.setProgressResetCron(rs.getString("progress_reset_cron"));
                     c.setSinglePlayerFind(rs.getBoolean("single_player_find"));
                     c.setHideWhenNotAvailable(rs.getBoolean("hide_when_not_available"));
                     c.setHideAfterFound(rs.getBoolean("hide_after_found"));
                     String defaultPreset = rs.getString("default_treasure_reward_preset_id");
                     if (defaultPreset != null && !defaultPreset.trim().isEmpty()) {
                        try {
                           c.setDefaultTreasureRewardPresetId(UUID.fromString(defaultPreset));
                        } catch (IllegalArgumentException var15) {
                           c.setDefaultTreasureRewardPresetId(null);
                        }
                     }

                     String rewardsJson = rs.getString("rewards");
                     List<Reward> rewards = (List<Reward>)this.gson.fromJson(rewardsJson, REWARD_LIST_TYPE);
                     c.setCompletionRewards(rewards);
                     List<ActRule> actRules = this.loadActRules(conn, id);
                     c.setActRules(actRules);
                     collections.add(c);
                  }
               } catch (Throwable var16) {
                  if (ps != null) {
                     try {
                        ps.close();
                     } catch (Throwable var14) {
                        var16.addSuppressed(var14);
                     }
                  }

                  throw var16;
               }

               if (ps != null) {
                  ps.close();
               }
            } catch (Throwable var17) {
               if (conn != null) {
                  try {
                     conn.close();
                  } catch (Throwable var13) {
                     var17.addSuppressed(var13);
                  }
               }

               throw var17;
            }

            if (conn != null) {
               conn.close();
            }
         } catch (SQLException var18) {
            this.plugin.getLogger().severe("Failed to load collections: " + var18.getMessage());
         }

         return collections;
      });
   }

   @Override
   public CompletableFuture<Void> saveCollection(Collection collection) {
      return this.runAsync(
         () -> {
            try {
               Connection conn = this.dataSource.getConnection();

               try {
                  boolean originalAutoCommit = conn.getAutoCommit();
                  conn.setAutoCommit(false);

                  try {
                     this.executeWithRetry(
                        () -> {
                           String upsertSql = this.useSqlite
                              ? "INSERT INTO ah_collections (id, name, enabled, reset_cron, active_start, active_end, progress_reset_cron, single_player_find, rewards, default_treasure_reward_preset_id, hide_when_not_available, hide_after_found) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) ON CONFLICT(id) DO UPDATE SET name=excluded.name, enabled=excluded.enabled, reset_cron=excluded.reset_cron, active_start=excluded.active_start, active_end=excluded.active_end, progress_reset_cron=excluded.progress_reset_cron, single_player_find=excluded.single_player_find, rewards=excluded.rewards, default_treasure_reward_preset_id=excluded.default_treasure_reward_preset_id, hide_when_not_available=excluded.hide_when_not_available, hide_after_found=excluded.hide_after_found"
                              : "INSERT INTO ah_collections (id, name, enabled, reset_cron, active_start, active_end, progress_reset_cron, single_player_find, rewards, default_treasure_reward_preset_id, hide_when_not_available, hide_after_found) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) ON DUPLICATE KEY UPDATE name=VALUES(name), enabled=VALUES(enabled), reset_cron=VALUES(reset_cron), active_start=VALUES(active_start), active_end=VALUES(active_end), progress_reset_cron=VALUES(progress_reset_cron), single_player_find=VALUES(single_player_find), rewards=VALUES(rewards), default_treasure_reward_preset_id=VALUES(default_treasure_reward_preset_id), hide_when_not_available=VALUES(hide_when_not_available), hide_after_found=VALUES(hide_after_found)";
                           PreparedStatement ps = conn.prepareStatement(upsertSql);

                           try {
                              ps.setString(1, collection.getId().toString());
                              ps.setString(2, collection.getName());
                              ps.setBoolean(3, collection.isEnabled());
                              ps.setNull(4, 12);
                              ps.setNull(5, 12);
                              ps.setNull(6, 12);
                              ps.setString(7, collection.getProgressResetCron());
                              ps.setBoolean(8, collection.isSinglePlayerFind());
                              ps.setString(9, this.gson.toJson(collection.getCompletionRewards()));
                              ps.setString(
                                 10, collection.getDefaultTreasureRewardPresetId() != null ? collection.getDefaultTreasureRewardPresetId().toString() : null
                              );
                              ps.setBoolean(11, collection.isHideWhenNotAvailable());
                              ps.setBoolean(12, collection.isHideAfterFound());
                              ps.executeUpdate();
                           } catch (Throwable var14x) {
                              if (ps != null) {
                                 try {
                                    ps.close();
                                 } catch (Throwable var11x) {
                                    var14x.addSuppressed(var11x);
                                 }
                              }

                              throw var14x;
                           }

                           if (ps != null) {
                              ps.close();
                           }

                           ps = conn.prepareStatement("DELETE FROM ah_act_rules WHERE collection_id = ?");

                           try {
                              ps.setString(1, collection.getId().toString());
                              ps.executeUpdate();
                           } catch (Throwable var13x) {
                              if (ps != null) {
                                 try {
                                    ps.close();
                                 } catch (Throwable var10) {
                                    var13x.addSuppressed(var10);
                                 }
                              }

                              throw var13x;
                           }

                           if (ps != null) {
                              ps.close();
                           }

                           for (ActRule rule : collection.getActRules()) {
                              PreparedStatement psx = conn.prepareStatement(
                                 "INSERT INTO ah_act_rules (id, collection_id, name, date_range, duration, cron_expression, enabled) VALUES (?, ?, ?, ?, ?, ?, ?)"
                              );

                              try {
                                 psx.setString(1, rule.getId().toString());
                                 psx.setString(2, rule.getCollectionId().toString());
                                 psx.setString(3, rule.getName());
                                 psx.setString(4, rule.getDateRange());
                                 psx.setString(5, rule.getDuration());
                                 psx.setString(6, rule.getCronExpression());
                                 psx.setBoolean(7, rule.isEnabled());
                                 psx.executeUpdate();
                              } catch (Throwable var12x) {
                                 if (psx != null) {
                                    try {
                                       psx.close();
                                    } catch (Throwable var9) {
                                       var12x.addSuppressed(var9);
                                    }
                                 }

                                 throw var12x;
                              }

                              if (psx != null) {
                                 psx.close();
                              }
                           }

                           conn.commit();
                           return null;
                        }
                     );
                  } catch (SQLException var12) {
                     this.safeRollback(conn, var12);
                     throw var12;
                  } finally {
                     this.safeRestoreAutoCommit(conn, originalAutoCommit);
                  }
               } catch (Throwable var14) {
                  if (conn != null) {
                     try {
                        conn.close();
                     } catch (Throwable var11) {
                        var14.addSuppressed(var11);
                     }
                  }

                  throw var14;
               }

               if (conn != null) {
                  conn.close();
               }
            } catch (SQLException var15) {
               this.plugin.getLogger().severe("Failed to save collection: " + var15.getMessage());
            }
         }
      );
   }

   @Override
   public CompletableFuture<List<RewardPreset>> loadRewardPresets(RewardPresetType type) {
      return this.supplyAsync(() -> {
         List<RewardPreset> presets = new ArrayList<>();

         try {
            Connection conn = this.dataSource.getConnection();

            try {
               PreparedStatement ps = conn.prepareStatement("SELECT * FROM ah_reward_presets WHERE type = ?");

               try {
                  ps.setString(1, type.name());
                  ResultSet rs = ps.executeQuery();

                  while (rs.next()) {
                     UUID id = UUID.fromString(rs.getString("id"));
                     String name = rs.getString("name");
                     String rewardsJson = rs.getString("rewards");
                     List<Reward> rewards = (List<Reward>)this.gson.fromJson(rewardsJson, REWARD_LIST_TYPE);
                     presets.add(new RewardPreset(id, type, name, rewards));
                  }
               } catch (Throwable var12) {
                  if (ps != null) {
                     try {
                        ps.close();
                     } catch (Throwable var11) {
                        var12.addSuppressed(var11);
                     }
                  }

                  throw var12;
               }

               if (ps != null) {
                  ps.close();
               }
            } catch (Throwable var13) {
               if (conn != null) {
                  try {
                     conn.close();
                  } catch (Throwable var10) {
                     var13.addSuppressed(var10);
                  }
               }

               throw var13;
            }

            if (conn != null) {
               conn.close();
            }
         } catch (SQLException var14) {
            this.plugin.getLogger().severe("Failed to load reward presets: " + var14.getMessage());
         }

         return presets;
      });
   }

   @Override
   public CompletableFuture<Void> saveRewardPreset(RewardPreset preset) {
      return this.runAsync(() -> {
         try {
            Connection conn = this.dataSource.getConnection();

            try {
               PreparedStatement ps = conn.prepareStatement("REPLACE INTO ah_reward_presets (id, type, name, rewards) VALUES (?, ?, ?, ?)");

               try {
                  ps.setString(1, preset.getId().toString());
                  ps.setString(2, preset.getType().name());
                  ps.setString(3, preset.getName());
                  ps.setString(4, this.gson.toJson(preset.getRewards()));
                  ps.executeUpdate();
               } catch (Throwable var8) {
                  if (ps != null) {
                     try {
                        ps.close();
                     } catch (Throwable var7) {
                        var8.addSuppressed(var7);
                     }
                  }

                  throw var8;
               }

               if (ps != null) {
                  ps.close();
               }
            } catch (Throwable var9) {
               if (conn != null) {
                  try {
                     conn.close();
                  } catch (Throwable var6) {
                     var9.addSuppressed(var6);
                  }
               }

               throw var9;
            }

            if (conn != null) {
               conn.close();
            }
         } catch (SQLException var10) {
            this.plugin.getLogger().severe("Failed to save reward preset: " + var10.getMessage());
         }
      });
   }

   @Override
   public CompletableFuture<Void> saveRewardPresetsBatch(List<RewardPreset> presets) {
      return this.runAsync(() -> {
         if (presets != null && !presets.isEmpty()) {
            try {
               Connection conn = this.dataSource.getConnection();

               try {
                  PreparedStatement ps = conn.prepareStatement("REPLACE INTO ah_reward_presets (id, type, name, rewards) VALUES (?, ?, ?, ?)");

                  try {
                     boolean originalAutoCommit = conn.getAutoCommit();
                     conn.setAutoCommit(false);

                     try {
                        this.executeWithRetry(() -> {
                           for (RewardPreset preset : presets) {
                              ps.setString(1, preset.getId().toString());
                              ps.setString(2, preset.getType().name());
                              ps.setString(3, preset.getName());
                              ps.setString(4, this.gson.toJson(preset.getRewards()));
                              ps.addBatch();
                           }

                           ps.executeBatch();
                           conn.commit();
                           return null;
                        });
                     } catch (SQLException var16) {
                        this.safeRollback(conn, var16);
                        throw var16;
                     } finally {
                        this.safeRestoreAutoCommit(conn, originalAutoCommit);
                     }
                  } catch (Throwable var18) {
                     if (ps != null) {
                        try {
                           ps.close();
                        } catch (Throwable var15) {
                           var18.addSuppressed(var15);
                        }
                     }

                     throw var18;
                  }

                  if (ps != null) {
                     ps.close();
                  }
               } catch (Throwable var19) {
                  if (conn != null) {
                     try {
                        conn.close();
                     } catch (Throwable var14) {
                        var19.addSuppressed(var14);
                     }
                  }

                  throw var19;
               }

               if (conn != null) {
                  conn.close();
               }
            } catch (SQLException var20) {
               this.plugin.getLogger().severe("Failed to save reward presets batch: " + var20.getMessage());
            }
         }
      });
   }

   @Override
   public CompletableFuture<Void> deleteRewardPreset(RewardPresetType type, UUID presetId) {
      return this.runAsync(() -> {
         try {
            Connection conn = this.dataSource.getConnection();

            try {
               PreparedStatement ps = conn.prepareStatement("DELETE FROM ah_reward_presets WHERE id = ? AND type = ?");

               try {
                  ps.setString(1, presetId.toString());
                  ps.setString(2, type.name());
                  ps.executeUpdate();
               } catch (Throwable var9) {
                  if (ps != null) {
                     try {
                        ps.close();
                     } catch (Throwable var8) {
                        var9.addSuppressed(var8);
                     }
                  }

                  throw var9;
               }

               if (ps != null) {
                  ps.close();
               }
            } catch (Throwable var10) {
               if (conn != null) {
                  try {
                     conn.close();
                  } catch (Throwable var7) {
                     var10.addSuppressed(var7);
                  }
               }

               throw var10;
            }

            if (conn != null) {
               conn.close();
            }
         } catch (SQLException var11) {
            this.plugin.getLogger().severe("Failed to delete reward preset: " + var11.getMessage());
         }
      });
   }

   @Override
   public CompletableFuture<List<PlaceItem>> loadPlaceItems() {
      return this.supplyAsync(
         () -> {
            List<PlaceItem> presets = new ArrayList<>();

            try {
               Connection conn = this.dataSource.getConnection();

               try {
                  PreparedStatement ps = conn.prepareStatement("SELECT * FROM ah_place_items");

                  try {
                     ResultSet rs = ps.executeQuery();

                     while (rs.next()) {
                        UUID id = UUID.fromString(rs.getString("id"));
                        String group = rs.getString("grp");
                        String name = rs.getString("name");
                        String item = rs.getString("item");
                        presets.add(new PlaceItem(id, group, name, item));
                     }
                  } catch (Throwable var11) {
                     if (ps != null) {
                        try {
                           ps.close();
                        } catch (Throwable var10) {
                           var11.addSuppressed(var10);
                        }
                     }

                     throw var11;
                  }

                  if (ps != null) {
                     ps.close();
                  }
               } catch (Throwable var12) {
                  if (conn != null) {
                     try {
                        conn.close();
                     } catch (Throwable var9) {
                        var12.addSuppressed(var9);
                     }
                  }

                  throw var12;
               }

               if (conn != null) {
                  conn.close();
               }
            } catch (SQLException var13) {
               this.plugin.getLogger().severe("Failed to load place presets: " + var13.getMessage());
            }

            presets.sort(
               Comparator.comparing(PlaceItem::getGroup, String.CASE_INSENSITIVE_ORDER).thenComparing(PlaceItem::getName, String.CASE_INSENSITIVE_ORDER)
            );
            return presets;
         }
      );
   }

   @Override
   public CompletableFuture<Void> savePlaceItem(PlaceItem preset) {
      return this.runAsync(() -> {
         try {
            Connection conn = this.dataSource.getConnection();

            try {
               PreparedStatement ps = conn.prepareStatement("REPLACE INTO ah_place_items (id, grp, name, item) VALUES (?, ?, ?, ?)");

               try {
                  ps.setString(1, preset.getId().toString());
                  ps.setString(2, preset.getGroup());
                  ps.setString(3, preset.getName());
                  ps.setString(4, preset.getItemData());
                  ps.executeUpdate();
               } catch (Throwable var8) {
                  if (ps != null) {
                     try {
                        ps.close();
                     } catch (Throwable var7) {
                        var8.addSuppressed(var7);
                     }
                  }

                  throw var8;
               }

               if (ps != null) {
                  ps.close();
               }
            } catch (Throwable var9) {
               if (conn != null) {
                  try {
                     conn.close();
                  } catch (Throwable var6) {
                     var9.addSuppressed(var6);
                  }
               }

               throw var9;
            }

            if (conn != null) {
               conn.close();
            }
         } catch (SQLException var10) {
            this.plugin.getLogger().severe("Failed to save place preset: " + var10.getMessage());
         }
      });
   }

   @Override
   public CompletableFuture<Void> deletePlaceItem(UUID presetId) {
      return this.runAsync(() -> {
         try {
            Connection conn = this.dataSource.getConnection();

            try {
               PreparedStatement ps = conn.prepareStatement("DELETE FROM ah_place_items WHERE id = ?");

               try {
                  ps.setString(1, presetId.toString());
                  ps.executeUpdate();
               } catch (Throwable var8) {
                  if (ps != null) {
                     try {
                        ps.close();
                     } catch (Throwable var7) {
                        var8.addSuppressed(var7);
                     }
                  }

                  throw var8;
               }

               if (ps != null) {
                  ps.close();
               }
            } catch (Throwable var9) {
               if (conn != null) {
                  try {
                     conn.close();
                  } catch (Throwable var6) {
                     var9.addSuppressed(var6);
                  }
               }

               throw var9;
            }

            if (conn != null) {
               conn.close();
            }
         } catch (SQLException var10) {
            this.plugin.getLogger().severe("Failed to delete place preset: " + var10.getMessage());
         }
      });
   }

   @Override
   public CompletableFuture<Set<String>> loadPlaceItemGroups() {
      return this.supplyAsync(() -> {
         LinkedHashSet<String> groups = new LinkedHashSet<>();

         try {
            Connection conn = this.dataSource.getConnection();

            try {
               PreparedStatement ps = conn.prepareStatement("SELECT grp FROM ah_place_preset_groups");

               try {
                  ResultSet rs = ps.executeQuery();

                  try {
                     while (rs.next()) {
                        String group = rs.getString("grp");
                        if (group != null && !group.trim().isEmpty()) {
                           groups.add(group.trim());
                        }
                     }
                  } catch (Throwable var10) {
                     if (rs != null) {
                        try {
                           rs.close();
                        } catch (Throwable var9) {
                           var10.addSuppressed(var9);
                        }
                     }

                     throw var10;
                  }

                  if (rs != null) {
                     rs.close();
                  }
               } catch (Throwable var11) {
                  if (ps != null) {
                     try {
                        ps.close();
                     } catch (Throwable var8) {
                        var11.addSuppressed(var8);
                     }
                  }

                  throw var11;
               }

               if (ps != null) {
                  ps.close();
               }
            } catch (Throwable var12) {
               if (conn != null) {
                  try {
                     conn.close();
                  } catch (Throwable var7) {
                     var12.addSuppressed(var7);
                  }
               }

               throw var12;
            }

            if (conn != null) {
               conn.close();
            }
         } catch (SQLException var13) {
         }

         return groups;
      });
   }

   @Override
   public CompletableFuture<Void> createPlaceItemGroup(String group) {
      return this.runAsync(
         () -> {
            if (group != null && !group.trim().isEmpty()) {
               String sql = this.useSqlite
                  ? "INSERT OR IGNORE INTO ah_place_preset_groups (grp) VALUES (?)"
                  : "INSERT IGNORE INTO ah_place_preset_groups (grp) VALUES (?)";

               try {
                  Connection conn = this.dataSource.getConnection();

                  try {
                     PreparedStatement ps = conn.prepareStatement(sql);

                     try {
                        ps.setString(1, group.trim());
                        ps.executeUpdate();
                     } catch (Throwable var9) {
                        if (ps != null) {
                           try {
                              ps.close();
                           } catch (Throwable var8) {
                              var9.addSuppressed(var8);
                           }
                        }

                        throw var9;
                     }

                     if (ps != null) {
                        ps.close();
                     }
                  } catch (Throwable var10) {
                     if (conn != null) {
                        try {
                           conn.close();
                        } catch (Throwable var7) {
                           var10.addSuppressed(var7);
                        }
                     }

                     throw var10;
                  }

                  if (conn != null) {
                     conn.close();
                  }
               } catch (SQLException var11) {
                  this.plugin.getLogger().severe("Failed to save place preset group: " + var11.getMessage());
               }
            }
         }
      );
   }

   @Override
   public CompletableFuture<Void> renamePlaceItemGroup(String oldGroup, String newGroup) {
      return this.runAsync(
         () -> {
            if (oldGroup != null && !oldGroup.trim().isEmpty() && newGroup != null && !newGroup.trim().isEmpty()) {
               String oldTrimmed = oldGroup.trim();
               String newTrimmed = newGroup.trim();
               String insertSql = this.useSqlite
                  ? "INSERT OR IGNORE INTO ah_place_preset_groups (grp) VALUES (?)"
                  : "INSERT IGNORE INTO ah_place_preset_groups (grp) VALUES (?)";

               try {
                  Connection conn = this.dataSource.getConnection();

                  try {
                     boolean originalAutoCommit = conn.getAutoCommit();
                     conn.setAutoCommit(false);

                     try {
                        this.executeWithRetry(() -> {
                           PreparedStatement ps = conn.prepareStatement(insertSql);

                           try {
                              ps.setString(1, newTrimmed);
                              ps.executeUpdate();
                           } catch (Throwable var12) {
                              if (ps != null) {
                                 try {
                                    ps.close();
                                 } catch (Throwable var9) {
                                    var12.addSuppressed(var9);
                                 }
                              }

                              throw var12;
                           }

                           if (ps != null) {
                              ps.close();
                           }

                           ps = conn.prepareStatement("UPDATE ah_place_items SET grp = ? WHERE LOWER(grp) = LOWER(?)");

                           try {
                              ps.setString(1, newTrimmed);
                              ps.setString(2, oldTrimmed);
                              ps.executeUpdate();
                           } catch (Throwable var11) {
                              if (ps != null) {
                                 try {
                                    ps.close();
                                 } catch (Throwable var8) {
                                    var11.addSuppressed(var8);
                                 }
                              }

                              throw var11;
                           }

                           if (ps != null) {
                              ps.close();
                           }

                           ps = conn.prepareStatement("DELETE FROM ah_place_preset_groups WHERE LOWER(grp) = LOWER(?)");

                           try {
                              ps.setString(1, oldTrimmed);
                              ps.executeUpdate();
                           } catch (Throwable var10) {
                              if (ps != null) {
                                 try {
                                    ps.close();
                                 } catch (Throwable var7x) {
                                    var10.addSuppressed(var7x);
                                 }
                              }

                              throw var10;
                           }

                           if (ps != null) {
                              ps.close();
                           }

                           conn.commit();
                           return null;
                        });
                     } catch (SQLException var16) {
                        this.safeRollback(conn, var16);
                        throw var16;
                     } finally {
                        this.safeRestoreAutoCommit(conn, originalAutoCommit);
                     }
                  } catch (Throwable var18) {
                     if (conn != null) {
                        try {
                           conn.close();
                        } catch (Throwable var15) {
                           var18.addSuppressed(var15);
                        }
                     }

                     throw var18;
                  }

                  if (conn != null) {
                     conn.close();
                  }
               } catch (SQLException var19) {
                  this.plugin.getLogger().severe("Failed to rename place preset group: " + var19.getMessage());
               }
            }
         }
      );
   }

   @Override
   public CompletableFuture<Void> deletePlaceItemGroup(String group) {
      return this.runAsync(() -> {
         if (group != null && !group.trim().isEmpty()) {
            try {
               Connection conn = this.dataSource.getConnection();

               try {
                  PreparedStatement ps = conn.prepareStatement("DELETE FROM ah_place_preset_groups WHERE LOWER(grp) = LOWER(?)");

                  try {
                     ps.setString(1, group.trim());
                     ps.executeUpdate();
                  } catch (Throwable var8) {
                     if (ps != null) {
                        try {
                           ps.close();
                        } catch (Throwable var7) {
                           var8.addSuppressed(var7);
                        }
                     }

                     throw var8;
                  }

                  if (ps != null) {
                     ps.close();
                  }
               } catch (Throwable var9) {
                  if (conn != null) {
                     try {
                        conn.close();
                     } catch (Throwable var6) {
                        var9.addSuppressed(var6);
                     }
                  }

                  throw var9;
               }

               if (conn != null) {
                  conn.close();
               }
            } catch (SQLException var10) {
               this.plugin.getLogger().severe("Failed to delete place preset group: " + var10.getMessage());
            }
         }
      });
   }

   @Override
   public CompletableFuture<Void> deleteCollection(UUID id) {
      return this.runAsync(
         () -> {
            try {
               Connection conn = this.dataSource.getConnection();

               try {
                  boolean originalAutoCommit = conn.getAutoCommit();
                  conn.setAutoCommit(false);

                  try {
                     this.executeWithRetry(
                        () -> {
                           PreparedStatement ps = conn.prepareStatement(
                              "DELETE FROM ah_player_found WHERE treasure_id IN (SELECT id FROM ah_treasures WHERE collection_id = ?)"
                           );

                           try {
                              ps.setString(1, id.toString());
                              ps.executeUpdate();
                           } catch (Throwable var10) {
                              if (ps != null) {
                                 try {
                                    ps.close();
                                 } catch (Throwable var7) {
                                    var10.addSuppressed(var7);
                                 }
                              }

                              throw var10;
                           }

                           if (ps != null) {
                              ps.close();
                           }

                           ps = conn.prepareStatement("DELETE FROM ah_collections WHERE id = ?");

                           try {
                              ps.setString(1, id.toString());
                              ps.executeUpdate();
                           } catch (Throwable var9) {
                              if (ps != null) {
                                 try {
                                    ps.close();
                                 } catch (Throwable var6) {
                                    var9.addSuppressed(var6);
                                 }
                              }

                              throw var9;
                           }

                           if (ps != null) {
                              ps.close();
                           }

                           ps = conn.prepareStatement("DELETE FROM ah_treasures WHERE collection_id = ?");

                           try {
                              ps.setString(1, id.toString());
                              ps.executeUpdate();
                           } catch (Throwable var8) {
                              if (ps != null) {
                                 try {
                                    ps.close();
                                 } catch (Throwable var5) {
                                    var8.addSuppressed(var5);
                                 }
                              }

                              throw var8;
                           }

                           if (ps != null) {
                              ps.close();
                           }

                           conn.commit();
                           return null;
                        }
                     );
                  } catch (SQLException var12) {
                     this.safeRollback(conn, var12);
                     throw var12;
                  } finally {
                     this.safeRestoreAutoCommit(conn, originalAutoCommit);
                  }
               } catch (Throwable var14) {
                  if (conn != null) {
                     try {
                        conn.close();
                     } catch (Throwable var11) {
                        var14.addSuppressed(var11);
                     }
                  }

                  throw var14;
               }

               if (conn != null) {
                  conn.close();
               }
            } catch (SQLException var15) {
               this.plugin.getLogger().severe("Failed to delete collection: " + var15.getMessage());
            }
         }
      );
   }

   @Override
   public CompletableFuture<Boolean> renameCollection(UUID id, String newName) {
      return this.supplyAsync(() -> {
         try {
            Connection conn = this.dataSource.getConnection();

            Boolean var5;
            label100: {
               try {
                  PreparedStatement ps = conn.prepareStatement("SELECT 1 FROM ah_collections WHERE name = ?");

                  label102: {
                     try {
                        ps.setString(1, newName);
                        if (!ps.executeQuery().next()) {
                           break label102;
                        }

                        var5 = false;
                     } catch (Throwable var11) {
                        if (ps != null) {
                           try {
                              ps.close();
                           } catch (Throwable var9) {
                              var11.addSuppressed(var9);
                           }
                        }

                        throw var11;
                     }

                     if (ps != null) {
                        ps.close();
                     }
                     break label100;
                  }

                  if (ps != null) {
                     ps.close();
                  }

                  ps = conn.prepareStatement("UPDATE ah_collections SET name = ? WHERE id = ?");

                  try {
                     ps.setString(1, newName);
                     ps.setString(2, id.toString());
                     var5 = ps.executeUpdate() > 0;
                  } catch (Throwable var10) {
                     if (ps != null) {
                        try {
                           ps.close();
                        } catch (Throwable var8) {
                           var10.addSuppressed(var8);
                        }
                     }

                     throw var10;
                  }

                  if (ps != null) {
                     ps.close();
                  }
               } catch (Throwable var12) {
                  if (conn != null) {
                     try {
                        conn.close();
                     } catch (Throwable var7) {
                        var12.addSuppressed(var7);
                     }
                  }

                  throw var12;
               }

               if (conn != null) {
                  conn.close();
               }

               return var5;
            }

            if (conn != null) {
               conn.close();
            }

            return var5;
         } catch (SQLException var13) {
            this.plugin.getLogger().severe("Failed to rename collection: " + var13.getMessage());
            return false;
         }
      });
   }

   @Override
   public CompletableFuture<List<PlayerData>> loadAllPlayerData() {
      return this.supplyAsync(() -> {
         List<PlayerData> allData = new ArrayList<>();

         try {
            Connection conn = this.dataSource.getConnection();

            try {
               PreparedStatement ps = conn.prepareStatement("SELECT player_uuid, treasure_id FROM ah_player_found");

               try {
                  ResultSet rs = ps.executeQuery();
                  Map<UUID, PlayerData> map = new HashMap<>();

                  while (rs.next()) {
                     UUID uuid = UUID.fromString(rs.getString("player_uuid"));
                     UUID treasureId = UUID.fromString(rs.getString("treasure_id"));
                     map.computeIfAbsent(uuid, PlayerData::new).addFoundTreasure(treasureId);
                  }

                  allData.addAll(map.values());
               } catch (Throwable var10) {
                  if (ps != null) {
                     try {
                        ps.close();
                     } catch (Throwable var9) {
                        var10.addSuppressed(var9);
                     }
                  }

                  throw var10;
               }

               if (ps != null) {
                  ps.close();
               }
            } catch (Throwable var11) {
               if (conn != null) {
                  try {
                     conn.close();
                  } catch (Throwable var8) {
                     var11.addSuppressed(var8);
                  }
               }

               throw var11;
            }

            if (conn != null) {
               conn.close();
            }
         } catch (SQLException var12) {
            this.plugin.getLogger().severe("Failed to load all player data: " + var12.getMessage());
         }

         return allData;
      });
   }

   @Override
   public CompletableFuture<List<UUID>> getAllPlayerUUIDs() {
      return this.supplyAsync(() -> {
         List<UUID> uuids = new ArrayList<>();

         try {
            Connection conn = this.dataSource.getConnection();

            try {
               Statement stmt = conn.createStatement();

               try {
                  ResultSet rs = stmt.executeQuery("SELECT uuid FROM ah_players");

                  try {
                     while (rs.next()) {
                        uuids.add(UUID.fromString(rs.getString("uuid")));
                     }
                  } catch (Throwable var10) {
                     if (rs != null) {
                        try {
                           rs.close();
                        } catch (Throwable var9) {
                           var10.addSuppressed(var9);
                        }
                     }

                     throw var10;
                  }

                  if (rs != null) {
                     rs.close();
                  }
               } catch (Throwable var11) {
                  if (stmt != null) {
                     try {
                        stmt.close();
                     } catch (Throwable var8) {
                        var11.addSuppressed(var8);
                     }
                  }

                  throw var11;
               }

               if (stmt != null) {
                  stmt.close();
               }
            } catch (Throwable var12) {
               if (conn != null) {
                  try {
                     conn.close();
                  } catch (Throwable var7) {
                     var12.addSuppressed(var7);
                  }
               }

               throw var12;
            }

            if (conn != null) {
               conn.close();
            }
         } catch (SQLException var13) {
            this.plugin.getLogger().severe("Failed to get all player UUIDs: " + var13.getMessage());
         }

         return uuids;
      });
   }

   @Override
   public CompletableFuture<Integer> resetAllProgress() {
      return this.supplyAsync(() -> {
         try {
            Connection conn = this.dataSource.getConnection();

            Integer var3;
            try {
               PreparedStatement ps = conn.prepareStatement("DELETE FROM ah_player_found");

               try {
                  var3 = ps.executeUpdate();
               } catch (Throwable var7) {
                  if (ps != null) {
                     try {
                        ps.close();
                     } catch (Throwable var6) {
                        var7.addSuppressed(var6);
                     }
                  }

                  throw var7;
               }

               if (ps != null) {
                  ps.close();
               }
            } catch (Throwable var8) {
               if (conn != null) {
                  try {
                     conn.close();
                  } catch (Throwable var5) {
                     var8.addSuppressed(var5);
                  }
               }

               throw var8;
            }

            if (conn != null) {
               conn.close();
            }

            return var3;
         } catch (SQLException var9) {
            this.plugin.getLogger().severe("Failed to reset all progress: " + var9.getMessage());
            return 0;
         }
      });
   }

   @Override
   public CompletableFuture<Integer> resetCollectionProgress(UUID collectionId) {
      return this.supplyAsync(() -> {
         String sql = "DELETE FROM ah_player_found WHERE treasure_id IN (SELECT id FROM ah_treasures WHERE collection_id = ?)";

         try {
            Connection conn = this.dataSource.getConnection();

            Integer var5;
            try {
               PreparedStatement ps = conn.prepareStatement(sql);

               try {
                  ps.setString(1, collectionId.toString());
                  var5 = ps.executeUpdate();
               } catch (Throwable var9) {
                  if (ps != null) {
                     try {
                        ps.close();
                     } catch (Throwable var8) {
                        var9.addSuppressed(var8);
                     }
                  }

                  throw var9;
               }

               if (ps != null) {
                  ps.close();
               }
            } catch (Throwable var10) {
               if (conn != null) {
                  try {
                     conn.close();
                  } catch (Throwable var7) {
                     var10.addSuppressed(var7);
                  }
               }

               throw var10;
            }

            if (conn != null) {
               conn.close();
            }

            return var5;
         } catch (SQLException var11) {
            this.plugin.getLogger().severe("Failed to reset collection progress: " + var11.getMessage());
            return 0;
         }
      });
   }

   @Override
   public CompletableFuture<Integer> resetPlayerProgress(UUID playerUuid) {
      return this.supplyAsync(() -> {
         try {
            Connection conn = this.dataSource.getConnection();

            Integer var4;
            try {
               PreparedStatement ps = conn.prepareStatement("DELETE FROM ah_player_found WHERE player_uuid = ?");

               try {
                  ps.setString(1, playerUuid.toString());
                  var4 = ps.executeUpdate();
               } catch (Throwable var8) {
                  if (ps != null) {
                     try {
                        ps.close();
                     } catch (Throwable var7) {
                        var8.addSuppressed(var7);
                     }
                  }

                  throw var8;
               }

               if (ps != null) {
                  ps.close();
               }
            } catch (Throwable var9) {
               if (conn != null) {
                  try {
                     conn.close();
                  } catch (Throwable var6) {
                     var9.addSuppressed(var6);
                  }
               }

               throw var9;
            }

            if (conn != null) {
               conn.close();
            }

            return var4;
         } catch (SQLException var10) {
            this.plugin.getLogger().severe("Failed to reset player progress: " + var10.getMessage());
            return 0;
         }
      });
   }

   @Override
   public CompletableFuture<Integer> resetPlayerCollectionProgress(UUID playerUuid, UUID collectionId) {
      return this.supplyAsync(() -> {
         String sql = "DELETE FROM ah_player_found WHERE player_uuid = ? AND treasure_id IN (SELECT id FROM ah_treasures WHERE collection_id = ?)";

         try {
            Connection conn = this.dataSource.getConnection();

            Integer var6;
            try {
               PreparedStatement ps = conn.prepareStatement(sql);

               try {
                  ps.setString(1, playerUuid.toString());
                  ps.setString(2, collectionId.toString());
                  var6 = ps.executeUpdate();
               } catch (Throwable var10) {
                  if (ps != null) {
                     try {
                        ps.close();
                     } catch (Throwable var9) {
                        var10.addSuppressed(var9);
                     }
                  }

                  throw var10;
               }

               if (ps != null) {
                  ps.close();
               }
            } catch (Throwable var11) {
               if (conn != null) {
                  try {
                     conn.close();
                  } catch (Throwable var8) {
                     var11.addSuppressed(var8);
                  }
               }

               throw var11;
            }

            if (conn != null) {
               conn.close();
            }

            return var6;
         } catch (SQLException var12) {
            this.plugin.getLogger().severe("Failed to reset player collection progress: " + var12.getMessage());
            return 0;
         }
      });
   }

   @Override
   public CompletableFuture<Map<UUID, Integer>> getLeaderboard(UUID collectionId, int limit) {
      return this.supplyAsync(
         () -> {
            Map<UUID, Integer> leaderboard = new LinkedHashMap<>();
            String sql = "SELECT player_uuid, COUNT(*) as count FROM ah_player_found JOIN ah_treasures ON ah_player_found.treasure_id = ah_treasures.id WHERE ah_treasures.collection_id = ? GROUP BY player_uuid ORDER BY count DESC LIMIT ?";

            try {
               Connection conn = this.dataSource.getConnection();

               try {
                  PreparedStatement ps = conn.prepareStatement(sql);

                  try {
                     ps.setString(1, collectionId.toString());
                     ps.setInt(2, limit);
                     ResultSet rs = ps.executeQuery();

                     while (rs.next()) {
                        leaderboard.put(UUID.fromString(rs.getString("player_uuid")), rs.getInt("count"));
                     }
                  } catch (Throwable var11) {
                     if (ps != null) {
                        try {
                           ps.close();
                        } catch (Throwable var10) {
                           var11.addSuppressed(var10);
                        }
                     }

                     throw var11;
                  }

                  if (ps != null) {
                     ps.close();
                  }
               } catch (Throwable var12) {
                  if (conn != null) {
                     try {
                        conn.close();
                     } catch (Throwable var9) {
                        var12.addSuppressed(var9);
                     }
                  }

                  throw var12;
               }

               if (conn != null) {
                  conn.close();
               }
            } catch (SQLException var13) {
               this.plugin.getLogger().severe("Failed to get leaderboard: " + var13.getMessage());
            }

            return leaderboard;
         }
      );
   }

   @Override
   public CompletableFuture<List<UUID>> getPlayersWhoFound(UUID treasureId) {
      return this.supplyAsync(() -> {
         List<UUID> players = new ArrayList<>();

         try {
            Connection conn = this.dataSource.getConnection();

            try {
               PreparedStatement ps = conn.prepareStatement("SELECT player_uuid FROM ah_player_found WHERE treasure_id = ?");

               try {
                  ps.setString(1, treasureId.toString());
                  ResultSet rs = ps.executeQuery();

                  while (rs.next()) {
                     players.add(UUID.fromString(rs.getString("player_uuid")));
                  }
               } catch (Throwable var9) {
                  if (ps != null) {
                     try {
                        ps.close();
                     } catch (Throwable var8) {
                        var9.addSuppressed(var8);
                     }
                  }

                  throw var9;
               }

               if (ps != null) {
                  ps.close();
               }
            } catch (Throwable var10) {
               if (conn != null) {
                  try {
                     conn.close();
                  } catch (Throwable var7) {
                     var10.addSuppressed(var7);
                  }
               }

               throw var10;
            }

            if (conn != null) {
               conn.close();
            }
         } catch (SQLException var11) {
            this.plugin.getLogger().severe("Failed to get players who found treasure: " + var11.getMessage());
         }

         return players;
      });
   }

   @Override
   public CompletableFuture<Integer> getFoundPlayerCount(UUID treasureId) {
      return this.supplyAsync(() -> {
         try {
            Connection conn = this.dataSource.getConnection();

            Integer var5;
            label79: {
               try {
                  PreparedStatement ps = conn.prepareStatement("SELECT COUNT(*) AS cnt FROM ah_player_found WHERE treasure_id = ?");

                  label74: {
                     try {
                        ps.setString(1, treasureId.toString());
                        ResultSet rs = ps.executeQuery();
                        if (!rs.next()) {
                           break label74;
                        }

                        var5 = rs.getInt("cnt");
                     } catch (Throwable var8) {
                        if (ps != null) {
                           try {
                              ps.close();
                           } catch (Throwable var7) {
                              var8.addSuppressed(var7);
                           }
                        }

                        throw var8;
                     }

                     if (ps != null) {
                        ps.close();
                     }
                     break label79;
                  }

                  if (ps != null) {
                     ps.close();
                  }
               } catch (Throwable var9) {
                  if (conn != null) {
                     try {
                        conn.close();
                     } catch (Throwable var6) {
                        var9.addSuppressed(var6);
                     }
                  }

                  throw var9;
               }

               if (conn != null) {
                  conn.close();
               }

               return 0;
            }

            if (conn != null) {
               conn.close();
            }

            return var5;
         } catch (SQLException var10) {
            this.plugin.getLogger().severe("Failed to get found player count: " + var10.getMessage());
            return 0;
         }
      });
   }

   @Override
   public CompletableFuture<Set<UUID>> getPlayerFoundInCollection(UUID playerUuid, UUID collectionId) {
      return this.supplyAsync(
         () -> {
            Set<UUID> treasures = new HashSet<>();
            String sql = "SELECT pf.treasure_id FROM ah_player_found pf JOIN ah_treasures t ON pf.treasure_id = t.id WHERE pf.player_uuid = ? AND t.collection_id = ?";

            try {
               Connection conn = this.dataSource.getConnection();

               try {
                  PreparedStatement ps = conn.prepareStatement(sql);

                  try {
                     ps.setString(1, playerUuid.toString());
                     ps.setString(2, collectionId.toString());
                     ResultSet rs = ps.executeQuery();

                     while (rs.next()) {
                        treasures.add(UUID.fromString(rs.getString("treasure_id")));
                     }
                  } catch (Throwable var11) {
                     if (ps != null) {
                        try {
                           ps.close();
                        } catch (Throwable var10) {
                           var11.addSuppressed(var10);
                        }
                     }

                     throw var11;
                  }

                  if (ps != null) {
                     ps.close();
                  }
               } catch (Throwable var12) {
                  if (conn != null) {
                     try {
                        conn.close();
                     } catch (Throwable var9) {
                        var12.addSuppressed(var9);
                     }
                  }

                  throw var12;
               }

               if (conn != null) {
                  conn.close();
               }
            } catch (SQLException var13) {
               this.plugin.getLogger().severe("Failed to get player found in collection: " + var13.getMessage());
            }

            return treasures;
         }
      );
   }

   @FunctionalInterface
   private interface SqlOperation<T> {
      T execute() throws SQLException;
   }
}
