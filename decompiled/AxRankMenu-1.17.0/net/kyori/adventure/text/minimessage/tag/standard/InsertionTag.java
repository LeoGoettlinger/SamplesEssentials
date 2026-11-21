package net.kyori.adventure.text.minimessage.tag.standard;

import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.minimessage.Context;
import net.kyori.adventure.text.minimessage.ParsingException;
import net.kyori.adventure.text.minimessage.internal.serializer.SerializableResolver;
import net.kyori.adventure.text.minimessage.internal.serializer.StyleClaim;
import net.kyori.adventure.text.minimessage.internal.serializer.TokenEmitter;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.ArgumentQueue;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;

final class InsertionTag {
   private static final String INSERTION = "insert";
   static final TagResolver RESOLVER = SerializableResolver.claimingStyle(
      "insert", InsertionTag::create, StyleClaim.claim("insert", Style::insertion, InsertionTag::emit)
   );

   private InsertionTag() {
   }

   static Tag create(final ArgumentQueue args, final Context ctx) throws ParsingException {
      String insertion = args.popOr("A value is required to produce an insertion component").value();
      return Tag.styling(b -> b.insertion(insertion));
   }

   static void emit(final String insertion, final TokenEmitter emitter) {
      emitter.tag("insert").argument(insertion);
   }
}
