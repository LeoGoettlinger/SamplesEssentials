package com.sk89q.worldedit.util.lifecycle;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import javax.annotation.Nullable;

class MapLifecycled<T, U> implements Lifecycled<U> {
   private final LifecycledCallbackHandler<U> events = new LifecycledCallbackHandler<>(this);
   private final Lifecycled<T> upstream;
   private final Function<T, U> mapper;
   @Nullable
   private U cache;
   private boolean computable;

   MapLifecycled(Lifecycled<T> upstream, Function<T, U> mapper) {
      this.upstream = upstream;
      this.mapper = mapper;
      upstream.events().onInvalidated(this, (this$, __) -> {
         boolean fire = this$.computable;
         this$.cache = null;
         this$.computable = false;
         if (fire) {
            this$.events.fireInvalidated();
         }
      });
      upstream.events().onNewValue(this, (this$, __) -> {
         boolean fire = !this$.computable;
         this$.computable = true;
         if (fire) {
            this$.events.fireOnNewValue();
         }
      });
   }

   private void compute() {
      T value = this.upstream.value().orElseThrow(() -> new AssertionError("Upstream lost value without calling onInvalidated event"));
      this.cache = Objects.requireNonNull(this.mapper.apply(value), "Mapper cannot produce null");
   }

   @Override
   public Optional<U> value() {
      if (!this.computable) {
         return Optional.empty();
      } else {
         if (this.cache == null) {
            this.compute();
         }

         return Optional.of(this.cache);
      }
   }

   @Override
   public boolean isValid() {
      return this.computable;
   }

   @Override
   public Lifecycled.Events<U> events() {
      return this.events;
   }
}
