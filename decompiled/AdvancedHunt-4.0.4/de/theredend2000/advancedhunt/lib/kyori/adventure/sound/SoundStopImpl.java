package de.theredend2000.advancedhunt.lib.kyori.adventure.sound;

import de.theredend2000.advancedhunt.lib.kyori.adventure.key.Key;
import org.jspecify.annotations.Nullable;

record SoundStopImpl(@Nullable Key sound, @Nullable Sound.Source source) implements SoundStop {
   static final SoundStopImpl ALL = new SoundStopImpl(null, null);
}
