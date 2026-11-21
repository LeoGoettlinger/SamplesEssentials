package de.theredend2000.advancedhunt.lib.caffeine.cache;

final class WeakInterner<E> implements Interner<E> {
   final BoundedLocalCache<E, Boolean> cache = Caffeine.newWeakInterner();

   @Override
   public E intern(E sample) {
      Boolean value;
      do {
         E canonical = this.cache.getKey(sample);
         if (canonical != null) {
            return canonical;
         }

         value = this.cache.putIfAbsent(sample, true);
      } while (value != null);

      return sample;
   }
}
