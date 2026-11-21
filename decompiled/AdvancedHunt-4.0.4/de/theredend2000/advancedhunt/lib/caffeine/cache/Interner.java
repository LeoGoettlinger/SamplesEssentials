package de.theredend2000.advancedhunt.lib.caffeine.cache;

import org.jspecify.annotations.NullMarked;

@NullMarked
@FunctionalInterface
public interface Interner<E> {
   E intern(E sample);

   static <E> Interner<E> newStrongInterner() {
      return new StrongInterner<>();
   }

   static <E> Interner<E> newWeakInterner() {
      return new WeakInterner<>();
   }
}
