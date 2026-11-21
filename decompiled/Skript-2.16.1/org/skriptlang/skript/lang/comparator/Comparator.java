package org.skriptlang.skript.lang.comparator;

@FunctionalInterface
public interface Comparator<T1, T2> {
   Relation compare(T1 var1, T2 var2);

   default boolean supportsOrdering() {
      return false;
   }

   default boolean supportsInversion() {
      return true;
   }
}
