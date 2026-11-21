package su.nightexpress.nightcore.util;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.regex.Pattern;
import org.bukkit.Color;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import su.nightexpress.nightcore.util.placeholder.PlaceholderEntry;
import su.nightexpress.nightcore.util.placeholder.PlaceholderResolver;
import su.nightexpress.nightcore.util.random.Rnd;
import su.nightexpress.nightcore.util.regex.TimedMatcher;
import su.nightexpress.nightcore.util.text.night.NightMessage;
import su.nightexpress.nightcore.util.text.night.wrapper.TagWrappers;

public class StringUtil {
   private static final Pattern ID_PATTERN = Pattern.compile("[<>\\%\\$\\!\\@\\#\\^\\&\\*\\(\\)\\,\\.\\'\\:\\;\\\"\\}\\]\\{\\[\\=\\+\\`\\~\\\\]");
   private static final Pattern ID_STRICT_PATTERN = Pattern.compile("[^a-zA-Zа-яА-Я_0-9]");
   private static final String[] DELIMITERS = new String[]{TagWrappers.BR, TagWrappers.NEWLINE, "\n"};

   @Deprecated
   @NonNull
   public static String oneSpace(@NonNull String str) {
      return str.trim().replaceAll("\\s+", " ");
   }

   @Deprecated
   @NonNull
   public static String noSpace(@NonNull String str) {
      return str.trim().replaceAll("\\s+", "");
   }

   @Deprecated
   @NonNull
   public static String replaceEach(@NonNull String text, @NonNull List<Pair<String, Supplier<String>>> replacements) {
      if (!text.isEmpty() && !replacements.isEmpty()) {
         int searchLength = replacements.size();
         boolean[] noMoreMatchesForReplIndex = new boolean[searchLength];
         int textIndex = -1;
         int replaceIndex = -1;

         for (int i = 0; i < searchLength; i++) {
            if (!noMoreMatchesForReplIndex[i]) {
               int tempIndex = text.indexOf(replacements.get(i).getFirst());
               if (tempIndex == -1) {
                  noMoreMatchesForReplIndex[i] = true;
               } else if (textIndex == -1 || tempIndex < textIndex) {
                  textIndex = tempIndex;
                  replaceIndex = i;
               }
            }
         }

         if (textIndex == -1) {
            return text;
         } else {
            int start = 0;
            StringBuilder buf = new StringBuilder();

            while (textIndex != -1) {
               for (int ix = start; ix < textIndex; ix++) {
                  buf.append(text.charAt(ix));
               }

               buf.append(replacements.get(replaceIndex).getSecond().get());
               start = textIndex + replacements.get(replaceIndex).getFirst().length();
               textIndex = -1;
               replaceIndex = -1;

               for (int ix = 0; ix < searchLength; ix++) {
                  if (!noMoreMatchesForReplIndex[ix]) {
                     int tempIndex = text.indexOf(replacements.get(ix).getFirst(), start);
                     if (tempIndex == -1) {
                        noMoreMatchesForReplIndex[ix] = true;
                     } else if (textIndex == -1 || tempIndex < textIndex) {
                        textIndex = tempIndex;
                        replaceIndex = ix;
                     }
                  }
               }
            }

            int textLength = text.length();

            for (int ixx = start; ixx < textLength; ixx++) {
               buf.append(text.charAt(ixx));
            }

            return buf.toString();
         }
      } else {
         return text;
      }
   }

   @Deprecated
   @NonNull
   public static <T> String replaceEach(@NonNull String text, @NonNull List<PlaceholderEntry<T>> replacements, @NonNull T source) {
      if (!text.isEmpty() && !replacements.isEmpty()) {
         int searchLength = replacements.size();
         boolean[] noMoreMatchesForReplIndex = new boolean[searchLength];
         int textIndex = -1;
         int replaceIndex = -1;

         for (int i = 0; i < searchLength; i++) {
            if (!noMoreMatchesForReplIndex[i]) {
               int tempIndex = text.indexOf(replacements.get(i).getKey());
               if (tempIndex == -1) {
                  noMoreMatchesForReplIndex[i] = true;
               } else if (textIndex == -1 || tempIndex < textIndex) {
                  textIndex = tempIndex;
                  replaceIndex = i;
               }
            }
         }

         if (textIndex == -1) {
            return text;
         } else {
            int start = 0;
            StringBuilder buf = new StringBuilder();

            while (textIndex != -1) {
               for (int ix = start; ix < textIndex; ix++) {
                  buf.append(text.charAt(ix));
               }

               buf.append(replacements.get(replaceIndex).get(source));
               start = textIndex + replacements.get(replaceIndex).getKey().length();
               textIndex = -1;
               replaceIndex = -1;

               for (int ix = 0; ix < searchLength; ix++) {
                  if (!noMoreMatchesForReplIndex[ix]) {
                     int tempIndex = text.indexOf(replacements.get(ix).getKey(), start);
                     if (tempIndex == -1) {
                        noMoreMatchesForReplIndex[ix] = true;
                     } else if (textIndex == -1 || tempIndex < textIndex) {
                        textIndex = tempIndex;
                        replaceIndex = ix;
                     }
                  }
               }
            }

            int textLength = text.length();

            for (int ixx = start; ixx < textLength; ixx++) {
               buf.append(text.charAt(ixx));
            }

            return buf.toString();
         }
      } else {
         return text;
      }
   }

   @NonNull
   public static String replacePlaceholders(@NonNull String string, @NonNull PlaceholderResolver resolver) {
      if (string.isBlank()) {
         return string;
      } else if (string.indexOf(37) == -1) {
         return string;
      } else {
         StringBuilder builder = new StringBuilder();
         int length = string.length();

         for (int index = 0; index < length; index++) {
            char letter = string.charAt(index);
            if (letter == '\\' && index + 1 < length && string.charAt(index + 1) == '%') {
               builder.append('%');
               index++;
            } else {
               if (letter == '%') {
                  int indexNext = string.indexOf(letter, index + 1);
                  if (indexNext != -1) {
                     String key = string.substring(index, indexNext + 1);
                     String replacement = resolver.resolve(key);
                     if (replacement != null) {
                        builder.append(replacement);
                        index = indexNext;
                        continue;
                     }
                  }
               }

               builder.append(letter);
            }
         }

         return builder.toString();
      }
   }

   @Nullable
   public static String parseQuotedContent(@NonNull String string) {
      char quote = string.charAt(0);
      if (quote != '\'' && quote != '"') {
         return null;
      } else if (string.length() < 3) {
         return null;
      } else {
         StringBuilder builder = new StringBuilder();

         for (int index = 1; index < string.length(); index++) {
            char letter = string.charAt(index);
            if (letter != '\\') {
               if (letter == quote) {
                  break;
               }

               builder.append(letter);
            }
         }

         return builder.toString();
      }
   }

   @Deprecated
   @NonNull
   public static <T extends Enum<T>> Optional<T> getEnum(String str, @NonNull Class<T> clazz) {
      return Enums.parse(str, clazz);
   }

   @Deprecated
   @NonNull
   public static String inlineEnum(@NonNull Class<? extends Enum<?>> clazz, @NonNull String delimiter) {
      return Enums.inline(clazz, delimiter);
   }

   @NonNull
   public static Color getColor(@NonNull String str) {
      String[] rgb = str.split(",");
      int red = NumberUtil.getIntegerAbs(rgb[0]);
      if (red < 0) {
         red = Rnd.get(255);
      }

      int green = rgb.length >= 2 ? NumberUtil.getIntegerAbs(rgb[1]) : 0;
      if (green < 0) {
         green = Rnd.get(255);
      }

      int blue = rgb.length >= 3 ? NumberUtil.getIntegerAbs(rgb[2]) : 0;
      if (blue < 0) {
         blue = Rnd.get(255);
      }

      return Color.fromRGB(red, green, blue);
   }

   @Deprecated
   @NonNull
   public static String transformForID(@NonNull String str) {
      return transformForID(str, -1);
   }

   @Deprecated
   @NonNull
   public static String transformForID(@NonNull String str, int length) {
      return Strings.filterForVariable(str, length);
   }

   @NonNull
   public static String lowerCaseUnderscore(@NonNull String str) {
      return LowerCase.INTERNAL.apply(str).replace(' ', '_');
   }

   @Deprecated
   @NonNull
   public static String lowerCaseUnderscore(@NonNull String str, int length) {
      return lowerCaseAndClean(str, ID_PATTERN, length);
   }

   @Deprecated
   @NonNull
   public static String lowerCaseUnderscoreStrict(@NonNull String str) {
      return lowerCaseUnderscoreStrict(str, -1);
   }

   @Deprecated
   @NonNull
   public static String lowerCaseUnderscoreStrict(@NonNull String str, int length) {
      return lowerCaseAndClean(str, ID_STRICT_PATTERN, length);
   }

   @Deprecated
   @NonNull
   private static String lowerCaseAndClean(@NonNull String str, @NonNull Pattern pattern, int length) {
      String clean = LowerCase.INTERNAL.apply(NightMessage.stripTags(str)).replace(" ", "_");
      if (length > 0 && clean.length() > length) {
         clean = clean.substring(0, length);
      }

      TimedMatcher matcher = TimedMatcher.create(pattern, clean, 200L);
      return matcher.replaceAll("");
   }

   @NonNull
   public static String capitalizeUnderscored(@NonNull String str) {
      return capitalizeFully(str.replace("_", " "));
   }

   @NonNull
   public static String capitalizeFully(@NonNull String str) {
      return capitalize(LowerCase.USER_LOCALE.apply(str));
   }

   @NonNull
   public static String capitalize(@NonNull String str) {
      if (str.isEmpty()) {
         return str;
      } else {
         int length = str.length();
         StringBuilder builder = new StringBuilder(length);
         boolean capitalizeNext = true;

         for (int index = 0; index < length; index++) {
            char letter = str.charAt(index);
            if (Character.isWhitespace(letter)) {
               builder.append(letter);
               capitalizeNext = true;
            } else if (capitalizeNext) {
               builder.append(Character.toTitleCase(letter));
               capitalizeNext = false;
            } else {
               builder.append(letter);
            }
         }

         return builder.toString();
      }
   }

   @NonNull
   public static String capitalizeFirstLetter(@NonNull String original) {
      return original.isEmpty() ? original : original.substring(0, 1).toUpperCase() + original.substring(1);
   }

   public static void splitDelimiters(@NonNull String string, @NonNull Consumer<String> result) {
      if (string.isBlank()) {
         result.accept(string);
      } else {
         int cursor = 0;
         int length = string.length();

         while (cursor < length) {
            int closestIndex = -1;
            String closestDelimiter = null;

            for (String token : DELIMITERS) {
               int index = string.indexOf(token, cursor);
               if (index != -1 && (closestIndex == -1 || index < closestIndex)) {
                  closestIndex = index;
                  closestDelimiter = token;
               }
            }

            if (closestIndex == -1) {
               if (cursor == 0) {
                  result.accept(string);
               } else {
                  result.accept(string.substring(cursor));
               }
               break;
            }

            result.accept(string.substring(cursor, closestIndex));
            cursor = closestIndex + closestDelimiter.length();
         }

         if (cursor == length) {
            result.accept("");
         }
      }
   }
}
