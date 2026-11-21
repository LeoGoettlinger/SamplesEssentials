package xyz.lychee.lagfixer.libs.kyori.adventure.text.minimessage.internal.serializer;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.lychee.lagfixer.libs.kyori.adventure.text.Component;

@FunctionalInterface
public interface Emitable {
   void emit(@NotNull final TokenEmitter emitter);

   @Nullable
   default Component substitute() {
      return null;
   }
}
