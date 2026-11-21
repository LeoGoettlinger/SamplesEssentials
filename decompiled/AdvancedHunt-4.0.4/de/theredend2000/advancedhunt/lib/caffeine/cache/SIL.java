package de.theredend2000.advancedhunt.lib.caffeine.cache;

class SIL<K, V> extends SI<K, V> {
   static final LocalCacheFactory FACTORY = SIL::new;
   final RemovalListener<K, V> removalListener;

   SIL(Caffeine<K, V> var1, AsyncCacheLoader<? super K, V> var2, boolean var3) {
      super(var1, var2, var3);
      this.removalListener = var1.getRemovalListener(var3);
   }

   @Override
   public final RemovalListener<K, V> removalListener() {
      return this.removalListener;
   }
}
