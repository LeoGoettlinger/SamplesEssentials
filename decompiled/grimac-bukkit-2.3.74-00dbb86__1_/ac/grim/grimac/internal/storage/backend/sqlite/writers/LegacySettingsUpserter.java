package ac.grim.grimac.internal.storage.backend.sqlite.writers;

import ac.grim.grimac.api.storage.config.TableNames;
import ac.grim.grimac.shaded.jetbrains.annotations.ApiStatus;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

@ApiStatus.Internal
final class LegacySettingsUpserter implements SettingsUpserter {
   private final PreparedStatement insertIgnore;
   private final PreparedStatement update;

   LegacySettingsUpserter(Connection c, TableNames t) throws SQLException {
      this.insertIgnore = c.prepareStatement("INSERT OR IGNORE INTO " + t.settings() + "(scope, scope_key, key, value, updated_at) VALUES (?, ?, ?, ?, ?)");
      this.update = c.prepareStatement("UPDATE " + t.settings() + " SET value = ?, updated_at = ? WHERE scope = ? AND scope_key = ? AND key = ?");
   }

   @Override
   public void addBatch(String scope, String scopeKey, String key, byte[] value, long updatedEpochMs) throws SQLException {
      this.insertIgnore.setString(1, scope);
      this.insertIgnore.setString(2, scopeKey);
      this.insertIgnore.setString(3, key);
      this.insertIgnore.setBytes(4, value);
      this.insertIgnore.setLong(5, updatedEpochMs);
      this.insertIgnore.addBatch();
      this.update.setBytes(1, value);
      this.update.setLong(2, updatedEpochMs);
      this.update.setString(3, scope);
      this.update.setString(4, scopeKey);
      this.update.setString(5, key);
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
