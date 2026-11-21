package su.nightexpress.nightcore.db.data.layer;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import su.nightexpress.nightcore.NightPlugin;
import su.nightexpress.nightcore.db.data.AbstractBaseDataManager;
import su.nightexpress.nightcore.db.state.Stateful;

public abstract class AbstractTwoLevelDataManager<P, K, V extends Stateful> extends AbstractBaseDataManager<V> {
   protected final Function<V, P> parentKeyFunction;
   protected final Function<V, K> dataKeyFunction;
   protected final TwoLevelDataCache<P, K, V> cache;

   protected AbstractTwoLevelDataManager(@NonNull NightPlugin plugin, @NonNull Function<V, P> parentKeyFunction, @NonNull Function<V, K> dataKeyFunction) {
      super(plugin);
      this.parentKeyFunction = parentKeyFunction;
      this.dataKeyFunction = dataKeyFunction;
      this.cache = TwoLevelDataCache.create(parentKeyFunction, dataKeyFunction);
   }

   @NonNull
   public TwoLevelDataCache<P, K, V> getCache() {
      return this.cache;
   }

   @NonNull
   public abstract P getGlobalKey();

   public void loadGlobalAndActivate() {
      this.selectByKey(this.getGlobalKey()).forEach(this::cachePermanently);
      this.activate(this.getGlobalKey());
   }

   public void activate(@NonNull P parentKey) {
      this.cache.activateHolder(parentKey);
      this.cache.getAllByOwner(parentKey).forEach(Stateful::setPermanentCache);
   }

   public void deactivate(@NonNull P parentKey) {
      this.cache.deactivateHolder(parentKey);
      this.cache.getAllByOwner(parentKey).forEach(x$0 -> this.setCacheTime((V)x$0));
   }

   public boolean isActive(@NonNull P parentKey) {
      return this.cache.isHolderActive(parentKey);
   }

   public boolean isActiveWithGlobal(@NonNull P parentKey) {
      return this.isActive(this.getGlobalKey()) && this.isActive(parentKey);
   }

   @NonNull
   public CompletableFuture<Optional<V>> loadAsync(@NonNull P parentKey, @NonNull K dataKey) {
      return this.loadOrCreateAndCacheAsync(parentKey, dataKey, false, false);
   }

   @NonNull
   public CompletableFuture<Optional<V>> loadOrCreateAsync(@NonNull P parentKey, @NonNull K dataKey) {
      return this.loadOrCreateAndCacheAsync(parentKey, dataKey, true, false);
   }

   @NonNull
   public CompletableFuture<Optional<V>> loadAndCacheAsync(@NonNull P parentKey, @NonNull K dataKey) {
      return this.loadOrCreateAndCacheAsync(parentKey, dataKey, false, true);
   }

   @NonNull
   public CompletableFuture<Optional<V>> loadOrCreateAndCacheAsync(@NonNull P parentKey, @NonNull K dataKey) {
      return this.loadOrCreateAndCacheAsync(parentKey, dataKey, true, true);
   }

   @NonNull
   protected CompletableFuture<Optional<V>> loadOrCreateAndCacheAsync(@NonNull P parentKey, @NonNull K dataKey, boolean create, boolean needCache) {
      V cached = this.getData(parentKey, dataKey);
      return cached != null ? CompletableFuture.completedFuture(Optional.of(cached)) : CompletableFuture.supplyAsync(() -> {
         V data = this.selectByKeyAndId(parentKey, dataKey);
         if (data == null) {
            if (!create) {
               return Optional.empty();
            }

            data = this.createData(parentKey, dataKey);
            data.markDirty();
         }

         if (needCache) {
            this.cacheNewData(data);
         }

         return Optional.of(data);
      }, this.asyncExecutor);
   }

   public void cacheNewData(@NonNull V data) {
      P parentKey = this.parentKeyFunction.apply(data);
      if (this.isActive(parentKey)) {
         this.cachePermanently(data);
      } else {
         this.cacheTemporary(data);
      }
   }

   @Nullable
   public V getGlobalData(@NonNull K dataKey) {
      return this.getData(this.getGlobalKey(), dataKey);
   }

   @NonNull
   public V getGlobalDataOrCreate(@NonNull K dataKey) {
      return this.getDataOrCreate(this.getGlobalKey(), dataKey);
   }

   @NonNull
   public V getGlobalDataOrCreateAndCache(@NonNull K dataKey) {
      return this.getDataOrCreateAndCache(this.getGlobalKey(), dataKey);
   }

   @Nullable
   public V getData(@NonNull P parentKey, @NonNull K dataKey) {
      V data = this.cache.get(parentKey, dataKey);
      if (data != null && data.getCachedUntil() >= 0L) {
         this.setCacheTime(data);
      }

      return data;
   }

   @NonNull
   public V getDataOrCreate(@NonNull P parentKey, @NonNull K dataKey) {
      return this.getDataOrCreateAndCache(parentKey, dataKey, false);
   }

   @NonNull
   public V getDataOrCreateAndCache(@NonNull P parentKey, @NonNull K dataKey) {
      return this.getDataOrCreateAndCache(parentKey, dataKey, true);
   }

   @NonNull
   protected V getDataOrCreateAndCache(@NonNull P parentKey, @NonNull K dataKey, boolean needCache) {
      V cached = this.getData(parentKey, dataKey);
      if (cached == null) {
         cached = this.createData(parentKey, dataKey);
         cached.markDirty();
         if (needCache) {
            this.cacheNewData(cached);
         }
      }

      return cached;
   }

   @NonNull
   protected abstract V createData(@NonNull P var1, @NonNull K var2);

   @NonNull
   protected abstract List<V> selectByKey(@NonNull P var1);

   @Nullable
   protected abstract V selectByKeyAndId(@NonNull P var1, @NonNull K var2);
}
