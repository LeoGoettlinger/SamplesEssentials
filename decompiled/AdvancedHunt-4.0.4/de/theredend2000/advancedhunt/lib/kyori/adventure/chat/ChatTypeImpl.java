package de.theredend2000.advancedhunt.lib.kyori.adventure.chat;

import de.theredend2000.advancedhunt.lib.kyori.adventure.key.Key;
import de.theredend2000.advancedhunt.lib.kyori.adventure.text.Component;
import org.jspecify.annotations.Nullable;

record ChatTypeImpl(Key key) implements ChatType {
   record BoundImpl(ChatType type, Component name, @Nullable Component target) implements ChatType.Bound {
   }
}
