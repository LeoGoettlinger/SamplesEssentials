package com.oheers.fish.database.data.manager;

import com.oheers.fish.EvenMoreFish;
import com.oheers.fish.database.data.strategy.DataSavingStrategy;
import com.oheers.fish.database.execute.DatabaseWorker;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.function.Supplier;

public class DataManager<T> {
   private final Map<String, T> cache = new ConcurrentHashMap<>();
   private final Set<String> dirtyKeys = ConcurrentHashMap.newKeySet();
   private final DataSavingStrategy<T> savingStrategy;
   private final Function<String, T> defaultLoader;
   private final ScheduledExecutorService flushScheduler;
   private static final AtomicInteger THREAD_COUNTER = new AtomicInteger(1);

   public DataManager(DataSavingStrategy<T> savingStrategy, Function<String, T> defaultLoader) {
      this(savingStrategy, defaultLoader, null, null);
   }

   public DataManager(DataSavingStrategy<T> savingStrategy, Function<String, T> defaultLoader, Long flushInterval, TimeUnit flushUnit) {
      this.savingStrategy = savingStrategy;
      this.defaultLoader = defaultLoader;
      this.flushScheduler = this.createFlushScheduler(flushInterval, flushUnit);
   }

   public T get(String key, Function<String, T> loader) {
      T cachedValue = this.cache.get(key);
      if (cachedValue != null) {
         return cachedValue;
      } else {
         T loadedValue = loader.apply(key);
         if (loadedValue == null) {
            return null;
         } else {
            T previousValue = this.cache.putIfAbsent(key, loadedValue);
            return previousValue == null ? loadedValue : previousValue;
         }
      }
   }

   public CompletableFuture<T> getAsync(String key, Function<String, T> loader, DatabaseWorker databaseWorker) {
      T cachedValue = this.cache.get(key);
      return cachedValue != null ? CompletableFuture.completedFuture(cachedValue) : databaseWorker.query(() -> this.get(key, loader));
   }

   public T getOrCreate(String key, Function<String, T> loader, Supplier<T> fallbackValueSupplier) {
      T loadedValue = this.get(key, loader);
      if (loadedValue != null) {
         this.debug("Using loaded value.");
         return loadedValue;
      } else {
         T cachedValue = this.cache.get(key);
         if (cachedValue != null) {
            this.debug("Using cached value.");
            return cachedValue;
         } else {
            T fallbackValue = fallbackValueSupplier.get();
            if (fallbackValue != null) {
               this.cache.put(key, fallbackValue);
               if (this.savingStrategy.writesThroughOnCreate()) {
                  this.savingStrategy.save(fallbackValue);
               } else {
                  this.dirtyKeys.add(key);
               }

               this.debug("Using fallback value.");
            }

            return fallbackValue;
         }
      }
   }

   public CompletableFuture<T> getOrCreateAsync(String key, Function<String, T> loader, Supplier<T> fallbackValueSupplier, DatabaseWorker databaseWorker) {
      T cachedValue = this.cache.get(key);
      return cachedValue != null
         ? CompletableFuture.completedFuture(cachedValue)
         : databaseWorker.query(() -> this.getOrCreate(key, loader, fallbackValueSupplier));
   }

   public T get(String key) {
      if (this.defaultLoader == null) {
         throw new IllegalStateException("No default loader configured");
      } else {
         return this.get(key, this.defaultLoader);
      }
   }

   public CompletableFuture<T> getAsync(String key, DatabaseWorker databaseWorker) {
      if (this.defaultLoader == null) {
         throw new IllegalStateException("No default loader configured");
      } else {
         return this.getAsync(key, this.defaultLoader, databaseWorker);
      }
   }

   public T peek(String key) {
      return this.cache.get(key);
   }

   public void cacheLoadedValue(String key, T data) {
      if (data != null) {
         this.cache.putIfAbsent(key, data);
      }
   }

   public void update(String key, T data) {
      this.cache.put(key, data);
      if (this.savingStrategy.writesThrough()) {
         this.savingStrategy.save(data);
         this.dirtyKeys.remove(key);
      } else {
         this.dirtyKeys.add(key);
      }
   }

   public void updateAll(Map<String, T> data) {
      this.cache.putAll(data);
      if (this.savingStrategy.writesThrough()) {
         this.savingStrategy.saveAll(data.values());
         this.dirtyKeys.removeAll(data.keySet());
      } else {
         this.dirtyKeys.addAll(data.keySet());
      }
   }

   public void flush() {
      this.flushDirty();
   }

   public void shutdown() {
      if (this.flushScheduler != null) {
         this.flushScheduler.shutdown();
      }

      this.flushDirty();
   }

   public void clear() {
      this.cache.clear();
      this.dirtyKeys.clear();
   }

   private void flushDirty() {
      if (!this.dirtyKeys.isEmpty()) {
         Map<String, T> dirtySnapshot = new LinkedHashMap<>();

         for (String key : this.dirtyKeys) {
            T value = this.cache.get(key);
            if (value != null) {
               dirtySnapshot.put(key, value);
            }
         }

         if (dirtySnapshot.isEmpty()) {
            this.dirtyKeys.clear();
         } else {
            Collection<T> dirtyValues = dirtySnapshot.values();
            this.savingStrategy.saveAll(dirtyValues);
            this.dirtyKeys.removeAll(dirtySnapshot.keySet());
         }
      }
   }

   private ScheduledExecutorService createFlushScheduler(Long flushInterval, TimeUnit flushUnit) {
      if (flushInterval != null && flushUnit != null && flushInterval > 0L) {
         ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(runnable -> {
            Thread thread = new Thread(runnable);
            thread.setName("emf-data-flush-" + THREAD_COUNTER.getAndIncrement());
            thread.setDaemon(true);
            return thread;
         });
         scheduler.scheduleAtFixedRate(this::flushDirty, flushInterval, flushInterval, flushUnit);
         return scheduler;
      } else {
         return null;
      }
   }

   private void debug(String message) {
      try {
         EvenMoreFish plugin = EvenMoreFish.getInstance();
         plugin.debug(message);
      } catch (RuntimeException var3) {
      }
   }
}
