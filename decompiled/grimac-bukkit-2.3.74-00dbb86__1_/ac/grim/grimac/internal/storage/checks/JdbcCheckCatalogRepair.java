package ac.grim.grimac.internal.storage.checks;

import ac.grim.grimac.api.storage.check.CheckCatalogRepairResult;
import ac.grim.grimac.shaded.jetbrains.annotations.ApiStatus;
import ac.grim.grimac.shaded.jetbrains.annotations.NotNull;
import ac.grim.grimac.shaded.jetbrains.annotations.Nullable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Map;
import java.util.Map.Entry;

@ApiStatus.Internal
public final class JdbcCheckCatalogRepair {
   public static final String STUB_VERSION = "0.0.0-stub";

   private JdbcCheckCatalogRepair() {
   }

   @NotNull
   public static CheckCatalogRepairResult run(
      @NotNull JdbcCheckCatalogPersistence.ConnectionFactory connections,
      @NotNull String checksTable,
      @NotNull String violationsTable,
      @NotNull Map<Integer, Integer> legacyToCatalogCheckIds,
      @Nullable String introducedVersionReplacement
   ) throws SQLException {
      CheckCatalogRepairResult var11;
      try (Connection c = connections.open()) {
         boolean autoCommit = c.getAutoCommit();
         c.setAutoCommit(false);

         try {
            long violationsUpdated = rewriteViolationIds(c, violationsTable, legacyToCatalogCheckIds);
            long versionsUpdated = repairStubVersions(c, checksTable, introducedVersionReplacement);
            c.commit();
            var11 = new CheckCatalogRepairResult(legacyToCatalogCheckIds.size(), violationsUpdated, versionsUpdated);
         } catch (RuntimeException | SQLException var25) {
            try {
               c.rollback();
            } catch (SQLException var24) {
            }

            throw var25;
         } finally {
            try {
               c.setAutoCommit(autoCommit);
            } catch (SQLException var23) {
            }
         }
      }

      return var11;
   }

   private static long rewriteViolationIds(Connection c, String violationsTable, Map<Integer, Integer> legacyToCatalogCheckIds) throws SQLException {
      if (legacyToCatalogCheckIds.isEmpty()) {
         return 0L;
      } else {
         long updated = 0L;

         try (PreparedStatement ps = c.prepareStatement("UPDATE " + violationsTable + " SET check_id = ? WHERE check_id = ?")) {
            for (Entry<Integer, Integer> e : legacyToCatalogCheckIds.entrySet()) {
               int legacyId = e.getKey();
               int catalogId = e.getValue();
               if (legacyId != catalogId) {
                  ps.setInt(1, catalogId);
                  ps.setInt(2, legacyId);
                  updated += ps.executeUpdate();
               }
            }
         }

         return updated;
      }
   }

   private static long repairStubVersions(Connection c, String checksTable, @Nullable String introducedVersionReplacement) throws SQLException {
      if (introducedVersionReplacement != null && !introducedVersionReplacement.isBlank()) {
         long var4;
         try (PreparedStatement ps = c.prepareStatement("UPDATE " + checksTable + " SET introduced_version = ? WHERE introduced_version = ?")) {
            ps.setString(1, introducedVersionReplacement);
            ps.setString(2, "0.0.0-stub");
            var4 = ps.executeUpdate();
         }

         return var4;
      } else {
         return 0L;
      }
   }
}
