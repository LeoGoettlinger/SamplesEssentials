package xyz.lychee.lagfixer.libs.kyori.adventure.text.minimessage.tag;

import org.jetbrains.annotations.ApiStatus;

@ApiStatus.NonExtendable
public interface ParserDirective extends Tag {
   Tag RESET = new ParserDirective() {
      @Override
      public String toString() {
         return "ParserDirective.RESET";
      }
   };
}
