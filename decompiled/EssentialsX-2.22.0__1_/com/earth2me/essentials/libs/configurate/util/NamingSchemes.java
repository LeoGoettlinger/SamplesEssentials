package com.earth2me.essentials.libs.configurate.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public enum NamingSchemes implements NamingScheme {
   PASSTHROUGH {
      @Override
      public String coerce(final String input) {
         return input;
      }
   },
   CAMEL_CASE {
      @Override
      public String coerce(final String input) {
         Matcher match = NamingSchemes.DASH_UNDERSCORE.matcher(input);
         if (!match.find()) {
            return input;
         } else {
            StringBuffer ret = new StringBuffer(input.length());

            do {
               match.appendReplacement(ret, "");
               ret.appendCodePoint(input.codePointAt(match.start()));
               ret.appendCodePoint(Character.toUpperCase(input.codePointBefore(match.end())));
            } while (match.find());

            match.appendTail(ret);
            return ret.toString();
         }
      }
   },
   SNAKE_CASE {
      @Override
      public String coerce(final String input) {
         return NamingSchemes.enforceLowerCaseSeparatorChar(input, '_', '-');
      }
   },
   LOWER_CASE_DASHED {
      @Override
      public String coerce(final String input) {
         return NamingSchemes.enforceLowerCaseSeparatorChar(input, '-', '_');
      }
   };

   private static final Pattern DASH_UNDERSCORE = Pattern.compile(".[-_].");
   private static final char UNDERSCORE = '_';
   private static final char DASH = '-';

   private NamingSchemes() {
   }

   private static String enforceLowerCaseSeparatorChar(final String input, final char preferredDelimiter, final char convertDelimiter) {
      StringBuilder build = new StringBuilder(input);

      for (int i = 0; i < build.length(); i++) {
         int ch = build.codePointAt(i);
         if (ch == convertDelimiter) {
            if (i != 0 && i != build.length() - 1) {
               build.setCharAt(i, preferredDelimiter);
            }
         } else if (Character.isUpperCase(ch)) {
            build.insert(i++, preferredDelimiter);
            int lower = Character.toLowerCase(ch);
            if (Character.isBmpCodePoint(lower)) {
               build.setCharAt(i, (char)lower);
            } else {
               build.setCharAt(i++, Character.highSurrogate(lower));
               build.setCharAt(i, Character.lowSurrogate(lower));
            }
         }
      }

      return build.toString();
   }
}
