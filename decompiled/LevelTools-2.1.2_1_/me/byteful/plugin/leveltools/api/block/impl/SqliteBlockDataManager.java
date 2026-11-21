package me.byteful.plugin.leveltools.api.block.impl;

import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import me.byteful.plugin.leveltools.api.block.BlockDataManager;
import me.byteful.plugin.leveltools.api.block.BlockPosition;
import me.byteful.plugin.leveltools.api.scheduler.ScheduledTask;
import me.byteful.plugin.leveltools.api.scheduler.Scheduler;

public class SqliteBlockDataManager implements BlockDataManager {
   private static final String CREATE_TABLE_SQL = "CREATE TABLE IF NOT EXISTS placed_blocks (world TEXT NOT NULL, x INTEGER NOT NULL, y INTEGER NOT NULL, z INTEGER NOT NULL, UNIQUE(world, x, y, z))";
   private static final String INSERT_BLOCK_SQL = "INSERT OR IGNORE INTO placed_blocks (world, x, y, z) VALUES (?, ?, ?, ?)";
   private static final String DELETE_BLOCK_SQL = "DELETE FROM placed_blocks WHERE world = ? AND x = ? AND y = ? AND z = ?";
   private final Set<BlockPosition> cache = ConcurrentHashMap.newKeySet();
   private final Set<BlockPosition> pendingInserts = ConcurrentHashMap.newKeySet();
   private final Set<BlockPosition> pendingDeletes = ConcurrentHashMap.newKeySet();
   private final Connection connection;
   private final ScheduledTask saveTask;

   public SqliteBlockDataManager(Path dbFile, Scheduler scheduler) {
      try {
         Class.forName("org.sqlite.JDBC");
         Properties properties = new Properties();
         properties.setProperty("foreign_keys", "on");
         properties.setProperty("busy_timeout", "1000");
         this.connection = DriverManager.getConnection("jdbc:sqlite:" + dbFile.toString(), properties);
         Statement stmt = this.connection.createStatement();

         try {
            stmt.execute(
               "CREATE TABLE IF NOT EXISTS placed_blocks (world TEXT NOT NULL, x INTEGER NOT NULL, y INTEGER NOT NULL, z INTEGER NOT NULL, UNIQUE(world, x, y, z))"
            );
         } catch (Throwable var8) {
            if (stmt != null) {
               try {
                  stmt.close();
               } catch (Throwable var7) {
                  var8.addSuppressed(var7);
               }
            }

            throw var8;
         }

         if (stmt != null) {
            stmt.close();
         }
      } catch (Exception var9) {
         throw new RuntimeException("Failed to setup SQLite database!", var9);
      }

      this.saveTask = scheduler.asyncTimer(this::save, 100L, 100L);
   }

   private void save() {
      if (!this.pendingInserts.isEmpty() || !this.pendingDeletes.isEmpty()) {
         try {
            this.connection.setAutoCommit(false);
            if (!this.pendingDeletes.isEmpty()) {
               PreparedStatement stmt = this.connection.prepareStatement("DELETE FROM placed_blocks WHERE world = ? AND x = ? AND y = ? AND z = ?");

               try {
                  for (BlockPosition pos : this.pendingDeletes) {
                     stmt.setString(1, pos.getWorld());
                     stmt.setInt(2, pos.getX());
                     stmt.setInt(3, pos.getY());
                     stmt.setInt(4, pos.getZ());
                     stmt.addBatch();
                  }

                  stmt.executeBatch();
               } catch (Throwable var21) {
                  if (stmt != null) {
                     try {
                        stmt.close();
                     } catch (Throwable var19) {
                        var21.addSuppressed(var19);
                     }
                  }

                  throw var21;
               }

               if (stmt != null) {
                  stmt.close();
               }
            }

            if (!this.pendingInserts.isEmpty()) {
               PreparedStatement stmt = this.connection.prepareStatement("INSERT OR IGNORE INTO placed_blocks (world, x, y, z) VALUES (?, ?, ?, ?)");

               try {
                  for (BlockPosition pos : this.pendingInserts) {
                     stmt.setString(1, pos.getWorld());
                     stmt.setInt(2, pos.getX());
                     stmt.setInt(3, pos.getY());
                     stmt.setInt(4, pos.getZ());
                     stmt.addBatch();
                  }

                  stmt.executeBatch();
               } catch (Throwable var20) {
                  if (stmt != null) {
                     try {
                        stmt.close();
                     } catch (Throwable var18) {
                        var20.addSuppressed(var18);
                     }
                  }

                  throw var20;
               }

               if (stmt != null) {
                  stmt.close();
               }
            }

            this.connection.commit();
            this.pendingInserts.clear();
            this.pendingDeletes.clear();
         } catch (SQLException var22) {
            try {
               this.connection.rollback();
            } catch (SQLException var17) {
               var17.printStackTrace();
            }

            var22.printStackTrace();
         } finally {
            try {
               this.connection.setAutoCommit(true);
            } catch (SQLException var16) {
               var16.printStackTrace();
            }
         }
      }
   }

   @Override
   public boolean isPlacedBlock(BlockPosition pos) {
      return this.cache.contains(pos);
   }

   @Override
   public void addPlacedBlock(BlockPosition pos) {
      this.cache.add(pos);
      this.pendingDeletes.remove(pos);
      this.pendingInserts.add(pos);
   }

   @Override
   public void removePlacedBlock(BlockPosition pos) {
      this.cache.remove(pos);
      this.pendingInserts.remove(pos);
      this.pendingDeletes.add(pos);
   }

   @Override
   public void load() {
      this.cache.clear();
      this.pendingInserts.clear();
      this.pendingDeletes.clear();

      try {
         Statement stmt = this.connection.createStatement();

         try {
            ResultSet rs = stmt.executeQuery("SELECT world, x, y, z FROM placed_blocks");

            try {
               while (rs.next()) {
                  this.cache.add(new BlockPosition(rs.getString("world"), rs.getInt("x"), rs.getInt("y"), rs.getInt("z")));
               }
            } catch (Throwable var7) {
               if (rs != null) {
                  try {
                     rs.close();
                  } catch (Throwable var6) {
                     var7.addSuppressed(var6);
                  }
               }

               throw var7;
            }

            if (rs != null) {
               rs.close();
            }
         } catch (Throwable var8) {
            if (stmt != null) {
               try {
                  stmt.close();
               } catch (Throwable var5) {
                  var8.addSuppressed(var5);
               }
            }

            throw var8;
         }

         if (stmt != null) {
            stmt.close();
         }
      } catch (SQLException var9) {
         var9.printStackTrace();
      }
   }

   @Override
   public void close() {
      this.saveTask.stop();
      this.save();
      this.cache.clear();
      this.pendingInserts.clear();
      this.pendingDeletes.clear();

      try {
         if (this.connection != null && !this.connection.isClosed()) {
            this.connection.close();
         }
      } catch (SQLException var2) {
         var2.printStackTrace();
      }
   }
}
