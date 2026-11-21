package ac.grim.grimac.internal.storage.backend.sql.v2;

import ac.grim.grimac.api.storage.backend.BackendException;
import ac.grim.grimac.api.storage.backend.KindAdapter;
import ac.grim.grimac.api.storage.backend.StorageEventHandler;
import ac.grim.grimac.api.storage.category.Capability;
import ac.grim.grimac.api.storage.category.Category;
import ac.grim.grimac.api.storage.codec.EncodeShape;
import ac.grim.grimac.api.storage.codec.FieldKind;
import ac.grim.grimac.api.storage.codec.MergeMode;
import ac.grim.grimac.api.storage.kind.Entity;
import ac.grim.grimac.api.storage.kind.IndexSpec;
import ac.grim.grimac.api.storage.kind.Operation;
import ac.grim.grimac.api.storage.kind.ops.EntityOps;
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
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sql.DataSource;

@ApiStatus.Internal
public final class SqlEntityAdapter implements KindAdapter<Entity<?, ?, ?>> {
   private static final int SQL_BATCH_SIZE = 256;
   @NotNull
   private final DataSource ds;
   @NotNull
   private final SqlDialect dialect;
   @NotNull
   private final Logger logger;

   public SqlEntityAdapter(@NotNull DataSource ds, @NotNull SqlDialect dialect, @NotNull Logger logger) {
      this.ds = ds;
      this.dialect = dialect;
      this.logger = logger;
   }

   @NotNull
   @Override
   public Class<Entity<?, ?, ?>> kindType() {
      return Entity.class;
   }

   @NotNull
   @Override
   public EnumSet<Capability> subcapabilities() {
      return EnumSet.of(Capability.KIND_ENTITY, Capability.ATOMIC_UPSERT);
   }

   public void ensureStore(@NotNull StoreId id, @NotNull Entity<?, ?, ?> kind) throws BackendException {
      String table = id.name();
      EncodeShape shape = kind.codec().shape();

      try {
         try (
            Connection c = this.ds.getConnection();
            Statement s = c.createStatement();
         ) {
            String createTable = this.dialect.createTableSql(table, shape);
            this.logger.log(Level.FINE, () -> "ensureStore DDL: " + createTable);
            s.executeUpdate(createTable);
            this.addMissingColumns(c, s, table, shape);

            for (IndexSpec spec : kind.secondaryIndexes()) {
               String createIdx = this.dialect.createIndexSql(table, spec);
               this.logger.log(Level.FINE, () -> "ensureStore DDL: " + createIdx);

               try {
                  s.executeUpdate(createIdx);
               } catch (SQLException var14) {
               }
            }
         }
      } catch (SQLException var17) {
         throw new BackendException("failed to ensure " + this.dialect.name() + " entity table " + table, var17);
      }
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
                  + this.dialect.columnTypeSql(f);
               alter = alter.replace(" NOT NULL", "");
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

   public void dropStore(@NotNull StoreId id, @NotNull Entity<?, ?, ?> kind) throws BackendException {
      String table = id.name();

      try {
         try (
            Connection c = this.ds.getConnection();
            Statement s = c.createStatement();
         ) {
            s.executeUpdate("DROP TABLE IF EXISTS " + this.dialect.quoteIdentifier(table));
         }
      } catch (SQLException var12) {
         throw new BackendException("failed to drop " + this.dialect.name() + " entity table " + table, var12);
      }
   }

   @NotNull
   public <E> StorageEventHandler<E> writeHandler(@NotNull StoreId id, @NotNull Entity<?, ?, ?> kind, @NotNull Category<E> category) {
      return (StorageEventHandler<E>)(this.dialect.usesLegacySqliteUpsert()
         ? new SqlEntityAdapter.LegacySqliteEntityHandler<>(id, (Entity<?, E, ?>)kind)
         : new SqlEntityAdapter.SqlEntityHandler<>(id, (Entity<?, E, ?>)kind));
   }

   @NotNull
   private String legacyInsertSql(@NotNull String tableName, @NotNull EncodeShape shape) {
      StringBuilder sb = new StringBuilder();
      sb.append("INSERT OR IGNORE INTO ").append(this.dialect.quoteIdentifier(tableName)).append(" (");
      boolean first = true;

      for (EncodeShape.FieldDef f : shape.fields()) {
         if (!first) {
            sb.append(", ");
         }

         sb.append(this.dialect.quoteIdentifier(f.name()));
         first = false;
      }

      sb.append(") VALUES (");

      for (int i = 0; i < shape.fields().size(); i++) {
         if (i > 0) {
            sb.append(", ");
         }

         sb.append('?');
      }

      sb.append(')');
      return sb.toString();
   }

   @NotNull
   private List<Integer> legacyUpdateIndexes(@NotNull EncodeShape shape) {
      List<Integer> indexes = new ArrayList<>();

      for (int i = 0; i < shape.fields().size(); i++) {
         EncodeShape.FieldDef f = shape.fields().get(i);
         if (!f.name().equals(shape.idField()) && f.mergeMode() != MergeMode.INSERT_ONLY) {
            indexes.add(i);
         }
      }

      return indexes;
   }

   @NotNull
   private String legacyUpdateSql(@NotNull String tableName, @NotNull EncodeShape shape, @NotNull List<Integer> updateIndexes) {
      StringBuilder sb = new StringBuilder();
      sb.append("UPDATE ").append(this.dialect.quoteIdentifier(tableName)).append(" SET ");
      boolean first = true;

      for (int index : updateIndexes) {
         if (!first) {
            sb.append(", ");
         }

         EncodeShape.FieldDef f = shape.fields().get(index);
         String col = this.dialect.quoteIdentifier(f.name());
         sb.append(col).append(" = ");
         switch (f.mergeMode()) {
            case OVERWRITE:
               sb.append('?');
               break;
            case PRESERVE_ON_NON_NULL:
               sb.append("COALESCE(").append(col).append(", ?)");
               break;
            case PRESERVE_ON_NON_SENTINEL:
               sb.append("CASE WHEN ")
                  .append(col)
                  .append(" IS NULL OR ")
                  .append(col)
                  .append(" = ")
                  .append(f.sentinelValue())
                  .append(" THEN ? ELSE ")
                  .append(col)
                  .append(" END");
               break;
            case MAX:
               sb.append("MAX(").append(col).append(", ?)");
               break;
            case MIN:
               sb.append("MIN(").append(col).append(", ?)");
            case INSERT_ONLY:
         }

         first = false;
      }

      sb.append(" WHERE ").append(this.dialect.quoteIdentifier(shape.idField())).append(" = ?");
      return sb.toString();
   }

   private static int idFieldIndex(@NotNull EncodeShape shape) {
      for (int i = 0; i < shape.fields().size(); i++) {
         if (shape.fields().get(i).name().equals(shape.idField())) {
            return i;
         }
      }

      throw new IllegalArgumentException("EncodeShape id field missing: " + shape.idField());
   }

   public <R> R execute(@NotNull StoreId id, @NotNull Entity<?, ?, ?> kind, @NotNull Operation<R> op) throws BackendException {
      try {
         if (op instanceof EntityOps.UpsertOp u) {
            this.upsertRecord(id, kind, u.record());
            return null;
         } else if (op instanceof EntityOps.GetByIdOp g) {
            return (R)this.getById(id, kind, g);
         } else if (op instanceof EntityOps.GetManyOp g) {
            return (R)this.getMany(id, kind, g);
         } else if (op instanceof EntityOps.DeleteByIdOp d) {
            this.deleteById(id, kind, d);
            return null;
         } else if (op instanceof EntityOps.DeleteByIndexOp d) {
            this.deleteByIndex(id, kind, d);
            return null;
         } else if (op instanceof EntityOps.FindByIndexOp f) {
            return (R)this.findByIndex(id, kind, f);
         } else if (op instanceof EntityOps.PrefixIndexOp p) {
            return (R)this.prefixIndex(id, kind, p);
         } else if (op instanceof EntityOps.CountByIndexOp c) {
            return (R)this.countByIndex(id, kind, c);
         } else {
            throw new UnsupportedOperationException("SqlEntityAdapter does not handle " + op.getClass().getSimpleName());
         }
      } catch (RuntimeException | SQLException var5) {
         throw new BackendException("sql entity execute failed for " + op.getClass().getSimpleName() + " on " + id, var5);
      }
   }

   private void upsertRecord(@NotNull StoreId id, @NotNull Entity<?, ?, ?> kind, @NotNull Object record) throws SQLException {
      if (!kind.recordType().isInstance(record)) {
         throw new IllegalArgumentException("record type " + record.getClass().getName() + " does not match entity " + kind.recordType().getName());
      } else {
         EncodeShape shape = kind.codec().shape();
         BsonCodec codec = BsonCodecs.regular(kind.recordType());
         if (this.dialect.usesLegacySqliteUpsert()) {
            this.upsertLegacySqliteRecord(id, shape, codec, record);
         } else {
            this.upsertModernSqlRecord(id, shape, codec, record);
         }
      }
   }

   private void upsertModernSqlRecord(@NotNull StoreId id, @NotNull EncodeShape shape, @NotNull BsonCodec codec, @NotNull Object record) throws SQLException {
      try (
         Connection c = this.ds.getConnection();
         PreparedStatement ps = c.prepareStatement(this.dialect.upsertSql(id.name(), shape));
      ) {
         bindRecordFields(ps, shape, codec, record);
         ps.executeUpdate();
      }
   }

   private void upsertLegacySqliteRecord(@NotNull StoreId id, @NotNull EncodeShape shape, @NotNull BsonCodec codec, @NotNull Object record) throws SQLException {
      List<Integer> updateIndexes = this.legacyUpdateIndexes(shape);
      String updateSql = updateIndexes.isEmpty() ? null : this.legacyUpdateSql(id.name(), shape, updateIndexes);
      int idIndex = idFieldIndex(shape);

      try (Connection c = this.ds.getConnection()) {
         boolean priorAutoCommit = c.getAutoCommit();
         c.setAutoCommit(false);

         try {
            try (PreparedStatement insert = c.prepareStatement(this.legacyInsertSql(id.name(), shape))) {
               bindRecordFields(insert, shape, codec, record);
               insert.executeUpdate();
            }

            if (updateSql != null) {
               try (PreparedStatement update = c.prepareStatement(updateSql)) {
                  bindLegacyUpdate(update, shape, codec, record, updateIndexes, idIndex);
                  update.executeUpdate();
               }
            }

            c.commit();
         } catch (RuntimeException | SQLException var29) {
            try {
               c.rollback();
            } catch (SQLException var24) {
            }

            throw var29;
         } finally {
            c.setAutoCommit(priorAutoCommit);
         }
      }
   }

   private static void bindRecordFields(@NotNull PreparedStatement ps, @NotNull EncodeShape shape, @NotNull BsonCodec codec, @NotNull Object record) throws SQLException {
      int n = shape.fields().size();

      for (int i = 0; i < n; i++) {
         EncodeShape.FieldDef f = shape.fields().get(i);
         SqlBindings.bind(ps, i + 1, f, codec.readField(record, i));
      }
   }

   private static void bindLegacyUpdate(
      @NotNull PreparedStatement ps,
      @NotNull EncodeShape shape,
      @NotNull BsonCodec codec,
      @NotNull Object record,
      @NotNull List<Integer> updateIndexes,
      int idIndex
   ) throws SQLException {
      int param = 1;

      for (int fieldIndex : updateIndexes) {
         EncodeShape.FieldDef f = shape.fields().get(fieldIndex);
         SqlBindings.bind(ps, param++, f, codec.readField(record, fieldIndex));
      }

      EncodeShape.FieldDef idField = shape.fields().get(idIndex);
      SqlBindings.bind(ps, param, idField, codec.readField(record, idIndex));
   }

   private <ID, R> Optional<R> getById(@NotNull StoreId id, @NotNull Entity<?, ?, ?> kind, @NotNull EntityOps.GetByIdOp<ID, R> op) throws SQLException {
      EncodeShape shape = kind.codec().shape();
      BsonCodec codec = BsonCodecs.regular(kind.recordType());
      String sql = "SELECT * FROM " + this.dialect.quoteIdentifier(id.name()) + " WHERE " + this.dialect.quoteIdentifier(shape.idField()) + " = ?";

      Optional var11;
      try (
         Connection c = this.ds.getConnection();
         PreparedStatement ps = c.prepareStatement(sql);
      ) {
         SqlBindings.bind(ps, 1, idFieldDef(shape), op.id());

         try (ResultSet rs = ps.executeQuery()) {
            if (!rs.next()) {
               return Optional.empty();
            }

            R record = (R)this.decodeRow(codec, shape, rs);
            var11 = Optional.of(record);
         }
      }

      return var11;
   }

   private <ID, R> List<R> getMany(@NotNull StoreId id, @NotNull Entity<?, ?, ?> kind, @NotNull EntityOps.GetManyOp<ID, R> op) throws SQLException {
      if (op.ids().isEmpty()) {
         return List.of();
      } else {
         EncodeShape shape = kind.codec().shape();
         BsonCodec codec = BsonCodecs.regular(kind.recordType());
         StringBuilder sql = new StringBuilder()
            .append("SELECT * FROM ")
            .append(this.dialect.quoteIdentifier(id.name()))
            .append(" WHERE ")
            .append(this.dialect.quoteIdentifier(shape.idField()))
            .append(" IN (");

         for (int i = 0; i < op.ids().size(); i++) {
            if (i > 0) {
               sql.append(", ");
            }

            sql.append('?');
         }

         sql.append(')');

         Object var24;
         try (
            Connection c = this.ds.getConnection();
            PreparedStatement ps = c.prepareStatement(sql.toString());
         ) {
            EncodeShape.FieldDef idDef = idFieldDef(shape);
            int i = 1;

            for (ID idVal : op.ids()) {
               SqlBindings.bind(ps, i++, idDef, idVal);
            }

            List<R> out = new ArrayList<>(op.ids().size());

            try (ResultSet rs = ps.executeQuery()) {
               while (rs.next()) {
                  out.add((R)this.decodeRow(codec, shape, rs));
               }
            }

            var24 = out;
         }

         return (List<R>)var24;
      }
   }

   private <ID> void deleteById(@NotNull StoreId id, @NotNull Entity<?, ?, ?> kind, @NotNull EntityOps.DeleteByIdOp<ID> op) throws SQLException {
      EncodeShape shape = kind.codec().shape();
      String sql = "DELETE FROM " + this.dialect.quoteIdentifier(id.name()) + " WHERE " + this.dialect.quoteIdentifier(shape.idField()) + " = ?";

      try (
         Connection c = this.ds.getConnection();
         PreparedStatement ps = c.prepareStatement(sql);
      ) {
         SqlBindings.bind(ps, 1, idFieldDef(shape), op.id());
         ps.executeUpdate();
      }
   }

   private void deleteByIndex(@NotNull StoreId id, @NotNull Entity<?, ?, ?> kind, @NotNull EntityOps.DeleteByIndexOp op) throws SQLException {
      IndexSpec spec = requireIndex(kind, op.indexName());
      EncodeShape shape = kind.codec().shape();
      String leading = stripDir(spec.fields().get(0));
      String sql = "DELETE FROM " + this.dialect.quoteIdentifier(id.name()) + " WHERE " + this.dialect.quoteIdentifier(leading) + " = ?";

      try (
         Connection c = this.ds.getConnection();
         PreparedStatement ps = c.prepareStatement(sql);
      ) {
         SqlBindings.bind(ps, 1, fieldDef(shape, leading), op.key());
         ps.executeUpdate();
      }
   }

   private Object decodeRow(@NotNull BsonCodec codec, @NotNull EncodeShape shape, @NotNull ResultSet rs) throws SQLException {
      Object[] args = new Object[shape.fields().size()];

      for (int i = 0; i < shape.fields().size(); i++) {
         args[i] = SqlBindings.extract(rs, shape.fields().get(i));
      }

      return codec.decodeFromValues(args);
   }

   @NotNull
   private static EncodeShape.FieldDef idFieldDef(@NotNull EncodeShape shape) {
      for (EncodeShape.FieldDef f : shape.fields()) {
         if (f.name().equals(shape.idField())) {
            return f;
         }
      }

      throw new IllegalStateException("shape has no field matching idField " + shape.idField());
   }

   private <R> Page<R> findByIndex(@NotNull StoreId id, @NotNull Entity<?, ?, ?> kind, @NotNull EntityOps.FindByIndexOp<R> op) throws SQLException {
      EncodeShape shape = kind.codec().shape();
      BsonCodec codec = BsonCodecs.regular(kind.recordType());
      IndexSpec spec = requireIndex(kind, op.indexName());
      String leadingCol = stripDir(spec.fields().get(0));
      EncodeShape.FieldDef leadingDef = fieldDef(shape, leadingCol);
      String leadingExpr = spec.caseInsensitivePrefix() ? "LOWER(" + this.dialect.quoteIdentifier(leadingCol) + ")" : this.dialect.quoteIdentifier(leadingCol);
      Object leadingValue = spec.caseInsensitivePrefix() && op.key() instanceof String s ? s.toLowerCase(Locale.ROOT) : op.key();
      List<SqlEntityAdapter.BoundValue> params = new ArrayList<>();
      StringBuilder where = new StringBuilder();
      where.append(leadingExpr).append(" = ?");
      params.add(new SqlEntityAdapter.BoundValue(leadingDef, leadingValue));
      this.appendCursorWhere(where, params, shape, spec, 0, op.cursor());
      return this.pageRead(id, kind, shape, codec, spec, 0, where, params, op.pageSize());
   }

   private <R> Page<R> prefixIndex(@NotNull StoreId id, @NotNull Entity<?, ?, ?> kind, @NotNull EntityOps.PrefixIndexOp<R> op) throws SQLException {
      EncodeShape shape = kind.codec().shape();
      BsonCodec codec = BsonCodecs.regular(kind.recordType());
      IndexSpec spec = requireIndex(kind, op.indexName());
      String leadingCol = stripDir(spec.fields().get(0));
      EncodeShape.FieldDef leadingDef = fieldDef(shape, leadingCol);
      String filterExpr = spec.caseInsensitivePrefix() ? "LOWER(" + this.dialect.quoteIdentifier(leadingCol) + ")" : this.dialect.quoteIdentifier(leadingCol);
      String prefix = spec.caseInsensitivePrefix() ? op.prefix().toLowerCase(Locale.ROOT) : op.prefix();
      String escaped = escapeLike(prefix);
      StringBuilder where = new StringBuilder();
      where.append(filterExpr).append(" LIKE ? ESCAPE '!'");
      List<SqlEntityAdapter.BoundValue> params = new ArrayList<>();
      params.add(new SqlEntityAdapter.BoundValue(stringFieldDef(), escaped + "%"));
      this.appendCursorWhere(where, params, shape, spec, -1, op.cursor());
      return this.pageRead(id, kind, shape, codec, spec, -1, where, params, op.pageSize());
   }

   private long countByIndex(@NotNull StoreId id, @NotNull Entity<?, ?, ?> kind, @NotNull EntityOps.CountByIndexOp op) throws SQLException {
      EncodeShape shape = kind.codec().shape();
      IndexSpec spec = requireIndex(kind, op.indexName());
      String leadingCol = stripDir(spec.fields().get(0));
      String sql = "SELECT COUNT(*) FROM " + this.dialect.quoteIdentifier(id.name()) + " WHERE " + this.dialect.quoteIdentifier(leadingCol) + " = ?";

      long var11;
      try (
         Connection c = this.ds.getConnection();
         PreparedStatement ps = c.prepareStatement(sql);
      ) {
         SqlBindings.bind(ps, 1, fieldDef(shape, leadingCol), op.key());

         try (ResultSet rs = ps.executeQuery()) {
            var11 = rs.next() ? rs.getLong(1) : 0L;
         }
      }

      return var11;
   }

   private <R> Page<R> pageRead(
      @NotNull StoreId id,
      @NotNull Entity<?, ?, ?> kind,
      @NotNull EncodeShape shape,
      @NotNull BsonCodec codec,
      @NotNull IndexSpec spec,
      int equalityColumnIndex,
      @NotNull StringBuilder where,
      @NotNull List<SqlEntityAdapter.BoundValue> params,
      int pageSize
   ) throws SQLException {
      int ps = Math.max(1, pageSize);
      String orderBy = this.buildOrderBy(spec, shape);
      String sql = "SELECT * FROM " + this.dialect.quoteIdentifier(id.name()) + " WHERE " + where + " " + orderBy + " LIMIT " + (ps + 1);

      Page var20;
      try (
         Connection c = this.ds.getConnection();
         PreparedStatement st = c.prepareStatement(sql);
      ) {
         for (int i = 0; i < params.size(); i++) {
            SqlEntityAdapter.BoundValue p = params.get(i);
            SqlBindings.bind(st, i + 1, p.def, p.value);
         }

         List<R> items = new ArrayList<>(ps);
         Object lastRecord = null;

         try (ResultSet rs = st.executeQuery()) {
            while (items.size() < ps && rs.next()) {
               R rec = (R)this.decodeRow(codec, shape, rs);
               items.add(rec);
               lastRecord = rec;
            }

            boolean hasMore = rs.next();
            Cursor next = hasMore && lastRecord != null ? this.encodeCursorFromRecord(lastRecord, shape, codec, spec, equalityColumnIndex) : null;
            var20 = new Page<>(items, next);
         }
      }

      return var20;
   }

   @NotNull
   private String buildOrderBy(@NotNull IndexSpec spec, @NotNull EncodeShape shape) {
      StringBuilder sb = new StringBuilder("ORDER BY ");

      for (int i = 0; i < spec.fields().size(); i++) {
         String f = spec.fields().get(i);
         if (i > 0) {
            sb.append(", ");
         }

         boolean desc = f.startsWith("-");
         String name = desc ? f.substring(1) : f;
         if (i == 0 && spec.caseInsensitivePrefix()) {
            sb.append("LOWER(").append(this.dialect.quoteIdentifier(name)).append(')');
         } else {
            sb.append(this.dialect.quoteIdentifier(name));
         }

         sb.append(desc ? " DESC" : " ASC");
      }

      sb.append(", ").append(this.dialect.quoteIdentifier(shape.idField())).append(" ASC");
      return sb.toString();
   }

   @NotNull
   private Cursor encodeCursorFromRecord(
      @NotNull Object record, @NotNull EncodeShape shape, @NotNull BsonCodec codec, @NotNull IndexSpec spec, int equalityColumnIndex
   ) {
      int orderedIdx = orderedColumnIndex(spec, equalityColumnIndex);
      byte[] idBytes = idToBytes(codec.readField(record, fieldIndex(shape, shape.idField())));
      if (orderedIdx < 0) {
         return Cursors.encode(0L, idBytes);
      } else {
         String orderedCol = stripDir(spec.fields().get(orderedIdx));
         int idx = fieldIndex(shape, orderedCol);
         Object orderedVal = codec.readField(record, idx);
         Class<?> t = shape.fields().get(idx).javaType();
         if (orderedVal == null) {
            return Cursors.encode(0L, idBytes);
         } else if (t != long.class && t != Long.class && t != int.class && t != Integer.class && !t.isEnum() && t != boolean.class && t != Boolean.class) {
            if (t == double.class || t == Double.class || t == float.class || t == Float.class) {
               double d = ((Number)orderedVal).doubleValue();
               byte[] payload = new byte[8];
               ByteBuffer.wrap(payload).putDouble(d);
               return Cursors.encodeTyped((byte)3, payload, idBytes);
            } else if (t == String.class) {
               String s = (String)orderedVal;
               if (spec.caseInsensitivePrefix() && orderedIdx == 0) {
                  s = s.toLowerCase(Locale.ROOT);
               }

               return Cursors.encodeTyped((byte)1, s.getBytes(StandardCharsets.UTF_8), idBytes);
            } else if (t == UUID.class) {
               byte[] payload = uuidToBytes((UUID)orderedVal);
               return Cursors.encodeTyped((byte)2, packBinary((byte)4, payload), idBytes);
            } else {
               return t == byte[].class
                  ? Cursors.encodeTyped((byte)2, packBinary((byte)0, (byte[])orderedVal), idBytes)
                  : Cursors.encodeTyped((byte)1, String.valueOf(orderedVal).getBytes(StandardCharsets.UTF_8), idBytes);
            }
         } else {
            long ord = orderedVal instanceof Number n
               ? n.longValue()
               : (orderedVal instanceof Enum<?> e ? e.ordinal() : (orderedVal instanceof Boolean b ? (b ? 1L : 0L) : 0L));
            return Cursors.encode(ord, idBytes);
         }
      }
   }

   private void appendCursorWhere(
      @NotNull StringBuilder where,
      @NotNull List<SqlEntityAdapter.BoundValue> params,
      @NotNull EncodeShape shape,
      @NotNull IndexSpec spec,
      int equalityColumnIndex,
      @Nullable Cursor cursor
   ) {
      if (cursor != null) {
         int orderedIdx = orderedColumnIndex(spec, equalityColumnIndex);
         EncodeShape.FieldDef idDef = fieldDef(shape, shape.idField());
         byte schema = Cursors.peekSchema(cursor);
         if (orderedIdx < 0) {
            byte[] idBytes = schema == 2 ? Cursors.decodeTyped(cursor).idBytes() : Cursors.decode(cursor).idBytes();
            where.append(" AND ").append(this.dialect.quoteIdentifier(shape.idField())).append(" > ?");
            params.add(new SqlEntityAdapter.BoundValue(idDef, bytesToIdValue(idDef, idBytes)));
         } else {
            String orderedCol = stripDir(spec.fields().get(orderedIdx));
            EncodeShape.FieldDef orderedDef = fieldDef(shape, orderedCol);
            String orderedExpr = spec.caseInsensitivePrefix() && orderedIdx == 0
               ? "LOWER(" + this.dialect.quoteIdentifier(orderedCol) + ")"
               : this.dialect.quoteIdentifier(orderedCol);
            boolean desc = spec.fields().get(orderedIdx).startsWith("-");
            Object orderedVal;
            byte[] idBytes;
            if (schema == 2) {
               Cursors.DecodedTyped d = Cursors.decodeTyped(cursor);
               orderedVal = decodeTypedOrderedValue(d.typeTag(), d.orderedBytes(), orderedDef);
               idBytes = d.idBytes();
            } else {
               Cursors.Decoded d = Cursors.decode(cursor);
               orderedVal = decodeNumericOrderedValue(d.orderedKey(), orderedDef);
               idBytes = d.idBytes();
            }

            Object idValue = bytesToIdValue(idDef, idBytes);
            String cmp = desc ? "<" : ">";
            where.append(" AND (")
               .append(orderedExpr)
               .append(' ')
               .append(cmp)
               .append(" ?")
               .append(" OR (")
               .append(orderedExpr)
               .append(" = ? AND ")
               .append(this.dialect.quoteIdentifier(shape.idField()))
               .append(" > ?))");
            params.add(new SqlEntityAdapter.BoundValue(orderedDef, orderedVal));
            params.add(new SqlEntityAdapter.BoundValue(orderedDef, orderedVal));
            params.add(new SqlEntityAdapter.BoundValue(idDef, idValue));
         }
      }
   }

   private static int orderedColumnIndex(@NotNull IndexSpec spec, int equalityColumnIndex) {
      int next = equalityColumnIndex + 1;
      return next < spec.fields().size() ? next : -1;
   }

   @NotNull
   private static String stripDir(@NotNull String f) {
      return f.startsWith("-") ? f.substring(1) : f;
   }

   @NotNull
   private static EncodeShape.FieldDef fieldDef(@NotNull EncodeShape shape, @NotNull String name) {
      for (EncodeShape.FieldDef f : shape.fields()) {
         if (f.name().equals(name)) {
            return f;
         }
      }

      throw new IllegalStateException("shape has no field " + name);
   }

   private static int fieldIndex(@NotNull EncodeShape shape, @NotNull String name) {
      for (int i = 0; i < shape.fields().size(); i++) {
         if (shape.fields().get(i).name().equals(name)) {
            return i;
         }
      }

      throw new IllegalStateException("shape has no field " + name);
   }

   @NotNull
   private static EncodeShape.FieldDef stringFieldDef() {
      return new EncodeShape.FieldDef("__prefix__", String.class, FieldKind.VALUE, false, null, 0, 0);
   }

   @NotNull
   private static IndexSpec requireIndex(@NotNull Entity<?, ?, ?> kind, @NotNull String name) {
      for (IndexSpec spec : kind.secondaryIndexes()) {
         if (spec.name().equals(name)) {
            return spec;
         }
      }

      throw new IllegalArgumentException("no secondary index named '" + name + "' on Entity " + kind.name());
   }

   @NotNull
   private static byte[] uuidToBytes(@NotNull UUID u) {
      byte[] out = new byte[16];
      ByteBuffer.wrap(out).putLong(u.getMostSignificantBits()).putLong(u.getLeastSignificantBits());
      return out;
   }

   @NotNull
   private static byte[] idToBytes(@Nullable Object idValue) {
      if (idValue == null) {
         return new byte[0];
      } else if (idValue instanceof UUID u) {
         return uuidToBytes(u);
      } else if (idValue instanceof byte[] b) {
         return b;
      } else if (idValue instanceof Number n) {
         byte[] out = new byte[8];
         ByteBuffer.wrap(out).putLong(n.longValue());
         return out;
      } else {
         return idValue.toString().getBytes(StandardCharsets.UTF_8);
      }
   }

   @NotNull
   private static Object bytesToIdValue(@NotNull EncodeShape.FieldDef idDef, @NotNull byte[] idBytes) {
      Class<?> t = idDef.javaType();
      if (t == UUID.class) {
         if (idBytes.length != 16) {
            throw new IllegalArgumentException("UUID id cursor bytes must be 16, got " + idBytes.length);
         } else {
            ByteBuffer buf = ByteBuffer.wrap(idBytes);
            return new UUID(buf.getLong(), buf.getLong());
         }
      } else if (t == String.class) {
         return new String(idBytes, StandardCharsets.UTF_8);
      } else if (t == long.class || t == Long.class) {
         if (idBytes.length != 8) {
            throw new IllegalArgumentException("long id cursor bytes must be 8, got " + idBytes.length);
         } else {
            return ByteBuffer.wrap(idBytes).getLong();
         }
      } else if (t == byte[].class) {
         return idBytes;
      } else {
         throw new IllegalArgumentException("cannot decode id of type " + t.getName() + " from cursor bytes");
      }
   }

   @NotNull
   private static byte[] packBinary(byte subtype, @NotNull byte[] data) {
      byte[] out = new byte[1 + data.length];
      out[0] = subtype;
      System.arraycopy(data, 0, out, 1, data.length);
      return out;
   }

   @NotNull
   private static Object decodeTypedOrderedValue(byte typeTag, @NotNull byte[] bytes, @NotNull EncodeShape.FieldDef def) {
      switch (typeTag) {
         case 1:
            return new String(bytes, StandardCharsets.UTF_8);
         case 2:
            if (bytes.length < 1) {
               throw new IllegalArgumentException("BINARY typed cursor missing subtype");
            } else {
               byte[] data = new byte[bytes.length - 1];
               System.arraycopy(bytes, 1, data, 0, data.length);
               if (def.javaType() == UUID.class) {
                  if (data.length != 16) {
                     throw new IllegalArgumentException("UUID column expects 16-byte cursor payload");
                  }

                  ByteBuffer buf = ByteBuffer.wrap(data);
                  return new UUID(buf.getLong(), buf.getLong());
               }

               return data;
            }
         case 3:
            if (bytes.length != 8) {
               throw new IllegalArgumentException("DOUBLE typed cursor must be 8 bytes");
            }

            return ByteBuffer.wrap(bytes).getDouble();
         case 4:
            if (bytes.length != 8) {
               throw new IllegalArgumentException("DATE typed cursor must be 8 bytes");
            }

            return new Date(ByteBuffer.wrap(bytes).getLong());
         default:
            throw new IllegalArgumentException("unknown typed-cursor tag 0x" + Integer.toHexString(typeTag & 255));
      }
   }

   @NotNull
   private static Object decodeNumericOrderedValue(long key, @NotNull EncodeShape.FieldDef def) {
      Class<?> t = def.javaType();
      if (t == long.class || t == Long.class) {
         return key;
      } else if (t == int.class || t == Integer.class) {
         return (int)key;
      } else if (t == boolean.class || t == Boolean.class) {
         return key != 0L;
      } else if (t.isEnum()) {
         Object[] constants = t.getEnumConstants();
         int ord = (int)key;
         return constants[ord];
      } else if (t == double.class || t == Double.class) {
         return (double)key;
      } else {
         return t != float.class && t != Float.class ? key : (float)key;
      }
   }

   @NotNull
   private static String escapeLike(@NotNull String s) {
      StringBuilder sb = new StringBuilder(s.length());

      for (int i = 0; i < s.length(); i++) {
         char c = s.charAt(i);
         if (c == '%' || c == '_' || c == '!') {
            sb.append('!');
         }

         sb.append(c);
      }

      return sb.toString();
   }

   @NotNull
   public List<Migration<Entity<?, ?, ?>>> migrations(@NotNull Entity<?, ?, ?> kind) {
      return List.of();
   }

   private record BoundValue(@NotNull EncodeShape.FieldDef def, @Nullable Object value) {
   }

   private final class LegacySqliteEntityHandler<E> implements StorageEventHandler<E> {
      @NotNull
      private final StoreId id;
      @NotNull
      private final String insertSql;
      @Nullable
      private final String updateSql;
      @NotNull
      private final List<Integer> updateIndexes;
      @NotNull
      private final BsonCodec codec;
      @NotNull
      private final EncodeShape shape;
      private final int idIndex;
      @NotNull
      private final Function<E, Object> eventToRecord;
      @NotNull
      private final List<Object> pendingRecords = new ArrayList<>(256);

      LegacySqliteEntityHandler(@NotNull StoreId id, @NotNull Entity<?, E, ?> kind) {
         this.id = id;
         this.shape = kind.codec().shape();
         this.insertSql = SqlEntityAdapter.this.legacyInsertSql(id.name(), this.shape);
         this.updateIndexes = SqlEntityAdapter.this.legacyUpdateIndexes(this.shape);
         this.updateSql = this.updateIndexes.isEmpty() ? null : SqlEntityAdapter.this.legacyUpdateSql(id.name(), this.shape, this.updateIndexes);
         this.idIndex = SqlEntityAdapter.idFieldIndex(this.shape);
         this.codec = BsonCodecs.regular(kind.recordType());
         this.eventToRecord = (Function<E, Object>)kind.eventToRecord();
      }

      @Override
      public void onEvent(E event, long sequence, boolean endOfBatch) throws BackendException {
         Object record;
         try {
            record = this.eventToRecord.apply(event);
         } catch (RuntimeException var7) {
            throw new BackendException("sql entity upsert failed for " + this.id, var7);
         }

         try {
            this.pendingRecords.add(record);
            if (endOfBatch || this.pendingRecords.size() >= 256) {
               this.flushBatch();
            }
         } catch (RuntimeException | SQLException var8) {
            throw new BackendException("sql entity upsert failed for " + this.id, var8);
         }
      }

      private void flushBatch() throws SQLException {
         if (!this.pendingRecords.isEmpty()) {
            try (Connection c = SqlEntityAdapter.this.ds.getConnection()) {
               boolean priorAutoCommit = c.getAutoCommit();
               c.setAutoCommit(false);

               try {
                  try (PreparedStatement insert = c.prepareStatement(this.insertSql)) {
                     for (Object record : this.pendingRecords) {
                        this.bindAllFields(insert, record);
                        insert.addBatch();
                     }

                     insert.executeBatch();
                  }

                  if (this.updateSql != null) {
                     try (PreparedStatement update = c.prepareStatement(this.updateSql)) {
                        for (Object record : this.pendingRecords) {
                           this.bindUpdate(update, record);
                           update.addBatch();
                        }

                        update.executeBatch();
                     }
                  }

                  c.commit();
               } catch (RuntimeException | SQLException var34) {
                  try {
                     c.rollback();
                  } catch (SQLException var29) {
                  }

                  throw var34;
               } finally {
                  c.setAutoCommit(priorAutoCommit);
               }
            } finally {
               this.pendingRecords.clear();
            }
         }
      }

      private void bindAllFields(@NotNull PreparedStatement ps, @NotNull Object record) throws SQLException {
         SqlEntityAdapter.bindRecordFields(ps, this.shape, this.codec, record);
      }

      private void bindUpdate(@NotNull PreparedStatement ps, @NotNull Object record) throws SQLException {
         SqlEntityAdapter.bindLegacyUpdate(ps, this.shape, this.codec, record, this.updateIndexes, this.idIndex);
      }
   }

   private final class SqlEntityHandler<E> implements StorageEventHandler<E> {
      @NotNull
      private final StoreId id;
      @NotNull
      private final String upsertSql;
      @NotNull
      private final BsonCodec codec;
      @NotNull
      private final EncodeShape shape;
      @NotNull
      private final Function<E, Object> eventToRecord;
      @NotNull
      private final List<Object> pendingRecords = new ArrayList<>(256);

      SqlEntityHandler(@NotNull StoreId id, @NotNull Entity<?, E, ?> kind) {
         this.id = id;
         this.shape = kind.codec().shape();
         this.upsertSql = SqlEntityAdapter.this.dialect.upsertSql(id.name(), this.shape);
         this.codec = BsonCodecs.regular(kind.recordType());
         this.eventToRecord = (Function<E, Object>)kind.eventToRecord();
      }

      @Override
      public void onEvent(E event, long sequence, boolean endOfBatch) throws BackendException {
         Object record;
         try {
            record = this.eventToRecord.apply(event);
         } catch (RuntimeException var7) {
            throw new BackendException("sql entity upsert failed for " + this.id, var7);
         }

         try {
            this.pendingRecords.add(record);
            if (endOfBatch || this.pendingRecords.size() >= 256) {
               this.flushBatch();
            }
         } catch (RuntimeException | SQLException var8) {
            throw new BackendException("sql entity upsert failed for " + this.id, var8);
         }
      }

      private void flushBatch() throws SQLException {
         if (!this.pendingRecords.isEmpty()) {
            try (Connection c = SqlEntityAdapter.this.ds.getConnection()) {
               boolean priorAutoCommit = c.getAutoCommit();
               c.setAutoCommit(false);

               try (PreparedStatement ps = c.prepareStatement(this.upsertSql)) {
                  for (Object record : this.pendingRecords) {
                     this.bindAllFields(ps, record);
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

      private void bindAllFields(@NotNull PreparedStatement ps, @NotNull Object record) throws SQLException {
         SqlEntityAdapter.bindRecordFields(ps, this.shape, this.codec, record);
      }
   }
}
