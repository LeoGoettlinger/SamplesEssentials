package de.theredend2000.advancedhunt.lib.kyori.adventure.sound;

import de.theredend2000.advancedhunt.lib.kyori.adventure.key.Key;
import java.util.Objects;
import java.util.function.Supplier;
import org.jspecify.annotations.Nullable;

public sealed interface SoundStop permits SoundStopImpl {
   static SoundStop all() {
      return SoundStopImpl.ALL;
   }

   static SoundStop named(final Key sound) {
      Objects.requireNonNull(sound, "sound");
      return new SoundStopImpl(sound, null);
   }

   static SoundStop named(final Sound.Type sound) {
      Objects.requireNonNull(sound, "sound");
      return new SoundStopImpl(sound.key(), null);
   }

   static SoundStop named(final Supplier<? extends Sound.Type> sound) {
      Objects.requireNonNull(sound, "sound");
      return new SoundStopImpl(sound.get().key(), null);
   }

   static SoundStop source(final Sound.Source source) {
      Objects.requireNonNull(source, "source");
      return new SoundStopImpl(null, source);
   }

   static SoundStop namedOnSource(final Key sound, final Sound.Source source) {
      Objects.requireNonNull(sound, "sound");
      Objects.requireNonNull(source, "source");
      return new SoundStopImpl(sound, source);
   }

   static SoundStop namedOnSource(final Sound.Type sound, final Sound.Source source) {
      Objects.requireNonNull(sound, "sound");
      return namedOnSource(sound.key(), source);
   }

   static SoundStop namedOnSource(final Supplier<? extends Sound.Type> sound, final Sound.Source source) {
      Objects.requireNonNull(sound, "sound");
      Objects.requireNonNull(source, "source");
      return new SoundStopImpl(sound.get().key(), source);
   }

   @Nullable
   Key sound();

   @Nullable
   Sound.Source source();
}
