package de.theredend2000.advancedhunt.data;

import de.theredend2000.advancedhunt.Main;
import de.theredend2000.advancedhunt.model.ActRule;
import de.theredend2000.advancedhunt.model.Collection;
import de.theredend2000.advancedhunt.model.PlaceItem;
import de.theredend2000.advancedhunt.model.PlayerData;
import de.theredend2000.advancedhunt.model.Reward;
import de.theredend2000.advancedhunt.model.RewardPreset;
import de.theredend2000.advancedhunt.model.RewardPresetType;
import de.theredend2000.advancedhunt.model.RewardType;
import de.theredend2000.advancedhunt.model.Treasure;
import de.theredend2000.advancedhunt.model.TreasureCore;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.Map.Entry;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.logging.Level;
import java.util.stream.Collectors;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

public class YamlRepository implements DataRepository {
   private static final String YML_EXTENSION = ".yml";
   private final JavaPlugin plugin;
   private final File collectionsFolder;
   private final File playerDataFolder;
   private File leaderboardFile;
   private File finderIndexFile;
   private File systemFile;
   private File legacyRewardPresetsFile;
   private File rewardPresetsFolder;
   private File treasureRewardPresetsFolder;
   private File collectionRewardPresetsFolder;
   private File placeItemsFolder;
   private File placePresetGroupsFile;
   private BukkitTask flushTask;
   private final Map<UUID, UUID> treasureToCollectionIndex = new ConcurrentHashMap<>();
   private final Map<UUID, Set<UUID>> treasureToFindersIndex = new ConcurrentHashMap<>();
   private volatile boolean finderIndexDirty = false;
   private final Map<File, ReadWriteLock> fileLocks = new ConcurrentHashMap<>();
   private final Set<CompletableFuture<?>> pendingOperations = ConcurrentHashMap.newKeySet();
   private volatile long shutdownTimeoutMs = 5000L;
   private final Map<Integer, Runnable> schemaMigrations = new HashMap<>();
   private int cachedSchemaVersion = -1;

   public YamlRepository(JavaPlugin plugin) {
      this.plugin = plugin;
      this.collectionsFolder = new File(plugin.getDataFolder(), "collections");
      this.playerDataFolder = new File(plugin.getDataFolder(), "playerdata");
      this.registerMigrations();
   }

   private void registerMigrations() {
      this.schemaMigrations.put(1, () -> {});
      this.schemaMigrations.put(2, () -> {
         try {
            this.migrateLegacyRewardPresetsIfNeeded();
         } catch (Exception var2) {
            this.plugin.getLogger().log(Level.WARNING, "Failed to migrate legacy reward presets", (Throwable)var2);
            throw var2;
         }
      });
   }

   @Override
   public void init() {
      if (!this.plugin.getDataFolder().exists()) {
         this.plugin.getDataFolder().mkdirs();
      }

      if (!this.collectionsFolder.exists()) {
         this.collectionsFolder.mkdirs();
      }

      if (!this.playerDataFolder.exists()) {
         this.playerDataFolder.mkdirs();
      }

      this.leaderboardFile = new File(this.plugin.getDataFolder(), "leaderboard.yml");
      if (!this.leaderboardFile.exists()) {
         try {
            this.leaderboardFile.createNewFile();
         } catch (IOException var3) {
            this.plugin.getLogger().severe("Failed to create leaderboard file: " + var3.getMessage());
         }
      }

      this.finderIndexFile = new File(this.plugin.getDataFolder(), "finder-index.yml");
      this.systemFile = new File(this.plugin.getDataFolder(), "system.yml");
      this.rewardPresetsFolder = new File(this.plugin.getDataFolder(), "reward-presets");
      this.treasureRewardPresetsFolder = new File(this.rewardPresetsFolder, "treasure");
      this.collectionRewardPresetsFolder = new File(this.rewardPresetsFolder, "collection");
      if (!this.treasureRewardPresetsFolder.exists()) {
         this.treasureRewardPresetsFolder.mkdirs();
      }

      if (!this.collectionRewardPresetsFolder.exists()) {
         this.collectionRewardPresetsFolder.mkdirs();
      }

      this.placeItemsFolder = new File(this.plugin.getDataFolder(), "place-items");
      if (!this.placeItemsFolder.exists()) {
         this.placeItemsFolder.mkdirs();
      }

      this.placePresetGroupsFile = new File(this.plugin.getDataFolder(), "place-preset-groups.yml");
      if (!this.placePresetGroupsFile.exists()) {
         try {
            this.placePresetGroupsFile.createNewFile();
         } catch (IOException var2) {
            this.plugin.getLogger().log(Level.SEVERE, "Could not create place-preset-groups.yml", (Throwable)var2);
         }
      }

      this.legacyRewardPresetsFile = new File(this.plugin.getDataFolder(), "reward-presets.yml");
      this.cleanOrphanedLockFiles();
      this.buildTreasureToCollectionIndex();
      this.loadFinderIndex();
      this.upgradeSchema();
   }

   private void cleanOrphanedLockFiles() {
      int cleaned = this.cleanLockFilesInDirectory(this.plugin.getDataFolder());
      cleaned += this.cleanLockFilesInDirectory(this.playerDataFolder);
      cleaned += this.cleanLockFilesInDirectory(this.collectionsFolder);
      if (cleaned > 0) {
         this.plugin.getLogger().info("Cleaned up " + cleaned + " orphaned .lock files from previous session");
      }
   }

   private int cleanLockFilesInDirectory(File directory) {
      if (directory != null && directory.exists()) {
         int count = 0;
         File[] lockFiles = directory.listFiles((dir, name) -> name.endsWith(".lock"));
         if (lockFiles != null) {
            for (File lockFile : lockFiles) {
               if (lockFile.delete()) {
                  count++;
               }
            }
         }

         File[] subdirs = directory.listFiles(File::isDirectory);
         if (subdirs != null) {
            for (File subdir : subdirs) {
               count += this.cleanLockFilesInDirectory(subdir);
            }
         }

         return count;
      } else {
         return 0;
      }
   }

   public void setShutdownTimeout(long timeoutMs) {
      this.shutdownTimeoutMs = Math.max(1000L, timeoutMs);
   }

   private File getPresetFolder(RewardPresetType type) {
      return type == RewardPresetType.COLLECTION ? this.collectionRewardPresetsFolder : this.treasureRewardPresetsFolder;
   }

   private File getPresetFile(RewardPresetType type, UUID presetId) {
      return new File(this.getPresetFolder(type), presetId.toString() + ".yml");
   }

   private static String sanitizeGroupFolderName(String group) {
      if (group != null && !group.trim().isEmpty()) {
         String trimmed = group.trim();
         String sanitized = trimmed.replaceAll("[^a-zA-Z0-9_-]", "_");
         return sanitized.trim().isEmpty() ? "default" : sanitized;
      } else {
         return "default";
      }
   }

   private ReadWriteLock getFileLock(File file) {
      return this.fileLocks.computeIfAbsent(file.getAbsoluteFile(), k -> new ReentrantReadWriteLock());
   }

   private void cleanupOrphanedLocks() {
      int removed = 0;
      Iterator<Entry<File, ReadWriteLock>> it = this.fileLocks.entrySet().iterator();

      while (it.hasNext()) {
         Entry<File, ReadWriteLock> entry = it.next();
         if (!entry.getKey().exists()) {
            it.remove();
            removed++;
         }
      }

      if (removed > 0 && ((Main)this.plugin).isDebugMode()) {
         this.plugin.getLogger().fine("Cleaned up " + removed + " orphaned file locks");
      }
   }

   private <T> CompletableFuture<T> trackFuture(CompletableFuture<T> future) {
      this.pendingOperations.add(future);
      return future.whenComplete((r, e) -> this.pendingOperations.remove(future));
   }

   @Deprecated
   private void saveConfigAtomic(FileConfiguration config, File file) {
      if (config instanceof YamlConfiguration) {
         this.saveConfigAtomicWithLock((YamlConfiguration)config, file);
      } else {
         this.saveConfigAtomicWithLockInternal(config, file);
      }
   }

   private boolean saveConfigAtomicWithLock(YamlConfiguration config, File file) {
      return this.saveConfigAtomicWithLockInternal(config, file);
   }

   private boolean saveConfigAtomicWithLockInternal(FileConfiguration config, File file) {
      ReadWriteLock rwLock = this.getFileLock(file);
      rwLock.writeLock().lock();

      boolean ex;
      try {
         File parent = file.getParentFile();
         if (parent != null && !parent.exists()) {
            parent.mkdirs();
         }

         File tmp = new File(file.getParentFile(), file.getName() + ".tmp");
         File lockFile = new File(file.getParentFile(), file.getName() + ".lock");

         try {
            try {
               RandomAccessFile raf = new RandomAccessFile(lockFile, "rw");

               boolean var11;
               label708: {
                  boolean e;
                  try {
                     FileChannel channel = raf.getChannel();

                     label689: {
                        try {
                           FileLock lock;
                           label690: {
                              lock = channel.tryLock();

                              try {
                                 if (lock == null) {
                                    this.plugin.getLogger().warning("Could not acquire file lock for " + file.getName() + ", another process may be writing");
                                 }

                                 try {
                                    config.save(tmp);

                                    try {
                                       Files.move(tmp.toPath(), file.toPath(), StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
                                    } catch (Exception var80) {
                                       Files.move(tmp.toPath(), file.toPath(), StandardCopyOption.REPLACE_EXISTING);
                                    }

                                    e = true;
                                 } catch (IOException var81) {
                                    this.plugin.getLogger().log(Level.SEVERE, "Could not save " + file.getName(), (Throwable)var81);
                                    var11 = false;
                                    break label690;
                                 } finally {
                                    if (tmp.exists()) {
                                       tmp.delete();
                                    }
                                 }
                              } catch (Throwable var83) {
                                 if (lock != null) {
                                    try {
                                       lock.close();
                                    } catch (Throwable var79) {
                                       var83.addSuppressed(var79);
                                    }
                                 }

                                 throw var83;
                              }

                              if (lock != null) {
                                 lock.close();
                              }
                              break label689;
                           }

                           if (lock != null) {
                              lock.close();
                           }
                        } catch (Throwable var84) {
                           if (channel != null) {
                              try {
                                 channel.close();
                              } catch (Throwable var78) {
                                 var84.addSuppressed(var78);
                              }
                           }

                           throw var84;
                        }

                        if (channel != null) {
                           channel.close();
                        }
                        break label708;
                     }

                     if (channel != null) {
                        channel.close();
                     }
                  } catch (Throwable var85) {
                     try {
                        raf.close();
                     } catch (Throwable var77) {
                        var85.addSuppressed(var77);
                     }

                     throw var85;
                  }

                  raf.close();
                  return e;
               }

               raf.close();
               return var11;
            } catch (IOException var86) {
               this.plugin.getLogger().log(Level.WARNING, "File locking failed for " + file.getName() + ", attempting save without lock", (Throwable)var86);
            }

            try {
               config.save(tmp);

               try {
                  Files.move(tmp.toPath(), file.toPath(), StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
               } catch (Exception var76) {
                  Files.move(tmp.toPath(), file.toPath(), StandardCopyOption.REPLACE_EXISTING);
               }

               ex = true;
            } catch (IOException var87) {
               this.plugin.getLogger().log(Level.SEVERE, "Could not save " + file.getName(), (Throwable)var87);
               return false;
            } finally {
               if (tmp.exists()) {
                  tmp.delete();
               }
            }
         } finally {
            if (lockFile.exists()) {
               lockFile.delete();
            }
         }
      } finally {
         rwLock.writeLock().unlock();
      }

      return ex;
   }

   private boolean isPresetFolderEmpty(File folder) {
      File[] files = folder.listFiles((dir, name) -> name.endsWith(".yml"));
      return files == null || files.length == 0;
   }

   private void migrateLegacyRewardPresetsIfNeeded() {
      if (this.legacyRewardPresetsFile != null && this.legacyRewardPresetsFile.exists()) {
         if (this.rewardPresetsFolder == null) {
            this.rewardPresetsFolder = new File(this.plugin.getDataFolder(), "reward-presets");
         }

         if (this.treasureRewardPresetsFolder == null) {
            this.treasureRewardPresetsFolder = new File(this.rewardPresetsFolder, "treasure");
         }

         if (this.collectionRewardPresetsFolder == null) {
            this.collectionRewardPresetsFolder = new File(this.rewardPresetsFolder, "collection");
         }

         this.treasureRewardPresetsFolder.mkdirs();
         this.collectionRewardPresetsFolder.mkdirs();
         if (this.isPresetFolderEmpty(this.treasureRewardPresetsFolder) && this.isPresetFolderEmpty(this.collectionRewardPresetsFolder)) {
            FileConfiguration legacy = YamlConfiguration.loadConfiguration(this.legacyRewardPresetsFile);
            int migrated = 0;
            migrated += this.migrateLegacySection(legacy, "treasure", RewardPresetType.TREASURE);
            migrated += this.migrateLegacySection(legacy, "collection", RewardPresetType.COLLECTION);
            if (migrated > 0) {
               File backup = new File(this.plugin.getDataFolder(), "reward-presets.legacy.yml");
               if (!backup.exists()) {
                  try {
                     Files.copy(this.legacyRewardPresetsFile.toPath(), backup.toPath());
                  } catch (IOException var5) {
                     this.plugin.getLogger().log(Level.WARNING, "Failed to create reward-presets.legacy.yml backup", (Throwable)var5);
                  }
               }

               this.plugin.getLogger().info("Migrated " + migrated + " reward preset(s) to per-file YAML storage");
            }
         }
      }
   }

   private int migrateLegacySection(FileConfiguration legacy, String rootKey, RewardPresetType type) {
      ConfigurationSection section = legacy.getConfigurationSection(rootKey);
      if (section == null) {
         return 0;
      } else {
         int migrated = 0;

         for (String idStr : section.getKeys(false)) {
            try {
               UUID id = UUID.fromString(idStr);
               String name = section.getString(idStr + ".name");
               List<Map<?, ?>> rewardList = section.getMapList(idStr + ".rewards");
               List<Reward> rewards = this.deserializeRewards(rewardList);
               if (name != null && !name.trim().isEmpty()) {
                  FileConfiguration out = new YamlConfiguration();
                  out.set("name", name);
                  out.set("rewards", this.serializeRewards(rewards));
                  this.saveConfigAtomic(out, this.getPresetFile(type, id));
                  migrated++;
               }
            } catch (Exception var13) {
               this.plugin.getLogger().warning("Failed to migrate legacy preset " + rootKey + "/" + idStr + ": " + var13.getMessage());
            }
         }

         return migrated;
      }
   }

   @Override
   public void reload() {
      this.init();
   }

   @Override
   public int getSchemaVersion() {
      if (this.cachedSchemaVersion != -1) {
         return this.cachedSchemaVersion;
      } else if (this.systemFile != null && this.systemFile.exists()) {
         YamlConfiguration config = YamlConfiguration.loadConfiguration(this.systemFile);
         this.cachedSchemaVersion = config.getInt("schema_version", 0);
         return this.cachedSchemaVersion;
      } else {
         return 0;
      }
   }

   @Override
   public void upgradeSchema() {
      if (this.systemFile == null) {
         this.systemFile = new File(this.plugin.getDataFolder(), "system.yml");
      }

      YamlConfiguration config = YamlConfiguration.loadConfiguration(this.systemFile);
      int currentVersion = config.getInt("schema_version", 0);
      int latestVersion = this.schemaMigrations.keySet().stream().mapToInt(v -> v).max().orElse(0);

      for (int i = currentVersion + 1; i <= latestVersion; i++) {
         Runnable migration = this.schemaMigrations.get(i);
         if (migration == null) {
            this.plugin.getLogger().severe("Missing YAML schema migration for version " + i + "; aborting upgrade");
            break;
         }

         try {
            migration.run();
            this.plugin.getLogger().info("Upgraded YAML data schema to version " + i);
         } catch (Exception var8) {
            this.plugin.getLogger().severe("Failed to apply YAML schema migration for version " + i + ": " + var8.getMessage());
            break;
         }

         config.set("schema_version", i);

         try {
            config.save(this.systemFile);
            this.cachedSchemaVersion = i;
         } catch (IOException var7) {
            this.plugin.getLogger().severe("Failed to persist YAML schema version " + i + " to system.yml; aborting upgrade: " + var7.getMessage());
            break;
         }
      }
   }

   @Override
   public CompletableFuture<List<UUID>> getAllPlayerUUIDs() {
      return this.trackFuture(CompletableFuture.supplyAsync(() -> {
         List<UUID> uuids = new ArrayList<>();
         File[] files = this.playerDataFolder.listFiles((dir, name) -> name.endsWith(".yml"));
         if (files != null) {
            for (File file : files) {
               try {
                  uuids.add(UUID.fromString(file.getName().replace(".yml", "")));
               } catch (IllegalArgumentException var8) {
               }
            }
         }

         return uuids;
      }));
   }

   private void buildTreasureToCollectionIndex() {
      this.treasureToCollectionIndex.clear();
      File[] collectionDirs = this.collectionsFolder.listFiles(File::isDirectory);
      if (collectionDirs != null) {
         for (File collectionDir : collectionDirs) {
            try {
               UUID collectionId = UUID.fromString(collectionDir.getName());
               File treasuresDir = new File(collectionDir, "treasures");
               if (treasuresDir.exists() && treasuresDir.isDirectory()) {
                  File[] files = treasuresDir.listFiles((dir, name) -> name.endsWith(".yml"));
                  if (files != null) {
                     for (File file : files) {
                        try {
                           UUID treasureId = UUID.fromString(file.getName().replace(".yml", ""));
                           this.treasureToCollectionIndex.put(treasureId, collectionId);
                        } catch (IllegalArgumentException var14) {
                        }
                     }
                  }
               }
            } catch (IllegalArgumentException var15) {
            }
         }
      }

      this.plugin.getLogger().info("Built treasure-to-collection index with " + this.treasureToCollectionIndex.size() + " entries");
   }

   private void loadFinderIndex() {
      this.treasureToFindersIndex.clear();
      if (this.finderIndexFile != null && this.finderIndexFile.exists()) {
         FileConfiguration config = YamlConfiguration.loadConfiguration(this.finderIndexFile);
         ConfigurationSection treasuresSection = config.getConfigurationSection("treasures");
         if (treasuresSection != null) {
            for (String treasureIdStr : treasuresSection.getKeys(false)) {
               try {
                  UUID treasureId = UUID.fromString(treasureIdStr);
                  List<String> finderStrs = config.getStringList("treasures." + treasureIdStr);
                  Set<UUID> finders = ConcurrentHashMap.newKeySet();

                  for (String finderStr : finderStrs) {
                     try {
                        finders.add(UUID.fromString(finderStr));
                     } catch (IllegalArgumentException var11) {
                     }
                  }

                  this.treasureToFindersIndex.put(treasureId, finders);
               } catch (IllegalArgumentException var12) {
               }
            }

            this.plugin.getLogger().info("Loaded finder index with " + this.treasureToFindersIndex.size() + " treasure entries");
            return;
         }
      }

      this.rebuildFinderIndex();
   }

   private void rebuildFinderIndex() {
      this.plugin.getLogger().info("Rebuilding finder index from player data...");
      this.treasureToFindersIndex.clear();
      if (this.playerDataFolder.exists()) {
         File[] files = this.playerDataFolder.listFiles((dir, name) -> name.endsWith(".yml"));
         if (files != null) {
            for (File file : files) {
               try {
                  UUID playerUuid = UUID.fromString(file.getName().replace(".yml", ""));
                  FileConfiguration config = YamlConfiguration.loadConfiguration(file);

                  for (String treasureIdStr : config.getStringList("found-treasures")) {
                     try {
                        UUID treasureId = UUID.fromString(treasureIdStr);
                        this.treasureToFindersIndex.computeIfAbsent(treasureId, k -> ConcurrentHashMap.newKeySet()).add(playerUuid);
                     } catch (IllegalArgumentException var12) {
                     }
                  }
               } catch (IllegalArgumentException var13) {
               }
            }
         }
      }

      this.saveFinderIndex();
      this.plugin.getLogger().info("Rebuilt finder index with " + this.treasureToFindersIndex.size() + " treasure entries");
   }

   private void saveFinderIndex() {
      FileConfiguration config = new YamlConfiguration();

      for (Entry<UUID, Set<UUID>> entry : this.treasureToFindersIndex.entrySet()) {
         List<String> finderStrs = new ArrayList<>();

         for (UUID finder : entry.getValue()) {
            finderStrs.add(finder.toString());
         }

         config.set("treasures." + entry.getKey().toString(), finderStrs);
      }

      this.saveConfigAtomic(config, this.finderIndexFile);
      this.finderIndexDirty = false;
   }

   @Override
   public void shutdown() {
      if (this.flushTask != null && !this.flushTask.isCancelled()) {
         this.flushTask.cancel();
      }

      this.awaitPendingOperations(this.shutdownTimeoutMs);
      if (this.finderIndexDirty) {
         this.saveFinderIndex();
      }
   }

   public void awaitPendingOperations(long timeoutMs) {
      if (!this.pendingOperations.isEmpty()) {
         CompletableFuture<?>[] pending = this.pendingOperations.toArray(new CompletableFuture[0]);
         if (pending.length != 0) {
            this.plugin.getLogger().info("Waiting for " + pending.length + " pending operations to complete...");

            try {
               CompletableFuture.allOf(pending).get(timeoutMs, TimeUnit.MILLISECONDS);
               this.plugin.getLogger().info("All pending operations completed successfully");
            } catch (TimeoutException var5) {
               this.plugin.getLogger().warning("Some operations did not complete within " + timeoutMs + "ms timeout");
            } catch (Exception var6) {
               this.plugin.getLogger().log(Level.WARNING, "Error waiting for pending operations", (Throwable)var6);
            }
         }
      }
   }

   @Deprecated
   private void saveConfig(FileConfiguration config, File file) {
      try {
         config.save(file);
      } catch (IOException var4) {
         this.plugin.getLogger().log(Level.SEVERE, "Could not save " + file.getName(), (Throwable)var4);
      }
   }

   @Override
   public CompletableFuture<PlayerData> loadPlayerData(UUID playerUuid) {
      return this.trackFuture(CompletableFuture.supplyAsync(() -> {
         File file = new File(this.playerDataFolder, playerUuid.toString() + ".yml");
         PlayerData data = new PlayerData(playerUuid);
         if (file.exists()) {
            ReadWriteLock rwLock = this.getFileLock(file);
            rwLock.readLock().lock();

            try {
               FileConfiguration config = YamlConfiguration.loadConfiguration(file);

               for (String s : config.getStringList("found-treasures")) {
                  try {
                     data.addFoundTreasure(UUID.fromString(s));
                  } catch (IllegalArgumentException var13) {
                  }
               }
            } finally {
               rwLock.readLock().unlock();
            }
         }

         return data;
      }));
   }

   @Override
   public CompletableFuture<Void> savePlayerData(PlayerData playerData) {
      return this.trackFuture(CompletableFuture.runAsync(() -> {
         File file = new File(this.playerDataFolder, playerData.getPlayerUuid().toString() + ".yml");
         YamlConfiguration config = new YamlConfiguration();
         ReadWriteLock rwLock = this.getFileLock(file);
         rwLock.writeLock().lock();
         boolean saved = false;

         try {
            if (file.exists()) {
               try {
                  config.load(file);
               } catch (Exception var12) {
                  this.plugin.getLogger().log(Level.WARNING, "Error loading existing player data for " + playerData.getPlayerUuid(), (Throwable)var12);
               }
            }

            List<String> found = new ArrayList<>();

            for (UUID uuid : playerData.getFoundTreasures()) {
               found.add(uuid.toString());
            }

            config.set("found-treasures", found);
            saved = this.saveConfigAtomicWithLock(config, file);
         } finally {
            rwLock.writeLock().unlock();
         }

         if (saved) {
            this.updateFinderIndex(playerData);
         } else {
            this.plugin.getLogger().warning("Failed to save player data for " + playerData.getPlayerUuid() + ", indexes not updated");
         }
      }));
   }

   @Override
   public CompletableFuture<Void> savePlayerDataBatch(List<PlayerData> playerDataList) {
      return this.trackFuture(CompletableFuture.runAsync(() -> {
         for (PlayerData pd : playerDataList) {
            File file = new File(this.playerDataFolder, pd.getPlayerUuid() + ".yml");
            YamlConfiguration config = new YamlConfiguration();
            ReadWriteLock rwLock = this.getFileLock(file);
            rwLock.writeLock().lock();
            boolean saved = false;

            try {
               if (file.exists()) {
                  try {
                     config.load(file);
                  } catch (Exception var14) {
                     this.plugin.getLogger().log(Level.WARNING, "Error loading existing player data for " + pd.getPlayerUuid(), (Throwable)var14);
                  }
               }

               config.set("FoundEggs", null);
               config.set("DeletionType", null);
               config.set("SelectedSection", null);
               List<String> found = new ArrayList<>();

               for (UUID uuid : pd.getFoundTreasures()) {
                  found.add(uuid.toString());
               }

               config.set("found-treasures", found);
               saved = this.saveConfigAtomicWithLock(config, file);
            } finally {
               rwLock.writeLock().unlock();
            }

            if (saved) {
               this.updateFinderIndex(pd);
            } else {
               this.plugin.getLogger().warning("Failed to save player data for " + pd.getPlayerUuid() + ", indexes not updated");
            }
         }
      }));
   }

   private void updateFinderIndex(PlayerData playerData) {
      UUID playerUuid = playerData.getPlayerUuid();

      for (UUID treasureId : playerData.getFoundTreasures()) {
         this.treasureToFindersIndex.computeIfAbsent(treasureId, k -> ConcurrentHashMap.newKeySet()).add(playerUuid);
      }

      this.finderIndexDirty = true;
   }

   @Override
   public CompletableFuture<List<Treasure>> loadTreasures() {
      return this.trackFuture(CompletableFuture.supplyAsync(() -> {
         List<Treasure> treasures = new ArrayList<>();
         File[] collectionDirs = this.collectionsFolder.listFiles(File::isDirectory);
         if (collectionDirs != null) {
            for (File collectionDir : collectionDirs) {
               File treasuresDir = new File(collectionDir, "treasures");
               if (treasuresDir.exists() && treasuresDir.isDirectory()) {
                  File[] files = treasuresDir.listFiles((dir, name) -> name.endsWith(".yml"));
                  if (files != null) {
                     for (File file : files) {
                        try {
                           String fileName = file.getName().replace(".yml", "");
                           UUID id = UUID.fromString(fileName);
                           FileConfiguration config = YamlConfiguration.loadConfiguration(file);
                           String collectionIdStr = config.getString("collection-id");
                           if (collectionIdStr != null) {
                              UUID collectionId = UUID.fromString(collectionIdStr);
                              String worldName = config.getString("world");
                              int x = config.getInt("x");
                              int y = config.getInt("y");
                              int z = config.getInt("z");
                              Location loc = new Location(Bukkit.getWorld(worldName), x, y, z);
                              List<Map<?, ?>> rewardList = config.getMapList("rewards");
                              List<Reward> rewards = this.deserializeRewards(rewardList);
                              String nbtData = config.getString("nbt-data");
                              String material = config.getString("material");
                              String blockState = config.getString("block-state");
                              treasures.add(new Treasure(id, collectionId, loc, rewards, nbtData, material, blockState));
                           }
                        } catch (Exception var28) {
                           this.plugin.getLogger().warning("Failed to load treasure " + file.getName() + ": " + var28.getMessage());
                        }
                     }
                  }
               }
            }
         }

         return treasures;
      }));
   }

   @Override
   public CompletableFuture<List<TreasureCore>> loadTreasureCores() {
      return this.trackFuture(CompletableFuture.supplyAsync(() -> {
         List<TreasureCore> cores = new ArrayList<>();
         File[] collectionDirs = this.collectionsFolder.listFiles(File::isDirectory);
         if (collectionDirs != null) {
            for (File collectionDir : collectionDirs) {
               File treasuresDir = new File(collectionDir, "treasures");
               if (treasuresDir.exists() && treasuresDir.isDirectory()) {
                  File[] files = treasuresDir.listFiles((dir, name) -> name.endsWith(".yml"));
                  if (files != null) {
                     for (File file : files) {
                        try {
                           String fileName = file.getName().replace(".yml", "");
                           UUID id = UUID.fromString(fileName);
                           FileConfiguration config = YamlConfiguration.loadConfiguration(file);
                           String collectionIdStr = config.getString("collection-id");
                           if (collectionIdStr != null) {
                              UUID collectionId = UUID.fromString(collectionIdStr);
                              String worldName = config.getString("world");
                              int x = config.getInt("x");
                              int y = config.getInt("y");
                              int z = config.getInt("z");
                              Location loc = new Location(Bukkit.getWorld(worldName), x, y, z);
                              String material = config.getString("material");
                              String blockState = config.getString("block-state");
                              cores.add(new TreasureCore(id, collectionId, loc, material, blockState));
                           }
                        } catch (Exception var25) {
                           this.plugin.getLogger().warning("Failed to load treasure core " + file.getName() + ": " + var25.getMessage());
                        }
                     }
                  }
               }
            }
         }

         return cores;
      }));
   }

   @Override
   public CompletableFuture<List<UUID>> getAllTreasureUUIDs() {
      return this.trackFuture(CompletableFuture.supplyAsync(() -> {
         List<UUID> ids = new ArrayList<>();
         File[] collectionDirs = this.collectionsFolder.listFiles(File::isDirectory);
         if (collectionDirs == null) {
            return ids;
         } else {
            for (File collectionDir : collectionDirs) {
               File treasuresDir = new File(collectionDir, "treasures");
               if (treasuresDir.exists() && treasuresDir.isDirectory()) {
                  File[] files = treasuresDir.listFiles((dir, name) -> name.endsWith(".yml"));
                  if (files != null) {
                     for (File file : files) {
                        try {
                           String fileName = file.getName();
                           String uuidStr = fileName.substring(0, fileName.length() - ".yml".length());
                           ids.add(UUID.fromString(uuidStr));
                        } catch (Exception var15) {
                        }
                     }
                  }
               }
            }

            return ids;
         }
      }));
   }

   @Override
   public CompletableFuture<Void> saveTreasure(Treasure treasure) {
      return this.trackFuture(CompletableFuture.runAsync(() -> {
         File collectionDir = new File(this.collectionsFolder, treasure.getCollectionId().toString());
         if (!collectionDir.exists()) {
            collectionDir.mkdirs();
         }

         File treasuresDir = new File(collectionDir, "treasures");
         if (!treasuresDir.exists()) {
            treasuresDir.mkdirs();
         }

         File file = new File(treasuresDir, treasure.getId().toString() + ".yml");
         YamlConfiguration config = new YamlConfiguration();
         config.set("collection-id", treasure.getCollectionId().toString());
         config.set("world", treasure.getLocation().getWorld().getName());
         config.set("x", treasure.getLocation().getBlockX());
         config.set("y", treasure.getLocation().getBlockY());
         config.set("z", treasure.getLocation().getBlockZ());
         config.set("rewards", this.serializeRewards(treasure.getRewards()));
         config.set("nbt-data", treasure.getNbtData());
         config.set("material", treasure.getMaterial());
         config.set("block-state", treasure.getBlockState());
         boolean saved = this.saveConfigAtomicWithLock(config, file);
         if (saved) {
            this.treasureToCollectionIndex.put(treasure.getId(), treasure.getCollectionId());
         } else {
            this.plugin.getLogger().warning("Failed to save treasure " + treasure.getId() + ", index not updated");
         }
      }));
   }

   @Override
   public CompletableFuture<Void> saveTreasuresBatch(List<Treasure> treasures) {
      return this.trackFuture(CompletableFuture.runAsync(() -> {
         if (treasures != null && !treasures.isEmpty()) {
            Map<UUID, File> treasuresDirByCollection = new HashMap<>();

            for (Treasure treasure : treasures) {
               File treasuresDir = treasuresDirByCollection.computeIfAbsent(treasure.getCollectionId(), collectionId -> {
                  File collectionDir = new File(this.collectionsFolder, collectionId.toString());
                  if (!collectionDir.exists()) {
                     collectionDir.mkdirs();
                  }

                  File dir = new File(collectionDir, "treasures");
                  if (!dir.exists()) {
                     dir.mkdirs();
                  }

                  return dir;
               });
               File file = new File(treasuresDir, treasure.getId().toString() + ".yml");
               YamlConfiguration config = new YamlConfiguration();
               config.set("collection-id", treasure.getCollectionId().toString());
               config.set("world", treasure.getLocation().getWorld().getName());
               config.set("x", treasure.getLocation().getBlockX());
               config.set("y", treasure.getLocation().getBlockY());
               config.set("z", treasure.getLocation().getBlockZ());
               config.set("rewards", this.serializeRewards(treasure.getRewards()));
               config.set("nbt-data", treasure.getNbtData());
               config.set("material", treasure.getMaterial());
               config.set("block-state", treasure.getBlockState());
               boolean saved = this.saveConfigAtomicWithLock(config, file);
               if (saved) {
                  this.treasureToCollectionIndex.put(treasure.getId(), treasure.getCollectionId());
               } else {
                  this.plugin.getLogger().warning("Failed to save treasure " + treasure.getId() + ", index not updated");
               }
            }
         }
      }));
   }

   @Override
   public CompletableFuture<Void> updateTreasureRewards(UUID treasureId, List<Reward> rewards) {
      return this.trackFuture(CompletableFuture.runAsync(() -> {
         File treasureFile = this.resolveTreasureFile(treasureId);
         if (treasureFile != null) {
            ReadWriteLock rwLock = this.getFileLock(treasureFile);
            rwLock.writeLock().lock();

            try {
               YamlConfiguration config = YamlConfiguration.loadConfiguration(treasureFile);
               config.set("rewards", this.serializeRewards(rewards));
               this.saveConfigAtomicWithLock(config, treasureFile);
            } finally {
               rwLock.writeLock().unlock();
            }
         }
      }));
   }

   @Override
   public CompletableFuture<Void> updateTreasureRewardsBatch(List<UUID> treasureIds, List<Reward> rewards) {
      return this.trackFuture(CompletableFuture.runAsync(() -> {
         if (treasureIds != null && !treasureIds.isEmpty()) {
            Object serializedRewards = this.serializeRewards(rewards);

            for (UUID treasureId : treasureIds) {
               File treasureFile = this.resolveTreasureFile(treasureId);
               if (treasureFile != null) {
                  ReadWriteLock rwLock = this.getFileLock(treasureFile);
                  rwLock.writeLock().lock();

                  try {
                     YamlConfiguration config = YamlConfiguration.loadConfiguration(treasureFile);
                     config.set("rewards", serializedRewards);
                     this.saveConfigAtomicWithLock(config, treasureFile);
                  } finally {
                     rwLock.writeLock().unlock();
                  }
               }
            }
         }
      }));
   }

   private File resolveTreasureFile(UUID treasureId) {
      UUID collectionId = this.treasureToCollectionIndex.get(treasureId);
      File treasureFile = null;
      if (collectionId != null) {
         File treasuresDir = new File(new File(this.collectionsFolder, collectionId.toString()), "treasures");
         treasureFile = new File(treasuresDir, treasureId.toString() + ".yml");
         if (!treasureFile.exists()) {
            treasureFile = null;
         }
      }

      if (treasureFile == null) {
         File[] collectionDirs = this.collectionsFolder.listFiles(File::isDirectory);
         if (collectionDirs != null) {
            for (File collectionDir : collectionDirs) {
               File treasuresDir = new File(collectionDir, "treasures");
               if (treasuresDir.exists()) {
                  File candidate = new File(treasuresDir, treasureId.toString() + ".yml");
                  if (candidate.exists()) {
                     treasureFile = candidate;
                     break;
                  }
               }
            }
         }
      }

      return treasureFile;
   }

   @Override
   public CompletableFuture<Void> deleteTreasure(UUID treasureId) {
      return this.trackFuture(CompletableFuture.runAsync(() -> {
         File[] collectionDirs = this.collectionsFolder.listFiles(File::isDirectory);
         if (collectionDirs != null) {
            for (File collectionDir : collectionDirs) {
               File treasuresDir = new File(collectionDir, "treasures");
               if (treasuresDir.exists()) {
                  File file = new File(treasuresDir, treasureId.toString() + ".yml");
                  if (file.exists()) {
                     file.delete();
                     break;
                  }
               }
            }
         }

         this.treasureToCollectionIndex.remove(treasureId);
         this.treasureToFindersIndex.remove(treasureId);
         this.finderIndexDirty = true;
      }));
   }

   @Override
   public CompletableFuture<Integer> deleteTreasuresInCollection(UUID collectionId) {
      return this.trackFuture(CompletableFuture.supplyAsync(() -> {
         int deleted = 0;
         File collectionDir = new File(this.collectionsFolder, collectionId.toString());
         File treasuresDir = new File(collectionDir, "treasures");
         if (treasuresDir.exists() && treasuresDir.isDirectory()) {
            File[] files = treasuresDir.listFiles((dir, name) -> name.endsWith(".yml"));
            if (files != null) {
               for (File file : files) {
                  try {
                     String fileName = file.getName();
                     if (fileName.endsWith(".yml")) {
                        UUID treasureId = UUID.fromString(fileName.substring(0, fileName.length() - ".yml".length()));
                        if (file.delete()) {
                           deleted++;
                        }

                        this.treasureToCollectionIndex.remove(treasureId);
                        this.treasureToFindersIndex.remove(treasureId);
                     }
                  } catch (Exception var12) {
                  }
               }
            }
         }

         this.treasureToCollectionIndex.entrySet().removeIf(e -> collectionId.equals(e.getValue()));
         this.finderIndexDirty = true;
         return deleted;
      }));
   }

   @Override
   public CompletableFuture<Treasure> loadTreasure(UUID treasureId) {
      return this.trackFuture(CompletableFuture.supplyAsync(() -> {
         UUID collectionId = this.treasureToCollectionIndex.get(treasureId);
         if (collectionId == null) {
            return null;
         } else {
            File collectionDir = new File(this.collectionsFolder, collectionId.toString());
            File treasuresDir = new File(collectionDir, "treasures");
            File file = new File(treasuresDir, treasureId.toString() + ".yml");
            if (!file.exists()) {
               return null;
            } else {
               try {
                  FileConfiguration config = YamlConfiguration.loadConfiguration(file);
                  String worldName = config.getString("world");
                  int x = config.getInt("x");
                  int y = config.getInt("y");
                  int z = config.getInt("z");
                  Location loc = new Location(Bukkit.getWorld(worldName), x, y, z);
                  List<Map<?, ?>> rewardList = config.getMapList("rewards");
                  List<Reward> rewards = this.deserializeRewards(rewardList);
                  String nbtData = config.getString("nbt-data");
                  String material = config.getString("material");
                  String blockState = config.getString("block-state");
                  return new Treasure(treasureId, collectionId, loc, rewards, nbtData, material, blockState);
               } catch (Exception var17) {
                  this.plugin.getLogger().warning("Failed to load treasure " + treasureId + ": " + var17.getMessage());
                  return null;
               }
            }
         }
      }));
   }

   @Override
   public CompletableFuture<List<Collection>> loadCollections() {
      return this.trackFuture(CompletableFuture.supplyAsync(() -> {
         List<Collection> collections = new ArrayList<>();
         File[] collectionDirs = this.collectionsFolder.listFiles(File::isDirectory);
         if (collectionDirs != null) {
            for (File dir : collectionDirs) {
               File configFile = new File(dir, "config.yml");
               if (configFile.exists()) {
                  try {
                     UUID id = UUID.fromString(dir.getName());
                     FileConfiguration config = YamlConfiguration.loadConfiguration(configFile);
                     String name = config.getString("name");
                     boolean enabled = config.getBoolean("enabled");
                     Collection c = new Collection(id, name, enabled);
                     c.setProgressResetCron(config.getString("progress-reset-cron"));
                     c.setSinglePlayerFind(config.getBoolean("single-player-find"));
                     c.setHideWhenNotAvailable(config.getBoolean("hide-when-not-available", config.getBoolean("hide-when-disabled", false)));
                     c.setHideAfterFound(config.getBoolean("hide-after-found", false));
                     String defaultPresetId = config.getString("default-treasure-reward-preset-id");
                     if (defaultPresetId != null && !defaultPresetId.trim().isEmpty()) {
                        try {
                           c.setDefaultTreasureRewardPresetId(UUID.fromString(defaultPresetId));
                        } catch (IllegalArgumentException var23) {
                           c.setDefaultTreasureRewardPresetId(null);
                        }
                     }

                     List<Map<?, ?>> rulesList = config.getMapList("act-rules");
                     List<ActRule> actRules = new ArrayList<>();

                     for (Map<?, ?> ruleMap : rulesList) {
                        try {
                           UUID ruleId = UUID.fromString((String)ruleMap.get("id"));
                           String ruleName = (String)ruleMap.get("name");
                           ActRule rule = new ActRule(ruleId, id, ruleName);
                           rule.setDateRange((String)ruleMap.get("date-range"));
                           rule.setDuration((String)ruleMap.get("duration"));
                           rule.setCronExpression((String)ruleMap.get("cron"));
                           Object enabledObj = ruleMap.get("enabled");
                           rule.setEnabled(enabledObj != null ? (Boolean)enabledObj : true);
                           actRules.add(rule);
                        } catch (Exception var22) {
                           this.plugin.getLogger().warning("Failed to load ACT rule: " + var22.getMessage());
                        }
                     }

                     c.setActRules(actRules);
                     List<Map<?, ?>> rewardList = config.getMapList("rewards");
                     c.setCompletionRewards(this.deserializeRewards(rewardList));
                     collections.add(c);
                  } catch (Exception var24) {
                     this.plugin.getLogger().warning("Failed to load collection from " + dir.getName() + ": " + var24.getMessage());
                  }
               }
            }
         }

         return collections;
      }));
   }

   @Override
   public CompletableFuture<Void> saveCollection(Collection collection) {
      return this.trackFuture(
         CompletableFuture.runAsync(
            () -> {
               File dir = new File(this.collectionsFolder, collection.getId().toString());
               if (!dir.exists()) {
                  dir.mkdirs();
               }

               File file = new File(dir, "config.yml");
               ReadWriteLock rwLock = this.getFileLock(file);
               rwLock.writeLock().lock();

               try {
                  YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
                  config.set("name", collection.getName());
                  config.set("enabled", collection.isEnabled());
                  config.set("progress-reset-cron", collection.getProgressResetCron());
                  config.set("single-player-find", collection.isSinglePlayerFind());
                  config.set("hide-when-not-available", collection.isHideWhenNotAvailable());
                  config.set("hide-after-found", collection.isHideAfterFound());
                  config.set(
                     "default-treasure-reward-preset-id",
                     collection.getDefaultTreasureRewardPresetId() != null ? collection.getDefaultTreasureRewardPresetId().toString() : null
                  );
                  List<Map<String, Object>> rulesList = new ArrayList<>();

                  for (ActRule rule : collection.getActRules()) {
                     Map<String, Object> ruleMap = new HashMap<>();
                     ruleMap.put("id", rule.getId().toString());
                     ruleMap.put("name", rule.getName());
                     ruleMap.put("date-range", rule.getDateRange());
                     ruleMap.put("duration", rule.getDuration());
                     ruleMap.put("cron", rule.getCronExpression());
                     ruleMap.put("enabled", rule.isEnabled());
                     rulesList.add(ruleMap);
                  }

                  config.set("act-rules", rulesList);
                  config.set("rewards", this.serializeRewards(collection.getCompletionRewards()));
                  this.saveConfigAtomicWithLock(config, file);
               } finally {
                  rwLock.writeLock().unlock();
               }
            }
         )
      );
   }

   @Override
   public CompletableFuture<List<RewardPreset>> loadRewardPresets(RewardPresetType type) {
      return this.trackFuture(CompletableFuture.supplyAsync(() -> {
         List<RewardPreset> presets = new ArrayList<>();
         File folder = this.getPresetFolder(type);
         if (folder != null && folder.exists() && folder.isDirectory()) {
            File[] files = folder.listFiles((dir, namex) -> namex.endsWith(".yml"));
            if (files != null) {
               for (File file : files) {
                  String idStr = file.getName().replace(".yml", "");

                  try {
                     UUID id = UUID.fromString(idStr);
                     FileConfiguration config = YamlConfiguration.loadConfiguration(file);
                     String name = config.getString("name");
                     List<Map<?, ?>> rewardList = config.getMapList("rewards");
                     List<Reward> rewards = this.deserializeRewards(rewardList);
                     if (name != null && !name.trim().isEmpty()) {
                        presets.add(new RewardPreset(id, type, name, rewards));
                     }
                  } catch (Exception var16) {
                     this.plugin.getLogger().warning("Failed to load reward preset file " + file.getName() + ": " + var16.getMessage());
                  }
               }
            }
         }

         if (presets.isEmpty() && this.legacyRewardPresetsFile != null && this.legacyRewardPresetsFile.exists()) {
            FileConfiguration legacy = YamlConfiguration.loadConfiguration(this.legacyRewardPresetsFile);
            String rootKey = type == RewardPresetType.COLLECTION ? "collection" : "treasure";
            ConfigurationSection section = legacy.getConfigurationSection(rootKey);
            if (section != null) {
               for (String idStr : section.getKeys(false)) {
                  try {
                     UUID id = UUID.fromString(idStr);
                     String name = section.getString(idStr + ".name");
                     List<Map<?, ?>> rewardList = section.getMapList(idStr + ".rewards");
                     List<Reward> rewards = this.deserializeRewards(rewardList);
                     if (name != null && !name.trim().isEmpty()) {
                        presets.add(new RewardPreset(id, type, name, rewards));
                     }
                  } catch (Exception var15) {
                     this.plugin.getLogger().warning("Failed to load legacy reward preset " + idStr + ": " + var15.getMessage());
                  }
               }
            }
         }

         presets.sort(Comparator.comparing(RewardPreset::getName, String.CASE_INSENSITIVE_ORDER));
         return presets;
      }));
   }

   @Override
   public CompletableFuture<Void> saveRewardPreset(RewardPreset preset) {
      return this.trackFuture(CompletableFuture.runAsync(() -> {
         File file = this.getPresetFile(preset.getType(), preset.getId());
         YamlConfiguration config = new YamlConfiguration();
         config.set("name", preset.getName());
         config.set("rewards", this.serializeRewards(preset.getRewards()));
         this.saveConfigAtomicWithLock(config, file);
      }));
   }

   @Override
   public CompletableFuture<Void> saveRewardPresetsBatch(List<RewardPreset> presets) {
      return this.trackFuture(CompletableFuture.runAsync(() -> {
         if (presets != null && !presets.isEmpty()) {
            for (RewardPreset preset : presets) {
               File file = this.getPresetFile(preset.getType(), preset.getId());
               YamlConfiguration config = new YamlConfiguration();
               config.set("name", preset.getName());
               config.set("rewards", this.serializeRewards(preset.getRewards()));
               this.saveConfigAtomicWithLock(config, file);
            }
         }
      }));
   }

   @Override
   public CompletableFuture<Void> deleteRewardPreset(RewardPresetType type, UUID presetId) {
      return this.trackFuture(CompletableFuture.runAsync(() -> {
         File file = this.getPresetFile(type, presetId);
         if (file.exists()) {
            file.delete();
         }
      }));
   }

   @Override
   public CompletableFuture<List<PlaceItem>> loadPlaceItems() {
      return this.trackFuture(
         CompletableFuture.supplyAsync(
            () -> {
               List<PlaceItem> presets = new ArrayList<>();
               if (this.placeItemsFolder == null) {
                  this.placeItemsFolder = new File(this.plugin.getDataFolder(), "place-items");
               }

               if (this.placeItemsFolder.exists() && this.placeItemsFolder.isDirectory()) {
                  File[] groupDirs = this.placeItemsFolder.listFiles(File::isDirectory);
                  if (groupDirs == null) {
                     return presets;
                  } else {
                     for (File groupDir : groupDirs) {
                        File[] files = groupDir.listFiles((dir, namex) -> namex.endsWith(".yml"));
                        if (files != null) {
                           for (File file : files) {
                              String idStr = file.getName().replace(".yml", "");

                              try {
                                 UUID id = UUID.fromString(idStr);
                                 FileConfiguration config = YamlConfiguration.loadConfiguration(file);
                                 String name = config.getString("name");
                                 String group = config.getString("group");
                                 if (group == null || group.trim().isEmpty()) {
                                    group = groupDir.getName();
                                 }

                                 String itemData = config.getString("item");
                                 if (name != null && !name.trim().isEmpty() && itemData != null && !itemData.trim().isEmpty()) {
                                    presets.add(new PlaceItem(id, group, name, itemData));
                                 }
                              } catch (Exception var18) {
                                 this.plugin.getLogger().warning("Failed to load place items file " + file.getName() + ": " + var18.getMessage());
                              }
                           }
                        }
                     }

                     presets.sort(
                        Comparator.comparing(PlaceItem::getGroup, String.CASE_INSENSITIVE_ORDER)
                           .thenComparing(PlaceItem::getName, String.CASE_INSENSITIVE_ORDER)
                     );
                     return presets;
                  }
               } else {
                  return presets;
               }
            }
         )
      );
   }

   @Override
   public CompletableFuture<Void> savePlaceItem(PlaceItem preset) {
      return this.trackFuture(CompletableFuture.runAsync(() -> {
         if (this.placeItemsFolder == null) {
            this.placeItemsFolder = new File(this.plugin.getDataFolder(), "place-items");
         }

         File groupDir = new File(this.placeItemsFolder, sanitizeGroupFolderName(preset.getGroup()));
         if (!groupDir.exists()) {
            groupDir.mkdirs();
         }

         File file = new File(groupDir, preset.getId().toString() + ".yml");
         YamlConfiguration config = new YamlConfiguration();
         config.set("group", preset.getGroup());
         config.set("name", preset.getName());
         config.set("item", preset.getItemData());
         this.saveConfigAtomicWithLock(config, file);
      }));
   }

   @Override
   public CompletableFuture<Void> deletePlaceItem(UUID presetId) {
      return this.trackFuture(CompletableFuture.runAsync(() -> {
         if (this.placeItemsFolder == null) {
            this.placeItemsFolder = new File(this.plugin.getDataFolder(), "place-items");
         }

         if (this.placeItemsFolder.exists() && this.placeItemsFolder.isDirectory()) {
            File[] groupDirs = this.placeItemsFolder.listFiles(File::isDirectory);
            if (groupDirs != null) {
               String filename = presetId.toString() + ".yml";

               for (File groupDir : groupDirs) {
                  File file = new File(groupDir, filename);
                  if (file.exists()) {
                     file.delete();
                     break;
                  }
               }
            }
         }
      }));
   }

   @Override
   public CompletableFuture<Set<String>> loadPlaceItemGroups() {
      return this.trackFuture(CompletableFuture.supplyAsync(() -> {
         if (this.placePresetGroupsFile == null) {
            this.placePresetGroupsFile = new File(this.plugin.getDataFolder(), "place-item-groups.yml");
         }

         if (!this.placePresetGroupsFile.exists()) {
            return Collections.emptySet();
         } else {
            FileConfiguration config = YamlConfiguration.loadConfiguration(this.placePresetGroupsFile);
            List<String> groups = config.getStringList("groups");
            if (groups != null && !groups.isEmpty()) {
               LinkedHashSet<String> result = new LinkedHashSet<>();

               for (String group : groups) {
                  if (group != null && !group.trim().isEmpty()) {
                     result.add(group.trim());
                  }
               }

               return result;
            } else {
               return Collections.emptySet();
            }
         }
      }));
   }

   @Override
   public CompletableFuture<Void> createPlaceItemGroup(String group) {
      return this.trackFuture(CompletableFuture.runAsync(() -> {
         if (group != null && !group.trim().isEmpty()) {
            if (this.placePresetGroupsFile == null) {
               this.placePresetGroupsFile = new File(this.plugin.getDataFolder(), "place-item-groups.yml");
            }

            ReadWriteLock rwLock = this.getFileLock(this.placePresetGroupsFile);
            rwLock.writeLock().lock();

            try {
               YamlConfiguration config = YamlConfiguration.loadConfiguration(this.placePresetGroupsFile);
               List<String> groups = new ArrayList<>(config.getStringList("groups"));
               String trimmed = group.trim();
               boolean exists = groups.stream().anyMatch(g -> g != null && g.equalsIgnoreCase(trimmed));
               if (!exists) {
                  groups.add(trimmed);
                  config.set("groups", groups);
                  this.saveConfigAtomicWithLock(config, this.placePresetGroupsFile);
               }
            } finally {
               rwLock.writeLock().unlock();
            }
         }
      }));
   }

   @Override
   public CompletableFuture<Void> renamePlaceItemGroup(String oldGroup, String newGroup) {
      return this.trackFuture(CompletableFuture.runAsync(() -> {
         if (oldGroup != null && !oldGroup.trim().isEmpty() && newGroup != null && !newGroup.trim().isEmpty()) {
            if (this.placePresetGroupsFile == null) {
               this.placePresetGroupsFile = new File(this.plugin.getDataFolder(), "place-item-groups.yml");
            }

            String oldTrimmed = oldGroup.trim();
            String newTrimmed = newGroup.trim();
            ReadWriteLock rwLock = this.getFileLock(this.placePresetGroupsFile);
            rwLock.writeLock().lock();

            try {
               YamlConfiguration config = YamlConfiguration.loadConfiguration(this.placePresetGroupsFile);
               List<String> groups = new ArrayList<>(config.getStringList("groups"));
               boolean changed = false;

               for (int i = 0; i < groups.size(); i++) {
                  String g = groups.get(i);
                  if (g != null && g.equalsIgnoreCase(oldTrimmed)) {
                     groups.set(i, newTrimmed);
                     changed = true;
                     break;
                  }
               }

               if (changed) {
                  config.set("groups", groups);
                  this.saveConfigAtomicWithLock(config, this.placePresetGroupsFile);
               }
            } finally {
               rwLock.writeLock().unlock();
            }

            if (this.placeItemsFolder == null) {
               this.placeItemsFolder = new File(this.plugin.getDataFolder(), "place-items");
            }

            File oldDir = new File(this.placeItemsFolder, sanitizeGroupFolderName(oldTrimmed));
            File newDir = new File(this.placeItemsFolder, sanitizeGroupFolderName(newTrimmed));
            if (oldDir.exists() && oldDir.isDirectory() && !oldDir.equals(newDir)) {
               if (!newDir.exists()) {
                  newDir.mkdirs();
               }

               File[] files = oldDir.listFiles((dir, name) -> name.endsWith(".yml"));
               if (files != null) {
                  for (File file : files) {
                     try {
                        Files.move(file.toPath(), new File(newDir, file.getName()).toPath(), StandardCopyOption.REPLACE_EXISTING);
                     } catch (Exception var16) {
                     }
                  }
               }

               if (this.isPresetFolderEmpty(oldDir)) {
                  oldDir.delete();
               }
            }
         }
      }));
   }

   @Override
   public CompletableFuture<Void> deletePlaceItemGroup(String group) {
      return this.trackFuture(CompletableFuture.runAsync(() -> {
         if (group != null && !group.trim().isEmpty()) {
            if (this.placePresetGroupsFile == null) {
               this.placePresetGroupsFile = new File(this.plugin.getDataFolder(), "place-item-groups.yml");
            }

            String trimmed = group.trim();
            ReadWriteLock rwLock = this.getFileLock(this.placePresetGroupsFile);
            rwLock.writeLock().lock();

            try {
               YamlConfiguration config = YamlConfiguration.loadConfiguration(this.placePresetGroupsFile);
               List<String> groups = new ArrayList<>(config.getStringList("groups"));
               boolean removed = groups.removeIf(g -> g != null && g.equalsIgnoreCase(trimmed));
               if (removed) {
                  config.set("groups", groups);
                  this.saveConfigAtomicWithLock(config, this.placePresetGroupsFile);
               }
            } finally {
               rwLock.writeLock().unlock();
            }

            if (this.placeItemsFolder == null) {
               this.placeItemsFolder = new File(this.plugin.getDataFolder(), "place-items");
            }

            File dir = new File(this.placeItemsFolder, sanitizeGroupFolderName(trimmed));
            if (dir.exists() && dir.isDirectory()) {
               this.deleteDirectoryRecursively(dir);
            }
         }
      }));
   }

   @Override
   public CompletableFuture<Void> deleteCollection(UUID id) {
      return this.trackFuture(CompletableFuture.runAsync(() -> {
         List<String> treasureIds = this.getTreasureIdsForCollection(id);
         File dir = new File(this.collectionsFolder, id.toString());
         if (dir.exists()) {
            this.deleteDirectoryRecursively(dir);
         }

         for (String tidStr : treasureIds) {
            try {
               UUID tid = UUID.fromString(tidStr);
               this.treasureToCollectionIndex.remove(tid);
               this.treasureToFindersIndex.remove(tid);
            } catch (IllegalArgumentException var7) {
            }
         }

         if (!treasureIds.isEmpty()) {
            this.finderIndexDirty = true;
         }
      }));
   }

   private void deleteDirectoryRecursively(File file) {
      if (file.isDirectory()) {
         File[] entries = file.listFiles();
         if (entries != null) {
            for (File entry : entries) {
               this.deleteDirectoryRecursively(entry);
            }
         }
      }

      file.delete();
   }

   @Override
   public CompletableFuture<Boolean> renameCollection(UUID id, String newName) {
      return this.trackFuture(CompletableFuture.supplyAsync(() -> {
         File dir = new File(this.collectionsFolder, id.toString());
         File file = new File(dir, "config.yml");
         if (!file.exists()) {
            return false;
         } else {
            File[] collectionDirs = this.collectionsFolder.listFiles(File::isDirectory);
            if (collectionDirs != null) {
               for (File d : collectionDirs) {
                  if (!d.getName().equals(id.toString())) {
                     File configFile = new File(d, "config.yml");
                     if (configFile.exists()) {
                        FileConfiguration cfg = YamlConfiguration.loadConfiguration(configFile);
                        if (newName.equals(cfg.getString("name"))) {
                           return false;
                        }
                     }
                  }
               }
            }

            ReadWriteLock rwLock = this.getFileLock(file);
            rwLock.writeLock().lock();

            Boolean var17;
            try {
               YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
               config.set("name", newName);
               var17 = this.saveConfigAtomicWithLock(config, file);
            } finally {
               rwLock.writeLock().unlock();
            }

            return var17;
         }
      }));
   }

   @Override
   public CompletableFuture<List<PlayerData>> loadAllPlayerData() {
      return this.trackFuture(CompletableFuture.supplyAsync(() -> {
         List<PlayerData> allData = new ArrayList<>();
         if (this.playerDataFolder.exists()) {
            File[] files = this.playerDataFolder.listFiles((dir, name) -> name.endsWith(".yml"));
            if (files != null) {
               for (File file : files) {
                  String uuidStr = file.getName().replace(".yml", "");

                  try {
                     UUID uuid = UUID.fromString(uuidStr);
                     FileConfiguration config = YamlConfiguration.loadConfiguration(file);
                     PlayerData pd = new PlayerData(uuid);

                     for (String s : config.getStringList("found-treasures")) {
                        try {
                           pd.addFoundTreasure(UUID.fromString(s));
                        } catch (IllegalArgumentException var15) {
                        }
                     }

                     allData.add(pd);
                  } catch (IllegalArgumentException var16) {
                     this.plugin.getLogger().warning("Invalid UUID in filename: " + file.getName());
                  }
               }
            }
         }

         return allData;
      }));
   }

   @Override
   public CompletableFuture<Integer> resetAllProgress() {
      return this.trackFuture(CompletableFuture.supplyAsync(() -> {
         int count = 0;
         if (this.playerDataFolder.exists()) {
            File[] files = this.playerDataFolder.listFiles((dir, name) -> name.endsWith(".yml"));
            if (files != null) {
               for (File file : files) {
                  FileConfiguration config = YamlConfiguration.loadConfiguration(file);
                  count += config.getStringList("found-treasures").size();
                  file.delete();
               }
            }
         }

         this.treasureToFindersIndex.clear();
         this.finderIndexDirty = true;
         return count;
      }));
   }

   @Override
   public CompletableFuture<Integer> resetCollectionProgress(UUID collectionId) {
      return this.trackFuture(CompletableFuture.supplyAsync(() -> {
         List<String> collectionTreasureIds = this.getTreasureIdsForCollection(collectionId);
         int count = 0;
         if (this.playerDataFolder.exists()) {
            File[] files = this.playerDataFolder.listFiles((dir, name) -> name.endsWith(".yml"));
            if (files != null) {
               for (File file : files) {
                  ReadWriteLock rwLock = this.getFileLock(file);
                  rwLock.writeLock().lock();

                  try {
                     YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
                     List<String> found = new ArrayList<>(config.getStringList("found-treasures"));
                     int sizeBefore = found.size();
                     found.removeAll(collectionTreasureIds);
                     int removed = sizeBefore - found.size();
                     if (removed > 0) {
                        count += removed;
                        config.set("found-treasures", found);
                        this.saveConfigAtomicWithLock(config, file);
                     }
                  } finally {
                     rwLock.writeLock().unlock();
                  }
               }
            }
         }

         for (String treasureIdStr : collectionTreasureIds) {
            try {
               UUID treasureId = UUID.fromString(treasureIdStr);
               this.treasureToFindersIndex.remove(treasureId);
            } catch (IllegalArgumentException var17) {
            }
         }

         this.finderIndexDirty = true;
         return count;
      }));
   }

   @Override
   public CompletableFuture<Integer> resetPlayerProgress(UUID playerUuid) {
      return this.trackFuture(CompletableFuture.supplyAsync(() -> {
         File file = new File(this.playerDataFolder, playerUuid.toString() + ".yml");
         if (!file.exists()) {
            return 0;
         } else {
            FileConfiguration config = YamlConfiguration.loadConfiguration(file);
            List<String> found = config.getStringList("found-treasures");
            int count = found.size();
            if (count > 0) {
               for (String treasureIdStr : found) {
                  try {
                     UUID treasureId = UUID.fromString(treasureIdStr);
                     Set<UUID> finders = this.treasureToFindersIndex.get(treasureId);
                     if (finders != null) {
                        finders.remove(playerUuid);
                     }
                  } catch (IllegalArgumentException var10) {
                  }
               }

               this.finderIndexDirty = true;
               file.delete();
            }

            return count;
         }
      }));
   }

   @Override
   public CompletableFuture<Integer> resetPlayerCollectionProgress(UUID playerUuid, UUID collectionId) {
      return this.trackFuture(CompletableFuture.supplyAsync(() -> {
         File file = new File(this.playerDataFolder, playerUuid.toString() + ".yml");
         if (!file.exists()) {
            return 0;
         } else {
            List<String> collectionTreasureIds = this.getTreasureIdsForCollection(collectionId);
            ReadWriteLock rwLock = this.getFileLock(file);
            rwLock.writeLock().lock();

            Integer var24;
            try {
               YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
               List<String> found = new ArrayList<>(config.getStringList("found-treasures"));
               int sizeBefore = found.size();
               List<String> removedTreasures = new ArrayList<>();

               for (String tid : found) {
                  if (collectionTreasureIds.contains(tid)) {
                     removedTreasures.add(tid);
                  }
               }

               found.removeAll(collectionTreasureIds);
               int removed = sizeBefore - found.size();
               if (removed <= 0) {
                  return 0;
               }

               config.set("found-treasures", found);
               boolean saved = this.saveConfigAtomicWithLock(config, file);
               if (saved) {
                  for (String treasureIdStr : removedTreasures) {
                     try {
                        UUID treasureId = UUID.fromString(treasureIdStr);
                        Set<UUID> finders = this.treasureToFindersIndex.get(treasureId);
                        if (finders != null) {
                           finders.remove(playerUuid);
                        }
                     } catch (IllegalArgumentException var19) {
                     }
                  }

                  this.finderIndexDirty = true;
               }

               var24 = removed;
            } finally {
               rwLock.writeLock().unlock();
            }

            return var24;
         }
      }));
   }

   @Override
   public CompletableFuture<Map<UUID, Integer>> getLeaderboard(UUID collectionId, int limit) {
      return this.trackFuture(
         CompletableFuture.supplyAsync(
            () -> {
               Map<UUID, Integer> scores = new HashMap<>();
               FileConfiguration config = YamlConfiguration.loadConfiguration(this.leaderboardFile);
               String path = "collections." + collectionId;
               if (config.contains(path) && config.getConfigurationSection(path) != null) {
                  for (String uuidStr : config.getConfigurationSection(path).getKeys(false)) {
                     int score = config.getInt(path + "." + uuidStr);
                     scores.put(UUID.fromString(uuidStr), score);
                  }
               }

               return scores.entrySet()
                  .stream()
                  .sorted(Entry.<UUID, Integer>comparingByValue().reversed())
                  .limit(limit)
                  .collect(Collectors.toMap(Entry::getKey, Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
            }
         )
      );
   }

   public void updatePlayerLeaderboard(PlayerData playerData) {
      if (this.leaderboardFile != null) {
         ReadWriteLock rwLock = this.getFileLock(this.leaderboardFile);
         rwLock.writeLock().lock();

         try {
            YamlConfiguration config = YamlConfiguration.loadConfiguration(this.leaderboardFile);

            for (UUID collectionId : new HashSet<>(this.treasureToCollectionIndex.values())) {
               int count = 0;

               for (UUID foundId : playerData.getFoundTreasures()) {
                  if (collectionId.equals(this.treasureToCollectionIndex.get(foundId))) {
                     count++;
                  }
               }

               String path = "collections." + collectionId + "." + playerData.getPlayerUuid();
               if (count > 0) {
                  config.set(path, count);
               } else {
                  config.set(path, null);
               }
            }

            this.saveConfigAtomicWithLock(config, this.leaderboardFile);
         } finally {
            rwLock.writeLock().unlock();
         }
      }
   }

   public void flushFinderIndex() {
      if (this.finderIndexDirty) {
         this.saveFinderIndex();
      }
   }

   private List<String> getTreasureIdsForCollection(UUID collectionId) {
      List<String> ids = new ArrayList<>();
      File collectionDir = new File(this.collectionsFolder, collectionId.toString());
      File treasuresDir = new File(collectionDir, "treasures");
      if (treasuresDir.exists() && treasuresDir.isDirectory()) {
         File[] files = treasuresDir.listFiles((dir, name) -> name.endsWith(".yml"));
         if (files != null) {
            for (File file : files) {
               ids.add(file.getName().replace(".yml", ""));
            }
         }
      }

      return ids;
   }

   @Override
   public CompletableFuture<List<UUID>> getPlayersWhoFound(UUID treasureId) {
      return this.trackFuture(CompletableFuture.supplyAsync(() -> {
         Set<UUID> finders = this.treasureToFindersIndex.get(treasureId);
         return finders == null ? new ArrayList<>() : new ArrayList<>(finders);
      }));
   }

   @Override
   public CompletableFuture<Integer> getFoundPlayerCount(UUID treasureId) {
      return this.trackFuture(CompletableFuture.supplyAsync(() -> {
         Set<UUID> finders = this.treasureToFindersIndex.get(treasureId);
         return finders == null ? 0 : finders.size();
      }));
   }

   @Override
   public CompletableFuture<Set<UUID>> getPlayerFoundInCollection(UUID playerUuid, UUID collectionId) {
      return this.trackFuture(CompletableFuture.supplyAsync(() -> {
         Set<UUID> result = new HashSet<>();
         File file = new File(this.playerDataFolder, playerUuid.toString() + ".yml");
         if (!file.exists()) {
            return result;
         } else {
            FileConfiguration config = YamlConfiguration.loadConfiguration(file);

            for (String treasureIdStr : config.getStringList("found-treasures")) {
               try {
                  UUID treasureId = UUID.fromString(treasureIdStr);
                  UUID treasureCollection = this.treasureToCollectionIndex.get(treasureId);
                  if (collectionId.equals(treasureCollection)) {
                     result.add(treasureId);
                  }
               } catch (IllegalArgumentException var11) {
               }
            }

            return result;
         }
      }));
   }

   private List<Reward> deserializeRewards(List<Map<?, ?>> list) {
      List<Reward> rewards = new ArrayList<>();
      if (list == null) {
         return rewards;
      } else {
         for (Map<?, ?> map : list) {
            try {
               RewardType type = RewardType.valueOf((String)map.get("type"));
               Object chanceObj = map.get("chance");
               double chance = 0.0;
               if (chanceObj instanceof Number) {
                  chance = ((Number)chanceObj).doubleValue();
               }

               String message = (String)map.get("message");
               String broadcast = (String)map.get("broadcast");
               String value = (String)map.get("value");
               rewards.add(new Reward(type, chance, message, broadcast, value));
            } catch (Exception var12) {
               this.plugin.getLogger().warning("Error deserializing reward: " + var12.getMessage());
            }
         }

         return rewards;
      }
   }

   private List<Map<String, Object>> serializeRewards(List<Reward> rewards) {
      List<Map<String, Object>> list = new ArrayList<>();
      if (rewards == null) {
         return list;
      } else {
         for (Reward r : rewards) {
            Map<String, Object> map = new HashMap<>();
            map.put("type", r.getType().name());
            map.put("chance", r.getChance());
            map.put("message", r.getMessage());
            map.put("broadcast", r.getBroadcast());
            map.put("value", r.getValue());
            list.add(map);
         }

         return list;
      }
   }

   @Override
   public void flushIndexes() {
      this.flushFinderIndex();
   }

   @Override
   public void rebuildLeaderboardCache() {
      CompletableFuture.runAsync(() -> {
         if (((Main)this.plugin).isDebugMode()) {
            this.plugin.getLogger().info("Rebuilding YAML leaderboard cache...");
         }

         FileConfiguration config = new YamlConfiguration();
         new HashSet<>(this.treasureToCollectionIndex.values());
         if (this.playerDataFolder.exists()) {
            File[] files = this.playerDataFolder.listFiles((dir, name) -> name.endsWith(".yml"));
            if (files != null) {
               for (File file : files) {
                  try {
                     UUID playerUuid = UUID.fromString(file.getName().replace(".yml", ""));
                     FileConfiguration playerConfig = YamlConfiguration.loadConfiguration(file);
                     List<String> found = playerConfig.getStringList("found-treasures");
                     Map<UUID, Integer> counts = new HashMap<>();

                     for (String treasureIdStr : found) {
                        try {
                           UUID treasureId = UUID.fromString(treasureIdStr);
                           UUID collectionId = this.treasureToCollectionIndex.get(treasureId);
                           if (collectionId != null) {
                              counts.merge(collectionId, 1, Integer::sum);
                           }
                        } catch (IllegalArgumentException var16) {
                        }
                     }

                     for (Entry<UUID, Integer> entry : counts.entrySet()) {
                        config.set("collections." + entry.getKey() + "." + playerUuid, entry.getValue());
                     }
                  } catch (IllegalArgumentException var17) {
                  }
               }
            }
         }

         this.saveConfigAtomic(config, this.leaderboardFile);
         if (((Main)this.plugin).isDebugMode()) {
            this.plugin.getLogger().info("YAML leaderboard cache rebuilt successfully");
         }
      });
   }

   public void startPeriodicFlush(int intervalSeconds) {
      int safeIntervalSeconds = Math.max(1, intervalSeconds);
      long intervalTicks = 20L * safeIntervalSeconds;
      if (this.flushTask != null && !this.flushTask.isCancelled()) {
         this.flushTask.cancel();
      }

      this.flushTask = Bukkit.getScheduler().runTaskTimerAsynchronously(this.plugin, () -> {
         if (this.finderIndexDirty) {
            this.saveFinderIndex();
            this.plugin.getLogger().fine("Flushed finder index to disk");
         }

         this.cleanupOrphanedLocks();
      }, intervalTicks, intervalTicks);
   }

   public int getActiveLockCount() {
      return this.fileLocks.size();
   }

   public int getPendingOperationCount() {
      return this.pendingOperations.size();
   }
}
