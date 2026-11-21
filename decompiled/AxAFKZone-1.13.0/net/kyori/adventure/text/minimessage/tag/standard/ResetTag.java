package net.kyori.adventure.text.minimessage.tag.standard;

import net.kyori.adventure.text.minimessage.tag.ParserDirective;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;

final class ResetTag {
   private static final String RESET = "reset";
   static final TagResolver RESOLVER = TagResolver.resolver("reset", ParserDirective.RESET);

   private ResetTag() {
   }
}
