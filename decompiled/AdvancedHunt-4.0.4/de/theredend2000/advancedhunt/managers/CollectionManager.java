package de.theredend2000.advancedhunt.managers;

import de.theredend2000.advancedhunt.Main;
import de.theredend2000.advancedhunt.data.DataRepository;
import de.theredend2000.advancedhunt.lib.cronutils.model.Cron;
import de.theredend2000.advancedhunt.lib.cronutils.model.time.ExecutionTime;
import de.theredend2000.advancedhunt.model.ActRule;
import de.theredend2000.advancedhunt.model.Collection;
import de.theredend2000.advancedhunt.model.PlayerData;
import de.theredend2000.advancedhunt.model.TreasureCore;
import de.theredend2000.advancedhunt.util.CronUtils;
import de.theredend2000.advancedhunt.util.HeadHelper;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

public class CollectionManager {
   private final JavaPlugin plugin;
   private final DataRepository repository;
   private final TreasureManager treasureManager;
   private final PlayerManager playerManager;
   private final RewardManager rewardManager;
   private final ActRuleEvaluator actRuleEvaluator;
   private volatile List<Collection> cachedCollections;
   private BukkitTask resetTask;

   public CollectionManager(
      JavaPlugin plugin, DataRepository repository, TreasureManager treasureManager, PlayerManager playerManager, RewardManager rewardManager
   ) {
      this.plugin = plugin;
      this.repository = repository;
      this.treasureManager = treasureManager;
      this.playerManager = playerManager;
      this.rewardManager = rewardManager;
      this.cachedCollections = new ArrayList<>();
      this.actRuleEvaluator = new ActRuleEvaluator(plugin);
      this.reloadCollections();
      this.resetTask = Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, this::checkResets, 1200L, 1200L);
   }

   public void stop() {
      if (this.resetTask != null) {
         this.resetTask.cancel();
         this.resetTask = null;
      }
   }

   public CompletableFuture<Void> reloadCollections() {
      return this.repository.loadCollections().thenAccept(collections -> {
         this.cachedCollections = (List<Collection>)collections;
         this.actRuleEvaluator.clearAllAvailabilityCache();
      });
   }

   public static String normalizeCollectionName(String name) {
      return name == null ? "" : name.replace(" ", "_").toLowerCase();
   }

   public Optional<Collection> getCollectionByName(String nameOrUuid) {
      if (nameOrUuid != null && nameOrUuid.matches("[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}")) {
         UUID uuid = UUID.fromString(nameOrUuid);
         return this.getCollectionById(uuid);
      } else {
         String normalizedInput = normalizeCollectionName(nameOrUuid);
         return this.cachedCollections.stream().filter(c -> normalizeCollectionName(c.getName()).equals(normalizedInput)).findFirst();
      }
   }

   public Optional<Collection> getCollectionById(UUID id) {
      return this.cachedCollections.stream().filter(c -> c.getId().equals(id)).findFirst();
   }

   public List<String> getAllCollectionNames() {
      return this.cachedCollections.stream().map(Collection::getName).collect(Collectors.toList());
   }

   public List<Collection> getAllCollections() {
      return new ArrayList<>(this.cachedCollections);
   }

   public void checkCompletion(Player player, Collection collection) {
      CompletableFuture.runAsync(() -> {
         PlayerData data = this.playerManager.getPlayerData(player.getUniqueId());
         List<TreasureCore> allCores = this.treasureManager.getTreasureCoresInCollection(collection.getId());
         if (!allCores.isEmpty()) {
            boolean allFound = true;

            for (TreasureCore core : allCores) {
               if (!data.hasFound(core.getId())) {
                  allFound = false;
                  break;
               }
            }

            if (allFound) {
               Bukkit.getScheduler().runTask(this.plugin, () -> {
                  player.sendMessage(((Main)this.plugin).getMessageManager().getMessage("collection.completed", "%collection%", collection.getName()));
                  ((Main)this.plugin).getSoundManager().playCollectionCompleted(player);
                  this.rewardManager.giveRewards(player, collection.getCompletionRewards(), collection);
               });
            } else {
               Bukkit.getScheduler().runTask(this.plugin, () -> ((Main)this.plugin).getSoundManager().playTreasureFound(player));
            }
         }
      });
   }

   public CompletableFuture<Boolean> renameCollection(String oldName, String newName) {
      String normalizedNewName = newName.replace(" ", "_");
      Optional<Collection> existing = this.getCollectionByName(normalizedNewName);
      if (existing.isPresent() && !normalizeCollectionName(existing.get().getName()).equals(normalizeCollectionName(oldName))) {
         return CompletableFuture.completedFuture(false);
      } else {
         Optional<Collection> c = this.getCollectionByName(oldName);
         return c.<CompletableFuture<Boolean>>map(
               collection -> this.repository
                  .renameCollection(collection.getId(), normalizedNewName)
                  .thenCompose(success -> success ? this.reloadCollections().thenApply(v -> true) : CompletableFuture.completedFuture(false))
            )
            .orElseGet(() -> CompletableFuture.completedFuture(false));
      }
   }

   public UUID generateUniqueCollectionId() {
      UUID id;
      do {
         id = UUID.randomUUID();
      } while (this.getCollectionById(id).isPresent());

      return id;
   }

   public UUID generateUniqueActRuleId() {
      UUID id;
      do {
         id = UUID.randomUUID();
      } while (this.doesActRuleExist(id));

      return id;
   }

   private boolean doesActRuleExist(UUID id) {
      for (Collection collection : this.cachedCollections) {
         if (collection.getActRules() != null) {
            for (ActRule rule : collection.getActRules()) {
               if (rule.getId().equals(id)) {
                  return true;
               }
            }
         }
      }

      return false;
   }

   public CompletableFuture<Boolean> createCollection(String name) {
      String normalizedName = name.replace(" ", "_");
      if (this.getCollectionByName(normalizedName).isPresent()) {
         return CompletableFuture.completedFuture(false);
      } else {
         Collection collection = new Collection(this.generateUniqueCollectionId(), normalizedName, true);
         return this.repository.saveCollection(collection).thenCompose(v -> this.reloadCollections().thenApply(x -> true));
      }
   }

   public CompletableFuture<Void> saveCollection(Collection collection) {
      this.actRuleEvaluator.clearAvailabilityCache(collection.getId());
      return this.repository.saveCollection(collection).thenCompose(v -> this.reloadCollections());
   }

   public CompletableFuture<Void> deleteCollection(UUID collectionId) {
      return this.repository.deleteCollection(collectionId).thenCompose(v -> {
         this.treasureManager.removeCollection(collectionId);
         return this.reloadCollections();
      });
   }

   public CompletableFuture<Void> deleteCollection(UUID collectionId, TreasureDeletionType handling) {
      TreasureDeletionType effectiveHandling = handling == null ? TreasureDeletionType.KEEP_ALL : handling;
      List<TreasureCore> cores = new ArrayList<>(this.treasureManager.getTreasureCoresInCollection(collectionId));
      if (cores.isEmpty()) {
         return this.deleteCollection(collectionId);
      } else {
         List<CompletableFuture<TreasureWorldEdit>> editFutures = new ArrayList<>(cores.size());

         for (TreasureCore core : cores) {
            if (core != null) {
               Location loc = core.getLocation();
               if (loc != null && loc.getWorld() != null) {
                  String worldName = loc.getWorld().getName();
                  int x = loc.getBlockX();
                  int y = loc.getBlockY();
                  int z = loc.getBlockZ();
                  TreasureWorldEdit.Action action;
                  switch (effectiveHandling) {
                     case REMOVE_BLOCKS_AND_FURNITURE:
                        action = TreasureWorldEdit.Action.REMOVE;
                        break;
                     case REMOVE_HEADS:
                        action = HeadHelper.isHeadMaterialName(core.getMaterial()) ? TreasureWorldEdit.Action.REMOVE : TreasureWorldEdit.Action.RESTORE;
                        break;
                     case KEEP_ALL:
                     default:
                        action = TreasureWorldEdit.Action.RESTORE;
                  }

                  if (action == TreasureWorldEdit.Action.REMOVE) {
                     editFutures.add(
                        CompletableFuture.completedFuture(
                           new TreasureWorldEdit(worldName, x, y, z, core.getMaterial(), core.getBlockState(), null, TreasureWorldEdit.Action.REMOVE)
                        )
                     );
                  } else if (HeadHelper.isHeadMaterialName(core.getMaterial())) {
                     CompletableFuture<TreasureWorldEdit> future = this.treasureManager.getFullTreasureAsync(core.getId()).handle((treasure, ex) -> {
                        String nbtData = treasure != null ? treasure.getNbtData() : null;
                        return new TreasureWorldEdit(worldName, x, y, z, core.getMaterial(), core.getBlockState(), nbtData, TreasureWorldEdit.Action.RESTORE);
                     });
                     editFutures.add(future);
                  } else {
                     editFutures.add(
                        CompletableFuture.completedFuture(
                           new TreasureWorldEdit(worldName, x, y, z, core.getMaterial(), core.getBlockState(), null, TreasureWorldEdit.Action.RESTORE)
                        )
                     );
                  }
               }
            }
         }

         CompletableFuture<Void> buildEdits = CompletableFuture.allOf(editFutures.toArray(new CompletableFuture[0]));
         return buildEdits.thenCompose(
            v -> {
               List<TreasureWorldEdit> edits = new ArrayList<>(editFutures.size());

               for (CompletableFuture<TreasureWorldEdit> f : editFutures) {
                  try {
                     TreasureWorldEdit edit = f.join();
                     if (edit != null) {
                        edits.add(edit);
                     }
                  } catch (Throwable var9x) {
                  }
               }

               return this.repository
                  .deleteCollection(collectionId)
                  .thenCompose(v2 -> {
                     this.treasureManager.removeCollection(collectionId);
                     return this.reloadCollections();
                  })
                  .thenRun(
                     () -> {
                        try {
                           Main main = (Main)this.plugin;
                           if (main.getCollectionDeletionCleanupManager() != null && !edits.isEmpty()) {
                              main.getLogger()
                                 .info(
                                    "Scheduling deletion cleanup for collection " + collectionId + " (" + effectiveHandling + ", edits=" + edits.size() + ")"
                                 );
                              main.getCollectionDeletionCleanupManager().scheduleEdits(edits);
                           }
                        } catch (Throwable var5x) {
                        }
                     }
                  );
            }
         );
      }
   }

   private void checkResets() {
      ZonedDateTime now = ZonedDateTime.now();

      for (Collection c : this.cachedCollections) {
         if (c.getProgressResetCron() != null && !c.getProgressResetCron().isEmpty()) {
            try {
               Cron cron = CronUtils.getParser().parse(c.getProgressResetCron());
               ExecutionTime executionTime = ExecutionTime.forCron(cron);
               Optional<ZonedDateTime> lastExecution = executionTime.lastExecution(now);
               if (lastExecution.isPresent()) {
                  ZonedDateTime lastRun = lastExecution.get();
                  if (lastRun.isAfter(now.minusMinutes(1L)) && lastRun.isBefore(now.plusSeconds(1L))) {
                     this.plugin.getLogger().info("Resetting collection progress: " + c.getName());
                     this.repository.resetCollectionProgress(c.getId());
                     ((Main)this.plugin).getParticleManager().clearGlobalCache(c.getId());

                     for (Player p : Bukkit.getOnlinePlayers()) {
                        this.playerManager.invalidate(p.getUniqueId());
                     }
                  }
               }
            } catch (Exception var10) {
               this.plugin.getLogger().warning("Invalid progress reset cron for collection " + c.getName() + ": " + c.getProgressResetCron());
            }
         }
      }
   }

   public boolean isCollectionAvailable(Collection collection) {
      return this.actRuleEvaluator.isCollectionAvailable(collection);
   }

   public Optional<ZonedDateTime> getNextActivation(Collection collection) {
      return this.actRuleEvaluator.getNextActivation(collection);
   }
}
