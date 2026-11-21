package com.earth2me.essentials.libs.kyori.adventure.internal;

import com.earth2me.essentials.libs.kyori.examination.Examinable;
import com.earth2me.essentials.libs.kyori.examination.string.StringExaminer;
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
