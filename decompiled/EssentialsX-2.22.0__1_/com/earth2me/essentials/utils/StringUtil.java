package com.earth2me.essentials.utils;

import com.google.common.primitives.Chars;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.function.Function;
import java.util.regex.Pattern;

public final class StringUtil {
   private static final Pattern INVALIDFILECHARS = Pattern.compile("[^a-z0-9-]");
   private static final Pattern STRICTINVALIDCHARS = Pattern.compile("[^a-z0-9]");
   private static final Pattern INVALIDCHARS = Pattern.compile("[^\t\n\r -~\u0085 -\ud7ff\ue000-￼]");
   private static final Pattern WINDOWS_RESERVED = Pattern.compile("^(con|prn|aux|nul|com[0-9]|lpt[0-9])$");

   private StringUtil() {
   }

   public static String sanitizeFileName(final String name) {
      return INVALIDFILECHARS.matcher(name.toLowerCase(Locale.ENGLISH)).replaceAll("_");
   }

   public static boolean isReservedFileName(final String name) {
      return WINDOWS_RESERVED.matcher(name.toLowerCase(Locale.ENGLISH)).matches();
   }

   public static String safeString(final String string) {
      return string == null ? null : STRICTINVALIDCHARS.matcher(string.toLowerCase(Locale.ENGLISH)).replaceAll("_");
   }

   public static String sanitizeString(final String string) {
      return INVALIDCHARS.matcher(string).replaceAll("");
   }

   public static String joinList(final Object... list) {
      return joinList(", ", list);
   }

   public static String joinList(final String seperator, final Object... list) {
      StringBuilder buf = new StringBuilder();

      for (Object each : list) {
         if (buf.length() > 0) {
            buf.append(seperator);
         }

         if (each instanceof Collection) {
            buf.append(joinList(seperator, ((Collection)each).toArray()));
         } else {
            try {
               buf.append(each.toString());
            } catch (Exception var8) {
               buf.append(each.toString());
            }
         }
      }

      return buf.toString();
   }

   public static String joinListSkip(final String seperator, final String skip, final Object... list) {
      StringBuilder buf = new StringBuilder();

      for (Object each : list) {
         if (!each.toString().equalsIgnoreCase(skip)) {
            if (buf.length() > 0) {
               buf.append(seperator);
            }

            if (each instanceof Collection) {
               buf.append(joinListSkip(seperator, skip, ((Collection)each).toArray()));
            } else {
               try {
                  buf.append(each.toString());
               } catch (Exception var9) {
                  buf.append(each.toString());
               }
            }
         }
      }

      return buf.toString();
   }

   public static UUID toUUID(final String input) {
      try {
         return UUID.fromString(input);
      } catch (IllegalArgumentException var2) {
         return null;
      }
   }

   public static String abbreviate(final String input, final int length) {
      if (input == null) {
         return null;
      } else if (length < 4) {
         throw new IllegalArgumentException("Invalid length " + length);
      } else {
         return input.length() <= length ? input : input.substring(0, length - 3) + "...";
      }
   }

   public static String stripToNull(final String input) {
      if (input == null) {
         return null;
      } else {
         String result = strip(input);
         return result.isEmpty() ? null : result;
      }
   }

   public static String strip(final String input) {
      return strip(input, Character::isWhitespace);
   }

   public static String strip(final String input, final String stripChars) {
      if (stripChars == null) {
         return strip(input);
      } else {
         List<Character> toStrip = Chars.asList(stripChars.toCharArray());
         return strip(input, toStrip::contains);
      }
   }

   public static String strip(final String input, Function<Character, Boolean> shouldStrip) {
      if (input == null) {
         return null;
      } else {
         int startIndex = 0;
         int endIndex = input.length();

         while (startIndex < endIndex && shouldStrip.apply(input.charAt(startIndex))) {
            startIndex++;
         }

         while (endIndex > startIndex && shouldStrip.apply(input.charAt(endIndex - 1))) {
            endIndex--;
         }

         return input.substring(startIndex, endIndex);
      }
   }
}
