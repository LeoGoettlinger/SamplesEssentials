package com.ranull.graves.bundledlibraries.imdanix.text;

import com.ranull.graves.libraries.kyori.adventure.text.format.NamedTextColor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class MiniTranslator {
   public static final Set<MiniTranslator.Option> DEFAULT_OPTIONS = Collections.unmodifiableSet(
      EnumSet.of(
         MiniTranslator.Option.COLOR,
         MiniTranslator.Option.FORMAT,
         MiniTranslator.Option.RESET,
         MiniTranslator.Option.GRADIENT,
         MiniTranslator.Option.FAST_RESET,
         MiniTranslator.Option.DOUBLE_TO_ESCAPE
      )
   );

   private MiniTranslator() {
   }

   @NotNull
   public static String toMini(@NotNull String text) {
      return toMini(text, DEFAULT_OPTIONS);
   }

   @NotNull
   public static String toMini(@NotNull String text, @NotNull MiniTranslator.Option... options) {
      return toMini(text, EnumSet.copyOf(Arrays.asList(options)));
   }

   @NotNull
   public static String toMini(@NotNull String text, @NotNull Collection<MiniTranslator.Option> options) {
      text = text.replace('§', '&');
      if (options.contains(MiniTranslator.Option.DOUBLE_TO_ESCAPE)) {
         text = text.replace("&&", "§");
      }

      String colorTagStart = options.contains(MiniTranslator.Option.VERBOSE_HEX_COLOR) ? "color:#" : "#";
      if (options.contains(MiniTranslator.Option.HEX_COLOR_STANDALONE)) {
         text = replaceHexColorStandalone(text, colorTagStart);
      }

      List<String> order = new ArrayList<>(2);
      StringBuilder builder = new StringBuilder();
      boolean fastReset = options.contains(MiniTranslator.Option.FAST_RESET);
      boolean closeColors = options.contains(MiniTranslator.Option.CLOSE_COLORS);
      boolean hadColor = false;
      int index = 0;
      int nextIndex = text.indexOf(38);

      for (int length = text.length(); index < length; nextIndex = text.indexOf(38, ++index)) {
         if (nextIndex == -1) {
            builder.append(text, index, length);
            break;
         }

         builder.append(text, index, nextIndex);
         index = nextIndex + 1;
         if (index >= length) {
            builder.append('&');
            break;
         }

         char symbol = text.charAt(index);
         String tag = tagByChar(symbol, options);
         if (tag == null) {
            builder.append('&').append(symbol);
         } else {
            switch (tag) {
               case "hex_color":
                  if (symbol == '#') {
                     if (length > index + 6 && isHexPattern(text, index + 1)) {
                        handleClosing(order, builder, hadColor, fastReset, closeColors);
                        hadColor = true;
                        String builtTag = colorTagStart + text.substring(index + 1, index + 7);
                        builder.append('<').append(builtTag).append('>');
                        index += 6;
                        order.add(builtTag);
                        break;
                     }
                  } else if (length > index + 12) {
                     String color = extractLegacyHex(text, index + 1);
                     if (color != null) {
                        handleClosing(order, builder, hadColor, fastReset, closeColors);
                        hadColor = true;
                        String builtTag = colorTagStart + color;
                        builder.append('<').append(builtTag).append('>');
                        index += 12;
                        order.add(builtTag);
                        break;
                     }
                  }

                  builder.append('&').append(symbol);
                  break;
               case "gradient":
                  int endIndex = -1;

                  for (int inner = index + 1; inner < length; inner++) {
                     char inCh = Character.toLowerCase(text.charAt(inner));
                     if (inCh == '@') {
                        endIndex = inner;
                        break;
                     }

                     if (('a' > inCh || inCh > 'z') && ('0' > inCh || inCh > '9') && inCh != '#' && inCh != '-') {
                        break;
                     }
                  }

                  String[] split;
                  if (endIndex != -1 && (split = text.substring(index + 1, endIndex).split("-")).length != 1) {
                     List<String> colors = new ArrayList<>(split.length);

                     for (String color : split) {
                        if (color.length() == 1) {
                           color = colorByChar(color.charAt(0));
                           if (color == null) {
                              break;
                           }
                        } else if (color.startsWith("#") && (color.length() < 7 || !isHexPattern(color, 1)) || NamedTextColor.NAMES.value(color) == null) {
                           break;
                        }

                        colors.add(color);
                     }

                     if (colors.size() == split.length) {
                        index = endIndex;
                        handleClosing(order, builder, hadColor, fastReset, closeColors);
                        hadColor = true;
                        builder.append("<gradient:").append(String.join(":", colors)).append('>');
                        order.add(tag);
                     }
                  } else {
                     builder.append("&@");
                  }
                  break;
               case "reset":
                  order.clear();
                  hadColor = false;
                  builder.append("<reset>");
                  break;
               case "b":
               case "u":
               case "st":
               case "i":
               case "obf":
                  order.add(tag);
                  builder.append('<').append(tag).append('>');
                  break;
               default:
                  handleClosing(order, builder, hadColor, fastReset, closeColors);
                  hadColor = true;
                  order.add(tag);
                  builder.append('<').append(tag).append('>');
            }
         }
      }

      if (closeColors) {
         handleClosing(order, builder, hadColor, false, true);
      }

      return builder.toString().replace('§', '&');
   }

   @Nullable
   private static String extractLegacyHex(String input, int from) {
      StringBuilder builder = new StringBuilder(6);
      int i = from + 1;

      for (int end = from + 12; i <= end; i += 2) {
         char ch = input.charAt(i);
         if (!isHexDigit(ch)) {
            return null;
         }

         builder.append(ch);
      }

      return builder.toString();
   }

   private static String replaceHexColorStandalone(String text, String colorTagStart) {
      StringBuilder result = new StringBuilder();
      int index = 0;

      while (index < text.length()) {
         int nextIndex = text.indexOf(35, index);
         if (nextIndex == -1) {
            result.append(text, index, text.length());
            break;
         }

         if (isHexColorStandalone(text, nextIndex)) {
            result.append(text, index, nextIndex).append('<').append(colorTagStart).append(text, nextIndex + 1, nextIndex + 7).append('>');
            index = nextIndex + 7;
         } else {
            result.append(text, index, nextIndex + 1);
            index = nextIndex + 1;
         }
      }

      return result.toString();
   }

   private static boolean isHexColorStandalone(String text, int index) {
      if (index + 6 >= text.length()) {
         return false;
      } else {
         char prevChar = index == 0 ? 32 : text.charAt(index - 1);
         char nextChar = index + 7 >= text.length() ? 32 : text.charAt(index + 7);
         if (prevChar == '&') {
            return false;
         } else if (prevChar == '<' && nextChar == '>') {
            return false;
         } else {
            return prevChar != ':' || nextChar != '>' && nextChar != ':' ? isHexPattern(text, index + 1) : false;
         }
      }
   }

   private static void handleClosing(List<String> order, StringBuilder builder, boolean hadColor, boolean fastReset, boolean closeColors) {
      if (fastReset && order.size() > 1) {
         builder.append("<reset>");
      } else {
         int i = order.size() - 1;

         for (int until = hadColor && !closeColors ? 1 : 0; i >= until; i--) {
            builder.append("</").append(order.get(i)).append('>');
         }
      }

      order.clear();
   }

   @Nullable
   private static String tagByChar(char ch, Collection<MiniTranslator.Option> options) {
      if (isHexDigit(ch)) {
         return !options.contains(MiniTranslator.Option.COLOR) ? null : colorByChar(ch);
      } else if (isHexPrefix(ch)) {
         return !options.contains(MiniTranslator.Option.COLOR) ? null : "hex_color";
      } else if (isFormatChar(ch)) {
         if (!options.contains(MiniTranslator.Option.FORMAT)) {
            return null;
         } else {
            return switch (ch) {
               case 'K', 'k' -> "obf";
               case 'L', 'l' -> "b";
               case 'M', 'm' -> "st";
               case 'N', 'n' -> "u";
               case 'O', 'o' -> "i";
               default -> throw new IllegalStateException("Provided impossible format symbol '" + ch + "'");
            };
         }
      } else if (ch != 'r' && ch != 'R') {
         if (ch != '@') {
            return null;
         } else {
            return !options.contains(MiniTranslator.Option.GRADIENT) ? null : "gradient";
         }
      } else {
         return !options.contains(MiniTranslator.Option.RESET) ? null : "reset";
      }
   }

   @Nullable
   private static String colorByChar(char ch) {
      return switch (ch) {
         case '0' -> "black";
         case '1' -> "dark_blue";
         case '2' -> "dark_green";
         case '3' -> "dark_aqua";
         case '4' -> "dark_red";
         case '5' -> "dark_purple";
         case '6' -> "gold";
         case '7' -> "gray";
         case '8' -> "dark_gray";
         case '9' -> "blue";
         default -> null;
         case 'A', 'a' -> "green";
         case 'B', 'b' -> "aqua";
         case 'C', 'c' -> "red";
         case 'D', 'd' -> "light_purple";
         case 'E', 'e' -> "yellow";
         case 'F', 'f' -> "white";
      };
   }

   private static boolean isHexPattern(String str, int from) {
      int index = from;

      for (int end = from + 6; index < end; index++) {
         if (!isHexDigit(str.charAt(index))) {
            return false;
         }
      }

      return true;
   }

   private static boolean isHexDigit(char ch) {
      return switch (ch) {
         case '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F', 'a', 'b', 'c', 'd', 'e', 'f' -> true;
         default -> false;
      };
   }

   private static boolean isHexPrefix(char ch) {
      return switch (ch) {
         case '#', 'X', 'x' -> true;
         default -> false;
      };
   }

   private static boolean isFormatChar(char ch) {
      return switch (ch) {
         case 'K', 'L', 'M', 'N', 'O', 'k', 'l', 'm', 'n', 'o' -> true;
         default -> false;
      };
   }

   public static enum Option {
      COLOR,
      HEX_COLOR_STANDALONE,
      FORMAT,
      RESET,
      VERBOSE_HEX_COLOR,
      GRADIENT,
      FAST_RESET,
      CLOSE_COLORS,
      DOUBLE_TO_ESCAPE;
   }
}
