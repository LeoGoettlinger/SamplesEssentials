package com.skyblockexp.ezrtp.config.safety;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import org.bukkit.configuration.ConfigurationSection;

public final class ProtectionSettings {
   private static final List<String> DEFAULT_PROVIDERS = List.of("worldguard", "griefprevention", "teamsapi");
   private final boolean avoidClaims;
   private final List<String> providers;

   public ProtectionSettings(boolean avoidClaims, List<String> providers) {
      this.avoidClaims = avoidClaims;
      this.providers = providers != null ? Collections.unmodifiableList(providers) : Collections.emptyList();
   }

   public boolean isAvoidClaims() {
      return this.avoidClaims;
   }

   public List<String> getProviders() {
      return this.providers;
   }

   public static ProtectionSettings fromConfiguration(ConfigurationSection section) {
      return fromConfiguration(section, null);
   }

   public static ProtectionSettings fromConfiguration(ConfigurationSection section, ProtectionSettings fallback) {
      if (section == null) {
         return fallback != null ? fallback : new ProtectionSettings(false, Collections.emptyList());
      } else {
         boolean avoidClaims = section.getBoolean("avoid-claims", fallback != null && fallback.isAvoidClaims());
         List<String> configuredProviders = section.getStringList("providers");
         if (configuredProviders.isEmpty() && fallback != null) {
            configuredProviders = fallback.getProviders();
         }

         if (configuredProviders.isEmpty() && avoidClaims) {
            configuredProviders = DEFAULT_PROVIDERS;
         }

         return new ProtectionSettings(avoidClaims, normalizeProviders(configuredProviders));
      }
   }

   private static List<String> normalizeProviders(List<String> providers) {
      if (providers != null && !providers.isEmpty()) {
         List<String> normalized = new ArrayList<>();

         for (String provider : providers) {
            if (provider != null && !provider.isBlank()) {
               normalized.add(provider.trim().toLowerCase(Locale.ROOT));
            }
         }

         return normalized;
      } else {
         return Collections.emptyList();
      }
   }
}
