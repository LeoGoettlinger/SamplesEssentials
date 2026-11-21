package ac.grim.grimac.internal.storage.backend.redis.v2;

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
import ac.grim.grimac.api.storage.query.Page;
import ac.grim.grimac.api.storage.registry.Migration;
import ac.grim.grimac.api.storage.registry.StoreId;
import ac.grim.grimac.internal.storage.codec.bson.BsonCodec;
import ac.grim.grimac.internal.storage.codec.bson.BsonCodecs;
import ac.grim.grimac.shaded.jetbrains.annotations.ApiStatus;
import ac.grim.grimac.shaded.jetbrains.annotations.NotNull;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.logging.Logger;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.resps.Tuple;

@ApiStatus.Internal
public final class RedisEventStreamAdapter implements KindAdapter<EventStream<?, ?>> {
   @NotNull
   private final JedisPool pool;
   @NotNull
   private final String keyPrefix;
   @NotNull
   private final Logger logger;

   public RedisEventStreamAdapter(@NotNull JedisPool pool, @NotNull String keyPrefix, @NotNull Logger logger) {
      this.pool = pool;
      this.keyPrefix = keyPrefix;
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

   public void ensureStore(@NotNull StoreId id, @NotNull EventStream<?, ?> kind) {
   }

   public void dropStore(@NotNull StoreId id, @NotNull EventStream<?, ?> kind) {
   }

   @NotNull
   public <E> StorageEventHandler<E> writeHandler(@NotNull StoreId id, @NotNull EventStream<?, ?> kind, @NotNull Category<E> category) {
      BsonCodec codec = BsonCodecs.regular(kind.recordType());
      EncodeShape shape = kind.codec().shape();
      String storePrefix = this.keyPrefix + id.name() + ":";
      int idIdx = idFieldIndex(shape);
      int tsIdx = tsFieldIndex(shape, kind.timestampField());
      List<String> partitions = kind.partitionFields();
      return (event, sequence, endOfBatch) -> {
         Object record = ((Function<E, ?>)kind.eventToRecord()).apply(event);
         Object idVal = codec.readField(record, idIdx);
         String eventId = RedisKeys.encode(idVal);
         String hashKey = storePrefix + eventId;
         Map<byte[], byte[]> fields = new LinkedHashMap<>();

         for (int i = 0; i < shape.fields().size(); i++) {
            Object v = codec.readField(record, i);
            if (v != null) {
               EncodeShape.FieldDef f = shape.fields().get(i);
               fields.put(bytes(f.name()), fieldValueBytes(f, v));
            }
         }

         double score = codec.readField(record, tsIdx) instanceof Number n ? n.doubleValue() : 0.0;
         Jedis j = this.pool.getResource();

         try {
            j.hset(bytes(hashKey), fields);

            for (String part : partitions) {
               int partIdx = fieldIndex(shape, part);
               Object partVal = codec.readField(record, partIdx);
               if (partVal != null) {
                  String indexKey = storePrefix + "by-" + part + ":" + RedisKeys.encode(partVal);
                  j.zadd(indexKey, score, eventId);
               }
            }
         } catch (Throwable var27) {
            if (j != null) {
               try {
                  j.close();
               } catch (Throwable var26) {
                  var27.addSuppressed(var26);
               }
            }

            throw var27;
         }

         if (j != null) {
            j.close();
         }
      };
   }

   public <R> R execute(@NotNull StoreId id, @NotNull EventStream<?, ?> kind, @NotNull Operation<R> op) throws BackendException {
      String storePrefix = this.keyPrefix + id.name() + ":";

      try {
         if (op instanceof EventStreamOps.CountOp c) {
            return (R)this.count(storePrefix, c);
         } else if (op instanceof EventStreamOps.DeleteByPartitionOp d) {
            this.deleteByPartition(storePrefix, kind, d);
            return null;
         } else if (op instanceof EventStreamOps.DeleteOlderThanOp d) {
            this.deleteOlderThan(storePrefix, kind, d);
            return null;
         } else if (op instanceof EventStreamOps.CountDistinctOp c) {
            return (R)0L;
         } else if (op instanceof EventStreamOps.PageOp<?> p) {
            return (R)this.page(storePrefix, kind, p);
         } else {
            throw new UnsupportedOperationException("RedisEventStreamAdapter: " + op.getClass().getName());
         }
      } catch (RuntimeException var6) {
         throw new BackendException("redis event stream failed", var6);
      }
   }

   @NotNull
   public List<Migration<EventStream<?, ?>>> migrations(@NotNull EventStream<?, ?> kind) {
      return List.of();
   }

   private long count(String prefix, EventStreamOps.CountOp op) {
      String indexKey = prefix + "by-" + op.partition() + ":" + RedisKeys.encode(op.key());
      Jedis j = this.pool.getResource();

      long var5;
      try {
         var5 = j.zcard(indexKey);
      } catch (Throwable var8) {
         if (j != null) {
            try {
               j.close();
            } catch (Throwable var7) {
               var8.addSuppressed(var7);
            }
         }

         throw var8;
      }

      if (j != null) {
         j.close();
      }

      return var5;
   }

   private <R> Page<R> page(String prefix, EventStream<?, ?> kind, EventStreamOps.PageOp<R> op) {
      String indexKey = prefix + "by-" + op.partition() + ":" + RedisKeys.encode(op.key());
      BsonCodec codec = BsonCodecs.regular(kind.recordType());
      EncodeShape shape = kind.codec().shape();
      Jedis j = this.pool.getResource();

      Page var20;
      try {
         List<Tuple> members = j.zrangeWithScores(indexKey, 0L, op.pageSize());
         List<R> items = new ArrayList<>(members.size());

         for (Tuple t : members) {
            String eventId = t.getElement();
            Map<byte[], byte[]> hash = j.hgetAll(bytes(prefix + eventId));
            if (hash != null && !hash.isEmpty()) {
               Object[] args = new Object[shape.fields().size()];

               for (int i = 0; i < shape.fields().size(); i++) {
                  byte[] raw = RedisEntityAdapter.hashGet(hash, shape.fields().get(i).name());
                  args[i] = raw == null ? null : RedisEntityAdapter.parseFieldBytesStatic(shape.fields().get(i), raw);
               }

               items.add((R)codec.decodeFromValues(args));
            }
         }

         Cursor nextCursor = members.size() > op.pageSize() ? new Cursor("next") : null;
         var20 = new Page<>(items, nextCursor);
      } catch (Throwable var18) {
         if (j != null) {
            try {
               j.close();
            } catch (Throwable var17) {
               var18.addSuppressed(var17);
            }
         }

         throw var18;
      }

      if (j != null) {
         j.close();
      }

      return var20;
   }

   private void deleteByPartition(String prefix, EventStream<?, ?> kind, EventStreamOps.DeleteByPartitionOp op) {
      String indexKey = prefix + "by-" + op.partition() + ":" + RedisKeys.encode(op.key());
      Jedis j = this.pool.getResource();

      try {
         for (String eventId : j.zrange(indexKey, 0L, -1L)) {
            j.del(bytes(prefix + eventId));
         }

         j.del(indexKey);
      } catch (Throwable var10) {
         if (j != null) {
            try {
               j.close();
            } catch (Throwable var9) {
               var10.addSuppressed(var9);
            }
         }

         throw var10;
      }

      if (j != null) {
         j.close();
      }
   }

   private void deleteOlderThan(String prefix, EventStream<?, ?> kind, EventStreamOps.DeleteOlderThanOp op) {
   }

   private static int idFieldIndex(EncodeShape shape) {
      for (int i = 0; i < shape.fields().size(); i++) {
         if (shape.fields().get(i).name().equals(shape.idField())) {
            return i;
         }
      }

      return 0;
   }

   private static int tsFieldIndex(EncodeShape shape, String tsField) {
      for (int i = 0; i < shape.fields().size(); i++) {
         if (shape.fields().get(i).name().equals(tsField)) {
            return i;
         }
      }

      return 0;
   }

   private static int fieldIndex(EncodeShape shape, String name) {
      for (int i = 0; i < shape.fields().size(); i++) {
         if (shape.fields().get(i).name().equals(name)) {
            return i;
         }
      }

      return 0;
   }

   private static byte[] fieldValueBytes(@NotNull EncodeShape.FieldDef f, @NotNull Object value) {
      return f.javaType() == byte[].class ? (byte[])value : bytes(RedisKeys.encode(value));
   }

   private static byte[] bytes(@NotNull String s) {
      return s.getBytes(StandardCharsets.UTF_8);
   }
}
