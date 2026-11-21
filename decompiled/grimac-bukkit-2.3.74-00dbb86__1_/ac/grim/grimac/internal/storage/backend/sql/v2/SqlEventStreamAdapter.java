package ac.grim.grimac.internal.storage.backend.sql.v2;

import ac.grim.grimac.api.storage.backend.BackendException;
import ac.grim.grimac.api.storage.backend.KindAdapter;
import ac.grim.grimac.api.storage.backend.StorageEventHandler;
import ac.grim.grimac.api.storage.category.Capability;
import ac.grim.grimac.api.storage.category.Category;
import ac.grim.grimac.api.storage.codec.EncodeShape;
import ac.grim.grimac.api.storage.kind.EventStream;
import ac.grim.grimac.api.storage.kind.Operation;
import ac.grim.grimac.api.storage.kind.ops.EventStreamOps;
import ac.grim.grimac.api.storage.query.Cursor;
import ac.grim.grimac.api.storage.query.Cursors;
import ac.grim.grimac.api.storage.query.Page;
import ac.grim.grimac.api.storage.registry.Migration;
import ac.grim.grimac.api.storage.registry.StoreId;
import ac.grim.grimac.internal.storage.backend.sql.v2.dialect.SqlDialect;
import ac.grim.grimac.internal.storage.codec.bson.BsonCodec;
import ac.grim.grimac.internal.storage.codec.bson.BsonCodecs;
import ac.grim.grimac.shaded.jetbrains.annotations.ApiStatus;
import ac.grim.grimac.shaded.jetbrains.annotations.NotNull;
import ac.grim.grimac.shaded.jetbrains.annotations.Nullable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Logger;
import javax.sql.DataSource;

@ApiStatus.Internal
public final class SqlEventStreamAdapter implements KindAdapter<EventStream<?, ?>> {
   private static final int SQL_BATCH_SIZE = 256;
   @NotNull
   private final DataSource ds;
   @NotNull
   private final SqlDialect dialect;
   @NotNull
   private final Logger logger;

   public SqlEventStreamAdapter(@NotNull DataSource ds, @NotNull SqlDialect dialect, @NotNull Logger logger) {
      this.ds = ds;
      this.dialect = dialect;
      this.logger = logger;
   }

   @NotNull
   @Override
   public Class<EventStream<?, ?>> kindType() {
      return EventStream.class;
   }

   @NotNull
   @Override
   public EnumSet<Capability> subcapabilities() {
      return EnumSet.of(Capability.KIND_EVENT_STREAM);
   }

   public void ensureStore(@NotNull StoreId id, @NotNull EventStream<?, ?> kind) throws BackendException {
      EncodeShape shape = kind.codec().shape();
      String table = this.dialect.quoteIdentifier(id.name());
      StringBuilder ddl = new StringBuilder("CREATE TABLE IF NOT EXISTS ").append(table).append(" (");
      boolean first = true;

      for (EncodeShape.FieldDef f : shape.fields()) {
         if (!first) {
            ddl.append(", ");
         }

         first = false;
         ddl.append(this.dialect.quoteIdentifier(f.name())).append(' ').append(this.dialect.columnTypeSql(f));
         if (f.name().equals(shape.idField())) {
            ddl.append(" PRIMARY KEY");
         }
      }

      ddl.append(')');

      try {
         try (
            Connection c = this.ds.getConnection();
            Statement s = c.createStatement();
         ) {
            s.executeUpdate(ddl.toString());
            this.addMissingColumns(c, s, id.name(), shape);

            for (String partition : kind.partitionFields()) {
               String idxName = "idx_" + id.name() + "_" + partition;
               String createIdx = "CREATE INDEX IF NOT EXISTS "
                  + this.dialect.quoteIdentifier(idxName)
                  + " ON "
                  + table
                  + " ("
                  + this.dialect.quoteIdentifier(partition)
                  + ")";

               try {
                  s.executeUpdate(createIdx);
               } catch (SQLException var17) {
               }
            }

            String tsIdx = "idx_" + id.name() + "_ts";
            String createTsIdx = "CREATE INDEX IF NOT EXISTS "
               + this.dialect.quoteIdentifier(tsIdx)
               + " ON "
               + table
               + " ("
               + this.dialect.quoteIdentifier(kind.timestampField())
               + ")";

            try {
               s.executeUpdate(createTsIdx);
            } catch (SQLException var16) {
            }
         }
      } catch (SQLException var20) {
         throw new BackendException("failed to ensure event stream table " + id, var20);
      }
   }

   public void dropStore(@NotNull StoreId id, @NotNull EventStream<?, ?> kind) throws BackendException {
      try {
         try (
            Connection c = this.ds.getConnection();
            Statement s = c.createStatement();
         ) {
            s.executeUpdate("DROP TABLE IF EXISTS " + this.dialect.quoteIdentifier(id.name()));
         }
      } catch (SQLException var11) {
         throw new BackendException("failed to drop event stream table " + id, var11);
      }
   }

   @NotNull
   public <E> StorageEventHandler<E> writeHandler(@NotNull StoreId id, @NotNull EventStream<?, ?> kind, @NotNull Category<E> category) {
      EncodeShape shape = kind.codec().shape();
      String table = this.dialect.quoteIdentifier(id.name());
      StringBuilder cols = new StringBuilder();
      StringBuilder placeholders = new StringBuilder();

      for (int i = 0; i < shape.fields().size(); i++) {
         if (i > 0) {
            cols.append(", ");
            placeholders.append(", ");
         }

         cols.append(this.dialect.quoteIdentifier(shape.fields().get(i).name()));
         placeholders.append('?');
      }

      String insertSql = "INSERT INTO " + table + " (" + cols + ") VALUES (" + placeholders + ")";
      return new SqlEventStreamAdapter.EventStreamWriteHandler<>(insertSql, kind, shape);
   }

   public <R> R execute(@NotNull StoreId id, @NotNull EventStream<?, ?> kind, @NotNull Operation<R> op) throws BackendException {
      try {
         if (op instanceof EventStreamOps.PageOp<?> p) {
            return (R)this.page(id, kind, p);
         } else if (op instanceof EventStreamOps.RangeByTimeOp<?> r) {
            return (R)this.rangeByTime(id, kind, r);
         } else if (op instanceof EventStreamOps.CountOp c) {
            return (R)this.count(id, kind, c);
         } else if (op instanceof EventStreamOps.CountManyOp<?> c) {
            return (R)this.countMany(id, kind, c);
         } else if (op instanceof EventStreamOps.CountDistinctOp c) {
            return (R)this.countDistinct(id, kind, c);
         } else if (op instanceof EventStreamOps.DeleteByPartitionOp d) {
            this.deleteByPartition(id, kind, d);
            return null;
         } else if (op instanceof EventStreamOps.DeleteOlderThanOp d) {
            this.deleteOlderThan(id, kind, d);
            return null;
         } else {
            throw new UnsupportedOperationException("SqlEventStreamAdapter does not handle " + op.getClass().getName());
         }
      } catch (SQLException var5) {
         throw new BackendException("sql event stream execute failed for " + op.getClass().getSimpleName(), var5);
      }
   }

   @NotNull
   public List<Migration<EventStream<?, ?>>> migrations(@NotNull EventStream<?, ?> kind) {
      return List.of();
   }

   private <R> Page<R> page(@NotNull StoreId id, @NotNull EventStream<?, ?> kind, @NotNull EventStreamOps.PageOp<R> op) throws SQLException {
      EncodeShape shape = kind.codec().shape();
      String table = this.dialect.quoteIdentifier(id.name());
      String tsCol = this.dialect.quoteIdentifier(kind.timestampField());
      String idCol = this.dialect.quoteIdentifier(shape.idField());
      String partCol = this.dialect.quoteIdentifier(op.partition());
      EncodeShape.FieldDef partField = fieldDef(shape, op.partition());
      EncodeShape.FieldDef idField = fieldDef(shape, shape.idField());
      boolean hasCursor = op.cursor() != null;
      String sql;
      if (hasCursor) {
         sql = "SELECT * FROM "
            + table
            + " WHERE "
            + partCol
            + " = ? AND ("
            + tsCol
            + " > ? OR ("
            + tsCol
            + " = ? AND "
            + idCol
            + " > ?)) ORDER BY "
            + tsCol
            + " ASC, "
            + idCol
            + " ASC LIMIT ?";
      } else {
         sql = "SELECT * FROM " + table + " WHERE " + partCol + " = ? ORDER BY " + tsCol + " ASC, " + idCol + " ASC LIMIT ?";
      }

      Page var25;
      try (
         Connection c = this.ds.getConnection();
         PreparedStatement ps = c.prepareStatement(sql);
      ) {
         int idx = 1;
         SqlBindings.bind(ps, idx++, partField, op.key());
         if (hasCursor) {
            Cursors.Decoded d = Cursors.decode(op.cursor());
            Object idValue = SqlBindings.bytesToId(idField, d.idBytes());
            ps.setLong(idx++, d.orderedKey());
            ps.setLong(idx++, d.orderedKey());
            SqlBindings.bind(ps, idx++, idField, idValue);
         }

         ps.setInt(idx, op.pageSize() + 1);
         var25 = this.readPageWithCursor(ps, kind, op.pageSize());
      }

      return var25;
   }

   private <R> Page<R> rangeByTime(@NotNull StoreId id, @NotNull EventStream<?, ?> kind, @NotNull EventStreamOps.RangeByTimeOp<R> op) throws SQLException {
      EncodeShape shape = kind.codec().shape();
      String table = this.dialect.quoteIdentifier(id.name());
      String tsCol = this.dialect.quoteIdentifier(kind.timestampField());
      String idCol = this.dialect.quoteIdentifier(shape.idField());
      EncodeShape.FieldDef idField = fieldDef(shape, shape.idField());
      boolean hasCursor = op.cursor() != null;
      String sql = hasCursor
         ? "SELECT * FROM "
            + table
            + " WHERE "
            + tsCol
            + " >= ? AND "
            + tsCol
            + " < ? AND ("
            + tsCol
            + " > ? OR ("
            + tsCol
            + " = ? AND "
            + idCol
            + " > ?)) ORDER BY "
            + tsCol
            + " ASC, "
            + idCol
            + " ASC LIMIT ?"
         : "SELECT * FROM " + table + " WHERE " + tsCol + " >= ? AND " + tsCol + " < ? ORDER BY " + tsCol + " ASC, " + idCol + " ASC LIMIT ?";

      Page var24;
      try (
         Connection c = this.ds.getConnection();
         PreparedStatement ps = c.prepareStatement(sql);
      ) {
         int idx = 1;
         ps.setLong(idx++, op.fromEpochMs());
         ps.setLong(idx++, op.toEpochMs());
         if (hasCursor) {
            Cursors.Decoded d = Cursors.decode(op.cursor());
            Object idValue = SqlBindings.bytesToId(idField, d.idBytes());
            ps.setLong(idx++, d.orderedKey());
            ps.setLong(idx++, d.orderedKey());
            SqlBindings.bind(ps, idx++, idField, idValue);
         }

         ps.setInt(idx, op.pageSize() + 1);
         var24 = this.readPageWithCursor(ps, kind, op.pageSize());
      }

      return var24;
   }

   private long count(@NotNull StoreId id, @NotNull EventStream<?, ?> kind, @NotNull EventStreamOps.CountOp op) throws SQLException {
      String table = this.dialect.quoteIdentifier(id.name());
      String sql = "SELECT COUNT(*) FROM " + table + " WHERE " + this.dialect.quoteIdentifier(op.partition()) + " = ?";
      EncodeShape shape = kind.codec().shape();

      long var10;
      try (
         Connection c = this.ds.getConnection();
         PreparedStatement ps = c.prepareStatement(sql);
      ) {
         SqlBindings.bind(ps, 1, fieldDef(shape, op.partition()), op.key());

         try (ResultSet rs = ps.executeQuery()) {
            var10 = rs.next() ? rs.getLong(1) : 0L;
         }
      }

      return var10;
   }

   private <K> Map<K, Long> countMany(@NotNull StoreId id, @NotNull EventStream<?, ?> kind, @NotNull EventStreamOps.CountManyOp<K> op) throws SQLException {
      Collection<K> keys = op.keys();
      if (keys.isEmpty()) {
         return Map.of();
      } else {
         EncodeShape shape = kind.codec().shape();
         String table = this.dialect.quoteIdentifier(id.name());
         String partCol = this.dialect.quoteIdentifier(op.partition());
         String placeholders = String.join(", ", keys.stream().map(k -> "?").toList());
         String sql = "SELECT " + partCol + ", COUNT(*) AS cnt FROM " + table + " WHERE " + partCol + " IN (" + placeholders + ") GROUP BY " + partCol;
         Map<K, Long> out = new LinkedHashMap<>(keys.size() * 4 / 3 + 1);

         for (K k : keys) {
            out.put(k, 0L);
         }

         EncodeShape.FieldDef partField = fieldDef(shape, op.partition());

         try (
            Connection c = this.ds.getConnection();
            PreparedStatement ps = c.prepareStatement(sql);
         ) {
            int idx = 1;

            for (K k : keys) {
               SqlBindings.bind(ps, idx++, partField, k);
            }

            try (ResultSet rs = ps.executeQuery()) {
               while (rs.next()) {
                  K key = (K)SqlBindings.extract(rs, partField);
                  if (key != null) {
                     out.put(key, rs.getLong("cnt"));
                  }
               }
            }
         }

         return out;
      }
   }

   private long countDistinct(@NotNull StoreId id, @NotNull EventStream<?, ?> kind, @NotNull EventStreamOps.CountDistinctOp op) throws SQLException {
      String table = this.dialect.quoteIdentifier(id.name());
      String sql = "SELECT COUNT(DISTINCT "
         + this.dialect.quoteIdentifier(op.field())
         + ") FROM "
         + table
         + " WHERE "
         + this.dialect.quoteIdentifier(op.partition())
         + " = ?";
      EncodeShape shape = kind.codec().shape();

      long var10;
      try (
         Connection c = this.ds.getConnection();
         PreparedStatement ps = c.prepareStatement(sql);
      ) {
         SqlBindings.bind(ps, 1, fieldDef(shape, op.partition()), op.key());

         try (ResultSet rs = ps.executeQuery()) {
            var10 = rs.next() ? rs.getLong(1) : 0L;
         }
      }

      return var10;
   }

   private void deleteByPartition(@NotNull StoreId id, @NotNull EventStream<?, ?> kind, @NotNull EventStreamOps.DeleteByPartitionOp op) throws SQLException {
      String table = this.dialect.quoteIdentifier(id.name());
      EncodeShape shape = kind.codec().shape();

      try (
         Connection c = this.ds.getConnection();
         PreparedStatement ps = c.prepareStatement("DELETE FROM " + table + " WHERE " + this.dialect.quoteIdentifier(op.partition()) + " = ?");
      ) {
         SqlBindings.bind(ps, 1, fieldDef(shape, op.partition()), op.key());
         ps.executeUpdate();
      }
   }

   private void deleteOlderThan(@NotNull StoreId id, @NotNull EventStream<?, ?> kind, @NotNull EventStreamOps.DeleteOlderThanOp op) throws SQLException {
      String table = this.dialect.quoteIdentifier(id.name());
      String tsField = this.dialect.quoteIdentifier(kind.timestampField());

      try (
         Connection c = this.ds.getConnection();
         PreparedStatement ps = c.prepareStatement("DELETE FROM " + table + " WHERE " + tsField + " < ?");
      ) {
         ps.setLong(1, op.cutoffEpochMs());
         ps.executeUpdate();
      }
   }

   private <R> Page<R> readPageWithCursor(@NotNull PreparedStatement ps, @NotNull EventStream<?, ?> kind, int pageSize) throws SQLException {
      BsonCodec codec = BsonCodecs.regular(kind.recordType());
      EncodeShape shape = kind.codec().shape();
      String tsFieldName = kind.timestampField();
      String idFieldName = shape.idField();
      EncodeShape.FieldDef idField = fieldDef(shape, idFieldName);
      List<R> items = new ArrayList<>(pageSize);
      Cursor nextCursor = null;
      long lastTs = 0L;
      Object lastIdValue = null;

      try (ResultSet rs = ps.executeQuery()) {
         while (rs.next()) {
            if (items.size() >= pageSize) {
               nextCursor = lastIdValue == null ? null : Cursors.encode(lastTs, SqlBindings.idToBytes(idField, lastIdValue));
               break;
            }

            Object[] args = new Object[shape.fields().size()];

            for (int i = 0; i < shape.fields().size(); i++) {
               EncodeShape.FieldDef f = shape.fields().get(i);
               args[i] = SqlBindings.extract(rs, f);
               if (f.name().equals(tsFieldName) && args[i] instanceof Number n) {
                  lastTs = n.longValue();
               }

               if (f.name().equals(idFieldName) && args[i] != null) {
                  lastIdValue = args[i];
               }
            }

            items.add((R)codec.decodeFromValues(args));
         }
      }

      return new Page<>(items, nextCursor);
   }

   private void addMissingColumns(@NotNull Connection c, @NotNull Statement s, @NotNull String table, @NotNull EncodeShape shape) throws SQLException {
      Map<String, String> existing = new HashMap<>();

      try (ResultSet cols = c.getMetaData().getColumns(null, null, table, null)) {
         while (cols.next()) {
            existing.put(cols.getString("COLUMN_NAME").toLowerCase(Locale.ROOT), cols.getString("TYPE_NAME"));
         }
      }

      if (!existing.isEmpty()) {
         for (EncodeShape.FieldDef f : shape.fields()) {
            if (!existing.containsKey(f.name().toLowerCase(Locale.ROOT))) {
               String alter = "ALTER TABLE "
                  + this.dialect.quoteIdentifier(table)
                  + " ADD COLUMN "
                  + this.dialect.quoteIdentifier(f.name())
                  + " "
                  + this.dialect.columnTypeSql(f).replace(" NOT NULL", "");
               this.logger.info(() -> "[v2-ensureStore] adding missing column: " + alter);

               try {
                  s.executeUpdate(alter);
               } catch (SQLException var14) {
               }
            }
         }

         for (EncodeShape.FieldDef fx : shape.fields()) {
            String actualTypeName = existing.get(fx.name().toLowerCase(Locale.ROOT));
            if (actualTypeName != null) {
               for (String alter : this.dialect.convertColumnToBinarySql(table, fx, actualTypeName)) {
                  this.logger.info(() -> "[v2-ensureStore] converting column to binary: " + alter);

                  try {
                     s.executeUpdate(alter);
                  } catch (SQLException var15) {
                     String refreshedType = this.columnType(c, table, fx.name());
                     if (refreshedType == null || !this.dialect.isBinaryColumnType(refreshedType)) {
                        throw var15;
                     }
                  }
               }
            }
         }
      }
   }

   @Nullable
   private String columnType(@NotNull Connection c, @NotNull String table, @NotNull String column) throws SQLException {
      try (ResultSet cols = c.getMetaData().getColumns(null, null, table, null)) {
         while (cols.next()) {
            if (column.equalsIgnoreCase(cols.getString("COLUMN_NAME"))) {
               return cols.getString("TYPE_NAME");
            }
         }
      }

      return null;
   }

   @NotNull
   private static EncodeShape.FieldDef fieldDef(@NotNull EncodeShape shape, @NotNull String fieldName) {
      for (EncodeShape.FieldDef f : shape.fields()) {
         if (f.name().equals(fieldName)) {
            return f;
         }
      }

      throw new IllegalArgumentException("no field '" + fieldName + "' in shape");
   }

   private final class EventStreamWriteHandler<E> implements StorageEventHandler<E> {
      private final String insertSql;
      private final EventStream kind;
      private final EncodeShape shape;
      private final BsonCodec codec;
      private final List<Object> pendingRecords = new ArrayList<>(256);

      EventStreamWriteHandler(String insertSql, EventStream<?, ?> kind, EncodeShape shape) {
         this.insertSql = insertSql;
         this.kind = kind;
         this.shape = shape;
         this.codec = BsonCodecs.regular(kind.recordType());
      }

      @Override
      public void onEvent(E event, long sequence, boolean endOfBatch) throws BackendException {
         Object record;
         try {
            record = this.kind.eventToRecord().apply(event);
         } catch (RuntimeException var7) {
            throw new BackendException("event stream write failed", var7);
         }

         try {
            this.pendingRecords.add(record);
            if (endOfBatch || this.pendingRecords.size() >= 256) {
               this.flushBatch();
            }
         } catch (Exception var8) {
            throw new BackendException("event stream write failed", var8);
         }
      }

      private void flushBatch() throws SQLException {
         if (!this.pendingRecords.isEmpty()) {
            try (Connection c = SqlEventStreamAdapter.this.ds.getConnection()) {
               boolean priorAutoCommit = c.getAutoCommit();
               c.setAutoCommit(false);

               try (PreparedStatement ps = c.prepareStatement(this.insertSql)) {
                  for (Object record : this.pendingRecords) {
                     this.bindRecord(ps, record);
                     ps.addBatch();
                  }

                  ps.executeBatch();
                  c.commit();
               } catch (RuntimeException | SQLException var28) {
                  try {
                     c.rollback();
                  } catch (SQLException var25) {
                  }

                  throw var28;
               } finally {
                  c.setAutoCommit(priorAutoCommit);
               }
            } finally {
               this.pendingRecords.clear();
            }
         }
      }

      private void bindRecord(@NotNull PreparedStatement ps, @NotNull Object record) throws SQLException {
         int idx = 1;

         for (int i = 0; i < this.shape.fields().size(); i++) {
            SqlBindings.bind(ps, idx++, this.shape.fields().get(i), this.codec.readField(record, i));
         }
      }
   }
}
