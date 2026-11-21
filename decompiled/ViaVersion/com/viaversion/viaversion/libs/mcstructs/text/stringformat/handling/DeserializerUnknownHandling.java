package com.viaversion.viaversion.libs.mcstructs.text.stringformat.handling;

import com.viaversion.viaversion.libs.mcstructs.text.TextFormatting;
import com.viaversion.viaversion.libs.mcstructs.text.stringformat.ResolvedFormatting;

@FunctionalInterface
public interface DeserializerUnknownHandling {
   DeserializerUnknownHandling WHITE = (resolved, currentText) -> TextFormatting.WHITE;
   DeserializerUnknownHandling IGNORE = (resolved, currentText) -> null;
   DeserializerUnknownHandling RESET = (resolved, currentText) -> TextFormatting.RESET;
   DeserializerUnknownHandling TEXT = (resolved, currentText) -> {
      currentText.append(resolved.raw());
      return null;
   };
   DeserializerUnknownHandling THROW = (resolved, currentText) -> {
      throw new IllegalArgumentException("Unknown formatting code: " + resolved.raw());
   };

   TextFormatting handle(ResolvedFormatting var1, StringBuilder var2);
}
