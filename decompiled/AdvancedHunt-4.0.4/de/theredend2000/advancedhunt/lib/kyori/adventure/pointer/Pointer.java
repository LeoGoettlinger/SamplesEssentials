package de.theredend2000.advancedhunt.lib.kyori.adventure.pointer;

import de.theredend2000.advancedhunt.lib.kyori.adventure.key.Key;
import de.theredend2000.advancedhunt.lib.kyori.adventure.key.Keyed;

public sealed interface Pointer<V> extends Keyed permits PointerImpl {
   static <V> Pointer<V> pointer(final Class<V> type, final Key key) {
      return new PointerImpl<>(type, key);
   }

   Class<V> type();

   @Override
   Key key();
}
