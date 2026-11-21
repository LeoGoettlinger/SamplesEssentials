package me.usainsrht.ujobs.utils;

import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.TagPattern;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.jetbrains.annotations.NotNull;

public class ProgressBarUtil {
   public static final int DEFAULT_CHARS = 45;
   public static final String DEFAULT_FULL_STYLE = "<green>";
   public static final String DEFAULT_FULL_CHAR = "|";
   public static final String DEFAULT_EMPTY_STYLE = "<dark_gray>";
   public static final String DEFAULT_EMPTY_CHAR = "|";

   public static String getProgressBar(double value, double max, int chars, String fullCharsStyle, String fullChar, String emptyCharsStyle, String emptyChar) {
      StringBuilder stringBuilder = new StringBuilder();
      int fullChars = (int)(value / max * chars);
      if (fullChars > 0) {
         stringBuilder.append(fullCharsStyle);
      }

      for (int i = 0; i < fullChars; i++) {
         stringBuilder.append(fullChar);
      }

      if (fullChars != chars) {
         stringBuilder.append(emptyCharsStyle);

         for (int i = 0; i < chars - fullChars; i++) {
            stringBuilder.append(emptyChar);
         }
      }

      return stringBuilder.toString();
   }

   public static TagResolver progressBar(@TagPattern @NotNull String key, double value, double max) {
      return TagResolver.resolver(key, (argumentQueue, context) -> {
         int chars = argumentQueue.hasNext() ? argumentQueue.pop().asInt().orElse(45) : 45;
         String fullStyle = argumentQueue.hasNext() ? argumentQueue.pop().value() : "<green>";
         String fullChar = argumentQueue.hasNext() ? argumentQueue.pop().value() : "|";
         String emptyStyle = argumentQueue.hasNext() ? argumentQueue.pop().value() : "<dark_gray>";
         String emptyChar = argumentQueue.hasNext() ? argumentQueue.pop().value() : "|";
         return Tag.selfClosingInserting(context.deserialize(getProgressBar(value, max, chars, fullStyle, fullChar, emptyStyle, emptyChar)));
      });
   }
}
