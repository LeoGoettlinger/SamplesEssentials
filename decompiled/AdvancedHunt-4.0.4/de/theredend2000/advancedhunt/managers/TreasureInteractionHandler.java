package de.theredend2000.advancedhunt.managers;

import de.theredend2000.advancedhunt.Main;
import de.theredend2000.advancedhunt.menu.reward.RewardsMenu;
import de.theredend2000.advancedhunt.model.CollectionRewardHolder;
import de.theredend2000.advancedhunt.model.PlayerData;
import de.theredend2000.advancedhunt.model.TreasureCore;
import de.theredend2000.advancedhunt.model.TreasureRewardHolder;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public final class TreasureInteractionHandler {
   private static volatile TreasureInteractionHandler instance;
   private final Main plugin;
   private final TreasureManager treasureManager;
   private final PlayerManager playerManager;
   private final CollectionManager collectionManager;
   private final RewardManager rewardManager;
   private final PlaceModeManager placeModeManager;
   private final Set<UUID> playersCollecting = ConcurrentHashMap.newKeySet();

   public static TreasureInteractionHandler getInstance(Main plugin) {
      TreasureInteractionHandler localRef = instance;
      if (localRef == null) {
         synchronized (TreasureInteractionHandler.class) {
            localRef = instance;
            if (localRef == null) {
               instance = localRef = new TreasureInteractionHandler(plugin);
            }
         }
      }

      return localRef;
   }

   public static synchronized void reset() {
      instance = null;
   }

   TreasureInteractionHandler(Main plugin) {
      this.plugin = plugin;
      this.treasureManager = plugin.getTreasureManager();
      this.playerManager = plugin.getPlayerManager();
      this.collectionManager = plugin.getCollectionManager();
      this.rewardManager = plugin.getRewardManager();
      this.placeModeManager = plugin.getPlaceModeManager();
   }

   public boolean handleSneakRewardsEditor(Player player, TreasureCore treasureCore) {
      if (player == null || treasureCore == null) {
         return false;
      } else if (!player.isSneaking()) {
         return false;
      } else if (!player.hasPermission("advancedhunt.admin.rewards")) {
         return true;
      } else {
         this.treasureManager
            .getFullTreasureAsync(treasureCore.getId())
            .thenAccept(
               treasure -> {
                  if (treasure != null) {
                     Bukkit.getScheduler()
                        .runTask(
                           this.plugin,
                           () -> {
                              RewardsMenu menu = new RewardsMenu(player, this.plugin, new TreasureRewardHolder(this.plugin, treasure));
                              this.collectionManager
                                 .getCollectionById(treasure.getCollectionId())
                                 .ifPresent(collection -> menu.setAlternateContext(new CollectionRewardHolder(this.plugin, collection)));
                              menu.open();
                           }
                        );
                  }
               }
            );
         return true;
      }
   }

   public void handleTreasureCollect(Player player, TreasureCore treasureCore) {
      if (player != null && treasureCore != null) {
         UUID playerId = player.getUniqueId();
         if (this.playersCollecting.add(playerId)) {
            this.handleTreasureCollectInternal(player, treasureCore, playerId);
         }
      }
   }

   private void handleTreasureCollectInternal(Player player, TreasureCore treasureCore, UUID playerId) {
      if (this.placeModeManager.isInPlaceMode(player)) {
         player.sendMessage(this.plugin.getMessageManager().getMessage("treasure.placemode"));
         this.plugin.getSoundManager().playPlaceModeCollectDeny(player);
         this.playersCollecting.remove(playerId);
      } else if (!this.playerManager.isPlayerDataLoaded(player.getUniqueId())) {
         this.playersCollecting.remove(playerId);
      } else {
         PlayerData data = this.playerManager.getPlayerData(player.getUniqueId());
         boolean[] asyncGuardHeld = new boolean[]{false};
         this.collectionManager.getCollectionById(treasureCore.getCollectionId()).ifPresent(collection -> {
            if (!this.collectionManager.isCollectionAvailable(collection)) {
               Bukkit.getScheduler().runTask(this.plugin, () -> {
                  this.plugin.getSoundManager().playCollectionUnavailable(player);
                  player.sendMessage(this.plugin.getMessageManager().getMessage("collection.unavailable", "%collection%", collection.getName()));
                  this.collectionManager.getNextActivation(collection).ifPresent(nextTime -> {
                     String timeStr = this.plugin.getMessageManager().formatDateTime(nextTime);
                     player.sendMessage(this.plugin.getMessageManager().getMessage("collection.available_at", "%time%", timeStr));
                  });
               });
               this.playersCollecting.remove(playerId);
            } else if (data.hasFound(treasureCore.getId())) {
               player.sendMessage(this.plugin.getMessageManager().getMessage("treasure.already_found"));
               this.plugin.getSoundManager().playTreasureAlreadyFound(player);
               this.collectionManager.getCollectionById(treasureCore.getCollectionId()).ifPresent(col -> {
                  if (col.isHideAfterFound()) {
                     this.plugin.getTreasureVisibilityManager().hideFoundTreasureForPlayer(player, treasureCore);
                  }
               });
               this.playersCollecting.remove(playerId);
            } else {
               if (collection.isSinglePlayerFind()) {
                  asyncGuardHeld[0] = true;
                  this.plugin.getDataRepository().getPlayersWhoFound(treasureCore.getId()).thenAccept(claimers -> {
                     if (!claimers.isEmpty()) {
                        Bukkit.getScheduler().runTask(this.plugin, () -> {
                           player.sendMessage(this.plugin.getMessageManager().getMessage("treasure.already_claimed_global"));
                           this.plugin.getSoundManager().playTreasureClaimedByOther(player);
                        });
                        this.playersCollecting.remove(playerId);
                     } else {
                        Bukkit.getScheduler().runTask(this.plugin, () -> {
                           this.plugin.getFireworkManager().spawnFireworkRocket(treasureCore.getLocation().clone().add(0.5, 1.0, 0.5));
                           this.claimTreasure(player, treasureCore, data);
                           this.playersCollecting.remove(playerId);
                        });
                     }
                  }).exceptionally(ex -> {
                     this.playersCollecting.remove(playerId);
                     return null;
                  });
               } else {
                  this.plugin.getFireworkManager().spawnFireworkRocket(treasureCore.getLocation().clone().add(0.5, 1.0, 0.5));
                  this.claimTreasure(player, treasureCore, data);
                  this.playersCollecting.remove(playerId);
               }
            }
         });
         if (!asyncGuardHeld[0]) {
            this.playersCollecting.remove(playerId);
         }
      }
   }

   private void claimTreasure(Player player, TreasureCore treasureCore, PlayerData data) {
      data.addFoundTreasure(treasureCore.getId());
      this.playerManager.savePlayerData(player.getUniqueId());
      player.sendMessage(this.plugin.getMessageManager().getMessage("treasure.found"));
      this.collectionManager.getCollectionById(treasureCore.getCollectionId()).ifPresent(col -> {
         if (col.isSinglePlayerFind()) {
            this.plugin.getParticleManager().markTreasureAsGloballyClaimed(treasureCore.getId());
         }

         if (col.isHideAfterFound()) {
            this.plugin.getTreasureVisibilityManager().hideFoundTreasureForPlayer(player, treasureCore);
            if (col.isSinglePlayerFind()) {
               for (Player other : Bukkit.getOnlinePlayers()) {
                  if (!other.equals(player)) {
                     this.plugin.getTreasureVisibilityManager().hideFoundTreasureForPlayer(other, treasureCore);
                  }
               }
            }
         }
      });
      this.treasureManager
         .getFullTreasureAsync(treasureCore.getId())
         .thenAccept(
            treasure -> {
               if (treasure != null && treasure.getRewards() != null) {
                  Bukkit.getScheduler()
                     .runTask(
                        this.plugin,
                        () -> this.collectionManager
                           .getCollectionById(treasureCore.getCollectionId())
                           .ifPresent(c -> this.rewardManager.giveRewards(player, treasure.getRewards(), c))
                     );
               }
            }
         );
      this.collectionManager.getCollectionById(treasureCore.getCollectionId()).ifPresent(c -> this.collectionManager.checkCompletion(player, c));
   }
}
