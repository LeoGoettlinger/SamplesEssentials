package net.kyori.adventure.text.minimessage.tag;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tree.Node;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.ApiStatus.OverrideOnly;

@OverrideOnly
public interface Modifying extends Tag {
   default void visit(@NotNull final Node current, final int depth) {
   }

   default void postVisit() {
   }

   Component apply(@NotNull final Component current, final int depth);
}
