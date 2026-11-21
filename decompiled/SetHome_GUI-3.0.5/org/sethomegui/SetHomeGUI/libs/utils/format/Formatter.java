package org.sethomegui.SetHomeGUI.libs.utils.format;

import org.jetbrains.annotations.NotNull;
import org.sethomegui.SetHomeGUI.libs.libs.org.snakeyaml.engine.v2.nodes.Tag;

public interface Formatter<S, V> {
   @NotNull
   S format(@NotNull Tag var1, @NotNull V var2, @NotNull NodeRole var3, @NotNull S var4);

   @NotNull
   static <S, V> Formatter<S, V> identity() {
      return (tag, value, role, def) -> def;
   }
}
