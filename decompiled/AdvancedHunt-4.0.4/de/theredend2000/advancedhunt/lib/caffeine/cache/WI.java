package de.theredend2000.advancedhunt.lib.caffeine.cache;

import java.lang.ref.ReferenceQueue;

class WI<K, V> extends BoundedLocalCache<K, V> {
   static final LocalCacheFactory FACTORY = WI::new;
   final ReferenceQueue<K> keyReferenceQueue = new ReferenceQueue<>();
   final ReferenceQueue<V> valueReferenceQueue = new ReferenceQueue<>();

   WI(Caffeine<K, V> var1, AsyncCacheLoader<? super K, V> var2, boolean var3) {
      super(var1, (AsyncCacheLoader<K, V>)var2, var3);
   }

   @Override
   protected final ReferenceQueue<K> keyReferenceQueue() {
      return this.keyReferenceQueue;
   }

   @Override
   public final boolean collectKeys() {
      return true;
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
