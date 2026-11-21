package com.magmaguy.elitemobs.magmacore.location;

import com.magmaguy.elitemobs.magmacore.location.api.LocationOwnership;
import com.magmaguy.elitemobs.magmacore.util.Logger;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Predicate;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public final class LocationQueryRegistry {
   private static final CopyOnWriteArrayList<DungeonLocator> dungeonLocators = new CopyOnWriteArrayList<>();
   private static final CopyOnWriteArrayList<RegionProtectionProvider> protectionProviders = new CopyOnWriteArrayList<>();
   private static final Map<String, LocationQueryRegistry.BuiltInRegistration> builtInProtectionProviders = new ConcurrentHashMap<>();
   private static final Map<String, Plugin> failedBuiltInAttempts = new ConcurrentHashMap<>();
   private static final AtomicBoolean warnedNoDungeonLocators = new AtomicBoolean(false);
   private static final AtomicBoolean warnedNoProtectionProviders = new AtomicBoolean(false);
   private static final Set<RegionProtectionProvider> warnedProtectionFailures = Collections.synchronizedSet(Collections.newSetFromMap(new WeakHashMap<>()));

   private LocationQueryRegistry() {
   }

   public static void registerDungeonLocator(DungeonLocator locator) {
      if (locator != null) {
         dungeonLocators.addIfAbsent(locator);
      }
   }

   public static void registerProtectionProvider(RegionProtectionProvider provider) {
      if (provider != null) {
         protectionProviders.addIfAbsent(provider);
      }
   }

   public static void unregisterDungeonLocator(DungeonLocator locator) {
      dungeonLocators.remove(locator);
   }

   public static void unregisterProtectionProvider(RegionProtectionProvider provider) {
      protectionProviders.remove(provider);
      warnedProtectionFailures.remove(provider);
   }

   public static boolean isInAnyDungeon(Location location) {
      if (location != null && location.getWorld() != null) {
         for (DungeonLocator locator : dungeonLocators) {
            try {
               if (locator.contains(location)) {
                  return true;
               }
            } catch (Throwable var4) {
               Logger.warn("DungeonLocator threw during query: " + var4.getMessage());
            }
         }

         if (LocationOwnership.hasKind(location, "dungeon")) {
            return true;
         } else {
            if (dungeonLocators.isEmpty()
               && Bukkit.getServicesManager().getRegistrations(Predicate.class).isEmpty()
               && warnedNoDungeonLocators.compareAndSet(false, true)) {
               Logger.warn(
                  "is_in_dungeon called but no dungeon locators or LocationOwnership providers are registered. If EliteMobs is installed, ensure it is up to date and loaded before this query."
               );
            }

            return false;
         }
      } else {
         return false;
      }
   }

   public static boolean isInAnyProtectedRegion(Location location) {
      if (location != null && location.getWorld() != null) {
         ensureBuiltInProtectionProviders();
         if (!failedBuiltInAttempts.isEmpty()) {
            return true;
         } else {
            for (RegionProtectionProvider provider : protectionProviders) {
               try {
                  if (provider.isProtected(location)) {
                     return true;
                  }
               } catch (Throwable var4) {
                  warnProtectionFailure(provider, var4);
                  return true;
               }
            }

            if (LocationOwnership.anyProtectedOwnerAt(location)) {
               return true;
            } else {
               if (protectionProviders.isEmpty()
                  && Bukkit.getServicesManager().getRegistrations(Predicate.class).isEmpty()
                  && warnedNoProtectionProviders.compareAndSet(false, true)) {
                  Logger.warn(
                     "is_protected called but no protection providers or LocationOwnership owners are registered. Install WorldGuard / GriefPrevention or have a content plugin register via LocationOwnership for this check to work."
                  );
               }

               return false;
            }
         }
      } else {
         return false;
      }
   }

   public static boolean canBuild(Player player, Location location) {
      if (player != null && location != null && location.getWorld() != null) {
         ensureBuiltInProtectionProviders();
         if (!failedBuiltInAttempts.isEmpty()) {
            return false;
         } else {
            for (RegionProtectionProvider provider : protectionProviders) {
               try {
                  if (!provider.canBuild(player, location)) {
                     return false;
                  }
               } catch (Throwable var5) {
                  warnProtectionFailure(provider, var5);
                  return false;
               }
            }

            return !LocationOwnership.anyProtectedOwnerAt(location);
         }
      } else {
         return false;
      }
   }

   public static int getDungeonLocatorCount() {
      return dungeonLocators.size();
   }

   private static void warnProtectionFailure(RegionProtectionProvider provider, Throwable failure) {
      if (warnedProtectionFailures.add(provider)) {
         Logger.warn(
            "RegionProtectionProvider '"
               + provider.providerName()
               + "' failed a protection query: "
               + failure.getMessage()
               + "; further failures suppressed until provider replacement"
         );
      }
   }

   public static int getProtectionProviderCount() {
      ensureBuiltInProtectionProviders();
      return protectionProviders.size();
   }

   public static void initializeBuiltInProtectionProviders() {
      ensureBuiltInProtectionProviders();
      if (protectionProviders.isEmpty()) {
         boolean wgInstalled = isPluginEnabled("WorldGuard");
         boolean gpInstalled = isPluginEnabled("GriefPrevention");
         if (!wgInstalled && !gpInstalled) {
            Logger.warn(
               "No protection providers registered — WorldGuard and GriefPrevention are both absent. em.location.is_protected() will always return false."
            );
         } else {
            Logger.warn(
               "No protection providers registered despite "
                  + (wgInstalled ? "WorldGuard" : "")
                  + (wgInstalled && gpInstalled ? " and " : "")
                  + (gpInstalled ? "GriefPrevention" : "")
                  + " being installed — check earlier 'Failed to attach ... provider' messages for the underlying class-load failure."
            );
         }
      }
   }

   public static void shutdown() {
      dungeonLocators.clear();
      protectionProviders.clear();
      warnedProtectionFailures.clear();
      builtInProtectionProviders.clear();
      failedBuiltInAttempts.clear();
      warnedNoDungeonLocators.set(false);
      warnedNoProtectionProviders.set(false);
   }

   private static void ensureBuiltInProtectionProviders() {
      refreshBuiltInProtection("WorldGuard", "com.magmaguy.elitemobs.magmacore.thirdparty.worldguard.WorldGuardProtectionProvider");
      refreshBuiltInProtection("GriefPrevention", "com.magmaguy.elitemobs.magmacore.thirdparty.griefprevention.GriefPreventionProtectionProvider");
   }

   private static void refreshBuiltInProtection(String pluginName, String providerClassName) {
      Plugin plugin = Bukkit.getPluginManager().getPlugin(pluginName);
      LocationQueryRegistry.BuiltInRegistration existing = builtInProtectionProviders.get(pluginName);
      if (plugin == null || !plugin.isEnabled() || existing == null || existing.plugin() != plugin) {
         if (plugin == null || !plugin.isEnabled() || failedBuiltInAttempts.get(pluginName) != plugin) {
            synchronized (builtInProtectionProviders) {
               plugin = Bukkit.getPluginManager().getPlugin(pluginName);
               existing = builtInProtectionProviders.get(pluginName);
               if (plugin != null && plugin.isEnabled()) {
                  if (existing == null || existing.plugin() != plugin) {
                     if (failedBuiltInAttempts.get(pluginName) != plugin) {
                        if (existing != null && builtInProtectionProviders.remove(pluginName, existing)) {
                           protectionProviders.remove(existing.provider());
                        }

                        try {
                           Class<?> cls = Class.forName(providerClassName, true, LocationQueryRegistry.class.getClassLoader());
                           if (cls.getDeclaredConstructor().newInstance() instanceof RegionProtectionProvider provider) {
                              LocationQueryRegistry.BuiltInRegistration registration = new LocationQueryRegistry.BuiltInRegistration(plugin, provider);
                              builtInProtectionProviders.put(pluginName, registration);
                              protectionProviders.addIfAbsent(provider);
                              failedBuiltInAttempts.remove(pluginName);
                              Logger.info("Registered " + provider.providerName() + " protection provider.");
                           }
                        } catch (ReflectiveOperationException | RuntimeException | LinkageError var10) {
                           failedBuiltInAttempts.put(pluginName, plugin);
                           Logger.warn("Failed to attach " + pluginName + " protection provider: " + var10.getMessage());
                        }
                     }
                  }
               } else {
                  if (existing != null && builtInProtectionProviders.remove(pluginName, existing)) {
                     protectionProviders.remove(existing.provider());
                  }

                  failedBuiltInAttempts.remove(pluginName);
               }
            }
         }
      }
   }

   private static boolean isPluginEnabled(String pluginName) {
      Plugin plugin = Bukkit.getPluginManager().getPlugin(pluginName);
      return plugin != null && plugin.isEnabled();
   }

   private record BuiltInRegistration(Plugin plugin, RegionProtectionProvider provider) {
   }
}
