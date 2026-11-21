package cm.chunkManager.events;

import cm.chunkManager.utils.Scheduler;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;

public class ChunkEventManager {
   private final JavaPlugin plugin;
   private final Map<ChunkEventManager.ChunkEventType, List<Consumer<ChunkEventManager.ChunkEvent>>> listeners;

   public ChunkEventManager(JavaPlugin plugin) {
      this.plugin = plugin;
      this.listeners = new ConcurrentHashMap<>();

      for (ChunkEventManager.ChunkEventType type : ChunkEventManager.ChunkEventType.values()) {
         this.listeners.put(type, new CopyOnWriteArrayList<>());
      }
   }

   public void registerListener(ChunkEventManager.ChunkEventType type, Consumer<ChunkEventManager.ChunkEvent> listener) {
      this.listeners.get(type).add(listener);
   }

   public void unregisterListener(ChunkEventManager.ChunkEventType type, Consumer<ChunkEventManager.ChunkEvent> listener) {
      this.listeners.get(type).remove(listener);
   }

   public void fireEvent(ChunkEventManager.ChunkEvent event) {
      if (!Scheduler.isFolia()) {
         try {
            if (Bukkit.isPrimaryThread()) {
               this.plugin.getServer().getPluginManager().callEvent(event);
            } else {
               Scheduler.runGlobal(this.plugin, () -> this.plugin.getServer().getPluginManager().callEvent(event));
            }
         } catch (Exception var7) {
            this.plugin.getLogger().warning("Failed to dispatch Bukkit event: " + var7.getMessage());
         }
      }

      for (Consumer<ChunkEventManager.ChunkEvent> listener : this.listeners.get(event.getEventType())) {
         try {
            listener.accept(event);
         } catch (Exception var6) {
            this.plugin.getLogger().severe("Error in chunk event listener: " + var6.getMessage());
         }
      }
   }

   public void fireLoadEvent(Chunk chunk, boolean isAsync, Player requester) {
      ChunkEventManager.ChunkLoadEvent event = new ChunkEventManager.ChunkLoadEvent(chunk, isAsync, requester);
      this.fireEvent(event);
   }

   public void fireUnloadEvent(Chunk chunk, ChunkEventManager.ChunkUnloadEvent.UnloadReason reason) {
      ChunkEventManager.ChunkUnloadEvent event = new ChunkEventManager.ChunkUnloadEvent(chunk, reason);
      this.fireEvent(event);
   }

   public void fireOptimizationEvent(Chunk chunk, ChunkEventManager.ChunkOptimizationEvent.OptimizationType type, int affected, double gain) {
      ChunkEventManager.ChunkOptimizationEvent event = new ChunkEventManager.ChunkOptimizationEvent(chunk, type, affected, gain);
      this.fireEvent(event);
   }

   public void fireHeatChangeEvent(Chunk chunk, double oldHeat, double newHeat, ChunkEventManager.ChunkHeatChangeEvent.HeatChangeReason reason) {
      ChunkEventManager.ChunkHeatChangeEvent event = new ChunkEventManager.ChunkHeatChangeEvent(chunk, oldHeat, newHeat, reason);
      this.fireEvent(event);
   }

   public Map<ChunkEventManager.ChunkEventType, Integer> getListenerCounts() {
      Map<ChunkEventManager.ChunkEventType, Integer> counts = new HashMap<>();

      for (Entry<ChunkEventManager.ChunkEventType, List<Consumer<ChunkEventManager.ChunkEvent>>> entry : this.listeners.entrySet()) {
         counts.put(entry.getKey(), entry.getValue().size());
      }

      return counts;
   }

   public abstract static class ChunkEvent extends Event {
      protected final Chunk chunk;
      protected final ChunkEventManager.ChunkEventType type;
      protected final long timestamp;
      protected final Map<String, Object> metadata;

      public ChunkEvent(Chunk chunk, ChunkEventManager.ChunkEventType type) {
         this.chunk = chunk;
         this.type = type;
         this.timestamp = System.currentTimeMillis();
         this.metadata = new HashMap<>();
      }

      public Chunk getChunk() {
         return this.chunk;
      }

      public ChunkEventManager.ChunkEventType getEventType() {
         return this.type;
      }

      public long getTimestamp() {
         return this.timestamp;
      }

      public Map<String, Object> getMetadata() {
         return this.metadata;
      }

      public void setMetadata(String key, Object value) {
         this.metadata.put(key, value);
      }
   }

   public static enum ChunkEventType {
      PRE_LOAD,
      POST_LOAD,
      PRE_UNLOAD,
      POST_UNLOAD,
      OPTIMIZATION_START,
      OPTIMIZATION_END,
      HEAT_CHANGE,
      PRIORITY_CHANGE,
      CACHE_HIT,
      CACHE_MISS,
      PREFETCH_SUCCESS,
      PREFETCH_FAIL;
   }

   public static class ChunkHeatChangeEvent extends ChunkEventManager.ChunkEvent {
      private static final HandlerList HANDLERS = new HandlerList();
      private final double oldHeat;
      private final double newHeat;
      private final ChunkEventManager.ChunkHeatChangeEvent.HeatChangeReason reason;

      public ChunkHeatChangeEvent(Chunk chunk, double oldHeat, double newHeat, ChunkEventManager.ChunkHeatChangeEvent.HeatChangeReason reason) {
         super(chunk, ChunkEventManager.ChunkEventType.HEAT_CHANGE);
         this.oldHeat = oldHeat;
         this.newHeat = newHeat;
         this.reason = reason;
      }

      public double getOldHeat() {
         return this.oldHeat;
      }

      public double getNewHeat() {
         return this.newHeat;
      }

      public ChunkEventManager.ChunkHeatChangeEvent.HeatChangeReason getReason() {
         return this.reason;
      }

      public HandlerList getHandlers() {
         return HANDLERS;
      }

      public static HandlerList getHandlerList() {
         return HANDLERS;
      }

      public static enum HeatChangeReason {
         PLAYER_ACTIVITY,
         ENTITY_SPAWN,
         REDSTONE_ACTIVITY,
         TIME_DECAY,
         MANUAL_ADJUSTMENT;
      }
   }

   public static class ChunkLoadEvent extends ChunkEventManager.ChunkEvent implements Cancellable {
      private static final HandlerList HANDLERS = new HandlerList();
      private boolean cancelled = false;
      private final boolean isAsync;
      private final Player requester;

      public ChunkLoadEvent(Chunk chunk, boolean isAsync, Player requester) {
         super(chunk, ChunkEventManager.ChunkEventType.PRE_LOAD);
         this.isAsync = isAsync;
         this.requester = requester;
      }

      public boolean isAsync() {
         return this.isAsync;
      }

      public Player getRequester() {
         return this.requester;
      }

      public boolean isCancelled() {
         return this.cancelled;
      }

      public void setCancelled(boolean cancel) {
         this.cancelled = cancel;
      }

      public HandlerList getHandlers() {
         return HANDLERS;
      }

      public static HandlerList getHandlerList() {
         return HANDLERS;
      }
   }

   public static class ChunkOptimizationEvent extends ChunkEventManager.ChunkEvent {
      private static final HandlerList HANDLERS = new HandlerList();
      private final int chunksAffected;
      private final ChunkEventManager.ChunkOptimizationEvent.OptimizationType optimizationType;
      private final double performanceGain;

      public ChunkOptimizationEvent(Chunk chunk, ChunkEventManager.ChunkOptimizationEvent.OptimizationType type, int affected, double gain) {
         super(chunk, ChunkEventManager.ChunkEventType.OPTIMIZATION_START);
         this.optimizationType = type;
         this.chunksAffected = affected;
         this.performanceGain = gain;
      }

      public ChunkEventManager.ChunkOptimizationEvent.OptimizationType getOptimizationType() {
         return this.optimizationType;
      }

      public int getChunksAffected() {
         return this.chunksAffected;
      }

      public double getPerformanceGain() {
         return this.performanceGain;
      }

      public HandlerList getHandlers() {
         return HANDLERS;
      }

      public static HandlerList getHandlerList() {
         return HANDLERS;
      }

      public static enum OptimizationType {
         ENTITY_CLEANUP,
         TILE_ENTITY_OPTIMIZATION,
         LIGHT_RECALCULATION,
         MEMORY_COMPACTION,
         REGION_MERGE;
      }
   }

   public static class ChunkUnloadEvent extends ChunkEventManager.ChunkEvent implements Cancellable {
      private static final HandlerList HANDLERS = new HandlerList();
      private boolean cancelled = false;
      private final ChunkEventManager.ChunkUnloadEvent.UnloadReason reason;

      public ChunkUnloadEvent(Chunk chunk, ChunkEventManager.ChunkUnloadEvent.UnloadReason reason) {
         super(chunk, ChunkEventManager.ChunkEventType.PRE_UNLOAD);
         this.reason = reason;
      }

      public ChunkEventManager.ChunkUnloadEvent.UnloadReason getReason() {
         return this.reason;
      }

      public boolean isCancelled() {
         return this.cancelled;
      }

      public void setCancelled(boolean cancel) {
         this.cancelled = cancel;
      }

      public HandlerList getHandlers() {
         return HANDLERS;
      }

      public static HandlerList getHandlerList() {
         return HANDLERS;
      }

      public static enum UnloadReason {
         MANUAL,
         AUTOMATIC,
         OPTIMIZATION,
         MEMORY_PRESSURE,
         IDLE_TIMEOUT;
      }
   }
}
