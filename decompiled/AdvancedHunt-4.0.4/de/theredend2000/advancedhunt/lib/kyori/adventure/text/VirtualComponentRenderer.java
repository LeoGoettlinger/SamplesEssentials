package de.theredend2000.advancedhunt.lib.kyori.adventure.text;

import org.jetbrains.annotations.UnknownNullability;

public interface VirtualComponentRenderer<C> {
   @UnknownNullability
   ComponentLike apply(final C context);

   default String fallbackString() {
      return "";
   }
}
