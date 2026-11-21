package ac.grim.grimac.internal.storage.backend.redis.v2;

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
import java.nio.charset.StandardCharsets;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Map.Entry;
import java.util.logging.Logger;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

@ApiStatus.Internal
public final class RedisKVScopedAdapter implements KindAdapter<KeyValueScoped<?, ?>> {
   @NotNull
   private final JedisPool pool;
   @NotNull
   private final String keyPrefix;
   @NotNull
   private final Logger logger;

   public RedisKVScopedAdapter(@NotNull JedisPool pool, @NotNull String keyPrefix, @NotNull Logger logger) {
      this.pool = pool;
      this.keyPrefix = keyPrefix;
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

   public void ensureStore(@NotNull StoreId id, @NotNull KeyValueScoped<?, ?> kind) {
   }

   public void dropStore(@NotNull StoreId id, @NotNull KeyValueScoped<?, ?> kind) {
   }

   @NotNull
   public <E> StorageEventHandler<E> writeHandler(@NotNull StoreId id, @NotNull KeyValueScoped<?, ?> kind, @NotNull Category<E> category) {
      String storePrefix = this.keyPrefix + id.name() + ":";
      return (event, sequence, endOfBatch) -> {
         KeyValueEvent<?, ?> kve = (KeyValueEvent<?, ?>)event;
         if (kve.scope != null && kve.scopeKey != null && kve.key != null) {
            byte[] hashKeyBytes = bytes(storePrefix + kve.scope + ":" + kve.scopeKey);
            byte[] fieldBytes = bytes(kve.key);
            Jedis j = this.pool.getResource();

            label51: {
               try {
                  if (kve.remove) {
                     j.hdel(hashKeyBytes, new byte[][]{fieldBytes});
                  } else {
                     if (kve.value == null) {
                        break label51;
                     }

                     j.hset(hashKeyBytes, fieldBytes, valueAsBytes(kve.value));
                  }
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

               return;
            }

            if (j != null) {
               j.close();
            }
         }
      };
   }

   public <R> R execute(@NotNull StoreId id, @NotNull KeyValueScoped<?, ?> kind, @NotNull Operation<R> op) throws BackendException {
      String storePrefix = this.keyPrefix + id.name() + ":";

      try {
         if (op instanceof KeyValueScopedOps.GetOp<?, ?> g) {
            return (R)this.get(storePrefix, g);
         } else if (op instanceof KeyValueScopedOps.GetAllOp<?, ?> g) {
            return (R)this.getAll(storePrefix, g);
         } else if (op instanceof KeyValueScopedOps.PutOp<?, ?> p) {
            this.put(storePrefix, p);
            return null;
         } else if (op instanceof KeyValueScopedOps.PutAllOp<?, ?> p) {
            this.putAll(storePrefix, p);
            return null;
         } else if (op instanceof KeyValueScopedOps.RemoveOp<?> r) {
            this.remove(storePrefix, r);
            return null;
         } else if (op instanceof KeyValueScopedOps.RemoveAllOp<?> r) {
            this.removeAll(storePrefix, r);
            return null;
         } else if (op instanceof KeyValueScopedOps.CountOp<?> c) {
            return (R)this.count(storePrefix, c);
         } else {
            throw new UnsupportedOperationException("RedisKVScopedAdapter: " + op.getClass().getName());
         }
      } catch (RuntimeException var6) {
         throw new BackendException("redis kv failed", var6);
      }
   }

   @NotNull
   public List<Migration<KeyValueScoped<?, ?>>> migrations(@NotNull KeyValueScoped<?, ?> kind) {
      return List.of();
   }

   private byte[] hashKeyBytes(String prefix, Object scope, String scopeKey) {
      return bytes(prefix + scope + ":" + scopeKey);
   }

   private Optional<Object> get(String prefix, KeyValueScopedOps.GetOp<?, ?> op) {
      Jedis j = this.pool.getResource();

      Optional var5;
      try {
         byte[] v = j.hget(this.hashKeyBytes(prefix, op.scope(), op.scopeKey()), bytes(op.key()));
         var5 = v == null ? Optional.empty() : Optional.of(v);
      } catch (Throwable var7) {
         if (j != null) {
            try {
               j.close();
            } catch (Throwable var6) {
               var7.addSuppressed(var6);
            }
         }

         throw var7;
      }

      if (j != null) {
         j.close();
      }

      return var5;
   }

   private Map<String, Object> getAll(String prefix, KeyValueScopedOps.GetAllOp<?, ?> op) {
      Jedis j = this.pool.getResource();

      Object var6;
      try {
         Map<byte[], byte[]> all = j.hgetAll(this.hashKeyBytes(prefix, op.scope(), op.scopeKey()));
         Map<String, Object> out = new LinkedHashMap<>(all.size());
         all.forEach((k, v) -> {
            if (v != null) {
               out.put(new String(k, StandardCharsets.UTF_8), v);
            }
         });
         var6 = out;
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

      return (Map<String, Object>)var6;
   }

   private void put(String prefix, KeyValueScopedOps.PutOp<?, ?> op) throws BackendException {
      Jedis j = this.pool.getResource();

      try {
         j.hset(this.hashKeyBytes(prefix, op.scope(), op.scopeKey()), bytes(op.key()), valueAsBytes(op.value()));
      } catch (Throwable var7) {
         if (j != null) {
            try {
               j.close();
            } catch (Throwable var6) {
               var7.addSuppressed(var6);
            }
         }

         throw var7;
      }

      if (j != null) {
         j.close();
      }
   }

   private void putAll(String prefix, KeyValueScopedOps.PutAllOp<?, ?> op) throws BackendException {
      Map<byte[], byte[]> fields = new LinkedHashMap<>(op.values().size());

      for (Entry<String, ?> e : op.values().entrySet()) {
         fields.put(bytes(e.getKey()), valueAsBytes(e.getValue()));
      }

      Jedis j = this.pool.getResource();

      try {
         j.hset(this.hashKeyBytes(prefix, op.scope(), op.scopeKey()), fields);
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
   }

   private void remove(String prefix, KeyValueScopedOps.RemoveOp<?> op) {
      Jedis j = this.pool.getResource();

      try {
         j.hdel(this.hashKeyBytes(prefix, op.scope(), op.scopeKey()), new byte[][]{bytes(op.key())});
      } catch (Throwable var7) {
         if (j != null) {
            try {
               j.close();
            } catch (Throwable var6) {
               var7.addSuppressed(var6);
            }
         }

         throw var7;
      }

      if (j != null) {
         j.close();
      }
   }

   private void removeAll(String prefix, KeyValueScopedOps.RemoveAllOp<?> op) {
      Jedis j = this.pool.getResource();

      try {
         j.del(this.hashKeyBytes(prefix, op.scope(), op.scopeKey()));
      } catch (Throwable var7) {
         if (j != null) {
            try {
               j.close();
            } catch (Throwable var6) {
               var7.addSuppressed(var6);
            }
         }

         throw var7;
      }

      if (j != null) {
         j.close();
      }
   }

   private long count(String prefix, KeyValueScopedOps.CountOp<?> op) {
      Jedis j = this.pool.getResource();

      long var4;
      try {
         var4 = j.hlen(this.hashKeyBytes(prefix, op.scope(), op.scopeKey()));
      } catch (Throwable var7) {
         if (j != null) {
            try {
               j.close();
            } catch (Throwable var6) {
               var7.addSuppressed(var6);
            }
         }

         throw var7;
      }

      if (j != null) {
         j.close();
      }

      return var4;
   }

   @NotNull
   private static byte[] bytes(@NotNull String s) {
      return s.getBytes(StandardCharsets.UTF_8);
   }

   @NotNull
   private static byte[] valueAsBytes(@NotNull Object value) throws BackendException {
      if (value instanceof byte[] b) {
         return b;
      } else if (value instanceof String s) {
         return bytes(s);
      } else {
         throw new BackendException("KV value must be byte[] or String, got " + value.getClass().getName());
      }
   }
}
