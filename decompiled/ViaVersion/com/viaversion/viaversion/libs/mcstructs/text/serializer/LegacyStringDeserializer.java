package com.viaversion.viaversion.libs.mcstructs.text.serializer;

import com.viaversion.viaversion.libs.mcstructs.text.Style;
import com.viaversion.viaversion.libs.mcstructs.text.TextComponent;
import com.viaversion.viaversion.libs.mcstructs.text.TextFormatting;
import com.viaversion.viaversion.libs.mcstructs.text.components.StringComponent;
import java.util.function.Function;
import java.util.function.Supplier;

@Deprecated
public class LegacyStringDeserializer {
   @Deprecated
   public static TextComponent parse(String s, boolean unknownWhite) {
      return parse(s, '§', unknownWhite);
   }

   @Deprecated
   public static TextComponent parse(String s, char colorChar, boolean unknownWhite) {
      return parse(s, colorChar, c -> {
         TextFormatting formatting = TextFormatting.getByCode(c);
         if (formatting == null) {
            return unknownWhite ? TextFormatting.WHITE : null;
         } else {
            return formatting;
         }
      });
   }

   @Deprecated
   public static TextComponent parse(String s, char colorChar, Function<Character, TextFormatting> formattingResolver) {
      return parse(s, colorChar, Style::new, formattingResolver);
   }

   @Deprecated
   public static TextComponent parse(String s, char colorChar, Supplier<Style> styleSupplier, Function<Character, TextFormatting> formattingResolver) {
      char[] chars = s.toCharArray();
      Style style = styleSupplier.get();
      StringBuilder currentPart = new StringBuilder();
      TextComponent out = new StringComponent("");

      for (int i = 0; i < chars.length; i++) {
         char c = chars[i];
         if (c == colorChar) {
            if (i + 1 < chars.length) {
               char format = chars[++i];
               TextFormatting formatting = formattingResolver.apply(format);
               if (formatting != null) {
                  if (currentPart.length() != 0) {
                     out.append(new StringComponent(currentPart.toString()).setStyle(style.copy()));
                     currentPart = new StringBuilder();
                     if (formatting.isColor() || TextFormatting.RESET.equals(formatting)) {
                        style = styleSupplier.get();
                     }
                  }

                  style.setFormatting(formatting);
               }
            }
         } else {
            currentPart.append(c);
         }
      }

      if (currentPart.length() != 0) {
         out.append(new StringComponent(currentPart.toString()).setStyle(style));
      }

      return out.getSiblings().size() == 1 ? out.getSiblings().get(0) : out;
   }
}
