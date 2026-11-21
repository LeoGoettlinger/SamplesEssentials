package net.kyori.adventure.pointer;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.Keyed;

public sealed interface Pointer<V> extends Keyed permits PointerImpl {
   static <V> Pointer<V> pointer(final Class<V> type, final Key key) {
      return new PointerImpl<>(type, key);
   }

   Class<V> type();

   @Override
   Key key();
}
