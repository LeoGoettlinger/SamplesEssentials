package org.jdbi.v3.core.collector;

import java.util.function.Function;
import java.util.function.Supplier;

class OptionalBuilder<T, O> {
   private final Supplier<O> empty;
   private final Function<T, O> factory;
   private boolean set;
   private T value;

   OptionalBuilder(Supplier<O> empty, Function<T, O> factory) {
      this.empty = empty;
      this.factory = factory;
   }

   void set(T newValue) {
      if (this.set) {
         throw tooManyValues(this.value, newValue);
      } else {
         this.value = newValue;
         this.set = true;
      }
   }

   O build() {
      return this.value == null ? this.empty.get() : this.factory.apply(this.value);
   }

   static <T, OPT_T> OptionalBuilder<T, OPT_T> combine(OptionalBuilder<T, OPT_T> left, OptionalBuilder<T, OPT_T> right) {
      if (left.set && right.set) {
         throw tooManyValues(left.value, right.value);
      } else {
         return left.set ? left : right;
      }
   }

   private static <T> IllegalStateException tooManyValues(T first, T second) {
      return new IllegalStateException(String.format("Multiple values for optional: [%s, %s, ...]", stringify(first), stringify(second)));
   }

   private static String stringify(Object value) {
      return value == null ? null : "'" + value + "'";
   }
}
