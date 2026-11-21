package xyz.lychee.lagfixer.libs.kyori.adventure.text.minimessage.tree;

import java.util.List;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@ApiStatus.NonExtendable
public interface Node {
   @NotNull
   @Override
   String toString();

   @NotNull
   List<? extends Node> children();

   @Nullable
   Node parent();

   @ApiStatus.NonExtendable
   public interface Root extends Node {
      @NotNull
      String input();
   }
}
