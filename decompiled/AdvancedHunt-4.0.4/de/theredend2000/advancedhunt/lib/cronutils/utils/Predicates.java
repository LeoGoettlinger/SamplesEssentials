package de.theredend2000.advancedhunt.lib.cronutils.utils;

import java.util.function.Predicate;

public class Predicates {
   private Predicates() {
   }

   public static <T> Predicate<T> not(Predicate<T> t) {
      return t.negate();
   }
}
