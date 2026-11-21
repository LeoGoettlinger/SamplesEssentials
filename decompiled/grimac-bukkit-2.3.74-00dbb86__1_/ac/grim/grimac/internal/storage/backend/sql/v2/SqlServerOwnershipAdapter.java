package ac.grim.grimac.internal.storage.backend.sql.v2;

import ac.grim.grimac.api.storage.backend.BackendException;
import ac.grim.grimac.api.storage.instance.OwnershipClaimResult;
import ac.grim.grimac.api.storage.instance.OwnershipRenewResult;
import ac.grim.grimac.api.storage.instance.ServerOwnershipAdapter;
import ac.grim.grimac.api.storage.instance.ServerOwnershipMetadata;
import ac.grim.grimac.api.storage.instance.ServerOwnershipSnapshot;
import ac.grim.grimac.api.storage.registry.StoreId;
import ac.grim.grimac.internal.storage.backend.sql.v2.dialect.SqlDialect;
import ac.grim.grimac.shaded.jetbrains.annotations.ApiStatus;
import ac.grim.grimac.shaded.jetbrains.annotations.NotNull;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sql.DataSource;

@ApiStatus.Internal
public final class SqlServerOwnershipAdapter implements ServerOwnershipAdapter {
   @NotNull
   private final DataSource ds;
   @NotNull
   private final SqlDialect dialect;
   @NotNull
   private final Logger logger;

   public SqlServerOwnershipAdapter(@NotNull DataSource ds, @NotNull SqlDialect dialect, @NotNull Logger logger) {
      this.ds = ds;
      this.dialect = dialect;
      this.logger = logger;
   }

   @Override
   public void ensureStore(@NotNull StoreId id) throws BackendException {
      String table = this.q(id.name());

      try {
         try (
            Connection c = this.ds.getConnection();
            Statement s = c.createStatement();
         ) {
            s.executeUpdate(
               "CREATE TABLE IF NOT EXISTS "
                  + table
                  + " ("
                  + this.q("persistent_id")
                  + " "
                  + this.textColumnType()
                  + " PRIMARY KEY, "
                  + this.q("owner_startup_id")
                  + " "
                  + this.textColumnType()
                  + " NOT NULL, "
                  + this.q("fence")
                  + " "
                  + this.textColumnType()
                  + " NOT NULL, "
                  + this.q("lease_expires_at_ms")
                  + " BIGINT NOT NULL, "
                  + this.q("last_renewed_at_ms")
                  + " BIGINT NOT NULL, "
                  + this.q("closed_at_ms")
                  + " BIGINT NOT NULL, "
                  + this.q("close_reason")
                  + " "
                  + this.textColumnType()
                  + ", "
                  + this.q("server_name")
                  + " "
                  + this.textColumnType()
                  + ", "
                  + this.q("hostname")
                  + " "
                  + this.textColumnType()
                  + ", "
                  + this.q("grim_version")
                  + " "
                  + this.textColumnType()
                  + ", "
                  + this.q("server_version")
                  + " "
                  + this.textColumnType()
                  + ")"
            );
         }
      } catch (SQLException var11) {
         throw new BackendException("failed to ensure server ownership table " + id.name(), var11);
      }
   }

   @Override
   public long dbNowEpochMs() throws BackendException {
      try {
         long var2;
         try (Connection c = this.ds.getConnection()) {
            var2 = this.dbNowEpochMs(c);
         }

         return var2;
      } catch (SQLException var6) {
         throw new BackendException("failed to read DB time", var6);
      }
   }

   @NotNull
   @Override
   public OwnershipClaimResult claimOwnership(
      @NotNull StoreId id, @NotNull UUID persistentId, @NotNull UUID startupId, @NotNull UUID fence, long ttlMs, @NotNull ServerOwnershipMetadata metadata
   ) throws BackendException {
      SQLException lastDuplicate = null;

      for (int attempt = 0; attempt < 3; attempt++) {
         try {
            OwnershipClaimResult var17;
            try (Connection c = this.ds.getConnection()) {
               boolean originalAutoCommit = c.getAutoCommit();

               try {
                  c.setAutoCommit(false);
                  long now = this.dbNowEpochMs(c);
                  ServerOwnershipSnapshot previous = this.select(c, id, persistentId, true).orElse(null);
                  if (previous == null) {
                     this.insert(c, id, persistentId, startupId, fence, now, now + ttlMs, metadata);
                     c.commit();
                     return OwnershipClaimResult.claimed(persistentId, startupId, fence, now, now + ttlMs, null);
                  }

                  boolean alreadyMine = startupId.equals(previous.ownerStartupId()) && fence.equals(previous.fence());
                  boolean claimable = alreadyMine || previous.closedAtEpochMs() != 0L || previous.leaseExpiresAtEpochMs() <= now;
                  if (!claimable) {
                     c.commit();
                     return OwnershipClaimResult.denied(persistentId, startupId, fence, now, previous);
                  }

                  this.updateOwner(c, id, persistentId, startupId, fence, now, now + ttlMs, metadata);
                  c.commit();
                  var17 = OwnershipClaimResult.claimed(persistentId, startupId, fence, now, now + ttlMs, previous);
               } catch (SQLException var33) {
                  rollbackQuietly(c);
                  if (!this.isDuplicate(var33)) {
                     throw var33;
                  }

                  lastDuplicate = var33;
                  continue;
               } finally {
                  try {
                     c.setAutoCommit(originalAutoCommit);
                  } catch (SQLException var32) {
                  }
               }
            }

            return var17;
         } catch (SQLException var36) {
            throw new BackendException("failed to claim server ownership", var36);
         }
      }

      throw new BackendException("failed to claim server ownership after duplicate-key retries", lastDuplicate);
   }

   @NotNull
   @Override
   public OwnershipRenewResult renewOwnership(@NotNull StoreId id, @NotNull UUID persistentId, @NotNull UUID startupId, @NotNull UUID fence, long ttlMs) throws BackendException {
      try {
         OwnershipRenewResult var19;
         try (Connection c = this.ds.getConnection()) {
            long now = this.dbNowEpochMs(c);
            String sql = "UPDATE "
               + this.q(id.name())
               + " SET "
               + this.q("lease_expires_at_ms")
               + " = ?, "
               + this.q("last_renewed_at_ms")
               + " = ?, "
               + this.q("closed_at_ms")
               + " = 0, "
               + this.q("close_reason")
               + " = NULL WHERE "
               + this.q("persistent_id")
               + " = ? AND "
               + this.q("owner_startup_id")
               + " = ? AND "
               + this.q("fence")
               + " = ? AND "
               + this.q("closed_at_ms")
               + " = 0 AND "
               + this.q("lease_expires_at_ms")
               + " > ?";

            try (PreparedStatement ps = c.prepareStatement(sql)) {
               ps.setLong(1, now + ttlMs);
               ps.setLong(2, now);
               this.bindUuid(ps, 3, persistentId);
               this.bindUuid(ps, 4, startupId);
               this.bindUuid(ps, 5, fence);
               ps.setLong(6, now);
               int rows = ps.executeUpdate();
               if (rows == 1) {
                  return OwnershipRenewResult.renewed(persistentId, startupId, fence, now, now + ttlMs);
               }

               var19 = OwnershipRenewResult.lost(persistentId, startupId, fence, now);
            }
         }

         return var19;
      } catch (SQLException var18) {
         throw new BackendException("failed to renew server ownership", var18);
      }
   }

   @Override
   public boolean closeOwnership(@NotNull StoreId id, @NotNull UUID persistentId, @NotNull UUID startupId, @NotNull UUID fence, @NotNull String reason) throws BackendException {
      try {
         boolean var11;
         try (Connection c = this.ds.getConnection()) {
            long now = this.dbNowEpochMs(c);
            String sql = "UPDATE "
               + this.q(id.name())
               + " SET "
               + this.q("closed_at_ms")
               + " = ?, "
               + this.q("close_reason")
               + " = ? WHERE "
               + this.q("persistent_id")
               + " = ? AND "
               + this.q("owner_startup_id")
               + " = ? AND "
               + this.q("fence")
               + " = ? AND "
               + this.q("closed_at_ms")
               + " = 0";

            try (PreparedStatement ps = c.prepareStatement(sql)) {
               ps.setLong(1, now);
               ps.setString(2, reason);
               this.bindUuid(ps, 3, persistentId);
               this.bindUuid(ps, 4, startupId);
               this.bindUuid(ps, 5, fence);
               var11 = ps.executeUpdate() == 1;
            }
         }

         return var11;
      } catch (SQLException var17) {
         throw new BackendException("failed to close server ownership", var17);
      }
   }

   @NotNull
   @Override
   public Optional<ServerOwnershipSnapshot> readOwnership(@NotNull StoreId id, @NotNull UUID persistentId) throws BackendException {
      try {
         Optional var4;
         try (Connection c = this.ds.getConnection()) {
            var4 = this.select(c, id, persistentId, false);
         }

         return var4;
      } catch (SQLException var8) {
         throw new BackendException("failed to read server ownership", var8);
      }
   }

   private long dbNowEpochMs(@NotNull Connection c) throws SQLException {
      String s = this.dialect.name();

      String sql = switch (s) {
         case "postgres" -> "SELECT CAST(EXTRACT(EPOCH FROM CURRENT_TIMESTAMP) * 1000 AS BIGINT)";
         case "mysql" -> "SELECT CAST(UNIX_TIMESTAMP(CURRENT_TIMESTAMP(3)) * 1000 AS UNSIGNED)";
         case "sqlite" -> "SELECT CAST((julianday('now') - 2440587.5) * 86400000 AS INTEGER)";
         default -> "SELECT CAST(EXTRACT(EPOCH FROM CURRENT_TIMESTAMP) * 1000 AS BIGINT)";
      };

      long var5;
      try (
         Statement s = c.createStatement();
         ResultSet rs = s.executeQuery(sql);
      ) {
         if (!rs.next()) {
            throw new SQLException("DB time query returned no rows");
         }

         var5 = rs.getLong(1);
      }

      return var5;
   }

   private Optional<ServerOwnershipSnapshot> select(@NotNull Connection c, @NotNull StoreId id, @NotNull UUID persistentId, boolean forUpdate) throws SQLException {
      String sql = "SELECT * FROM "
         + this.q(id.name())
         + " WHERE "
         + this.q("persistent_id")
         + " = ?"
         + (forUpdate && !"sqlite".equals(this.dialect.name()) ? " FOR UPDATE" : "");

      Optional var14;
      try (PreparedStatement ps = c.prepareStatement(sql)) {
         this.bindUuid(ps, 1, persistentId);

         try (ResultSet rs = ps.executeQuery()) {
            if (!rs.next()) {
               return Optional.empty();
            }

            var14 = Optional.of(this.readSnapshot(rs));
         }
      }

      return var14;
   }

   private void insert(
      @NotNull Connection c,
      @NotNull StoreId id,
      @NotNull UUID persistentId,
      @NotNull UUID startupId,
      @NotNull UUID fence,
      long now,
      long leaseExpiresAt,
      @NotNull ServerOwnershipMetadata metadata
   ) throws SQLException {
      String sql = "INSERT INTO "
         + this.q(id.name())
         + " ("
         + this.q("persistent_id")
         + ", "
         + this.q("owner_startup_id")
         + ", "
         + this.q("fence")
         + ", "
         + this.q("lease_expires_at_ms")
         + ", "
         + this.q("last_renewed_at_ms")
         + ", "
         + this.q("closed_at_ms")
         + ", "
         + this.q("close_reason")
         + ", "
         + this.q("server_name")
         + ", "
         + this.q("hostname")
         + ", "
         + this.q("grim_version")
         + ", "
         + this.q("server_version")
         + ") VALUES (?, ?, ?, ?, ?, 0, NULL, ?, ?, ?, ?)";

      try (PreparedStatement ps = c.prepareStatement(sql)) {
         this.bindOwnerFields(ps, persistentId, startupId, fence, leaseExpiresAt, now, metadata);
         ps.executeUpdate();
      }
   }

   private void updateOwner(
      @NotNull Connection c,
      @NotNull StoreId id,
      @NotNull UUID persistentId,
      @NotNull UUID startupId,
      @NotNull UUID fence,
      long now,
      long leaseExpiresAt,
      @NotNull ServerOwnershipMetadata metadata
   ) throws SQLException {
      String sql = "UPDATE "
         + this.q(id.name())
         + " SET "
         + this.q("owner_startup_id")
         + " = ?, "
         + this.q("fence")
         + " = ?, "
         + this.q("lease_expires_at_ms")
         + " = ?, "
         + this.q("last_renewed_at_ms")
         + " = ?, "
         + this.q("closed_at_ms")
         + " = 0, "
         + this.q("close_reason")
         + " = NULL, "
         + this.q("server_name")
         + " = ?, "
         + this.q("hostname")
         + " = ?, "
         + this.q("grim_version")
         + " = ?, "
         + this.q("server_version")
         + " = ? WHERE "
         + this.q("persistent_id")
         + " = ?";

      try (PreparedStatement ps = c.prepareStatement(sql)) {
         this.bindUuid(ps, 1, startupId);
         this.bindUuid(ps, 2, fence);
         ps.setLong(3, leaseExpiresAt);
         ps.setLong(4, now);
         ps.setString(5, metadata.serverName());
         ps.setString(6, metadata.hostname());
         ps.setString(7, metadata.grimVersion());
         ps.setString(8, metadata.serverVersionString());
         this.bindUuid(ps, 9, persistentId);
         ps.executeUpdate();
      }
   }

   private void bindOwnerFields(
      @NotNull PreparedStatement ps,
      @NotNull UUID persistentId,
      @NotNull UUID startupId,
      @NotNull UUID fence,
      long leaseExpiresAt,
      long now,
      @NotNull ServerOwnershipMetadata metadata
   ) throws SQLException {
      this.bindUuid(ps, 1, persistentId);
      this.bindUuid(ps, 2, startupId);
      this.bindUuid(ps, 3, fence);
      ps.setLong(4, leaseExpiresAt);
      ps.setLong(5, now);
      ps.setString(6, metadata.serverName());
      ps.setString(7, metadata.hostname());
      ps.setString(8, metadata.grimVersion());
      ps.setString(9, metadata.serverVersionString());
   }

   @NotNull
   private ServerOwnershipSnapshot readSnapshot(@NotNull ResultSet rs) throws SQLException {
      return new ServerOwnershipSnapshot(
         this.readUuid(rs, "persistent_id"),
         this.readUuid(rs, "owner_startup_id"),
         this.readUuid(rs, "fence"),
         rs.getLong("lease_expires_at_ms"),
         rs.getLong("last_renewed_at_ms"),
         rs.getLong("closed_at_ms"),
         rs.getString("close_reason"),
         rs.getString("server_name"),
         rs.getString("hostname"),
         rs.getString("grim_version"),
         rs.getString("server_version")
      );
   }

   private void bindUuid(@NotNull PreparedStatement ps, int idx, @NotNull UUID uuid) throws SQLException {
      ps.setString(idx, uuid.toString());
   }

   @NotNull
   private UUID readUuid(@NotNull ResultSet rs, @NotNull String column) throws SQLException {
      Object value = rs.getObject(column);
      if (value instanceof UUID u) {
         return u;
      } else if (value instanceof String s) {
         return UUID.fromString(s);
      } else {
         throw new SQLException("cannot read UUID column " + column + " from " + (value == null ? "null" : value.getClass().getName()));
      }
   }

   @NotNull
   private String textColumnType() {
      return "mysql".equals(this.dialect.name()) ? "VARCHAR(512)" : "TEXT";
   }

   @NotNull
   private String q(@NotNull String identifier) {
      return this.dialect.quoteIdentifier(identifier);
   }

   private static void rollbackQuietly(@NotNull Connection c) {
      try {
         c.rollback();
      } catch (SQLException var2) {
      }
   }

   private boolean isDuplicate(@NotNull SQLException e) {
      String state = e.getSQLState();
      if (state != null && state.startsWith("23")) {
         return true;
      } else {
         String message = e.getMessage();
         boolean duplicate = message != null && message.toLowerCase(Locale.ROOT).contains("duplicate");
         if (duplicate) {
            this.logger.log(Level.FINE, "duplicate ownership insert race; retrying", (Throwable)e);
         }

         return duplicate;
      }
   }
}
