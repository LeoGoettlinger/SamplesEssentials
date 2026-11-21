package com.magmaguy.elitemobs.mobconstructor;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.api.power.EliteLuaPowerService;
import com.magmaguy.elitemobs.magmacore.util.Logger;
import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.ServicePriority;

public final class EliteLuaPowerServiceModule {
   private static EliteLuaPowerServiceImpl implementation;

   private EliteLuaPowerServiceModule() {
   }

   public static void initialize() {
      if (implementation == null) {
         EliteLuaPowerServiceImpl candidate = new EliteLuaPowerServiceImpl();

         try {
            Bukkit.getPluginManager().registerEvents(candidate, MetadataHandler.PLUGIN);
            Bukkit.getServicesManager().register(EliteLuaPowerService.class, candidate, MetadataHandler.PLUGIN, ServicePriority.Normal);
            implementation = candidate;
         } catch (Error | RuntimeException var6) {
            try {
               Bukkit.getServicesManager().unregister(EliteLuaPowerService.class, candidate);
            } catch (RuntimeException var5) {
               var6.addSuppressed(var5);
            }

            try {
               HandlerList.unregisterAll(candidate);
            } catch (RuntimeException var4) {
               var6.addSuppressed(var4);
            }

            try {
               candidate.shutdown();
            } catch (RuntimeException var3) {
               var6.addSuppressed(var3);
            }

            throw var6;
         }
      }
   }

   public static void shutdown() {
      EliteLuaPowerServiceImpl current = implementation;
      if (current != null) {
         implementation = null;

         try {
            Bukkit.getServicesManager().unregister(EliteLuaPowerService.class, current);
         } catch (RuntimeException var4) {
            Logger.warn("Failed to unregister the EliteMobs Lua power service: " + var4.getMessage());
         }

         try {
            HandlerList.unregisterAll(current);
         } catch (RuntimeException var3) {
            Logger.warn("Failed to unregister EliteMobs Lua power listeners: " + var3.getMessage());
         }

         try {
            current.shutdown();
         } catch (RuntimeException var2) {
            Logger.warn("Failed to finish EliteMobs Lua power service shutdown: " + var2.getMessage());
         }
      }
   }

   static EliteLuaPowerServiceImpl implementation() {
      return implementation;
   }
}
