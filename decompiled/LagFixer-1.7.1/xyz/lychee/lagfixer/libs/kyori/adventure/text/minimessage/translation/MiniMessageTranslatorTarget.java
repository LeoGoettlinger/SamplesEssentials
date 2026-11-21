package xyz.lychee.lagfixer.libs.kyori.adventure.text.minimessage.translation;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnknownNullability;
import xyz.lychee.lagfixer.libs.kyori.adventure.pointer.Pointered;
import xyz.lychee.lagfixer.libs.kyori.adventure.text.ComponentLike;
import xyz.lychee.lagfixer.libs.kyori.adventure.text.VirtualComponentRenderer;

final class MiniMessageTranslatorTarget implements VirtualComponentRenderer<Void> {
   private final Pointered pointered;

   MiniMessageTranslatorTarget(@NotNull final Pointered pointered) {
      this.pointered = pointered;
   }

   @NotNull
   Pointered pointered() {
      return this.pointered;
   }

   @UnknownNullability
   public ComponentLike apply(@NotNull final Void context) {
      return null;
   }
}
