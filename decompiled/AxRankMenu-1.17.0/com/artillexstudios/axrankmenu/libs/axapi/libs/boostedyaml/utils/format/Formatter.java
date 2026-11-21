package com.artillexstudios.axrankmenu.libs.axapi.libs.boostedyaml.utils.format;

import com.artillexstudios.axrankmenu.libs.axapi.libs.boostedyaml.libs.org.snakeyaml.engine.v2.nodes.Tag;
import org.jetbrains.annotations.NotNull;

public interface Formatter<S, V> {
   @NotNull
   S format(@NotNull Tag var1, @NotNull V var2, @NotNull NodeRole var3, @NotNull S var4);

   @NotNull
   static <S, V> Formatter<S, V> identity() {
      return (tag, value, role, def) -> def;
   }
}
