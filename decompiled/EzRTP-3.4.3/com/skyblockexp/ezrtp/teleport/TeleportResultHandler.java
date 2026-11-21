package com.skyblockexp.ezrtp.teleport;

import com.skyblockexp.ezrtp.config.RandomTeleportSettings;
import com.skyblockexp.ezrtp.config.effects.ParticleSettings;
import com.skyblockexp.ezrtp.message.MessageKey;
import com.skyblockexp.ezrtp.message.MessageProvider;
import com.skyblockexp.ezrtp.platform.PlatformScheduler;
import com.skyblockexp.ezrtp.statistics.RtpStatistics;
import com.skyblockexp.ezrtp.util.MessageUtil;
import com.skyblockexp.ezrtp.util.compat.ParticleCompat;
import java.util.Map;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public final class TeleportResultHandler {
   private final JavaPlugin plugin;
   private final PlatformScheduler scheduler;
   private final MessageProvider messageProvider;
   private final RtpStatistics statistics;
   private final LocationFinder locationFinder;

   public TeleportResultHandler(
      JavaPlugin plugin, PlatformScheduler scheduler, MessageProvider messageProvider, RtpStatistics statistics, LocationFinder locationFinder
   ) {
      this.plugin = plugin;
      this.scheduler = scheduler;
      this.messageProvider = messageProvider;
      this.statistics = statistics;
      this.locationFinder = locationFinder;
   }

   public void handleSuccess(
      Player player,
      Location destination,
      SearchResult result,
      RandomTeleportSettings teleportSettings,
      long duration,
      Biome biome,
      boolean cacheHit,
      boolean cacheChecked,
      Location validLocation
   ) {
      if (!teleportSettings.isSuppressPlayerMessages()) {
         this.sendSuccessMessage(player, destination, result);
      }

      this.playParticles(destination, teleportSettings.getParticleSettings());
      this.statistics.recordAttempt(true, duration, biome, cacheHit, cacheChecked);
      if (!cacheHit && validLocation != null) {
         this.locationFinder.cacheValidLocation(validLocation, teleportSettings);
      }

      this.unloadOldChunk(player.getLocation(), destination);
   }

   public void handleFailure(
      Player player, RandomTeleportSettings teleportSettings, SearchResult result, long duration, Biome biome, boolean cacheHit, boolean cacheChecked
   ) {
      SearchResult.SearchLimitType limitType = result != null ? result.limitType() : SearchResult.SearchLimitType.NONE;
      boolean cacheFallbackFailure = result != null && result.noCacheAvailable() && limitType == SearchResult.SearchLimitType.NONE;
      String reasonClass;
      if (cacheFallbackFailure) {
         reasonClass = "CACHE_FALLBACK_NO_CACHE";
         if (!teleportSettings.isSuppressPlayerMessages()) {
            MessageUtil.send(player, this.messageProvider.format(MessageKey.TELEPORT_FALLBACK_NO_CACHE, player));
         }

         this.triggerCacheWarmup(teleportSettings);
         this.statistics.recordGenericSearchErrorFailure();
      } else if (limitType != SearchResult.SearchLimitType.NONE && limitType != SearchResult.SearchLimitType.BIOME_REJECTIONS) {
         reasonClass = "SEARCH_LIMIT_" + limitType.name();
         if (!teleportSettings.isSuppressPlayerMessages()) {
            switch (teleportSettings.getBiomeSearchSettings().getFailoverMode()) {
               case ABORT:
                  MessageUtil.send(player, this.messageProvider.format(MessageKey.TELEPORT_FAILED, player));
                  break;
               default:
                  MessageUtil.send(player, this.messageProvider.format(MessageKey.TELEPORT_FAILED_SEARCH, player));
            }
         }

         this.statistics.recordTimeoutFailure();
      } else if (result != null && result.noValidBiome()) {
         reasonClass = "NO_VALID_BIOME";
         if (!teleportSettings.isSuppressPlayerMessages()) {
            if (!teleportSettings.isEnableFallbackToCache()) {
               MessageUtil.send(player, this.messageProvider.format(MessageKey.TELEPORT_FAILED_SEARCH, player));
            } else {
               MessageUtil.send(player, this.messageProvider.format(MessageKey.TELEPORT_FAILED_BIOME, player));
            }
         }

         this.statistics.recordBiomeFailure(null);
      } else {
         reasonClass = "SEARCH_EXHAUSTED_OR_GENERIC";
         if (!teleportSettings.isSuppressPlayerMessages()) {
            if (!teleportSettings.isEnableFallbackToCache()) {
               MessageUtil.send(player, this.messageProvider.format(MessageKey.TELEPORT_FAILED_SEARCH, player));
            } else {
               MessageUtil.send(player, this.messageProvider.format(MessageKey.TELEPORT_FAILED, player));
            }
         }

         this.statistics.recordGenericSearchErrorFailure();
      }

      this.logFailureDebug(player, teleportSettings, result, reasonClass, limitType, duration, cacheHit);
      this.statistics.recordAttempt(false, duration, biome, cacheHit, cacheChecked);
   }

   private void logFailureDebug(
      Player player,
      RandomTeleportSettings teleportSettings,
      SearchResult result,
      String reasonClass,
      SearchResult.SearchLimitType limitType,
      long duration,
      boolean cacheHit
   ) {
      if (teleportSettings != null && teleportSettings.isDebugRejectionLoggingEnabled()) {
         try {
            String playerName = player != null ? player.getName() : "unknown";
            String worldName = player != null && player.getWorld() != null ? player.getWorld().getName() : "unknown";
            this.plugin
               .getLogger()
               .warning(
                  "EzRTP debug: failure breadcrumb player="
                     + playerName
                     + " world="
                     + worldName
                     + " reasonClass="
                     + reasonClass
                     + " limitType="
                     + limitType
                     + " noValidBiome="
                     + (result != null && result.noValidBiome())
                     + " fallbackUsed="
                     + (result != null && result.fallbackUsed())
                     + " noCacheAvailable="
                     + (result != null && result.noCacheAvailable())
                     + " durationMs="
                     + duration
                     + " cacheHit="
                     + cacheHit
               );
         } catch (Exception var11) {
         }
      }
   }

   private void sendSuccessMessage(Player player, Location destination, SearchResult result) {
      if (result != null && result.fallbackUsed()) {
         MessageUtil.send(
            player,
            this.messageProvider
               .format(
                  MessageKey.TELEPORT_FALLBACK_SUCCESS,
                  Map.of("x", String.valueOf(destination.getBlockX()), "z", String.valueOf(destination.getBlockZ())),
                  player
               )
         );
      } else {
         MessageUtil.send(
            player,
            this.messageProvider
               .format(
                  MessageKey.TELEPORT_SUCCESS,
                  Map.of("x", String.valueOf(destination.getBlockX()), "z", String.valueOf(destination.getBlockZ()), "world", destination.getWorld().getName()),
                  player
               )
         );
      }
   }

   private void playParticles(Location destination, ParticleSettings particleSettings) {
      if (particleSettings != null && particleSettings.isEnabled()) {
         World world = destination.getWorld();
         if (world != null) {
            ParticleCompat.spawnParticle(
               world,
               particleSettings.getParticle(),
               destination,
               particleSettings.getCount(),
               particleSettings.getOffsetX(),
               particleSettings.getOffsetY(),
               particleSettings.getOffsetZ(),
               particleSettings.getExtra(),
               null,
               particleSettings.isForce()
            );
         }
      }
   }

   private void unloadOldChunk(Location oldLocation, Location newLocation) {
      World oldWorld = oldLocation.getWorld();
      if (oldWorld != null) {
         int oldChunkX = oldLocation.getBlockX() >> 4;
         int oldChunkZ = oldLocation.getBlockZ() >> 4;
         this.scheduler
            .executeRegion(
               oldWorld,
               oldChunkX,
               oldChunkZ,
               () -> {
                  try {
                     Chunk oldChunk = oldLocation.getWorld().getChunkAt(oldLocation);
                     if (oldLocation.getWorld().isChunkLoaded(oldChunk.getX(), oldChunk.getZ()) && oldChunk.getEntities().length == 0) {
                        boolean hasPlayers = false;

                        for (Player p : this.plugin.getServer().getOnlinePlayers()) {
                           if (p.getWorld().equals(oldLocation.getWorld())
                              && p.getLocation().getBlockX() >> 4 == oldChunk.getX()
                              && p.getLocation().getBlockZ() >> 4 == oldChunk.getZ()) {
                              hasPlayers = true;
                              break;
                           }
                        }

                        if (!hasPlayers) {
                           oldLocation.getWorld().unloadChunkRequest(oldChunk.getX(), oldChunk.getZ());
                        }
                     }
                  } catch (Exception var6) {
                  }
               }
            );
      }
   }

   private void triggerCacheWarmup(RandomTeleportSettings teleportSettings) {
      if (teleportSettings != null
         && teleportSettings.getPreCacheSettings() != null
         && teleportSettings.getPreCacheSettings().isEnabled()
         && LocationValidator.hasBiomeFilters(teleportSettings)) {
         World world = Bukkit.getWorld(teleportSettings.getWorldName());
         if (world != null) {
            this.locationFinder.generateSafeLocationForCache(world, teleportSettings).thenAccept(location -> {
               if (location != null) {
                  this.locationFinder.cacheValidLocation(location, teleportSettings);
               }
            });
         }
      }
   }
}
