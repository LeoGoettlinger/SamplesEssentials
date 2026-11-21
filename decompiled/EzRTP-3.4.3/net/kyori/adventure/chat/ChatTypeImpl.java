package net.kyori.adventure.chat;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import org.jspecify.annotations.Nullable;

record ChatTypeImpl(Key key) implements ChatType {
   record BoundImpl(ChatType type, Component name, @Nullable Component target) implements ChatType.Bound {
   }
}
