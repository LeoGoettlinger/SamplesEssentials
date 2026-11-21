package ac.grim.grimac.internal.storage.backend.sql.v2;

import ac.grim.grimac.api.storage.backend.BackendException;
import ac.grim.grimac.api.storage.backend.KindAdapter;
import ac.grim.grimac.api.storage.backend.StorageEventHandler;
import ac.grim.grimac.api.storage.category.Capability;
import ac.grim.grimac.api.storage.category.Category;
import ac.grim.grimac.api.storage.kind.Counter;
import ac.grim.grimac.api.storage.kind.CounterEvent;
import ac.grim.grimac.api.storage.kind.Operation;
import ac.grim.grimac.api.storage.kind.ops.CounterOps;
import ac.grim.grimac.api.storage.registry.Migration;
import ac.grim.grimac.api.storage.registry.StoreId;
import ac.grim.grimac.internal.storage.backend.sql.v2.dialect.SqlDialect;
import ac.grim.grimac.shaded.jetbrains.annotations.ApiStatus;
import ac.grim.grimac.shaded.jetbrains.annotations.NotNull;
import ac.grim.grimac.shaded.jetbrains.annotations.Nullable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;
import javax.sql.DataSource;

@ApiStatus.Internal
public final class SqlCounterAdapter implements KindAdapter<Counter<?>> {
   @NotNull
   private final DataSource ds;
   @NotNull
   private final SqlDialect dialect;
   @NotNull
   private final Logger logger;
   @NotNull
   private final SqlCounterAdapter.CounterStrategy counterStrategy;

   public SqlCounterAdapter(@NotNull DataSource ds, @NotNull SqlDialect dialect, @NotNull Logger logger) {
      this.ds = ds;
      this.dialect = dialect;
      this.logger = logger;
      this.counterStrategy = SqlCounterAdapter.CounterStrategy.of(dialect, SqlCounterAdapter.CounterSql.of(dialect));
   }

   @NotNull
   @Override
   public Class<Counter<?>> kindType() {
      return Counter.class;
   }

   @NotNull
   @Override
   public EnumSet<Capability> subcapabilities() {
      return EnumSet.of(Capability.KIND_COUNTER, Capability.ATOMIC_UPSERT);
   }

   public void ensureStore(@NotNull StoreId id, @NotNull Counter<?> kind) throws BackendException {
      String table = this.dialect.quoteIdentifier(id.name());
      String keyCol = this.keyColumnType(kind.keyType());
      String ddl = "CREATE TABLE IF NOT EXISTS " + table + " (id " + keyCol + " PRIMARY KEY, value BIGINT NOT NULL DEFAULT 0)";

      try {
         try (
            Connection c = this.ds.getConnection();
            Statement s = c.createStatement();
         ) {
            s.executeUpdate(ddl);
         }
      } catch (SQLException var14) {
         throw new BackendException("failed to ensure counter table " + id, var14);
      }
   }

   public void dropStore(@NotNull StoreId id, @NotNull Counter<?> kind) throws BackendException {
      try {
         try (
            Connection c = this.ds.getConnection();
            Statement s = c.createStatement();
         ) {
            s.executeUpdate("DROP TABLE IF EXISTS " + this.dialect.quoteIdentifier(id.name()));
         }
      } catch (SQLException var11) {
         throw new BackendException("failed to drop counter table " + id, var11);
      }
   }

   @NotNull
   public <E> StorageEventHandler<E> writeHandler(@NotNull StoreId id, @NotNull Counter<?> kind, @NotNull Category<E> category) {
      String table = this.dialect.quoteIdentifier(id.name());
      return this.counterStrategy.writeHandler(this, table);
   }

   public <R> R execute(@NotNull StoreId id, @NotNull Counter<?> kind, @NotNull Operation<R> op) throws BackendException {
      try {
         if (op instanceof CounterOps.GetOp g) {
            return (R)this.get(id, g);
         } else if (op instanceof CounterOps.GetManyOp g) {
            return (R)this.getMany(id, g);
         } else if (op instanceof CounterOps.IncrementByOp i) {
            return (R)this.incrementBy(id, i);
         } else if (op instanceof CounterOps.SetIfHigherOp s) {
            return (R)this.setIfHigher(id, s);
         } else {
            throw new UnsupportedOperationException("SqlCounterAdapter does not handle " + op.getClass().getName());
         }
      } catch (SQLException var5) {
         throw new BackendException("sql counter execute failed for " + op.getClass().getSimpleName(), var5);
      }
   }

   @NotNull
   public List<Migration<Counter<?>>> migrations(@NotNull Counter<?> kind) {
      return List.of();
   }

   private long get(@NotNull StoreId id, @NotNull CounterOps.GetOp<?> op) throws SQLException {
      String table = this.dialect.quoteIdentifier(id.name());

      long var7;
      try (
         Connection c = this.ds.getConnection();
         PreparedStatement ps = c.prepareStatement("SELECT value FROM " + table + " WHERE id = ?");
      ) {
         bindKey(ps, 1, op.key());

         try (ResultSet rs = ps.executeQuery()) {
            var7 = rs.next() ? rs.getLong("value") : 0L;
         }
      }

      return var7;
   }

   private <K> Map<K, Long> getMany(@NotNull StoreId id, @NotNull CounterOps.GetManyOp<K> op) throws SQLException {
      Collection<K> keys = op.keys();
      if (keys.isEmpty()) {
         return Map.of();
      } else {
         Map<K, Long> out = new LinkedHashMap<>(keys.size() * 4 / 3 + 1);

         for (K k : keys) {
            out.put(k, 0L);
         }

         String table = this.dialect.quoteIdentifier(id.name());
         String placeholders = String.join(", ", keys.stream().map(k -> "?").toList());
         String sql = "SELECT id, value FROM " + table + " WHERE id IN (" + placeholders + ")";

         try (
            Connection c = this.ds.getConnection();
            PreparedStatement ps = c.prepareStatement(sql);
         ) {
            int idx = 1;

            for (K k : keys) {
               bindKey(ps, idx++, k);
            }

            try (ResultSet rs = ps.executeQuery()) {
               while (rs.next()) {
                  K key = (K)extractKey(rs, "id", keys.iterator().next().getClass());
                  if (key != null) {
                     out.put(key, rs.getLong("value"));
                  }
               }
            }
         }

         return out;
      }
   }

   private long incrementBy(@NotNull StoreId id, @NotNull CounterOps.IncrementByOp<?> op) throws SQLException {
      String table = this.dialect.quoteIdentifier(id.name());
      return this.counterStrategy.incrementBy(this, table, op.key(), op.delta());
   }

   private long setIfHigher(@NotNull StoreId id, @NotNull CounterOps.SetIfHigherOp<?> op) throws SQLException {
      String table = this.dialect.quoteIdentifier(id.name());
      return this.counterStrategy.setIfHigher(this, table, op.key(), op.value());
   }

   private long incrementReturning(@NotNull String upsert, @NotNull Object key, long delta) throws SQLException {
      String sql = upsert + " RETURNING value";

      long var9;
      try (
         Connection c = this.ds.getConnection();
         PreparedStatement ps = c.prepareStatement(sql);
      ) {
         bindKey(ps, 1, key);
         ps.setLong(2, delta);

         try (ResultSet rs = ps.executeQuery()) {
            var9 = rs.next() ? rs.getLong("value") : delta;
         }
      }

      return var9;
   }

   private long incrementSelect(@NotNull String table, @NotNull String upsert, @NotNull Object key, long delta) throws SQLException {
      return this.upsertThenSelect(table, upsert, key, delta);
   }

   private long setIfHigherReturning(@NotNull String upsert, @NotNull Object key, long value) throws SQLException {
      String sql = upsert + " RETURNING value";

      long var9;
      try (
         Connection c = this.ds.getConnection();
         PreparedStatement ps = c.prepareStatement(sql);
      ) {
         bindKey(ps, 1, key);
         ps.setLong(2, value);

         try (ResultSet rs = ps.executeQuery()) {
            var9 = rs.next() ? rs.getLong("value") : value;
         }
      }

      return var9;
   }

   private long setIfHigherSelect(@NotNull String table, @NotNull String upsert, @NotNull Object key, long value) throws SQLException {
      return this.upsertThenSelect(table, upsert, key, value);
   }

   private long upsertThenSelect(@NotNull String table, @NotNull String upsert, @NotNull Object key, long fallback) throws SQLException {
      long var10;
      try (Connection c = this.ds.getConnection()) {
         boolean prior = c.getAutoCommit();
         c.setAutoCommit(false);

         try {
            try (PreparedStatement ps = c.prepareStatement(upsert)) {
               bindKey(ps, 1, key);
               ps.setLong(2, fallback);
               ps.executeUpdate();
            }

            try (PreparedStatement sel = c.prepareStatement("SELECT value FROM " + table + " WHERE id = ?")) {
               bindKey(sel, 1, key);

               try (ResultSet rs = sel.executeQuery()) {
                  c.commit();
                  var10 = rs.next() ? rs.getLong("value") : fallback;
               }
            }
         } catch (Exception var32) {
            try {
               c.rollback();
            } catch (SQLException var25) {
            }

            throw var32;
         } finally {
            c.setAutoCommit(prior);
         }
      }

      return var10;
   }

   private long legacyIncrementBy(@NotNull String table, @NotNull Object key, long delta) throws SQLException {
      long var9;
      try (Connection c = this.ds.getConnection()) {
         boolean prior = c.getAutoCommit();
         c.setAutoCommit(false);

         try {
            this.legacyInsertCounter(c, table, key, 0L);
            this.legacyAddCounter(c, table, key, delta);
            long value = this.selectCounterValue(c, table, key, delta);
            c.commit();
            var9 = value;
         } catch (Exception var19) {
            try {
               c.rollback();
            } catch (SQLException var18) {
            }

            throw var19;
         } finally {
            c.setAutoCommit(prior);
         }
      }

      return var9;
   }

   private long legacySetIfHigher(@NotNull String table, @NotNull Object key, long value) throws SQLException {
      long var9;
      try (Connection c = this.ds.getConnection()) {
         boolean prior = c.getAutoCommit();
         c.setAutoCommit(false);

         try {
            this.legacyInsertCounter(c, table, key, value);
            this.legacyMaxCounter(c, table, key, value);
            long current = this.selectCounterValue(c, table, key, value);
            c.commit();
            var9 = current;
         } catch (Exception var19) {
            try {
               c.rollback();
            } catch (SQLException var18) {
            }

            throw var19;
         } finally {
            c.setAutoCommit(prior);
         }
      }

      return var9;
   }

   private void legacyInsertCounter(@NotNull Connection c, @NotNull String table, @NotNull Object key, long value) throws SQLException {
      try (PreparedStatement ps = c.prepareStatement(
            "INSERT OR IGNORE INTO " + table + " (" + this.dialect.quoteIdentifier("id") + ", " + this.dialect.quoteIdentifier("value") + ") VALUES (?, ?)"
         )) {
         bindKey(ps, 1, key);
         ps.setLong(2, value);
         ps.executeUpdate();
      }
   }

   private void legacyAddCounter(@NotNull Connection c, @NotNull String table, @NotNull Object key, long delta) throws SQLException {
      String value = this.dialect.quoteIdentifier("value");

      try (PreparedStatement ps = c.prepareStatement(
            "UPDATE " + table + " SET " + value + " = " + value + " + ? WHERE " + this.dialect.quoteIdentifier("id") + " = ?"
         )) {
         ps.setLong(1, delta);
         bindKey(ps, 2, key);
         ps.executeUpdate();
      }
   }

   private void legacyMaxCounter(@NotNull Connection c, @NotNull String table, @NotNull Object key, long incoming) throws SQLException {
      String value = this.dialect.quoteIdentifier("value");

      try (PreparedStatement ps = c.prepareStatement(
            "UPDATE " + table + " SET " + value + " = MAX(" + value + ", ?) WHERE " + this.dialect.quoteIdentifier("id") + " = ?"
         )) {
         ps.setLong(1, incoming);
         bindKey(ps, 2, key);
         ps.executeUpdate();
      }
   }

   private long selectCounterValue(@NotNull Connection c, @NotNull String table, @NotNull Object key, long fallback) throws SQLException {
      long var8;
      try (PreparedStatement ps = c.prepareStatement("SELECT value FROM " + table + " WHERE " + this.dialect.quoteIdentifier("id") + " = ?")) {
         bindKey(ps, 1, key);

         try (ResultSet rs = ps.executeQuery()) {
            var8 = rs.next() ? rs.getLong("value") : fallback;
         }
      }

      return var8;
   }

   private static void bindKey(@NotNull PreparedStatement ps, int idx, @NotNull Object key) throws SQLException {
      if (key instanceof UUID u) {
         ps.setObject(idx, u);
      } else if (key instanceof String s) {
         ps.setString(idx, s);
      } else if (key instanceof Long l) {
         ps.setLong(idx, l);
      } else if (key instanceof Integer i) {
         ps.setInt(idx, i);
      } else if (key instanceof byte[] b) {
         ps.setBytes(idx, b);
      } else if (key instanceof Enum<?> e) {
         ps.setInt(idx, e.ordinal());
      } else {
         throw new SQLException("unsupported counter key type: " + key.getClass().getName());
      }
   }

   @Nullable
   private static Object extractKey(@NotNull ResultSet rs, @NotNull String col, @NotNull Class<?> keyType) throws SQLException {
      if (keyType == UUID.class) {
         return rs.getObject(col, UUID.class);
      } else if (keyType == String.class) {
         return rs.getString(col);
      } else if (keyType == Long.class || keyType == long.class) {
         long v = rs.getLong(col);
         return rs.wasNull() ? null : v;
      } else if (keyType == Integer.class || keyType == int.class) {
         int v = rs.getInt(col);
         return rs.wasNull() ? null : v;
      } else {
         return keyType == byte[].class ? rs.getBytes(col) : rs.getObject(col);
      }
   }

   @NotNull
   private String keyColumnType(@NotNull Class<?> keyType) {
      if (keyType == UUID.class) {
         return this.dialect.name().equals("postgres") ? "UUID" : "BINARY(16)";
      } else if (keyType == String.class) {
         return "VARCHAR(255)";
      } else if (keyType == Long.class || keyType == long.class) {
         return "BIGINT";
      } else if (keyType == Integer.class || keyType == int.class) {
         return "INTEGER";
      } else if (keyType == byte[].class) {
         return this.dialect.name().equals("postgres") ? "BYTEA" : "BLOB";
      } else if (keyType.isEnum()) {
         return "INTEGER";
      } else {
         throw new IllegalArgumentException("unsupported counter key type: " + keyType.getName());
      }
   }

   private record ConflictCounterSql(@NotNull SqlDialect dialect) implements SqlCounterAdapter.CounterSql {
      @NotNull
      @Override
      public String increment(@NotNull String table) {
         String id = this.dialect.quoteIdentifier("id");
         String value = this.dialect.quoteIdentifier("value");
         return "INSERT INTO "
            + table
            + " ("
            + id
            + ", "
            + value
            + ") VALUES (?, ?) ON CONFLICT ("
            + id
            + ") DO UPDATE SET "
            + value
            + " = "
            + table
            + "."
            + value
            + " + "
            + this.dialect.excludedRef(value);
      }

      @NotNull
      @Override
      public String setIfHigher(@NotNull String table) {
         String id = this.dialect.quoteIdentifier("id");
         String value = this.dialect.quoteIdentifier("value");
         return "INSERT INTO "
            + table
            + " ("
            + id
            + ", "
            + value
            + ") VALUES (?, ?) ON CONFLICT ("
            + id
            + ") DO UPDATE SET "
            + value
            + " = "
            + this.dialect.greatestFn(table + "." + value, this.dialect.excludedRef(value));
      }
   }

   private interface CounterSql {
      @NotNull
      static SqlCounterAdapter.CounterSql of(@NotNull SqlDialect dialect) {
         return (SqlCounterAdapter.CounterSql)("mysql".equals(dialect.name())
            ? new SqlCounterAdapter.MysqlCounterSql(dialect)
            : new SqlCounterAdapter.ConflictCounterSql(dialect));
      }

      @NotNull
      String increment(@NotNull String var1);

      @NotNull
      String setIfHigher(@NotNull String var1);
   }

   private interface CounterStrategy {
      @NotNull
      static SqlCounterAdapter.CounterStrategy of(@NotNull SqlDialect dialect, @NotNull SqlCounterAdapter.CounterSql sql) {
         if (dialect.usesLegacySqliteUpsert()) {
            return SqlCounterAdapter.LegacyCounterStrategy.INSTANCE;
         } else {
            return (SqlCounterAdapter.CounterStrategy)(dialect.supportsReturning()
               ? new SqlCounterAdapter.ReturningCounterStrategy(sql)
               : new SqlCounterAdapter.SelectCounterStrategy(sql));
         }
      }

      @NotNull
      <E> StorageEventHandler<E> writeHandler(@NotNull SqlCounterAdapter var1, @NotNull String var2);

      long incrementBy(@NotNull SqlCounterAdapter var1, @NotNull String var2, @NotNull Object var3, long var4) throws SQLException;

      long setIfHigher(@NotNull SqlCounterAdapter var1, @NotNull String var2, @NotNull Object var3, long var4) throws SQLException;
   }

   private final class CounterWriteHandler<E> implements StorageEventHandler<E> {
      private final String upsertSql;

      CounterWriteHandler(String upsertSql) {
         this.upsertSql = upsertSql;
      }

      @Override
      public void onEvent(E event, long sequence, boolean endOfBatch) throws BackendException {
         CounterEvent<?> ce = (CounterEvent<?>)event;
         if (ce.key != null) {
            if (ce.delta != 0L) {
               try {
                  try (
                     Connection c = SqlCounterAdapter.this.ds.getConnection();
                     PreparedStatement ps = c.prepareStatement(this.upsertSql);
                  ) {
                     SqlCounterAdapter.bindKey(ps, 1, ce.key);
                     ps.setLong(2, ce.delta);
                     ps.executeUpdate();
                  }
               } catch (SQLException var14) {
                  throw new BackendException("counter write failed", var14);
               }
            }
         }
      }
   }

   private record LegacyCounterStrategy() implements SqlCounterAdapter.CounterStrategy {
      @NotNull
      private static final SqlCounterAdapter.CounterStrategy INSTANCE = new SqlCounterAdapter.LegacyCounterStrategy();

      @NotNull
      @Override
      public <E> StorageEventHandler<E> writeHandler(@NotNull SqlCounterAdapter adapter, @NotNull String table) {
         return adapter.new LegacyCounterWriteHandler<>(table);
      }

      @Override
      public long incrementBy(@NotNull SqlCounterAdapter adapter, @NotNull String table, @NotNull Object key, long delta) throws SQLException {
         return adapter.legacyIncrementBy(table, key, delta);
      }

      @Override
      public long setIfHigher(@NotNull SqlCounterAdapter adapter, @NotNull String table, @NotNull Object key, long value) throws SQLException {
         return adapter.legacySetIfHigher(table, key, value);
      }
   }

   private final class LegacyCounterWriteHandler<E> implements StorageEventHandler<E> {
      private final String table;

      LegacyCounterWriteHandler(String table) {
         this.table = table;
      }

      @Override
      public void onEvent(E event, long sequence, boolean endOfBatch) throws BackendException {
         CounterEvent<?> ce = (CounterEvent<?>)event;
         if (ce.key != null) {
            if (ce.delta != 0L) {
               try {
                  try (Connection c = SqlCounterAdapter.this.ds.getConnection()) {
                     boolean prior = c.getAutoCommit();
                     c.setAutoCommit(false);

                     try {
                        SqlCounterAdapter.this.legacyInsertCounter(c, this.table, ce.key, 0L);
                        SqlCounterAdapter.this.legacyAddCounter(c, this.table, ce.key, ce.delta);
                        c.commit();
                     } catch (Exception var19) {
                        try {
                           c.rollback();
                        } catch (SQLException var18) {
                        }

                        throw var19;
                     } finally {
                        c.setAutoCommit(prior);
                     }
                  }
               } catch (SQLException var22) {
                  throw new BackendException("counter write failed", var22);
               }
            }
         }
      }
   }

   private record MysqlCounterSql(@NotNull SqlDialect dialect) implements SqlCounterAdapter.CounterSql {
      @NotNull
      @Override
      public String increment(@NotNull String table) {
         String id = this.dialect.quoteIdentifier("id");
         String value = this.dialect.quoteIdentifier("value");
         return "INSERT INTO "
            + table
            + " ("
            + id
            + ", "
            + value
            + ") VALUES (?, ?) ON DUPLICATE KEY UPDATE "
            + value
            + " = "
            + value
            + " + "
            + this.dialect.excludedRef(value);
      }

      @NotNull
      @Override
      public String setIfHigher(@NotNull String table) {
         String id = this.dialect.quoteIdentifier("id");
         String value = this.dialect.quoteIdentifier("value");
         return "INSERT INTO "
            + table
            + " ("
            + id
            + ", "
            + value
            + ") VALUES (?, ?) ON DUPLICATE KEY UPDATE "
            + value
            + " = "
            + this.dialect.greatestFn(value, this.dialect.excludedRef(value));
      }
   }

   private record ReturningCounterStrategy(@NotNull SqlCounterAdapter.CounterSql sql) implements SqlCounterAdapter.CounterStrategy {
      @NotNull
      @Override
      public <E> StorageEventHandler<E> writeHandler(@NotNull SqlCounterAdapter adapter, @NotNull String table) {
         return adapter.new CounterWriteHandler<>(this.sql.increment(table));
      }

      @Override
      public long incrementBy(@NotNull SqlCounterAdapter adapter, @NotNull String table, @NotNull Object key, long delta) throws SQLException {
         return adapter.incrementReturning(this.sql.increment(table), key, delta);
      }

      @Override
      public long setIfHigher(@NotNull SqlCounterAdapter adapter, @NotNull String table, @NotNull Object key, long value) throws SQLException {
         return adapter.setIfHigherReturning(this.sql.setIfHigher(table), key, value);
      }
   }

   private record SelectCounterStrategy(@NotNull SqlCounterAdapter.CounterSql sql) implements SqlCounterAdapter.CounterStrategy {
      @NotNull
      @Override
      public <E> StorageEventHandler<E> writeHandler(@NotNull SqlCounterAdapter adapter, @NotNull String table) {
         return adapter.new CounterWriteHandler<>(this.sql.increment(table));
      }

      @Override
      public long incrementBy(@NotNull SqlCounterAdapter adapter, @NotNull String table, @NotNull Object key, long delta) throws SQLException {
         return adapter.incrementSelect(table, this.sql.increment(table), key, delta);
      }

      @Override
      public long setIfHigher(@NotNull SqlCounterAdapter adapter, @NotNull String table, @NotNull Object key, long value) throws SQLException {
         return adapter.setIfHigherSelect(table, this.sql.setIfHigher(table), key, value);
      }
   }
}
