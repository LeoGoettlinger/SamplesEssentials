package org.jdbi.v3.core.collector;

import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.OptionalLong;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;
import org.jdbi.v3.core.internal.UtilityClassException;

public class OptionalCollectors {
   private OptionalCollectors() {
      throw new UtilityClassException();
   }

   public static <T> Collector<T, ?, Optional<T>> toOptional() {
      return toOptional(Optional::empty, Optional::of);
   }

   public static Collector<Integer, ?, OptionalInt> toOptionalInt() {
      return toOptional(OptionalInt::empty, OptionalInt::of);
   }

   public static Collector<Long, ?, OptionalLong> toOptionalLong() {
      return toOptional(OptionalLong::empty, OptionalLong::of);
   }

   public static Collector<Double, ?, OptionalDouble> toOptionalDouble() {
      return toOptional(OptionalDouble::empty, OptionalDouble::of);
   }

   public static <T, O> Collector<T, ?, O> toOptional(Supplier<O> empty, Function<T, O> factory) {
      return Collector.of(() -> new OptionalBuilder<>(empty, factory), OptionalBuilder::set, OptionalBuilder::combine, OptionalBuilder::build);
   }
}
