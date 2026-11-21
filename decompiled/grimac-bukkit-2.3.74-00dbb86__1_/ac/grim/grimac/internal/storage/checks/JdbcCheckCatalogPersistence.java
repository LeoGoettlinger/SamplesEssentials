package ac.grim.grimac.internal.storage.checks;

import ac.grim.grimac.api.storage.check.CheckCatalogPersistence;
import ac.grim.grimac.api.storage.check.CheckCatalogRow;
import ac.grim.grimac.shaded.jetbrains.annotations.ApiStatus;
import ac.grim.grimac.shaded.jetbrains.annotations.Nullable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

@ApiStatus.Internal
public final class JdbcCheckCatalogPersistence implements CheckCatalogPersistence {
   private final JdbcCheckCatalogPersistence.ConnectionFactory connections;
   private final String checksTable;
   private final String sequenceAlignmentSql;

   public JdbcCheckCatalogPersistence(JdbcCheckCatalogPersistence.ConnectionFactory connections, String checksTable) {
      this(connections, checksTable, null);
   }

   public JdbcCheckCatalogPersistence(JdbcCheckCatalogPersistence.ConnectionFactory connections, String checksTable, @Nullable String sequenceAlignmentSql) {
      this.connections = connections;
      this.checksTable = checksTable;
      this.sequenceAlignmentSql = sequenceAlignmentSql;
   }

   @Override
   public Iterable<CheckCatalogRow> loadAll() {
      List<CheckCatalogRow> out = new ArrayList<>();

      try {
         try (
            Connection c = this.connections.open();
            PreparedStatement ps = c.prepareStatement(
               "SELECT check_id, stable_key, display, description, introduced_version, introduced_at FROM " + this.checksTable
            );
            ResultSet rs = ps.executeQuery();
         ) {
            while (rs.next()) {
               long introducedAt = rs.getLong(6);
               if (rs.wasNull()) {
                  introducedAt = 0L;
               }

               out.add(new CheckCatalogRow(rs.getInt(1), rs.getString(2), rs.getString(3), rs.getString(4), rs.getString(5), introducedAt));
            }
         }

         return out;
      } catch (SQLException var13) {
         throw new RuntimeException("failed to load " + this.checksTable, var13);
      }
   }

   @Override
   public int insert(String stableKey, @Nullable String display, @Nullable String description, @Nullable String introducedVersion, long introducedAt) {
      SQLException lastFailure = null;

      for (int attempt = 0; attempt < 5; attempt++) {
         try (Connection c = this.connections.open()) {
            boolean priorAutoCommit = c.getAutoCommit();
            c.setAutoCommit(false);

            try {
               try {
                  Integer existing = this.findExistingId(c, stableKey);
                  if (existing != null) {
                     c.commit();
                     return existing;
                  }

                  int checkId = this.nextCheckId(c);

                  try (PreparedStatement ps = c.prepareStatement(
                        "INSERT INTO "
                           + this.checksTable
                           + "(check_id, stable_key, display, description, introduced_version, introduced_at) VALUES (?, ?, ?, ?, ?, ?)"
                     )) {
                     ps.setInt(1, checkId);
                     ps.setString(2, stableKey);
                     bindNullableString(ps, 3, display);
                     bindNullableString(ps, 4, description);
                     bindNullableString(ps, 5, introducedVersion);
                     ps.setLong(6, introducedAt);
                     ps.executeUpdate();
                  }

                  this.alignSequence(c);
                  c.commit();
                  return checkId;
               } catch (SQLException var36) {
                  lastFailure = var36;

                  try {
                     c.rollback();
                  } catch (SQLException var33) {
                  }
               }

               Integer existing = this.findExistingId(stableKey);
               if (existing != null) {
                  return existing;
               }
            } finally {
               try {
                  c.setAutoCommit(priorAutoCommit);
               } catch (SQLException var32) {
               }
            }
         } catch (SQLException var39) {
            lastFailure = var39;
         }
      }

      throw new RuntimeException("failed to insert " + this.checksTable + " row for " + stableKey, lastFailure);
   }

   @Override
   public void upsert(CheckCatalogRow row) {
      this.validateNoConflict(row);

      try {
         try (Connection c = this.connections.open()) {
            try (PreparedStatement ps = c.prepareStatement(
                  "INSERT INTO "
                     + this.checksTable
                     + "(check_id, stable_key, display, description, introduced_version, introduced_at) VALUES (?, ?, ?, ?, ?, ?)"
               )) {
               this.bindRow(ps, row);
               ps.executeUpdate();
            } catch (SQLException var12) {
               SQLException duplicateOrConflict = var12;
               this.validateNoConflict(row);

               try (PreparedStatement psx = c.prepareStatement(
                     "UPDATE "
                        + this.checksTable
                        + " SET stable_key = ?, display = ?, description = ?, introduced_version = ?, introduced_at = ? WHERE check_id = ?"
                  )) {
                  psx.setString(1, row.stableKey());
                  bindNullableString(psx, 2, row.display());
                  bindNullableString(psx, 3, row.description());
                  bindNullableString(psx, 4, row.introducedVersion());
                  psx.setLong(5, row.introducedAt());
                  psx.setInt(6, row.checkId());
                  if (psx.executeUpdate() == 0) {
                     throw duplicateOrConflict;
                  }
               }
            }

            this.alignSequence(c);
         }
      } catch (SQLException var14) {
         throw new RuntimeException("failed to upsert " + this.checksTable + " row for " + row.stableKey(), var14);
      }
   }

   private void validateNoConflict(CheckCatalogRow row) {
      String stableKeyForId = this.findStableKeyById(row.checkId());
      if (stableKeyForId != null && !stableKeyForId.equals(row.stableKey())) {
         throw new IllegalStateException("check_id " + row.checkId() + " already maps to stable key " + stableKeyForId + ", cannot import " + row.stableKey());
      } else {
         Integer idForStableKey = this.findExistingId(row.stableKey());
         if (idForStableKey != null && idForStableKey != row.checkId()) {
            throw new IllegalStateException(
               "stable key " + row.stableKey() + " already maps to check_id " + idForStableKey + ", cannot import as " + row.checkId()
            );
         }
      }
   }

   @Override
   public void updateDisplayAndDescription(int checkId, @Nullable String display, @Nullable String description) {
      try {
         try (
            Connection c = this.connections.open();
            PreparedStatement ps = c.prepareStatement("UPDATE " + this.checksTable + " SET display = ?, description = ? WHERE check_id = ?");
         ) {
            bindNullableString(ps, 1, display);
            bindNullableString(ps, 2, description);
            ps.setInt(3, checkId);
            ps.executeUpdate();
         }
      } catch (SQLException var12) {
         throw new RuntimeException("failed to update " + this.checksTable + " display/description", var12);
      }
   }

   private Integer findExistingId(String stableKey) {
      try (
         Connection c = this.connections.open();
         PreparedStatement ps = c.prepareStatement("SELECT check_id FROM " + this.checksTable + " WHERE stable_key = ?");
      ) {
         ps.setString(1, stableKey);

         try (ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getInt(1) : null;
         }
      } catch (SQLException var13) {
         return null;
      }
   }

   private Integer findExistingId(Connection c, String stableKey) throws SQLException {
      try (PreparedStatement ps = c.prepareStatement("SELECT check_id FROM " + this.checksTable + " WHERE stable_key = ?")) {
         ps.setString(1, stableKey);

         try (ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
               return rs.getInt(1);
            }
         }
      }

      return null;
   }

   private int nextCheckId(Connection c) throws SQLException {
      try (
         PreparedStatement ps = c.prepareStatement("SELECT COALESCE(MAX(check_id), 0) + 1 FROM " + this.checksTable);
         ResultSet rs = ps.executeQuery();
      ) {
         if (rs.next()) {
            return rs.getInt(1);
         }
      }

      return 1;
   }

   private String findStableKeyById(int checkId) {
      try (
         Connection c = this.connections.open();
         PreparedStatement ps = c.prepareStatement("SELECT stable_key FROM " + this.checksTable + " WHERE check_id = ?");
      ) {
         ps.setInt(1, checkId);

         try (ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getString(1) : null;
         }
      } catch (SQLException var13) {
         return null;
      }
   }

   private void bindRow(PreparedStatement ps, CheckCatalogRow row) throws SQLException {
      ps.setInt(1, row.checkId());
      ps.setString(2, row.stableKey());
      bindNullableString(ps, 3, row.display());
      bindNullableString(ps, 4, row.description());
      bindNullableString(ps, 5, row.introducedVersion());
      ps.setLong(6, row.introducedAt());
   }

   private void alignSequence(Connection c) throws SQLException {
      if (this.sequenceAlignmentSql != null && !this.sequenceAlignmentSql.isBlank()) {
         try (Statement s = c.createStatement()) {
            s.execute(this.sequenceAlignmentSql);
         }
      }
   }

   private static void bindNullableString(PreparedStatement ps, int index, @Nullable String value) throws SQLException {
      if (value == null) {
         ps.setNull(index, 12);
      } else {
         ps.setString(index, value);
      }
   }

   @FunctionalInterface
   public interface ConnectionFactory {
      Connection open() throws SQLException;
   }
}
