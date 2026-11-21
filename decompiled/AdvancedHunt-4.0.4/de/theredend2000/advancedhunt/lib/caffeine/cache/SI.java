package de.theredend2000.advancedhunt.lib.caffeine.cache;

import java.lang.ref.ReferenceQueue;

class SI<K, V> extends BoundedLocalCache<K, V> {
   static final LocalCacheFactory FACTORY = SI::new;
   final ReferenceQueue<V> valueReferenceQueue = new ReferenceQueue<>();

   SI(Caffeine<K, V> var1, AsyncCacheLoader<? super K, V> var2, boolean var3) {
      super(var1, (AsyncCacheLoader<K, V>)var2, var3);
   }

   @Override
   protected final ReferenceQueue<V> valueReferenceQueue() {
      return this.valueReferenceQueue;
   }

   @Override
   public final boolean collectValues() {
      return true;
   }
}
