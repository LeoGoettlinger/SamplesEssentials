package com.artillexstudios.axrankmenu.utils;

import com.artillexstudios.axrankmenu.AxRankMenu;
import com.artillexstudios.axrankmenu.libs.axapi.utils.StringUtils;
import java.util.Locale;
import revxrsal.commands.locales.LocaleReader;

public class CommandMessages implements LocaleReader {
   private final Locale locale = new Locale("en", "US");

   @Override
   public boolean containsKey(String s) {
      return true;
   }

   @Override
   public String get(String s) {
      return StringUtils.formatToString(
         AxRankMenu.CONFIG.getString("prefix", "")
            + switch (s) {
               case "invalid-enum", "invalid-number", "invalid-uuid", "invalid-url", "invalid-boolean" -> AxRankMenu.LANG
                  .getString("commands.invalid-value")
                  .replace("%value%", "{0}");
               case "missing-argument" -> AxRankMenu.LANG.getString("commands.missing-argument").replace("%value%", "{0}");
               case "no-permission" -> AxRankMenu.LANG.getString("commands.no-permission");
               case "number-not-in-range" -> AxRankMenu.LANG
                  .getString("commands.out-of-range")
                  .replace("%number%", "{0}")
                  .replace("%min%", "{1}")
                  .replace("%max%", "{2}");
               case "must-be-player" -> AxRankMenu.LANG.getString("commands.player-only");
               case "must-be-console" -> AxRankMenu.LANG.getString("commands.console-only");
               case "invalid-player" -> AxRankMenu.LANG.getString("commands.invalid-player").replace("%player%", "{0}");
               case "invalid-selector" -> AxRankMenu.LANG.getString("commands.invalid-selector");
               default -> AxRankMenu.LANG.getString("commands.invalid-command");
            }
      );
   }

   @Override
   public Locale getLocale() {
      return this.locale;
   }
}
