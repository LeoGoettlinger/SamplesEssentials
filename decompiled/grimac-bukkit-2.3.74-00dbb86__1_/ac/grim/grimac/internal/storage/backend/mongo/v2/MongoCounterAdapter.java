package ac.grim.grimac.internal.storage.backend.mongo.v2;

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
import ac.grim.grimac.internal.storage.codec.bson.BsonBinaries;
import ac.grim.grimac.shaded.jetbrains.annotations.ApiStatus;
import ac.grim.grimac.shaded.jetbrains.annotations.NotNull;
import com.mongodb.MongoCommandException;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.FindOneAndUpdateOptions;
import com.mongodb.client.model.ReturnDocument;
import com.mongodb.client.model.UpdateOptions;
import com.mongodb.client.model.Updates;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;
import org.bson.BsonBinary;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.Binary;

@ApiStatus.Internal
public final class MongoCounterAdapter implements KindAdapter<Counter<?>> {
   public static final String VALUE_FIELD = "value";
   private static final int NAMESPACE_EXISTS = 48;
   @NotNull
   private final MongoDatabase db;
   @NotNull
   private final Logger logger;
   private final Map<String, MongoCollection<Document>> collections = new ConcurrentHashMap<>();

   public MongoCounterAdapter(@NotNull MongoDatabase db, @NotNull Logger logger) {
      this.db = db;
      this.logger = logger;
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
      String coll = id.name();

      try {
         if (!this.collectionExists(coll)) {
            try {
               this.db.createCollection(coll);
            } catch (MongoCommandException var5) {
               if (var5.getErrorCode() != 48) {
                  throw var5;
               }
            }
         }
      } catch (RuntimeException var6) {
         throw new BackendException("failed to ensure counter collection " + coll, var6);
      }
   }

   public void dropStore(@NotNull StoreId id, @NotNull Counter<?> kind) throws BackendException {
      try {
         this.db.getCollection(id.name()).drop();
         this.collections.remove(id.qualified());
      } catch (RuntimeException var4) {
         throw new BackendException("failed to drop counter " + id, var4);
      }
   }

   @NotNull
   public <E> StorageEventHandler<E> writeHandler(@NotNull StoreId id, @NotNull Counter<?> kind, @NotNull Category<E> category) {
      return new MongoCounterAdapter.CounterHandler<>(id, kind, this.coll(id));
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
            throw new UnsupportedOperationException("MongoCounterAdapter does not handle " + op.getClass().getName());
         }
      } catch (RuntimeException var5) {
         throw new BackendException("mongo counter execute failed for " + op.getClass().getSimpleName() + " on " + id, var5);
      }
   }

   @NotNull
   public List<Migration<Counter<?>>> migrations(@NotNull Counter<?> kind) {
      return List.of();
   }

   private long get(@NotNull StoreId id, @NotNull CounterOps.GetOp<?> op) {
      Document doc = (Document)this.coll(id).find(Filters.eq("_id", encodeKey(op.key()))).projection(new Document("value", 1).append("_id", 0)).first();
      return doc == null ? 0L : extractValue(doc);
   }

   private <K> Map<K, Long> getMany(@NotNull StoreId id, @NotNull CounterOps.GetManyOp<K> op) {
      Collection<K> keys = op.keys();
      if (keys.isEmpty()) {
         return Map.of();
      } else {
         Map<K, Long> out = new LinkedHashMap<>(keys.size() * 4 / 3 + 1);

         for (K k : keys) {
            out.put(k, 0L);
         }

         Map<Object, K> byEncoded = new HashMap<>(keys.size() * 4 / 3 + 1);
         List<Object> encodedIds = new ArrayList<>(keys.size());

         for (K k : keys) {
            Object enc = encodeKey(k);
            encodedIds.add(enc);
            byEncoded.put(enc, k);
         }

         MongoCursor var13 = this.coll(id).find(Filters.in("_id", encodedIds)).projection(new Document("value", 1)).iterator();

         while (var13.hasNext()) {
            Document d = (Document)var13.next();
            Object idVal = d.get("_id");
            K originalKey = byEncoded.get(idVal);
            if (originalKey == null) {
               originalKey = matchByBytes(byEncoded, idVal);
            }

            if (originalKey != null) {
               out.put(originalKey, extractValue(d));
            }
         }

         return out;
      }
   }

   private long incrementBy(@NotNull StoreId id, @NotNull CounterOps.IncrementByOp<?> op) {
      Document after = (Document)this.coll(id)
         .findOneAndUpdate(
            Filters.eq("_id", encodeKey(op.key())),
            Updates.combine(new Bson[]{Updates.inc("value", op.delta()), Updates.setOnInsert("_id", encodeKey(op.key()))}),
            new FindOneAndUpdateOptions().upsert(true).returnDocument(ReturnDocument.AFTER)
         );
      return after == null ? op.delta() : extractValue(after);
   }

   private long setIfHigher(@NotNull StoreId id, @NotNull CounterOps.SetIfHigherOp<?> op) {
      Document after = (Document)this.coll(id)
         .findOneAndUpdate(
            Filters.eq("_id", encodeKey(op.key())),
            Updates.combine(new Bson[]{Updates.max("value", op.value()), Updates.setOnInsert("_id", encodeKey(op.key()))}),
            new FindOneAndUpdateOptions().upsert(true).returnDocument(ReturnDocument.AFTER)
         );
      return after == null ? op.value() : extractValue(after);
   }

   @NotNull
   static Object encodeKey(@NotNull Object key) {
      if (key instanceof UUID u) {
         return BsonBinaries.uuidBinary(u);
      } else if (key instanceof Enum<?> e) {
         return e.ordinal();
      } else {
         return key instanceof byte[] b ? new BsonBinary(b) : key;
      }
   }

   private static long extractValue(@NotNull Document d) {
      return d.get("value") instanceof Number n ? n.longValue() : 0L;
   }

   private static <K> K matchByBytes(@NotNull Map<Object, K> byEncoded, @NotNull Object idVal) {
      byte[] target = bytesOf(idVal);
      if (target == null) {
         return null;
      } else {
         for (Entry<Object, K> e : byEncoded.entrySet()) {
            byte[] candidate = bytesOf(e.getKey());
            if (candidate != null && Arrays.equals(target, candidate)) {
               return e.getValue();
            }
         }

         return null;
      }
   }

   private static byte[] bytesOf(@NotNull Object v) {
      if (v instanceof BsonBinary b) {
         return b.getData();
      } else {
         return v instanceof Binary b ? b.getData() : null;
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

   @NotNull
   private MongoCollection<Document> coll(@NotNull StoreId id) {
      return this.collections.computeIfAbsent(id.qualified(), k -> this.db.getCollection(id.name()));
   }

   private final class CounterHandler<E> implements StorageEventHandler<E> {
      @NotNull
      private final StoreId id;
      @NotNull
      private final MongoCollection<Document> coll;

      CounterHandler(@NotNull StoreId id, @NotNull Counter<?> kind, @NotNull MongoCollection<Document> coll) {
         this.id = id;
         this.coll = coll;
      }

      @Override
      public void onEvent(E event, long sequence, boolean endOfBatch) throws BackendException {
         try {
            CounterEvent<?> ce = (CounterEvent<?>)event;
            Object key = ce.key;
            if (key == null) {
               throw new IllegalStateException("CounterEvent.key must be set on " + this.id);
            } else {
               long delta = ce.delta;
               if (delta != 0L) {
                  this.coll
                     .updateOne(
                        Filters.eq("_id", MongoCounterAdapter.encodeKey(key)),
                        Updates.combine(new Bson[]{Updates.inc("value", delta), Updates.setOnInsert("_id", MongoCounterAdapter.encodeKey(key))}),
                        new UpdateOptions().upsert(true)
                     );
               }
            }
         } catch (RuntimeException var9) {
            throw new BackendException("counter increment failed for " + this.id, var9);
         }
      }
   }
}
