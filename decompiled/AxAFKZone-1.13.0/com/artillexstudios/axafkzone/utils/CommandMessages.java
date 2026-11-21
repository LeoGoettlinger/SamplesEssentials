package com.artillexstudios.axafkzone.utils;

import com.artillexstudios.axafkzone.AxAFKZone;
import com.artillexstudios.axafkzone.libs.axapi.utils.StringUtils;
import com.artillexstudios.axafkzone.libs.lamp.locales.LocaleReader;
import java.util.Locale;

public class CommandMessages implements LocaleReader {
   private final Locale locale = new Locale("en", "US");

   @Override
   public boolean containsKey(String s) {
      return true;
   }

   @Override
   public String get(String s) {
      return StringUtils.formatToString(
         AxAFKZone.CONFIG.getString("prefix", "")
            + switch (s) {
               case "invalid-enum", "invalid-number", "invalid-uuid", "invalid-url", "invalid-boolean" -> AxAFKZone.LANG
                  .getString("commands.invalid-value")
                  .replace("%value%", "{0}");
               case "missing-argument" -> AxAFKZone.LANG.getString("commands.missing-argument").replace("%value%", "{0}");
               case "no-permission" -> AxAFKZone.LANG.getString("commands.no-permission");
               case "number-not-in-range" -> AxAFKZone.LANG
                  .getString("commands.out-of-range")
                  .replace("%number%", "{0}")
                  .replace("%min%", "{1}")
                  .replace("%max%", "{2}");
               case "must-be-player" -> AxAFKZone.LANG.getString("commands.player-only");
               case "must-be-console" -> AxAFKZone.LANG.getString("commands.console-only");
               case "invalid-player" -> AxAFKZone.LANG.getString("commands.invalid-player").replace("%player%", "{0}");
               case "invalid-selector" -> AxAFKZone.LANG.getString("commands.invalid-selector");
               default -> AxAFKZone.LANG.getString("commands.invalid-command");
            }
      );
   }

   @Override
   public Locale getLocale() {
      return this.locale;
   }
}
