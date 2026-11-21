package com.earth2me.essentials.libs.kyori.adventure.text.flattener;

import com.earth2me.essentials.libs.kyori.adventure.text.format.Style;
import org.jetbrains.annotations.NotNull;

@FunctionalInterface
public interface FlattenerListener {
   default void pushStyle(@NotNull final Style style) {
   }

   void component(@NotNull final String text);

   default boolean shouldContinue() {
      return true;
   }

   default void popStyle(@NotNull final Style style) {
   }
}
