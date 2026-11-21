package com.skyblockexp.ezrtp.config;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.logging.Logger;
import org.bukkit.configuration.ConfigurationSection;

public final class EzCountdownIntegrationSettings {
   private static final String DEFAULT_FORMAT = "<yellow>Teleporting in <white>{formatted}</white>...</yellow>";
   private final boolean enabled;
   private final List<String> displayTypes;
   private final String formatMessage;

   private EzCountdownIntegrationSettings(boolean enabled, List<String> displayTypes, String formatMessage) {
      this.enabled = enabled;
      this.displayTypes = List.copyOf(displayTypes);
      this.formatMessage = formatMessage;
   }

   public static EzCountdownIntegrationSettings disabled() {
      return new EzCountdownIntegrationSettings(false, List.of("ACTION_BAR"), "<yellow>Teleporting in <white>{formatted}</white>...</yellow>");
   }

   public static EzCountdownIntegrationSettings fromConfiguration(ConfigurationSection section, Logger logger) {
      if (section == null) {
         return disabled();
      } else {
         boolean enabled = section.getBoolean("enabled", false);
         List<String> displayTypes = new ArrayList<>();
         if (section.isList("display-types")) {
            for (Object entry : section.getList("display-types")) {
               if (entry instanceof String s && !s.isBlank()) {
                  displayTypes.add(s.toUpperCase(Locale.ROOT));
               }
            }
         }

         if (displayTypes.isEmpty()) {
            displayTypes.add("ACTION_BAR");
         }

         String format = section.getString("format", "<yellow>Teleporting in <white>{formatted}</white>...</yellow>");
         return new EzCountdownIntegrationSettings(
            enabled, displayTypes, format != null ? format : "<yellow>Teleporting in <white>{formatted}</white>...</yellow>"
         );
      }
   }

   public boolean isEnabled() {
      return this.enabled;
   }

   public List<String> getDisplayTypes() {
      return this.displayTypes;
   }

   public String getFormatMessage() {
      return this.formatMessage;
   }
}
