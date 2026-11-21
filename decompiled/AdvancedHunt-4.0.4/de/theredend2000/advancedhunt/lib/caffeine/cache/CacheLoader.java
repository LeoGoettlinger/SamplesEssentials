package de.theredend2000.advancedhunt.lib.caffeine.cache;

import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.Executor;
import java.util.function.Function;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.NullMarked;

@NullMarked
@FunctionalInterface
public interface CacheLoader<K, V> extends AsyncCacheLoader<K, V> {
   V load(K key) throws Exception;

   default Map<? extends K, ? extends V> loadAll(Set<? extends K> keys) throws Exception {
      throw new UnsupportedOperationException();
   }

   @Override
   default CompletableFuture<? extends V> asyncLoad(K key, Executor executor) throws Exception {
      Objects.requireNonNull(key);
      Objects.requireNonNull(executor);
      return CompletableFuture.supplyAsync(() -> {
         try {
            return this.load(key);
         } catch (RuntimeException var3) {
            throw var3;
         } catch (InterruptedException var4) {
            Thread.currentThread().interrupt();
            throw new CompletionException(var4);
         } catch (Exception var5) {
            throw new CompletionException(var5);
         }
      }, executor);
   }

   @Override
   default CompletableFuture<? extends Map<? extends K, ? extends V>> asyncLoadAll(Set<? extends K> keys, Executor executor) throws Exception {
      Objects.requireNonNull(keys);
      Objects.requireNonNull(executor);
      return CompletableFuture.supplyAsync(() -> {
         try {
            return this.loadAll(keys);
         } catch (RuntimeException var3) {
            throw var3;
         } catch (InterruptedException var4) {
            Thread.currentThread().interrupt();
            throw new CompletionException(var4);
         } catch (Exception var5) {
            throw new CompletionException(var5);
         }
      }, executor);
   }

   default V reload(K key, @NonNull V oldValue) throws Exception {
      return this.load(key);
   }

   @Override
   default CompletableFuture<? extends V> asyncReload(K key, @NonNull V oldValue, Executor executor) throws Exception {
      Objects.requireNonNull(key);
      Objects.requireNonNull(executor);
      return CompletableFuture.supplyAsync(() -> {
         try {
            return this.reload(key, oldValue);
         } catch (RuntimeException var4) {
            throw var4;
         } catch (InterruptedException var5) {
            Thread.currentThread().interrupt();
            throw new CompletionException(var5);
         } catch (Exception var6) {
            throw new CompletionException(var6);
         }
      }, executor);
   }

   static <K, V> CacheLoader<K, V> bulk(Function<? super Set<? extends K>, ? extends Map<? extends K, ? extends V>> mappingFunction) {
      return new CacheLoader<K, V>() {
         @Override
         public V load(K key) {
            return (V)this.loadAll(Set.of(key)).get(key);
         }

         @Override
         public Map<? extends K, ? extends V> loadAll(Set<? extends K> keys) {
            return (Map<? extends K, ? extends V>)mappingFunction.apply(keys);
         }
      };
   }
}
