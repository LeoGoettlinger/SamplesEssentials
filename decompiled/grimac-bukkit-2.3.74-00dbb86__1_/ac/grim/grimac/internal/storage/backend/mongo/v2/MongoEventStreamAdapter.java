package ac.grim.grimac.internal.storage.backend.mongo.v2;

import ac.grim.grimac.api.storage.backend.BackendException;
import ac.grim.grimac.api.storage.backend.KindAdapter;
import ac.grim.grimac.api.storage.backend.StorageEventHandler;
import ac.grim.grimac.api.storage.category.Capability;
import ac.grim.grimac.api.storage.category.Category;
import ac.grim.grimac.api.storage.kind.EventStream;
import ac.grim.grimac.api.storage.kind.Granularity;
import ac.grim.grimac.api.storage.kind.Operation;
import ac.grim.grimac.api.storage.kind.ops.EventStreamOps;
import ac.grim.grimac.api.storage.query.Cursor;
import ac.grim.grimac.api.storage.query.Cursors;
import ac.grim.grimac.api.storage.query.Page;
import ac.grim.grimac.api.storage.registry.Migration;
import ac.grim.grimac.api.storage.registry.StoreId;
import ac.grim.grimac.internal.storage.codec.bson.BsonBinaries;
import ac.grim.grimac.internal.storage.codec.bson.BsonCodecs;
import ac.grim.grimac.internal.storage.codec.bson.BsonTsCodec;
import ac.grim.grimac.shaded.jetbrains.annotations.ApiStatus;
import ac.grim.grimac.shaded.jetbrains.annotations.NotNull;
import ac.grim.grimac.shaded.jetbrains.annotations.Nullable;
import com.mongodb.MongoCommandException;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Accumulators;
import com.mongodb.client.model.Aggregates;
import com.mongodb.client.model.BsonField;
import com.mongodb.client.model.CreateCollectionOptions;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Indexes;
import com.mongodb.client.model.TimeSeriesGranularity;
import com.mongodb.client.model.TimeSeriesOptions;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.logging.Logger;
import org.bson.BsonBinary;
import org.bson.BsonBinarySubType;
import org.bson.BsonBinaryWriter;
import org.bson.Document;
import org.bson.RawBsonDocument;
import org.bson.conversions.Bson;
import org.bson.types.Binary;

@ApiStatus.Internal
public final class MongoEventStreamAdapter implements KindAdapter<EventStream<?, ?>> {
   private static final int NAMESPACE_EXISTS = 48;
   @NotNull
   private final MongoDatabase db;
   @NotNull
   private final Logger logger;
   private final int batchFlushCap;
   private final Map<String, MongoCollection<Document>> docCollections = new ConcurrentHashMap<>();
   private final Map<String, MongoCollection<RawBsonDocument>> rawCollections = new ConcurrentHashMap<>();
   private static final ThreadLocal<MongoBsonBuffer> BSON_BUFFER = ThreadLocal.withInitial(MongoBsonBuffer::new);

   public MongoEventStreamAdapter(@NotNull MongoDatabase db, @NotNull Logger logger, int batchFlushCap) {
      this.db = db;
      this.logger = logger;
      this.batchFlushCap = batchFlushCap > 0 ? batchFlushCap : 256;
   }

   @NotNull
   @Override
   public Class<EventStream<?, ?>> kindType() {
      return EventStream.class;
   }

   @NotNull
   @Override
   public EnumSet<Capability> subcapabilities() {
      return EnumSet.of(
         Capability.EVENT_STREAM_TIMESERIES_NATIVE, Capability.EVENT_STREAM_TTL_NATIVE, Capability.EVENT_STREAM_RANGE_BY_TIME, Capability.BINARY_UUID_KEYS
      );
   }

   public void ensureStore(@NotNull StoreId id, @NotNull EventStream<?, ?> kind) throws BackendException {
      String coll = id.name();

      try {
         if (!this.collectionExists(coll)) {
            CreateCollectionOptions opts = new CreateCollectionOptions().timeSeriesOptions(this.buildTimeSeriesOptions(kind));
            if (kind.retention() != null) {
               opts.expireAfter(kind.retention().toSeconds(), TimeUnit.SECONDS);
            }

            try {
               this.db.createCollection(coll, opts);
               this.logger.info(() -> "created timeseries collection " + coll + " (meta=" + kind.partitionFields() + ", time=" + kind.timestampField() + ")");
            } catch (MongoCommandException var6) {
               if (var6.getErrorCode() != 48) {
                  throw var6;
               }

               this.logger.fine(() -> "timeseries collection " + coll + " was created concurrently by another instance; continuing");
            }
         }

         this.ensureIndexes(coll, kind);
      } catch (RuntimeException var7) {
         throw new BackendException("failed to ensure timeseries collection " + coll, var7);
      }
   }

   public void dropStore(@NotNull StoreId id, @NotNull EventStream<?, ?> kind) throws BackendException {
      try {
         this.db.getCollection(id.name()).drop();
         this.docCollections.remove(id.qualified());
         this.rawCollections.remove(id.qualified());
      } catch (RuntimeException var4) {
         throw new BackendException("failed to drop " + id, var4);
      }
   }

   @NotNull
   public <E> StorageEventHandler<E> writeHandler(@NotNull StoreId id, @NotNull EventStream<?, ?> kind, @NotNull Category<E> category) {
      return new MongoEventStreamAdapter.EventStreamHandler<>(id, (EventStream<E, ?>)kind, this.rawCollection(id));
   }

   public <R> R execute(@NotNull StoreId id, @NotNull EventStream<?, ?> kind, @NotNull Operation<R> op) throws BackendException {
      try {
         if (op instanceof EventStreamOps.PageOp<?> p) {
            return (R)this.page(id, kind, p);
         } else if (op instanceof EventStreamOps.RangeByTimeOp<?> r) {
            return (R)this.rangeByTime(id, kind, r);
         } else if (op instanceof EventStreamOps.CountOp c) {
            return (R)this.count(id, c);
         } else if (op instanceof EventStreamOps.CountManyOp<?> c) {
            return (R)this.countMany(id, c);
         } else if (op instanceof EventStreamOps.CountDistinctOp c) {
            return (R)this.countDistinct(id, c);
         } else if (op instanceof EventStreamOps.DeleteByPartitionOp d) {
            this.deleteByPartition(id, d);
            return null;
         } else if (op instanceof EventStreamOps.DeleteOlderThanOp d) {
            this.deleteOlderThan(id, kind, d);
            return null;
         } else {
            throw new UnsupportedOperationException("MongoEventStreamAdapter does not handle " + op.getClass().getName());
         }
      } catch (RuntimeException var5) {
         throw new BackendException("mongo execute failed for " + op.getClass().getSimpleName() + " on " + id, var5);
      }
   }

   @NotNull
   public List<Migration<EventStream<?, ?>>> migrations(@NotNull EventStream<?, ?> kind) {
      return List.of(new MongoViolationsV6ToV7Migration());
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

   @NotNull
   private TimeSeriesOptions buildTimeSeriesOptions(@NotNull EventStream<?, ?> kind) {
      TimeSeriesOptions opts = new TimeSeriesOptions(kind.timestampField()).metaField("meta");
      opts.granularity(this.toMongoGranularity(kind.granularity()));
      return opts;
   }

   @NotNull
   private TimeSeriesGranularity toMongoGranularity(@NotNull Granularity g) {
      return switch (g) {
         case SECONDS -> TimeSeriesGranularity.SECONDS;
         case MINUTES -> TimeSeriesGranularity.MINUTES;
         case HOURS -> TimeSeriesGranularity.HOURS;
      };
   }

   private void ensureIndexes(@NotNull String coll, @NotNull EventStream<?, ?> kind) {
      MongoCollection<Document> c = this.db.getCollection(coll);
      c.createIndex(Indexes.ascending(new String[]{kind.timestampField()}));

      for (String partition : kind.partitionFields()) {
         c.createIndex(Indexes.ascending(new String[]{"meta." + partition}));
         c.createIndex(
            Indexes.compoundIndex(new Bson[]{Indexes.ascending(new String[]{"meta." + partition}), Indexes.ascending(new String[]{kind.timestampField()})})
         );
      }
   }

   private <R> Page<R> page(@NotNull StoreId id, @NotNull EventStream<?, ?> kind, @NotNull EventStreamOps.PageOp<?> p) {
      Bson partitionFilter = this.partitionEq(p.partition(), p.key());
      Bson filter = this.applyCursor(partitionFilter, kind, p.cursor(), p.partition());
      int pageSize = Math.max(1, p.pageSize());
      List<Document> docs = new ArrayList<>(pageSize + 1);
      this.rawDocColl(id).find(filter).sort(new Document(kind.timestampField(), 1).append("id", 1)).limit(pageSize + 1).into(docs);
      return this.materialise(kind, docs, pageSize);
   }

   private <R> Page<R> rangeByTime(@NotNull StoreId id, @NotNull EventStream<?, ?> kind, @NotNull EventStreamOps.RangeByTimeOp<?> r) {
      Bson timeFilter = Filters.and(
         new Bson[]{Filters.gte(kind.timestampField(), new Date(r.fromEpochMs())), Filters.lt(kind.timestampField(), new Date(r.toEpochMs()))}
      );
      Bson filter = this.applyCursor(timeFilter, kind, r.cursor(), null);
      int pageSize = Math.max(1, r.pageSize());
      List<Document> docs = new ArrayList<>(pageSize + 1);
      this.rawDocColl(id).find(filter).sort(new Document(kind.timestampField(), 1).append("id", 1)).limit(pageSize + 1).into(docs);
      return this.materialise(kind, docs, pageSize);
   }

   private long count(@NotNull StoreId id, @NotNull EventStreamOps.CountOp op) {
      return this.rawDocColl(id).countDocuments(this.partitionEq(op.partition(), op.key()));
   }

   private <K> Map<K, Long> countMany(@NotNull StoreId id, @NotNull EventStreamOps.CountManyOp<K> op) {
      Collection<K> keys = op.keys();
      if (keys.isEmpty()) {
         return Collections.emptyMap();
      } else {
         List<Object> encodedKeys = new ArrayList<>(keys.size());

         for (K k : keys) {
            encodedKeys.add(this.encodePartitionValue(k));
         }

         List<Bson> pipeline = List.of(
            Aggregates.match(Filters.in("meta." + op.partition(), encodedKeys)), Aggregates.group("$meta." + op.partition(), List.of(Accumulators.sum("n", 1)))
         );
         Map<K, Long> out = new HashMap<>(keys.size() * 2);

         for (K k : keys) {
            out.put(k, 0L);
         }

         MongoCursor var16 = this.rawDocColl(id).aggregate(pipeline).iterator();

         while (var16.hasNext()) {
            Document d = (Document)var16.next();
            Object raw = d.get("_id");
            Object decoded = this.decodePartitionValue(raw, sampleKey(keys));
            long n = ((Number)d.get("n")).longValue();
            out.put((K)decoded, n);
         }

         return out;
      }
   }

   private long countDistinct(@NotNull StoreId id, @NotNull EventStreamOps.CountDistinctOp op) {
      List<Bson> pipeline = List.of(
         Aggregates.match(this.partitionEq(op.partition(), op.key())), Aggregates.group("$meta." + op.field(), new BsonField[0]), Aggregates.count("n")
      );
      Document first = (Document)this.rawDocColl(id).aggregate(pipeline).first();
      return first == null ? 0L : first.getInteger("n", 0);
   }

   private void deleteByPartition(@NotNull StoreId id, @NotNull EventStreamOps.DeleteByPartitionOp op) {
      this.rawDocColl(id).deleteMany(this.partitionEq(op.partition(), op.key()));
   }

   private void deleteOlderThan(@NotNull StoreId id, @NotNull EventStream<?, ?> kind, @NotNull EventStreamOps.DeleteOlderThanOp op) {
      this.rawDocColl(id).deleteMany(Filters.lt(kind.timestampField(), new Date(op.cutoffEpochMs())));
   }

   private <R> Page<R> materialise(@NotNull EventStream<?, ?> kind, @NotNull List<Document> docs, int pageSize) {
      BsonTsCodec codec = BsonCodecs.timeseries(kind.recordType());
      boolean hasMore = docs.size() > pageSize;
      List<R> items = new ArrayList<>(Math.min(docs.size(), pageSize));
      Document last = null;
      int n = Math.min(docs.size(), pageSize);

      for (int i = 0; i < n; i++) {
         Document d = docs.get(i);
         items.add((R)codec.decode(d));
         last = d;
      }

      Cursor cursor = hasMore && last != null ? this.encodeCursor(last, kind) : null;
      return new Page<>(items, cursor);
   }

   @NotNull
   private Cursor encodeCursor(@NotNull Document last, @NotNull EventStream<?, ?> kind) {
      Object ts = last.get(kind.timestampField());
      long tsMs = ts instanceof Date d ? d.getTime() : ((Number)ts).longValue();
      Object idRaw = last.get("id");
      byte[] idBytes;
      if (idRaw instanceof Binary b) {
         idBytes = b.getData();
      } else if (idRaw instanceof BsonBinary b) {
         idBytes = b.getData();
      } else {
         if (!(idRaw instanceof byte[] b)) {
            throw new IllegalStateException("unexpected id encoding: " + (idRaw == null ? "null" : idRaw.getClass().getName()));
         }

         idBytes = b;
      }

      return Cursors.encode(tsMs, idBytes);
   }

   @NotNull
   private Bson applyCursor(@NotNull Bson base, @NotNull EventStream<?, ?> kind, @Nullable Cursor cursor, @Nullable String partitionField) {
      if (cursor == null) {
         return base;
      } else {
         Cursors.Decoded d = Cursors.decode(cursor);
         BsonBinary idBinary = new BsonBinary(BsonBinarySubType.UUID_STANDARD, d.idBytes());
         Bson seek = Filters.or(
            new Bson[]{
               Filters.gt(kind.timestampField(), new Date(d.orderedKey())),
               Filters.and(new Bson[]{Filters.eq(kind.timestampField(), new Date(d.orderedKey())), Filters.gt("id", idBinary)})
            }
         );
         return Filters.and(new Bson[]{base, seek});
      }
   }

   @NotNull
   private Bson partitionEq(@NotNull String field, @NotNull Object key) {
      return Filters.eq("meta." + field, this.encodePartitionValue(key));
   }

   @NotNull
   private Object encodePartitionValue(@NotNull Object key) {
      if (key instanceof UUID u) {
         return BsonBinaries.uuidBinary(u);
      } else if (key instanceof Enum<?> e) {
         return e.ordinal();
      } else {
         return key instanceof byte[] b ? new BsonBinary(b) : key;
      }
   }

   @NotNull
   private Object decodePartitionValue(@NotNull Object raw, @NotNull Object sample) {
      if (sample instanceof UUID) {
         return BsonBinaries.toUuid(raw);
      } else if (sample instanceof Enum<?> e) {
         int ordinal = raw instanceof Number n ? n.intValue() : Integer.parseInt(raw.toString());
         return ((Enum[])e.getClass().getEnumConstants())[ordinal];
      } else {
         return raw;
      }
   }

   @NotNull
   private static <K> K sampleKey(@NotNull Collection<K> keys) {
      Iterator var1 = keys.iterator();
      if (var1.hasNext()) {
         return (K)var1.next();
      } else {
         throw new IllegalStateException("empty keys");
      }
   }

   @NotNull
   private MongoCollection<RawBsonDocument> rawCollection(@NotNull StoreId id) {
      return this.rawCollections.computeIfAbsent(id.qualified(), k -> this.db.getCollection(id.name(), RawBsonDocument.class));
   }

   @NotNull
   private MongoCollection<Document> rawDocColl(@NotNull StoreId id) {
      return this.docCollections.computeIfAbsent(id.qualified(), k -> this.db.getCollection(id.name()));
   }

   private final class EventStreamHandler<E> implements StorageEventHandler<E> {
      @NotNull
      private final StoreId id;
      @NotNull
      private final MongoCollection<RawBsonDocument> coll;
      @NotNull
      private final Function<E, Object> eventToRecord;
      @NotNull
      private final BsonTsCodec codec;
      @NotNull
      private final List<RawBsonDocument> pending = new ArrayList<>();
      private final int flushCap;

      EventStreamHandler(@NotNull StoreId id, @NotNull EventStream<E, ?> kind, @NotNull MongoCollection<RawBsonDocument> coll) {
         this.id = id;
         this.coll = coll;
         this.eventToRecord = (Function<E, Object>)kind.eventToRecord();
         this.codec = BsonCodecs.timeseries(kind.recordType());
         this.flushCap = MongoEventStreamAdapter.this.batchFlushCap;
      }

      @Override
      public void onEvent(E event, long sequence, boolean endOfBatch) throws BackendException {
         try {
            Object record = this.eventToRecord.apply(event);
            MongoBsonBuffer pool = MongoEventStreamAdapter.BSON_BUFFER.get();
            BsonBinaryWriter writer = pool.writer();

            try {
               this.codec.encode(record, writer);
            } finally {
               writer.close();
            }

            this.pending.add(pool.snapshot());
            if (endOfBatch || this.pending.size() >= this.flushCap) {
               this.flush();
            }
         } catch (RuntimeException var12) {
            throw new BackendException("encode failed for " + this.id, var12);
         }
      }

      private void flush() throws BackendException {
         if (!this.pending.isEmpty()) {
            try {
               this.coll.insertMany(this.pending);
            } catch (RuntimeException var5) {
               throw new BackendException("insert failed for " + this.id, var5);
            } finally {
               this.pending.clear();
            }
         }
      }
   }
}
