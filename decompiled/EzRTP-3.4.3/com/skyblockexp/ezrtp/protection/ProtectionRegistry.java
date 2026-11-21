package com.skyblockexp.ezrtp.protection;

import com.skyblockexp.ezrtp.config.safety.ProtectionSettings;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Logger;
import org.bukkit.Location;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public final class ProtectionRegistry {
   private final Map<String, ProtectionProvider> providers = new HashMap<>();
   private final Logger logger;

   public ProtectionRegistry(JavaPlugin plugin) {
      this(plugin.getServer() != null ? plugin.getServer().getPluginManager() : null, plugin.getLogger());
   }

   public ProtectionRegistry(PluginManager pluginManager, Logger logger) {
      this.logger = logger != null ? logger : Logger.getAnonymousLogger();
      this.registerProvider(new WorldGuardProtectionProvider(pluginManager, this.logger));
      this.registerProvider(new GriefPreventionProtectionProvider(pluginManager, this.logger));
      this.registerProvider(new TeamsApiProtectionProvider(pluginManager, this.logger));
   }

   public Optional<String> findProtectionProvider(Location location, ProtectionSettings settings) {
      if (settings != null && settings.isAvoidClaims()) {
         for (String providerId : settings.getProviders()) {
            ProtectionProvider provider = this.providers.get(providerId);
            if (provider != null && provider.isAvailable() && provider.isLocationProtected(location)) {
               return Optional.of(providerId);
            }
         }

         return Optional.empty();
      } else {
         return Optional.empty();
      }
   }

   public void warnMissingProviders(ProtectionSettings settings) {
      if (settings != null && settings.isAvoidClaims()) {
         List<String> missing = new ArrayList<>();

         for (String providerId : settings.getProviders()) {
            ProtectionProvider provider = this.providers.get(providerId);
            if (provider == null || !provider.isAvailable()) {
               missing.add(providerId);
            }
         }

         if (!missing.isEmpty()) {
            if (missing.size() == settings.getProviders().size()) {
               this.logger
                  .warning(
                     "EzRTP: avoid-claims is enabled but none of the configured protection providers "
                        + missing
                        + " are available. Protected regions will NOT be avoided. Install WorldGuard or GriefPrevention, or set avoid-claims: false in rtp.yml."
                  );
            } else {
               for (String providerIdx : missing) {
                  this.logger
                     .warning(
                        "EzRTP protection provider '"
                           + providerIdx
                           + "' is configured but not available; protected regions will not be avoided for this provider."
                     );
               }
            }
         }
      }
   }

   public WorldGuardProtectionProvider getWorldGuardProvider() {
      ProtectionProvider provider = this.providers.get("worldguard");
      return provider instanceof WorldGuardProtectionProvider worldGuardProtectionProvider ? worldGuardProtectionProvider : null;
   }

   private void registerProvider(ProtectionProvider provider) {
      this.providers.put(provider.getId(), provider);
   }
}
