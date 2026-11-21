package cm.chunkManager.components;

import cm.chunkManager.utils.ChunkUtils;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

public class ChunkPrefetcher {
   private final JavaPlugin plugin;
   private final Map<UUID, ChunkPrefetcher.PlayerMovementPattern> movementPatterns;
   private final Map<ChunkPrefetcher.ChunkKey, ChunkPrefetcher.PrefetchData> prefetchCache;
   private final ScheduledExecutorService scheduler;
   private final double confidenceThreshold = 0.7;

   public ChunkPrefetcher(JavaPlugin plugin) {
      this.plugin = plugin;
      this.scheduler = Executors.newScheduledThreadPool(1);
      this.movementPatterns = new ConcurrentHashMap<>();
      this.prefetchCache = new ConcurrentHashMap<>();
      this.startCleanupTask();
   }

   private void startCleanupTask() {
      this.scheduler.scheduleAtFixedRate(() -> {
         long cutoff = System.currentTimeMillis() - 60000L;
         this.prefetchCache.entrySet().removeIf(e -> e.getValue().timestamp < cutoff);
         this.movementPatterns.entrySet().removeIf(e -> e.getValue().lastUpdate < System.currentTimeMillis() - 120000L);
      }, 30L, 30L, TimeUnit.SECONDS);
   }

   public void trackPlayerMovement(Player player) {
      ChunkPrefetcher.PlayerMovementPattern pattern = this.movementPatterns
         .computeIfAbsent(player.getUniqueId(), k -> new ChunkPrefetcher.PlayerMovementPattern(player.getUniqueId()));
      pattern.addLocation(player.getLocation());
      if (pattern.getConfidence() > 0.7) {
         this.prefetchChunks(player.getWorld(), pattern.getPredictedChunks(), pattern.getConfidence());
      }
   }

   private void prefetchChunks(World world, List<ChunkPrefetcher.ChunkKey> chunks, double confidence) {
      for (int i = 0; i < chunks.size(); i++) {
         ChunkPrefetcher.ChunkKey key = chunks.get(i);
         double priority = confidence * (1.0 - i * 0.15);
         ChunkPrefetcher.PrefetchData data = this.prefetchCache.computeIfAbsent(key, k -> new ChunkPrefetcher.PrefetchData(k, priority));
         if (!data.isPrefetched.get()) {
            ChunkUtils.getChunkAtAsync(world, key.x, key.z, this.plugin).thenAccept(chunk -> {
               data.isPrefetched.set(true);
               data.prefetchTask.complete(true);
            }).exceptionally(ex -> {
               data.prefetchTask.completeExceptionally(ex);
               return null;
            });
         }
      }
   }

   public void prefetchArea(World world, Location center, int radius) {
      int centerX = center.getBlockX() >> 4;
      int centerZ = center.getBlockZ() >> 4;
      List<ChunkPrefetcher.ChunkKey> chunks = new ArrayList<>();

      for (int x = -radius; x <= radius; x++) {
         for (int z = -radius; z <= radius; z++) {
            chunks.add(new ChunkPrefetcher.ChunkKey(world.getName(), centerX + x, centerZ + z));
         }
      }

      chunks.sort((a, b) -> {
         double distA = Math.sqrt(Math.pow(a.x - centerX, 2.0) + Math.pow(a.z - centerZ, 2.0));
         double distB = Math.sqrt(Math.pow(b.x - centerX, 2.0) + Math.pow(b.z - centerZ, 2.0));
         return Double.compare(distA, distB);
      });
      this.prefetchChunks(world, chunks, 1.0);
   }

   public Map<String, Object> getPrefetchStatistics() {
      Map<String, Object> stats = new HashMap<>();
      int totalPrefetched = (int)this.prefetchCache.values().stream().filter(d -> d.isPrefetched.get()).count();
      double avgConfidence = this.movementPatterns.values().stream().mapToDouble(ChunkPrefetcher.PlayerMovementPattern::getConfidence).average().orElse(0.0);
      stats.put("totalPrefetched", totalPrefetched);
      stats.put("cacheSize", this.prefetchCache.size());
      stats.put("trackedPlayers", this.movementPatterns.size());
      stats.put("averageConfidence", avgConfidence);
      return stats;
   }

   public void onPlayerQuit(Player player) {
      this.movementPatterns.remove(player.getUniqueId());
   }

   public void shutdown() {
      this.scheduler.shutdown();

      try {
         if (!this.scheduler.awaitTermination(5L, TimeUnit.SECONDS)) {
            this.scheduler.shutdownNow();
         }
      } catch (InterruptedException var2) {
         this.scheduler.shutdownNow();
      }
   }

   public static class ChunkKey {
      public final String world;
      public final int x;
      public final int z;

      public ChunkKey(String world, int x, int z) {
         this.world = world;
         this.x = x;
         this.z = z;
      }

      public ChunkKey(Chunk chunk) {
         this.world = chunk.getWorld().getName();
         this.x = chunk.getX();
         this.z = chunk.getZ();
      }

      @Override
      public boolean equals(Object o) {
         if (this == o) {
            return true;
         } else {
            return !(o instanceof ChunkPrefetcher.ChunkKey key) ? false : this.x == key.x && this.z == key.z && this.world.equals(key.world);
         }
      }

      @Override
      public int hashCode() {
         return Objects.hash(this.world, this.x, this.z);
      }
   }

   public static class PlayerMovementPattern {
      private final UUID playerId;
      private final Deque<Location> history;
      private Vector averageVelocity;
      private List<ChunkPrefetcher.ChunkKey> predictedPath;
      private double pathConfidence;
      private long lastUpdate;

      public PlayerMovementPattern(UUID playerId) {
         this.playerId = playerId;
         this.history = new LinkedList<>();
         this.averageVelocity = new Vector(0, 0, 0);
         this.predictedPath = new ArrayList<>();
         this.pathConfidence = 0.0;
         this.lastUpdate = System.currentTimeMillis();
      }

      public void addLocation(Location loc) {
         this.history.offer(loc);
         if (this.history.size() > 20) {
            this.history.poll();
         }

         this.calculatePattern();
         this.lastUpdate = System.currentTimeMillis();
      }

      private void calculatePattern() {
         if (this.history.size() >= 3) {
            List<Location> locs = new ArrayList<>(this.history);
            double totalSpeed = 0.0;
            Vector totalVelocity = new Vector(0, 0, 0);

            for (int i = 1; i < locs.size(); i++) {
               Location prev = locs.get(i - 1);
               Location curr = locs.get(i);
               Vector vel = curr.toVector().subtract(prev.toVector());
               totalVelocity.add(vel);
               totalSpeed += vel.length();
            }

            this.averageVelocity = totalVelocity.multiply(1.0 / (locs.size() - 1));
            this.pathConfidence = this.calculateConfidence();
            this.predictPath();
         }
      }

      private double calculateConfidence() {
         if (this.history.size() < 5) {
            return 0.0;
         } else {
            List<Location> locs = new ArrayList<>(this.history);
            double variance = 0.0;

            for (int i = 2; i < locs.size(); i++) {
               Vector v1 = locs.get(i - 1).toVector().subtract(locs.get(i - 2).toVector());
               Vector v2 = locs.get(i).toVector().subtract(locs.get(i - 1).toVector());
               if (!(v1.lengthSquared() < 0.001) && !(v2.lengthSquared() < 0.001)) {
                  double angle = v1.angle(v2);
                  variance += angle * angle;
               }
            }

            variance /= locs.size() - 2;
            return Math.max(0.0, 1.0 - variance / Math.PI);
         }
      }

      private void predictPath() {
         this.predictedPath.clear();
         if (!this.history.isEmpty() && !(this.pathConfidence < 0.5)) {
            Location last = this.history.peekLast();
            World world = last.getWorld();

            for (int i = 1; i <= 5; i++) {
               double predictX = last.getX() + this.averageVelocity.getX() * i * 5.0;
               double predictZ = last.getZ() + this.averageVelocity.getZ() * i * 5.0;
               int chunkX = (int)predictX >> 4;
               int chunkZ = (int)predictZ >> 4;
               this.predictedPath.add(new ChunkPrefetcher.ChunkKey(world.getName(), chunkX, chunkZ));
            }
         }
      }

      public List<ChunkPrefetcher.ChunkKey> getPredictedChunks() {
         return new ArrayList<>(this.predictedPath);
      }

      public double getConfidence() {
         return this.pathConfidence;
      }
   }

   public static class PrefetchData {
      public final ChunkPrefetcher.ChunkKey chunk;
      public final long timestamp;
      public final double priority;
      public final AtomicBoolean isPrefetched;
      public final CompletableFuture<Boolean> prefetchTask;

      public PrefetchData(ChunkPrefetcher.ChunkKey chunk, double priority) {
         this.chunk = chunk;
         this.timestamp = System.currentTimeMillis();
         this.priority = priority;
         this.isPrefetched = new AtomicBoolean(false);
         this.prefetchTask = new CompletableFuture<>();
      }
   }
}
