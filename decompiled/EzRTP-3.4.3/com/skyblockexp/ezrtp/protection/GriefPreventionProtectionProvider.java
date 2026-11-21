package com.skyblockexp.ezrtp.protection;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.Location;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;

public final class GriefPreventionProtectionProvider implements ProtectionProvider {
   private final Logger logger;
   private final boolean available;
   private final Object dataStore;
   private final Method getClaimAtMethod;

   public GriefPreventionProtectionProvider(PluginManager pluginManager, Logger logger) {
      this.logger = Objects.requireNonNull(logger, "logger");
      Plugin plugin = pluginManager != null ? pluginManager.getPlugin("GriefPrevention") : null;
      if (plugin != null && plugin.isEnabled()) {
         Object resolvedDataStore = null;
         Method resolvedGetClaimAt = null;
         boolean resolvedAvailable = false;

         try {
            ClassLoader classLoader = plugin.getClass().getClassLoader();
            Class<?> pluginClass = classLoader.loadClass("me.ryanhamshire.GriefPrevention.GriefPrevention");
            Field dataStoreField = pluginClass.getField("dataStore");
            resolvedDataStore = dataStoreField.get(plugin);
            if (resolvedDataStore != null) {
               try {
                  Class<?> dataStoreInterface = classLoader.loadClass("me.ryanhamshire.GriefPrevention.DataStore");
                  Class<?> claimClass = classLoader.loadClass("me.ryanhamshire.GriefPrevention.Claim");

                  try {
                     resolvedGetClaimAt = dataStoreInterface.getMethod("getClaimAt", Location.class, boolean.class, boolean.class, claimClass);
                  } catch (NoSuchMethodException var19) {
                     try {
                        resolvedGetClaimAt = resolvedDataStore.getClass().getMethod("getClaimAt", Location.class, boolean.class, boolean.class, claimClass);
                     } catch (NoSuchMethodException var18) {
                        try {
                           resolvedGetClaimAt = resolvedDataStore.getClass().getMethod("getClaimAt", Location.class, boolean.class, boolean.class);
                        } catch (NoSuchMethodException var17) {
                           try {
                              resolvedGetClaimAt = resolvedDataStore.getClass().getMethod("getClaimAt", Location.class);
                           } catch (NoSuchMethodException var16) {
                              logger.log(Level.WARNING, "GriefPrevention DataStore does not have a compatible getClaimAt method.", (Throwable)var16);
                           }
                        }
                     }
                  }

                  if (resolvedGetClaimAt != null) {
                     resolvedAvailable = true;
                  } else {
                     logger.warning("GriefPrevention getClaimAt method not found; RTP protection will be disabled for this provider.");
                  }
               } catch (ClassNotFoundException var20) {
                  logger.log(Level.WARNING, "GriefPrevention classes not found during reflection setup.", (Throwable)var20);
               }
            } else {
               logger.warning("GriefPrevention dataStore field is null; RTP protection will be disabled for this provider.");
            }
         } catch (RuntimeException | ReflectiveOperationException var21) {
            logger.log(Level.WARNING, "Failed to initialize GriefPrevention RTP protection hook.", (Throwable)var21);
         }

         this.available = resolvedAvailable;
         this.dataStore = resolvedDataStore;
         this.getClaimAtMethod = resolvedGetClaimAt;
      } else {
         this.available = false;
         this.dataStore = null;
         this.getClaimAtMethod = null;
      }
   }

   @Override
   public String getId() {
      return "griefprevention";
   }

   @Override
   public boolean isAvailable() {
      return this.available;
   }

   @Override
   public boolean isLocationProtected(Location location) {
      if (this.available && location != null && this.dataStore != null && this.getClaimAtMethod != null) {
         try {
            int paramCount = this.getClaimAtMethod.getParameterCount();
            Object claim;
            if (paramCount == 4) {
               claim = this.getClaimAtMethod.invoke(this.dataStore, location, false, false, null);
            } else if (paramCount == 3) {
               claim = this.getClaimAtMethod.invoke(this.dataStore, location, false, false);
            } else {
               claim = this.getClaimAtMethod.invoke(this.dataStore, location);
            }

            return claim != null;
         } catch (RuntimeException | ReflectiveOperationException var4) {
            this.logger.log(Level.WARNING, "Failed to query GriefPrevention claims for RTP protection check.", (Throwable)var4);
            return false;
         }
      } else {
         return false;
      }
   }
}
