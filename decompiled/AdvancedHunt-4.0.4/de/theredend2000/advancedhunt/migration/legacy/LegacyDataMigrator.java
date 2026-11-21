package de.theredend2000.advancedhunt.migration.legacy;

import de.theredend2000.advancedhunt.data.DataRepository;
import de.theredend2000.advancedhunt.managers.CollectionManager;
import de.theredend2000.advancedhunt.model.Collection;
import de.theredend2000.advancedhunt.model.PlaceItem;
import de.theredend2000.advancedhunt.model.PlayerData;
import de.theredend2000.advancedhunt.model.RewardPreset;
import de.theredend2000.advancedhunt.model.Treasure;
import de.theredend2000.advancedhunt.util.ZipBackupUtil;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.Map.Entry;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.logging.Level;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.plugin.Plugin;

public final class LegacyDataMigrator {
   private static final String MISSING_SNAPSHOT_FALLBACK_MATERIAL = "STONE";
   private final Plugin plugin;
   private final DataRepository repository;
   private final LegacyMigrationConfig config;

   public LegacyDataMigrator(Plugin plugin, DataRepository repository, LegacyMigrationConfig config) {
      this.plugin = plugin;
      this.repository = repository;
      this.config = config;
   }

   public CompletableFuture<Void> forceCleanup() {
      return CompletableFuture.runAsync(() -> {
         File legacyRoot = this.config.sourceFolder();
         if (legacyRoot != null && legacyRoot.exists() && legacyRoot.isDirectory()) {
            this.plugin.getLogger().info("Running forced legacy cleanup on: " + legacyRoot.getAbsolutePath());
            this.cleanupLegacyFiles(legacyRoot);
         } else {
            this.plugin.getLogger().warning("Cannot cleanup: Legacy source folder not found: " + legacyRoot);
         }
      });
   }

   public CompletableFuture<LegacyMigratorResult> run() {
      if (!this.config.enabled()) {
         return CompletableFuture.completedFuture(new LegacyMigratorResult(0, 0, 0, 0, 0, 0, 0, null));
      } else {
         File marker = new File(this.plugin.getDataFolder(), this.config.markerFileName());
         if (marker.exists()) {
            this.plugin.getLogger().info("Legacy migration marker exists (" + marker.getName() + ") - skipping.");
            this.plugin.getLogger().info("Re-running legacy cleanup to ensure all files are removed...");
            File legacyRoot = this.config.sourceFolder();
            if (legacyRoot != null && legacyRoot.exists() && legacyRoot.isDirectory()) {
               this.cleanupLegacyFiles(legacyRoot);
            }

            return CompletableFuture.completedFuture(new LegacyMigratorResult(0, 0, 0, 0, 0, 0, 0, null));
         } else {
            File legacyRoot = this.config.sourceFolder();
            return legacyRoot != null && legacyRoot.exists() && legacyRoot.isDirectory()
               ? this.doMigration(marker, legacyRoot)
               : this.failed("Legacy migration source folder not found: " + legacyRoot);
         }
      }
   }

   private CompletableFuture<LegacyMigratorResult> doMigration(File marker, File legacyRoot) {
      File backupsDir = new File(this.plugin.getDataFolder(), "backups");
      File targetBackup = null;
      if (this.config.createBackups()) {
         try {
            targetBackup = ZipBackupUtil.createZipBackup(this.plugin.getDataFolder(), backupsDir, "legacy-migration-target");
            this.plugin.getLogger().info("Created migration backup: " + targetBackup.getAbsolutePath());
            this.verifyBackup(targetBackup);
         } catch (IOException var19) {
            return failedFuture(var19);
         }
      }

      File finalTargetBackup = targetBackup;
      List<LegacyEggsParser.LegacyCollection> legacyCollections = LegacyEggsParser.parseAll(legacyRoot);
      List<LegacyPlayerDataParser.LegacyPlayerData> legacyPlayers = LegacyPlayerDataParser.parseAll(legacyRoot);
      List<RewardPreset> rewardPresets = LegacyRewardPresetParser.parseAll(legacyRoot);
      List<PlaceItem> placeItems = LegacyPlacePresetParser.parseAll(legacyRoot);
      this.plugin
         .getLogger()
         .info(
            "Legacy migration loaded "
               + legacyCollections.size()
               + " collection(s), "
               + legacyPlayers.size()
               + " player(s), "
               + rewardPresets.size()
               + " reward preset(s), "
               + placeItems.size()
               + " place preset(s)."
         );
      Map<String, UUID> legacyNameToCollectionId = new HashMap<>();
      List<Collection> newCollections = new ArrayList<>();
      List<LegacyDataMigrator.PlacedEggToCreate> eggsToCreate = new ArrayList<>();

      for (LegacyEggsParser.LegacyCollection lc : legacyCollections) {
         UUID newCollectionId = UUID.randomUUID();
         legacyNameToCollectionId.put(CollectionManager.normalizeCollectionName(lc.name), newCollectionId);
         Collection collection = new Collection(newCollectionId, lc.name, lc.enabled);
         collection.setSinglePlayerFind(lc.onePlayer);
         collection.setCompletionRewards(lc.globalRewards);
         collection.setActRules(LegacyActRuleTranslator.translate(newCollectionId, lc.requirements));
         collection.setProgressResetCron(translateProgressResetCron(lc.reset));
         newCollections.add(collection);

         for (LegacyEggsParser.LegacyPlacedEgg egg : lc.placedEggs) {
            eggsToCreate.add(new LegacyDataMigrator.PlacedEggToCreate(lc.name, newCollectionId, egg));
         }
      }

      List<LegacyLocationKey> snapshotKeys = new ArrayList<>();

      for (LegacyDataMigrator.PlacedEggToCreate e : eggsToCreate) {
         snapshotKeys.add(new LegacyLocationKey(e.legacyCollectionName, e.egg.world, e.egg.x, e.egg.y, e.egg.z));
      }

      return LegacyBlockSnapshotter.snapshotBlocks(this.plugin, snapshotKeys, this.config.snapshotBatchPerTick())
         .thenCompose(
            snapshots -> {
               List<Treasure> treasures = new ArrayList<>();
               Map<LegacyLocationKey, UUID> legacyToTreasureId = new HashMap<>();
               Map<String, List<UUID>> treasureIdsByCollection = new LinkedHashMap<>();
               Set<UUID> usedTreasureIds = new HashSet<>();
               int fallbackSnapshotCount = 0;

               for (LegacyDataMigrator.PlacedEggToCreate ex : eggsToCreate) {
                  LegacyLocationKey key = new LegacyLocationKey(ex.legacyCollectionName, ex.egg.world, ex.egg.x, ex.egg.y, ex.egg.z);
                  World world = Bukkit.getWorld(ex.egg.world);
                  if (world == null) {
                     this.plugin.getLogger().warning("Skipping legacy egg (world not loaded): " + key);
                  } else {
                     LegacyDataMigrator.MigratedSnapshotData snapshotData = resolveMigratedSnapshot(snapshots.get(key));
                     if (snapshotData.usedFallback) {
                        fallbackSnapshotCount++;
                        this.plugin.getLogger().warning("Migrating legacy egg with generic STONE snapshot (original block unavailable): " + key);
                     }

                     UUID treasureId = generateUniqueId(usedTreasureIds);
                     usedTreasureIds.add(treasureId);
                     Location loc = new Location(world, ex.egg.x, ex.egg.y, ex.egg.z);
                     Treasure treasure = new Treasure(
                        treasureId, ex.collectionId, loc, ex.egg.rewards, snapshotData.nbtData, snapshotData.material, snapshotData.blockState
                     );
                     treasures.add(treasure);
                     legacyToTreasureId.put(key, treasureId);
                     treasureIdsByCollection.computeIfAbsent(key.collectionName(), ignored -> new ArrayList<>()).add(treasureId);
                  }
               }

               if (fallbackSnapshotCount > 0) {
                  this.plugin
                     .getLogger()
                     .info(
                        "Migrated "
                           + fallbackSnapshotCount
                           + " legacy egg(s) using generic "
                           + "STONE"
                           + " snapshots because the original blocks could not be sampled."
                     );
               }

               Map<String, LinkedHashSet<LegacyLocationKey>> missingKeysByCollection = new LinkedHashMap<>();

               for (LegacyPlayerDataParser.LegacyPlayerData lp : legacyPlayers) {
                  for (LegacyPlayerDataParser.LegacyFoundEgg fe : lp.found) {
                     String normalizedCollectionName = CollectionManager.normalizeCollectionName(fe.collectionName);
                     if (legacyNameToCollectionId.containsKey(normalizedCollectionName)) {
                        LegacyLocationKey key = new LegacyLocationKey(fe.collectionName, fe.world, fe.x, fe.y, fe.z);
                        if (!legacyToTreasureId.containsKey(key)) {
                           missingKeysByCollection.computeIfAbsent(normalizedCollectionName, ignored -> new LinkedHashSet<>()).add(key);
                        }
                     }
                  }
               }

               Map<LegacyLocationKey, UUID> fallbackLinks = buildCollectionFallbackLinks(missingKeysByCollection, treasureIdsByCollection);
               List<PlayerData> playerDataList = new ArrayList<>();
               int foundLinks = 0;
               int missingLinks = 0;
               int fallbackLinkCount = 0;
               List<String> missingLinkDetails = new ArrayList<>();

               for (LegacyPlayerDataParser.LegacyPlayerData lp : legacyPlayers) {
                  PlayerData pd = new PlayerData(lp.playerUuid);

                  for (LegacyPlayerDataParser.LegacyFoundEgg fex : lp.found) {
                     String normalizedCollectionName = CollectionManager.normalizeCollectionName(fex.collectionName);
                     if (legacyNameToCollectionId.containsKey(normalizedCollectionName)) {
                        LegacyLocationKey key = new LegacyLocationKey(fex.collectionName, fex.world, fex.x, fex.y, fex.z);
                        UUID treasureId = legacyToTreasureId.get(key);
                        if (treasureId == null) {
                           treasureId = fallbackLinks.get(key);
                           if (treasureId != null) {
                              fallbackLinkCount++;
                           }
                        }

                        if (treasureId == null) {
                           missingLinks++;
                           String detail = String.format(
                              "Player %s: treasure at %s/%s/%d/%d/%d", lp.playerUuid, fex.collectionName, fex.world, fex.x, fex.y, fex.z
                           );
                           missingLinkDetails.add(detail);
                        } else {
                           pd.addFoundTreasure(treasureId);
                           foundLinks++;
                        }
                     }
                  }

                  playerDataList.add(pd);
               }

               if (fallbackLinkCount > 0) {
                  this.plugin.getLogger().info("Resolved " + fallbackLinkCount + " player progress link(s) using collection-level fallback matching.");
               }

               if (!missingLinkDetails.isEmpty()) {
                  this.plugin.getLogger().warning("Missing treasure links during migration:");
                  missingLinkDetails.forEach(d -> this.plugin.getLogger().warning("  - " + d));
               }

               int totalLinks = foundLinks + missingLinks;
               if (totalLinks > 0) {
                  double missingPct = (double)missingLinks / totalLinks * 100.0;
                  if (missingPct > 5.0) {
                     throw new RuntimeException(String.format("Migration aborted: %.1f%% of player progress data is missing (threshold: 5%%)", missingPct));
                  }
               }

               CompletableFuture<?>[] saveCollections = newCollections.stream().map(this.repository::saveCollection).toArray(CompletableFuture[]::new);
               CompletableFuture<?>[] saveRewardPresets = rewardPresets.stream().map(this.repository::saveRewardPreset).toArray(CompletableFuture[]::new);
               CompletableFuture<?>[] savePlaceItems = placeItems.stream().map(this.repository::savePlaceItem).toArray(CompletableFuture[]::new);
               int finalFoundLinks = foundLinks;
               int finalMissingLinks = missingLinks;
               int rewardPresetCount = rewardPresets.size();
               int placePresetCount = placeItems.size();
               return CompletableFuture.allOf(saveCollections)
                  .thenCompose(v2 -> this.repository.saveTreasuresBatch(treasures))
                  .thenCompose(v2 -> this.repository.savePlayerDataBatch(playerDataList))
                  .thenCompose(v2 -> CompletableFuture.allOf(saveRewardPresets))
                  .thenCompose(v2 -> CompletableFuture.allOf(savePlaceItems))
                  .thenApply(
                     v2 -> {
                        this.cleanupLegacyFiles(legacyRoot);
                        this.writeMarker(
                           marker,
                           legacyRoot,
                           newCollections.size(),
                           treasures.size(),
                           playerDataList.size(),
                           rewardPresetCount,
                           placePresetCount,
                           finalTargetBackup
                        );
                        return new LegacyMigratorResult(
                           newCollections.size(),
                           treasures.size(),
                           playerDataList.size(),
                           rewardPresetCount,
                           placePresetCount,
                           finalFoundLinks,
                           finalMissingLinks,
                           finalTargetBackup
                        );
                     }
                  );
            }
         )
         .handle((result, ex) -> {
            if (ex != null) {
               this.plugin.getLogger().log(Level.SEVERE, "Legacy migration failed", ex);
               return failedFuture(ex);
            } else {
               return CompletableFuture.completedFuture(result);
            }
         })
         .thenCompose(f -> (CompletionStage<LegacyMigratorResult>)f);
   }

   private <T> CompletableFuture<T> failed(String message) {
      IllegalStateException ex = new IllegalStateException(message);
      this.plugin.getLogger().severe(message);
      return failedFuture(ex);
   }

   private static <T> CompletableFuture<T> failedFuture(Throwable ex) {
      CompletableFuture<T> f = new CompletableFuture<>();
      f.completeExceptionally(ex);
      return f;
   }

   static LegacyDataMigrator.MigratedSnapshotData resolveMigratedSnapshot(LegacyBlockSnapshotter.Snapshot snapshot) {
      return snapshot == null
         ? new LegacyDataMigrator.MigratedSnapshotData("STONE", null, null, true)
         : new LegacyDataMigrator.MigratedSnapshotData(snapshot.material, snapshot.blockState, snapshot.nbtData, false);
   }

   static Map<LegacyLocationKey, UUID> buildCollectionFallbackLinks(
      Map<String, LinkedHashSet<LegacyLocationKey>> missingKeysByCollection, Map<String, List<UUID>> treasureIdsByCollection
   ) {
      Map<LegacyLocationKey, UUID> resolved = new LinkedHashMap<>();

      for (Entry<String, LinkedHashSet<LegacyLocationKey>> entry : missingKeysByCollection.entrySet()) {
         List<UUID> treasureIds = treasureIdsByCollection.get(entry.getKey());
         if (treasureIds != null && !treasureIds.isEmpty()) {
            int index = 0;

            for (LegacyLocationKey missingKey : entry.getValue()) {
               if (index >= treasureIds.size()) {
                  break;
               }

               resolved.put(missingKey, treasureIds.get(index));
               index++;
            }
         }
      }

      return resolved;
   }

   private static UUID generateUniqueId(Set<UUID> used) {
      UUID id;
      do {
         id = UUID.randomUUID();
      } while (used.contains(id));

      return id;
   }

   private static String translateProgressResetCron(LegacyEggsParser.LegacyReset reset) {
      if (reset == null) {
         return null;
      } else {
         int year = reset.year;
         int month = reset.month;
         int date = reset.date;
         int hour = reset.hour;
         int minute = reset.minute;
         int second = reset.second;
         boolean allZero = year == 0 && month == 0 && date == 0 && hour == 0 && minute == 0 && second == 0;
         if (allZero) {
            return null;
         } else {
            if (year == 0 && month == 0 && date == 0) {
               if (hour > 0 && minute == 0 && second == 0) {
                  return "0 0 0/" + hour + " * * ?";
               }

               if (minute > 0 && second == 0 && hour == 0) {
                  return "0 0/" + minute + " * * * ?";
               }

               if (second > 0 && minute == 0 && hour == 0) {
                  return "0/" + second + " * * * * ?";
               }
            }

            return null;
         }
      }
   }

   private void verifyBackup(File backupFile) throws IOException {
      ZipFile zip = new ZipFile(backupFile);

      try {
         int entryCount = Collections.list(zip.entries()).size();
         if (entryCount == 0) {
            throw new IOException("Backup file is empty");
         }

         Enumeration<? extends ZipEntry> entries = zip.entries();
         if (entries.hasMoreElements()) {
            ZipEntry entry = entries.nextElement();
            if (!entry.isDirectory()) {
               InputStream is = zip.getInputStream(entry);

               try {
                  byte[] buffer = new byte[Math.min(1024, (int)Math.max(1L, entry.getSize()))];
                  is.read(buffer);
               } catch (Throwable var11) {
                  if (is != null) {
                     try {
                        is.close();
                     } catch (Throwable var10) {
                        var11.addSuppressed(var10);
                     }
                  }

                  throw var11;
               }

               if (is != null) {
                  is.close();
               }
            }
         }

         this.plugin.getLogger().info("Backup verified: " + entryCount + " entries");
      } catch (Throwable var12) {
         try {
            zip.close();
         } catch (Throwable var9) {
            var12.addSuppressed(var9);
         }

         throw var12;
      }

      zip.close();
   }

   private void writeMarker(File marker, File legacyRoot, int collections, int treasures, int players, int rewardPresets, int placePresets, File targetBackup) {
      try {
         String summary = "Legacy migration completed successfully.\nDate: "
            + Instant.now()
            + "\nSource: "
            + legacyRoot.getAbsolutePath()
            + "\nCollections: "
            + collections
            + "\nTreasures: "
            + treasures
            + "\nPlayers: "
            + players
            + "\nReward Presets: "
            + rewardPresets
            + "\nPlace Presets: "
            + placePresets
            + "\n"
            + (targetBackup != null ? "Backup: " + targetBackup.getAbsolutePath() + "\n" : "");
         Files.write(marker.toPath(), summary.getBytes(StandardCharsets.UTF_8));
         this.plugin.getLogger().info("Migration marker written: " + marker.getName());
      } catch (IOException var10) {
         this.plugin.getLogger().log(Level.WARNING, "Failed to write migration marker", (Throwable)var10);
      }
   }

   public void cleanupLegacyFiles(File legacyRoot) {
      try {
         int deletedDirs = 0;
         int deletedFiles = 0;
         deletedDirs += deleteLegacySubfolder(legacyRoot, "eggs") ? 1 : 0;
         deletedDirs += deleteLegacySubfolder(legacyRoot, "invs") ? 1 : 0;
         deletedDirs += deleteLegacySubfolder(legacyRoot, "menus") ? 1 : 0;
         deletedDirs += deleteLegacySubfolder(legacyRoot, "messages") ? 1 : 0;
         deletedDirs += deleteLegacySubfolder(legacyRoot, "presets") ? 1 : 0;
         deletedFiles += deleteLegacyFile(legacyRoot, "config.yml") ? 1 : 0;
         deletedFiles += deleteLegacyFile(legacyRoot, "mysql.yml") ? 1 : 0;
         deletedFiles += deleteLegacyFile(legacyRoot, "plugin_data.yml") ? 1 : 0;
         if (deletedDirs > 0 || deletedFiles > 0) {
            this.plugin
               .getLogger()
               .info("Legacy cleanup complete. Removed " + deletedDirs + " folder(s) and " + deletedFiles + " file(s) from: " + legacyRoot.getAbsolutePath());
         }
      } catch (Exception var4) {
         this.plugin.getLogger().log(Level.WARNING, "Legacy cleanup failed", (Throwable)var4);
      }
   }

   private static boolean deleteLegacySubfolder(File root, String name) throws Exception {
      File folder = new File(root, name);
      if (folder.exists() && folder.isDirectory()) {
         deleteRecursively(folder.toPath());
         return true;
      } else {
         return false;
      }
   }

   private static boolean deleteLegacyFile(File root, String name) throws Exception {
      File file = new File(root, name);
      return file.exists() && !file.isDirectory() ? Files.deleteIfExists(file.toPath()) : false;
   }

   private static void deleteRecursively(Path root) throws Exception {
      if (root != null && Files.exists(root)) {
         Files.walkFileTree(root, new SimpleFileVisitor<Path>() {
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
               Files.deleteIfExists(file);
               return FileVisitResult.CONTINUE;
            }

            public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
               Files.deleteIfExists(dir);
               return FileVisitResult.CONTINUE;
            }
         });
      }
   }

   static final class MigratedSnapshotData {
      final String material;
      final String blockState;
      final String nbtData;
      final boolean usedFallback;

      private MigratedSnapshotData(String material, String blockState, String nbtData, boolean usedFallback) {
         this.material = material;
         this.blockState = blockState;
         this.nbtData = nbtData;
         this.usedFallback = usedFallback;
      }
   }

   private static final class PlacedEggToCreate {
      final String legacyCollectionName;
      final UUID collectionId;
      final LegacyEggsParser.LegacyPlacedEgg egg;

      private PlacedEggToCreate(String legacyCollectionName, UUID collectionId, LegacyEggsParser.LegacyPlacedEgg egg) {
         this.legacyCollectionName = legacyCollectionName;
         this.collectionId = collectionId;
         this.egg = egg;
      }
   }
}
