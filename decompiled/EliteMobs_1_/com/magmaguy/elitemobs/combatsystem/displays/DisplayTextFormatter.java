package com.magmaguy.elitemobs.combatsystem.displays;

import com.magmaguy.elitemobs.magmacore.util.Round;
import java.util.Locale;

final class DisplayTextFormatter {
   private static final double THOUSAND = 1000.0;
   private static final double MILLION = 1000000.0;
   private static final double BILLION = 1.0E9;
   private static final double TRILLION = 1.0E12;

   private DisplayTextFormatter() {
   }

   static String number(double value) {
      if (value < 0.0) {
         return "-" + number(-value);
      } else if (value >= 1.0E12) {
         return Round.twoDecimalPlaces(value / 1.0E12) + "T";
      } else if (value >= 1.0E9) {
         return Round.twoDecimalPlaces(value / 1.0E9) + "B";
      } else if (value >= 1000000.0) {
         return Round.twoDecimalPlaces(value / 1000000.0) + "M";
      } else {
         return value >= 1000.0 ? Round.twoDecimalPlaces(value / 1000.0) + "K" : String.valueOf(Round.twoDecimalPlaces(value));
      }
   }

   static String percentage(double ratio) {
      double percent = Math.max(0.0, ratio) * 100.0;
      if (percent >= 99.95) {
         return "100%";
      } else if (percent >= 10.0) {
         return Math.round(percent) + "%";
      } else {
         return percent >= 1.0 ? Round.twoDecimalPlaces(percent) + "%" : String.format(Locale.US, "%.3f%%", percent);
      }
   }
}
