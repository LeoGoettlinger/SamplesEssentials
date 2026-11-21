package xyz.lychee.lagfixer.libs.kyori.adventure.text.flattener;

import org.jetbrains.annotations.NotNull;
import xyz.lychee.lagfixer.libs.kyori.adventure.text.format.Style;

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
