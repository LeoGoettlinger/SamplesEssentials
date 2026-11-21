package xyz.lychee.lagfixer.libs.kyori.adventure.text.minimessage.tag.standard;

import org.jetbrains.annotations.Nullable;
import xyz.lychee.lagfixer.libs.kyori.adventure.text.Component;
import xyz.lychee.lagfixer.libs.kyori.adventure.text.minimessage.Context;
import xyz.lychee.lagfixer.libs.kyori.adventure.text.minimessage.ParsingException;
import xyz.lychee.lagfixer.libs.kyori.adventure.text.minimessage.internal.serializer.Emitable;
import xyz.lychee.lagfixer.libs.kyori.adventure.text.minimessage.internal.serializer.SerializableResolver;
import xyz.lychee.lagfixer.libs.kyori.adventure.text.minimessage.tag.Tag;
import xyz.lychee.lagfixer.libs.kyori.adventure.text.minimessage.tag.resolver.ArgumentQueue;
import xyz.lychee.lagfixer.libs.kyori.adventure.text.minimessage.tag.resolver.TagResolver;

final class NewlineTag {
   private static final String BR = "br";
   private static final String NEWLINE = "newline";
   static final TagResolver RESOLVER = SerializableResolver.claimingComponent(
      StandardTags.names("newline", "br"), NewlineTag::create, NewlineTag::claimComponent
   );

   private NewlineTag() {
   }

   static Tag create(final ArgumentQueue args, final Context ctx) throws ParsingException {
      return Tag.selfClosingInserting((Component)Component.newline());
   }

   @Nullable
   static Emitable claimComponent(final Component input) {
      return Component.newline().equals(input) ? emit -> emit.selfClosingTag("br") : null;
   }
}
