package ac.grim.grimac.internal.storage.migrate;

import ac.grim.grimac.shaded.jetbrains.annotations.ApiStatus;
import ac.grim.grimac.shaded.jetbrains.annotations.Nullable;
import java.nio.ByteBuffer;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

@ApiStatus.Internal
public final class V0Reader {
   private final String jdbcUrl;
   @Nullable
   private final String username;
   @Nullable
   private final String password;

   public V0Reader(String jdbcUrl) {
      this(jdbcUrl, null, null);
   }

   public V0Reader(String jdbcUrl, @Nullable String username, @Nullable String password) {
      this.jdbcUrl = jdbcUrl;
      this.username = username;
      this.password = password;
   }

   private Connection open() throws SQLException {
      return this.username == null && this.password == null
         ? DriverManager.getConnection(this.jdbcUrl)
         : DriverManager.getConnection(this.jdbcUrl, this.username, this.password);
   }

   public boolean isLegacyStorePresent() {
      try {
         boolean var14;
         try (Connection c = this.open()) {
            DatabaseMetaData md = c.getMetaData();

            try (ResultSet rs = md.getTables(null, null, "grim_history_violations", null)) {
               if (rs.next()) {
                  return true;
               }
            }

            try (ResultSet rsx = md.getTables(null, null, "GRIM_HISTORY_VIOLATIONS", null)) {
               var14 = rsx.next();
            }
         }

         return var14;
      } catch (SQLException var12) {
         return false;
      }
   }

   public Map<Integer, String> loadLookup(String table, String stringColumn) throws SQLException {
      Map<Integer, String> out = new LinkedHashMap<>();

      try (
         Connection c = this.open();
         PreparedStatement ps = c.prepareStatement("SELECT id, " + stringColumn + " FROM " + table);
         ResultSet rs = ps.executeQuery();
      ) {
         while (rs.next()) {
            out.put(rs.getInt(1), rs.getString(2));
         }
      }

      return out;
   }

   public long maxViolationId() throws SQLException {
      long var4;
      try (
         Connection c = this.open();
         PreparedStatement ps = c.prepareStatement("SELECT COALESCE(MAX(id), 0) FROM grim_history_violations");
         ResultSet rs = ps.executeQuery();
      ) {
         var4 = rs.next() ? rs.getLong(1) : 0L;
      }

      return var4;
   }

   public List<V0Reader.V0Violation> readChunk(long afterId, int limit) throws SQLException {
      List<V0Reader.V0Violation> out = new ArrayList<>(limit);

      try (
         Connection c = this.open();
         PreparedStatement ps = c.prepareStatement(
            "SELECT id, server_id, uuid, check_name_id, verbose, vl, created_at, grim_version_id, client_brand_id, client_version_id, server_version_id FROM grim_history_violations WHERE id > ? ORDER BY uuid ASC, created_at ASC, id ASC LIMIT ?"
         );
      ) {
         ps.setLong(1, afterId);
         ps.setInt(2, limit);

         try (ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
               out.add(mapRow(rs));
            }
         }
      }

      return out;
   }

   public void streamAll(Consumer<V0Reader.V0Violation> consumer) throws SQLException {
      try (
         Connection c = this.open();
         PreparedStatement ps = c.prepareStatement(
            "SELECT id, server_id, uuid, check_name_id, verbose, vl, created_at, grim_version_id, client_brand_id, client_version_id, server_version_id FROM grim_history_violations ORDER BY uuid ASC, created_at ASC, id ASC"
         );
      ) {
         ps.setFetchSize(1000);

         try (ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
               consumer.accept(mapRow(rs));
            }
         }
      }
   }

   private static V0Reader.V0Violation mapRow(ResultSet rs) throws SQLException {
      long id = rs.getLong(1);
      int serverId = rs.getInt(2);
      UUID player = parseLegacyUuid(rs.getBytes(3));
      int checkId = rs.getInt(4);
      String verbose = rs.getString(5);
      double vl = rs.getDouble(6);
      long createdAt = rs.getLong(7);
      int grimV = rs.getInt(8);
      int brandId = rs.getInt(9);
      int clientV = rs.getInt(10);
      int serverV = rs.getInt(11);
      return new V0Reader.V0Violation(id, player, serverId, checkId, grimV, brandId, clientV, serverV, vl, createdAt, verbose);
   }

   private static UUID parseLegacyUuid(byte[] bytes) {
      if (bytes != null && bytes.length == 16) {
         ByteBuffer bb = ByteBuffer.wrap(bytes);
         return new UUID(bb.getLong(), bb.getLong());
      } else {
         throw new IllegalStateException("v0 uuid bytes must be 16, got " + (bytes == null ? -1 : bytes.length));
      }
   }

   public record V0Violation(
      long legacyId,
      UUID playerUuid,
      int serverId,
      int checkNameId,
      int grimVersionId,
      int clientBrandId,
      int clientVersionId,
      int serverVersionId,
      double vl,
      long createdAtEpochMs,
      String verbose
   ) {
   }
}
