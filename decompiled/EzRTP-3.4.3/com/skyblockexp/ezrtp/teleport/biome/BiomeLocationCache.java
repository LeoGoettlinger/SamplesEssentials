package com.skyblockexp.ezrtp.teleport.biome;

import com.skyblockexp.ezrtp.config.RandomTeleportSettings;
import com.skyblockexp.ezrtp.platform.PlatformRuntimeRegistry;
import com.skyblockexp.ezrtp.platform.PlatformScheduler;
import com.skyblockexp.ezrtp.platform.PlatformTask;
import com.skyblockexp.ezrtp.teleport.ChunkyWarmupCoordinator;
import com.skyblockexp.ezrtp.teleport.RandomTeleportService;
import com.skyblockexp.ezrtp.util.compat.BiomeCompat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.atomic.AtomicLong;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.plugin.java.JavaPlugin;

public final class BiomeLocationCache {
   public static final int DEFAULT_MAX_LOCATIONS_PER_BIOME = 16;
   public static final int DEFAULT_CACHE_WARMUP_SIZE = 8;
   public static final long DEFAULT_EXPIRATION_MINUTES = 10L;
   private static final long WARMUP_DELAY_TICKS = 100L;
   private static final long WARMUP_BATCH_DELAY_TICKS = 2L;
   private static final int WARMUP_BATCH_SIZE = 5;
   private static final int DUPLICATE_LOCATION_DISTANCE_SQUARED = 100;
   private final JavaPlugin plugin;
   private final ChunkyWarmupCoordinator chunkyCoordinator;
   private final Map<String, Map<Biome, Deque<BiomeLocationCache.CachedLocation>>> worldCaches;
   private final int maxLocationsPerBiome;
   private final long expirationMillis;
   private final int warmupSize;
   private PlatformTask warmupTask;
   private PlatformScheduler platformScheduler;
   private volatile boolean enabled;
   private final AtomicLong evictionCount = new AtomicLong(0L);

   public BiomeLocationCache(JavaPlugin plugin) {
      this(plugin, 16, 8, 10L, null);
   }

   public BiomeLocationCache(JavaPlugin plugin, int maxLocationsPerBiome, int warmupSize, long expirationMinutes) {
      this(plugin, maxLocationsPerBiome, warmupSize, expirationMinutes, null);
   }

   public BiomeLocationCache(JavaPlugin plugin, int maxLocationsPerBiome, int warmupSize, long expirationMinutes, ChunkyWarmupCoordinator chunkyCoordinator) {
      this(plugin, maxLocationsPerBiome, warmupSize, expirationMinutes, chunkyCoordinator, null);
   }

   public BiomeLocationCache(
      JavaPlugin plugin,
      int maxLocationsPerBiome,
      int warmupSize,
      long expirationMinutes,
      ChunkyWarmupCoordinator chunkyCoordinator,
      PlatformScheduler platformScheduler
   ) {
      this.plugin = plugin;
      this.worldCaches = new ConcurrentHashMap<>();
      this.maxLocationsPerBiome = maxLocationsPerBiome;
      this.warmupSize = warmupSize;
      this.expirationMillis = expirationMinutes * 60L * 1000L;
      this.enabled = true;
      this.chunkyCoordinator = chunkyCoordinator;
      this.platformScheduler = platformScheduler;
   }

   private PlatformScheduler effectiveScheduler() {
      return this.platformScheduler != null ? this.platformScheduler : PlatformRuntimeRegistry.get().scheduler();
   }

   public void setEnabled(boolean enabled) {
      this.enabled = enabled;
      if (!enabled) {
         this.clear();
      }
   }

   public boolean isEnabled() {
      return this.enabled;
   }

   public Location get(World world, Set<Biome> includeList, Set<Biome> excludeList) {
      if (this.enabled && world != null) {
         Map<Biome, Deque<BiomeLocationCache.CachedLocation>> biomeCache = this.worldCaches.get(world.getName());
         if (biomeCache != null && !biomeCache.isEmpty()) {
            if (includeList != null && !includeList.isEmpty() || excludeList != null && !excludeList.isEmpty()) {
               if (includeList != null && !includeList.isEmpty()) {
                  List<Biome> targetBiomes = new ArrayList<>(includeList);
                  Collections.shuffle(targetBiomes);
                  long now = System.currentTimeMillis();

                  for (Biome biome : targetBiomes) {
                     Deque<BiomeLocationCache.CachedLocation> locations = biomeCache.get(biome);
                     if (locations != null && !locations.isEmpty()) {
                        Iterator<BiomeLocationCache.CachedLocation> it = locations.iterator();

                        while (it.hasNext()) {
                           BiomeLocationCache.CachedLocation cached = it.next();
                           if (cached.isExpired(now)) {
                              it.remove();
                           } else {
                              if (excludeList == null || excludeList.isEmpty() || !excludeList.contains(biome)) {
                                 it.remove();
                                 return cached.location();
                              }

                              it.remove();
                           }
                        }
                     }
                  }

                  return null;
               } else {
                  return null;
               }
            } else {
               return null;
            }
         } else {
            return null;
         }
      } else {
         return null;
      }
   }

   public void cache(Location location) {
      if (this.enabled && location != null && location.getWorld() != null) {
         Biome biome = location.getBlock().getBiome();
         String worldName = location.getWorld().getName();
         Map<Biome, Deque<BiomeLocationCache.CachedLocation>> biomeCache = this.worldCaches.computeIfAbsent(worldName, k -> new ConcurrentHashMap<>());
         Deque<BiomeLocationCache.CachedLocation> locations = biomeCache.computeIfAbsent(biome, k -> new ConcurrentLinkedDeque<>());
         Location finalLocation = location;
         int scanLimit = Math.min(locations.size(), 10);
         Iterator<BiomeLocationCache.CachedLocation> tailIter = locations.descendingIterator();

         for (int i = 0; i < scanLimit; i++) {
            if (tailIter.next().location().distanceSquared(finalLocation) < 100.0) {
               return;
            }
         }

         while (locations.size() >= this.maxLocationsPerBiome) {
            locations.pollFirst();
            this.evictionCount.incrementAndGet();
         }

         locations.addLast(new BiomeLocationCache.CachedLocation(location.clone(), System.currentTimeMillis()));
         Runtime runtime = Runtime.getRuntime();
         long freeMemoryMb = (runtime.freeMemory() + (runtime.maxMemory() - runtime.totalMemory())) / 1048576L;
         this.plugin
            .getLogger()
            .fine(
               String.format(
                  "[BiomeCache] Cached location for %s in world '%s' (total: %d, Memory: %dMB free)",
                  BiomeCompat.safeName(biome),
                  worldName,
                  locations.size(),
                  freeMemoryMb
               )
            );
      }
   }

   public Location getRandomCachedLocation(World world) {
      if (this.enabled && world != null) {
         Map<Biome, Deque<BiomeLocationCache.CachedLocation>> biomeCache = this.worldCaches.get(world.getName());
         if (biomeCache != null && !biomeCache.isEmpty()) {
            List<Biome> biomes = new ArrayList<>(biomeCache.keySet());
            Collections.shuffle(biomes);
            long now = System.currentTimeMillis();

            for (Biome biome : biomes) {
               Deque<BiomeLocationCache.CachedLocation> locations = biomeCache.get(biome);
               if (locations != null) {
                  BiomeLocationCache.CachedLocation head;
                  while ((head = locations.peekFirst()) != null && head.isExpired(now)) {
                     locations.pollFirst();
                  }

                  if (head != null) {
                     locations.pollFirst();
                     return head.location();
                  }
               }
            }

            return null;
         } else {
            return null;
         }
      } else {
         return null;
      }
   }

   public void clear() {
      this.worldCaches.clear();
   }

   public void clear(String worldName) {
      if (worldName != null) {
         this.worldCaches.remove(worldName);
      }
   }

   public void startWarmup(World world, RandomTeleportSettings settings, RandomTeleportService teleportService) {
      if (this.enabled && world != null && settings != null && teleportService != null) {
         if (!settings.getBiomeInclude().isEmpty() || !settings.getBiomeExclude().isEmpty()) {
            if (this.warmupTask != null) {
               this.warmupTask.cancel();
               this.warmupTask = null;
            }

            this.warmupTask = this.effectiveScheduler().executeGlobalDelayed(() -> this.performWarmup(world, settings, teleportService, 0), 100L);
         }
      }
   }

   private void performWarmup(World world, RandomTeleportSettings settings, RandomTeleportService teleportService, int completedCount) {
      Runtime runtime = Runtime.getRuntime();
      long freeMemoryMb = (runtime.freeMemory() + (runtime.maxMemory() - runtime.totalMemory())) / 1048576L;
      long totalMemoryMb = runtime.totalMemory() / 1048576L;
      if (freeMemoryMb < 256L) {
         this.plugin
            .getLogger()
            .warning(
               String.format(
                  "[BiomeCache] Skipping warmup for world '%s' due to low memory: %dMB free / %dMB total", world.getName(), freeMemoryMb, totalMemoryMb
               )
            );
      } else if (this.enabled && completedCount < this.warmupSize) {
         this.plugin
            .getLogger()
            .fine(
               String.format(
                  "[BiomeCache] Starting warmup batch for world '%s' (completed: %d/%d, Memory: %dMB free / %dMB total)",
                  world.getName(),
                  completedCount,
                  this.warmupSize,
                  freeMemoryMb,
                  totalMemoryMb
               )
            );
         if (this.chunkyCoordinator != null) {
            Set<Long> chunks = this.chunkyCoordinator.getChunksForWorld(world.getName());
            if (chunks != null && !chunks.isEmpty()) {
               int batchSize = Math.min(5, Math.min(this.warmupSize - completedCount, chunks.size()));
               long memoryPerLocationEstimate = 50L;
               long estimatedMemoryKb = batchSize * memoryPerLocationEstimate;
               long freeMemoryKb = (runtime.freeMemory() + (runtime.maxMemory() - runtime.totalMemory())) / 1024L;
               int effectiveBatchSize = batchSize;
               if (freeMemoryKb < estimatedMemoryKb * 2L) {
                  effectiveBatchSize = Math.max(1, (int)(freeMemoryKb / (memoryPerLocationEstimate * 2L)));
                  this.plugin
                     .getLogger()
                     .info(
                        String.format("[BiomeCache] Reducing warmup batch size to %d due to memory constraints (%dKB free)", effectiveBatchSize, freeMemoryKb)
                     );
               }

               this.plugin
                  .getLogger()
                  .fine(
                     String.format(
                        "[BiomeCache] Processing %d chunk-based warmup locations for world '%s' (Memory: %dMB free)",
                        effectiveBatchSize,
                        world.getName(),
                        freeMemoryMb
                     )
                  );
               List<CompletableFuture<Location>> futures = new ArrayList<>();
               Iterator<Long> iterator = chunks.iterator();

               for (int i = 0; i < effectiveBatchSize && iterator.hasNext(); i++) {
                  Long key = iterator.next();
                  int chunkX = (int)(key >> 32);
                  int chunkZ = key.intValue();
                  CompletableFuture<Location> future = teleportService.generateSafeLocationForChunk(world, chunkX, chunkZ, settings);
                  futures.add(future);
                  future.thenAccept(location -> {
                     if (location != null) {
                        this.cache(location);
                        if (this.chunkyCoordinator != null) {
                           this.chunkyCoordinator.recordWarmupProcessed();
                        }
                     }
                  });
               }

               int batchSnapshot = effectiveBatchSize;
               CompletableFuture<Void> allOf = CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
               allOf.thenRun(() -> this.effectiveScheduler().executeGlobal(() -> {
                  int newCompleted = completedCount + batchSnapshot;
                  if (newCompleted < this.warmupSize) {
                     this.effectiveScheduler().executeGlobalDelayed(() -> this.performWarmup(world, settings, teleportService, newCompleted), 2L);
                  } else {
                     this.plugin.getLogger().info(String.format("[BiomeCache] Warmed up %d locations for world '%s'", this.warmupSize, world.getName()));
                  }
               }));
               return;
            }
         }

         this.warmupAttempt(world, settings, teleportService, () -> {
            int newCompleted = completedCount + 1;
            if (newCompleted < this.warmupSize) {
               this.effectiveScheduler().executeGlobalDelayed(() -> this.performWarmup(world, settings, teleportService, newCompleted), 2L);
            } else {
               this.plugin.getLogger().info(String.format("[BiomeCache] Warmed up %d locations for world '%s'", this.warmupSize, world.getName()));
            }
         });
      } else {
         if (this.enabled && completedCount > 0) {
            this.plugin
               .getLogger()
               .info(
                  String.format(
                     "[BiomeCache] Warmed up %d locations for world '%s' (Memory: %dMB free / %dMB total)",
                     completedCount,
                     world.getName(),
                     freeMemoryMb,
                     totalMemoryMb
                  )
               );
         }
      }
   }

   private void warmupAttempt(World world, RandomTeleportSettings settings, RandomTeleportService teleportService, Runnable onSuccess) {
      Runtime runtime = Runtime.getRuntime();
      long freeMemoryMb = (runtime.freeMemory() + (runtime.maxMemory() - runtime.totalMemory())) / 1048576L;
      if (freeMemoryMb < 128L) {
         this.plugin
            .getLogger()
            .warning(
               String.format("[BiomeCache] Aborting warmup attempt for world '%s' due to critically low memory: %dMB free", world.getName(), freeMemoryMb)
            );
      } else {
         this.plugin
            .getLogger()
            .fine(String.format("[BiomeCache] Attempting warmup location for world '%s' (Memory: %dMB free)", world.getName(), freeMemoryMb));
         teleportService.generateSafeLocationForCache(world, settings)
            .thenAccept(
               location -> {
                  if (location != null) {
                     this.cache(location);
                     this.plugin
                        .getLogger()
                        .fine(String.format("[BiomeCache] Successfully cached location at %s in world '%s'", location.toString(), world.getName()));

                     try {
                        this.effectiveScheduler().executeGlobal(onSuccess);
                     } catch (IllegalStateException var5x) {
                        if (this.plugin.isEnabled()) {
                           this.plugin.getLogger().warning("[BiomeCache] Warmup interrupted - plugin may be shutting down");
                        }
                     }
                  } else {
                     this.plugin.getLogger().fine(String.format("[BiomeCache] Failed to generate warmup location for world '%s'", world.getName()));
                  }
               }
            );
      }
   }

   public List<Location> getAllLocations(String worldName) {
      if (this.enabled && worldName != null) {
         Map<Biome, Deque<BiomeLocationCache.CachedLocation>> biomeCache = this.worldCaches.get(worldName);
         if (biomeCache != null && !biomeCache.isEmpty()) {
            List<Location> allLocations = new ArrayList<>();
            long now = System.currentTimeMillis();

            for (Deque<BiomeLocationCache.CachedLocation> locations : biomeCache.values()) {
               locations.removeIf(cachedx -> cachedx.isExpired(now));

               for (BiomeLocationCache.CachedLocation cached : locations) {
                  allLocations.add(cached.location().clone());
               }
            }

            return allLocations;
         } else {
            return Collections.emptyList();
         }
      } else {
         return Collections.emptyList();
      }
   }

   public List<Location> getLocations(String worldName, Biome biome) {
      if (this.enabled && worldName != null && biome != null) {
         Map<Biome, Deque<BiomeLocationCache.CachedLocation>> biomeCache = this.worldCaches.get(worldName);
         if (biomeCache != null && !biomeCache.isEmpty()) {
            Deque<BiomeLocationCache.CachedLocation> cached = biomeCache.get(biome);
            if (cached != null && !cached.isEmpty()) {
               List<Location> result = new ArrayList<>();
               long now = System.currentTimeMillis();
               cached.removeIf(cx -> cx.isExpired(now));

               for (BiomeLocationCache.CachedLocation c : cached) {
                  result.add(c.location().clone());
               }

               return result;
            } else {
               return Collections.emptyList();
            }
         } else {
            return Collections.emptyList();
         }
      } else {
         return Collections.emptyList();
      }
   }

   public BiomeLocationCache.CacheStats getStats() {
      int totalLocations = 0;
      int totalBiomes = 0;

      for (Map<Biome, Deque<BiomeLocationCache.CachedLocation>> worldCache : this.worldCaches.values()) {
         totalBiomes += worldCache.size();

         for (Deque<BiomeLocationCache.CachedLocation> locations : worldCache.values()) {
            totalLocations += locations.size();
         }
      }

      return new BiomeLocationCache.CacheStats(this.worldCaches.size(), totalBiomes, totalLocations, this.evictionCount.get());
   }

   public long getEvictionCount() {
      return this.evictionCount.get();
   }

   public int getCachedLocationCount(World world, Set<Biome> biomes) {
      if (this.enabled && world != null && biomes != null && !biomes.isEmpty()) {
         Map<Biome, Deque<BiomeLocationCache.CachedLocation>> biomeCache = this.worldCaches.get(world.getName());
         if (biomeCache != null && !biomeCache.isEmpty()) {
            int count = 0;
            long now = System.currentTimeMillis();

            for (Biome biome : biomes) {
               Deque<BiomeLocationCache.CachedLocation> locations = biomeCache.get(biome);
               if (locations != null && !locations.isEmpty()) {
                  for (BiomeLocationCache.CachedLocation cached : locations) {
                     if (!cached.isExpired(now)) {
                        count++;
                     }
                  }
               }
            }

            return count;
         } else {
            return 0;
         }
      } else {
         return 0;
      }
   }

   public void shutdown() {
      if (this.warmupTask != null) {
         this.warmupTask.cancel();
         this.warmupTask = null;
      }

      this.clear();
      this.enabled = false;
   }

   public record CacheStats(int worldCount, int biomeCount, int locationCount, long evictions) {
      @Override
      public String toString() {
         return String.format(
            "CacheStats[worlds=%d, biomes=%d, locations=%d, evictions=%d]", this.worldCount, this.biomeCount, this.locationCount, this.evictions
         );
      }
   }

   private class CachedLocation {
      private final Location location;
      private final long timestamp;

      CachedLocation(Location location, long timestamp) {
         this.location = location;
         this.timestamp = timestamp;
      }

      Location location() {
         return this.location;
      }

      boolean isExpired(long currentTime) {
         return currentTime - this.timestamp > BiomeLocationCache.this.expirationMillis;
      }
   }
}
