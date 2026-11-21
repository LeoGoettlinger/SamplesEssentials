package xyz.lychee.lagfixer.libs.kyori.adventure.internal;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import xyz.lychee.lagfixer.libs.kyori.examination.Examinable;
import xyz.lychee.lagfixer.libs.kyori.examination.string.StringExaminer;

@ApiStatus.Internal
public final class Internals {
   private Internals() {
   }

   @NotNull
   public static String toString(@NotNull final Examinable examinable) {
      return examinable.examine(StringExaminer.simpleEscaping());
   }
}
