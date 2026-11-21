package xyz.lychee.lagfixer.libs.kyori.adventure.text.minimessage.tag;

import org.jetbrains.annotations.NotNull;
import xyz.lychee.lagfixer.libs.kyori.adventure.text.Component;

public interface Inserting extends Tag {
   @NotNull
   Component value();

   default boolean allowsChildren() {
      return true;
   }
}
