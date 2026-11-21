package com.earth2me.essentials.libs.kyori.adventure.text.minimessage.tag.standard;

import com.earth2me.essentials.libs.kyori.adventure.text.minimessage.tag.ParserDirective;
import com.earth2me.essentials.libs.kyori.adventure.text.minimessage.tag.resolver.TagResolver;

final class ResetTag {
   private static final String RESET = "reset";
   static final TagResolver RESOLVER = TagResolver.resolver("reset", ParserDirective.RESET);

   private ResetTag() {
   }
}
