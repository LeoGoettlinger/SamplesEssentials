package com.donutorders.util;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

public final class NumberFormatter {
   private static final DecimalFormat COMMA_FORMAT;
   private static final DecimalFormat SUFFIX_FORMAT;

   private NumberFormatter() {
   }

   public static String format(double value) {
      if (value < 0.0) {
         return "-" + format(-value);
      } else if (value >= 1.0E9) {
         return SUFFIX_FORMAT.format(value / 1.0E9) + "B";
      } else if (value >= 1000000.0) {
         return SUFFIX_FORMAT.format(value / 1000000.0) + "M";
      } else {
         return value >= 1000.0 ? SUFFIX_FORMAT.format(value / 1000.0) + "K" : COMMA_FORMAT.format(value);
      }
   }

   public static String format(int value) {
      return format((double)value);
   }

   public static String formatPrice(double value) {
      return "$" + format(value);
   }

   static {
      DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.US);
      COMMA_FORMAT = new DecimalFormat("#,##0.##", symbols);
      SUFFIX_FORMAT = new DecimalFormat("#,##0.##", symbols);
   }
}
