package ac.grim.grimac.internal.storage.backend.sql.v2;

import ac.grim.grimac.api.storage.backend.BackendException;
import ac.grim.grimac.api.storage.backend.KindAdapter;
import ac.grim.grimac.api.storage.backend.StorageEventHandler;
import ac.grim.grimac.api.storage.category.Capability;
import ac.grim.grimac.api.storage.category.Category;
import ac.grim.grimac.api.storage.kind.KeyValueEvent;
import ac.grim.grimac.api.storage.kind.KeyValueScoped;
import ac.grim.grimac.api.storage.kind.Operation;
import ac.grim.grimac.api.storage.kind.ops.KeyValueScopedOps;
import ac.grim.grimac.api.storage.registry.Migration;
import ac.grim.grimac.api.storage.registry.StoreId;
import ac.grim.grimac.internal.storage.backend.sql.v2.dialect.SqlDialect;
import ac.grim.grimac.shaded.jetbrains.annotations.ApiStatus;
import ac.grim.grimac.shaded.jetbrains.annotations.NotNull;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Map.Entry;
import java.util.logging.Logger;
import javax.sql.DataSource;

@ApiStatus.Internal
public final class SqlKeyValueScopedAdapter implements KindAdapter<KeyValueScoped<?, ?>> {
   @NotNull
   private final DataSource ds;
   @NotNull
   private final SqlDialect dialect;
   @NotNull
   private final Logger logger;

   public SqlKeyValueScopedAdapter(@NotNull DataSource ds, @NotNull SqlDialect dialect, @NotNull Logger logger) {
      this.ds = ds;
      this.dialect = dialect;
      this.logger = logger;
   }

   @NotNull
   @Override
   public Class<KeyValueScoped<?, ?>> kindType() {
      return KeyValueScoped.class;
   }

   @NotNull
   @Override
   public EnumSet<Capability> subcapabilities() {
      return EnumSet.of(Capability.KIND_KV_SCOPED, Capability.ATOMIC_UPSERT);
   }

   public void ensureStore(@NotNull StoreId id, @NotNull KeyValueScoped<?, ?> kind) throws BackendException {
      String table = this.dialect.quoteIdentifier(id.name());
      String scope = this.dialect.quoteIdentifier("scope");
      String scopeKey = this.dialect.quoteIdentifier("scope_key");
      String key = this.dialect.quoteIdentifier("key");
      String value = this.dialect.quoteIdentifier("value");
      String updatedAt = this.dialect.quoteIdentifier("updated_at");
      String ddl = "CREATE TABLE IF NOT EXISTS "
         + table
         + " ("
         + scope
         + " VARCHAR(64) NOT NULL, "
         + scopeKey
         + " VARCHAR(255) NOT NULL, "
         + key
         + " VARCHAR(255) NOT NULL, "
         + value
         + " "
         + this.dialect.kvValueColumnType()
         + ", "
         + updatedAt
         + " BIGINT NOT NULL DEFAULT 0, PRIMARY KEY ("
         + scope
         + ", "
         + scopeKey
         + ", "
         + key
         + "))";

      try {
         try (
            Connection c = this.ds.getConnection();
            Statement s = c.createStatement();
         ) {
            s.executeUpdate(ddl);
         }
      } catch (SQLException var18) {
         throw new BackendException("failed to ensure KV table " + id, var18);
      }
   }

   public void dropStore(@NotNull StoreId id, @NotNull KeyValueScoped<?, ?> kind) throws BackendException {
      try {
         try (
            Connection c = this.ds.getConnection();
            Statement s = c.createStatement();
         ) {
            s.executeUpdate("DROP TABLE IF EXISTS " + this.dialect.quoteIdentifier(id.name()));
         }
      } catch (SQLException var11) {
         throw new BackendException("failed to drop KV table " + id, var11);
      }
   }

   @NotNull
   public <E> StorageEventHandler<E> writeHandler(@NotNull StoreId id, @NotNull KeyValueScoped<?, ?> kind, @NotNull Category<E> category) {
      String table = this.dialect.quoteIdentifier(id.name());
      String upsertSql = this.dialect.kvUpsertSql(table);
      String deleteSql = "DELETE FROM "
         + table
         + " WHERE "
         + this.dialect.quoteIdentifier("scope")
         + " = ? AND "
         + this.dialect.quoteIdentifier("scope_key")
         + " = ? AND "
         + this.dialect.quoteIdentifier("key")
         + " = ?";
      return (StorageEventHandler<E>)(this.dialect.usesLegacySqliteUpsert()
         ? new SqlKeyValueScopedAdapter.LegacyKVWriteHandler<>(this.legacyKvInsertSql(table), this.legacyKvUpdateSql(table), deleteSql)
         : new SqlKeyValueScopedAdapter.KVWriteHandler<>(upsertSql, deleteSql));
   }

   public <R> R execute(@NotNull StoreId id, @NotNull KeyValueScoped<?, ?> kind, @NotNull Operation<R> op) throws BackendException {
      try {
         if (op instanceof KeyValueScopedOps.GetOp<?, ?> g) {
            return (R)this.get(id, g);
         } else if (op instanceof KeyValueScopedOps.GetAllOp<?, ?> g) {
            return (R)this.getAll(id, g);
         } else if (op instanceof KeyValueScopedOps.PutOp<?, ?> p) {
            this.put(id, p);
            return null;
         } else if (op instanceof KeyValueScopedOps.PutAllOp<?, ?> p) {
            this.putAll(id, p);
            return null;
         } else if (op instanceof KeyValueScopedOps.RemoveOp<?> r) {
            this.remove(id, r);
            return null;
         } else if (op instanceof KeyValueScopedOps.RemoveAllOp<?> r) {
            this.removeAll(id, r);
            return null;
         } else if (op instanceof KeyValueScopedOps.CountOp<?> c) {
            return (R)this.count(id, c);
         } else {
            throw new UnsupportedOperationException("SqlKeyValueScopedAdapter does not handle " + op.getClass().getName());
         }
      } catch (SQLException var5) {
         throw new BackendException("sql kv execute failed for " + op.getClass().getSimpleName(), var5);
      }
   }

   @NotNull
   public List<Migration<KeyValueScoped<?, ?>>> migrations(@NotNull KeyValueScoped<?, ?> kind) {
      return List.of();
   }

   @NotNull
   private static byte[] valueAsBytes(@NotNull Object value) throws BackendException {
      if (value instanceof byte[] b) {
         return b;
      } else if (value instanceof String s) {
         return s.getBytes(StandardCharsets.UTF_8);
      } else {
         throw new BackendException("KV value must be byte[] or String, got " + value.getClass().getName());
      }
   }

   @NotNull
   private String legacyKvInsertSql(@NotNull String table) {
      String scope = this.dialect.quoteIdentifier("scope");
      String scopeKey = this.dialect.quoteIdentifier("scope_key");
      String key = this.dialect.quoteIdentifier("key");
      String value = this.dialect.quoteIdentifier("value");
      String updatedAt = this.dialect.quoteIdentifier("updated_at");
      return "INSERT OR IGNORE INTO " + table + " (" + scope + ", " + scopeKey + ", " + key + ", " + value + ", " + updatedAt + ") VALUES (?, ?, ?, ?, ?)";
   }

   @NotNull
   private String legacyKvUpdateSql(@NotNull String table) {
      String scope = this.dialect.quoteIdentifier("scope");
      String scopeKey = this.dialect.quoteIdentifier("scope_key");
      String key = this.dialect.quoteIdentifier("key");
      String value = this.dialect.quoteIdentifier("value");
      String updatedAt = this.dialect.quoteIdentifier("updated_at");
      return "UPDATE " + table + " SET " + value + " = ?, " + updatedAt + " = ? WHERE " + scope + " = ? AND " + scopeKey + " = ? AND " + key + " = ?";
   }

   private void legacyKvUpsert(
      @NotNull Connection c,
      @NotNull String insertSql,
      @NotNull String updateSql,
      @NotNull String scope,
      @NotNull String scopeKey,
      @NotNull String key,
      @NotNull byte[] value,
      long updatedAt
   ) throws SQLException {
      try (PreparedStatement insert = c.prepareStatement(insertSql)) {
         insert.setString(1, scope);
         insert.setString(2, scopeKey);
         insert.setString(3, key);
         insert.setBytes(4, value);
         insert.setLong(5, updatedAt);
         insert.executeUpdate();
      }

      try (PreparedStatement update = c.prepareStatement(updateSql)) {
         update.setBytes(1, value);
         update.setLong(2, updatedAt);
         update.setString(3, scope);
         update.setString(4, scopeKey);
         update.setString(5, key);
         update.executeUpdate();
      }
   }

   private Optional<Object> get(@NotNull StoreId id, @NotNull KeyValueScopedOps.GetOp<?, ?> op) throws SQLException {
      String table = this.dialect.quoteIdentifier(id.name());

      Optional var8;
      try (
         Connection c = this.ds.getConnection();
         PreparedStatement ps = c.prepareStatement(
            "SELECT "
               + this.dialect.quoteIdentifier("value")
               + " FROM "
               + table
               + " WHERE "
               + this.dialect.quoteIdentifier("scope")
               + " = ? AND "
               + this.dialect.quoteIdentifier("scope_key")
               + " = ? AND "
               + this.dialect.quoteIdentifier("key")
               + " = ?"
         );
      ) {
         ps.setString(1, String.valueOf(op.scope()));
         ps.setString(2, String.valueOf(op.scopeKey()));
         ps.setString(3, op.key());

         try (ResultSet rs = ps.executeQuery()) {
            if (!rs.next()) {
               return Optional.empty();
            }

            byte[] v = rs.getBytes(1);
            var8 = v == null ? Optional.empty() : Optional.of(v);
         }
      }

      return var8;
   }

   @NotNull
   private Map<String, Object> getAll(@NotNull StoreId id, @NotNull KeyValueScopedOps.GetAllOp<?, ?> op) throws SQLException {
      String table = this.dialect.quoteIdentifier(id.name());
      Map<String, Object> out = new LinkedHashMap<>();

      try (
         Connection c = this.ds.getConnection();
         PreparedStatement ps = c.prepareStatement(
            "SELECT "
               + this.dialect.quoteIdentifier("key")
               + ", "
               + this.dialect.quoteIdentifier("value")
               + " FROM "
               + table
               + " WHERE "
               + this.dialect.quoteIdentifier("scope")
               + " = ? AND "
               + this.dialect.quoteIdentifier("scope_key")
               + " = ?"
         );
      ) {
         ps.setString(1, String.valueOf(op.scope()));
         ps.setString(2, String.valueOf(op.scopeKey()));

         try (ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
               byte[] v = rs.getBytes(2);
               if (v != null) {
                  out.put(rs.getString(1), v);
               }
            }
         }
      }

      return out;
   }

   private void put(@NotNull StoreId id, @NotNull KeyValueScopedOps.PutOp<?, ?> op) throws SQLException, BackendException {
      if (op.value() == null) {
         throw new IllegalArgumentException("KV PutOp value must be non-null; use RemoveOp for deletion");
      } else {
         String table = this.dialect.quoteIdentifier(id.name());
         if (this.dialect.usesLegacySqliteUpsert()) {
            try (Connection c = this.ds.getConnection()) {
               boolean prior = c.getAutoCommit();
               c.setAutoCommit(false);

               try {
                  this.legacyKvUpsert(
                     c,
                     this.legacyKvInsertSql(table),
                     this.legacyKvUpdateSql(table),
                     String.valueOf(op.scope()),
                     op.scopeKey(),
                     op.key(),
                     valueAsBytes(op.value()),
                     System.currentTimeMillis()
                  );
                  c.commit();
               } catch (Exception var22) {
                  try {
                     c.rollback();
                  } catch (SQLException var21) {
                  }

                  throw var22;
               } finally {
                  c.setAutoCommit(prior);
               }
            }
         } else {
            String sql = this.dialect.kvUpsertSql(table);

            try (
               Connection c = this.ds.getConnection();
               PreparedStatement ps = c.prepareStatement(sql);
            ) {
               ps.setString(1, String.valueOf(op.scope()));
               ps.setString(2, op.scopeKey());
               ps.setString(3, op.key());
               ps.setBytes(4, valueAsBytes(op.value()));
               ps.setLong(5, System.currentTimeMillis());
               ps.executeUpdate();
            }
         }
      }
   }

   private void putAll(@NotNull StoreId id, @NotNull KeyValueScopedOps.PutAllOp<?, ?> op) throws SQLException, BackendException {
      String table = this.dialect.quoteIdentifier(id.name());
      String sql = this.dialect.kvUpsertSql(table);
      String legacyInsert = this.legacyKvInsertSql(table);
      String legacyUpdate = this.legacyKvUpdateSql(table);
      long now = System.currentTimeMillis();

      try (Connection c = this.ds.getConnection()) {
         boolean priorAutoCommit = c.getAutoCommit();
         c.setAutoCommit(false);

         try {
            if (this.dialect.usesLegacySqliteUpsert()) {
               for (Entry<String, ?> e : op.values().entrySet()) {
                  if (e.getValue() == null) {
                     throw new IllegalArgumentException("KV PutAllOp value for key '" + e.getKey() + "' must be non-null");
                  }

                  this.legacyKvUpsert(c, legacyInsert, legacyUpdate, String.valueOf(op.scope()), op.scopeKey(), e.getKey(), valueAsBytes(e.getValue()), now);
               }
            } else {
               try (PreparedStatement ps = c.prepareStatement(sql)) {
                  for (Entry<String, ?> e : op.values().entrySet()) {
                     if (e.getValue() == null) {
                        throw new IllegalArgumentException("KV PutAllOp value for key '" + e.getKey() + "' must be non-null");
                     }

                     ps.setString(1, String.valueOf(op.scope()));
                     ps.setString(2, op.scopeKey());
                     ps.setString(3, e.getKey());
                     ps.setBytes(4, valueAsBytes(e.getValue()));
                     ps.setLong(5, now);
                     ps.addBatch();
                  }

                  ps.executeBatch();
               }
            }

            c.commit();
         } catch (Exception var26) {
            try {
               c.rollback();
            } catch (SQLException var23) {
            }

            throw var26;
         } finally {
            c.setAutoCommit(priorAutoCommit);
         }
      }
   }

   private void remove(@NotNull StoreId id, @NotNull KeyValueScopedOps.RemoveOp<?> op) throws SQLException {
      String table = this.dialect.quoteIdentifier(id.name());

      try (
         Connection c = this.ds.getConnection();
         PreparedStatement ps = c.prepareStatement(
            "DELETE FROM "
               + table
               + " WHERE "
               + this.dialect.quoteIdentifier("scope")
               + " = ? AND "
               + this.dialect.quoteIdentifier("scope_key")
               + " = ? AND "
               + this.dialect.quoteIdentifier("key")
               + " = ?"
         );
      ) {
         ps.setString(1, String.valueOf(op.scope()));
         ps.setString(2, String.valueOf(op.scopeKey()));
         ps.setString(3, op.key());
         ps.executeUpdate();
      }
   }

   private void removeAll(@NotNull StoreId id, @NotNull KeyValueScopedOps.RemoveAllOp<?> op) throws SQLException {
      String table = this.dialect.quoteIdentifier(id.name());

      try (
         Connection c = this.ds.getConnection();
         PreparedStatement ps = c.prepareStatement(
            "DELETE FROM " + table + " WHERE " + this.dialect.quoteIdentifier("scope") + " = ? AND " + this.dialect.quoteIdentifier("scope_key") + " = ?"
         );
      ) {
         ps.setString(1, String.valueOf(op.scope()));
         ps.setString(2, String.valueOf(op.scopeKey()));
         ps.executeUpdate();
      }
   }

   private long count(@NotNull StoreId id, @NotNull KeyValueScopedOps.CountOp<?> op) throws SQLException {
      String table = this.dialect.quoteIdentifier(id.name());

      long var7;
      try (
         Connection c = this.ds.getConnection();
         PreparedStatement ps = c.prepareStatement(
            "SELECT COUNT(*) FROM "
               + table
               + " WHERE "
               + this.dialect.quoteIdentifier("scope")
               + " = ? AND "
               + this.dialect.quoteIdentifier("scope_key")
               + " = ?"
         );
      ) {
         ps.setString(1, String.valueOf(op.scope()));
         ps.setString(2, String.valueOf(op.scopeKey()));

         try (ResultSet rs = ps.executeQuery()) {
            var7 = rs.next() ? rs.getLong(1) : 0L;
         }
      }

      return var7;
   }

   private final class KVWriteHandler<E> implements StorageEventHandler<E> {
      private final String upsertSql;
      private final String deleteSql;

      KVWriteHandler(String upsertSql, String deleteSql) {
         this.upsertSql = upsertSql;
         this.deleteSql = deleteSql;
      }

      @Override
      public void onEvent(E event, long sequence, boolean endOfBatch) throws BackendException {
         KeyValueEvent<?, ?> kve = (KeyValueEvent<?, ?>)event;
         if (kve.scope != null && kve.scopeKey != null && kve.key != null) {
            try {
               try (Connection c = SqlKeyValueScopedAdapter.this.ds.getConnection()) {
                  if (kve.remove) {
                     try (PreparedStatement ps = c.prepareStatement(this.deleteSql)) {
                        ps.setString(1, String.valueOf(kve.scope));
                        ps.setString(2, String.valueOf(kve.scopeKey));
                        ps.setString(3, kve.key);
                        ps.executeUpdate();
                     }
                  } else {
                     if (kve.value == null) {
                        throw new BackendException("KV PutOp value must be non-null; use remove flag for deletion");
                     }

                     try (PreparedStatement ps = c.prepareStatement(this.upsertSql)) {
                        ps.setString(1, String.valueOf(kve.scope));
                        ps.setString(2, String.valueOf(kve.scopeKey));
                        ps.setString(3, kve.key);
                        ps.setBytes(4, SqlKeyValueScopedAdapter.valueAsBytes(kve.value));
                        ps.setLong(5, kve.updatedEpochMs);
                        ps.executeUpdate();
                     }
                  }
               }
            } catch (SQLException var16) {
               throw new BackendException("kv write failed", var16);
            }
         }
      }
   }

   private final class LegacyKVWriteHandler<E> implements StorageEventHandler<E> {
      private final String insertSql;
      private final String updateSql;
      private final String deleteSql;

      LegacyKVWriteHandler(String insertSql, String updateSql, String deleteSql) {
         this.insertSql = insertSql;
         this.updateSql = updateSql;
         this.deleteSql = deleteSql;
      }

      @Override
      public void onEvent(E event, long sequence, boolean endOfBatch) throws BackendException {
         KeyValueEvent<?, ?> kve = (KeyValueEvent<?, ?>)event;
         if (kve.scope != null && kve.scopeKey != null && kve.key != null) {
            try {
               try (Connection c = SqlKeyValueScopedAdapter.this.ds.getConnection()) {
                  if (kve.remove) {
                     try (PreparedStatement ps = c.prepareStatement(this.deleteSql)) {
                        ps.setString(1, String.valueOf(kve.scope));
                        ps.setString(2, String.valueOf(kve.scopeKey));
                        ps.setString(3, kve.key);
                        ps.executeUpdate();
                        return;
                     }
                  }

                  if (kve.value == null) {
                     throw new BackendException("KV PutOp value must be non-null; use remove flag for deletion");
                  }

                  boolean priorAutoCommit = c.getAutoCommit();
                  c.setAutoCommit(false);

                  try {
                     SqlKeyValueScopedAdapter.this.legacyKvUpsert(
                        c,
                        this.insertSql,
                        this.updateSql,
                        String.valueOf(kve.scope),
                        String.valueOf(kve.scopeKey),
                        kve.key,
                        SqlKeyValueScopedAdapter.valueAsBytes(kve.value),
                        kve.updatedEpochMs
                     );
                     c.commit();
                  } catch (Exception var22) {
                     try {
                        c.rollback();
                     } catch (SQLException var20) {
                     }

                     throw var22;
                  } finally {
                     c.setAutoCommit(priorAutoCommit);
                  }
               }
            } catch (SQLException var26) {
               throw new BackendException("kv write failed", var26);
            }
         }
      }
   }
}
