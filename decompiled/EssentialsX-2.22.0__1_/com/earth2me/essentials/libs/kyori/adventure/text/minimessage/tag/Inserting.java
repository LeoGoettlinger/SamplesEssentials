package com.earth2me.essentials.libs.kyori.adventure.text.minimessage.tag;

import com.earth2me.essentials.libs.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

public interface Inserting extends Tag {
   @NotNull
   Component value();

   default boolean allowsChildren() {
      return true;
   }
}
