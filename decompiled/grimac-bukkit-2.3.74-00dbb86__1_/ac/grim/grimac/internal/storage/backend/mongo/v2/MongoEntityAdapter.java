package ac.grim.grimac.internal.storage.backend.mongo.v2;

import ac.grim.grimac.api.storage.backend.BackendException;
import ac.grim.grimac.api.storage.backend.KindAdapter;
import ac.grim.grimac.api.storage.backend.StorageEventHandler;
import ac.grim.grimac.api.storage.category.Capability;
import ac.grim.grimac.api.storage.category.Category;
import ac.grim.grimac.api.storage.codec.EncodeShape;
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
import ac.grim.grimac.internal.storage.codec.bson.BsonBinaries;
import ac.grim.grimac.internal.storage.codec.bson.BsonCodec;
import ac.grim.grimac.internal.storage.codec.bson.BsonCodecs;
import ac.grim.grimac.shaded.jetbrains.annotations.ApiStatus;
import ac.grim.grimac.shaded.jetbrains.annotations.NotNull;
import ac.grim.grimac.shaded.jetbrains.annotations.Nullable;
import com.mongodb.MongoCommandException;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.IndexOptions;
import com.mongodb.client.model.Indexes;
import com.mongodb.client.model.UpdateOptions;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import org.bson.BsonBinary;
import org.bson.BsonBinarySubType;
import org.bson.BsonBinaryWriter;
import org.bson.Document;
import org.bson.RawBsonDocument;
import org.bson.conversions.Bson;
import org.bson.types.Binary;

@ApiStatus.Internal
public final class MongoEntityAdapter implements KindAdapter<Entity<?, ?, ?>> {
   private static final int NAMESPACE_EXISTS = 48;
   private static final int INDEX_OPTIONS_CONFLICT = 85;
   @NotNull
   private final MongoDatabase db;
   @NotNull
   private final Logger logger;
   private final Map<String, MongoCollection<Document>> docCollections = new ConcurrentHashMap<>();
   private final Map<String, MongoCollection<RawBsonDocument>> rawCollections = new ConcurrentHashMap<>();
   private static final ThreadLocal<MongoBsonBuffer> BSON_BUFFER = ThreadLocal.withInitial(MongoBsonBuffer::new);

   public MongoEntityAdapter(@NotNull MongoDatabase db, @NotNull Logger logger) {
      this.db = db;
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
      String coll = id.name();

      try {
         if (!this.collectionExists(coll)) {
            try {
               this.db.createCollection(coll);
            } catch (MongoCommandException var10) {
               if (var10.getErrorCode() != 48) {
                  throw var10;
               }
            }
         }

         MongoCollection<Document> c = this.db.getCollection(coll);

         for (IndexSpec spec : kind.secondaryIndexes()) {
            this.createIndexIgnoringConflict(c, buildIndex(spec), new IndexOptions().unique(spec.unique()).name(spec.name()));
            if (spec.caseInsensitivePrefix()) {
               for (String f : spec.fields()) {
                  String field = stripDir(f);
                  this.createIndexIgnoringConflict(c, Indexes.ascending(new String[]{field + "_lower"}), new IndexOptions().name(spec.name() + "_lower"));
               }
            }
         }
      } catch (RuntimeException var11) {
         throw new BackendException("failed to ensure entity collection " + coll, var11);
      }
   }

   public void dropStore(@NotNull StoreId id, @NotNull Entity<?, ?, ?> kind) throws BackendException {
      try {
         this.db.getCollection(id.name()).drop();
         this.docCollections.remove(id.qualified());
         this.rawCollections.remove(id.qualified());
      } catch (RuntimeException var4) {
         throw new BackendException("failed to drop " + id, var4);
      }
   }

   @NotNull
   public <E> StorageEventHandler<E> writeHandler(@NotNull StoreId id, @NotNull Entity<?, ?, ?> kind, @NotNull Category<E> category) {
      return new MongoEntityAdapter.EntityHandler<>(id, (Entity<?, E, ?>)kind, this.rawCollection(id));
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
         } else if (op instanceof EntityOps.FindByIndexOp f) {
            return (R)this.findByIndex(id, kind, f);
         } else if (op instanceof EntityOps.PrefixIndexOp p) {
            return (R)this.prefixIndex(id, kind, p);
         } else if (op instanceof EntityOps.CountByIndexOp c) {
            return (R)this.countByIndex(id, kind, c);
         } else if (op instanceof EntityOps.DeleteByIdOp d) {
            this.deleteById(id, d);
            return null;
         } else if (op instanceof EntityOps.DeleteByIndexOp d) {
            this.deleteByIndex(id, kind, d);
            return null;
         } else {
            throw new UnsupportedOperationException("MongoEntityAdapter does not handle " + op.getClass().getName());
         }
      } catch (RuntimeException var5) {
         throw new BackendException("mongo entity execute failed for " + op.getClass().getSimpleName() + " on " + id, var5);
      }
   }

   @NotNull
   public List<Migration<Entity<?, ?, ?>>> migrations(@NotNull Entity<?, ?, ?> kind) {
      return List.of(new MongoEntityV6ToV7UuidSubtypeMigration());
   }

   private void upsertRecord(@NotNull StoreId id, @NotNull Entity<?, ?, ?> kind, @NotNull Object record) throws BackendException {
      if (!kind.recordType().isInstance(record)) {
         throw new IllegalArgumentException("record type " + record.getClass().getName() + " does not match entity " + kind.recordType().getName());
      } else {
         new MongoEntityAdapter.EntityHandler<>(id, kind, this.rawCollection(id)).upsertRecord(record);
      }
   }

   private boolean collectionExists(@NotNull String name) {
      MongoCursor var2 = this.db.listCollectionNames().iterator();

      while (var2.hasNext()) {
         String existing = (String)var2.next();
         if (existing.equals(name)) {
            return true;
         }
      }

      return false;
   }

   private void createIndexIgnoringConflict(@NotNull MongoCollection<Document> c, @NotNull Bson keys, @NotNull IndexOptions opts) {
      try {
         c.createIndex(keys, opts);
      } catch (MongoCommandException var5) {
         if (var5.getErrorCode() == 85) {
            this.logger
               .fine(
                  () -> "index "
                     + opts.getName()
                     + " conflicts with existing equivalent key on "
                     + c.getNamespace().getCollectionName()
                     + " — treating as success"
               );
         } else {
            throw var5;
         }
      }
   }

   @NotNull
   private static Bson buildIndex(@NotNull IndexSpec spec) {
      if (spec.fields().size() == 1) {
         String f = spec.fields().get(0);
         return f.startsWith("-") ? Indexes.descending(new String[]{stripDir(f)}) : Indexes.ascending(new String[]{stripDir(f)});
      } else {
         List<Bson> parts = new ArrayList<>(spec.fields().size());

         for (String f : spec.fields()) {
            parts.add(f.startsWith("-") ? Indexes.descending(new String[]{stripDir(f)}) : Indexes.ascending(new String[]{stripDir(f)}));
         }

         return Indexes.compoundIndex(parts);
      }
   }

   @NotNull
   private static String stripDir(@NotNull String f) {
      return f.startsWith("-") ? f.substring(1) : f;
   }

   @NotNull
   private static Object literal(@Nullable Object v) {
      return new Document("$literal", v);
   }

   @Nullable
   private static Object toBsonValue(@Nullable Object v, @NotNull Class<?> javaType) {
      if (v == null) {
         return null;
      } else if (javaType == UUID.class) {
         return BsonBinaries.uuidBinary((UUID)v);
      } else if (javaType == byte[].class) {
         return new BsonBinary((byte[])v);
      } else {
         return javaType.isEnum() ? ((Enum)v).ordinal() : v;
      }
   }

   @NotNull
   private static MongoEntityAdapter.LowerCompanion[] buildLowerCompanions(@NotNull Entity<?, ?, ?> kind) {
      EncodeShape shape = kind.codec().shape();
      List<MongoEntityAdapter.LowerCompanion> out = new ArrayList<>();

      for (IndexSpec spec : kind.secondaryIndexes()) {
         if (spec.caseInsensitivePrefix()) {
            for (String f : spec.fields()) {
               String fieldName = stripDir(f);
               int idx = resolveFieldIndex(shape, fieldName);
               if (idx < 0) {
                  throw new IllegalStateException(
                     "caseInsensitivePrefix index '" + spec.name() + "' references unknown field '" + fieldName + "' on " + kind.recordType().getName()
                  );
               }

               out.add(new MongoEntityAdapter.LowerCompanion(idx, fieldName + "_lower"));
            }
         }
      }

      return out.toArray(new MongoEntityAdapter.LowerCompanion[0]);
   }

   private static int resolveFieldIndex(@NotNull EncodeShape shape, @NotNull String fieldName) {
      List<EncodeShape.FieldDef> fields = shape.fields();

      for (int i = 0; i < fields.size(); i++) {
         if (fields.get(i).name().equals(fieldName)) {
            return i;
         }
      }

      return -1;
   }

   private <ID, R> Optional<R> getById(@NotNull StoreId id, @NotNull Entity<?, ?, ?> kind, @NotNull EntityOps.GetByIdOp<ID, R> op) {
      Document doc = (Document)this.docColl(id).find(Filters.eq("_id", encodeIdValue(op.id()))).first();
      if (doc == null) {
         return Optional.empty();
      } else {
         BsonCodec codec = BsonCodecs.regular(kind.recordType());
         R r = (R)codec.decode(projectIdIfMissing(doc, kind.codec().shape()));
         return Optional.of(r);
      }
   }

   @NotNull
   private static Document projectIdIfMissing(@NotNull Document doc, @NotNull EncodeShape shape) {
      String idName = shape.idField();
      if (idName.equals("_id")) {
         return doc;
      } else if (doc.containsKey(idName)) {
         return doc;
      } else {
         Object id = doc.get("_id");
         if (id == null) {
            return doc;
         } else {
            Document copy = new Document(doc);
            copy.put(idName, id);
            return copy;
         }
      }
   }

   private <ID, R> List<R> getMany(@NotNull StoreId id, @NotNull Entity<?, ?, ?> kind, @NotNull EntityOps.GetManyOp<ID, R> op) {
      if (op.ids().isEmpty()) {
         return List.of();
      } else {
         List<Object> encodedIds = new ArrayList<>(op.ids().size());

         for (ID i : op.ids()) {
            encodedIds.add(encodeIdValue(i));
         }

         BsonCodec codec = BsonCodecs.regular(kind.recordType());
         List<R> out = new ArrayList<>(op.ids().size());
         EncodeShape shape = kind.codec().shape();
         MongoCursor var8 = this.docColl(id).find(Filters.in("_id", encodedIds)).iterator();

         while (var8.hasNext()) {
            Document d = (Document)var8.next();
            R r = (R)codec.decode(projectIdIfMissing(d, shape));
            out.add(r);
         }

         return out;
      }
   }

   private <R> Page<R> findByIndex(@NotNull StoreId id, @NotNull Entity<?, ?, ?> kind, @NotNull EntityOps.FindByIndexOp<R> op) {
      IndexSpec spec = requireIndex(kind, op.indexName());
      IndexSpec effective = caseInsensitiveEffective(spec);
      Object key = spec.caseInsensitivePrefix() && op.key() instanceof String s ? s.toLowerCase(Locale.ROOT) : op.key();
      Bson filter = Filters.eq(stripDir(effective.fields().get(0)), encodeIndexValue(key));
      return this.pageRead(id, kind, filter, effective, 0, op.cursor(), op.pageSize());
   }

   private <R> Page<R> prefixIndex(@NotNull StoreId id, @NotNull Entity<?, ?, ?> kind, @NotNull EntityOps.PrefixIndexOp<R> op) {
      IndexSpec spec = requireIndex(kind, op.indexName());
      IndexSpec effective = caseInsensitiveEffective(spec);
      String field = stripDir(effective.fields().get(0));
      String prefix = spec.caseInsensitivePrefix() ? op.prefix().toLowerCase(Locale.ROOT) : op.prefix();
      String pattern = "^" + Pattern.quote(prefix);
      Bson filter = Filters.regex(field, pattern, "");
      return this.pageRead(id, kind, filter, effective, -1, op.cursor(), op.pageSize());
   }

   @NotNull
   private static IndexSpec caseInsensitiveEffective(@NotNull IndexSpec spec) {
      if (!spec.caseInsensitivePrefix()) {
         return spec;
      } else {
         List<String> fields = spec.fields();
         if (fields.isEmpty()) {
            return spec;
         } else {
            List<String> rewritten = new ArrayList<>(fields);
            String leading = rewritten.get(0);
            boolean desc = leading.startsWith("-");
            String name = desc ? leading.substring(1) : leading;
            rewritten.set(0, (desc ? "-" : "") + name + "_lower");
            return new IndexSpec(spec.name(), List.copyOf(rewritten), spec.unique(), spec.caseInsensitivePrefix());
         }
      }
   }

   private long countByIndex(@NotNull StoreId id, @NotNull Entity<?, ?, ?> kind, @NotNull EntityOps.CountByIndexOp op) {
      IndexSpec spec = requireIndex(kind, op.indexName());
      String leading = stripDir(spec.fields().get(0));
      return this.docColl(id).countDocuments(Filters.eq(leading, encodeIndexValue(op.key())));
   }

   private <ID> void deleteById(@NotNull StoreId id, @NotNull EntityOps.DeleteByIdOp<ID> op) {
      this.docColl(id).deleteOne(Filters.eq("_id", encodeIdValue(op.id())));
   }

   private void deleteByIndex(@NotNull StoreId id, @NotNull Entity<?, ?, ?> kind, @NotNull EntityOps.DeleteByIndexOp op) {
      IndexSpec spec = requireIndex(kind, op.indexName());
      Bson filter = Filters.eq(stripDir(spec.fields().get(0)), encodeIndexValue(op.key()));
      this.docColl(id).deleteMany(filter);
   }

   private <R> Page<R> pageRead(
      @NotNull StoreId id,
      @NotNull Entity<?, ?, ?> kind,
      @NotNull Bson filter,
      @NotNull IndexSpec spec,
      int equalityColumnIndex,
      @Nullable Cursor cursor,
      int pageSize
   ) {
      int ps = Math.max(1, pageSize);
      BsonCodec codec = BsonCodecs.regular(kind.recordType());
      Bson finalFilter = this.applyCursor(filter, spec, equalityColumnIndex, cursor);
      List<Document> docs = new ArrayList<>(ps + 1);
      Bson sort = buildSort(spec);
      this.docColl(id).find(finalFilter).sort(sort).limit(ps + 1).into(docs);
      boolean hasMore = docs.size() > ps;
      List<R> items = new ArrayList<>(Math.min(docs.size(), ps));
      Document last = null;
      int n = Math.min(docs.size(), ps);
      EncodeShape shape = kind.codec().shape();

      for (int i = 0; i < n; i++) {
         Document d = docs.get(i);
         R r = (R)codec.decode(projectIdIfMissing(d, shape));
         items.add(r);
         last = d;
      }

      Cursor next = hasMore && last != null ? encodeCursor(last, spec, equalityColumnIndex) : null;
      return new Page<>(items, next);
   }

   @NotNull
   private static Bson buildSort(@NotNull IndexSpec spec) {
      Document sort = new Document();

      for (String f : spec.fields()) {
         sort.append(stripDir(f), f.startsWith("-") ? -1 : 1);
      }

      sort.append("_id", 1);
      return sort;
   }

   private static int orderedColumnIndex(@NotNull IndexSpec spec, int equalityColumnIndex) {
      int next = equalityColumnIndex + 1;
      return next < spec.fields().size() ? next : -1;
   }

   @NotNull
   private static Cursor encodeCursor(@NotNull Document last, @NotNull IndexSpec spec, int equalityColumnIndex) {
      int orderedIdx = orderedColumnIndex(spec, equalityColumnIndex);
      byte[] idBytes = idAsBytes(last.get("_id"));
      if (orderedIdx < 0) {
         return Cursors.encode(0L, idBytes);
      } else {
         String orderedField = stripDir(spec.fields().get(orderedIdx));
         Object orderedRaw = last.get(orderedField);
         if (orderedRaw instanceof Long || orderedRaw instanceof Integer || orderedRaw instanceof Short || orderedRaw instanceof Byte) {
            return Cursors.encode(((Number)orderedRaw).longValue(), idBytes);
         } else if (orderedRaw instanceof Double || orderedRaw instanceof Float) {
            double d = ((Number)orderedRaw).doubleValue();
            byte[] payload = new byte[8];
            ByteBuffer.wrap(payload).putDouble(d);
            return Cursors.encodeTyped((byte)3, payload, idBytes);
         } else if (orderedRaw instanceof Date date) {
            byte[] payload = new byte[8];
            ByteBuffer.wrap(payload).putLong(date.getTime());
            return Cursors.encodeTyped((byte)4, payload, idBytes);
         } else if (orderedRaw instanceof String s) {
            return Cursors.encodeTyped((byte)1, s.getBytes(StandardCharsets.UTF_8), idBytes);
         } else if (orderedRaw instanceof byte[] b) {
            return Cursors.encodeTyped((byte)2, packBinary(BsonBinarySubType.BINARY.getValue(), b), idBytes);
         } else if (orderedRaw instanceof Binary b) {
            return Cursors.encodeTyped((byte)2, packBinary(b.getType(), b.getData()), idBytes);
         } else {
            return orderedRaw instanceof BsonBinary b
               ? Cursors.encodeTyped((byte)2, packBinary(b.getType(), b.getData()), idBytes)
               : Cursors.encodeTyped((byte)1, String.valueOf(orderedRaw).getBytes(StandardCharsets.UTF_8), idBytes);
         }
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
   private Bson applyCursor(@NotNull Bson base, @NotNull IndexSpec spec, int equalityColumnIndex, @Nullable Cursor cursor) {
      if (cursor == null) {
         return base;
      } else {
         int orderedIdx = orderedColumnIndex(spec, equalityColumnIndex);
         byte schema = Cursors.peekSchema(cursor);
         if (schema == 1) {
            return applyNumericCursor(base, spec, orderedIdx, Cursors.decode(cursor));
         } else if (schema == 2) {
            return applyTypedCursor(base, spec, orderedIdx, Cursors.decodeTyped(cursor));
         } else {
            throw new IllegalArgumentException("unsupported cursor schema 0x" + Integer.toHexString(schema & 255));
         }
      }
   }

   @NotNull
   private static Bson applyNumericCursor(@NotNull Bson base, @NotNull IndexSpec spec, int orderedIdx, @NotNull Cursors.Decoded d) {
      BsonBinary idBin = wrapBinaryIfUuid(d.idBytes());
      if (orderedIdx < 0) {
         return Filters.and(new Bson[]{base, Filters.gt("_id", idBin)});
      } else {
         String orderedField = stripDir(spec.fields().get(orderedIdx));
         boolean descending = spec.fields().get(orderedIdx).startsWith("-");
         Bson orderedPast = descending ? Filters.lt(orderedField, d.orderedKey()) : Filters.gt(orderedField, d.orderedKey());
         Bson tiedThenIdPast = Filters.and(new Bson[]{Filters.eq(orderedField, d.orderedKey()), Filters.gt("_id", idBin)});
         return Filters.and(new Bson[]{base, Filters.or(new Bson[]{orderedPast, tiedThenIdPast})});
      }
   }

   @NotNull
   private static Bson applyTypedCursor(@NotNull Bson base, @NotNull IndexSpec spec, int orderedIdx, @NotNull Cursors.DecodedTyped d) {
      BsonBinary idBin = wrapBinaryIfUuid(d.idBytes());
      if (orderedIdx < 0) {
         return Filters.and(new Bson[]{base, Filters.gt("_id", idBin)});
      } else {
         String orderedField = stripDir(spec.fields().get(orderedIdx));
         boolean descending = spec.fields().get(orderedIdx).startsWith("-");
         Object orderedValue = decodeTypedOrderedValue(d.typeTag(), d.orderedBytes());
         Bson orderedPast = descending ? Filters.lt(orderedField, orderedValue) : Filters.gt(orderedField, orderedValue);
         Bson tiedThenIdPast = Filters.and(new Bson[]{Filters.eq(orderedField, orderedValue), Filters.gt("_id", idBin)});
         return Filters.and(new Bson[]{base, Filters.or(new Bson[]{orderedPast, tiedThenIdPast})});
      }
   }

   @NotNull
   private static Object decodeTypedOrderedValue(byte typeTag, @NotNull byte[] bytes) {
      return switch (typeTag) {
         case 1 -> new String(bytes, StandardCharsets.UTF_8);
         case 2 -> {
            if (bytes.length < 1) {
               throw new IllegalArgumentException("BINARY typed cursor payload missing subtype byte");
            }

            byte subtype = bytes[0];
            byte[] data = new byte[bytes.length - 1];
            System.arraycopy(bytes, 1, data, 0, data.length);
            yield new BsonBinary(subtype, data);
         }
         case 3 -> {
            if (bytes.length != 8) {
               throw new IllegalArgumentException("DOUBLE typed cursor payload must be 8 bytes, got " + bytes.length);
            }

            yield ByteBuffer.wrap(bytes).getDouble();
         }
         case 4 -> {
            if (bytes.length != 8) {
               throw new IllegalArgumentException("DATE typed cursor payload must be 8 bytes, got " + bytes.length);
            }

            yield new Date(ByteBuffer.wrap(bytes).getLong());
         }
         default -> throw new IllegalArgumentException("unsupported typed-cursor type tag 0x" + Integer.toHexString(typeTag & 255));
      };
   }

   @NotNull
   private static Object encodeIdValue(@NotNull Object id) {
      if (id instanceof UUID u) {
         return BsonBinaries.uuidBinary(u);
      } else if (id instanceof Enum<?> e) {
         return e.ordinal();
      } else {
         return id instanceof byte[] b ? new BsonBinary(b) : id;
      }
   }

   @NotNull
   private static Object encodeIndexValue(@NotNull Object v) {
      if (v instanceof UUID u) {
         return BsonBinaries.uuidBinary(u);
      } else if (v instanceof Enum<?> e) {
         return e.ordinal();
      } else {
         return v instanceof byte[] b ? new BsonBinary(b) : v;
      }
   }

   @NotNull
   private static BsonBinary wrapBinaryIfUuid(@NotNull byte[] idBytes) {
      return idBytes.length == 16 ? new BsonBinary(BsonBinarySubType.UUID_STANDARD, idBytes) : new BsonBinary(idBytes);
   }

   @NotNull
   private static byte[] idAsBytes(@NotNull Object raw) {
      if (raw instanceof byte[] b) {
         return b;
      } else if (raw instanceof Binary b) {
         return b.getData();
      } else {
         return raw instanceof BsonBinary b ? b.getData() : raw.toString().getBytes(StandardCharsets.UTF_8);
      }
   }

   @NotNull
   private MongoCollection<Document> docColl(@NotNull StoreId id) {
      return this.docCollections.computeIfAbsent(id.qualified(), k -> this.db.getCollection(id.name()));
   }

   @NotNull
   private MongoCollection<RawBsonDocument> rawCollection(@NotNull StoreId id) {
      return this.rawCollections.computeIfAbsent(id.qualified(), k -> this.db.getCollection(id.name(), RawBsonDocument.class));
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

   private final class EntityHandler<E> implements StorageEventHandler<E> {
      @NotNull
      private final StoreId id;
      @NotNull
      private final MongoCollection<RawBsonDocument> coll;
      @NotNull
      private final MongoCollection<Document> docColl;
      @NotNull
      private final Function<E, Object> eventToRecord;
      @NotNull
      private final BsonCodec codec;
      @NotNull
      private final EncodeShape shape;
      @NotNull
      private final Function<Object, Object> idExtractor;
      @NotNull
      private final String idField;
      @NotNull
      private final MongoEntityAdapter.LowerCompanion[] lowerCompanions;
      @NotNull
      private final MongoEntityAdapter.UpsertPlan upsertPlan;

      EntityHandler(@NotNull StoreId id, @NotNull Entity<?, E, ?> kind, @NotNull MongoCollection<RawBsonDocument> coll) {
         this.id = id;
         this.coll = coll;
         this.docColl = MongoEntityAdapter.this.docColl(id);
         this.eventToRecord = (Function<E, Object>)kind.eventToRecord();
         this.codec = BsonCodecs.regular(kind.recordType());
         this.shape = kind.codec().shape();
         this.idExtractor = (Function<Object, Object>)kind.idOf();
         this.idField = this.shape.idField();
         this.lowerCompanions = MongoEntityAdapter.buildLowerCompanions(kind);
         this.upsertPlan = MongoEntityAdapter.UpsertPlan.of(this.shape);
      }

      @Override
      public void onEvent(E event, long sequence, boolean endOfBatch) throws BackendException {
         try {
            Object record = this.eventToRecord.apply(event);
            this.upsertRecord(record);
         } catch (RuntimeException var6) {
            throw new BackendException("entity upsert failed for " + this.id, var6);
         }
      }

      private void upsertRecord(@NotNull Object record) {
         Object idValue = this.idExtractor.apply(record);
         this.upsertPlan.write(this, record, idValue);
      }

      private void writeRawBson(@NotNull Object record, @NotNull Object idValue) {
         MongoBsonBuffer pool = MongoEntityAdapter.BSON_BUFFER.get();
         BsonBinaryWriter writer = pool.writer();

         try {
            if (this.lowerCompanions.length == 0) {
               this.codec.encode(record, writer);
            } else {
               writer.writeStartDocument();
               this.codec.encodeFields(record, writer);

               for (MongoEntityAdapter.LowerCompanion lc : this.lowerCompanions) {
                  if (this.codec.readField(record, lc.fieldIndex) instanceof String s) {
                     writer.writeString(lc.columnName, s.toLowerCase(Locale.ROOT));
                  }
               }

               writer.writeEndDocument();
            }
         } finally {
            writer.close();
         }

         RawBsonDocument var14 = pool.snapshot();
         this.coll
            .updateOne(
               Filters.eq("_id", MongoEntityAdapter.encodeIdValue(idValue)),
               new Document("$set", var14).append("$setOnInsert", new Document("_id", MongoEntityAdapter.encodeIdValue(idValue))),
               new UpdateOptions().upsert(true)
            );
      }

      private void writePipeline(@NotNull Object record, @NotNull Object idValue) {
         Document overwriteDoc = new Document();
         Document mergeDoc = new Document();
         overwriteDoc.append("_v", this.codec.version());
         int n = this.shape.fields().size();

         for (int i = 0; i < n; i++) {
            EncodeShape.FieldDef f = this.shape.fields().get(i);
            Object raw = this.codec.readField(record, i);
            Object bsonVal = MongoEntityAdapter.toBsonValue(raw, f.javaType());
            switch (f.mergeMode()) {
               case OVERWRITE:
                  if (raw != null || !f.nullable()) {
                     overwriteDoc.append(f.name(), MongoEntityAdapter.literal(bsonVal));
                  }
                  break;
               case PRESERVE_ON_NON_NULL:
                  mergeDoc.append(f.name(), new Document("$ifNull", Arrays.asList("$" + f.name(), MongoEntityAdapter.literal(bsonVal))));
                  break;
               case PRESERVE_ON_NON_SENTINEL:
                  Document preserveSentinel = new Document(
                     "$cond",
                     Arrays.asList(
                        new Document(
                           "$or",
                           Arrays.asList(
                              new Document("$in", Arrays.asList(new Document("$type", "$" + f.name()), Arrays.asList("missing", "null"))),
                              new Document("$eq", Arrays.asList("$" + f.name(), MongoEntityAdapter.literal(f.sentinelValue())))
                           )
                        ),
                        MongoEntityAdapter.literal(bsonVal),
                        "$" + f.name()
                     )
                  );
                  mergeDoc.append(f.name(), preserveSentinel);
                  break;
               case INSERT_ONLY:
                  Document insertOnly = new Document(
                     "$cond",
                     Arrays.asList(
                        new Document("$eq", Arrays.asList(new Document("$type", "$" + f.name()), "missing")),
                        MongoEntityAdapter.literal(bsonVal),
                        "$" + f.name()
                     )
                  );
                  mergeDoc.append(f.name(), insertOnly);
                  break;
               case MAX:
                  mergeDoc.append(f.name(), new Document("$max", Arrays.asList("$" + f.name(), MongoEntityAdapter.literal(bsonVal))));
                  break;
               case MIN:
                  mergeDoc.append(f.name(), new Document("$min", Arrays.asList("$" + f.name(), MongoEntityAdapter.literal(bsonVal))));
            }
         }

         for (MongoEntityAdapter.LowerCompanion lc : this.lowerCompanions) {
            if (this.codec.readField(record, lc.fieldIndex) instanceof String s) {
               overwriteDoc.append(lc.columnName, MongoEntityAdapter.literal(s.toLowerCase(Locale.ROOT)));
            }
         }

         List<Bson> pipeline = new ArrayList<>(2);
         if (!overwriteDoc.isEmpty()) {
            pipeline.add(new Document("$set", overwriteDoc));
         }

         if (!mergeDoc.isEmpty()) {
            pipeline.add(new Document("$set", mergeDoc));
         }

         this.docColl.updateOne(Filters.eq("_id", MongoEntityAdapter.encodeIdValue(idValue)), pipeline, new UpdateOptions().upsert(true));
      }
   }

   private record LowerCompanion(int fieldIndex, @NotNull String columnName) {
   }

   private record UpsertPlan(@NotNull MongoEntityAdapter.UpsertWriter writer) {
      @NotNull
      private static final MongoEntityAdapter.UpsertPlan RAW_BSON = new MongoEntityAdapter.UpsertPlan(
         (handler, record, idValue) -> handler.writeRawBson(record, idValue)
      );
      @NotNull
      private static final MongoEntityAdapter.UpsertPlan PIPELINE = new MongoEntityAdapter.UpsertPlan(
         (handler, record, idValue) -> handler.writePipeline(record, idValue)
      );

      @NotNull
      private static MongoEntityAdapter.UpsertPlan of(@NotNull EncodeShape shape) {
         for (EncodeShape.FieldDef field : shape.fields()) {
            if (field.mergeMode() != MergeMode.OVERWRITE) {
               return PIPELINE;
            }
         }

         return RAW_BSON;
      }

      private void write(@NotNull MongoEntityAdapter.EntityHandler<?> handler, @NotNull Object record, @NotNull Object idValue) {
         this.writer.write(handler, record, idValue);
      }
   }

   @FunctionalInterface
   private interface UpsertWriter {
      void write(@NotNull MongoEntityAdapter.EntityHandler<?> var1, @NotNull Object var2, @NotNull Object var3);
   }
}
