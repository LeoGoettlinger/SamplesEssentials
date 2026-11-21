package me.clip.placeholderapi.libs.kyori.adventure.internal;

import me.clip.placeholderapi.libs.kyori.examination.Examinable;
import me.clip.placeholderapi.libs.kyori.examination.string.StringExaminer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.ApiStatus.Internal;

@Internal
public final class Internals {
   private Internals() {
   }

   @NotNull
   public static String toString(@NotNull final Examinable examinable) {
      return examinable.examine(StringExaminer.simpleEscaping());
   }
}
