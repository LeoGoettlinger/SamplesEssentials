package ac.grim.grimac.internal.storage.backend.redis.v2;

import ac.grim.grimac.api.storage.backend.BackendException;
import ac.grim.grimac.api.storage.backend.KindAdapter;
import ac.grim.grimac.api.storage.backend.StorageEventHandler;
import ac.grim.grimac.api.storage.category.Capability;
import ac.grim.grimac.api.storage.category.Category;
import ac.grim.grimac.api.storage.codec.EncodeShape;
import ac.grim.grimac.api.storage.kind.Entity;
import ac.grim.grimac.api.storage.kind.IndexSpec;
import ac.grim.grimac.api.storage.kind.Operation;
import ac.grim.grimac.api.storage.kind.ops.EntityOps;
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
import java.util.Arrays;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.logging.Logger;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.params.ScanParams;
import redis.clients.jedis.resps.ScanResult;

@ApiStatus.Internal
public final class RedisEntityAdapter implements KindAdapter<Entity<?, ?, ?>> {
   @NotNull
   private final JedisPool pool;
   @NotNull
   private final String keyPrefix;
   @NotNull
   private final Logger logger;

   public RedisEntityAdapter(@NotNull JedisPool pool, @NotNull String keyPrefix, @NotNull Logger logger) {
      this.pool = pool;
      this.keyPrefix = keyPrefix;
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
      if (!kind.secondaryIndexes().isEmpty()) {
         String storePrefix = this.keyPrefix + id.name() + ":";

         try {
            Jedis j = this.pool.getResource();

            try {
               deleteIndexKeys(j, storePrefix);
               BsonCodec codec = BsonCodecs.regular(kind.recordType());
               EncodeShape shape = kind.codec().shape();
               String cursor = ScanParams.SCAN_POINTER_START;
               ScanParams params = new ScanParams().match(storePrefix + "*").count(1000);

               do {
                  ScanResult<String> result = j.scan(cursor, params);

                  for (String key : result.getResult()) {
                     if (!key.startsWith(storePrefix + "__idx:")) {
                        Map<byte[], byte[]> hash = j.hgetAll(bytes(key));
                        if (hash != null && !hash.isEmpty()) {
                           Object record = this.decodeHash(kind, hash);
                           Object idVal = codec.readField(record, idFieldIndex(shape));
                           this.addIndexEntries(j, storePrefix, kind, shape, codec, record, idVal);
                        }
                     }
                  }

                  cursor = result.getCursor();
               } while (!ScanParams.SCAN_POINTER_START.equals(cursor));
            } catch (Throwable var16) {
               if (j != null) {
                  try {
                     j.close();
                  } catch (Throwable var15) {
                     var16.addSuppressed(var15);
                  }
               }

               throw var16;
            }

            if (j != null) {
               j.close();
            }
         } catch (RuntimeException var17) {
            throw new BackendException("redis entity ensureStore failed for " + id.name(), var17);
         }
      }
   }

   public void dropStore(@NotNull StoreId id, @NotNull Entity<?, ?, ?> kind) {
   }

   @NotNull
   public <E> StorageEventHandler<E> writeHandler(@NotNull StoreId id, @NotNull Entity<?, ?, ?> kind, @NotNull Category<E> category) {
      String storePrefix = this.keyPrefix + id.name() + ":";
      return (event, sequence, endOfBatch) -> {
         Object record = ((Function<E, ?>)kind.eventToRecord()).apply(event);
         this.upsertRecord(storePrefix, kind, record);
      };
   }

   public <R> R execute(@NotNull StoreId id, @NotNull Entity<?, ?, ?> kind, @NotNull Operation<R> op) throws BackendException {
      String storePrefix = this.keyPrefix + id.name() + ":";

      try {
         if (op instanceof EntityOps.UpsertOp u) {
            this.upsertRecord(storePrefix, kind, u.record());
            return null;
         } else if (op instanceof EntityOps.GetByIdOp g) {
            return (R)this.getById(storePrefix, kind, g);
         } else if (op instanceof EntityOps.GetManyOp g) {
            return (R)this.getMany(storePrefix, kind, g);
         } else if (op instanceof EntityOps.FindByIndexOp f) {
            return (R)this.findByIndex(storePrefix, kind, f);
         } else if (op instanceof EntityOps.PrefixIndexOp p) {
            return (R)this.prefixIndex(storePrefix, kind, p);
         } else if (op instanceof EntityOps.DeleteByIdOp d) {
            this.deleteById(storePrefix, kind, d);
            return null;
         } else if (op instanceof EntityOps.DeleteByIndexOp d) {
            this.deleteByIndex(storePrefix, kind, d);
            return null;
         } else if (op instanceof EntityOps.CountByIndexOp c) {
            return (R)this.countByIndex(storePrefix, kind, c);
         } else {
            throw new UnsupportedOperationException("RedisEntityAdapter: " + op.getClass().getName());
         }
      } catch (RuntimeException var6) {
         throw new BackendException("redis entity failed", var6);
      }
   }

   @NotNull
   public List<Migration<Entity<?, ?, ?>>> migrations(@NotNull Entity<?, ?, ?> kind) {
      return List.of();
   }

   private void upsertRecord(@NotNull String storePrefix, @NotNull Entity<?, ?, ?> kind, @NotNull Object record) {
      if (!kind.recordType().isInstance(record)) {
         throw new IllegalArgumentException("record type " + record.getClass().getName() + " does not match entity " + kind.recordType().getName());
      } else {
         BsonCodec codec = BsonCodecs.regular(kind.recordType());
         EncodeShape shape = kind.codec().shape();
         Object idVal = codec.readField(record, idFieldIndex(shape));
         String redisKey = storePrefix + RedisKeys.encode(idVal);
         Map<byte[], byte[]> fields = new LinkedHashMap<>();

         for (int i = 0; i < shape.fields().size(); i++) {
            Object v = codec.readField(record, i);
            if (v != null) {
               EncodeShape.FieldDef f = shape.fields().get(i);
               fields.put(bytes(f.name()), fieldValueBytes(f, v));
            }
         }

         Jedis j = this.pool.getResource();

         try {
            Map<byte[], byte[]> existing = j.hgetAll(bytes(redisKey));
            if (existing != null && !existing.isEmpty()) {
               this.removeIndexEntries(j, storePrefix, kind, shape, existing);
            }

            j.hset(bytes(redisKey), fields);
            this.addIndexEntries(j, storePrefix, kind, shape, codec, record, idVal);
         } catch (Throwable var13) {
            if (j != null) {
               try {
                  j.close();
               } catch (Throwable var12) {
                  var13.addSuppressed(var12);
               }
            }

            throw var13;
         }

         if (j != null) {
            j.close();
         }
      }
   }

   private <ID, R> Optional<R> getById(String prefix, Entity<?, ?, ?> kind, EntityOps.GetByIdOp<ID, R> op) {
      String redisKey = prefix + RedisKeys.encode(op.id());
      Jedis j = this.pool.getResource();

      Optional var16;
      label60: {
         Optional codec;
         try {
            Map<byte[], byte[]> hash = j.hgetAll(bytes(redisKey));
            if (hash != null && !hash.isEmpty()) {
               BsonCodec codecx = BsonCodecs.regular(kind.recordType());
               EncodeShape shape = kind.codec().shape();
               Object[] args = new Object[shape.fields().size()];

               for (int i = 0; i < shape.fields().size(); i++) {
                  EncodeShape.FieldDef f = shape.fields().get(i);
                  byte[] raw = hashGet(hash, f.name());
                  args[i] = raw == null ? null : parseField(f, raw);
               }

               var16 = Optional.of(codecx.decodeFromValues(args));
               break label60;
            }

            codec = Optional.empty();
         } catch (Throwable var14) {
            if (j != null) {
               try {
                  j.close();
               } catch (Throwable var13) {
                  var14.addSuppressed(var13);
               }
            }

            throw var14;
         }

         if (j != null) {
            j.close();
         }

         return codec;
      }

      if (j != null) {
         j.close();
      }

      return var16;
   }

   private <ID, R> List<R> getMany(String prefix, Entity<?, ?, ?> kind, EntityOps.GetManyOp<ID, R> op) {
      List<R> out = new ArrayList<>(op.ids().size());

      for (ID id : op.ids()) {
         Optional<R> one = this.getById(prefix, kind, new EntityOps.GetByIdOp<>(op.category(), id));
         one.ifPresent(out::add);
      }

      return out;
   }

   private <R> Page<R> findByIndex(String prefix, Entity<?, ?, ?> kind, EntityOps.FindByIndexOp<R> op) {
      IndexSpec spec = requireIndex(kind, op.indexName());
      int offset = cursorOffset(op.cursor());
      int pageSize = Math.max(1, op.pageSize());
      String indexKey = exactIndexKey(prefix, spec, normalizedIndexValue(spec, op.key()));
      Jedis j = this.pool.getResource();

      Page var10;
      try {
         List<String> ids = j.zrange(indexKey, offset, offset + pageSize);
         var10 = this.pageFromIds(prefix, kind, ids, pageSize, offset);
      } catch (Throwable var12) {
         if (j != null) {
            try {
               j.close();
            } catch (Throwable var11) {
               var12.addSuppressed(var11);
            }
         }

         throw var12;
      }

      if (j != null) {
         j.close();
      }

      return var10;
   }

   private <R> Page<R> prefixIndex(String prefix, Entity<?, ?, ?> kind, EntityOps.PrefixIndexOp<R> op) {
      IndexSpec spec = requireIndex(kind, op.indexName());
      if (!spec.caseInsensitivePrefix()) {
         throw new UnsupportedOperationException("RedisEntityAdapter: PrefixIndexOp requires caseInsensitivePrefix index, got " + spec.name());
      } else {
         int offset = cursorOffset(op.cursor());
         int pageSize = Math.max(1, op.pageSize());
         String normalizedPrefix = op.prefix().toLowerCase(Locale.ROOT);
         String min = "[" + normalizedPrefix;
         String max = "[" + normalizedPrefix + "\uffff";
         Jedis j = this.pool.getResource();

         Page var17;
         try {
            List<String> members = j.zrangeByLex(lexIndexKey(prefix, spec), min, max, offset, pageSize + 1);
            List<String> ids = new ArrayList<>(members.size());

            for (String member : members) {
               ids.add(idFromLexMember(member));
            }

            var17 = this.pageFromIds(prefix, kind, ids, pageSize, offset);
         } catch (Throwable var16) {
            if (j != null) {
               try {
                  j.close();
               } catch (Throwable var15) {
                  var16.addSuppressed(var15);
               }
            }

            throw var16;
         }

         if (j != null) {
            j.close();
         }

         return var17;
      }
   }

   private long countByIndex(String prefix, Entity<?, ?, ?> kind, EntityOps.CountByIndexOp op) {
      IndexSpec spec = requireIndex(kind, op.indexName());
      String indexKey = exactIndexKey(prefix, spec, normalizedIndexValue(spec, op.key()));
      Jedis j = this.pool.getResource();

      long var7;
      try {
         var7 = j.zcard(indexKey);
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

      return var7;
   }

   private void deleteById(String prefix, Entity<?, ?, ?> kind, EntityOps.DeleteByIdOp<?> op) {
      EncodeShape shape = kind.codec().shape();
      String key = prefix + RedisKeys.encode(op.id());
      Jedis j = this.pool.getResource();

      try {
         Map<byte[], byte[]> existing = j.hgetAll(bytes(key));
         if (existing != null && !existing.isEmpty()) {
            this.removeIndexEntries(j, prefix, kind, shape, existing);
         }

         j.del(bytes(key));
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

   private void deleteByIndex(String prefix, Entity<?, ?, ?> kind, EntityOps.DeleteByIndexOp op) {
      IndexSpec spec = requireIndex(kind, op.indexName());
      String indexKey = exactIndexKey(prefix, spec, normalizedIndexValue(spec, op.key()));
      Jedis j = this.pool.getResource();

      try {
         for (String id : j.zrange(indexKey, 0L, -1L)) {
            Map<byte[], byte[]> existing = j.hgetAll(bytes(prefix + id));
            if (existing != null && !existing.isEmpty()) {
               this.removeIndexEntries(j, prefix, kind, kind.codec().shape(), existing);
            }

            j.del(bytes(prefix + id));
         }

         j.del(indexKey);
      } catch (Throwable var12) {
         if (j != null) {
            try {
               j.close();
            } catch (Throwable var11) {
               var12.addSuppressed(var11);
            }
         }

         throw var12;
      }

      if (j != null) {
         j.close();
      }
   }

   private <R> Page<R> pageFromIds(String prefix, Entity<?, ?, ?> kind, List<String> ids, int pageSize, int offset) {
      boolean hasMore = ids.size() > pageSize;
      int limit = hasMore ? pageSize : ids.size();
      List<R> out = new ArrayList<>(limit);

      for (int i = 0; i < limit; i++) {
         String id = ids.get(i);
         Jedis j = this.pool.getResource();

         label57: {
            try {
               Map<byte[], byte[]> hash = j.hgetAll(bytes(prefix + id));
               if (hash != null && !hash.isEmpty()) {
                  out.add((R)this.decodeHash(kind, hash));
                  break label57;
               }
            } catch (Throwable var15) {
               if (j != null) {
                  try {
                     j.close();
                  } catch (Throwable var14) {
                     var15.addSuppressed(var14);
                  }
               }

               throw var15;
            }

            if (j != null) {
               j.close();
            }
            continue;
         }

         if (j != null) {
            j.close();
         }
      }

      Cursor next = hasMore ? new Cursor("redis:" + (offset + pageSize)) : null;
      return new Page<>(out, next);
   }

   private void addIndexEntries(Jedis j, String prefix, Entity<?, ?, ?> kind, EncodeShape shape, BsonCodec codec, Object record, Object idVal) {
      String id = RedisKeys.encode(idVal);

      for (IndexSpec spec : kind.secondaryIndexes()) {
         String leadingField = stripDir(spec.fields().get(0));
         Object leading = codec.readField(record, fieldIndex(shape, leadingField));
         if (leading != null) {
            String normalizedLeading = normalizedIndexValue(spec, leading);
            j.zadd(exactIndexKey(prefix, spec, normalizedLeading), orderedScoreFromRecord(shape, codec, record, spec), id);
            if (spec.caseInsensitivePrefix()) {
               j.zadd(lexIndexKey(prefix, spec), 0.0, lexMember(normalizedLeading, id));
            }
         }
      }
   }

   private static void deleteIndexKeys(Jedis j, String prefix) {
      String cursor = ScanParams.SCAN_POINTER_START;
      ScanParams params = new ScanParams().match(prefix + "__idx:*").count(1000);

      do {
         ScanResult<String> result = j.scan(cursor, params);
         List<String> keys = result.getResult();
         if (!keys.isEmpty()) {
            j.del(keys.toArray(String[]::new));
         }

         cursor = result.getCursor();
      } while (!ScanParams.SCAN_POINTER_START.equals(cursor));
   }

   private void removeIndexEntries(Jedis j, String prefix, Entity<?, ?, ?> kind, EncodeShape shape, Map<byte[], byte[]> hash) {
      byte[] rawId = hashGet(hash, shape.idField());
      if (rawId != null) {
         EncodeShape.FieldDef idDef = shape.fields().get(fieldIndex(shape, shape.idField()));
         String id = RedisKeys.encode(parseField(idDef, rawId));

         for (IndexSpec spec : kind.secondaryIndexes()) {
            byte[] leading = hashGet(hash, stripDir(spec.fields().get(0)));
            if (leading != null) {
               EncodeShape.FieldDef leadingDef = shape.fields().get(fieldIndex(shape, stripDir(spec.fields().get(0))));
               String normalizedLeading = normalizedIndexValue(spec, parseField(leadingDef, leading));
               j.zrem(exactIndexKey(prefix, spec, normalizedLeading), new String[]{id});
               if (spec.caseInsensitivePrefix()) {
                  j.zrem(lexIndexKey(prefix, spec), new String[]{lexMember(normalizedLeading, id)});
               }
            }
         }
      }
   }

   private Object decodeHash(Entity<?, ?, ?> kind, Map<byte[], byte[]> hash) {
      BsonCodec codec = BsonCodecs.regular(kind.recordType());
      EncodeShape shape = kind.codec().shape();
      Object[] args = new Object[shape.fields().size()];

      for (int i = 0; i < shape.fields().size(); i++) {
         EncodeShape.FieldDef f = shape.fields().get(i);
         byte[] raw = hashGet(hash, f.name());
         args[i] = raw == null ? null : parseField(f, raw);
      }

      return codec.decodeFromValues(args);
   }

   private static IndexSpec requireIndex(Entity<?, ?, ?> kind, String name) {
      for (IndexSpec spec : kind.secondaryIndexes()) {
         if (spec.name().equals(name)) {
            return spec;
         }
      }

      throw new IllegalArgumentException("entity " + kind.name() + " has no index " + name);
   }

   private static String exactIndexKey(String prefix, IndexSpec spec, String leading) {
      return prefix + "__idx:" + spec.name() + ":eq:" + leading;
   }

   private static String lexIndexKey(String prefix, IndexSpec spec) {
      return prefix + "__idx:" + spec.name() + ":lex";
   }

   private static String lexMember(String leading, String id) {
      return leading + "\u0000" + id;
   }

   private static String idFromLexMember(String member) {
      int sep = member.indexOf(0);
      return sep < 0 ? member : member.substring(sep + 1);
   }

   private static String normalizedIndexValue(IndexSpec spec, Object value) {
      String encoded = RedisKeys.encode(value);
      return spec.caseInsensitivePrefix() ? encoded.toLowerCase(Locale.ROOT) : encoded;
   }

   private static double orderedScoreFromRecord(EncodeShape shape, BsonCodec codec, Object record, IndexSpec spec) {
      if (spec.fields().size() <= 1) {
         return 0.0;
      } else {
         String field = spec.fields().get(1);
         Object value = codec.readField(record, fieldIndex(shape, stripDir(field)));
         return score(field, value);
      }
   }

   private static double score(String field, Object value) {
      if (value == null) {
         return 0.0;
      } else {
         double score;
         if (value instanceof Number n) {
            score = n.doubleValue();
         } else {
            score = Double.parseDouble(RedisKeys.encode(value));
         }

         return field.startsWith("-") ? -score : score;
      }
   }

   private static int cursorOffset(Cursor cursor) {
      if (cursor == null) {
         return 0;
      } else {
         String token = cursor.token();
         return token.startsWith("redis:") ? Integer.parseInt(token.substring("redis:".length())) : Integer.parseInt(token);
      }
   }

   private static String stripDir(String field) {
      return field.startsWith("-") ? field.substring(1) : field;
   }

   private static int fieldIndex(EncodeShape shape, String name) {
      for (int i = 0; i < shape.fields().size(); i++) {
         if (shape.fields().get(i).name().equals(name)) {
            return i;
         }
      }

      throw new IllegalArgumentException("shape has no field " + name);
   }

   static Object parseFieldStatic(@NotNull EncodeShape.FieldDef f, @NotNull String raw) {
      return parseField(f, raw);
   }

   static Object parseFieldBytesStatic(@NotNull EncodeShape.FieldDef f, byte[] raw) {
      return parseField(f, raw);
   }

   private static Object parseField(@NotNull EncodeShape.FieldDef f, byte[] raw) {
      return f.javaType() == byte[].class ? raw : parseField(f, new String(raw, StandardCharsets.UTF_8));
   }

   private static Object parseField(@NotNull EncodeShape.FieldDef f, @NotNull String raw) {
      Class<?> t = f.javaType();
      if (t == long.class || t == Long.class) {
         return Long.parseLong(raw);
      } else if (t == int.class || t == Integer.class) {
         return Integer.parseInt(raw);
      } else if (t == double.class || t == Double.class) {
         return Double.parseDouble(raw);
      } else if (t == float.class || t == Float.class) {
         return Float.parseFloat(raw);
      } else if (t == boolean.class || t == Boolean.class) {
         return Boolean.parseBoolean(raw);
      } else if (t == UUID.class) {
         return UUID.fromString(raw);
      } else if (t == String.class) {
         return raw;
      } else if (t.isEnum()) {
         int ordinal = Integer.parseInt(raw);
         return t.getEnumConstants()[ordinal];
      } else {
         return raw;
      }
   }

   private static byte[] fieldValueBytes(@NotNull EncodeShape.FieldDef f, @NotNull Object value) {
      return f.javaType() == byte[].class ? (byte[])value : bytes(RedisKeys.encode(value));
   }

   private static byte[] bytes(@NotNull String s) {
      return s.getBytes(StandardCharsets.UTF_8);
   }

   static byte[] hashGet(@NotNull Map<byte[], byte[]> hash, @NotNull String field) {
      byte[] wanted = bytes(field);

      for (Entry<byte[], byte[]> entry : hash.entrySet()) {
         if (Arrays.equals(entry.getKey(), wanted)) {
            return entry.getValue();
         }
      }

      return null;
   }

   private static int idFieldIndex(@NotNull EncodeShape shape) {
      for (int i = 0; i < shape.fields().size(); i++) {
         if (shape.fields().get(i).name().equals(shape.idField())) {
            return i;
         }
      }

      return 0;
   }
}
