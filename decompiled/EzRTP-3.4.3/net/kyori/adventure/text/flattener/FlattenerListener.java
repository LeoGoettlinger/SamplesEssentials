package net.kyori.adventure.text.flattener;

import net.kyori.adventure.text.format.Style;

@FunctionalInterface
public interface FlattenerListener {
   default void pushStyle(final Style style) {
   }

   void component(final String text);

   default boolean shouldContinue() {
      return true;
   }

   default void popStyle(final Style style) {
   }
}
