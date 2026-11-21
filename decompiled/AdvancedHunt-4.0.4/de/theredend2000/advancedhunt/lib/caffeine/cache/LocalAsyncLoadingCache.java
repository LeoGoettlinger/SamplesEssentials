package de.theredend2000.advancedhunt.lib.caffeine.cache;

import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeoutException;
import java.util.function.BiFunction;
import java.util.function.Function;
import org.jspecify.annotations.Nullable;

abstract class LocalAsyncLoadingCache<K, V> implements LocalAsyncCache<K, V>, AsyncLoadingCache<K, V> {
   static final Logger logger = System.getLogger(LocalAsyncLoadingCache.class.getName());
   @Nullable
   final BiFunction<? super Set<? extends K>, ? super Executor, ? extends CompletableFuture<? extends Map<? extends K, ? extends V>>> bulkMappingFunction;
   final BiFunction<? super K, ? super Executor, ? extends CompletableFuture<? extends V>> mappingFunction;
   final AsyncCacheLoader<K, V> cacheLoader;
   @Nullable
   LocalAsyncLoadingCache.LoadingCacheView<K, V> cacheView;

   LocalAsyncLoadingCache(AsyncCacheLoader<? super K, V> cacheLoader) {
      this.bulkMappingFunction = this.newBulkMappingFunction(cacheLoader);
      this.cacheLoader = (AsyncCacheLoader<K, V>)cacheLoader;
      this.mappingFunction = this.newMappingFunction(cacheLoader);
   }

   BiFunction<? super K, ? super Executor, ? extends CompletableFuture<? extends V>> newMappingFunction(AsyncCacheLoader<? super K, V> cacheLoader) {
      return (key, executor) -> {
         try {
            return cacheLoader.asyncLoad(key, executor);
         } catch (RuntimeException var4) {
            throw var4;
         } catch (InterruptedException var5) {
            Thread.currentThread().interrupt();
            throw new CompletionException(var5);
         } catch (Exception var6) {
            throw new CompletionException(var6);
         }
      };
   }

   @Nullable
   BiFunction<Set<? extends K>, Executor, CompletableFuture<Map<K, V>>> newBulkMappingFunction(AsyncCacheLoader<? super K, V> cacheLoader) {
      return !this.canBulkLoad(cacheLoader) ? null : (keysToLoad, executor) -> {
         try {
            return (CompletableFuture<Map<K, V>>)cacheLoader.asyncLoadAll(keysToLoad, executor);
         } catch (RuntimeException var4) {
            throw var4;
         } catch (InterruptedException var5) {
            Thread.currentThread().interrupt();
            throw new CompletionException(var5);
         } catch (Exception var6) {
            throw new CompletionException(var6);
         }
      };
   }

   boolean canBulkLoad(AsyncCacheLoader<?, ?> loader) {
      Class<?> defaultLoaderClass = AsyncCacheLoader.class;
      if (loader instanceof CacheLoader) {
         defaultLoaderClass = CacheLoader.class;
         if (Caffeine.hasMethodOverride(defaultLoaderClass, loader, "loadAll", Set.class)) {
            return true;
         }
      }

      return Caffeine.hasMethodOverride(defaultLoaderClass, loader, "asyncLoadAll", Set.class, Executor.class);
   }

   @Override
   public CompletableFuture<V> get(K key) {
      return this.get(key, this.mappingFunction);
   }

   @Override
   public CompletableFuture<Map<K, V>> getAll(Iterable<? extends K> keys) {
      if (this.bulkMappingFunction != null) {
         return this.getAll(keys, this.bulkMappingFunction);
      } else {
         Function<K, CompletableFuture<V>> mappingFunction = this::get;
         LinkedHashMap<K, CompletableFuture<V>> result = new LinkedHashMap<>(Caffeine.calculateHashMapCapacity(keys));

         for (K key : keys) {
            CompletableFuture<V> future = result.computeIfAbsent(key, mappingFunction);
            Objects.requireNonNull(future);
         }

         return LocalAsyncCache.composeResult(result);
      }
   }

   @Override
   public LoadingCache<K, V> synchronous() {
      return this.cacheView == null ? (this.cacheView = new LocalAsyncLoadingCache.LoadingCacheView<>(this)) : this.cacheView;
   }

   static final class LoadingCacheView<K, V> extends LocalAsyncCache.AbstractCacheView<K, V> implements LoadingCache<K, V> {
      private static final long serialVersionUID = 1L;
      final LocalAsyncLoadingCache<K, V> asyncCache;

      LoadingCacheView(LocalAsyncLoadingCache<K, V> asyncCache) {
         this.asyncCache = Objects.requireNonNull(asyncCache);
      }

      LocalAsyncLoadingCache<K, V> asyncCache() {
         return this.asyncCache;
      }

      @Override
      public V get(K key) {
         return resolve(this.asyncCache.get(key));
      }

      @Override
      public Map<K, V> getAll(Iterable<? extends K> keys) {
         return resolve(this.asyncCache.getAll(keys));
      }

      @Override
      public CompletableFuture<V> refresh(K key) {
         Objects.requireNonNull(key);
         Object keyReference = this.asyncCache.cache().referenceKey(key);

         CompletableFuture<V> future;
         do {
            future = this.tryOptimisticRefresh(key, keyReference);
            if (future == null) {
               future = this.tryComputeRefresh(key, keyReference);
            }
         } while (future == null);

         return future;
      }

      @Override
      public CompletableFuture<Map<K, V>> refreshAll(Iterable<? extends K> keys) {
         LinkedHashMap<K, CompletableFuture<V>> result = new LinkedHashMap<>(Caffeine.calculateHashMapCapacity(keys));

         for (K key : keys) {
            result.computeIfAbsent(key, this::refresh);
         }

         return LocalAsyncCache.composeResult(result);
      }

      @Nullable
      private CompletableFuture<V> tryOptimisticRefresh(K key, Object keyReference) {
         CompletableFuture<V> lastRefresh = (CompletableFuture<V>)this.asyncCache.cache().refreshes().get(keyReference);
         if (lastRefresh != null) {
            if (!Async.isReady(lastRefresh) && !this.asyncCache.cache().isPendingEviction(key)) {
               return lastRefresh;
            }

            this.asyncCache.cache().refreshes().remove(keyReference, lastRefresh);
         }

         CompletableFuture<V> oldValueFuture = this.asyncCache.cache().getIfPresentQuietly(key);
         if (oldValueFuture == null || oldValueFuture.isDone() && oldValueFuture.isCompletedExceptionally()) {
            if (oldValueFuture != null) {
               this.asyncCache.cache().remove(key, oldValueFuture);
            }

            CompletableFuture<V> future = this.asyncCache.get(key, this.asyncCache.mappingFunction, false);
            CompletableFuture<V> prior = (CompletableFuture<V>)this.asyncCache.cache().refreshes().putIfAbsent(keyReference, future);
            CompletableFuture<V> result = prior == null ? future : prior;
            result.whenComplete((r, e) -> this.asyncCache.cache().refreshes().remove(keyReference, result));
            return result;
         } else {
            return !oldValueFuture.isDone() ? oldValueFuture : null;
         }
      }

      private CompletableFuture<V> tryComputeRefresh(K key, Object keyReference) {
         long[] startTime = new long[1];
         boolean[] refreshed = new boolean[1];
         CompletableFuture<V>[] oldValueFuture = new CompletableFuture[1];
         CompletableFuture<?> future = this.asyncCache.cache().refreshes().computeIfAbsent(keyReference, k -> {
            oldValueFuture[0] = this.asyncCache.cache().getIfPresentQuietly(key);
            V oldValue = Async.getIfReady(oldValueFuture[0]);
            if (oldValue == null) {
               return null;
            } else {
               refreshed[0] = true;
               startTime[0] = this.asyncCache.cache().statsTicker().read();

               try {
                  CompletableFuture<? extends V> reloadFuture = this.asyncCache.cacheLoader.asyncReload(key, oldValue, this.asyncCache.cache().executor());
                  return Objects.requireNonNull(reloadFuture, "Null future");
               } catch (RuntimeException var8) {
                  throw var8;
               } catch (InterruptedException var9) {
                  Thread.currentThread().interrupt();
                  throw new CompletionException(var9);
               } catch (Exception var10) {
                  throw new CompletionException(var10);
               }
            }
         });
         if (future == null) {
            return null;
         } else {
            if (refreshed[0]) {
               future.whenComplete((newValue, error) -> {
                  long loadTime = this.asyncCache.cache().statsTicker().read() - startTime[0];
                  if (error != null) {
                     if (!(error instanceof CancellationException) && !(error instanceof TimeoutException)) {
                        LocalAsyncLoadingCache.logger.log(Level.WARNING, "Exception thrown during refresh", error);
                     }

                     this.asyncCache.cache().refreshes().remove(keyReference, future);
                     this.asyncCache.cache().statsCounter().recordLoadFailure(loadTime);
                  } else {
                     try {
                        boolean[] discard = new boolean[1];
                        boolean[] preserveTimestamps = new boolean[1];
                        CompletableFuture<V> value = this.asyncCache.cache().compute(key, (ignored, currentValue) -> {
                           boolean successful = this.asyncCache.cache().refreshes().remove(keyReference, future);
                           if (!successful || currentValue != oldValueFuture[0]) {
                              preserveTimestamps[0] = true;
                              discard[0] = true;
                              return currentValue;
                           } else if (currentValue == future) {
                              return currentValue;
                           } else if (newValue == Async.<V>getIfReady((CompletableFuture<V>)currentValue)) {
                              return currentValue;
                           } else {
                              return (CompletableFuture<V>)(newValue == null ? null : future);
                           }
                        }, this.asyncCache.cache().expiry(), false, true, preserveTimestamps);
                        if (discard[0] && newValue != null) {
                           RemovalCause cause = value == null ? RemovalCause.EXPLICIT : RemovalCause.REPLACED;
                           this.asyncCache.cache().notifyRemoval(key, (CompletableFuture<V>)future, cause);
                        }

                        if (newValue == null) {
                           this.asyncCache.cache().statsCounter().recordLoadFailure(loadTime);
                        } else {
                           this.asyncCache.cache().statsCounter().recordLoadSuccess(loadTime);
                        }
                     } catch (Throwable var14) {
                        LocalAsyncLoadingCache.logger.log(Level.WARNING, "Exception thrown during asynchronous load", var14);
                        this.asyncCache.cache().statsCounter().recordLoadFailure(loadTime);
                        this.asyncCache.cache().remove(key, future);
                     }
                  }
               });
            }

            return (CompletableFuture<V>)future;
         }
      }
   }
}
