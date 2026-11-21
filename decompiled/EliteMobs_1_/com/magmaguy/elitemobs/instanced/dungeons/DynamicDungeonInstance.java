package com.magmaguy.elitemobs.instanced.dungeons;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.config.DungeonsConfig;
import com.magmaguy.elitemobs.config.PartyConfig;
import com.magmaguy.elitemobs.config.contentpackages.ContentPackagesConfig;
import com.magmaguy.elitemobs.config.contentpackages.ContentPackagesConfigFields;
import com.magmaguy.elitemobs.dungeons.utility.DungeonUtils;
import com.magmaguy.elitemobs.entitytracker.EntityTracker;
import com.magmaguy.elitemobs.instanced.WorldOperationQueue;
import com.magmaguy.elitemobs.magmacore.util.Logger;
import com.magmaguy.elitemobs.mobconstructor.custombosses.CustomMusic;
import com.magmaguy.elitemobs.mobconstructor.custombosses.InstancedBossEntity;
import com.magmaguy.elitemobs.parties.PartyDungeonReadyCheckManager;
import com.magmaguy.elitemobs.quests.DynamicQuest;
import com.magmaguy.elitemobs.utils.ConfigurationLocation;
import com.magmaguy.elitemobs.utils.WorldInstantiator;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.function.BooleanSupplier;
import java.util.stream.Collectors;
import lombok.Generated;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class DynamicDungeonInstance extends DungeonInstance {
   private final int selectedLevel;

   public DynamicDungeonInstance(
      ContentPackagesConfigFields contentPackagesConfigFields,
      Location lobbyLocation,
      Location startLocation,
      World world,
      Player player,
      String difficultyName,
      int selectedLevel
   ) {
      this(contentPackagesConfigFields, lobbyLocation, startLocation, world, List.of(player), difficultyName, selectedLevel);
   }

   public DynamicDungeonInstance(
      ContentPackagesConfigFields contentPackagesConfigFields,
      Location lobbyLocation,
      Location startLocation,
      World world,
      Collection<Player> initialPlayers,
      String difficultyName,
      int selectedLevel
   ) {
      this(contentPackagesConfigFields, lobbyLocation, startLocation, world, initialPlayers, difficultyName, selectedLevel, () -> true);
   }

   private DynamicDungeonInstance(
      ContentPackagesConfigFields contentPackagesConfigFields,
      Location lobbyLocation,
      Location startLocation,
      World world,
      Collection<Player> initialPlayers,
      String difficultyName,
      int selectedLevel,
      BooleanSupplier admissionAuthorization
   ) {
      super(contentPackagesConfigFields, lobbyLocation, startLocation, world, initialPlayers, difficultyName, admissionAuthorization);
      this.selectedLevel = selectedLevel;
      if (!this.cancelled && !this.isInstanceRemovalScheduled() && this.getWorld() != null) {
         try {
            this.recalculateLevelSyncForDynamicLevel(selectedLevel);
            this.getPlayers().forEach(this::applyDynamicEntryState);
            new DynamicDungeonInstance.SetBossLevelsTask(this, selectedLevel).runTaskLater(MetadataHandler.PLUGIN, 80L);
         } catch (RuntimeException var10) {
            this.deregisterFailedConstruction();
            throw var10;
         }
      }
   }

   public static void setupDynamicDungeon(Player player, String dungeonConfigFieldsString, String difficultyName, int selectedLevel) {
      ContentPackagesConfigFields dynamicDungeonConfigFields = ContentPackagesConfig.getDungeonPackages().get(dungeonConfigFieldsString);
      if (dynamicDungeonConfigFields == null) {
         player.sendMessage(DungeonsConfig.getDynamicDungeonDataFailedMessage().replace("$dungeon", dungeonConfigFieldsString));
      } else if (dynamicDungeonConfigFields.getPermission() != null
         && !dynamicDungeonConfigFields.getPermission().isEmpty()
         && !player.hasPermission(dynamicDungeonConfigFields.getPermission())) {
         player.sendMessage(DungeonsConfig.getDynamicDungeonNoPermissionMessage());
      } else {
         List<UUID> entryMemberIds = prepareDungeonEntryRoster(player, dynamicDungeonConfigFields);
         if (!entryMemberIds.isEmpty()) {
            PartyDungeonReadyCheckManager.request(
               player,
               entryMemberIds,
               readyCheckLevelDescription(readyCheckDescription(dynamicDungeonConfigFields, difficultyName), selectedLevel),
               reservation -> launchDynamicDungeon(player, dynamicDungeonConfigFields, entryMemberIds, difficultyName, selectedLevel, reservation)
            );
         }
      }
   }

   private static boolean launchDynamicDungeon(
      Player player,
      ContentPackagesConfigFields dynamicDungeonConfigFields,
      List<UUID> entryMemberIds,
      String difficultyName,
      int selectedLevel,
      PartyDungeonReadyCheckManager.LaunchReservation reservation
   ) {
      if (!reservation.isValid()) {
         return false;
      } else if (resolveDungeonEntryRoster(player, entryMemberIds, dynamicDungeonConfigFields, null).isEmpty()) {
         return false;
      } else {
         String instancedWorldName = WorldInstantiator.getNewWorldName(dynamicDungeonConfigFields.getWorldName());
         if (!launchEvent(dynamicDungeonConfigFields, instancedWorldName, player)) {
            return false;
         } else if (!reservation.isValid()) {
            return false;
         } else {
            PendingWorldCopy copy = new PendingWorldCopy();
            WorldOperationQueue.queueOperation(
               player, () -> !reservation.isValid() ? true : copy.copy(() -> cloneWorldFiles(dynamicDungeonConfigFields, instancedWorldName)), () -> {
                  if (reservation.isValid() && copy.transferToInitializer()) {
                     initializeDynamicWorld(dynamicDungeonConfigFields, instancedWorldName, player, entryMemberIds, difficultyName, selectedLevel, reservation);
                  }
               }, dynamicDungeonConfigFields.getName(), () -> {
                  try {
                     copy.cancel();
                  } finally {
                     reservation.release();
                  }
               }
            );
            return true;
         }
      }
   }

   protected static DynamicDungeonInstance initializeDynamicWorld(
      ContentPackagesConfigFields dynamicDungeonConfigFields, String instancedWorldName, Player player, String difficultyName, int selectedLevel
   ) {
      return initializeDynamicWorld(dynamicDungeonConfigFields, instancedWorldName, player, List.of(player.getUniqueId()), difficultyName, selectedLevel);
   }

   protected static DynamicDungeonInstance initializeDynamicWorld(
      ContentPackagesConfigFields dynamicDungeonConfigFields,
      String instancedWorldName,
      Player player,
      List<UUID> entryMemberIds,
      String difficultyName,
      int selectedLevel
   ) {
      return initializeDynamicWorld(dynamicDungeonConfigFields, instancedWorldName, player, entryMemberIds, difficultyName, selectedLevel, null);
   }

   protected static DynamicDungeonInstance initializeDynamicWorld(
      ContentPackagesConfigFields dynamicDungeonConfigFields,
      String instancedWorldName,
      Player player,
      List<UUID> entryMemberIds,
      String difficultyName,
      int selectedLevel,
      PartyDungeonReadyCheckManager.LaunchReservation reservation
   ) {
      if (reservation != null && !reservation.isValid()) {
         cleanupUnloadedWorldFolder(instancedWorldName);
         return null;
      } else {
         World world = null;

         try {
            dynamicDungeonConfigFields.prepareInstanceDefinition();
            world = DungeonUtils.loadWorld(instancedWorldName, dynamicDungeonConfigFields.getEnvironment(), dynamicDungeonConfigFields);
            if (world == null) {
               player.sendMessage(DungeonsConfig.getDynamicDungeonWorldLoadFailedMessage());
               cleanupUnloadedWorldFolder(instancedWorldName);
               return null;
            } else {
               List<Player> entryPlayers = resolveDungeonEntryRoster(player, entryMemberIds, dynamicDungeonConfigFields, null);
               if (!entryPlayers.isEmpty() && (reservation == null || reservation.isValid())) {
                  if (dynamicDungeonConfigFields.getSong() != null) {
                     new CustomMusic(dynamicDungeonConfigFields.getSong(), dynamicDungeonConfigFields, world);
                  }

                  Location startLocation = dynamicDungeonConfigFields.instanceStartLocation(world);
                  Location lobbyLocation = ConfigurationLocation.serialize(dynamicDungeonConfigFields.getTeleportLocationString());
                  if (lobbyLocation != null) {
                     lobbyLocation.setWorld(world);
                  } else {
                     lobbyLocation = startLocation;
                  }

                  return new DynamicDungeonInstance(
                     dynamicDungeonConfigFields,
                     lobbyLocation,
                     startLocation,
                     world,
                     entryPlayers,
                     difficultyName,
                     selectedLevel,
                     reservation == null ? () -> true : reservation::isValid
                  );
               } else {
                  cleanupLoadedWorld(world);
                  return null;
               }
            }
         } catch (Exception var11) {
            Logger.warn("Failed to initialize dynamic dungeon world " + instancedWorldName + ": " + var11.getMessage());
            World loaded = world != null ? world : Bukkit.getWorld(instancedWorldName);
            if (loaded == null) {
               cleanupUnloadedWorldFolder(instancedWorldName);
            } else {
               cleanupLoadedWorld(loaded);
            }

            throw new RuntimeException(var11);
         }
      }
   }

   @Override
   public boolean addNewPlayer(Player player) {
      Set<UUID> existingPlayerIds = this.getPlayers().stream().<UUID>map(OfflinePlayer::getUniqueId).collect(Collectors.toSet());
      if (!super.addNewPlayer(player)) {
         return false;
      } else {
         this.getPlayers().stream().filter(joinedPlayer -> !existingPlayerIds.contains(joinedPlayer.getUniqueId())).forEach(this::applyDynamicEntryState);
         return true;
      }
   }

   @Override
   protected boolean addEntryRoster(Player player, List<UUID> entryMemberIds, BooleanSupplier admissionAuthorization) {
      Set<UUID> existingPlayerIds = this.getPlayers().stream().<UUID>map(OfflinePlayer::getUniqueId).collect(Collectors.toSet());
      if (!super.addEntryRoster(player, entryMemberIds, admissionAuthorization)) {
         return false;
      } else {
         this.getPlayers().stream().filter(joinedPlayer -> !existingPlayerIds.contains(joinedPlayer.getUniqueId())).forEach(this::applyDynamicEntryState);
         return true;
      }
   }

   private void applyDynamicEntryState(Player player) {
      player.sendMessage(DungeonsConfig.getDynamicDungeonLevelSetMessage().replace("$level", String.valueOf(this.selectedLevel)));
      DynamicQuest.adaptPlayerQuestsToLevel(player, this.selectedLevel);
   }

   @Override
   protected String readyCheckDescription() {
      return readyCheckLevelDescription(super.readyCheckDescription(), this.selectedLevel);
   }

   private static String readyCheckLevelDescription(String dungeonDescription, int level) {
      return PartyConfig.getDungeonReadyCheckLevelFormat().replace("$dungeon", dungeonDescription).replace("$level", String.valueOf(level));
   }

   @Generated
   public int getSelectedLevel() {
      return this.selectedLevel;
   }

   private class SetBossLevelsTask extends BukkitRunnable {
      private final DynamicDungeonInstance dynamicDungeonInstance;
      private final int level;

      public SetBossLevelsTask(DynamicDungeonInstance dynamicDungeonInstance, int level) {
         this.dynamicDungeonInstance = dynamicDungeonInstance;
         this.level = level;
      }

      public void run() {
         if (!DynamicDungeonInstance.this.isInstanceRemovalScheduled()
            && DynamicDungeonInstance.this.getWorld() != null
            && DungeonInstance.getDungeonInstances().contains(this.dynamicDungeonInstance)) {
            DynamicDungeonInstance.this.getWorld()
               .getEntities()
               .forEach(
                  entity -> {
                     if (entity instanceof LivingEntity
                        && EntityTracker.getEliteMobEntity(entity) instanceof InstancedBossEntity boss
                        && boss.getDungeonInstance() == this.dynamicDungeonInstance) {
                        boss.setEntityLevel(this.level);
                     }
                  }
               );
         }
      }
   }
}
