package com.skyblockexp.ezrtp.teleport.biome;

import com.skyblockexp.ezrtp.platform.PlatformRuntimeRegistry;
import com.skyblockexp.ezrtp.storage.HotspotStorage;
import com.skyblockexp.ezrtp.util.compat.BiomeCompat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.block.Biome;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.plugin.java.JavaPlugin;

public final class RareBiomeRegistry {
   private static final int MAX_HOTSPOTS_PER_BIOME = 30;
   private static final int HOTSPOT_RADIUS = 256;
   private final JavaPlugin plugin;
   private final Map<String, Map<Biome, List<RareBiomeRegistry.BiomeHotspot>>> worldHotspots;
   private final Set<Biome> rareBiomes;
   private final Set<Long> scannedChunks;
   private RareBiomeRegistry.RareBiomeScanListener scanListener;
   private volatile boolean enabled;
   private volatile boolean backgroundScanningEnabled;
   private volatile HotspotStorage storage;

   public RareBiomeRegistry(JavaPlugin plugin, Set<Biome> rareBiomes) {
      this(plugin, rareBiomes, null);
   }

   public RareBiomeRegistry(JavaPlugin plugin, Set<Biome> rareBiomes, HotspotStorage storage) {
      this.plugin = plugin;
      this.worldHotspots = new ConcurrentHashMap<>();
      this.rareBiomes = (Set<Biome>)(rareBiomes != null ? new HashSet<>(rareBiomes) : getDefaultRareBiomes());
      this.scannedChunks = ConcurrentHashMap.newKeySet();
      this.storage = storage;
      this.enabled = true;
      this.backgroundScanningEnabled = true;
   }

   private static Set<Biome> getDefaultRareBiomes() {
      Set<Biome> rare = new HashSet<>();

      for (String name : new String[]{
         "MUSHROOM_FIELDS",
         "JUNGLE",
         "BAMBOO_JUNGLE",
         "BADLANDS",
         "ERODED_BADLANDS",
         "WOODED_BADLANDS",
         "ICE_SPIKES",
         "SUNFLOWER_PLAINS",
         "FLOWER_FOREST",
         "MODIFIED_JUNGLE",
         "MODIFIED_JUNGLE_EDGE",
         "DEEP_DARK",
         "SULFUR_CAVES"
      }) {
         Biome b = BiomeCompat.safeValueOf(name);
         if (b != null) {
            rare.add(b);
         }
      }

      return rare;
   }

   public void setEnabled(boolean enabled) {
      this.enabled = enabled;
      if (!enabled) {
         this.stopBackgroundScanning();
         this.clear();
      }
   }

   public boolean isEnabled() {
      return this.enabled;
   }

   public void setBackgroundScanningEnabled(boolean enabled) {
      this.backgroundScanningEnabled = enabled;
      if (enabled) {
         this.startBackgroundScanning();
      } else {
         this.stopBackgroundScanning();
      }
   }

   public boolean isBackgroundScanningEnabled() {
      return this.backgroundScanningEnabled;
   }

   public boolean isRareBiome(Biome biome) {
      return this.rareBiomes.contains(biome);
   }

   public void registerHotspot(Location location) {
      if (this.enabled && location != null && location.getWorld() != null) {
         Biome biome = location.getBlock().getBiome();
         if (this.isRareBiome(biome)) {
            String worldName = location.getWorld().getName();
            Map<Biome, List<RareBiomeRegistry.BiomeHotspot>> hotspots = this.worldHotspots.computeIfAbsent(worldName, k -> new ConcurrentHashMap<>());
            List<RareBiomeRegistry.BiomeHotspot> biomeHotspots = hotspots.computeIfAbsent(biome, k -> new ArrayList<>());
            synchronized (biomeHotspots) {
               boolean tooClose = biomeHotspots.stream().anyMatch(hs -> hs.location().distanceSquared(location) < 65536.0);
               if (!tooClose) {
                  if (biomeHotspots.size() >= 30) {
                     biomeHotspots.remove(0);
                  }

                  biomeHotspots.add(new RareBiomeRegistry.BiomeHotspot(location.clone(), System.currentTimeMillis()));
                  if (this.storage != null) {
                     HotspotStorage s = this.storage;
                     double fx = location.getX();
                     double fy = location.getY();
                     double fz = location.getZ();
                     String biomeName = BiomeCompat.safeName(biome);
                     PlatformRuntimeRegistry.get().scheduler().executeAsync(() -> s.saveHotspot(worldName, biomeName, fx, fy, fz));
                  }
               }
            }
         }
      }
   }

   public List<Location> getHotspots(World world, Biome biome) {
      if (this.enabled && world != null && this.isRareBiome(biome)) {
         Map<Biome, List<RareBiomeRegistry.BiomeHotspot>> hotspots = this.worldHotspots.get(world.getName());
         if (hotspots == null) {
            return Collections.emptyList();
         } else {
            List<RareBiomeRegistry.BiomeHotspot> biomeHotspots = hotspots.get(biome);
            if (biomeHotspots != null && !biomeHotspots.isEmpty()) {
               List<Location> shuffled;
               synchronized (biomeHotspots) {
                  shuffled = new ArrayList<>(biomeHotspots.size());

                  for (RareBiomeRegistry.BiomeHotspot hs : biomeHotspots) {
                     shuffled.add(hs.location());
                  }
               }

               Collections.shuffle(shuffled);
               return shuffled;
            } else {
               return Collections.emptyList();
            }
         }
      } else {
         return Collections.emptyList();
      }
   }

   public int getHotspotCount(World world, Biome biome) {
      if (this.enabled && world != null && this.isRareBiome(biome)) {
         Map<Biome, List<RareBiomeRegistry.BiomeHotspot>> hotspots = this.worldHotspots.get(world.getName());
         if (hotspots == null) {
            return 0;
         } else {
            List<RareBiomeRegistry.BiomeHotspot> biomeHotspots = hotspots.get(biome);
            if (biomeHotspots == null) {
               return 0;
            } else {
               synchronized (biomeHotspots) {
                  return biomeHotspots.size();
               }
            }
         }
      } else {
         return 0;
      }
   }

   public void startBackgroundScanning() {
      if (this.enabled && this.backgroundScanningEnabled) {
         this.stopBackgroundScanning();
         this.scanListener = new RareBiomeRegistry.RareBiomeScanListener();
         Bukkit.getPluginManager().registerEvents(this.scanListener, this.plugin);
      }
   }

   public void stopBackgroundScanning() {
      if (this.scanListener != null) {
         HandlerList.unregisterAll(this.scanListener);
         this.scanListener = null;
      }
   }

   private void scanChunk(World world, int chunkX, int chunkZ) {
      if (this.enabled && this.backgroundScanningEnabled) {
         if (world.getEnvironment() == Environment.NORMAL) {
            int baseX = chunkX << 4;
            int baseZ = chunkZ << 4;

            for (int dx = 0; dx < 16; dx += 8) {
               for (int dz = 0; dz < 16; dz += 8) {
                  Location loc = world.getHighestBlockAt(baseX + dx, baseZ + dz).getLocation();
                  Biome biome = loc.getBlock().getBiome();
                  if (this.isRareBiome(biome)) {
                     this.registerHotspot(loc);
                  }
               }
            }
         }
      }
   }

   public void loadFromStorage(HotspotStorage hotspotStorage) {
      if (hotspotStorage != null) {
         this.storage = hotspotStorage;

         for (HotspotStorage.HotspotRecord rec : hotspotStorage.loadAll()) {
            Biome biome = BiomeCompat.safeValueOf(rec.biome());
            if (biome != null && this.isRareBiome(biome)) {
               Map<Biome, List<RareBiomeRegistry.BiomeHotspot>> hotspots = this.worldHotspots.computeIfAbsent(rec.world(), k -> new ConcurrentHashMap<>());
               List<RareBiomeRegistry.BiomeHotspot> biomeHotspots = hotspots.computeIfAbsent(biome, k -> new ArrayList<>());
               World world = Bukkit.getWorld(rec.world());
               if (world != null) {
                  Location loc = new Location(world, rec.x(), rec.y(), rec.z());
                  synchronized (biomeHotspots) {
                     if (biomeHotspots.size() < 30) {
                        biomeHotspots.add(new RareBiomeRegistry.BiomeHotspot(loc, rec.timestamp()));
                     }
                  }
               }
            }
         }
      }
   }

   public void clear() {
      this.worldHotspots.clear();
      this.scannedChunks.clear();
   }

   public void clear(String worldName) {
      if (worldName != null) {
         this.worldHotspots.remove(worldName);
      }
   }

   public RareBiomeRegistry.RegistryStats getStats() {
      int totalHotspots = 0;
      int totalBiomes = 0;
      Map<Biome, Integer> biomeDistribution = new HashMap<>();

      for (Map<Biome, List<RareBiomeRegistry.BiomeHotspot>> worldMap : this.worldHotspots.values()) {
         totalBiomes += worldMap.size();

         for (Entry<Biome, List<RareBiomeRegistry.BiomeHotspot>> entry : worldMap.entrySet()) {
            List<RareBiomeRegistry.BiomeHotspot> list = entry.getValue();
            int count;
            synchronized (list) {
               count = list.size();
            }

            totalHotspots += count;
            biomeDistribution.merge(entry.getKey(), count, Integer::sum);
         }
      }

      return new RareBiomeRegistry.RegistryStats(this.worldHotspots.size(), totalBiomes, totalHotspots, this.rareBiomes.size(), biomeDistribution);
   }

   public void shutdown() {
      this.stopBackgroundScanning();
      this.clear();
      this.enabled = false;
   }

   private record BiomeHotspot(Location location, long timestamp) {
   }

   private final class RareBiomeScanListener implements Listener {
      @EventHandler(
         priority = EventPriority.MONITOR,
         ignoreCancelled = true
      )
      public void onChunkLoad(ChunkLoadEvent event) {
         int cx = event.getChunk().getX();
         int cz = event.getChunk().getZ();
         long key = (long)event.getWorld().getName().hashCode() << 32 | (long)cx << 16 & 4294901760L | cz & 65535;
         if (RareBiomeRegistry.this.scannedChunks.add(key)) {
            RareBiomeRegistry.this.scanChunk(event.getWorld(), cx, cz);
         }
      }
   }

   public record RegistryStats(int worldCount, int biomeCount, int hotspotCount, int rareBiomeCount, Map<Biome, Integer> biomeDistribution) {
      @Override
      public String toString() {
         return String.format(
            "RegistryStats[worlds=%d, biomes=%d, hotspots=%d, rareBiomes=%d]", this.worldCount, this.biomeCount, this.hotspotCount, this.rareBiomeCount
         );
      }
   }
}
