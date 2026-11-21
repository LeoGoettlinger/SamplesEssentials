package net.kyori.adventure.sound;

import net.kyori.adventure.key.Key;
import org.jspecify.annotations.Nullable;

record SoundStopImpl(@Nullable Key sound, @Nullable Sound.Source source) implements SoundStop {
   static final SoundStopImpl ALL = new SoundStopImpl(null, null);
}
