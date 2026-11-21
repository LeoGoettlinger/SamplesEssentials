package com.sk89q.worldedit.util.lifecycle;

import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Predicate;

public interface Lifecycled<T> {
   Optional<T> value();

   default T valueOrThrow() throws IllegalStateException {
      return this.value().orElseThrow(() -> new IllegalStateException("Currently invalid"));
   }

   default boolean isValid() {
      return this.value().isPresent();
   }

   Lifecycled.Events<T> events();

   default <U> Lifecycled<U> map(Function<T, U> mapper) {
      return new MapLifecycled<>(this, mapper);
   }

   default Lifecycled<T> filter(Predicate<T> filterer) {
      SimpleLifecycled<T> downstream = SimpleLifecycled.invalid();
      this.events().onInvalidated(downstream, (d, lifecycled) -> d.invalidate());
      this.events().onNewValue(downstream, (d, lifecycled) -> {
         T value = lifecycled.valueOrThrow();
         if (filterer.test(value)) {
            d.newValue(value);
         }
      });
      return downstream;
   }

   default <U> Lifecycled<U> flatMap(Function<T, Lifecycled<U>> mapper) {
      return new FlatMapLifecycled<>(this, mapper);
   }

   public interface Events<T> {
      <O> void onNewValue(O owner, BiConsumer<O, ? super Lifecycled<T>> callback);

      <O> void onInvalidated(O owner, BiConsumer<O, ? super Lifecycled<T>> callback);
   }
}
