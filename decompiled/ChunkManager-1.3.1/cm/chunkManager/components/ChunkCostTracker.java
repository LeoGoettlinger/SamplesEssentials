package cm.chunkManager.components;

import cm.chunkManager.utils.Scheduler;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.plugin.java.JavaPlugin;

public class ChunkCostTracker {
   private final JavaPlugin plugin;
   private final Map<String, ChunkCostTracker.ChunkCost> costs;
   private boolean enabled;

   public ChunkCostTracker(JavaPlugin plugin) {
      this.plugin = plugin;
      this.costs = new ConcurrentHashMap<>();
      this.enabled = true;
   }

   private String key(String world, int x, int z) {
      return world + ";" + x + ";" + z;
   }

   public void updateChunk(Chunk chunk) {
      if (this.enabled) {
         String k = this.key(chunk.getWorld().getName(), chunk.getX(), chunk.getZ());
         ChunkCostTracker.ChunkCost cost = this.costs
            .computeIfAbsent(k, i -> new ChunkCostTracker.ChunkCost(chunk.getWorld().getName(), chunk.getX(), chunk.getZ()));
         int entities = 0;
         if (!Scheduler.isFolia()) {
            entities = chunk.getEntities().length;
         }

         int tileEntities = 0;

         try {
            tileEntities = chunk.getTileEntities().length;
         } catch (Exception var7) {
         }

         cost.entityCount = entities;
         cost.tileEntityCount = tileEntities;
         cost.costScore = entities * 2.0 + tileEntities * 5.0;
         cost.lastUpdate = System.currentTimeMillis();
      }
   }

   public void updateAll() {
      if (this.enabled) {
         for (World world : Bukkit.getWorlds()) {
            for (Chunk chunk : world.getLoadedChunks()) {
               if (Scheduler.isFolia()) {
                  Scheduler.runAtChunk(this.plugin, world, chunk.getX(), chunk.getZ(), () -> {
                     if (chunk.isLoaded()) {
                        this.updateChunk(chunk);
                     }
                  });
               } else {
                  this.updateChunk(chunk);
               }
            }
         }
      }
   }

   public List<ChunkCostTracker.ChunkCost> getTopChunks(int count) {
      return this.costs
         .values()
         .stream()
         .sorted(Comparator.<ChunkCostTracker.ChunkCost>comparingDouble(c -> c.costScore).reversed())
         .limit(count)
         .collect(Collectors.toList());
   }

   public ChunkCostTracker.ChunkCost getCost(String world, int x, int z) {
      return this.costs.get(this.key(world, x, z));
   }

   public void setEnabled(boolean enabled) {
      this.enabled = enabled;
   }

   public boolean isEnabled() {
      return this.enabled;
   }

   public void cleanup() {
      Set<String> loaded = new HashSet<>();

      for (World world : Bukkit.getWorlds()) {
         for (Chunk chunk : world.getLoadedChunks()) {
            loaded.add(this.key(world.getName(), chunk.getX(), chunk.getZ()));
         }
      }

      this.costs.keySet().removeIf(k -> !loaded.contains(k));
   }

   public static class ChunkCost {
      public final String worldName;
      public final int x;
      public final int z;
      public int entityCount;
      public int tileEntityCount;
      public double costScore;
      public long lastUpdate;

      public ChunkCost(String worldName, int x, int z) {
         this.worldName = worldName;
         this.x = x;
         this.z = z;
         this.entityCount = 0;
         this.tileEntityCount = 0;
         this.costScore = 0.0;
         this.lastUpdate = System.currentTimeMillis();
      }
   }
}
