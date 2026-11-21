package cm.chunkManager.hooks;

import cm.chunkManager.components.ChunkCostTracker;
import java.util.List;
import org.bukkit.plugin.java.JavaPlugin;

public class MapOverlayManager {
   private final JavaPlugin plugin;
   private final ChunkCostTracker costTracker;
   private DynmapBridge dynmap;
   private BluemapBridge bluemap;
   private boolean enabled;
   private int topCount;

   public MapOverlayManager(JavaPlugin plugin, ChunkCostTracker costTracker, boolean enabled, int topCount) {
      this.plugin = plugin;
      this.costTracker = costTracker;
      this.enabled = enabled;
      this.topCount = Math.max(1, topCount);
      if (enabled) {
         this.start();
      }
   }

   public void start() {
      if (this.dynmap == null) {
         this.dynmap = new DynmapBridge(this.plugin);
      }

      if (this.bluemap == null) {
         this.bluemap = new BluemapBridge(this.plugin);
      }
   }

   public void setEnabled(boolean enabled) {
      if (this.enabled != enabled) {
         this.enabled = enabled;
         if (enabled) {
            this.start();
         } else {
            this.shutdown();
         }
      }
   }

   public void setTopCount(int topCount) {
      this.topCount = Math.max(1, topCount);
   }

   public boolean isEnabled() {
      return this.enabled;
   }

   public boolean isAnyAvailable() {
      return this.dynmap != null && this.dynmap.isAvailable() || this.bluemap != null && this.bluemap.isAvailable();
   }

   public void refresh() {
      if (this.enabled && this.costTracker != null && this.costTracker.isEnabled()) {
         List<ChunkCostTracker.ChunkCost> top = this.costTracker.getTopChunks(this.topCount);
         if (!top.isEmpty()) {
            if (this.dynmap != null && this.dynmap.isAvailable()) {
               this.dynmap.refresh(top);
            }

            if (this.bluemap != null && this.bluemap.isAvailable()) {
               this.bluemap.refresh(top);
            }
         }
      }
   }

   public void shutdown() {
      if (this.dynmap != null) {
         this.dynmap.shutdown();
         this.dynmap = null;
      }

      if (this.bluemap != null) {
         this.bluemap.shutdown();
         this.bluemap = null;
      }
   }
}
