package de.theredend2000.advancedhunt.lib.kyori.adventure.sound;

import de.theredend2000.advancedhunt.lib.kyori.adventure.key.Key;
import de.theredend2000.advancedhunt.lib.kyori.adventure.util.ShadyPines;
import java.util.Objects;
import java.util.OptionalLong;
import java.util.function.Supplier;
import org.jetbrains.annotations.Range;
import org.jspecify.annotations.Nullable;

abstract sealed class SoundImpl implements Sound permits SoundImpl.Eager, SoundImpl.Lazy {
   static final Sound.Emitter EMITTER_SELF = new Sound.Emitter() {
      @Override
      public String toString() {
         return "SelfSoundEmitter";
      }
   };
   private final Sound.Source source;
   private final float volume;
   private final float pitch;
   private final OptionalLong seed;
   @Nullable
   private SoundStop stop;

   SoundImpl(final Sound.Source source, final float volume, final float pitch, final OptionalLong seed) {
      this.source = source;
      this.volume = volume;
      this.pitch = pitch;
      this.seed = seed;
   }

   @Override
   public Sound.Source source() {
      return this.source;
   }

   @Override
   public float volume() {
      return this.volume;
   }

   @Override
   public float pitch() {
      return this.pitch;
   }

   @Override
   public OptionalLong seed() {
      return this.seed;
   }

   @Override
   public SoundStop asStop() {
      if (this.stop == null) {
         this.stop = SoundStop.namedOnSource(this.name(), this.source());
      }

      return this.stop;
   }

   @Override
   public boolean equals(@Nullable final Object other) {
      if (this == other) {
         return true;
      } else {
         return !(other instanceof SoundImpl that)
            ? false
            : this.name().equals(that.name())
               && this.source == that.source
               && ShadyPines.equals(this.volume, that.volume)
               && ShadyPines.equals(this.pitch, that.pitch)
               && this.seed.equals(that.seed);
      }
   }

   @Override
   public int hashCode() {
      int result = this.name().hashCode();
      result = 31 * result + this.source.hashCode();
      result = 31 * result + Float.hashCode(this.volume);
      result = 31 * result + Float.hashCode(this.pitch);
      return 31 * result + this.seed.hashCode();
   }

   @Override
   public String toString() {
      return "SoundImpl{name=" + this.name() + ", source=" + this.source + ", volume=" + this.volume + ", pitch=" + this.pitch + ", seed=" + this.seed + "}";
   }

   static final class BuilderImpl implements Sound.Builder {
      private static final float DEFAULT_VOLUME = 1.0F;
      private static final float DEFAULT_PITCH = 1.0F;
      @Nullable
      private Key eagerType;
      @Nullable
      private Supplier<? extends Sound.Type> lazyType;
      private Sound.Source source = Sound.Source.MASTER;
      private float volume = 1.0F;
      private float pitch = 1.0F;
      private OptionalLong seed = OptionalLong.empty();

      BuilderImpl() {
      }

      BuilderImpl(final Sound existing) {
         if (existing instanceof SoundImpl.Eager eager) {
            this.type(eager.name);
         } else {
            if (!(existing instanceof SoundImpl.Lazy lazy)) {
               throw new IllegalArgumentException("Unknown sound type " + existing + ", must be Eager or Lazy");
            }

            this.type(lazy.supplier);
         }

         this.source(existing.source()).volume(existing.volume()).pitch(existing.pitch()).seed(existing.seed());
      }

      @Override
      public Sound.Builder type(final Key type) {
         this.eagerType = Objects.requireNonNull(type, "type");
         this.lazyType = null;
         return this;
      }

      @Override
      public Sound.Builder type(final Sound.Type type) {
         this.eagerType = Objects.requireNonNull(Objects.requireNonNull(type, "type").key(), "type.key()");
         this.lazyType = null;
         return this;
      }

      @Override
      public Sound.Builder type(final Supplier<? extends Sound.Type> typeSupplier) {
         this.lazyType = Objects.requireNonNull(typeSupplier, "typeSupplier");
         this.eagerType = null;
         return this;
      }

      @Override
      public Sound.Builder source(final Sound.Source source) {
         this.source = Objects.requireNonNull(source, "source");
         return this;
      }

      @Override
      public Sound.Builder source(final Sound.Source.Provider source) {
         return this.source(source.soundSource());
      }

      @Override
      public Sound.Builder volume(@Range(from = 0L,to = 2147483647L) final float volume) {
         this.volume = volume;
         return this;
      }

      @Override
      public Sound.Builder pitch(@Range(from = -1L,to = 1L) final float pitch) {
         this.pitch = pitch;
         return this;
      }

      @Override
      public Sound.Builder seed(final long seed) {
         this.seed = OptionalLong.of(seed);
         return this;
      }

      @Override
      public Sound.Builder seed(final OptionalLong seed) {
         this.seed = Objects.requireNonNull(seed, "seed");
         return this;
      }

      public Sound build() {
         if (this.eagerType != null) {
            return new SoundImpl.Eager(this.eagerType, this.source, this.volume, this.pitch, this.seed);
         } else if (this.lazyType != null) {
            return new SoundImpl.Lazy(this.lazyType, this.source, this.volume, this.pitch, this.seed);
         } else {
            throw new IllegalStateException("A sound type must be provided to build a sound");
         }
      }
   }

   static final class Eager extends SoundImpl {
      final Key name;

      Eager(final Key name, final Sound.Source source, final float volume, final float pitch, final OptionalLong seed) {
         super(source, volume, pitch, seed);
         this.name = name;
      }

      @Override
      public Key name() {
         return this.name;
      }
   }

   static final class Lazy extends SoundImpl {
      final Supplier<? extends Sound.Type> supplier;

      Lazy(final Supplier<? extends Sound.Type> supplier, final Sound.Source source, final float volume, final float pitch, final OptionalLong seed) {
         super(source, volume, pitch, seed);
         this.supplier = supplier;
      }

      @Override
      public Key name() {
         return this.supplier.get().key();
      }
   }
}
