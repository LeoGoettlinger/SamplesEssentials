package com.earth2me.essentials.libs.kyori.adventure.text.minimessage.tree;

import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.ApiStatus.NonExtendable;

@NonExtendable
public interface Node {
   @NotNull
   @Override
   String toString();

   @NotNull
   List<? extends Node> children();

   @Nullable
   Node parent();

   @NonExtendable
   public interface Root extends Node {
      @NotNull
      String input();
   }
}
