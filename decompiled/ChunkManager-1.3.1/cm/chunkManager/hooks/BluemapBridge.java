package cm.chunkManager.hooks;

import cm.chunkManager.components.ChunkCostTracker;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

public class BluemapBridge {
   private static final String SET_ID = "chunkmanager.hotchunks";
   private final JavaPlugin plugin;
   private final AtomicReference<Object> apiRef = new AtomicReference<>();
   private volatile boolean available;
   private List<ChunkCostTracker.ChunkCost> pendingTop;

   public BluemapBridge(JavaPlugin plugin) {
      this.plugin = plugin;
      this.init();
   }

   private void init() {
      try {
         Plugin bluemap = Bukkit.getPluginManager().getPlugin("BlueMap");
         if (bluemap == null) {
            return;
         }

         Class<?> apiClass = Class.forName("de.bluecolored.bluemap.api.BlueMapAPI");
         Method onEnable = apiClass.getMethod("onEnable", Consumer.class);
         Consumer<Object> enableCb = api -> {
            this.apiRef.set(api);
            this.available = true;
            this.plugin.getLogger().info("BlueMap hot-chunk overlay ready.");
            if (this.pendingTop != null) {
               this.refresh(this.pendingTop);
               this.pendingTop = null;
            }
         };
         onEnable.invoke(null, enableCb);
         Method onDisable = apiClass.getMethod("onDisable", Consumer.class);
         Consumer<Object> disableCb = api -> {
            this.available = false;
            this.apiRef.set(null);
         };
         onDisable.invoke(null, disableCb);
      } catch (Throwable var7) {
         this.plugin.getLogger().warning("BlueMap hook disabled: " + var7.getMessage());
         this.available = false;
      }
   }

   public boolean isAvailable() {
      return this.available && this.apiRef.get() != null;
   }

   public void refresh(List<ChunkCostTracker.ChunkCost> top) {
      Object api = this.apiRef.get();
      if (api == null) {
         this.pendingTop = top;
      } else {
         try {
            Class<?> shapeClass = Class.forName("de.bluecolored.bluemap.api.math.Shape");
            Class<?> colorClass = Class.forName("de.bluecolored.bluemap.api.math.Color");
            Class<?> markerSetClass = Class.forName("de.bluecolored.bluemap.api.markers.MarkerSet");
            Class<?> extrudeClass = Class.forName("de.bluecolored.bluemap.api.markers.ExtrudeMarker");
            Method shapeCreateRect = shapeClass.getMethod("createRect", double.class, double.class, double.class, double.class);
            Method msBuilder = markerSetClass.getMethod("builder");
            Method extrudeBuilder = extrudeClass.getMethod("builder");
            double max = 0.0;

            for (ChunkCostTracker.ChunkCost c : top) {
               if (c.costScore > max) {
                  max = c.costScore;
               }
            }

            if (max <= 0.0) {
               max = 1.0;
            }

            Map<String, List<ChunkCostTracker.ChunkCost>> byWorld = new HashMap<>();

            for (ChunkCostTracker.ChunkCost cx : top) {
               byWorld.computeIfAbsent(cx.worldName, k -> new ArrayList<>()).add(cx);
            }

            Method getWorld = api.getClass().getMethod("getWorld", Object.class);

            for (Entry<String, List<ChunkCostTracker.ChunkCost>> e : byWorld.entrySet()) {
               World w = Bukkit.getWorld(e.getKey());
               if (w != null) {
                  Object bmWorldOpt = getWorld.invoke(api, w);
                  if (bmWorldOpt instanceof Optional) {
                     Object bmWorld = ((Optional)bmWorldOpt).orElse(null);
                     if (bmWorld != null) {
                        Object msb = msBuilder.invoke(null);
                        Class<?> msbClass = msb.getClass();
                        this.tryCall(msbClass, msb, "label", String.class, "Hot Chunks");
                        this.tryCallBool(msbClass, msb, "toggleable", true);
                        this.tryCallBool(msbClass, msb, "defaultHidden", false);
                        Object markerSet = msbClass.getMethod("build").invoke(msb);
                        Object markerMap = markerSetClass.getMethod("getMarkers").invoke(markerSet);
                        int i = 0;

                        for (ChunkCostTracker.ChunkCost cx : e.getValue()) {
                           int bx = cx.x << 4;
                           int bz = cx.z << 4;
                           Object shape = shapeCreateRect.invoke(null, (double)bx, (double)bz, (double)(bx + 16), (double)(bz + 16));
                           int rgb = this.colorFor(cx.costScore / max);
                           Object fill = colorClass.getConstructor(int.class, int.class, int.class, float.class)
                              .newInstance(rgb >> 16 & 0xFF, rgb >> 8 & 0xFF, rgb & 0xFF, 0.35F);
                           Object line = colorClass.getConstructor(int.class, int.class, int.class, float.class)
                              .newInstance(rgb >> 16 & 0xFF, rgb >> 8 & 0xFF, rgb & 0xFF, 0.9F);
                           Object mb = extrudeBuilder.invoke(null);
                           Class<?> mbClass = mb.getClass();
                           this.tryCall(mbClass, mb, "label", String.class, "Cost " + (int)cx.costScore);
                           mbClass.getMethod("shape", shapeClass, float.class, float.class)
                              .invoke(mb, shape, (float)w.getMinHeight(), (float)(w.getMinHeight() + 64));
                           mbClass.getMethod("fillColor", colorClass).invoke(mb, fill);
                           mbClass.getMethod("lineColor", colorClass).invoke(mb, line);
                           this.tryCallBool(mbClass, mb, "depthTestEnabled", false);
                           Object marker = mbClass.getMethod("build").invoke(mb);
                           markerMap.getClass().getMethod("put", Object.class, Object.class).invoke(markerMap, "chunkmanager.hotchunks." + i++, marker);
                        }

                        for (Object map : (Iterable)bmWorld.getClass().getMethod("getMaps").invoke(bmWorld)) {
                           Object sets = map.getClass().getMethod("getMarkerSets").invoke(map);
                           sets.getClass().getMethod("put", Object.class, Object.class).invoke(sets, "chunkmanager.hotchunks", markerSet);
                        }
                     }
                  }
               }
            }
         } catch (Throwable var35) {
            this.plugin.getLogger().warning("BlueMap refresh failed: " + var35.getMessage());
         }
      }
   }

   private void tryCall(Class<?> cls, Object instance, String name, Class<?> argType, Object arg) {
      try {
         cls.getMethod(name, argType).invoke(instance, arg);
      } catch (Throwable var7) {
      }
   }

   private void tryCallBool(Class<?> cls, Object instance, String name, boolean value) {
      try {
         cls.getMethod(name, Boolean.class).invoke(instance, value);
      } catch (Throwable var7) {
         try {
            cls.getMethod(name, boolean.class).invoke(instance, value);
         } catch (Throwable var6) {
         }
      }
   }

   public void clear() {
      Object api = this.apiRef.get();
      if (api != null) {
         try {
            for (Object w : (Iterable)api.getClass().getMethod("getWorlds").invoke(api)) {
               for (Object map : (Iterable)w.getClass().getMethod("getMaps").invoke(w)) {
                  Object sets = map.getClass().getMethod("getMarkerSets").invoke(map);
                  sets.getClass().getMethod("remove", Object.class).invoke(sets, "chunkmanager.hotchunks");
               }
            }
         } catch (Throwable var9) {
         }
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
