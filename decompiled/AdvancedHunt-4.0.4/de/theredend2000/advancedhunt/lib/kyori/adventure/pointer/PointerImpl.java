package de.theredend2000.advancedhunt.lib.kyori.adventure.pointer;

import de.theredend2000.advancedhunt.lib.kyori.adventure.key.Key;

record PointerImpl<T>(Class<T> type, Key key) implements Pointer<T> {
}
