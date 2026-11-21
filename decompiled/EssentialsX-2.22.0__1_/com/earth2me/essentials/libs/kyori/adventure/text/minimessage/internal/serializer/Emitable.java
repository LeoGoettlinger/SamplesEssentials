package com.earth2me.essentials.libs.kyori.adventure.text.minimessage.internal.serializer;

import com.earth2me.essentials.libs.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@FunctionalInterface
public interface Emitable {
   void emit(@NotNull final TokenEmitter emitter);

   @Nullable
   default Component substitute() {
      return null;
   }
}
