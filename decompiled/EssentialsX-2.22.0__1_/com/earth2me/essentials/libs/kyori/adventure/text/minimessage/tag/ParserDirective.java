package com.earth2me.essentials.libs.kyori.adventure.text.minimessage.tag;

import org.jetbrains.annotations.ApiStatus.NonExtendable;

@NonExtendable
public interface ParserDirective extends Tag {
   Tag RESET = new ParserDirective() {
      @Override
      public String toString() {
         return "ParserDirective.RESET";
      }
   };
}
