package cm.chunkManager.components;

import cm.chunkManager.utils.Scheduler;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class RegionTickMonitor {
   private static final int REGION_SHIFT = 5;
   private static final long STALE_NANOS = 10000000000L;
   private final JavaPlugin plugin;
   private final Map<String, RegionTickMonitor.RegionStats> stats = new ConcurrentHashMap<>();
   private final Map<String, Scheduler.Handle> heartbeats = new ConcurrentHashMap<>();
   private boolean enabled;

   public RegionTickMonitor(JavaPlugin plugin) {
      this.plugin = plugin;
      this.enabled = true;
   }

   public void setEnabled(boolean enabled) {
      this.enabled = enabled;
      if (!enabled) {
         this.stop();
      }
   }

   public boolean isEnabled() {
      return this.enabled;
   }

   public void tick() {
      if (this.enabled) {
         Set<String> active = new HashSet<>();

         for (Player player : Bukkit.getOnlinePlayers()) {
            Location loc = player.getLocation();
            World world = loc.getWorld();
            if (world != null) {
               int cx = loc.getBlockX() >> 4;
               int cz = loc.getBlockZ() >> 4;
               active.add(this.ensureTracker(world, cx, cz));
            }
         }

         long now = System.nanoTime();
         Iterator<Entry<String, RegionTickMonitor.RegionStats>> it = this.stats.entrySet().iterator();

         while (it.hasNext()) {
            Entry<String, RegionTickMonitor.RegionStats> e = it.next();
            if (!active.contains(e.getKey()) && now - e.getValue().lastTickNanos > 10000000000L) {
               Scheduler.Handle h = this.heartbeats.remove(e.getKey());
               if (h != null) {
                  h.cancel();
               }

               it.remove();
            }
         }
      }
   }

   private String ensureTracker(World world, int chunkX, int chunkZ) {
      int rx = chunkX >> 5;
      int rz = chunkZ >> 5;
      String key = world.getName() + ";" + rx + ";" + rz;
      this.stats.computeIfAbsent(key, k -> new RegionTickMonitor.RegionStats(world.getName(), rx, rz));
      if (!this.heartbeats.containsKey(key)) {
         RegionTickMonitor.RegionStats s = this.stats.get(key);
         s.lastTickNanos = System.nanoTime();
         int anchorX = (rx << 5) + 16;
         int anchorZ = (rz << 5) + 16;
         Scheduler.Handle handle = Scheduler.runAtChunkTimer(this.plugin, world, anchorX, anchorZ, () -> this.onHeartbeat(key), 1L, 1L);
         this.heartbeats.put(key, handle);
      }

      return key;
   }

   private void onHeartbeat(String key) {
      RegionTickMonitor.RegionStats s = this.stats.get(key);
      if (s != null) {
         long now = System.nanoTime();
         long delta = s.lastTickNanos == 0L ? 50000000L : now - s.lastTickNanos;
         s.lastTickNanos = now;
         double deltaMs = delta / 1000000.0;
         s.mspt = deltaMs;
         s.avgMspt = (s.avgMspt * 19.0 + deltaMs) / 20.0;
         s.samples++;
      }
   }

   public Collection<RegionTickMonitor.RegionStats> getAll() {
      return this.stats.values();
   }

   public List<RegionTickMonitor.RegionStats> getLaggiest(int count) {
      return this.stats
         .values()
         .stream()
         .sorted(Comparator.<RegionTickMonitor.RegionStats>comparingDouble(r -> r.avgMspt).reversed())
         .limit(count)
         .collect(Collectors.toList());
   }

   public RegionTickMonitor.RegionStats getForChunk(World world, int chunkX, int chunkZ) {
      int rx = chunkX >> 5;
      int rz = chunkZ >> 5;
      return this.stats.get(world.getName() + ";" + rx + ";" + rz);
   }

   public double getWorstMspt() {
      double worst = 0.0;

      for (RegionTickMonitor.RegionStats s : this.stats.values()) {
         if (s.avgMspt > worst) {
            worst = s.avgMspt;
         }
      }

      return worst;
   }

   public int getTrackedCount() {
      return this.stats.size();
   }

   public void stop() {
      for (Scheduler.Handle h : this.heartbeats.values()) {
         if (h != null) {
            h.cancel();
         }
      }

      this.heartbeats.clear();
      this.stats.clear();
   }

   public static class RegionStats {
      public final String worldName;
      public final int regionX;
      public final int regionZ;
      public volatile double avgMspt;
      public volatile double mspt;
      public volatile long lastTickNanos;
      public volatile long samples;

      RegionStats(String worldName, int regionX, int regionZ) {
         this.worldName = worldName;
         this.regionX = regionX;
         this.regionZ = regionZ;
         this.avgMspt = 50.0;
         this.mspt = 50.0;
         this.lastTickNanos = 0L;
         this.samples = 0L;
      }

      public double getTps() {
         return this.avgMspt <= 0.0 ? 20.0 : Math.min(20.0, 1000.0 / Math.max(this.avgMspt, 50.0));
      }
   }
}
