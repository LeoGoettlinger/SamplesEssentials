package cm.chunkManager.hooks;

import cm.chunkManager.components.ChunkCostTracker;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

public class DynmapBridge {
   private static final String SET_ID = "chunkmanager.hotchunks";
   private final JavaPlugin plugin;
   private boolean available;
   private Object markerApi;
   private Object markerSet;
   private final Set<String> activeMarkers = new HashSet<>();

   public DynmapBridge(JavaPlugin plugin) {
      this.plugin = plugin;
      this.init();
   }

   private void init() {
      try {
         Plugin dynmap = Bukkit.getPluginManager().getPlugin("dynmap");
         if (dynmap == null || !dynmap.isEnabled()) {
            return;
         }

         Method getApi = dynmap.getClass().getMethod("getMarkerAPI");
         this.markerApi = getApi.invoke(dynmap);
         if (this.markerApi == null) {
            return;
         }

         Method getSet = this.markerApi.getClass().getMethod("getMarkerSet", String.class);
         Object existing = getSet.invoke(this.markerApi, "chunkmanager.hotchunks");
         if (existing != null) {
            this.markerSet = existing;
         } else {
            Method create = this.markerApi.getClass().getMethod("createMarkerSet", String.class, String.class, Set.class, boolean.class);
            this.markerSet = create.invoke(this.markerApi, "chunkmanager.hotchunks", "Hot Chunks", null, false);
         }

         this.available = this.markerSet != null;
         if (this.available) {
            this.plugin.getLogger().info("Dynmap hot-chunk overlay ready.");
         }
      } catch (Throwable var6) {
         this.plugin.getLogger().warning("Dynmap hook disabled: " + var6.getMessage());
         this.available = false;
      }
   }

   public boolean isAvailable() {
      return this.available;
   }

   public void refresh(List<ChunkCostTracker.ChunkCost> top) {
      if (this.available) {
         try {
            this.clear();
            double max = 0.0;

            for (ChunkCostTracker.ChunkCost c : top) {
               if (c.costScore > max) {
                  max = c.costScore;
               }
            }

            if (max <= 0.0) {
               max = 1.0;
            }

            Method createArea = this.markerSet
               .getClass()
               .getMethod("createAreaMarker", String.class, String.class, boolean.class, String.class, double[].class, double[].class, boolean.class);
            int i = 0;

            for (ChunkCostTracker.ChunkCost cx : top) {
               int bx = cx.x << 4;
               int bz = cx.z << 4;
               double[] xs = new double[]{bx, bx + 16, bx + 16, bx};
               double[] zs = new double[]{bz, bz, bz + 16, bz + 16};
               String id = "chunkmanager.hotchunks." + i++;
               String label = "Cost " + (int)cx.costScore + " [" + cx.x + "," + cx.z + "]";
               Object area = createArea.invoke(this.markerSet, id, label, false, cx.worldName, xs, zs, false);
               if (area != null) {
                  int color = this.colorFor(cx.costScore / max);
                  area.getClass().getMethod("setFillStyle", double.class, int.class).invoke(area, 0.35, color);
                  area.getClass().getMethod("setLineStyle", int.class, double.class, int.class).invoke(area, 1, 0.8, color);
                  this.activeMarkers.add(id);
               }
            }
         } catch (Throwable var16) {
            this.plugin.getLogger().warning("Dynmap refresh failed: " + var16.getMessage());
         }
      }
   }

   public void clear() {
      if (this.available) {
         try {
            Method getMarker = this.markerSet.getClass().getMethod("findAreaMarker", String.class);

            for (String id : this.activeMarkers) {
               Object m = getMarker.invoke(this.markerSet, id);
               if (m != null) {
                  m.getClass().getMethod("deleteMarker").invoke(m);
               }
            }
         } catch (Throwable var5) {
         }

         this.activeMarkers.clear();
      }
   }

   private int colorFor(double ratio) {
      ratio = Math.max(0.0, Math.min(1.0, ratio));
      int r = (int)(255.0 * ratio);
      int g = (int)(200.0 * (1.0 - ratio));
      return r << 16 | g << 8;
   }

   public void shutdown() {
      this.clear();
   }
}
