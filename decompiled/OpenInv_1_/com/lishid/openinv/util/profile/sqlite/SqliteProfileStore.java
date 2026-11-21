package com.lishid.openinv.util.profile.sqlite;

import com.github.jikoo.openinv.lib.nahu.scheduler-wrapper.WrappedJavaPlugin;
import com.github.jikoo.openinv.lib.planarwrappers.function.ThrowingFunction;
import com.lishid.openinv.util.profile.BatchProfileStore;
import com.lishid.openinv.util.profile.OfflinePlayerImporter;
import com.lishid.openinv.util.profile.Profile;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Iterator;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.sqlite.Function;

public class SqliteProfileStore extends BatchProfileStore {
   private static final int BATCH_SIZE = 1000;
   private static final int MAX_BATCHES = 20;
   private Connection connection;

   public SqliteProfileStore(@NotNull WrappedJavaPlugin plugin) {
      super(plugin);
   }

   @Override
   public void setup() throws Exception {
      Class.forName("org.sqlite.JDBC");
      this.connection = DriverManager.getConnection("jdbc:sqlite:" + this.plugin.getDataFolder().getAbsolutePath() + "/profiles.db");

      try (Statement statement = this.connection.createStatement()) {
         statement.executeUpdate(
            "CREATE TABLE IF NOT EXISTS profiles(\n  name TEXT PRIMARY KEY,\n  uuid_least INTEGER NOT NULL,\n  uuid_most INTEGER NOT NULL,\n  last_update TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP\n) WITHOUT ROWID\n"
         );
         statement.executeUpdate("CREATE TABLE IF NOT EXISTS openinv_meta(\n  name TEXT PRIMARY KEY,\n  value INTEGER NOT NULL\n)\n");
      }

      Function.create(this.connection, "JaroWinkler", new JaroWinklerFunction());
   }

   @Override
   public void shutdown() {
      super.shutdown();

      try {
         if (!this.connection.getAutoCommit()) {
            this.connection.commit();
         }

         this.connection.close();
      } catch (SQLException var2) {
         this.plugin.getLogger().log(Level.WARNING, "Exception closing database: " + var2.getMessage(), (Throwable)var2);
      }
   }

   @Override
   public void tryImport() throws SQLException {
      try (PreparedStatement statement = this.connection.prepareStatement("SELECT value FROM openinv_meta WHERE name = 'imported'")) {
         ResultSet resultSet = statement.executeQuery();
         if (resultSet.next() && resultSet.getInt("value") == 1) {
            return;
         }
      }

      this.plugin.getLogger().info("Indexing player names.");
      (new OfflinePlayerImporter(this, 1000) {
            @Override
            public void onComplete() {
               try (PreparedStatement statementx = SqliteProfileStore.this.connection
                     .prepareStatement("INSERT INTO openinv_meta(name, value) VALUES ('imported', 1)")) {
                  statementx.executeUpdate();
                  SqliteProfileStore.this.plugin.getLogger().info("Indexed player names successfully.");
               } catch (SQLException var6) {
                  SqliteProfileStore.this.plugin.getLogger().log(Level.WARNING, "Exception marking import complete: " + var6.getMessage(), (Throwable)var6);
               }
            }
         })
         .runTaskAsynchronously(this.plugin);
   }

   @Override
   protected void pushBatch(@NotNull Set<Profile> batch) {
      if (!batch.isEmpty()) {
         int batchSize = Math.min(batch.size(), 1000);
         int rem = batch.size() % batchSize;
         Iterator<Profile> iterator = batch.iterator();

         try {
            boolean autoCommit = this.connection.getAutoCommit();
            if (autoCommit) {
               this.connection.setAutoCommit(false);
            }

            this.pushBatch(iterator, batch.size(), batchSize);
            if (rem > 0) {
               this.pushBatch(iterator, rem, rem);
            }

            this.connection.commit();
            if (autoCommit) {
               this.connection.setAutoCommit(true);
            }
         } catch (SQLException var6) {
            this.plugin.getLogger().log(Level.WARNING, "Encountered an exception updating profiles", (Throwable)var6);
         }
      }
   }

   private void pushBatch(Iterator<Profile> iterator, int iteratorSize, int batchSize) throws SQLException {
      try (PreparedStatement upsert = this.createBulkUpsertProfile(batchSize)) {
         for (int batchIndex = 0; batchIndex < iteratorSize / batchSize; batchIndex++) {
            for (int entryIndex = 0; entryIndex < batchSize; entryIndex++) {
               int startIndex = entryIndex * 3;
               Profile profile = iterator.next();
               upsert.setString(startIndex + 1, profile.name());
               upsert.setLong(startIndex + 2, profile.id().getLeastSignificantBits());
               upsert.setLong(startIndex + 3, profile.id().getMostSignificantBits());
            }

            upsert.addBatch();
            if ((batchIndex + 1) % 20 == 0) {
               upsert.executeBatch();
               this.connection.commit();
            }
         }

         upsert.executeBatch();
      }
   }

   @NotNull
   private PreparedStatement createBulkUpsertProfile(int count) throws SQLException {
      return this.connection
         .prepareStatement(
            "INSERT INTO profiles(name, uuid_least, uuid_most)\n  VALUES (?, ?, ?)\n"
               + ", (?, ?, ?)".repeat(count - 1)
               + "  ON CONFLICT(name) DO UPDATE SET\n    uuid_least=excluded.uuid_least,\n    uuid_most=excluded.uuid_most,\n    last_update=CURRENT_TIMESTAMP\n"
         );
   }

   @Nullable
   @Override
   public Profile getProfileExact(@NotNull String name) {
      return this.getProfile(name, text -> {
         PreparedStatement statement = this.connection.prepareStatement("SELECT name, uuid_least, uuid_most FROM `profiles` WHERE name = ?");
         statement.setString(1, text);
         return statement;
      });
   }

   @Nullable
   @Override
   public Profile getProfileInexact(@NotNull String search) {
      return this.getProfile(
         search,
         text -> {
            PreparedStatement statement = this.connection
               .prepareStatement("SELECT name, uuid_least, uuid_most FROM `profiles`\n  WHERE name LIKE ?\n  ORDER BY JaroWinkler(?, name) DESC\n  LIMIT 1\n");
            statement.setString(1, this.getLikePrefix(text));
            statement.setString(2, text);
            return statement;
         }
      );
   }

   @NotNull
   private String getLikePrefix(@NotNull String search) {
      StringBuilder prefix = new StringBuilder();
      int searchLen = search.length();
      if (searchLen > 0) {
         char first = search.charAt(0);
         prefix.append(first);
         if (searchLen > 1 && (first == '.' || first == '*')) {
            prefix.append(search.charAt(1));
         }
      }

      prefix.append('%');
      return prefix.toString();
   }

   @Nullable
   private Profile getProfile(@NotNull String text, @NotNull ThrowingFunction<String, PreparedStatement, SQLException> create) {
      try (PreparedStatement statement = create.apply(text)) {
         ResultSet resultSet = statement.executeQuery();
         if (resultSet.next()) {
            String name = resultSet.getString(1);
            if (name != null && !name.isEmpty()) {
               UUID uuid = new UUID(resultSet.getLong(3), resultSet.getLong(2));
               return new Profile(name, uuid);
            } else {
               return null;
            }
         } else {
            return null;
         }
      } catch (SQLException var10) {
         this.plugin.getLogger().log(Level.WARNING, "Encountered an exception retrieving profile", (Throwable)var10);
         return null;
      }
   }
}
