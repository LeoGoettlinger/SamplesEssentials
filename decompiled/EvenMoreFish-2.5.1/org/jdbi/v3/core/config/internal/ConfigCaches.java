package org.jdbi.v3.core.config.internal;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;
import java.util.function.Function;
import org.jdbi.v3.core.config.ConfigRegistry;
import org.jdbi.v3.core.config.JdbiConfig;
import org.jdbi.v3.core.internal.exceptions.Sneaky;

public final class ConfigCaches implements JdbiConfig<ConfigCaches> {
   private final Map<ConfigCache<?, ?>, Map<Object, Object>> caches = new ConcurrentHashMap<>();

   public ConfigCaches createCopy() {
      return this;
   }

   public static <K, V> ConfigCache<K, V> declare(Function<K, V> computer) {
      return declare(Function.identity(), computer);
   }

   public static <K, V> ConfigCache<K, V> declare(Function<K, ?> keyNormalizer, Function<K, V> computer) {
      return declare(keyNormalizer, (config, k) -> computer.apply(k));
   }

   public static <K, V> ConfigCache<K, V> declare(BiFunction<ConfigRegistry, K, V> computer) {
      return declare(Function.identity(), computer);
   }

   public static <K, V> ConfigCache<K, V> declare(Function<K, ?> keyNormalizer, BiFunction<ConfigRegistry, K, V> computer) {
      return new ConfigCache<K, V>() {
         @Override
         public V get(K key, ConfigRegistry config) {
            Map<Object, Object> cache = config.get(ConfigCaches.class).caches.computeIfAbsent(this, x -> new ConcurrentHashMap<>());
            Object normalizedKey = keyNormalizer.apply(key);
            ConfigCaches.CacheEntry entry = new ConfigCaches.CacheEntry();
            ConfigCaches.CacheEntry existing = (ConfigCaches.CacheEntry)cache.putIfAbsent(normalizedKey, entry);
            if (existing != null) {
               if (Thread.currentThread().equals(existing.owner) && !existing.value.isDone()) {
                  throw new IllegalStateException("Recursive cache computation for key " + key);
               } else {
                  try {
                     return (V)existing.value.join();
                  } catch (CompletionException var9) {
                     throw Sneaky.throwAnyway(var9.getCause());
                  }
               }
            } else {
               Object computed;
               try {
                  computed = computer.apply(config, key);
               } catch (Throwable var10) {
                  ConfigCaches.fail(cache, normalizedKey, entry, var10);
                  throw var10;
               }

               if (computed == null) {
                  IllegalStateException e = new IllegalStateException("Cache computer returned null for key " + key);
                  ConfigCaches.fail(cache, normalizedKey, entry, e);
                  throw e;
               } else {
                  entry.complete(computed);
                  return (V)computed;
               }
            }
         }
      };
   }

   private static void fail(Map<Object, Object> cache, Object normalizedKey, ConfigCaches.CacheEntry entry, Throwable cause) {
      cache.remove(normalizedKey, entry);
      entry.completeExceptionally(cause);
   }

   private static final class CacheEntry {
      Thread owner = Thread.currentThread();
      final CompletableFuture<Object> value = new CompletableFuture<>();

      void complete(Object computed) {
         this.owner = null;
         this.value.complete(computed);
      }

      void completeExceptionally(Throwable cause) {
         this.owner = null;
         this.value.completeExceptionally(cause);
      }
   }
}
