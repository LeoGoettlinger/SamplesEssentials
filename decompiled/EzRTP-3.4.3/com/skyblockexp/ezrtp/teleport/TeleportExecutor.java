package com.skyblockexp.ezrtp.teleport;

import com.skyblockexp.ezrtp.config.RandomTeleportSettings;
import com.skyblockexp.ezrtp.message.MessageKey;
import com.skyblockexp.ezrtp.message.MessageProvider;
import com.skyblockexp.ezrtp.performance.PerformanceMonitor;
import com.skyblockexp.ezrtp.platform.PlatformScheduler;
import com.skyblockexp.ezrtp.statistics.RtpStatistics;
import com.skyblockexp.ezrtp.teleport.queue.TeleportQueueManager;
import com.skyblockexp.ezrtp.util.MessageUtil;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Supplier;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public final class TeleportExecutor {
   private final JavaPlugin plugin;
   private final PlatformScheduler scheduler;
   private final TeleportPreCheckHandler preCheckHandler;
   private final TeleportResultHandler resultHandler;
   private final TeleportQueueManager queueManager;
   private final Supplier<RandomTeleportSettings> defaultSettingsSupplier;
   private final MessageProvider messageProvider;
   private final RtpStatistics statistics;
   private final TeleportCostCalculator costCalculator;
   private final CountdownManager countdownManager;
   private final LocationFinder locationFinder;
   private final Set<UUID> activeTeleportAttempts = ConcurrentHashMap.newKeySet();
   private volatile PerformanceMonitor performanceMonitor;

   public TeleportExecutor(
      JavaPlugin plugin,
      PlatformScheduler scheduler,
      MessageProvider messageProvider,
      RtpStatistics statistics,
      TeleportCostCalculator costCalculator,
      CountdownManager countdownManager,
      LocationFinder locationFinder,
      TeleportQueueManager queueManager,
      Supplier<RandomTeleportSettings> defaultSettingsSupplier
   ) {
      this.plugin = plugin;
      this.scheduler = scheduler;
      this.preCheckHandler = new TeleportPreCheckHandler(plugin, scheduler, messageProvider, costCalculator);
      this.resultHandler = new TeleportResultHandler(plugin, scheduler, messageProvider, statistics, locationFinder);
      this.queueManager = queueManager;
      this.defaultSettingsSupplier = defaultSettingsSupplier;
      this.messageProvider = messageProvider;
      this.statistics = statistics;
      this.costCalculator = costCalculator;
      this.countdownManager = countdownManager;
      this.locationFinder = locationFinder;
      this.queueManager.setExecutionHandler(this::teleportQueuedPlayer);
   }

   public void setPerformanceMonitor(PerformanceMonitor performanceMonitor) {
      this.performanceMonitor = performanceMonitor;
   }

   public void teleportPlayer(Player player, TeleportReason reason) {
      this.teleportPlayer(player, this.defaultSettingsSupplier.get(), reason, null);
   }

   public void teleportPlayer(Player player, RandomTeleportSettings teleportSettings, TeleportReason reason) {
      this.teleportPlayer(player, teleportSettings, reason, null);
   }

   public void teleportPlayer(Player player, TeleportReason reason, Consumer<Boolean> callback) {
      this.teleportPlayer(player, this.defaultSettingsSupplier.get(), reason, callback);
   }

   public void teleportPlayer(Player player, RandomTeleportSettings teleportSettings, TeleportReason reason, Consumer<Boolean> callback) {
      UUID playerId = player.getUniqueId();
      if (!this.activeTeleportAttempts.add(playerId)) {
         if (callback != null) {
            callback.accept(false);
         }
      } else {
         boolean handedOff = false;

         try {
            if (teleportSettings != null) {
               Consumer<Boolean> trackedCallback = this.withActiveAttemptCleanup(playerId, callback);
               if (!this.preCheckHandler.checkMemorySafety(player, trackedCallback)) {
                  return;
               }

               if (!this.preCheckHandler.checkPayment(player, teleportSettings, reason)) {
                  trackedCallback.accept(false);
                  return;
               }

               if (!this.queueManager.enqueueIfNeeded(player, teleportSettings, reason)) {
                  handedOff = true;
                  this.countdownManager
                     .startCountdown(
                        player,
                        teleportSettings,
                        reason,
                        trackedCallback,
                        () -> this.performTeleport(player, teleportSettings, reason, trackedCallback, () -> this.clearActiveAttempt(playerId))
                     );
                  return;
               }

               trackedCallback.accept(false);
               return;
            }

            if (callback != null) {
               callback.accept(false);
            }
         } finally {
            if (!handedOff) {
               this.clearActiveAttempt(playerId);
            }
         }
      }
   }

   private void teleportQueuedPlayer(Player player, RandomTeleportSettings teleportSettings, TeleportReason reason, Runnable completionHook) {
      UUID playerId = player.getUniqueId();
      if (!this.activeTeleportAttempts.add(playerId)) {
         if (completionHook != null) {
            completionHook.run();
         }
      } else {
         Runnable trackedCompletionHook = () -> {
            try {
               if (completionHook != null) {
                  completionHook.run();
               }
            } finally {
               this.clearActiveAttempt(playerId);
            }
         };
         this.performTeleport(player, teleportSettings, reason, null, trackedCompletionHook);
      }
   }

   private Consumer<Boolean> withActiveAttemptCleanup(UUID playerId, Consumer<Boolean> callback) {
      return success -> {
         try {
            if (callback != null) {
               callback.accept(success);
            }
         } finally {
            this.clearActiveAttempt(playerId);
         }
      };
   }

   private void clearActiveAttempt(UUID playerId) {
      this.activeTeleportAttempts.remove(playerId);
   }

   private void performTeleport(
      Player player, RandomTeleportSettings teleportSettings, TeleportReason reason, Consumer<Boolean> callback, Runnable completionHook
   ) {
      RandomTeleportSettings effectiveSettings = teleportSettings.isAutoWorld()
         ? teleportSettings.withWorldName(player.getWorld().getName())
         : teleportSettings;
      World world = this.plugin.getServer().getWorld(effectiveSettings.getWorldName());
      if (world == null) {
         if (!effectiveSettings.isSuppressPlayerMessages()) {
            MessageUtil.send(player, this.messageProvider.format(MessageKey.WORLD_MISSING, Map.of("world", effectiveSettings.getWorldName()), player));
         }

         if (callback != null) {
            callback.accept(false);
         }

         if (completionHook != null) {
            completionHook.run();
         }
      } else {
         if (!effectiveSettings.isSuppressPlayerMessages()) {
            if (reason == TeleportReason.JOIN) {
               MessageUtil.send(player, this.messageProvider.format(MessageKey.JOIN_SEARCHING, player));
            } else {
               MessageUtil.send(player, this.messageProvider.format(MessageKey.TELEPORTING, player));
            }
         }

         long startTime = System.currentTimeMillis();
         CompletableFuture<SearchResult> locationSearchFuture = this.locationFinder.findSafeLocationAsync(world, effectiveSettings);
         boolean rareSearch = this.locationFinder.isRareSearch(effectiveSettings);
         long searchTimeoutMillis = this.resolveSearchTimeoutMillis(effectiveSettings, rareSearch);
         CompletableFuture<SearchResult> completionFuture = locationSearchFuture;
         if (searchTimeoutMillis > 0L) {
            completionFuture = locationSearchFuture.orTimeout(searchTimeoutMillis, TimeUnit.MILLISECONDS);
         }

         completionFuture.whenComplete(
            (result, throwable) -> {
               Runnable task = () -> {
                  long duration = System.currentTimeMillis() - startTime;
                  boolean success = false;
                  Biome biome = null;
                  boolean cacheHit = result != null && result.cacheHit();
                  boolean cacheChecked = result != null && result.cacheChecked();
                  Location validLocation = null;
                  boolean asyncTeleportStarted = false;

                  try {
                     if (throwable != null || result == null || result.location().isEmpty()) {
                        if (throwable != null && effectiveSettings.isDebugRejectionLoggingEnabled()) {
                           this.plugin
                              .getLogger()
                              .warning(
                                 "EzRTP debug: location search failed for player '"
                                    + player.getName()
                                    + "' in world '"
                                    + world.getName()
                                    + "' after "
                                    + duration
                                    + "ms: "
                                    + throwable.getClass().getSimpleName()
                                    + " - "
                                    + throwable.getMessage()
                              );
                        }

                        this.resultHandler.handleFailure(player, effectiveSettings, result, duration, biome, cacheHit, cacheChecked);
                        if (callback != null) {
                           callback.accept(false);
                        }

                        return;
                     }

                     validLocation = result.location().get();
                     if (!player.isOnline()) {
                        this.locationFinder.cacheValidLocation(validLocation, effectiveSettings);
                        this.statistics.recordPlayerOfflineOrCancelledFailure();
                        this.statistics.recordAttempt(false, duration, biome, cacheHit, cacheChecked);
                        if (callback != null) {
                           callback.accept(false);
                        }

                        return;
                     }

                     double resolvedCost = this.costCalculator.calculateCost(player, teleportSettings);
                     if (!this.costCalculator.requiresPayment(reason, resolvedCost) || this.costCalculator.withdraw(player, resolvedCost)) {
                        Location destination = TeleportDestinationAdjuster.adjustForSafety(validLocation, effectiveSettings);
                        Biome finalBiome = destination.getBlock().getBiome();
                        asyncTeleportStarted = true;
                        this.scheduler
                           .teleportAsync(player, destination)
                           .whenComplete(
                              (tpSuccess, tpErr) -> {
                                 boolean tpResult = Boolean.TRUE.equals(tpSuccess) && tpErr == null;
                                 if (tpResult) {
                                    this.resultHandler
                                       .handleSuccess(
                                          player, destination, result, effectiveSettings, duration, finalBiome, cacheHit, cacheChecked, validLocation
                                       );
                                 } else {
                                    this.locationFinder.cacheValidLocation(validLocation, effectiveSettings);
                                    if (!effectiveSettings.isSuppressPlayerMessages()) {
                                       MessageUtil.send(player, this.messageProvider.format(MessageKey.TELEPORT_FAILED, player));
                                    }

                                    this.statistics.recordTeleportApiFailure();
                                    this.statistics.recordAttempt(false, duration, finalBiome, cacheHit, cacheChecked);
                                 }

                                 if (callback != null) {
                                    callback.accept(tpResult);
                                 }

                                 if (completionHook != null) {
                                    completionHook.run();
                                 }

                                 PerformanceMonitor pmx = this.performanceMonitor;
                                 if (pmx != null) {
                                    pmx.afterOperation(duration, world.getName());
                                 }
                              }
                           );
                        return;
                     }

                     this.locationFinder.cacheValidLocation(validLocation, teleportSettings);
                     if (!effectiveSettings.isSuppressPlayerMessages()) {
                        MessageUtil.send(
                           player,
                           this.messageProvider.format(MessageKey.INSUFFICIENT_FUNDS, Map.of("cost", String.format(Locale.US, "%.2f", resolvedCost)), player)
                        );
                     }

                     this.statistics.recordEconomyFailure();
                     this.statistics.recordAttempt(false, duration, biome, cacheHit, cacheChecked);
                     if (callback != null) {
                        callback.accept(false);
                     }
                  } finally {
                     if (!asyncTeleportStarted) {
                        if (callback != null) {
                           callback.accept(success);
                        }

                        if (completionHook != null) {
                           completionHook.run();
                        }

                        PerformanceMonitor pm = this.performanceMonitor;
                        if (pm != null) {
                           pm.afterOperation(duration, world.getName());
                        }
                     }
                  }
               };

               try {
                  if (result != null && result.location().isPresent()) {
                     Location dest = result.location().get();
                     World destWorld = dest.getWorld();
                     if (destWorld != null) {
                        this.scheduler.executeRegion(destWorld, dest.getBlockX() >> 4, dest.getBlockZ() >> 4, task);
                     } else {
                        this.scheduler.executeGlobal(task);
                     }
                  } else {
                     this.scheduler.executeGlobal(task);
                  }
               } catch (IllegalStateException var15) {
                  if (callback != null) {
                     callback.accept(false);
                  }

                  if (completionHook != null) {
                     completionHook.run();
                  }
               }
            }
         );
      }
   }

   long resolveSearchTimeoutMillis(RandomTeleportSettings settings, boolean rareSearch) {
      if (settings != null && settings.getBiomeSearchSettings() != null) {
         int selectedLimit = rareSearch
            ? settings.getBiomeSearchSettings().getMaxWallClockMillisRare()
            : settings.getBiomeSearchSettings().getMaxWallClockMillis();
         return selectedLimit <= 0 ? 0L : Math.max(5000L, selectedLimit + 5000L);
      } else {
         return 30000L;
      }
   }

   private void handleTeleportFailure(
      Player player,
      RandomTeleportSettings teleportSettings,
      SearchResult result,
      long duration,
      Biome biome,
      boolean cacheHit,
      boolean cacheChecked,
      Consumer<Boolean> callback
   ) {
      this.resultHandler.handleFailure(player, teleportSettings, result, duration, biome, cacheHit, cacheChecked);
      if (callback != null) {
         callback.accept(false);
      }
   }

   public void shutdown() {
      this.countdownManager.shutdown();
      this.queueManager.shutdown();
   }
}
