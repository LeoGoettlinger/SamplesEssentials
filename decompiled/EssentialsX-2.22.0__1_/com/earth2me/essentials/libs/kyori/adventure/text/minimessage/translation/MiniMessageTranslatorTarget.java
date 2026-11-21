package com.earth2me.essentials.libs.kyori.adventure.text.minimessage.translation;

import com.earth2me.essentials.libs.kyori.adventure.pointer.Pointered;
import com.earth2me.essentials.libs.kyori.adventure.text.ComponentLike;
import com.earth2me.essentials.libs.kyori.adventure.text.VirtualComponentRenderer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnknownNullability;

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
