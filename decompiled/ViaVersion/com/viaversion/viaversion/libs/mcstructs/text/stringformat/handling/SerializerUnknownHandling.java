package com.viaversion.viaversion.libs.mcstructs.text.stringformat.handling;

import com.viaversion.viaversion.libs.mcstructs.text.TextFormatting;

@FunctionalInterface
public interface SerializerUnknownHandling {
   SerializerUnknownHandling IGNORE = (formatting, output) -> null;
   SerializerUnknownHandling THROW = (formatting, output) -> {
      throw new IllegalArgumentException("Unknown formatting: " + formatting);
   };

   TextFormatting handle(TextFormatting var1, StringBuilder var2);
}
