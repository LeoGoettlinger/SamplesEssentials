package su.nightexpress.nightcore.db.data.layer;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import su.nightexpress.nightcore.NightPlugin;
import su.nightexpress.nightcore.db.data.AbstractBaseDataManager;
import su.nightexpress.nightcore.db.state.Stateful;

public abstract class AbstractOneLevelDataManager<K, V extends Stateful> extends AbstractBaseDataManager<V> {
   protected final Function<V, K> dataKeyFunction;
   protected final OneLevelDataCache<K, V> cache;

   protected AbstractOneLevelDataManager(@NonNull NightPlugin plugin, @NonNull Function<V, K> dataKeyFunction) {
      super(plugin);
      this.dataKeyFunction = dataKeyFunction;
      this.cache = OneLevelDataCache.create(dataKeyFunction);
   }

   @NonNull
   public OneLevelDataCache<K, V> getCache() {
      return this.cache;
   }

   @NonNull
   public CompletableFuture<Optional<V>> loadAsync(@NonNull K key) {
      return this.loadOrCreateAndCacheAsync(key, false, false);
   }

   @NonNull
   public CompletableFuture<Optional<V>> loadOrCreateAsync(@NonNull K key) {
      return this.loadOrCreateAndCacheAsync(key, true, false);
   }

   @NonNull
   public CompletableFuture<Optional<V>> loadAndCacheAsync(@NonNull K key) {
      return this.loadOrCreateAndCacheAsync(key, false, true);
   }

   @NonNull
   public CompletableFuture<Optional<V>> loadOrCreateAndCacheAsync(@NonNull K key) {
      return this.loadOrCreateAndCacheAsync(key, true, true);
   }

   @NonNull
   protected CompletableFuture<Optional<V>> loadOrCreateAndCacheAsync(@NonNull K key, boolean create, boolean needCache) {
      V cached = this.getData(key);
      return cached != null ? CompletableFuture.completedFuture(Optional.of(cached)) : CompletableFuture.supplyAsync(() -> {
         V data = this.selectByKey(key);
         if (data == null) {
            if (!create) {
               return Optional.empty();
            }

            data = this.createData(key);
            data.markDirty();
         }

         if (needCache) {
            this.cacheNewData(data);
         }

         return Optional.of(data);
      }, this.asyncExecutor);
   }

   @Nullable
   public V getData(@NonNull K key) {
      V data = this.cache.get(key);
      if (data != null && data.getCachedUntil() >= 0L) {
         this.setCacheTime(data);
      }

      return data;
   }

   @NonNull
   public V getDataOrCreate(@NonNull K key) {
      return this.getDataOrCreateAndCache(key, false);
   }

   @NonNull
   public V getDataOrCreateAndCache(@NonNull K key) {
      return this.getDataOrCreateAndCache(key, true);
   }

   @NonNull
   protected V getDataOrCreateAndCache(@NonNull K key, boolean needCache) {
      V cached = this.getData(key);
      if (cached == null) {
         cached = this.createData(key);
         cached.markDirty();
         if (needCache) {
            this.cacheNewData(cached);
         }
      }

      return cached;
   }

   public abstract void cacheNewData(@NonNull V var1);

   @NonNull
   protected abstract V createData(@NonNull K var1);

   @Nullable
   protected abstract V selectByKey(@NonNull K var1);
}
