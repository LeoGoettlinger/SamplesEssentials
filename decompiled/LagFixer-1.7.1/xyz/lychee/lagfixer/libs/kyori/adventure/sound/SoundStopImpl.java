package xyz.lychee.lagfixer.libs.kyori.adventure.sound;

import java.util.Objects;
import java.util.stream.Stream;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.lychee.lagfixer.libs.kyori.adventure.internal.Internals;
import xyz.lychee.lagfixer.libs.kyori.adventure.key.Key;
import xyz.lychee.lagfixer.libs.kyori.examination.ExaminableProperty;

abstract class SoundStopImpl implements SoundStop {
   static final SoundStop ALL = new SoundStopImpl(null) {
      @Nullable
      @Override
      public Key sound() {
         return null;
      }
   };
   @Nullable
   private final Sound.Source source;

   SoundStopImpl(@Nullable final Sound.Source source) {
      this.source = source;
   }

   @Nullable
   @Override
   public Sound.Source source() {
      return this.source;
   }

   @Override
   public boolean equals(@Nullable final Object other) {
      if (this == other) {
         return true;
      } else if (!(other instanceof SoundStopImpl)) {
         return false;
      } else {
         SoundStopImpl that = (SoundStopImpl)other;
         return Objects.equals(this.sound(), that.sound()) && Objects.equals(this.source, that.source);
      }
   }

   @Override
   public int hashCode() {
      int result = Objects.hashCode(this.sound());
      return 31 * result + Objects.hashCode(this.source);
   }

   @NotNull
   @Override
   public Stream<? extends ExaminableProperty> examinableProperties() {
      return Stream.of(ExaminableProperty.of("name", this.sound()), ExaminableProperty.of("source", this.source));
   }

   @Override
   public String toString() {
      return Internals.toString(this);
   }
}
