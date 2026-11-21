package com.artillexstudios.axafkzone.utils;

import com.artillexstudios.axafkzone.AxAFKZone;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.NumberFormat.Style;
import java.util.Locale;
import org.jetbrains.annotations.Nullable;

public class NumberUtils {
   private static NumberFormat formatter = new DecimalFormat(AxAFKZone.CONFIG.getString("number-formatting.formatted", "#,###.##"));
   private static NumberFormat shortFormat = new DecimalFormat(AxAFKZone.CONFIG.getString("number-formatting.formatted", "#,###.##"));

   public static void reload() {
      String[] lang = AxAFKZone.CONFIG.getString("number-formatting.short", "en_US").split("_");
      shortFormat = DecimalFormat.getCompactNumberInstance(Locale.of(lang[0], lang[1]), Style.SHORT);
      int mode = AxAFKZone.CONFIG.getInt("number-formatting.mode", 0);
      switch (mode) {
         case 0:
            formatter = new DecimalFormat(AxAFKZone.CONFIG.getString("number-formatting.formatted", "#,###.##"));
            break;
         case 1:
            formatter = shortFormat;
            break;
         case 2:
            formatter = null;
      }
   }

   public static String formatNumber(double number) {
      return formatter == null ? number + "" : formatter.format(number);
   }

   @Nullable
   public static Double parseNumber(String number) {
      try {
         return shortFormat.parse(number.toUpperCase()).doubleValue();
      } catch (ParseException var2) {
         return null;
      }
   }
}
