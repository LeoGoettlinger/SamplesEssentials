package ch.njol.util;

import java.lang.invoke.StringConcatFactory;
import java.util.Iterator;
import java.util.Locale;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jetbrains.annotations.Nullable;

public abstract class StringUtils {
   public static void checkIndices(String s, int start, int end) {
      if (start < 0 || end > s.length()) {
         throw new StringIndexOutOfBoundsException("invalid start/end indices " + start + "," + end + " for string \"" + s + "\" (length " + s.length() + ")");
      }
   }

   public static String fancyOrderNumber(int i) {
      int iModTen = i % 10;
      int iModHundred = i % 100;
      if (iModTen == 1 && iModHundred != 11) {
         return i + "st";
      } else if (iModTen == 2 && iModHundred != 12) {
         return i + "nd";
      } else {
         return iModTen == 3 && iModHundred != 13 ? i + "rd" : i + "th";
      }
   }

   @Nullable
   public static String replaceAll(CharSequence string, String regex, Function<Matcher, String> callback) {
      return replaceAll(string, Pattern.compile(regex), callback);
   }

   @Nullable
   public static String replaceAll(CharSequence string, Pattern regex, Function<Matcher, String> callback) {
      Matcher m = regex.matcher(string);
      StringBuffer sb = new StringBuffer();

      while (m.find()) {
         String r = callback.apply(m);
         if (r == null) {
            return null;
         }

         m.appendReplacement(sb, r);
      }

      m.appendTail(sb);
      return sb.toString();
   }

   public static int count(String s, char c) {
      return count(s, c, 0, s.length());
   }

   public static int count(String s, char c, int start) {
      return count(s, c, start, s.length());
   }

   public static int count(String s, char c, int start, int end) {
      checkIndices(s, start, end);
      int r = 0;

      for (int i = start; i < end; i++) {
         if (s.charAt(i) == c) {
            r++;
         }
      }

      return r;
   }

   public static boolean contains(String s, char c, int start, int end) {
      checkIndices(s, start, end);

      for (int i = start; i < end; i++) {
         if (s.charAt(i) == c) {
            return true;
         }
      }

      return false;
   }

   public static String toString(double d, int accuracy) {
      assert accuracy >= 0;

      if (accuracy <= 0) {
         return Math.round(d) + "";
      } else {
         String s = String.format(Locale.ENGLISH, "%." + accuracy + "f", d);
         int c = s.length() - 1;

         while (s.charAt(c) == '0') {
            c--;
         }

         if (s.charAt(c) == '.') {
            c--;
         }

         return StringConcatFactory.makeConcatWithConstants<"makeConcatWithConstants","\u0001">(s.substring(0, c + 1));
      }
   }

   public static String firstToUpper(String s) {
      if (s.isEmpty()) {
         return s;
      } else {
         return Character.isUpperCase(s.charAt(0)) ? s : Character.toUpperCase(s.charAt(0)) + s.substring(1);
      }
   }

   public static String substring(String s, int start, int end) {
      if (start < 0) {
         start += s.length();
      }

      if (end < 0) {
         end += s.length();
      }

      if (end < start) {
         throw new IllegalArgumentException("invalid indices");
      } else {
         return StringConcatFactory.makeConcatWithConstants<"makeConcatWithConstants","\u0001">(s.substring(start, end));
      }
   }

   public static String fixCapitalization(String string) {
      char[] s = string.toCharArray();

      for (int c = 0; c != -1; c = indexOf(s, c + 1, '.', '!', '?')) {
         while (c < s.length && (s[c] == '.' || s[c] == '!' || s[c] == '?' || Character.isWhitespace(s[c]))) {
            c++;
         }

         if (c == s.length) {
            return new String(s);
         }

         if (c == 0 || Character.isWhitespace(s[c - 1])) {
            s[c] = Character.toUpperCase(s[c]);
         }
      }

      return new String(s);
   }

   private static int indexOf(char[] s, int start, char... cs) {
      for (int i = start; i < s.length; i++) {
         for (char c : cs) {
            if (s[i] == c) {
               return i;
            }
         }
      }

      return -1;
   }

   public static int indexOf(String haystack, String needle, boolean caseSensitive) {
      if (caseSensitive) {
         haystack = haystack.toLowerCase(Locale.ENGLISH);
         needle = needle.toLowerCase(Locale.ENGLISH);
      }

      return haystack.indexOf(needle);
   }

   public static int indexOf(String haystack, String needle, int fromIndex, boolean caseSensitive) {
      if (fromIndex >= 0 && fromIndex < haystack.length()) {
         if (caseSensitive) {
            haystack = haystack.toLowerCase(Locale.ENGLISH);
            needle = needle.toLowerCase(Locale.ENGLISH);
         }

         return haystack.indexOf(needle, fromIndex);
      } else {
         return -1;
      }
   }

   public static int lastIndexOf(String haystack, String needle, boolean caseSensitive) {
      if (caseSensitive) {
         haystack = haystack.toLowerCase(Locale.ENGLISH);
         needle = needle.toLowerCase(Locale.ENGLISH);
      }

      return haystack.lastIndexOf(needle);
   }

   public static double numberAfter(CharSequence s, int index) {
      return numberAt(s, index, true);
   }

   public static double numberBefore(CharSequence s, int index) {
      return numberAt(s, index, false);
   }

   public static double numberAt(CharSequence s, int index, boolean forward) {
      assert s != null;

      assert index >= 0 && index < s.length() : index;

      int direction = forward ? 1 : -1;
      boolean stillWhitespace = true;
      boolean hasDot = false;
      int d1 = -1;
      int d2 = -1;

      for (int i = index; i >= 0 && i < s.length(); i += direction) {
         char c = s.charAt(i);
         if ('0' <= c && c <= '9') {
            if (d1 == -1) {
               d2 = i;
               d1 = i;
            } else {
               d1 += direction;
            }

            stillWhitespace = false;
         } else if (c == '.') {
            if (hasDot) {
               break;
            }

            if (d1 == -1) {
               d2 = i;
               d1 = i;
            } else {
               d1 += direction;
            }

            hasDot = true;
            stillWhitespace = false;
         } else if (!Character.isWhitespace(c) || !stillWhitespace) {
            break;
         }
      }

      if (d1 == -1) {
         return -1.0;
      } else if (s.charAt(Math.min(d1, d2)) == '.') {
         return -1.0;
      } else {
         return d1 + direction > 0 && d1 + direction < s.length() && !Character.isWhitespace(s.charAt(d1 + direction))
            ? -1.0
            : Double.parseDouble(s.subSequence(Math.min(d1, d2), Math.max(d1, d2) + 1).toString());
      }
   }

   public static boolean startsWithIgnoreCase(String string, String start) {
      return startsWithIgnoreCase(string, start, 0);
   }

   public static boolean startsWithIgnoreCase(String string, String start, int offset) {
      assert string != null;

      assert start != null;

      return string.length() < offset + start.length() ? false : string.substring(offset, start.length()).equalsIgnoreCase(start);
   }

   public static boolean endsWithIgnoreCase(String string, String end) {
      assert string != null;

      assert end != null;

      return string.length() < end.length() ? false : string.substring(string.length() - end.length()).equalsIgnoreCase(end);
   }

   public static String multiply(@Nullable String s, int amount) {
      assert amount >= 0 : amount;

      if (s == null) {
         return "";
      } else if (amount <= 0) {
         return "";
      } else if (amount == 1) {
         return s;
      } else {
         char[] input = s.toCharArray();
         char[] multiplied = new char[input.length * amount];

         for (int i = 0; i < amount; i++) {
            System.arraycopy(input, 0, multiplied, i * input.length, input.length);
         }

         return new String(multiplied);
      }
   }

   public static String multiply(char c, int amount) {
      if (amount == 0) {
         return "";
      } else {
         char[] multiplied = new char[amount];

         for (int i = 0; i < amount; i++) {
            multiplied[i] = c;
         }

         return new String(multiplied);
      }
   }

   public static String join(@Nullable Object[] strings) {
      return strings == null ? "" : join(strings, "", 0, strings.length);
   }

   public static String join(@Nullable Object[] strings, String delimiter) {
      return strings == null ? "" : join(strings, delimiter, 0, strings.length);
   }

   public static String join(@Nullable Object[] strings, String delimiter, int start, int end) {
      if (strings == null) {
         return "";
      } else {
         assert start >= 0 && start <= end && end <= strings.length : start + ", " + end + ", " + strings.length;

         if (start >= 0 && start < strings.length && start != end) {
            StringBuilder b = new StringBuilder(strings[start] + "");

            for (int i = start + 1; i < end; i++) {
               b.append(delimiter);
               b.append(strings[i]);
            }

            return b + "";
         } else {
            return "";
         }
      }
   }

   public static String join(@Nullable Iterable<?> strings) {
      return strings == null ? "" : join(strings.iterator(), "");
   }

   public static String join(@Nullable Iterable<?> strings, String delimiter) {
      return strings == null ? "" : join(strings.iterator(), delimiter);
   }

   public static String join(@Nullable Iterator<?> strings, String delimiter) {
      return join(strings, delimiter, delimiter);
   }

   public static String join(@Nullable Iterator<?> strings, String delimiter, String lastDelimiter) {
      if (strings != null && strings.hasNext()) {
         StringBuilder builder = new StringBuilder(String.valueOf(strings.next()));

         while (strings.hasNext()) {
            Object next = strings.next();
            builder.append(!strings.hasNext() ? lastDelimiter : delimiter);
            builder.append(next);
         }

         return builder.toString();
      } else {
         return "";
      }
   }

   public static String join(@Nullable Iterable<?> strings, String delimiter, String lastDelimiter) {
      return strings == null ? "" : join(strings.iterator(), delimiter, lastDelimiter);
   }

   public static int findLastDigit(String s, int start) {
      int end = start;

      while (end < s.length() && '0' <= s.charAt(end) && s.charAt(end) <= '9') {
         end++;
      }

      return end;
   }

   public static boolean containsAny(String s, String chars) {
      for (int i = 0; i < chars.length(); i++) {
         if (s.indexOf(chars.charAt(i)) != -1) {
            return true;
         }
      }

      return false;
   }

   public static boolean equals(String s1, String s2, boolean caseSensitive) {
      return caseSensitive ? s1.equals(s2) : s1.equalsIgnoreCase(s2);
   }

   public static boolean contains(String haystack, String needle, boolean caseSensitive) {
      return caseSensitive ? haystack.contains(needle) : haystack.toLowerCase().contains(needle.toLowerCase());
   }

   public static String replace(String haystack, String needle, String replacement, boolean caseSensitive) {
      return caseSensitive
         ? StringConcatFactory.makeConcatWithConstants<"makeConcatWithConstants","\u0001">(haystack.replace(needle, replacement))
         : StringConcatFactory.makeConcatWithConstants<"makeConcatWithConstants","\u0001">(
            haystack.replaceAll("(?ui)" + Pattern.quote(needle), Matcher.quoteReplacement(replacement))
         );
   }

   public static String replaceFirst(String haystack, String needle, String replacement, boolean caseSensitive) {
      return caseSensitive
         ? StringConcatFactory.makeConcatWithConstants<"makeConcatWithConstants","\u0001">(haystack.replaceFirst(needle, replacement))
         : StringConcatFactory.makeConcatWithConstants<"makeConcatWithConstants","\u0001">(haystack.replaceFirst("(?ui)" + Pattern.quote(needle), replacement));
   }

   public static byte[] hexStringToByteArray(String s) {
      int len = s.length();
      byte[] data = new byte[len / 2];

      for (int i = 0; i < len; i += 2) {
         data[i / 2] = (byte)((Character.digit(s.charAt(i), 16) << 4) + Character.digit(s.charAt(i + 1), 16));
      }

      return data;
   }

   public static int indexOfOutsideGroup(String string, char find, char groupOpen, char groupClose, int i) {
      int group = 0;

      while (i < string.length()) {
         char c = string.charAt(i);
         if (c == '\\') {
            i++;
         } else if (c == groupOpen) {
            group++;
         } else if (c == groupClose) {
            group--;
         } else if (c == find && group == 0) {
            return i;
         }

         i++;
      }

      return -1;
   }
}
