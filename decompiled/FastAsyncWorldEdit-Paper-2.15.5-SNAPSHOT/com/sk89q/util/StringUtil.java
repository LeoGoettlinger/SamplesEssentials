package com.sk89q.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Pattern;

public final class StringUtil {
   private StringUtil() {
   }

   public static String trimLength(String str, int len) {
      return str.length() > len ? str.substring(0, len) : str;
   }

   public static String joinString(String[] str, String delimiter, int initialIndex) {
      if (str.length == 0) {
         return "";
      } else {
         StringBuilder buffer = new StringBuilder(str[initialIndex]);

         for (int i = initialIndex + 1; i < str.length; i++) {
            buffer.append(delimiter).append(str[i]);
         }

         return buffer.toString();
      }
   }

   public static String joinQuotedString(String[] str, String delimiter, int initialIndex, String quote) {
      if (str.length == 0) {
         return "";
      } else {
         StringBuilder buffer = new StringBuilder();
         buffer.append(quote);
         buffer.append(str[initialIndex]);
         buffer.append(quote);

         for (int i = initialIndex + 1; i < str.length; i++) {
            buffer.append(delimiter).append(quote).append(str[i]).append(quote);
         }

         return buffer.toString();
      }
   }

   public static String joinString(String[] str, String delimiter) {
      return joinString(str, delimiter, 0);
   }

   public static String joinString(Object[] str, String delimiter, int initialIndex) {
      if (str.length == 0) {
         return "";
      } else {
         StringBuilder buffer = new StringBuilder(str[initialIndex].toString());

         for (int i = initialIndex + 1; i < str.length; i++) {
            buffer.append(delimiter).append(str[i]);
         }

         return buffer.toString();
      }
   }

   public static String joinString(int[] str, String delimiter, int initialIndex) {
      if (str.length == 0) {
         return "";
      } else {
         StringBuilder buffer = new StringBuilder(Integer.toString(str[initialIndex]));

         for (int i = initialIndex + 1; i < str.length; i++) {
            buffer.append(delimiter).append(str[i]);
         }

         return buffer.toString();
      }
   }

   public static String joinString(Collection<?> str, String delimiter, int initialIndex) {
      if (str.isEmpty()) {
         return "";
      } else {
         StringBuilder buffer = new StringBuilder();
         int i = 0;

         for (Object o : str) {
            if (i >= initialIndex) {
               if (i > 0) {
                  buffer.append(delimiter);
               }

               buffer.append(o);
            }

            i++;
         }

         return buffer.toString();
      }
   }

   public static int getLevenshteinDistance(String s, String t) {
      if (s != null && t != null) {
         int n = s.length();
         int m = t.length();
         if (n == 0) {
            return m;
         } else if (m == 0) {
            return n;
         } else {
            int[] p = new int[n + 1];
            int[] d = new int[n + 1];
            int i = 0;

            while (i <= n) {
               p[i] = i++;
            }

            for (int j = 1; j <= m; j++) {
               char tj = t.charAt(j - 1);
               d[0] = j;

               for (int var11 = 1; var11 <= n; var11++) {
                  int cost = s.charAt(var11 - 1) == tj ? 0 : 1;
                  d[var11] = Math.min(Math.min(d[var11 - 1] + 1, p[var11] + 1), p[var11 - 1] + cost);
               }

               int[] _d = p;
               p = d;
               d = _d;
            }

            return p[n];
         }
      } else {
         throw new IllegalArgumentException("Strings must not be null");
      }
   }

   public static <T extends Enum<?>> T lookup(Map<String, T> lookup, String name, boolean fuzzy) {
      String testName = name.replaceAll("[ _]", "").toLowerCase(Locale.ROOT);
      T type = (T)lookup.get(testName);
      if (type != null) {
         return type;
      } else if (!fuzzy) {
         return null;
      } else {
         int minDist = -1;

         for (Entry<String, T> entry : lookup.entrySet()) {
            String key = entry.getKey();
            if (key.charAt(0) == testName.charAt(0)) {
               int dist = getLevenshteinDistance(key, testName);
               if ((dist < minDist || minDist == -1) && dist < 2) {
                  minDist = dist;
                  type = (T)entry.getValue();
               }
            }
         }

         return type;
      }
   }

   public static List<String> parseListInQuotes(String[] input, char delimiter, char quoteOpen, char quoteClose) {
      return parseListInQuotes(input, delimiter, quoteOpen, quoteClose, false);
   }

   public static List<String> parseListInQuotes(String[] input, char delimiter, char quoteOpen, char quoteClose, boolean appendLeftover) {
      List<String> parsableBlocks = new ArrayList<>();
      StringBuilder buffer = new StringBuilder();

      for (String split : input) {
         if (split.indexOf(quoteOpen) != -1 && split.indexOf(quoteClose) == -1) {
            buffer.append(split).append(delimiter);
         } else if (split.indexOf(quoteClose) != -1 && split.indexOf(quoteOpen) == -1) {
            buffer.append(split);
            parsableBlocks.add(buffer.toString());
            buffer = new StringBuilder();
         } else if (buffer.length() == 0) {
            parsableBlocks.add(split);
         } else {
            buffer.append(split).append(delimiter);
         }
      }

      if (appendLeftover && buffer.length() != 0) {
         parsableBlocks.add(buffer.delete(buffer.length() - 1, buffer.length()).toString());
      }

      return parsableBlocks;
   }

   public static List<String> parseListInQuotes(String[] input, char delimiter, char[] quoteOpen, char[] quoteClose, boolean appendLeftover) {
      List<String> parsableBlocks = new ArrayList<>();
      StringBuilder buffer = new StringBuilder();
      int quotes = quoteOpen.length;
      if (quotes != quoteClose.length) {
         throw new Error("Mismatched quoteOpen and quoteClose lengths");
      } else {
         for (String split : input) {
            boolean quoteHandled = false;

            for (int i = 0; i < quotes; i++) {
               if (split.indexOf(quoteOpen[i]) != -1 && split.indexOf(quoteClose[i]) == -1) {
                  buffer.append(split).append(delimiter);
                  quoteHandled = true;
                  break;
               }

               if (split.indexOf(quoteClose[i]) != -1 && split.indexOf(quoteOpen[i]) == -1) {
                  buffer.append(split);
                  parsableBlocks.add(buffer.toString());
                  buffer = new StringBuilder();
                  quoteHandled = true;
                  break;
               }
            }

            if (!quoteHandled) {
               if (buffer.length() == 0) {
                  parsableBlocks.add(split);
               } else {
                  buffer.append(split).append(delimiter);
               }
            }
         }

         if (appendLeftover && buffer.length() != 0) {
            parsableBlocks.add(buffer.delete(buffer.length() - 1, buffer.length()).toString());
         }

         return parsableBlocks;
      }
   }

   public static Pattern convertGlobToRegex(String glob) {
      return Pattern.compile("^" + glob.replace("*", ".*").replace("?", ".") + "$");
   }

   public static List<String> split(String input, char delimiter, char open, char close) {
      if (input.indexOf(open) == -1 && input.indexOf(close) == -1) {
         return Arrays.asList(input.split(String.valueOf(delimiter), -1));
      } else {
         int level = 0;
         int begin = 0;
         List<String> split = new ArrayList<>();

         for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            if (c == delimiter && level == 0) {
               split.add(input.substring(begin, i));
               begin = i + 1;
            } else if (c == open) {
               level++;
            } else if (c == close) {
               level--;
            }
         }

         if (begin <= input.length()) {
            split.add(input.substring(begin));
         }

         return split;
      }
   }
}
