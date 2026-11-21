package cm.chunkManager.components;

import cm.chunkManager.utils.Scheduler;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.ChunkSnapshot;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.bukkit.plugin.java.JavaPlugin;

public class ChunkAnalyzer {
   private final JavaPlugin plugin;
   private final Map<ChunkAnalyzer.ChunkIdentifier, ChunkAnalyzer.ChunkAnalysisData> analysisCache;
   private final Random random = new Random();

   public ChunkAnalyzer(JavaPlugin plugin) {
      this.plugin = plugin;
      this.analysisCache = new ConcurrentHashMap<>();
   }

   public ChunkAnalyzer.ChunkAnalysisData analyzeChunkSync(Chunk chunk) {
      ChunkAnalyzer.ChunkIdentifier id = new ChunkAnalyzer.ChunkIdentifier(chunk);
      ChunkAnalyzer.ChunkAnalysisData cached = this.analysisCache.get(id);
      if (cached != null && !cached.isExpired()) {
         return cached;
      } else if (!Scheduler.isFolia() && Bukkit.isPrimaryThread()) {
         return this.performSmartAnalysis(chunk);
      } else {
         CompletableFuture<ChunkAnalyzer.ChunkAnalysisData> future = new CompletableFuture<>();
         Scheduler.runGlobal(this.plugin, () -> future.complete(this.performSmartAnalysis(chunk)));

         try {
            return future.get(2L, TimeUnit.SECONDS);
         } catch (TimeoutException var6) {
            this.plugin.getLogger().warning("Chunk analysis timed out after 2 seconds");
            return this.createEmptyAnalysis();
         } catch (Exception var7) {
            this.plugin.getLogger().warning("Failed to analyze chunk: " + var7.getMessage());
            return this.createEmptyAnalysis();
         }
      }
   }

   public CompletableFuture<ChunkAnalyzer.ChunkAnalysisData> analyzeChunkAsync(Chunk chunk) {
      ChunkAnalyzer.ChunkIdentifier id = new ChunkAnalyzer.ChunkIdentifier(chunk);
      ChunkAnalyzer.ChunkAnalysisData cached = this.analysisCache.get(id);
      if (cached != null && !cached.isExpired()) {
         return CompletableFuture.completedFuture(cached);
      } else {
         CompletableFuture<ChunkAnalyzer.ChunkAnalysisData> future = new CompletableFuture<>();
         Scheduler.runGlobal(this.plugin, () -> {
            ChunkAnalyzer.ChunkDataSnapshot dataSnapshot = this.captureChunkData(chunk);
            Scheduler.runAsync(this.plugin, () -> {
               ChunkAnalyzer.ChunkAnalysisData analysis = this.analyzeSnapshot(dataSnapshot, new ChunkAnalyzer.ChunkIdentifier(chunk));
               future.complete(analysis);
            });
         });
         return future;
      }
   }

   private ChunkAnalyzer.ChunkDataSnapshot captureChunkData(Chunk chunk) {
      if (!chunk.isLoaded()) {
         return new ChunkAnalyzer.ChunkDataSnapshot(new Entity[0], 0, new ArrayList<>(), chunk.getChunkSnapshot(true, false, false));
      } else {
         Entity[] entities;
         if (Scheduler.isFolia()) {
            entities = new Entity[0];
         } else {
            entities = chunk.getEntities();
         }

         ChunkSnapshot snapshot = chunk.getChunkSnapshot(true, false, false);
         List<Material> tileEntityTypes = new ArrayList<>();
         int tileEntityCount = 0;
         if (!Scheduler.isFolia()) {
            try {
               BlockState[] tiles = chunk.getTileEntities();
               tileEntityCount = tiles.length;

               for (BlockState tile : tiles) {
                  tileEntityTypes.add(tile.getType());
               }
            } catch (Exception var11) {
            }
         }

         return new ChunkAnalyzer.ChunkDataSnapshot(entities, tileEntityCount, tileEntityTypes, snapshot);
      }
   }

   private ChunkAnalyzer.ChunkAnalysisData performSmartAnalysis(Chunk chunk) {
      ChunkAnalyzer.ChunkIdentifier id = new ChunkAnalyzer.ChunkIdentifier(chunk);
      if (!chunk.isLoaded()) {
         return this.createEmptyAnalysis();
      } else {
         ChunkAnalyzer.ChunkDataSnapshot dataSnapshot = this.captureChunkData(chunk);
         return this.analyzeSnapshot(dataSnapshot, id);
      }
   }

   private ChunkAnalyzer.ChunkAnalysisData analyzeSnapshot(ChunkAnalyzer.ChunkDataSnapshot dataSnapshot, ChunkAnalyzer.ChunkIdentifier id) {
      Map<EntityType, Integer> entityCounts = new HashMap<>();
      Map<String, Integer> blockCategories = new HashMap<>();
      int mobCount = 0;
      int passiveEntityCount = 0;
      int itemCount = 0;

      for (Entity entity : dataSnapshot.entities) {
         EntityType type = entity.getType();
         entityCounts.merge(type, 1, Integer::sum);
         if (entity instanceof Monster) {
            mobCount++;
         } else if (entity instanceof LivingEntity) {
            passiveEntityCount++;
         }

         if (type == EntityType.ITEM) {
            itemCount++;
         }
      }

      boolean hasPlayerStructures = false;
      boolean hasRedstone = false;
      boolean hasFarmland = false;
      Set<Material> redstoneTypes = new HashSet<>();

      for (Material typex : dataSnapshot.tileEntityTypes) {
         String category = this.categorizeBlock(typex);
         blockCategories.merge(category, 1, Integer::sum);
         if (this.isPlayerStructureBlock(typex)) {
            hasPlayerStructures = true;
         }

         if (this.isRedstoneComponent(typex)) {
            hasRedstone = true;
            redstoneTypes.add(typex);
         }
      }

      ChunkSnapshot snapshot = dataSnapshot.snapshot;
      int sampleSize = 128;
      int sampledBlocks = 0;

      for (int i = 0; i < sampleSize; i++) {
         int x = this.random.nextInt(16);
         int z = this.random.nextInt(16);
         int y = snapshot.getHighestBlockYAt(x, z);
         if (y > -64) {
            for (int j = 0; j < 3; j++) {
               int sampleY = y - j * 10;
               if (sampleY < -64) {
                  break;
               }

               Material mat = snapshot.getBlockType(x, sampleY, z);
               if (mat != Material.AIR) {
                  sampledBlocks++;
                  if (mat == Material.FARMLAND) {
                     hasFarmland = true;
                  }

                  if (this.isRedstoneComponent(mat)) {
                     hasRedstone = true;
                     redstoneTypes.add(mat);
                  }
               }
            }
         }
      }

      double entityDensity = dataSnapshot.entities.length / 256.0;
      int complexity = this.calculateSmartComplexity(
         entityCounts, dataSnapshot.tileEntityCount, mobCount, itemCount, hasRedstone, redstoneTypes.size(), sampledBlocks
      );
      double performanceImpact = this.calculatePerformanceImpact(dataSnapshot.tileEntityCount, mobCount, itemCount, entityDensity, hasRedstone);
      int estimatedMemoryUsage = this.estimateMemoryUsage(dataSnapshot.entities.length, dataSnapshot.tileEntityCount, sampledBlocks);
      int priority = this.calculateSmartPriority(complexity, performanceImpact, hasPlayerStructures, entityDensity);
      ChunkAnalyzer.ChunkAnalysisData data = new ChunkAnalyzer.ChunkAnalysisData(
         entityCounts,
         blockCategories,
         complexity,
         priority,
         hasPlayerStructures,
         hasRedstone,
         hasFarmland,
         dataSnapshot.tileEntityCount,
         entityDensity,
         mobCount,
         passiveEntityCount,
         itemCount,
         performanceImpact,
         estimatedMemoryUsage
      );
      this.analysisCache.put(id, data);
      return data;
   }

   private String categorizeBlock(Material material) {
      String name = material.name();
      if (name.contains("CHEST") || name.contains("BARREL") || name.contains("SHULKER")) {
         return "STORAGE";
      } else if (name.contains("FURNACE") || name.contains("SMOKER") || name.contains("BLAST")) {
         return "PROCESSING";
      } else if (name.contains("REDSTONE") || name.contains("PISTON") || name.contains("OBSERVER")) {
         return "REDSTONE";
      } else if (name.contains("SPAWNER")) {
         return "SPAWNER";
      } else if (name.contains("COMMAND") || name.contains("STRUCTURE")) {
         return "TECHNICAL";
      } else if (name.contains("SIGN") || name.contains("BANNER") || name.contains("FRAME")) {
         return "DECORATIVE";
      } else {
         return !name.contains("HOPPER") && !name.contains("DROPPER") && !name.contains("DISPENSER") ? "OTHER" : "AUTOMATION";
      }
   }

   private int calculateSmartComplexity(
      Map<EntityType, Integer> entities, int tileEntities, int mobs, int items, boolean hasRedstone, int redstoneTypes, int sampledBlocks
   ) {
      int complexity = 0;
      complexity += tileEntities * 15;
      complexity += mobs * 8;
      complexity += items * 2;
      complexity += entities.size() * 5;
      if (hasRedstone) {
         complexity += 50 + redstoneTypes * 10;
      }

      if (tileEntities > 50) {
         complexity += (tileEntities - 50) * 5;
      }

      if (mobs > 30) {
         complexity += (mobs - 30) * 10;
      }

      if (items > 100) {
         complexity += items - 100;
      }

      complexity += Math.min(sampledBlocks / 10, 50);
      return Math.min(complexity, 1000);
   }

   private double calculatePerformanceImpact(int tileEntities, int mobs, int items, double entityDensity, boolean hasRedstone) {
      double impact = tileEntities * 0.5 + mobs * 0.3 + items * 0.05 + entityDensity * 10.0;
      if (hasRedstone) {
         impact *= 1.5;
      }

      if (tileEntities > 100) {
         impact *= 2.0;
      }

      return Math.min(impact, 100.0);
   }

   private int estimateMemoryUsage(int entities, int tileEntities, int blocks) {
      return 8192 + entities * 512 + tileEntities * 1024 + blocks * 4;
   }

   private int calculateSmartPriority(int complexity, double performanceImpact, boolean hasStructures, double entityDensity) {
      int priority = (int)(complexity * 0.4 + performanceImpact * 0.6);
      if (hasStructures) {
         priority += 20;
      }

      if (entityDensity > 1.0) {
         priority += (int)(entityDensity * 10.0);
      }

      return Math.min(priority, 100);
   }

   private boolean isPlayerStructureBlock(Material material) {
      String name = material.name();
      return name.contains("CHEST")
         || name.contains("FURNACE")
         || name.contains("CRAFTING")
         || name.contains("ANVIL")
         || name.contains("ENCHANTING")
         || name.contains("BREWING")
         || name.contains("BED")
         || name.contains("BEACON")
         || name.contains("ENDER_CHEST")
         || name.contains("SHULKER");
   }

   private boolean isRedstoneComponent(Material material) {
      String name = material.name();
      return name.contains("REDSTONE")
         || name.contains("PISTON")
         || name.contains("OBSERVER")
         || name.contains("HOPPER")
         || name.contains("COMPARATOR")
         || name.contains("REPEATER")
         || name.contains("DROPPER")
         || name.contains("DISPENSER")
         || name.contains("RAIL")
         || name.contains("LEVER")
         || name.contains("BUTTON")
         || name.contains("PRESSURE");
   }

   private ChunkAnalyzer.ChunkAnalysisData createEmptyAnalysis() {
      return new ChunkAnalyzer.ChunkAnalysisData(new HashMap<>(), new HashMap<>(), 0, 0, false, false, false, 0, 0.0, 0, 0, 0, 0.0, 0);
   }

   public List<ChunkAnalyzer.ChunkAnalysisData> analyzeWorld(World world) {
      List<ChunkAnalyzer.ChunkAnalysisData> results = new ArrayList<>();
      Chunk[] loadedChunks = world.getLoadedChunks();

      for (Chunk chunk : loadedChunks) {
         results.add(this.analyzeChunkSync(chunk));
      }

      return results;
   }

   public Map<String, Object> getWorldStatistics(World world) {
      if (world == null) {
         Map<String, Object> emptyStats = new HashMap<>();
         emptyStats.put("status", "World is null");
         return emptyStats;
      } else {
         List<ChunkAnalyzer.ChunkAnalysisData> analyses = this.analyzeWorld(world);
         Map<String, Object> stats = new HashMap<>();
         if (analyses.isEmpty()) {
            stats.put("status", "No chunks loaded");
            return stats;
         } else {
            int totalComplexity = 0;
            int highPriorityChunks = 0;
            int extremeComplexityChunks = 0;
            int structureChunks = 0;
            int redstoneChunks = 0;
            int farmChunks = 0;
            double totalPerformanceImpact = 0.0;
            long totalMemoryUsage = 0L;
            int totalMobs = 0;
            int totalItems = 0;
            int totalTileEntities = 0;

            for (ChunkAnalyzer.ChunkAnalysisData data : analyses) {
               totalComplexity += data.complexity;
               totalPerformanceImpact += data.performanceImpact;
               totalMemoryUsage += data.estimatedMemoryUsage;
               totalMobs += data.mobCount;
               totalItems += data.itemCount;
               totalTileEntities += data.tileEntityCount;
               if (data.priority > 70) {
                  highPriorityChunks++;
               }

               if (data.complexity > 300) {
                  extremeComplexityChunks++;
               }

               if (data.hasPlayerStructures) {
                  structureChunks++;
               }

               if (data.hasRedstone) {
                  redstoneChunks++;
               }

               if (data.hasFarmland) {
                  farmChunks++;
               }
            }

            int chunkCount = analyses.size();
            stats.put("totalChunks", chunkCount);
            stats.put("averageComplexity", chunkCount > 0 ? totalComplexity / chunkCount : 0);
            stats.put("averagePerformanceImpact", chunkCount > 0 ? totalPerformanceImpact / chunkCount : 0.0);
            stats.put("totalMemoryUsageMB", totalMemoryUsage / 1048576L);
            stats.put("highPriorityChunks", highPriorityChunks);
            stats.put("extremeComplexityChunks", extremeComplexityChunks);
            stats.put("structureChunks", structureChunks);
            stats.put("redstoneChunks", redstoneChunks);
            stats.put("farmChunks", farmChunks);
            stats.put("totalMobs", totalMobs);
            stats.put("totalItems", totalItems);
            stats.put("totalTileEntities", totalTileEntities);
            stats.put("averageMobsPerChunk", chunkCount > 0 ? totalMobs / chunkCount : 0);
            stats.put("averageTileEntitiesPerChunk", chunkCount > 0 ? totalTileEntities / chunkCount : 0);
            return stats;
         }
      }
   }

   public void clearCache() {
      this.analysisCache.clear();
   }

   public void cleanExpiredCache() {
      this.analysisCache.entrySet().removeIf(entry -> entry.getValue().isExpired());
   }

   public static class ChunkAnalysisData {
      public final Map<EntityType, Integer> entityCounts;
      public final Map<String, Integer> blockCategories;
      public final int complexity;
      public final int priority;
      public final long timestamp;
      public final boolean hasPlayerStructures;
      public final boolean hasRedstone;
      public final boolean hasFarmland;
      public final int tileEntityCount;
      public final double entityDensity;
      public final int mobCount;
      public final int passiveEntityCount;
      public final int itemCount;
      public final double performanceImpact;
      public final int estimatedMemoryUsage;

      public ChunkAnalysisData(
         Map<EntityType, Integer> entityCounts,
         Map<String, Integer> blockCategories,
         int complexity,
         int priority,
         boolean hasPlayerStructures,
         boolean hasRedstone,
         boolean hasFarmland,
         int tileEntityCount,
         double entityDensity,
         int mobCount,
         int passiveEntityCount,
         int itemCount,
         double performanceImpact,
         int estimatedMemoryUsage
      ) {
         this.entityCounts = entityCounts;
         this.blockCategories = blockCategories;
         this.complexity = complexity;
         this.priority = priority;
         this.hasPlayerStructures = hasPlayerStructures;
         this.hasRedstone = hasRedstone;
         this.hasFarmland = hasFarmland;
         this.tileEntityCount = tileEntityCount;
         this.entityDensity = entityDensity;
         this.mobCount = mobCount;
         this.passiveEntityCount = passiveEntityCount;
         this.itemCount = itemCount;
         this.performanceImpact = performanceImpact;
         this.estimatedMemoryUsage = estimatedMemoryUsage;
         this.timestamp = System.currentTimeMillis();
      }

      public boolean isExpired() {
         return System.currentTimeMillis() - this.timestamp > 300000L;
      }

      public String getComplexityLevel() {
         if (this.complexity < 50) {
            return "LOW";
         } else if (this.complexity < 150) {
            return "MEDIUM";
         } else {
            return this.complexity < 300 ? "HIGH" : "EXTREME";
         }
      }
   }

   private static class ChunkDataSnapshot {
      public final Entity[] entities;
      public final int tileEntityCount;
      public final List<Material> tileEntityTypes;
      public final ChunkSnapshot snapshot;

      public ChunkDataSnapshot(Entity[] entities, int tileEntityCount, List<Material> tileEntityTypes, ChunkSnapshot snapshot) {
         this.entities = entities;
         this.tileEntityCount = tileEntityCount;
         this.tileEntityTypes = tileEntityTypes;
         this.snapshot = snapshot;
      }
   }

   public static class ChunkIdentifier {
      public final String worldName;
      public final int x;
      public final int z;

      public ChunkIdentifier(Chunk chunk) {
         this.worldName = chunk.getWorld().getName();
         this.x = chunk.getX();
         this.z = chunk.getZ();
      }

      @Override
      public boolean equals(Object o) {
         if (this == o) {
            return true;
         } else {
            return !(o instanceof ChunkAnalyzer.ChunkIdentifier that) ? false : this.x == that.x && this.z == that.z && this.worldName.equals(that.worldName);
         }
      }

      @Override
      public int hashCode() {
         return Objects.hash(this.worldName, this.x, this.z);
      }
   }
}
