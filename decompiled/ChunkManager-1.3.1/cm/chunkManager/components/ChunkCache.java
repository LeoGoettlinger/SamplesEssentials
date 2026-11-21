package cm.chunkManager.components;

import java.util.Comparator;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class ChunkCache {
   private final JavaPlugin plugin;
   private final Logger logger;
   private final Map<ChunkCache.ChunkKey, ChunkCache.CacheEntry> cache;
   private final int maxCacheSize;
   private final int cacheExpirationTime;

   public ChunkCache(JavaPlugin plugin, int maxCacheSize, int cacheExpirationTime) {
      this.plugin = plugin;
      this.logger = plugin.getLogger();
      this.maxCacheSize = maxCacheSize;
      this.cacheExpirationTime = cacheExpirationTime;
      this.cache = new ConcurrentHashMap<>();
   }

   public void addToQueue(Chunk chunk) {
      ChunkCache.ChunkKey key = new ChunkCache.ChunkKey(chunk);
      ChunkCache.CacheEntry existing = this.cache.get(key);
      if (existing == null) {
         if (this.cache.size() >= this.maxCacheSize) {
            this.evictOldest();
         }

         this.cache.put(key, new ChunkCache.CacheEntry(chunk, ChunkCache.ChunkState.QUEUED));
      } else if (existing.state == ChunkCache.ChunkState.CACHED) {
         existing.state = ChunkCache.ChunkState.QUEUED;
      }
   }

   public Chunk pollFromQueue() {
      for (Entry<ChunkCache.ChunkKey, ChunkCache.CacheEntry> entry : this.cache.entrySet()) {
         if (entry.getValue().state == ChunkCache.ChunkState.QUEUED) {
            entry.getValue().state = ChunkCache.ChunkState.KEEP_LOADED;
            return entry.getValue().chunk;
         }
      }

      return null;
   }

   public boolean isQueueEmpty() {
      return this.cache.values().stream().noneMatch(e -> e.state == ChunkCache.ChunkState.QUEUED);
   }

   public int getQueueSize() {
      return (int)this.cache.values().stream().filter(e -> e.state == ChunkCache.ChunkState.QUEUED).count();
   }

   public void addToCache(Chunk chunk) {
      ChunkCache.ChunkKey key = new ChunkCache.ChunkKey(chunk);
      ChunkCache.CacheEntry existing = this.cache.get(key);
      if (existing == null) {
         if (this.cache.size() >= this.maxCacheSize) {
            this.evictOldest();
         }

         this.cache.put(key, new ChunkCache.CacheEntry(chunk, ChunkCache.ChunkState.CACHED));
      } else {
         existing.state = ChunkCache.ChunkState.CACHED;
      }
   }

   public void removeFromCache(Chunk chunk) {
      this.cache.remove(new ChunkCache.ChunkKey(chunk));
   }

   public boolean isCached(Chunk chunk) {
      ChunkCache.CacheEntry entry = this.cache.get(new ChunkCache.ChunkKey(chunk));
      return entry != null && entry.state == ChunkCache.ChunkState.CACHED;
   }

   public void addToKeepLoaded(Chunk chunk) {
      ChunkCache.ChunkKey key = new ChunkCache.ChunkKey(chunk);
      ChunkCache.CacheEntry existing = this.cache.get(key);
      if (existing == null) {
         if (this.cache.size() >= this.maxCacheSize) {
            this.evictOldest();
         }

         this.cache.put(key, new ChunkCache.CacheEntry(chunk, ChunkCache.ChunkState.KEEP_LOADED));
      } else {
         existing.state = ChunkCache.ChunkState.KEEP_LOADED;
      }
   }

   public void removeFromKeepLoaded(Chunk chunk) {
      ChunkCache.ChunkKey key = new ChunkCache.ChunkKey(chunk);
      ChunkCache.CacheEntry entry = this.cache.get(key);
      if (entry != null && entry.state == ChunkCache.ChunkState.KEEP_LOADED) {
         this.cache.remove(key);
      }
   }

   public boolean shouldKeepLoaded(Chunk chunk) {
      ChunkCache.CacheEntry entry = this.cache.get(new ChunkCache.ChunkKey(chunk));
      return entry != null && entry.state == ChunkCache.ChunkState.KEEP_LOADED;
   }

   public Set<Chunk> getChunksToKeepLoaded() {
      Set<Chunk> result = new HashSet<>();

      for (ChunkCache.CacheEntry entry : this.cache.values()) {
         if (entry.state == ChunkCache.ChunkState.KEEP_LOADED) {
            result.add(entry.chunk);
         }
      }

      return result;
   }

   public int getKeepLoadedSize() {
      return (int)this.cache.values().stream().filter(e -> e.state == ChunkCache.ChunkState.KEEP_LOADED).count();
   }

   public void cleanExpiredCache() {
      long currentTime = System.currentTimeMillis();
      this.cache.entrySet().removeIf(entry -> {
         ChunkCache.CacheEntry e = entry.getValue();
         return e.state == ChunkCache.ChunkState.CACHED && currentTime - e.timestamp > this.cacheExpirationTime;
      });
   }

   private void evictOldest() {
      this.cache
         .entrySet()
         .stream()
         .filter(e -> e.getValue().state == ChunkCache.ChunkState.CACHED)
         .min(Comparator.comparingLong(e -> e.getValue().timestamp))
         .ifPresent(oldest -> this.cache.remove(oldest.getKey()));
   }

   public void clearAll() {
      this.cache.clear();
   }

   public boolean isChunkNearPlayer(Chunk chunk, Player player, int radius) {
      if (chunk == null || player == null || player.getLocation() == null) {
         return false;
      } else if (player.getLocation().getWorld() == null) {
         return false;
      } else if (chunk.getWorld() != player.getWorld()) {
         return false;
      } else {
         int chunkX = player.getLocation().getBlockX() >> 4;
         int chunkZ = player.getLocation().getBlockZ() >> 4;
         int deltaX = Math.abs(chunk.getX() - chunkX);
         int deltaZ = Math.abs(chunk.getZ() - chunkZ);
         return deltaX <= radius && deltaZ <= radius;
      }
   }

   public Set<Chunk> getChunksAroundLocation(World world, int centerX, int centerZ, int radius) {
      Set<Chunk> chunks = new HashSet<>();
      if (world == null) {
         return chunks;
      } else {
         for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {
               int cx = centerX + x;
               int cz = centerZ + z;
               if (world.isChunkLoaded(cx, cz)) {
                  chunks.add(world.getChunkAt(cx, cz));
               }
            }
         }

         return chunks;
      }
   }

   public int unloadDistantChunks(Set<World> blacklistedWorlds, int dynamicRadius) {
      int unloadedCount = 0;
      Set<Chunk> toUnload = new HashSet<>();

      for (ChunkCache.CacheEntry entry : this.cache.values()) {
         if (entry.state == ChunkCache.ChunkState.KEEP_LOADED) {
            Chunk chunk = entry.chunk;
            if (!blacklistedWorlds.contains(chunk.getWorld())) {
               boolean shouldUnload = true;

               for (Player player : chunk.getWorld().getPlayers()) {
                  if (this.isChunkNearPlayer(chunk, player, dynamicRadius)) {
                     shouldUnload = false;
                     break;
                  }
               }

               if (shouldUnload) {
                  toUnload.add(chunk);
               }
            }
         }
      }

      for (Chunk chunk : toUnload) {
         if (chunk.isLoaded()) {
            try {
               chunk.unload();
               this.cache.remove(new ChunkCache.ChunkKey(chunk));
               unloadedCount++;
            } catch (Exception var11) {
               this.logger.severe("Failed to unload chunk: " + chunk.toString());
            }
         }
      }

      return unloadedCount;
   }

   private static class CacheEntry {
      final Chunk chunk;
      final long timestamp;
      ChunkCache.ChunkState state;

      CacheEntry(Chunk chunk, ChunkCache.ChunkState state) {
         this.chunk = chunk;
         this.timestamp = System.currentTimeMillis();
         this.state = state;
      }
   }

   private static class ChunkKey {
      final String world;
      final int x;
      final int z;

      ChunkKey(Chunk chunk) {
         this.world = chunk.getWorld().getName();
         this.x = chunk.getX();
         this.z = chunk.getZ();
      }

      @Override
      public boolean equals(Object o) {
         if (this == o) {
            return true;
         } else {
            return !(o instanceof ChunkCache.ChunkKey key) ? false : this.x == key.x && this.z == key.z && this.world.equals(key.world);
         }
      }

      @Override
      public int hashCode() {
         return Objects.hash(this.world, this.x, this.z);
      }
   }

   private static enum ChunkState {
      QUEUED,
      KEEP_LOADED,
      CACHED;
   }
}
