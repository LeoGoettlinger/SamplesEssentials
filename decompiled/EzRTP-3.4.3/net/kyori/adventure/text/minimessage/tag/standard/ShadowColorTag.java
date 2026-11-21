package net.kyori.adventure.text.minimessage.tag.standard;

import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.ShadowColor;
import net.kyori.adventure.text.format.StyleGetter;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.minimessage.Context;
import net.kyori.adventure.text.minimessage.ParsingException;
import net.kyori.adventure.text.minimessage.internal.serializer.SerializableResolver;
import net.kyori.adventure.text.minimessage.internal.serializer.StyleClaim;
import net.kyori.adventure.text.minimessage.internal.serializer.TokenEmitter;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.ArgumentQueue;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.jetbrains.annotations.NotNull;

final class ShadowColorTag {
   private static final String SHADOW_COLOR = "shadow";
   private static final String SHADOW_NONE = "!shadow";
   private static final float DEFAULT_ALPHA = 0.25F;
   static final TagResolver RESOLVER = TagResolver.resolver(
      SerializableResolver.claimingStyle("shadow", ShadowColorTag::create, StyleClaim.claim("shadow", StyleGetter::shadowColor, ShadowColorTag::emit)),
      TagResolver.resolver("!shadow", Tag.styling(ShadowColor.none()))
   );

   static Tag create(@NotNull final ArgumentQueue args, @NotNull final Context ctx) throws ParsingException {
      String colorString = args.popOr("Expected to find a color parameter: #RRGGBBAA").lowerValue();
      ShadowColor color;
      if (colorString.startsWith("#") && colorString.length() == 9) {
         color = ShadowColor.fromHexString(colorString);
         if (color == null) {
            throw ctx.newException(String.format("Unable to parse a shadow color from '%s'. Please use #RRGGBBAA formatting.", colorString));
         }
      } else {
         TextColor text = ColorTagResolver.resolveColor(colorString, ctx);
         float alpha = args.hasNext() ? (float)args.pop().asDouble().orElseThrow(() -> ctx.newException("Number was expected to be a double")) : 0.25F;
         color = ShadowColor.shadowColor(text, (int)(alpha * 255.0F));
      }

      return Tag.styling(color);
   }

   static void emit(@NotNull final ShadowColor color, @NotNull final TokenEmitter emitter) {
      if (ShadowColor.none().equals(color)) {
         emitter.tag("!shadow");
      } else {
         emitter.tag("shadow");
         NamedTextColor possibleMatch = NamedTextColor.namedColor(TextColor.color(color).value());
         if (possibleMatch != null) {
            emitter.argument(NamedTextColor.NAMES.key(possibleMatch)).argument(Float.toString(color.alpha() / 255.0F));
         } else {
            emitter.argument(color.asHexString());
         }
      }
   }

   private ShadowColorTag() {
   }
}
