package net.kyori.adventure.pointer;

import net.kyori.adventure.key.Key;

record PointerImpl<T>(Class<T> type, Key key) implements Pointer<T> {
}
