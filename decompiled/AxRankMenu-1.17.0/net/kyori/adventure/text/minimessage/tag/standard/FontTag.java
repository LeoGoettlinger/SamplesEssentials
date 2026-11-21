package net.kyori.adventure.text.minimessage.tag.standard;

import net.kyori.adventure.key.InvalidKeyException;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.minimessage.Context;
import net.kyori.adventure.text.minimessage.ParsingException;
import net.kyori.adventure.text.minimessage.internal.serializer.SerializableResolver;
import net.kyori.adventure.text.minimessage.internal.serializer.StyleClaim;
import net.kyori.adventure.text.minimessage.internal.serializer.TokenEmitter;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.ArgumentQueue;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;

final class FontTag {
   static final String FONT = "font";
   static final TagResolver RESOLVER = SerializableResolver.claimingStyle("font", FontTag::create, StyleClaim.claim("font", Style::font, FontTag::emit));

   private FontTag() {
   }

   static Tag create(final ArgumentQueue args, final Context ctx) throws ParsingException {
      String valueOrNamespace = args.popOr("A font tag must have either arguments of either <value> or <namespace:value>").value();

      Key font;
      try {
         if (!args.hasNext()) {
            font = Key.key(valueOrNamespace);
         } else {
            String fontKey = args.pop().value();
            font = Key.key(valueOrNamespace, fontKey);
         }
      } catch (InvalidKeyException var5) {
         throw ctx.newException(var5.getMessage(), args);
      }

      return Tag.styling(builder -> builder.font(font));
   }

   static void emit(final Key font, final TokenEmitter emitter) {
      emitter.tag("font");
      if (font.namespace().equals("minecraft")) {
         emitter.argument(font.value());
      } else {
         emitter.arguments(font.namespace(), font.value());
      }
   }
}
