package com.skyblockexp.ezrtp.teleport;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.AbstractMap.SimpleEntry;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

public final class ChunkyWarmupCoordinator {
   private final ConcurrentMap<String, ConcurrentMap<Long, Long>> generatedChunks = new ConcurrentHashMap<>();
   private final AtomicLong chunksMarked = new AtomicLong();
   private final AtomicLong chunksExpired = new AtomicLong();
   private final AtomicLong chunksWarmupProcessed = new AtomicLong();
   private final AtomicLong duplicateSkips = new AtomicLong();
   private volatile long retentionMillis = 3600000L;
   private volatile boolean memorySafetyEnabled = true;
   private volatile long minFreeMemoryMb = 512L;
   private volatile int maxCoordinatorEntries = 2500;
   private volatile long lowMemoryRetentionMillis = 900000L;
   private volatile BukkitTask cleanupTask;

   public void registerWithChunky(ChunkyProvider chunkyProvider, JavaPlugin plugin) {
      if (chunkyProvider != null) {
         chunkyProvider.registerListeners(plugin);

         try {
            if (this.cleanupTask != null && !this.cleanupTask.isCancelled()) {
               this.cleanupTask.cancel();
            }

            this.cleanupTask = plugin.getServer().getScheduler().runTaskTimerAsynchronously(plugin, this::purgeOldEntries, 1200L, 1200L);
         } catch (Throwable var4) {
            plugin.getLogger().warning("Failed to schedule ChunkyWarmupCoordinator cleanup: " + var4.getMessage());
         }
      }
   }

   public void markChunkGenerated(String worldName, int chunkX, int chunkZ) {
      long key = toKey(chunkX, chunkZ);
      this.generatedChunks.computeIfAbsent(worldName, w -> new ConcurrentHashMap<>()).put(key, System.currentTimeMillis());
      this.chunksMarked.incrementAndGet();
   }

   public void markRegionPlanned(String worldName, int centerChunkX, int centerChunkZ, int radiusChunks) {
      if (!this.memorySafetyEnabled || this.hasSufficientMemory()) {
         ConcurrentMap<Long, Long> worldChunks = this.generatedChunks.computeIfAbsent(worldName, w -> new ConcurrentHashMap<>());
         int r = Math.max(0, radiusChunks);
         long timestamp = System.currentTimeMillis();
         int regionSize = (2 * r + 1) * (2 * r + 1);
         if (this.wouldExceedMaxEntries() && regionSize > 0) {
            this.evictOldEntries(regionSize);
         }

         for (int dx = -r; dx <= r; dx++) {
            for (int dz = -r; dz <= r; dz++) {
               long key = toKey(centerChunkX + dx, centerChunkZ + dz);
               worldChunks.put(key, timestamp);
            }
         }

         this.chunksMarked.addAndGet((2L * r + 1L) * (2L * r + 1L));
      }
   }

   public boolean isChunkPlannedOrGenerated(String worldName, int chunkX, int chunkZ) {
      ConcurrentMap<Long, Long> worldChunks = this.generatedChunks.get(worldName);
      return worldChunks == null ? false : worldChunks.containsKey(toKey(chunkX, chunkZ));
   }

   public Set<Long> getChunksForWorld(String worldName) {
      Map<Long, Long> worldChunks = this.generatedChunks.getOrDefault(worldName, new ConcurrentHashMap<>());
      return worldChunks.keySet();
   }

   public void recordWarmupProcessed() {
      this.chunksWarmupProcessed.incrementAndGet();
   }

   public void recordDuplicateSkip() {
      this.duplicateSkips.incrementAndGet();
   }

   public long getChunksMarked() {
      return this.chunksMarked.get();
   }

   public long getChunksExpired() {
      return this.chunksExpired.get();
   }

   public long getChunksWarmupProcessed() {
      return this.chunksWarmupProcessed.get();
   }

   public long getDuplicateSkips() {
      return this.duplicateSkips.get();
   }

   public void setRetentionMillis(long retentionMillis) {
      this.retentionMillis = retentionMillis;
   }

   public void configureMemorySafety(boolean memorySafetyEnabled, long minFreeMemoryMb, int maxCoordinatorEntries, long lowMemoryRetentionMinutes) {
      this.memorySafetyEnabled = memorySafetyEnabled;
      this.minFreeMemoryMb = minFreeMemoryMb;
      this.maxCoordinatorEntries = maxCoordinatorEntries;
      this.lowMemoryRetentionMillis = lowMemoryRetentionMinutes * 60L * 1000L;
   }

   public boolean hasSufficientMemory() {
      if (!this.memorySafetyEnabled) {
         return true;
      } else {
         Runtime runtime = Runtime.getRuntime();
         long freeMemoryMb = (runtime.freeMemory() + (runtime.maxMemory() - runtime.totalMemory())) / 1048576L;
         return freeMemoryMb >= this.minFreeMemoryMb;
      }
   }

   private long getEffectiveRetentionMillis() {
      return this.memorySafetyEnabled && !this.hasSufficientMemory() ? Math.min(this.retentionMillis, this.lowMemoryRetentionMillis) : this.retentionMillis;
   }

   private boolean wouldExceedMaxEntries() {
      if (this.memorySafetyEnabled && this.maxCoordinatorEntries > 0) {
         int totalEntries = this.generatedChunks.values().stream().mapToInt(Map::size).sum();
         return totalEntries >= this.maxCoordinatorEntries;
      } else {
         return false;
      }
   }

   private void evictOldEntries(int entriesNeeded) {
      if (entriesNeeded > 0) {
         List<Entry<String, Entry<Long, Long>>> allEntries = new ArrayList<>();

         for (Entry<String, ConcurrentMap<Long, Long>> worldEntry : this.generatedChunks.entrySet()) {
            String worldName = worldEntry.getKey();

            for (Entry<Long, Long> chunkEntry : worldEntry.getValue().entrySet()) {
               allEntries.add(new SimpleEntry<>(worldName, new SimpleEntry<>(chunkEntry.getKey(), chunkEntry.getValue())));
            }
         }

         allEntries.sort(Comparator.comparing(e -> e.getValue().getValue()));
         int removed = 0;

         for (Entry<String, Entry<Long, Long>> entry : allEntries) {
            if (removed >= entriesNeeded) {
               break;
            }

            String worldName = entry.getKey();
            Long chunkKey = entry.getValue().getKey();
            ConcurrentMap<Long, Long> worldChunks = this.generatedChunks.get(worldName);
            if (worldChunks != null && worldChunks.remove(chunkKey) != null) {
               removed++;
               this.chunksExpired.incrementAndGet();
            }
         }
      }
   }

   private void purgeOldEntries() {
      long now = System.currentTimeMillis();
      long effectiveRetention = this.getEffectiveRetentionMillis();

      for (Entry<String, ConcurrentMap<Long, Long>> worldEntry : this.generatedChunks.entrySet()) {
         ConcurrentMap<Long, Long> worldChunks = worldEntry.getValue();

         for (Entry<Long, Long> chunkEntry : worldChunks.entrySet()) {
            if (now - chunkEntry.getValue() > effectiveRetention) {
               worldChunks.remove(chunkEntry.getKey());
               this.chunksExpired.incrementAndGet();
            }
         }
      }
   }

   private static long toKey(int chunkX, int chunkZ) {
      return (long)chunkX << 32 | chunkZ & 4294967295L;
   }
}
