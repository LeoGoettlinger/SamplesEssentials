package cm.chunkManager.api;

import cm.chunkManager.components.PerformanceMonitor;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import org.bukkit.World;
import org.bukkit.entity.Player;

public interface ChunkManagerAPI {
   int getCurrentRadius();

   boolean isMaintenanceMode();

   void setMaintenanceMode(boolean var1);

   void setPlayerRadius(Player var1, int var2);

   int getPlayerRadius(Player var1);

   CompletableFuture<PerformanceMonitor.WorldStatistics> getWorldStatistics(World var1);

   Map<String, String> getPerformanceInfo();

   void forceChunkOptimization();

   void forceGarbageCollection();

   boolean isWorldBlacklisted(World var1);

   void reloadConfiguration();

   int getLoadedChunksCount();

   int getQueuedChunksCount();

   double getAverageTickTime();

   double getMemoryUsagePercentage();

   boolean isBedrockPlayer(Player var1);

   double getCurrentTps();
}
