package ac.grim.grimac.internal.storage.backend.sqlite.writers;

import ac.grim.grimac.api.storage.config.TableNames;
import ac.grim.grimac.internal.storage.util.UuidCodec;
import ac.grim.grimac.shaded.jetbrains.annotations.ApiStatus;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Locale;
import java.util.UUID;

@ApiStatus.Internal
final class LegacyIdentityUpserter implements IdentityUpserter {
   private final PreparedStatement insertIgnore;
   private final PreparedStatement update;

   LegacyIdentityUpserter(Connection c, TableNames t) throws SQLException {
      this.insertIgnore = c.prepareStatement(
         "INSERT OR IGNORE INTO " + t.players() + "(uuid, current_name, current_name_lower, first_seen, last_seen) VALUES (?, ?, ?, ?, ?)"
      );
      this.update = c.prepareStatement(
         "UPDATE "
            + t.players()
            + " SET current_name = ?, current_name_lower = ?, first_seen = min(first_seen, ?), last_seen = max(last_seen, ?) WHERE uuid = ?"
      );
   }

   @Override
   public void addBatch(UUID uuid, String currentName, long firstSeenEpochMs, long lastSeenEpochMs) throws SQLException {
      byte[] uuidBytes = UuidCodec.toBytes(uuid);
      String currentNameLower = currentName == null ? null : currentName.toLowerCase(Locale.ROOT);
      this.insertIgnore.setBytes(1, uuidBytes);
      this.insertIgnore.setString(2, currentName);
      this.insertIgnore.setString(3, currentNameLower);
      this.insertIgnore.setLong(4, firstSeenEpochMs);
      this.insertIgnore.setLong(5, lastSeenEpochMs);
      this.insertIgnore.addBatch();
      this.update.setString(1, currentName);
      this.update.setString(2, currentNameLower);
      this.update.setLong(3, firstSeenEpochMs);
      this.update.setLong(4, lastSeenEpochMs);
      this.update.setBytes(5, uuidBytes);
      this.update.addBatch();
   }

   @Override
   public void executeBatch() throws SQLException {
      this.insertIgnore.executeBatch();
      this.update.executeBatch();
   }

   @Override
   public void close() throws SQLException {
      SQLException first = null;

      try {
         this.insertIgnore.close();
      } catch (SQLException var3) {
         first = var3;
      }

      try {
         this.update.close();
      } catch (SQLException var4) {
         if (first == null) {
            first = var4;
         } else {
            first.addSuppressed(var4);
         }
      }

      if (first != null) {
         throw first;
      }
   }
}
