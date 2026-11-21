package com.magmaguy.elitemobs.instanced.dungeons;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.config.DungeonsConfig;
import com.magmaguy.elitemobs.config.SpecialItemSystemsConfig;
import com.magmaguy.elitemobs.config.contentpackages.ContentPackagesConfigFields;
import com.magmaguy.elitemobs.dungeons.WorldDungeonPackage;
import com.magmaguy.elitemobs.instanced.WorldOperationQueue;
import com.magmaguy.elitemobs.items.upgradesystem.EnchantmentAcquisition;
import com.magmaguy.elitemobs.menus.ItemEnchantmentMenu;
import com.magmaguy.elitemobs.utils.WorldInstantiator;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import lombok.Generated;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

public class EnchantmentDungeonInstance extends DungeonInstance {
   Player player;
   private EnchantmentAcquisition acquisition;
   private BukkitTask acquisitionMonitor;
   private boolean challengeResolved = false;

   public EnchantmentDungeonInstance(
      ContentPackagesConfigFields contentPackagesConfigFields,
      Location lobbyLocation,
      Location startLocation,
      World world,
      Player player,
      String difficultyName
   ) {
      super(contentPackagesConfigFields, lobbyLocation, startLocation, world, player, difficultyName);
      this.player = player;
   }

   public static boolean setupRandomEnchantedChallengeDungeon(Player player, EnchantmentAcquisition acquisition) {
      List<ContentPackagesConfigFields> contentPackagesConfigFieldsList = new ArrayList<>();
      WorldDungeonPackage.getEmPackages().values().stream().forEach(emPackage -> {
         if (emPackage.isInstalled() && emPackage.getContentPackagesConfigFields().isEnchantmentChallenge()) {
            contentPackagesConfigFieldsList.add(emPackage.getContentPackagesConfigFields());
         }
      });
      if (contentPackagesConfigFieldsList.isEmpty()) {
         player.sendMessage(DungeonsConfig.getEnchantNoChallengeMessage());
         return false;
      } else {
         ContentPackagesConfigFields contentPackagesConfigFields = contentPackagesConfigFieldsList.get(
            ThreadLocalRandom.current().nextInt(0, contentPackagesConfigFieldsList.size())
         );
         String instancedWordName = WorldInstantiator.getNewWorldName(contentPackagesConfigFields.getWorldName());
         if (!launchEvent(contentPackagesConfigFields, instancedWordName, player)) {
            throw new IllegalStateException("Enchantment challenge launch was rejected");
         } else {
            boolean[] accepted = new boolean[]{false};
            PendingWorldCopy copy = new PendingWorldCopy();
            WorldOperationQueue.queueOperation(player, () -> copy.copy(() -> cloneWorldFiles(contentPackagesConfigFields, instancedWordName)), () -> {
               if (player.isOnline() && !MetadataHandler.shutdownRequested && acquisition.isOwned()) {
                  acquisition.validateProviders();
                  String difficulty = (String)contentPackagesConfigFields.getDifficulties().get(0).get("name");
                  if (copy.transferToInitializer()) {
                     DungeonInstance instance = initializeInstancedWorld(contentPackagesConfigFields, instancedWordName, player, difficulty);
                     if (instance instanceof EnchantmentDungeonInstance challenge && !challenge.isDefunct()) {
                        challenge.accept(acquisition);
                        accepted[0] = true;
                     } else if (instance != null) {
                        instance.removeInstance();
                     }
                  }
               }
            }, contentPackagesConfigFields.getName(), () -> {
               if (!accepted[0]) {
                  try {
                     copy.cancel();
                  } finally {
                     acquisition.abort("challenge startup failed or was cancelled");
                  }
               }
            });
            return true;
         }
      }
   }

   private void accept(EnchantmentAcquisition acquisition) {
      this.acquisition = acquisition;
      this.acquisitionMonitor = Bukkit.getScheduler().runTaskTimer(MetadataHandler.PLUGIN, () -> {
         try {
            acquisition.validateProviders();
         } catch (RuntimeException var3) {
            this.removeInstance();
         }
      }, 20L, 20L);
   }

   @Override
   protected void cancelScheduledTasks() {
      if (this.acquisitionMonitor != null) {
         this.acquisitionMonitor.cancel();
         this.acquisitionMonitor = null;
      }

      super.cancelScheduledTasks();
   }

   @Override
   public void removeInstance() {
      if (this.acquisition != null && !this.challengeResolved) {
         this.challengeResolved = true;
         this.acquisition.abort("challenge infrastructure or provider lifetime ended before resolution");
      }

      super.removeInstance();
   }

   @Override
   public boolean addNewPlayer(Player player) {
      return false;
   }

   @Override
   public boolean requestPartyEntry(Player player) {
      return false;
   }

   @Override
   public void endMatch() {
      if (this.players.isEmpty()) {
         this.removeInstance();
      } else {
         this.scheduleDelayedDestroy(200L);
      }
   }

   @Override
   protected void victory() {
      if (this.markChallengeResolved()) {
         this.acquisition.success();
         super.victory();
         this.player.sendMessage(DungeonsConfig.getEnchantChallengeCompleteMessage());
         this.player.sendMessage(DungeonsConfig.getEnchantChallengeSuccessMessage());
         ItemEnchantmentMenu.broadcastEnchantmentMessage(this.acquisition.upgraded(), this.player, SpecialItemSystemsConfig.getSuccessAnnouncement());
      }
   }

   @Override
   protected void defeat() {
      if (this.markChallengeResolved()) {
         boolean destroyed = ThreadLocalRandom.current().nextDouble() < SpecialItemSystemsConfig.getCriticalFailureChanceDuringChallengeChance();
         if (destroyed) {
            this.acquisition.criticalFailure();
         } else {
            this.acquisition.failure();
         }

         super.defeat();
         if (destroyed) {
            this.player
               .sendMessage(DungeonsConfig.getEnchantCriticalFailureMessage().replace("$item", this.acquisition.original().getItemMeta().getDisplayName()));
            ItemEnchantmentMenu.broadcastEnchantmentMessage(this.acquisition.upgraded(), this.player, SpecialItemSystemsConfig.getCriticalFailureAnnouncement());
         } else {
            this.player
               .sendMessage(DungeonsConfig.getEnchantChallengeFailedMessage().replace("$item", this.acquisition.original().getItemMeta().getDisplayName()));
         }
      }
   }

   private boolean markChallengeResolved() {
      if (!this.challengeResolved && this.acquisition != null) {
         try {
            this.acquisition.validateProviders();
         } catch (RuntimeException var2) {
            this.removeInstance();
            return false;
         }

         this.challengeResolved = true;
         if (this.acquisitionMonitor != null) {
            this.acquisitionMonitor.cancel();
            this.acquisitionMonitor = null;
         }

         return true;
      } else {
         return false;
      }
   }

   @Generated
   public Player getPlayer() {
      return this.player;
   }

   @Generated
   public void setPlayer(Player player) {
      this.player = player;
   }
}
