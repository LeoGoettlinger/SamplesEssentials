package com.skyblockexp.ezrtp.protection;

import com.skyblockexp.teamsapi.api.TeamsAPI;
import com.skyblockexp.teamsapi.api.TeamsClaimService;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;

public final class TeamsApiProtectionProvider implements ProtectionProvider {
   private final Logger logger;
   private final boolean teamsApiPluginPresent;

   public TeamsApiProtectionProvider(PluginManager pluginManager, Logger logger) {
      this.logger = Objects.requireNonNull(logger, "logger");
      boolean present = false;
      if (pluginManager != null) {
         try {
            Plugin plugin = pluginManager.getPlugin("TeamsAPI");
            present = plugin != null && plugin.isEnabled();
         } catch (Exception var5) {
         }
      }

      this.teamsApiPluginPresent = present;
   }

   @Override
   public String getId() {
      return "teamsapi";
   }

   @Override
   public boolean isAvailable() {
      if (!this.teamsApiPluginPresent) {
         return false;
      } else {
         try {
            return TeamsAPI.isClaimAvailable();
         } catch (NoClassDefFoundError var2) {
            return false;
         }
      }
   }

   @Override
   public boolean isLocationProtected(Location location) {
      if (this.teamsApiPluginPresent && location != null) {
         World world = location.getWorld();
         if (world == null) {
            return false;
         } else {
            try {
               TeamsClaimService claimService = TeamsAPI.getClaimService();
               if (claimService == null) {
                  return false;
               } else {
                  Chunk chunk = location.getChunk();
                  return claimService.isClaimed(world.getName(), chunk.getX(), chunk.getZ());
               }
            } catch (RuntimeException | NoClassDefFoundError var5) {
               this.logger.log(Level.WARNING, "Failed to query TeamsAPI claims for RTP protection check.", (Throwable)var5);
               return false;
            }
         }
      } else {
         return false;
      }
   }
}
