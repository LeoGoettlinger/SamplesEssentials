package net.kyori.adventure.text;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnknownNullability;

public interface VirtualComponentRenderer<C> {
   @UnknownNullability
   ComponentLike apply(@NotNull final C context);

   @NotNull
   default String fallbackString() {
      return "";
   }
}
