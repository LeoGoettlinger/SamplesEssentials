package com.artillexstudios.axtrade.utils;

import com.artillexstudios.axtrade.AxTrade;
import com.artillexstudios.axtrade.libs.axapi.utils.StringUtils;
import com.artillexstudios.axtrade.libs.lamp.locales.LocaleReader;
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
         AxTrade.CONFIG.getString("prefix", "")
            + switch (s) {
               case "invalid-enum", "invalid-number", "invalid-uuid", "invalid-url", "invalid-boolean" -> AxTrade.LANG
                  .getString("commands.invalid-value")
                  .replace("%value%", "{0}");
               case "missing-argument" -> AxTrade.LANG.getString("commands.missing-argument").replace("%value%", "{0}");
               case "no-permission" -> AxTrade.LANG.getString("commands.no-permission");
               case "number-not-in-range" -> AxTrade.LANG
                  .getString("commands.out-of-range")
                  .replace("%number%", "{0}")
                  .replace("%min%", "{1}")
                  .replace("%max%", "{2}");
               case "must-be-player" -> AxTrade.LANG.getString("commands.player-only");
               case "must-be-console" -> AxTrade.LANG.getString("commands.console-only");
               case "invalid-player" -> AxTrade.LANG.getString("commands.invalid-player").replace("%player%", "{0}");
               case "invalid-selector" -> AxTrade.LANG.getString("commands.invalid-selector");
               default -> AxTrade.LANG.getString("commands.invalid-command");
            }
      );
   }

   @Override
   public Locale getLocale() {
      return this.locale;
   }
}
