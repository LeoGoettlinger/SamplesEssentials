package com.magmaguy.elitemobs.peacebanner;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.config.ConfigurationEngine;
import com.magmaguy.elitemobs.config.PeaceBannerConfig;
import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Banner;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.scheduler.BukkitTask;

public class PeaceBannerManager {
   private static final HashMap<UUID, HashMap<Long, Integer>> protectedChunks = new HashMap<>();
   private static final HashMap<String, Set<PeaceBannerManager.ChunkEntry>> bannerChunkMap = new HashMap<>();
   private static final HashMap<String, PeaceBannerManager.BannerData> bannerDataMap = new HashMap<>();
   private static File dataFile;
   private static BukkitTask pendingSave;
   private static CompletableFuture<Void> writing;
   private static long revision;
   private static volatile long persistedRevision;
   private static volatile boolean closing;
   private static final Map<PeaceBannerManager.ChunkEntry, Set<String>> sourceChunks = new HashMap<>();
   private static final Map<UUID, Set<String>> worldBanners = new HashMap<>();

   public static boolean isProtected(Location location) {
      if (!PeaceBannerConfig.isEnabled()) {
         return false;
      } else if (location != null && location.getWorld() != null) {
         UUID worldUID = location.getWorld().getUID();
         HashMap<Long, Integer> worldChunks = protectedChunks.get(worldUID);
         if (worldChunks == null) {
            return false;
         } else {
            long chunkKey = chunkKey(location.getBlockX() >> 4, location.getBlockZ() >> 4);
            return worldChunks.containsKey(chunkKey);
         }
      } else {
         return false;
      }
   }

   public static void registerBanner(Location bannerLocation) {
      String locKey = locationKey(bannerLocation);
      if (!bannerChunkMap.containsKey(locKey)) {
         PeaceBannerManager.BannerData data = new PeaceBannerManager.BannerData(
            bannerLocation.getWorld().getName(),
            bannerLocation.getWorld().getUID(),
            bannerLocation.getBlockX(),
            bannerLocation.getBlockY(),
            bannerLocation.getBlockZ(),
            PeaceBannerConfig.getChunkRadius()
         );
         addRecord(locKey, data);
         activate(locKey, data);
         saveData();
      }
   }

   public static boolean unregisterBanner(Location bannerLocation) {
      String locKey = locationKey(bannerLocation);
      if (!removeRecord(locKey)) {
         return false;
      } else {
         saveData();
         return true;
      }
   }

   private static boolean removeRecord(String locKey) {
      PeaceBannerManager.BannerData data = bannerDataMap.remove(locKey);
      if (data == null) {
         return false;
      } else {
         Set<PeaceBannerManager.ChunkEntry> coverage = bannerChunkMap.remove(locKey);
         if (coverage != null) {
            for (PeaceBannerManager.ChunkEntry entry : coverage) {
               HashMap<Long, Integer> chunks = protectedChunks.get(entry.worldUID());
               if (chunks != null) {
                  chunks.computeIfPresent(entry.chunkKey(), (key, count) -> count <= 1 ? null : count - 1);
                  if (chunks.isEmpty()) {
                     protectedChunks.remove(entry.worldUID());
                  }
               }
            }
         }

         PeaceBannerManager.ChunkEntry source = new PeaceBannerManager.ChunkEntry(data.worldUUID(), chunkKey(data.x() >> 4, data.z() >> 4));
         removeIndex(sourceChunks, source, locKey);
         removeIndex(worldBanners, data.worldUUID(), locKey);
         return true;
      }
   }

   private static <K> void removeIndex(Map<K, Set<String>> index, K key, String location) {
      Set<String> entries = index.get(key);
      if (entries != null) {
         entries.remove(location);
         if (entries.isEmpty()) {
            index.remove(key);
         }
      }
   }

   private static void addRecord(String key, PeaceBannerManager.BannerData data) {
      if (bannerDataMap.putIfAbsent(key, data) == null) {
         worldBanners.computeIfAbsent(data.worldUUID(), ignored -> new HashSet<>()).add(key);
         sourceChunks.computeIfAbsent(new PeaceBannerManager.ChunkEntry(data.worldUUID(), chunkKey(data.x() >> 4, data.z() >> 4)), ignored -> new HashSet<>())
            .add(key);
      }
   }

   private static void activate(String key, PeaceBannerManager.BannerData data) {
      if (!bannerChunkMap.containsKey(key)) {
         Set<PeaceBannerManager.ChunkEntry> coverage = new HashSet<>();
         int radius = data.chunkRadius();
         int chunkX = data.x() >> 4;
         int chunkZ = data.z() >> 4;
         HashMap<Long, Integer> chunks = protectedChunks.computeIfAbsent(data.worldUUID(), ignored -> new HashMap<>());

         for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {
               long covered = chunkKey(chunkX + x, chunkZ + z);
               coverage.add(new PeaceBannerManager.ChunkEntry(data.worldUUID(), covered));
               chunks.merge(covered, 1, Integer::sum);
            }
         }

         bannerChunkMap.put(key, coverage);
      }
   }

   public static void activateWorld(World world) {
      Set<String> keys = worldBanners.get(world.getUID());
      if (keys != null) {
         Set<PeaceBannerManager.ChunkEntry> loadedSources = new HashSet<>();

         for (String key : Set.copyOf(keys)) {
            PeaceBannerManager.BannerData data = bannerDataMap.get(key);
            if (data != null) {
               activate(key, data);
               int x = data.x() >> 4;
               int z = data.z() >> 4;
               if (world.isChunkLoaded(x, z)) {
                  loadedSources.add(new PeaceBannerManager.ChunkEntry(world.getUID(), chunkKey(x, z)));
               }
            }
         }

         for (PeaceBannerManager.ChunkEntry source : loadedSources) {
            validateChunk(world.getChunkAt((int)(source.chunkKey() >> 32), (int)source.chunkKey()));
         }
      }
   }

   private static long chunkKey(int chunkX, int chunkZ) {
      return (long)chunkX << 32 | chunkZ & 4294967295L;
   }

   private static String locationKey(Location loc) {
      return loc.getWorld().getUID() + ":" + loc.getBlockX() + ":" + loc.getBlockY() + ":" + loc.getBlockZ();
   }

   public static void loadData() {
      closing = false;
      if (dataFile != null && revision != persistedRevision) {
         MetadataHandler.PLUGIN.getLogger().warning("Retaining unsaved Peace Banner changes after failed shutdown flush.");

         for (World world : Bukkit.getWorlds()) {
            activateWorld(world);
         }

         saveData();
      } else {
         dataFile = new File(MetadataHandler.PLUGIN.getDataFolder(), "data/peace-banners.yml");
         if (dataFile.exists()) {
            YamlConfiguration yaml = YamlConfiguration.loadConfiguration(dataFile);

            for (String key : yaml.getKeys(false)) {
               ConfigurationSection section = yaml.getConfigurationSection(key);
               if (section != null) {
                  String worldName = section.getString("worldName");
                  String worldUUIDString = section.getString("worldUUID");
                  if (worldUUIDString != null) {
                     UUID worldUUID;
                     try {
                        worldUUID = UUID.fromString(worldUUIDString);
                     } catch (IllegalArgumentException var14) {
                        continue;
                     }

                     int x = section.getInt("x");
                     int y = section.getInt("y");
                     int z = section.getInt("z");
                     int chunkRadius = section.getInt("chunkRadius", PeaceBannerConfig.getChunkRadius());
                     PeaceBannerManager.BannerData data = new PeaceBannerManager.BannerData(worldName, worldUUID, x, y, z, chunkRadius);
                     String location = worldUUID + ":" + x + ":" + y + ":" + z;
                     addRecord(location, data);
                     World world = Bukkit.getWorld(worldUUID);
                     if (world != null) {
                        activate(location, data);
                     } else if (worldName != null && Bukkit.getWorld(worldName) != null) {
                        MetadataHandler.PLUGIN
                           .getLogger()
                           .warning("Peace Banner at " + location + " belongs to another world UUID; the same-named world was left unchanged.");
                     }
                  }
               }
            }
         }
      }
   }

   public static void saveData() {
      if (dataFile != null && !closing) {
         revision++;
         queueSave();
      }
   }

   private static void queueSave() {
      if (!closing && pendingSave == null && writing == null) {
         pendingSave = Bukkit.getScheduler().runTaskLater(MetadataHandler.PLUGIN, () -> {
            pendingSave = null;
            long capturedRevision = revision;
            List<PeaceBannerManager.BannerData> snapshot = List.copyOf(bannerDataMap.values());
            File target = dataFile;
            CompletableFuture<Void> completion = new CompletableFuture<>();
            writing = completion;

            try {
               Bukkit.getScheduler().runTaskAsynchronously(MetadataHandler.PLUGIN, () -> {
                  try {
                     writeSnapshot(snapshot, target);
                     persistedRevision = capturedRevision;
                     completion.complete(null);
                  } catch (RuntimeException var14) {
                     MetadataHandler.PLUGIN.getLogger().warning("Peace Banner changes remain unsaved: " + var14.getMessage());
                     completion.completeExceptionally(var14);
                  } finally {
                     if (!closing) {
                        try {
                           Bukkit.getScheduler().runTask(MetadataHandler.PLUGIN, () -> {
                              if (writing == completion) {
                                 writing = null;
                                 if (revision > capturedRevision) {
                                    queueSave();
                                 }
                              }
                           });
                        } catch (RuntimeException var13) {
                        }
                     }
                  }
               });
            } catch (RuntimeException var6) {
               writing = null;
               completion.completeExceptionally(var6);
               MetadataHandler.PLUGIN.getLogger().warning("Could not schedule Peace Banner save: " + var6.getMessage());
            }
         }, 1L);
      }
   }

   private static void writeSnapshot(List<PeaceBannerManager.BannerData> snapshot, File target) {
      YamlConfiguration yaml = new YamlConfiguration();
      int index = 0;

      for (PeaceBannerManager.BannerData data : snapshot) {
         String key = "banner_" + index++;
         yaml.set(key + ".worldName", data.worldName());
         yaml.set(key + ".worldUUID", data.worldUUID().toString());
         yaml.set(key + ".x", data.x());
         yaml.set(key + ".y", data.y());
         yaml.set(key + ".z", data.z());
         yaml.set(key + ".chunkRadius", data.chunkRadius());
      }

      ConfigurationEngine.fileSaverSerialized(yaml.saveToString(), target);
   }

   public static void validateChunk(Chunk chunk) {
      PeaceBannerManager.ChunkEntry source = new PeaceBannerManager.ChunkEntry(chunk.getWorld().getUID(), chunkKey(chunk.getX(), chunk.getZ()));
      Set<String> keys = sourceChunks.get(source);
      if (keys != null) {
         boolean changed = false;

         for (String key : Set.copyOf(keys)) {
            PeaceBannerManager.BannerData data = bannerDataMap.get(key);
            if (data != null) {
               Block block = chunk.getWorld().getBlockAt(data.x(), data.y(), data.z());
               if (!(block.getState() instanceof Banner)) {
                  changed |= removeRecord(key);
               }
            }
         }

         if (changed) {
            saveData();
         }
      }
   }

   public static Map<String, PeaceBannerManager.BannerData> getAllBannerData() {
      return Collections.unmodifiableMap(bannerDataMap);
   }

   public static int getBannerCount() {
      return bannerDataMap.size();
   }

   public static void shutdown() {
      closing = true;
      if (pendingSave != null) {
         pendingSave.cancel();
      }

      pendingSave = null;
      if (writing != null) {
         try {
            writing.join();
         } catch (CompletionException var2) {
         }

         writing = null;
      }

      if (dataFile != null && revision != persistedRevision) {
         try {
            writeSnapshot(List.copyOf(bannerDataMap.values()), dataFile);
            persistedRevision = revision;
         } catch (RuntimeException var1) {
            MetadataHandler.PLUGIN.getLogger().severe("Peace Banner shutdown save failed; retaining changes in memory: " + var1.getMessage());
            return;
         }
      }

      protectedChunks.clear();
      bannerChunkMap.clear();
      bannerDataMap.clear();
      sourceChunks.clear();
      worldBanners.clear();
   }

   public record BannerData(String worldName, UUID worldUUID, int x, int y, int z, int chunkRadius) {
   }

   record ChunkEntry(UUID worldUID, long chunkKey) {
   }
}
