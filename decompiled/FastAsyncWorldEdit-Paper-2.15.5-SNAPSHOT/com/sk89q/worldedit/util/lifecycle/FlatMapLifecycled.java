package com.sk89q.worldedit.util.lifecycle;

import java.util.Optional;
import java.util.function.Function;
import javax.annotation.Nullable;

class FlatMapLifecycled<T, U> implements Lifecycled<U> {
   private final LifecycledCallbackHandler<U> events = new LifecycledCallbackHandler<>(this);
   private Lifecycled<U> mapped;
   private Token<FlatMapLifecycled<T, U>> mappedToken;
   @Nullable
   private U value;

   FlatMapLifecycled(Lifecycled<T> upstream, Function<T, Lifecycled<U>> mapper) {
      upstream.events().onInvalidated(this, (this$, up) -> {
         boolean fire = this$.value != null;
         this$.value = null;
         this$.mappedToken = null;
         this$.mapped = null;
         if (fire) {
            this$.events.fireInvalidated();
         }
      });
      upstream.events().onNewValue(this, (this$, up) -> {
         this$.mapped = mapper.apply(up.valueOrThrow());
         this$.mappedToken = new Token<>(this$);
         this.mapped.events().onInvalidated(this$.mappedToken, (token, mapped$) -> {
            boolean fire = token.inner.value != null;
            token.inner.value = null;
            if (fire) {
               this$.events.fireInvalidated();
            }
         });
         this.mapped.events().onNewValue(this$.mappedToken, (token, mapped$) -> {
            U newValue = mapped$.valueOrThrow();
            boolean fire = token.inner.value != newValue;
            token.inner.value = newValue;
            if (fire) {
               this$.events.fireOnNewValue();
            }
         });
      });
   }

   @Override
   public Optional<U> value() {
      return Optional.ofNullable(this.value);
   }

   @Override
   public boolean isValid() {
      return this.value != null;
   }

   @Override
   public Lifecycled.Events<U> events() {
      return this.events;
   }
}
