package xyz.lychee.lagfixer.libs.kyori.adventure.text.minimessage.tag;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import xyz.lychee.lagfixer.libs.kyori.adventure.text.Component;
import xyz.lychee.lagfixer.libs.kyori.adventure.text.minimessage.tree.Node;

@ApiStatus.OverrideOnly
public interface Modifying extends Tag {
   default void visit(@NotNull final Node current, final int depth) {
   }

   default void postVisit() {
   }

   Component apply(@NotNull final Component current, final int depth);
}
