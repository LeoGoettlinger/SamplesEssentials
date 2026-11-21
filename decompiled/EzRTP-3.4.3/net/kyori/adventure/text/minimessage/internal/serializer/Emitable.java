package net.kyori.adventure.text.minimessage.internal.serializer;

import net.kyori.adventure.text.Component;
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
