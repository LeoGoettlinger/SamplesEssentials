package com.fastasyncworldedit.core.database;

import com.fastasyncworldedit.core.Fawe;
import com.fastasyncworldedit.core.IFawe;
import com.fastasyncworldedit.core.configuration.Settings;
import com.fastasyncworldedit.core.history.RollbackOptimizedHistory;
import com.fastasyncworldedit.core.util.MainUtil;
import com.fastasyncworldedit.core.util.collection.YieldIterable;
import com.fastasyncworldedit.core.util.task.AsyncNotifyQueue;
import com.sk89q.worldedit.internal.util.LogManagerCompat;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.world.World;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import java.util.stream.IntStream;
import javax.annotation.Nonnull;
import org.apache.logging.log4j.Logger;

public class RollbackDatabase extends AsyncNotifyQueue {
   private static final Logger LOGGER = LogManagerCompat.getLogger();
   private final String prefix;
   private final File dbLocation;
   private final World world;
   private final ConcurrentLinkedQueue<RollbackOptimizedHistory> historyChanges = new ConcurrentLinkedQueue<>();
   private Connection connection;

   RollbackDatabase(World world) throws SQLException, ClassNotFoundException {
      super((t, e) -> e.printStackTrace());
      this.prefix = "";
      this.world = world;
      this.dbLocation = MainUtil.getFile(
         Fawe.<IFawe>platform().getDirectory(), Settings.settings().PATHS.HISTORY + File.separator + world.getName() + File.separator + "summary.db"
      );
      this.connection = this.openConnection();

      try {
         this.init().get();
         this.purge((int)TimeUnit.DAYS.toSeconds(Settings.settings().HISTORY.DELETE_AFTER_DAYS));
      } catch (ExecutionException | InterruptedException var3) {
         throw new RuntimeException(var3);
      }
   }

   private byte[] toBytes(UUID uuid) {
      return ByteBuffer.allocate(16).putLong(uuid.getMostSignificantBits()).putLong(uuid.getLeastSignificantBits()).array();
   }

   public Future<Boolean> init() {
      return this.call(
         () -> {
            try (PreparedStatement stmt = this.connection
                  .prepareStatement(
                     "CREATE TABLE IF NOT EXISTS`"
                        + this.prefix
                        + "_edits` (`player` BLOB(16) NOT NULL,`id` INT NOT NULL, `time` INT NOT NULL,`x1` INT NOT NULL,`x2` INT NOT NULL,`z1` INT NOT NULL,`z2` INT NOT NULL,`y1` INT NOT NULL, `y2` INT NOT NULL, `size` BIGINT NOT NULL, `command` VARCHAR, PRIMARY KEY (player, id))"
                  )) {
               stmt.executeUpdate();
            }

            String alterTablePrefix = "ALTER TABLE`" + this.prefix + "edits` ";

            try (PreparedStatement stmt = this.connection.prepareStatement(alterTablePrefix + "ADD COLUMN `command` VARCHAR")) {
               stmt.executeUpdate();
            } catch (SQLException var18) {
            }

            try (PreparedStatement stmt = this.connection.prepareStatement(alterTablePrefix + "ADD COLUMN `size` BIGINT DEFAULT 0 NOT NULL")) {
               stmt.executeUpdate();
            } catch (SQLException var16) {
            }

            boolean migrated = false;

            try (PreparedStatement stmt = this.connection
                  .prepareStatement(
                     "INSERT INTO `"
                        + this.prefix
                        + "_edits` (player, id, time, x1, x2, z1, z2, y1, y2, size, command) SELECT player, id, time, x1, x2, z1, z2, y1, y2, size, command FROM `"
                        + this.prefix
                        + "edits`"
                  )) {
               stmt.executeUpdate();
               migrated = true;
            } catch (SQLException var14) {
            }

            if (migrated) {
               try (PreparedStatement stmt = this.connection.prepareStatement("DROP TABLE `" + this.prefix + "edits`")) {
                  stmt.executeUpdate();
               }
            }

            return true;
         }
      );
   }

   public Future<Integer> delete(UUID uuid, int id) {
      return this.call(() -> {
         Integer t$;
         try (PreparedStatement stmt = this.connection.prepareStatement("DELETE FROM`" + this.prefix + "_edits` WHERE `player`=? AND `id`=?")) {
            stmt.setBytes(1, this.toBytes(uuid));
            stmt.setInt(2, id);
            t$ = stmt.executeUpdate();
         }

         return t$;
      });
   }

   public Future<RollbackOptimizedHistory> getEdit(@Nonnull UUID uuid, int id) {
      return this.call(() -> {
         RollbackOptimizedHistory x2;
         try (PreparedStatement stmt = this.connection.prepareStatement("SELECT * FROM`" + this.prefix + "_edits` WHERE `player`=? AND `id`=?")) {
            stmt.setBytes(1, this.toBytes(uuid));
            stmt.setInt(2, id);
            ResultSet result = stmt.executeQuery();
            if (!result.next()) {
               return null;
            }

            x2 = this.create(result).get();
         }

         return x2;
      });
   }

   private Supplier<RollbackOptimizedHistory> create(ResultSet result) throws SQLException {
      byte[] uuidBytes = result.getBytes("player");
      int index = result.getInt("id");
      int x1 = result.getInt("x1");
      int x2 = result.getInt("x2");
      int y1 = result.getInt("y1") + 128;
      int y2 = result.getInt("y2") + 128;
      int z1 = result.getInt("z1");
      int z2 = result.getInt("z2");
      CuboidRegion region = new CuboidRegion(BlockVector3.at(x1, y1, z1), BlockVector3.at(x2, y2, z2));
      long time = result.getInt("time") * 1000L;
      long size = result.getLong("size");
      String command = result.getString("command");
      ByteBuffer bb = ByteBuffer.wrap(uuidBytes);
      long high = bb.getLong();
      long low = bb.getLong();
      UUID uuid = new UUID(high, low);
      return () -> new RollbackOptimizedHistory(this.world, uuid, index, time, size, region, command);
   }

   public Future<Integer> purge(int diff) {
      long now = System.currentTimeMillis() / 1000L;
      int then = (int)(now - diff);
      return this.call(() -> {
         Integer t$;
         try (PreparedStatement stmt = this.connection.prepareStatement("DELETE FROM`" + this.prefix + "_edits` WHERE `time`<?")) {
            stmt.setInt(1, then);
            t$ = stmt.executeUpdate();
         }

         return t$;
      });
   }

   public Iterable<Supplier<RollbackOptimizedHistory>> getEdits(BlockVector3 pos, boolean ascending) {
      return this.getEdits(null, 0L, pos, pos, false, ascending);
   }

   public Iterable<Supplier<RollbackOptimizedHistory>> getEdits(
      UUID uuid, long minTime, BlockVector3 pos1, BlockVector3 pos2, boolean delete, boolean ascending
   ) {
      YieldIterable<Supplier<RollbackOptimizedHistory>> yieldIterable = new YieldIterable<>();
      Future<Integer> future = this.call(
         () -> {
            Integer var27;
            try {
               int count = 0;
               String stmtStr = "SELECT * FROM `%s_edits`\n  WHERE `time` > ?\n    AND `x2` >= ?\n    AND `x1` <= ?\n    AND `z2` >= ?\n    AND `z1` <= ?\n    AND `y2` >= ?\n    AND `y1` <= ?\n";
               if (uuid != null) {
                  stmtStr = stmtStr + "\n    AND `player`= ?";
               }

               if (ascending) {
                  stmtStr = stmtStr + "\n  ORDER BY `time` ASC, `id` ASC";
               } else {
                  stmtStr = stmtStr + "\n  ORDER BY `time` DESC, `id` DESC";
               }

               try (PreparedStatement stmt = this.connection.prepareStatement(stmtStr.formatted(this.prefix))) {
                  stmt.setInt(1, (int)(minTime / 1000L));
                  stmt.setInt(2, pos1.x());
                  stmt.setInt(3, pos2.x());
                  stmt.setInt(4, pos1.z());
                  stmt.setInt(5, pos2.z());
                  stmt.setInt(6, pos1.y() - 128);
                  stmt.setInt(7, pos2.y() - 128);
                  if (uuid != null) {
                     byte[] uuidBytes = this.toBytes(uuid);
                     stmt.setBytes(8, uuidBytes);
                  }

                  ResultSet result = stmt.executeQuery();
                  if (!result.next()) {
                     return 0;
                  }

                  do {
                     count++;
                     Supplier<RollbackOptimizedHistory> history = this.create(result);
                     yieldIterable.accept(history);
                  } while (result.next());
               }

               if (delete && uuid != null) {
                  try (PreparedStatement stmt = this.connection
                        .prepareStatement(
                           "DELETE FROM`"
                              + this.prefix
                              + "_edits` WHERE `player`=? AND `time`>? AND `x2`>=? AND `x1`<=? AND `y2`>=? AND `y1`<=? AND `z2`>=? AND `z1`<=?"
                        )) {
                     byte[] uuidBytes = ByteBuffer.allocate(16).putLong(uuid.getMostSignificantBits()).putLong(uuid.getLeastSignificantBits()).array();
                     stmt.setBytes(1, uuidBytes);
                     stmt.setInt(2, (int)(minTime / 1000L));
                     stmt.setInt(3, pos1.x());
                     stmt.setInt(4, pos2.x());
                     stmt.setInt(5, pos1.z());
                     stmt.setInt(6, pos2.z());
                     stmt.setInt(7, pos1.y() - 128);
                     stmt.setInt(8, pos2.y() - 128);
                  }
               }

               var27 = count;
            } finally {
               yieldIterable.close();
            }

            return var27;
         }
      );
      yieldIterable.setFuture(future);
      return yieldIterable;
   }

   public Future<?> logEdit(RollbackOptimizedHistory history) {
      this.historyChanges.add(history);
      return this.call(this::sendBatch);
   }

   private boolean sendBatch() throws SQLException {
      int size = Math.min(1048572, this.historyChanges.size());
      if (size == 0) {
         return false;
      } else {
         this.commit();
         if (this.connection.getAutoCommit()) {
            this.connection.setAutoCommit(false);
         }

         RollbackOptimizedHistory[] copy = IntStream.range(0, size).mapToObj(i -> this.historyChanges.poll()).toArray(RollbackOptimizedHistory[]::new);

         try (PreparedStatement stmt = this.connection
               .prepareStatement(
                  "INSERT OR REPLACE INTO`"
                     + this.prefix
                     + "_edits` (`player`,`id`,`time`,`x1`,`x2`,`z1`,`z2`,`y1`,`y2`,`command`,`size`) VALUES(?,?,?,?,?,?,?,?,?,?,?)"
               )) {
            for (RollbackOptimizedHistory change : copy) {
               UUID uuid = change.getUUID();
               byte[] uuidBytes = this.toBytes(uuid);
               stmt.setBytes(1, uuidBytes);
               stmt.setInt(2, change.getIndex());
               stmt.setInt(3, (int)(change.getTime() / 1000L));
               BlockVector3 pos1 = change.getMinimumPoint();
               BlockVector3 pos2 = change.getMaximumPoint();
               stmt.setInt(4, pos1.x());
               stmt.setInt(5, pos2.x());
               stmt.setInt(6, pos1.z());
               stmt.setInt(7, pos2.z());
               stmt.setInt(8, pos1.y() - 128);
               stmt.setInt(9, pos2.y() - 128);
               stmt.setString(10, change.getCommand());
               stmt.setLong(11, change.longSize());
               stmt.executeUpdate();
               stmt.clearParameters();
            }
         } finally {
            this.commit();
         }

         return true;
      }
   }

   private void commit() {
      try {
         if (this.connection == null) {
            return;
         }

         if (!this.connection.getAutoCommit()) {
            this.connection.commit();
            this.connection.setAutoCommit(true);
         }
      } catch (SQLException var2) {
         var2.printStackTrace();
      }
   }

   private Connection openConnection() throws SQLException, ClassNotFoundException {
      if (this.checkConnection()) {
         return this.connection;
      } else {
         if (!Fawe.<IFawe>platform().getDirectory().exists()) {
            Fawe.<IFawe>platform().getDirectory().mkdirs();
         }

         if (!this.dbLocation.exists()) {
            try {
               this.dbLocation.getParentFile().mkdirs();
               this.dbLocation.createNewFile();
            } catch (IOException var2) {
               var2.printStackTrace();
               LOGGER.error("Unable to create the database!");
            }
         }

         Class.forName("org.sqlite.JDBC");
         this.connection = DriverManager.getConnection("jdbc:sqlite:" + this.dbLocation);
         return this.connection;
      }
   }

   private Connection forceConnection() throws SQLException, ClassNotFoundException {
      Class.forName("org.sqlite.JDBC");
      this.connection = DriverManager.getConnection("jdbc:sqlite:" + this.dbLocation);
      return this.connection;
   }

   public Connection getConnection() {
      if (this.connection == null) {
         try {
            this.forceConnection();
         } catch (SQLException | ClassNotFoundException var2) {
            var2.printStackTrace();
         }
      }

      return this.connection;
   }

   public boolean closeConnection() throws SQLException {
      if (this.connection == null) {
         return false;
      } else {
         synchronized (this) {
            if (this.connection == null) {
               return false;
            } else {
               this.connection.close();
               this.connection = null;
               return true;
            }
         }
      }
   }

   public boolean checkConnection() {
      try {
         return this.connection != null && !this.connection.isClosed();
      } catch (SQLException var2) {
         return false;
      }
   }

   @Override
   public void close() {
      try {
         this.closeConnection();
      } catch (SQLException var2) {
         var2.printStackTrace();
      }

      super.close();
   }
}
