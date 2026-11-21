package ac.grim.grimac.internal.storage.backend.mongo.v2;

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
import ac.grim.grimac.shaded.jetbrains.annotations.ApiStatus;
import ac.grim.grimac.shaded.jetbrains.annotations.NotNull;
import com.mongodb.MongoCommandException;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.UpdateOptions;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;
import org.bson.BsonBinary;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.Binary;

@ApiStatus.Internal
public final class MongoKeyValueScopedAdapter implements KindAdapter<KeyValueScoped<?, ?>> {
   private static final String VALUES_FIELD = "values";
   private static final String UPDATED_FIELD = "updated_at";
   private static final int NAMESPACE_EXISTS = 48;
   @NotNull
   private final MongoDatabase db;
   private final Map<String, MongoCollection<Document>> collections = new ConcurrentHashMap<>();

   @NotNull
   private static String requireValidKey(@NotNull String key) {
      if (key.isEmpty()) {
         throw new IllegalArgumentException("KV key must be non-empty");
      } else if (key.indexOf(46) >= 0) {
         throw new IllegalArgumentException("KV key may not contain '.': " + key);
      } else if (key.startsWith("$")) {
         throw new IllegalArgumentException("KV key may not start with '$': " + key);
      } else {
         return key;
      }
   }

   public MongoKeyValueScopedAdapter(@NotNull MongoDatabase db, @NotNull Logger logger) {
      this.db = db;
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
         throw new BackendException("failed to ensure kv-scoped collection " + coll, var6);
      }
   }

   public void dropStore(@NotNull StoreId id, @NotNull KeyValueScoped<?, ?> kind) throws BackendException {
      try {
         this.db.getCollection(id.name()).drop();
         this.collections.remove(id.qualified());
      } catch (RuntimeException var4) {
         throw new BackendException("failed to drop " + id, var4);
      }
   }

   @NotNull
   public <E> StorageEventHandler<E> writeHandler(@NotNull StoreId id, @NotNull KeyValueScoped<?, ?> kind, @NotNull Category<E> category) {
      StorageEventHandler<E> handler = new MongoKeyValueScopedAdapter.KvHandler(id);
      return handler;
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
            throw new UnsupportedOperationException("MongoKeyValueScopedAdapter does not handle " + op.getClass().getName());
         }
      } catch (RuntimeException var5) {
         throw new BackendException("mongo kv execute failed for " + op.getClass().getSimpleName(), var5);
      }
   }

   @NotNull
   public List<Migration<KeyValueScoped<?, ?>>> migrations(@NotNull KeyValueScoped<?, ?> kind) {
      return List.of(new MongoSettingsV6ToV7Migration());
   }

   private Optional<Object> get(@NotNull StoreId id, @NotNull KeyValueScopedOps.GetOp<?, ?> op) {
      String key = requireValidKey(op.key());
      Document doc = (Document)this.coll(id).find(idFilter(op.scope(), op.scopeKey())).first();
      if (doc == null) {
         return Optional.empty();
      } else {
         Document values = (Document)doc.get("values", Document.class);
         if (values == null) {
            return Optional.empty();
         } else {
            Object raw = values.get(key);
            return raw == null ? Optional.empty() : Optional.of(unwrapBinary(raw));
         }
      }
   }

   @NotNull
   private Map<String, Object> getAll(@NotNull StoreId id, @NotNull KeyValueScopedOps.GetAllOp<?, ?> op) {
      Document doc = (Document)this.coll(id).find(idFilter(op.scope(), op.scopeKey())).first();
      if (doc == null) {
         return Map.of();
      } else {
         Document values = (Document)doc.get("values", Document.class);
         if (values == null) {
            return Map.of();
         } else {
            Map<String, Object> out = new LinkedHashMap<>(values.size());

            for (Entry<String, Object> e : values.entrySet()) {
               out.put(e.getKey(), unwrapBinary(e.getValue()));
            }

            return out;
         }
      }
   }

   private void put(@NotNull StoreId id, @NotNull KeyValueScopedOps.PutOp<?, ?> op) {
      String key = requireValidKey(op.key());
      if (op.value() == null) {
         throw new IllegalArgumentException("KV PutOp value must be non-null; use RemoveOp for deletion");
      } else {
         Document filter = compositeId(op.scope(), op.scopeKey());
         this.coll(id)
            .updateOne(
               idFilter(op.scope(), op.scopeKey()),
               new Document("$set", new Document("values." + key, wrapValue(op.value())).append("updated_at", System.currentTimeMillis()))
                  .append("$setOnInsert", new Document("_id", filter)),
               new UpdateOptions().upsert(true)
            );
      }
   }

   private void putAll(@NotNull StoreId id, @NotNull KeyValueScopedOps.PutAllOp<?, ?> op) {
      if (!op.values().isEmpty()) {
         Document filter = compositeId(op.scope(), op.scopeKey());
         Document setOps = new Document();

         for (Entry<String, ?> e : op.values().entrySet()) {
            String key = requireValidKey(e.getKey());
            if (e.getValue() == null) {
               throw new IllegalArgumentException("KV PutAllOp value for key '" + key + "' must be non-null");
            }

            setOps.append("values." + key, wrapValue(e.getValue()));
         }

         setOps.append("updated_at", System.currentTimeMillis());
         this.coll(id)
            .updateOne(
               idFilter(op.scope(), op.scopeKey()),
               new Document("$set", setOps).append("$setOnInsert", new Document("_id", filter)),
               new UpdateOptions().upsert(true)
            );
      }
   }

   private void remove(@NotNull StoreId id, @NotNull KeyValueScopedOps.RemoveOp<?> op) {
      String key = requireValidKey(op.key());
      this.coll(id)
         .updateOne(
            idFilter(op.scope(), op.scopeKey()),
            new Document("$unset", new Document("values." + key, "")).append("$set", new Document("updated_at", System.currentTimeMillis()))
         );
   }

   private void removeAll(@NotNull StoreId id, @NotNull KeyValueScopedOps.RemoveAllOp<?> op) {
      this.coll(id).deleteOne(idFilter(op.scope(), op.scopeKey()));
   }

   private long count(@NotNull StoreId id, @NotNull KeyValueScopedOps.CountOp<?> op) {
      Document doc = (Document)this.coll(id).find(idFilter(op.scope(), op.scopeKey())).first();
      if (doc == null) {
         return 0L;
      } else {
         Document values = (Document)doc.get("values", Document.class);
         return values == null ? 0L : values.size();
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

   @NotNull
   private static Bson idFilter(@NotNull Object scope, @NotNull String scopeKey) {
      return Filters.eq("_id", compositeId(scope, scopeKey));
   }

   @NotNull
   private static Document compositeId(@NotNull Object scope, @NotNull String scopeKey) {
      return new Document("scope", scopeName(scope)).append("scope_key", scopeKey);
   }

   @NotNull
   private static String scopeName(@NotNull Object scope) {
      return scope instanceof Enum<?> e ? e.name() : scope.toString();
   }

   @NotNull
   private static Object wrapValue(@NotNull Object v) {
      return v instanceof byte[] b ? new BsonBinary(b) : v;
   }

   @NotNull
   private static Object unwrapBinary(@NotNull Object raw) {
      if (raw instanceof Binary b) {
         return b.getData();
      } else {
         return raw instanceof BsonBinary b ? b.getData() : raw;
      }
   }

   private final class KvHandler implements StorageEventHandler<KeyValueEvent<?, ?>> {
      @NotNull
      private final StoreId id;

      KvHandler(@NotNull StoreId id) {
         this.id = id;
      }

      public void onEvent(KeyValueEvent<?, ?> event, long sequence, boolean endOfBatch) throws BackendException {
         try {
            Object scope = event.scope;
            String scopeKey = event.scopeKey;
            String key = event.key;
            if (scope == null || scopeKey == null || key == null) {
               throw new IllegalStateException("KeyValueEvent missing scope/scopeKey/key on " + this.id);
            }

            MongoKeyValueScopedAdapter.requireValidKey(key);
            if (!event.remove && event.value == null) {
               throw new IllegalStateException("KeyValueEvent.value is null and event.remove is false on " + this.id + " (key=" + key + ")");
            }

            if (event.remove) {
               MongoKeyValueScopedAdapter.this.coll(this.id)
                  .updateOne(
                     MongoKeyValueScopedAdapter.idFilter(scope, scopeKey),
                     new Document("$unset", new Document("values." + key, "")).append("$set", new Document("updated_at", System.currentTimeMillis()))
                  );
            } else {
               Document filter = MongoKeyValueScopedAdapter.compositeId(scope, scopeKey);
               MongoKeyValueScopedAdapter.this.coll(this.id)
                  .updateOne(
                     MongoKeyValueScopedAdapter.idFilter(scope, scopeKey),
                     new Document(
                           "$set",
                           new Document("values." + key, MongoKeyValueScopedAdapter.wrapValue(event.value)).append("updated_at", System.currentTimeMillis())
                        )
                        .append("$setOnInsert", new Document("_id", filter)),
                     new UpdateOptions().upsert(true)
                  );
            }
         } catch (RuntimeException var12) {
            throw new BackendException("kv handler failed for " + this.id, var12);
         } finally {
            event.clear();
         }
      }
   }
}
