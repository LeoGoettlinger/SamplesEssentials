package org.apache.commons.lang3.function;

import java.util.function.Predicate;

public class Predicates {
   private static final Predicate<?> TRUE = t -> true;
   private static final Predicate<?> FALSE = t -> false;

   public static <T> Predicate<T> falsePredicate() {
      return (Predicate<T>)FALSE;
   }

   public static <T> Predicate<T> truePredicate() {
      return (Predicate<T>)TRUE;
   }

   private Predicates() {
   }
}
