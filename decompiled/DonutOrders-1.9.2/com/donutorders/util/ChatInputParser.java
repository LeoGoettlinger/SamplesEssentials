package com.donutorders.util;

import java.util.Locale;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.regex.Pattern;

public final class ChatInputParser {
   private static final Pattern COLOR_CODES = Pattern.compile("(?i)[§&][0-9a-fk-orx]");
   private static final Pattern HEX_CODES = Pattern.compile("(?i)[§&]#?[0-9a-f]{6}");
   private static final Pattern MINI_TAGS = Pattern.compile("<[^>]*>");

   private ChatInputParser() {
   }

   public static String normalize(String raw) {
      if (raw == null) {
         return "";
      } else {
         String text = HEX_CODES.matcher(raw).replaceAll("");
         text = COLOR_CODES.matcher(text).replaceAll("");
         text = MINI_TAGS.matcher(text).replaceAll("");
         return text.trim();
      }
   }

   public static boolean isCancel(String raw, String keyword) {
      String expected = normalize(keyword);
      if (expected.isEmpty()) {
         expected = "cancel";
      }

      return normalize(raw).equalsIgnoreCase(expected);
   }

   public static OptionalInt parseAmount(String raw) {
      String text = normalizeForNumber(raw);
      if (text.isEmpty()) {
         return OptionalInt.empty();
      } else {
         try {
            if (!text.contains(".") && !text.contains("e") && !text.contains("E")) {
               int value = Integer.parseInt(text);
               return value <= 0 ? OptionalInt.empty() : OptionalInt.of(value);
            } else {
               double value = Double.parseDouble(text);
               return !(value <= 0.0) && !(value > 2.147483647E9) && value == Math.rint(value) ? OptionalInt.of((int)value) : OptionalInt.empty();
            }
         } catch (NumberFormatException var4) {
            return OptionalInt.empty();
         }
      }
   }

   public static OptionalDouble parsePrice(String raw) {
      String text = normalizeForNumber(raw);
      if (text.isEmpty()) {
         return OptionalDouble.empty();
      } else {
         try {
            double value = Double.parseDouble(text);
            return !(value <= 0.0) && !Double.isNaN(value) && !Double.isInfinite(value) ? OptionalDouble.of(value) : OptionalDouble.empty();
         } catch (NumberFormatException var4) {
            return OptionalDouble.empty();
         }
      }
   }

   private static String normalizeForNumber(String raw) {
      String text = normalize(raw).replace(" ", "").replace("_", "");
      text = text.replace(",", "");
      return text.toLowerCase(Locale.ROOT);
   }
}
