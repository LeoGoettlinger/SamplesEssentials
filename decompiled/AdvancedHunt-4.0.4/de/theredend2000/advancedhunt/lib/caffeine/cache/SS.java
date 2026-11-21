package de.theredend2000.advancedhunt.lib.caffeine.cache;

class SS<K, V> extends BoundedLocalCache<K, V> {
   static final LocalCacheFactory FACTORY = SS::new;

   SS(Caffeine<K, V> var1, AsyncCacheLoader<? super K, V> var2, boolean var3) {
      super(var1, (AsyncCacheLoader<K, V>)var2, var3);
   }
}
