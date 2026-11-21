package net.kyori.adventure.text.serializer.legacy;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextFormat;

record CharacterAndFormatSet(List<TextFormat> formats, List<TextColor> colors, String characters) {
   static final CharacterAndFormatSet DEFAULT = of(CharacterAndFormat.defaults());

   static CharacterAndFormatSet of(final List<CharacterAndFormat> pairs) {
      int size = pairs.size();
      List<TextColor> colors = new ArrayList<>();
      List<TextFormat> formats = new ArrayList<>(size);
      StringBuilder characters = new StringBuilder(size);

      for (CharacterAndFormat pair : pairs) {
         char character = pair.character();
         TextFormat format = pair.format();
         boolean formatIsTextColor = format instanceof TextColor;
         characters.append(character);
         formats.add(format);
         if (formatIsTextColor) {
            colors.add((TextColor)format);
         }

         if (pair.caseInsensitive()) {
            boolean added = false;
            if (Character.isUpperCase(character)) {
               characters.append(Character.toLowerCase(character));
               added = true;
            } else if (Character.isLowerCase(character)) {
               characters.append(Character.toUpperCase(character));
               added = true;
            }

            if (added) {
               formats.add(format);
               if (formatIsTextColor) {
                  colors.add((TextColor)format);
               }
            }
         }
      }

      if (formats.size() != characters.length()) {
         throw new IllegalStateException("formats length differs from characters length");
      } else {
         return new CharacterAndFormatSet(Collections.unmodifiableList(formats), Collections.unmodifiableList(colors), characters.toString());
      }
   }
}
