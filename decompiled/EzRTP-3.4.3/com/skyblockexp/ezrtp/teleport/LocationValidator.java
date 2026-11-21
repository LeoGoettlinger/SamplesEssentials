package com.skyblockexp.ezrtp.teleport;

import com.skyblockexp.ezrtp.config.RandomTeleportSettings;
import com.skyblockexp.ezrtp.config.safety.SafetySettings;
import com.skyblockexp.ezrtp.protection.ProtectionRegistry;
import com.skyblockexp.ezrtp.unsafe.UnsafeLocationCause;
import com.skyblockexp.ezrtp.util.compat.WorldBorderCompat;
import java.util.Optional;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.plugin.java.JavaPlugin;

public final class LocationValidator {
   private final JavaPlugin plugin;
   private volatile ProtectionRegistry protectionRegistry;

   public LocationValidator(JavaPlugin plugin, ProtectionRegistry protectionRegistry) {
      this.plugin = plugin;
      this.protectionRegistry = protectionRegistry;
   }

   public void setProtectionRegistry(ProtectionRegistry protectionRegistry) {
      this.protectionRegistry = protectionRegistry;
   }

   public boolean isSafe(Location location, RandomTeleportSettings currentSettings) {
      return this.checkSafety(location, currentSettings).isEmpty();
   }

   public Optional<UnsafeLocationCause> checkSafety(Location location, RandomTeleportSettings currentSettings) {
      if (location != null && currentSettings != null) {
         Block blockBelow = location.clone().subtract(0.0, 1.0, 0.0).getBlock();
         Block destination = location.getBlock();
         Material typeBelow = blockBelow.getType();
         int y = location.getBlockY();
         int minY = currentSettings.getMinY() != null ? currentSettings.getMinY() : getWorldMinHeight(location.getWorld());
         int maxY = currentSettings.getMaxY() != null ? currentSettings.getMaxY() : getWorldMaxHeight(location.getWorld());
         if (y < minY || y > maxY) {
            this.debugReject(currentSettings, location, "Y out of range: " + y + " not in [" + minY + ", " + maxY + "]");
            return Optional.of(UnsafeLocationCause.OUT_OF_BOUNDS);
         } else if (destination.isLiquid()) {
            this.debugReject(currentSettings, location, "Destination is inside liquid: " + destination.getType());
            return Optional.of(destination.getType() == Material.LAVA ? UnsafeLocationCause.LAVA : UnsafeLocationCause.LIQUID);
         } else {
            Block blockAbove = location.clone().add(0.0, 1.0, 0.0).getBlock();
            if (blockAbove.isLiquid()) {
               Environment env = location.getWorld().getEnvironment();
               Material aboveType = blockAbove.getType();
               SafetySettings safety = currentSettings.getSafetySettings();
               if (env == Environment.NETHER && aboveType == Material.LAVA && safety.isRejectLavaAboveInNether()) {
                  this.debugReject(currentSettings, location, "Lava above destination in Nether: " + aboveType);
                  return Optional.of(UnsafeLocationCause.LAVA);
               }

               if (env == Environment.NORMAL && aboveType == Material.WATER && safety.isRejectWaterAboveInOverworld()) {
                  this.debugReject(currentSettings, location, "Water above destination in Overworld: " + aboveType);
                  return Optional.of(UnsafeLocationCause.LIQUID_SURFACE);
               }
            }

            boolean waterSurface = typeBelow == Material.WATER && !destination.isLiquid();
            if (currentSettings.getUnsafeBlocks().contains(typeBelow) && !waterSurface) {
               this.debugReject(currentSettings, location, "Unsafe block below: " + typeBelow);
               UnsafeLocationCause cause = typeBelow != Material.LAVA && typeBelow != Material.MAGMA_BLOCK
                  ? UnsafeLocationCause.UNSAFE_BLOCK
                  : UnsafeLocationCause.LAVA;
               return Optional.of(cause);
            } else if (!blockBelow.getType().isSolid() && !waterSurface) {
               this.debugReject(currentSettings, location, "Block below not solid: " + typeBelow);
               return Optional.of(UnsafeLocationCause.VOID);
            } else if (!WorldBorderCompat.isInside(location)) {
               this.debugReject(currentSettings, location, "Outside world border");
               return Optional.of(UnsafeLocationCause.OUT_OF_BOUNDS);
            } else {
               return Optional.empty();
            }
         }
      } else {
         return Optional.of(UnsafeLocationCause.OTHER);
      }
   }

   public boolean isBiomeAllowed(Location location, RandomTeleportSettings currentSettings) {
      if (location == null || currentSettings == null) {
         return false;
      } else if (currentSettings.getBiomeInclude().isEmpty() && currentSettings.getBiomeExclude().isEmpty()) {
         return true;
      } else {
         Biome biome = location.getBlock().getBiome();
         return !currentSettings.getBiomeExclude().isEmpty() && currentSettings.getBiomeExclude().contains(biome)
            ? false
            : currentSettings.getBiomeInclude().isEmpty() || currentSettings.getBiomeInclude().contains(biome);
      }
   }

   public boolean isProtectedByClaims(Location location, RandomTeleportSettings currentSettings) {
      ProtectionRegistry registry = this.protectionRegistry;
      return registry != null && location != null && currentSettings != null
         ? registry.findProtectionProvider(location, currentSettings.getProtectionSettings()).isPresent()
         : false;
   }

   public static boolean hasBiomeFilters(RandomTeleportSettings settings) {
      return settings != null
         && settings.isBiomeSystemEnabled()
         && settings.isBiomeFilteringEnabled()
         && (!settings.getBiomeInclude().isEmpty() || !settings.getBiomeExclude().isEmpty());
   }

   private void debugReject(RandomTeleportSettings settings, Location loc, String reason) {
      if (settings != null && settings.isDebugRejectionLoggingEnabled()) {
         this.plugin.getLogger().info("RTP rejected " + loc + ": " + reason);
      }
   }

   private static int getWorldMinHeight(World world) {
      try {
         return (Integer)World.class.getMethod("getMinHeight").invoke(world);
      } catch (ReflectiveOperationException var2) {
         return 0;
      }
   }

   private static int getWorldMaxHeight(World world) {
      try {
         return (Integer)World.class.getMethod("getMaxHeight").invoke(world);
      } catch (ReflectiveOperationException var2) {
         return world.getMaxHeight();
      }
   }
}
