package ac.grim.grimac.internal.storage.backend.redis.v2;

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
import ac.grim.grimac.shaded.jetbrains.annotations.ApiStatus;
import ac.grim.grimac.shaded.jetbrains.annotations.NotNull;
import java.util.Collection;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

@ApiStatus.Internal
public final class RedisCounterAdapter implements KindAdapter<Counter<?>> {
   @NotNull
   private final JedisPool pool;
   @NotNull
   private final String keyPrefix;
   @NotNull
   private final Logger logger;

   public RedisCounterAdapter(@NotNull JedisPool pool, @NotNull String keyPrefix, @NotNull Logger logger) {
      this.pool = pool;
      this.keyPrefix = keyPrefix;
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

   public void ensureStore(@NotNull StoreId id, @NotNull Counter<?> kind) {
   }

   public void dropStore(@NotNull StoreId id, @NotNull Counter<?> kind) throws BackendException {
   }

   @NotNull
   public <E> StorageEventHandler<E> writeHandler(@NotNull StoreId id, @NotNull Counter<?> kind, @NotNull Category<E> category) {
      String prefix = this.keyPrefix + id.name() + ":";
      return (event, sequence, endOfBatch) -> {
         CounterEvent<?> ce = (CounterEvent<?>)event;
         if (ce.key != null && ce.delta != 0L) {
            Jedis j = this.pool.getResource();

            try {
               j.incrBy(prefix + RedisKeys.encode(ce.key), ce.delta);
            } catch (Throwable var11) {
               if (j != null) {
                  try {
                     j.close();
                  } catch (Throwable var10) {
                     var11.addSuppressed(var10);
                  }
               }

               throw var11;
            }

            if (j != null) {
               j.close();
            }
         }
      };
   }

   public <R> R execute(@NotNull StoreId id, @NotNull Counter<?> kind, @NotNull Operation<R> op) throws BackendException {
      String prefix = this.keyPrefix + id.name() + ":";

      try {
         if (op instanceof CounterOps.GetOp g) {
            return (R)this.get(prefix, g);
         } else if (op instanceof CounterOps.GetManyOp g) {
            return (R)this.getMany(prefix, g);
         } else if (op instanceof CounterOps.IncrementByOp i) {
            return (R)this.incrementBy(prefix, i);
         } else if (op instanceof CounterOps.SetIfHigherOp s) {
            return (R)this.setIfHigher(prefix, s);
         } else {
            throw new UnsupportedOperationException("RedisCounterAdapter does not handle " + op.getClass().getName());
         }
      } catch (RuntimeException var6) {
         throw new BackendException("redis counter failed", var6);
      }
   }

   @NotNull
   public List<Migration<Counter<?>>> migrations(@NotNull Counter<?> kind) {
      return List.of();
   }

   private long get(@NotNull String prefix, @NotNull CounterOps.GetOp<?> op) {
      Jedis j = this.pool.getResource();

      long var5;
      try {
         String val = j.get(prefix + RedisKeys.encode(op.key()));
         var5 = val == null ? 0L : Long.parseLong(val);
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

   private <K> Map<K, Long> getMany(@NotNull String prefix, @NotNull CounterOps.GetManyOp<K> op) {
      Collection<K> keys = op.keys();
      if (keys.isEmpty()) {
         return Map.of();
      } else {
         Map<K, Long> out = new LinkedHashMap<>(keys.size() * 4 / 3 + 1);
         String[] redisKeys = new String[keys.size()];
         K[] originalKeys = (K[])keys.toArray();
         int i = 0;

         for (K k : keys) {
            redisKeys[i] = prefix + RedisKeys.encode(k);
            out.put(k, 0L);
            i++;
         }

         Jedis j = this.pool.getResource();

         try {
            List<String> values = j.mget(redisKeys);

            for (int idx = 0; idx < values.size(); idx++) {
               String v = values.get(idx);
               if (v != null) {
                  out.put(originalKeys[idx], Long.parseLong(v));
               }
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

         return out;
      }
   }

   private long incrementBy(@NotNull String prefix, @NotNull CounterOps.IncrementByOp<?> op) {
      Jedis j = this.pool.getResource();

      long var4;
      try {
         var4 = j.incrBy(prefix + RedisKeys.encode(op.key()), op.delta());
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

   private long setIfHigher(@NotNull String prefix, @NotNull CounterOps.SetIfHigherOp<?> op) {
      String key = prefix + RedisKeys.encode(op.key());
      String lua = "local c = tonumber(redis.call('GET', KEYS[1]) or '0') if ARGV[1]+0 > c then redis.call('SET', KEYS[1], ARGV[1]) return ARGV[1]+0 else return c end";
      Jedis j = this.pool.getResource();

      long var11;
      try {
         var11 = j.eval(lua, 1, new String[]{key, String.valueOf(op.value())}) instanceof Number n ? n.longValue() : op.value();
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

      return var11;
   }
}
