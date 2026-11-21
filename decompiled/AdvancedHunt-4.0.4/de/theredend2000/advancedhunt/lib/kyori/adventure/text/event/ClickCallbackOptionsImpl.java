package de.theredend2000.advancedhunt.lib.kyori.adventure.text.event;

import java.time.Duration;
import java.time.temporal.TemporalAmount;
import java.util.Objects;

record ClickCallbackOptionsImpl(int uses, Duration lifetime) implements ClickCallback.Options {
   static final ClickCallback.Options DEFAULT = new ClickCallbackOptionsImpl.BuilderImpl().build();

   static final class BuilderImpl implements ClickCallback.Options.Builder {
      private static final int DEFAULT_USES = 1;
      private int uses;
      private Duration lifetime;

      BuilderImpl() {
         this.uses = 1;
         this.lifetime = ClickCallback.DEFAULT_LIFETIME;
      }

      BuilderImpl(final ClickCallback.Options existing) {
         this.uses = existing.uses();
         this.lifetime = existing.lifetime();
      }

      public ClickCallback.Options build() {
         return new ClickCallbackOptionsImpl(this.uses, this.lifetime);
      }

      @Override
      public ClickCallback.Options.Builder uses(final int uses) {
         this.uses = uses;
         return this;
      }

      @Override
      public ClickCallback.Options.Builder lifetime(final TemporalAmount lifetime) {
         this.lifetime = lifetime instanceof Duration duration ? duration : Duration.from(Objects.requireNonNull(lifetime, "lifetime"));
         return this;
      }
   }
}
