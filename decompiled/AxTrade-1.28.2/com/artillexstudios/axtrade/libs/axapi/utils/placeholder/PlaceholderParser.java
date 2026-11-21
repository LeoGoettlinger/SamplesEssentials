package com.artillexstudios.axtrade.libs.axapi.utils.placeholder;

import java.util.ArrayList;

public class PlaceholderParser {
   private final ArrayList<StringPlaceholder> placeholders = new ArrayList<>();
   private boolean empty = true;
   private final char startChar;
   private final char endChar;

   public PlaceholderParser(char both) {
      this(both, both);
   }

   public PlaceholderParser(char start, char end) {
      this.startChar = start;
      this.endChar = end;
   }

   public void register(StringPlaceholder placeholder) {
      this.placeholders.add(placeholder);
      this.empty = false;
   }

   public String parseAll(String string, Object... objects) {
      if (this.empty) {
         return string;
      } else {
         int length = string.length();
         StringBuilder builder = new StringBuilder(string);
         int start = -1;
         StringBuilder placeholder = new StringBuilder(length);

         for (int i = 0; i < builder.length(); i++) {
            char ch = builder.charAt(i);
            if (ch == this.startChar || ch == this.endChar) {
               placeholder.append(ch);
               if (i - 1 != start && start != -1) {
                  StringPlaceholder stringPlaceholder = this.getPlaceholder(placeholder);
                  placeholder = new StringBuilder(length);
                  if (stringPlaceholder != null) {
                     int lt = stringPlaceholder.getKey().length();
                     String value = stringPlaceholder.getValue(objects);
                     int len = value.length();
                     builder.replace(start, start + lt, value);
                     i = len - 1;
                  }

                  start = -1;
               } else {
                  start = i;
               }
            } else if (start != -1) {
               placeholder.append(ch);
            }
         }

         return builder.toString();
      }
   }

   private StringPlaceholder getPlaceholder(CharSequence string) {
      for (StringPlaceholder placeholder : this.placeholders) {
         if (placeholder.getKey().contentEquals(string)) {
            return placeholder;
         }
      }

      return null;
   }
}
