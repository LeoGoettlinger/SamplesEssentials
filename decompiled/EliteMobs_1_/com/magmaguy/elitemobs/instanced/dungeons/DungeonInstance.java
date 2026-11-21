package com.magmaguy.elitemobs.instanced.dungeons;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.api.DungeonCompleteEvent;
import com.magmaguy.elitemobs.api.DungeonStartEvent;
import com.magmaguy.elitemobs.api.InstancedDungeonRemoveEvent;
import com.magmaguy.elitemobs.api.WorldInstanceEvent;
import com.magmaguy.elitemobs.api.WorldUninstanceEvent;
import com.magmaguy.elitemobs.api.internal.RemovalReason;
import com.magmaguy.elitemobs.config.DungeonsConfig;
import com.magmaguy.elitemobs.config.PartyConfig;
import com.magmaguy.elitemobs.config.contentpackages.ContentPackagesConfig;
import com.magmaguy.elitemobs.config.contentpackages.ContentPackagesConfigFields;
import com.magmaguy.elitemobs.config.custombosses.CustomBossesConfigFields;
import com.magmaguy.elitemobs.dungeons.EliteMobsWorld;
import com.magmaguy.elitemobs.dungeons.utility.DungeonUtils;
import com.magmaguy.elitemobs.entitytracker.EntityTracker;
import com.magmaguy.elitemobs.explosionregen.Explosion;
import com.magmaguy.elitemobs.instanced.InstancePlayerManager;
import com.magmaguy.elitemobs.instanced.InstancePlayerMovement;
import com.magmaguy.elitemobs.instanced.MatchInstance;
import com.magmaguy.elitemobs.instanced.WorldOperationQueue;
import com.magmaguy.elitemobs.magmacore.instance.InstanceProtector;
import com.magmaguy.elitemobs.magmacore.instance.WorldProtectionRules;
import com.magmaguy.elitemobs.magmacore.match.MatchPhase;
import com.magmaguy.elitemobs.magmacore.util.Logger;
import com.magmaguy.elitemobs.magmacore.util.TemporaryWorldManager;
import com.magmaguy.elitemobs.magmacore.util.WorldFolderResolver;
import com.magmaguy.elitemobs.mobconstructor.PersistentObjectHandler;
import com.magmaguy.elitemobs.mobconstructor.custombosses.CustomMusic;
import com.magmaguy.elitemobs.mobconstructor.custombosses.InstancedBossEntity;
import com.magmaguy.elitemobs.npcs.NPCEntity;
import com.magmaguy.elitemobs.parties.PartyDungeonReadyCheckManager;
import com.magmaguy.elitemobs.parties.PartyManager;
import com.magmaguy.elitemobs.playerdata.database.PlayerData;
import com.magmaguy.elitemobs.treasurechest.TreasureChest;
import com.magmaguy.elitemobs.utils.ConfigurationLocation;
import com.magmaguy.elitemobs.utils.EventCaller;
import com.magmaguy.elitemobs.utils.MapListInterpreter;
import com.magmaguy.elitemobs.utils.WorldInstantiator;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.BooleanSupplier;
import java.util.stream.Collectors;
import lombok.Generated;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

public class DungeonInstance extends MatchInstance {
   private static final Set<DungeonInstance> dungeonInstances = new HashSet<>();
   private static final Map<UUID, DungeonInstance> worldInstances = new HashMap<>();
   private static final Map<UUID, DungeonInstance.WorldRetirement> worldRetirements = new HashMap<>();
   private final List<DungeonObjective> dungeonObjectives = new ArrayList<>();
   private boolean instanceRemovalScheduled = false;
   private boolean removalEventCalled = false;
   private World world;
   private String instancedWorldName;
   private ContentPackagesConfigFields contentPackagesConfigFields;
   private List<InstancedBossEntity> instancedBossEntities = new ArrayList<>();
   private boolean instancedBossEntitiesRemoved = false;
   private int levelSync = -1;
   private String rawLevelSync = null;
   private String difficultyName = null;
   private String difficultyID = null;
   private BukkitTask initializeEntitiesTask = null;
   private BukkitTask destroyMatchTask = null;

   @Override
   protected Location participantExitLocation(Player player) {
      return this.previousLocationOrExit(player);
   }

   public static DungeonInstance forWorld(UUID worldId) {
      return worldInstances.get(worldId);
   }

   public static boolean isWorldRetiring(UUID worldId) {
      return worldRetirements.containsKey(worldId);
   }

   public static void shutdown() {
      HashSet<DungeonInstance> copy = new HashSet<>(dungeonInstances);
      copy.addAll(worldInstances.values());
      copy.forEach(DungeonInstance::removeInstance);

      for (DungeonInstance.WorldRetirement retirement : new ArrayList<>(worldRetirements.values())) {
         retirement.runNow(true);
      }
   }

   List<DungeonObjective> objectiveSnapshot() {
      return List.copyOf(this.dungeonObjectives);
   }

   public InstancedBossEntity createEncounterBoss(CustomBossesConfigFields fields, Location location) {
      if (!this.instanceRemovalScheduled
         && !this.instancedBossEntitiesRemoved
         && this.world != null
         && location != null
         && this.world.equals(location.getWorld())) {
         InstancedBossEntity boss = new InstancedBossEntity(fields, location.clone(), this);
         this.instancedBossEntities.add(boss);
         return boss;
      } else {
         return null;
      }
   }

   public DungeonInstance(
      ContentPackagesConfigFields contentPackagesConfigFields,
      Location lobbyLocation,
      Location startLocation,
      World world,
      Player player,
      String difficultyName
   ) {
      this(contentPackagesConfigFields, lobbyLocation, startLocation, world, List.of(player), difficultyName);
   }

   public DungeonInstance(
      ContentPackagesConfigFields contentPackagesConfigFields,
      Location lobbyLocation,
      Location startLocation,
      World world,
      Collection<Player> initialPlayers,
      String difficultyName
   ) {
      this(contentPackagesConfigFields, lobbyLocation, startLocation, world, initialPlayers, difficultyName, () -> true);
   }

   protected DungeonInstance(
      ContentPackagesConfigFields contentPackagesConfigFields,
      Location lobbyLocation,
      Location startLocation,
      World world,
      Collection<Player> initialPlayers,
      String difficultyName,
      BooleanSupplier admissionAuthorization
   ) {
      super(startLocation, null, contentPackagesConfigFields.getMinPlayerCount(), contentPackagesConfigFields.getMaxPlayerCount());
      if (this.cancelled) {
         cleanupLoadedWorld(world);
      } else {
         try {
            super.lobbyLocation = lobbyLocation;
            this.contentPackagesConfigFields = contentPackagesConfigFields;
            this.world = world;
            super.world = world;
            DungeonInstance previous = worldInstances.putIfAbsent(world.getUID(), this);
            if (previous != null && previous != this) {
               throw new IllegalStateException("Dungeon world already has an owner: " + world.getName());
            } else {
               for (DungeonObjective.TargetDefinition objective : contentPackagesConfigFields.instanceObjectives()) {
                  this.dungeonObjectives.add(new DungeonKillTargetObjective(this, objective));
               }

               this.world = world;
               super.world = world;
               this.instancedWorldName = world.getName();
               this.difficultyName = difficultyName;
               this.setDifficulty(difficultyName);
               super.permission = contentPackagesConfigFields.getPermission();
               if (!this.addDungeonPlayers(initialPlayers, admissionAuthorization)) {
                  if (initialPlayers.size() > 1) {
                     PartyManager.sendConfiguredMessage(initialPlayers.iterator().next(), PartyConfig.getDungeonPartyJoinFailedMessage());
                  }

                  dungeonInstances.add(this);
                  this.removeInstance();
               } else {
                  this.initializeEntitiesTask = new DungeonInstance.InitializeEntitiesTask(this, contentPackagesConfigFields, world)
                     .runTaskLater(MetadataHandler.PLUGIN, 60L);
                  dungeonInstances.add(this);
               }
            }
         } catch (RuntimeException var11) {
            this.deregisterFailedConstruction();
            throw var11;
         }
      }
   }

   protected final void deregisterFailedConstruction() {
      this.cancelScheduledTasks();
      this.cancelInitializeEntitiesTask();
      this.cancelDestroyMatchTask();
      this.cancelRemoveInstanceTask();
      InstancePlayerManager.rollbackAdmissions(new HashSet<>(this.participants), this);
      this.players.clear();
      this.spectators.clear();
      this.participants.clear();
      this.playerLives.clear();
      this.previousPlayerLocations.clear();

      for (DungeonObjective dungeonObjective : this.dungeonObjectives) {
         if (dungeonObjective != null) {
            dungeonObjective.unregister();
         }
      }

      this.retireInstance();
      dungeonInstances.remove(this);
   }

   public static void setupInstancedDungeon(Player player, String instancedDungeonConfigFieldsString, String difficultyName) {
      ContentPackagesConfigFields instancedDungeonsConfigFields = ContentPackagesConfig.getDungeonPackages().get(instancedDungeonConfigFieldsString);
      if (instancedDungeonsConfigFields == null) {
         player.sendMessage(DungeonsConfig.getDungeonDataFailedMessage().replace("$dungeon", instancedDungeonConfigFieldsString));
      } else if (instancedDungeonsConfigFields.getPermission() != null
         && !instancedDungeonsConfigFields.getPermission().isEmpty()
         && !player.hasPermission(instancedDungeonsConfigFields.getPermission())) {
         player.sendMessage(DungeonsConfig.getDungeonNoPermissionMessage());
      } else {
         List<UUID> entryMemberIds = instancedDungeonsConfigFields.isEnchantmentChallenge()
            ? List.of(player.getUniqueId())
            : prepareDungeonEntryRoster(player, instancedDungeonsConfigFields);
         if (!entryMemberIds.isEmpty()) {
            PartyDungeonReadyCheckManager.request(
               player,
               entryMemberIds,
               readyCheckDescription(instancedDungeonsConfigFields, difficultyName),
               reservation -> launchInstancedDungeon(player, instancedDungeonsConfigFields, entryMemberIds, difficultyName, reservation)
            );
         }
      }
   }

   private static boolean launchInstancedDungeon(
      Player player,
      ContentPackagesConfigFields instancedDungeonsConfigFields,
      List<UUID> entryMemberIds,
      String difficultyName,
      PartyDungeonReadyCheckManager.LaunchReservation reservation
   ) {
      if (!reservation.isValid()) {
         return false;
      } else if (resolveDungeonEntryRoster(player, entryMemberIds, instancedDungeonsConfigFields, null).isEmpty()) {
         return false;
      } else {
         String instancedWorldName = WorldInstantiator.getNewWorldName(instancedDungeonsConfigFields.getWorldName());
         if (!launchEvent(instancedDungeonsConfigFields, instancedWorldName, player)) {
            return false;
         } else if (!reservation.isValid()) {
            return false;
         } else {
            PendingWorldCopy copy = new PendingWorldCopy();
            WorldOperationQueue.queueOperation(
               player, () -> !reservation.isValid() ? true : copy.copy(() -> cloneWorldFiles(instancedDungeonsConfigFields, instancedWorldName)), () -> {
                  if (reservation.isValid() && copy.transferToInitializer()) {
                     initializeInstancedWorld(instancedDungeonsConfigFields, instancedWorldName, player, entryMemberIds, difficultyName, reservation);
                  }
               }, instancedDungeonsConfigFields.getName(), () -> {
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

   protected static boolean launchEvent(ContentPackagesConfigFields instancedDungeonsConfigFields, String instancedWordName, Player player) {
      try {
         instancedDungeonsConfigFields.prepareInstanceDefinition();
      } catch (IllegalArgumentException var4) {
         Logger.warn("Cannot launch dungeon: " + var4.getMessage());
         player.sendMessage(DungeonsConfig.getDungeonCancelledEventMessage());
         return false;
      }

      WorldInstanceEvent worldInstanceEvent = new WorldInstanceEvent(
         instancedDungeonsConfigFields.getWorldName(), instancedWordName, instancedDungeonsConfigFields
      );
      new EventCaller(worldInstanceEvent);
      if (worldInstanceEvent.isCancelled()) {
         player.sendMessage(DungeonsConfig.getDungeonCancelledEventMessage());
         return false;
      } else {
         return true;
      }
   }

   protected static File cloneWorldFiles(ContentPackagesConfigFields instancedDungeonsConfigFields, String instancedWordName) {
      instancedDungeonsConfigFields.prepareInstanceDefinition();
      return WorldInstantiator.cloneWorld(
         instancedDungeonsConfigFields.getWorldName(),
         instancedWordName,
         instancedDungeonsConfigFields.getDungeonConfigFolderName(),
         instancedDungeonsConfigFields.getEnvironment()
      );
   }

   protected static DungeonInstance initializeInstancedWorld(
      ContentPackagesConfigFields instancedDungeonsConfigFields, String instancedWordName, Player player, String difficultyName
   ) {
      return initializeInstancedWorld(instancedDungeonsConfigFields, instancedWordName, player, List.of(player.getUniqueId()), difficultyName);
   }

   protected static DungeonInstance initializeInstancedWorld(
      ContentPackagesConfigFields instancedDungeonsConfigFields, String instancedWordName, Player player, List<UUID> entryMemberIds, String difficultyName
   ) {
      return initializeInstancedWorld(instancedDungeonsConfigFields, instancedWordName, player, entryMemberIds, difficultyName, null);
   }

   protected static DungeonInstance initializeInstancedWorld(
      ContentPackagesConfigFields instancedDungeonsConfigFields,
      String instancedWordName,
      Player player,
      List<UUID> entryMemberIds,
      String difficultyName,
      PartyDungeonReadyCheckManager.LaunchReservation reservation
   ) {
      if (reservation != null && !reservation.isValid()) {
         cleanupUnloadedWorldFolder(instancedWordName);
         return null;
      } else {
         World world = null;

         try {
            instancedDungeonsConfigFields.prepareInstanceDefinition();
            world = DungeonUtils.loadWorld(instancedWordName, instancedDungeonsConfigFields.getEnvironment(), instancedDungeonsConfigFields);
            if (world == null) {
               player.sendMessage(DungeonsConfig.getDungeonWorldLoadFailedMessage());
               cleanupUnloadedWorldFolder(instancedWordName);
               return null;
            } else {
               List<Player> entryPlayers = resolveDungeonEntryRoster(player, entryMemberIds, instancedDungeonsConfigFields, null);
               if (!entryPlayers.isEmpty() && (reservation == null || reservation.isValid())) {
                  if (instancedDungeonsConfigFields.getSong() != null) {
                     new CustomMusic(instancedDungeonsConfigFields.getSong(), instancedDungeonsConfigFields, world);
                  }

                  Location startLocation = instancedDungeonsConfigFields.instanceStartLocation(world);
                  Location lobbyLocation = ConfigurationLocation.serialize(instancedDungeonsConfigFields.getTeleportLocationString());
                  if (lobbyLocation != null) {
                     lobbyLocation.setWorld(world);
                  } else {
                     lobbyLocation = startLocation;
                  }

                  return (DungeonInstance)(!instancedDungeonsConfigFields.isEnchantmentChallenge()
                     ? new DungeonInstance(
                        instancedDungeonsConfigFields,
                        lobbyLocation,
                        startLocation,
                        world,
                        entryPlayers,
                        difficultyName,
                        reservation == null ? () -> true : reservation::isValid
                     )
                     : new EnchantmentDungeonInstance(instancedDungeonsConfigFields, lobbyLocation, startLocation, world, entryPlayers.get(0), difficultyName));
               } else {
                  cleanupLoadedWorld(world);
                  return null;
               }
            }
         } catch (Exception var10) {
            Logger.warn("Failed to initialize instanced dungeon world " + instancedWordName + ": " + var10.getMessage());
            World loaded = world != null ? world : Bukkit.getWorld(instancedWordName);
            if (loaded == null) {
               cleanupUnloadedWorldFolder(instancedWordName);
            } else {
               cleanupLoadedWorld(loaded);
            }

            throw new RuntimeException(var10);
         }
      }
   }

   @Override
   public boolean addNewPlayer(Player player) {
      return this.addDungeonPlayers(List.of(player));
   }

   public boolean requestPartyEntry(Player player) {
      List<UUID> entryMemberIds = PartyManager.getDungeonEntryMemberIds(player);
      return this.resolveAdmissionRoster(player, entryMemberIds).isEmpty()
         ? false
         : PartyDungeonReadyCheckManager.request(player, entryMemberIds, this.readyCheckDescription(), reservation -> {
            boolean var4;
            try {
               var4 = this.addEntryRoster(player, entryMemberIds, reservation::isValid);
            } finally {
               reservation.release();
            }

            return var4;
         });
   }

   protected boolean addEntryRoster(Player initiator, List<UUID> entryMemberIds, BooleanSupplier admissionAuthorization) {
      if (!admissionAuthorization.getAsBoolean()) {
         return false;
      } else {
         List<Player> entryPlayers = this.resolveAdmissionRoster(initiator, entryMemberIds);
         if (!entryPlayers.isEmpty() && admissionAuthorization.getAsBoolean()) {
            boolean added = this.addDungeonPlayers(entryPlayers, admissionAuthorization);
            if (!added && entryMemberIds.size() > 1) {
               PartyManager.sendConfiguredMessage(initiator, PartyConfig.getDungeonPartyJoinFailedMessage());
            }

            return added;
         } else {
            return false;
         }
      }
   }

   private List<Player> resolveAdmissionRoster(Player player, List<UUID> entryMemberIds) {
      List<Player> entryPlayers = resolveDungeonEntryRoster(player, entryMemberIds, this.contentPackagesConfigFields, this);
      if (entryPlayers.isEmpty()) {
         return List.of();
      } else {
         long playersNeedingAdmission = entryPlayers.stream().filter(candidate -> !this.players.contains(candidate)).count();
         int availableSlots = this.maxPlayers - this.players.size();
         if (playersNeedingAdmission <= availableSlots) {
            return entryPlayers;
         } else {
            PartyManager.sendConfiguredMessage(
               player,
               PartyConfig.getDungeonPartyTooLargeMessage()
                  .replace("$count", String.valueOf(playersNeedingAdmission))
                  .replace("$max", String.valueOf(Math.max(0, availableSlots)))
            );
            return List.of();
         }
      }
   }

   protected static String readyCheckDescription(ContentPackagesConfigFields configFields, String difficultyName) {
      String dungeonName = configFields.getName();
      if (dungeonName == null || dungeonName.isBlank()) {
         dungeonName = configFields.getFilename();
      }

      return difficultyName != null && !difficultyName.isBlank() ? dungeonName + " - " + difficultyName : dungeonName;
   }

   protected String readyCheckDescription() {
      return readyCheckDescription(this.contentPackagesConfigFields, this.difficultyName);
   }

   private boolean addDungeonPlayers(Collection<Player> entryPlayers) {
      return this.addDungeonPlayers(entryPlayers, () -> true);
   }

   private boolean addDungeonPlayers(Collection<Player> entryPlayers, BooleanSupplier admissionAuthorization) {
      Set<UUID> existingPlayerIds = this.players.stream().<UUID>map(OfflinePlayer::getUniqueId).collect(Collectors.toSet());
      if (!InstancePlayerManager.addNewPlayers(entryPlayers, this, admissionAuthorization)) {
         return false;
      } else {
         for (Player entryPlayer : entryPlayers) {
            if (!existingPlayerIds.contains(entryPlayer.getUniqueId()) && this.levelSync > 0) {
               entryPlayer.sendMessage(
                  DungeonsConfig.getDungeonDifficultyMessage()
                     .replace("$difficulty", this.difficultyName)
                     .replace("$levelSync", String.valueOf(this.levelSync))
               );
            }
         }

         return true;
      }
   }

   protected static List<UUID> prepareDungeonEntryRoster(Player initiator, ContentPackagesConfigFields configFields) {
      List<UUID> memberIds = PartyManager.getDungeonEntryMemberIds(initiator);
      if (memberIds.size() > configFields.getMaxPlayerCount()) {
         PartyManager.sendConfiguredMessage(
            initiator,
            PartyConfig.getDungeonPartyTooLargeMessage()
               .replace("$count", String.valueOf(memberIds.size()))
               .replace("$max", String.valueOf(configFields.getMaxPlayerCount()))
         );
         return List.of();
      } else {
         return resolveDungeonEntryRoster(initiator, memberIds, configFields, null).isEmpty() ? List.of() : memberIds;
      }
   }

   protected static List<Player> resolveDungeonEntryRoster(
      Player initiator, Collection<UUID> memberIds, ContentPackagesConfigFields configFields, DungeonInstance allowedInstance
   ) {
      if (initiator != null && initiator.isOnline()) {
         boolean personalEnchantmentChallenge = configFields.isEnchantmentChallenge() && memberIds.size() == 1 && memberIds.contains(initiator.getUniqueId());
         if (!personalEnchantmentChallenge && !PartyManager.isDungeonEntryRosterCurrent(initiator, memberIds)) {
            PartyManager.sendConfiguredMessage(initiator, PartyConfig.getDungeonPartyChangedMessage());
            return List.of();
         } else {
            List<Player> entryPlayers = new ArrayList<>();

            for (UUID memberId : memberIds) {
               Player member = Bukkit.getPlayer(memberId);
               String memberName = member == null ? Bukkit.getOfflinePlayer(memberId).getName() : member.getName();
               if (member != null && member.isOnline() && member.isValid() && PlayerData.isInMemory(memberId)) {
                  MatchInstance currentInstance = PlayerData.getMatchInstance(member);
                  MatchInstance indexedInstance = MatchInstance.getAnyPlayerInstance(member);
                  if ((currentInstance == null || currentInstance == allowedInstance) && (indexedInstance == null || indexedInstance == allowedInstance)) {
                     String permission = configFields.getPermission();
                     if (permission != null && !permission.isEmpty() && !member.hasPermission(permission)) {
                        PartyManager.sendConfiguredMessage(initiator, PartyConfig.getDungeonPartyNoPermissionMessage().replace("$player", member.getName()));
                        return List.of();
                     }

                     entryPlayers.add(member);
                     continue;
                  }

                  PartyManager.sendConfiguredMessage(initiator, PartyConfig.getDungeonPartyInInstanceMessage().replace("$player", member.getName()));
                  return List.of();
               }

               PartyManager.sendConfiguredMessage(
                  initiator,
                  PartyConfig.getDungeonPartyUnavailableMessage().replace("$player", memberName == null ? PartyConfig.getUnknownPlayerName() : memberName)
               );
               return List.of();
            }

            return List.copyOf(entryPlayers);
         }
      } else {
         return List.of();
      }
   }

   @Override
   protected void startMatch() {
      this.updateBossHealth();
      super.startMatch();
      new EventCaller(new DungeonStartEvent(this));
   }

   private void updateBossHealth() {
      this.instancedBossEntities.forEach(instancedBossEntity -> instancedBossEntity.setNormalizedMaxHealth(this.players.size()));
   }

   public boolean checkCompletionStatus() {
      if (!super.state.equals(MatchInstance.InstancedRegionState.ONGOING)) {
         return false;
      } else {
         for (DungeonObjective dungeonObjective : this.dungeonObjectives) {
            if (!dungeonObjective.isCompleted()) {
               return false;
            }
         }

         new EventCaller(new DungeonCompleteEvent(this));
         this.victory();
         return true;
      }
   }

   @Override
   public void endMatch() {
      super.endMatch();
      if (this.players.isEmpty()) {
         this.destroyMatch();
      } else {
         this.announce(DungeonsConfig.getInstancedDungeonCompleteMessage());
         this.scheduleDelayedDestroy(2400L);
      }
   }

   protected void scheduleDelayedDestroy(long delayTicks) {
      this.cancelDestroyMatchTask();
      this.destroyMatchTask = new DungeonInstance.DestroyMatchTask().runTaskLater(MetadataHandler.PLUGIN, delayTicks);
   }

   @Override
   public void destroyMatch() {
      super.destroyMatch();
      this.removeInstance();
   }

   public void removeInstance() {
      if (!this.isDestroyingMatch() && !this.hasMatchDestroyEventFired()) {
         super.destroyMatch();
      }

      boolean immediateRemoval = MetadataHandler.shutdownRequested;
      this.cancelScheduledTasks();
      this.cancelInitializeEntitiesTask();
      this.cancelDestroyMatchTask();
      if (this.instanceRemovalScheduled) {
         if (!immediateRemoval) {
            Logger.warn("removeInstance() called but already scheduled for " + (this.world != null ? this.world.getName() : "null world"));
            return;
         }

         this.cancelRemoveInstanceTask();
      } else {
         this.instanceRemovalScheduled = true;

         for (DungeonObjective dungeonObjective : this.dungeonObjectives) {
            if (dungeonObjective != null) {
               dungeonObjective.unregister();
            }
         }

         this.participants.forEach(player -> player.sendMessage(DungeonsConfig.getInstancedDungeonClosingInstanceMessage()));
         HashSet<Player> participants = new HashSet<>(this.participants);
         participants.forEach(this::removeAnyKind);
         this.retireInstance();
      }

      this.removeInstancedBossEntities(RemovalReason.WORLD_UNLOAD);
      if (this.world == null) {
         Logger.warn("Instanced dungeon's world was already unloaded before removing the entities in it! This shouldn't happen, but doesn't break anything.");
         this.cleanupInstanceReferences();
      } else {
         this.world.getEntities().forEach(entity -> EntityTracker.unregister(entity, RemovalReason.WORLD_UNLOAD));
         DungeonInstance.WorldRetirement retirement = worldRetirements.computeIfAbsent(
            this.world.getUID(), ignored -> new DungeonInstance.WorldRetirement(this.world, this)
         );
         if (immediateRemoval) {
            retirement.runNow(true);
         } else {
            retirement.schedule(600L);
         }
      }
   }

   private void cancelInitializeEntitiesTask() {
      if (this.initializeEntitiesTask != null) {
         this.initializeEntitiesTask.cancel();
         this.initializeEntitiesTask = null;
      }
   }

   private void cancelDestroyMatchTask() {
      if (this.destroyMatchTask != null) {
         this.destroyMatchTask.cancel();
         this.destroyMatchTask = null;
      }
   }

   private void cancelRemoveInstanceTask() {
      DungeonInstance.WorldRetirement retirement = this.world == null ? null : worldRetirements.get(this.world.getUID());
      if (retirement != null) {
         retirement.cancelPending();
      }
   }

   private void removeInstancedBossEntities(RemovalReason removalReason) {
      if (!this.instancedBossEntitiesRemoved) {
         this.instancedBossEntitiesRemoved = true;

         for (InstancedBossEntity instancedBossEntity : new ArrayList<>(this.instancedBossEntities)) {
            if (instancedBossEntity != null) {
               instancedBossEntity.remove(removalReason);
            }
         }
      }
   }

   protected boolean isInstanceRemovalScheduled() {
      return this.instanceRemovalScheduled;
   }

   @Override
   public boolean isDefunct() {
      return super.isDefunct() || this.instanceRemovalScheduled || this.world == null;
   }

   protected static void cleanupUnloadedWorldFolder(String instancedWorldName) {
      if (Bukkit.getWorld(instancedWorldName) != null) {
         Logger.warn("Cannot clean up instanced dungeon world folder " + instancedWorldName + " while the world is loaded.");
      } else {
         WorldFolderResolver.deleteAllLayouts(instancedWorldName);
         Logger.info("Cleaned up unloaded instanced dungeon world folder " + instancedWorldName);
      }
   }

   protected static void cleanupLoadedWorld(World world) {
      if (world != null) {
         DungeonInstance.WorldRetirement retirement = worldRetirements.computeIfAbsent(
            world.getUID(), ignored -> new DungeonInstance.WorldRetirement(world, worldInstances.get(world.getUID()))
         );
         retirement.runNow(MetadataHandler.shutdownRequested);
      }
   }

   private static void cleanupWorldScopedState(World worldToDelete) {
      Explosion.discardForWorld(worldToDelete.getUID());
      UUID worldUUID = worldToDelete.getUID();
      EliteMobsWorld.destroy(worldUUID);
      InstanceProtector.removeProtectedWorld(worldToDelete);
      CustomMusic.removeDungeonMusic(worldUUID);
      TreasureChest.removeInstancedTreasureChests(worldToDelete);
      PersistentObjectHandler.removeForWorld(worldUUID);
   }

   public String getResolvedDifficultyID() {
      return this.contentPackagesConfigFields.getDifficultyResolver().resolveSelected(this.difficultyID);
   }

   public boolean matchesDifficulty(List<String> filter, String source) {
      return this.contentPackagesConfigFields.getDifficultyResolver().matches(filter, this.difficultyID, source);
   }

   private void setDifficulty(String difficultyName) {
      if (difficultyName != null) {
         if (this.contentPackagesConfigFields.getDifficulties() != null && !this.contentPackagesConfigFields.getDifficulties().isEmpty()) {
            Map difficulty = null;

            for (Map difficultyMap : this.contentPackagesConfigFields.getDifficulties()) {
               if (difficultyMap.get("name") != null && difficultyMap.get("name").equals(difficultyName)) {
                  difficulty = difficultyMap;
                  break;
               }
            }

            if (difficulty == null) {
               Logger.warn("Failed to set difficulty " + difficultyName + " for instanced dungeon " + this.contentPackagesConfigFields.getFilename());
            } else {
               if (difficulty.get("levelSync") != null) {
                  try {
                     this.rawLevelSync = MapListInterpreter.parseString(
                        "levelSync", difficulty.get("levelSync"), this.contentPackagesConfigFields.getFilename()
                     );
                     this.levelSync = this.parseLevelSync(this.rawLevelSync, this.contentPackagesConfigFields.getContentLevel());
                  } catch (Exception var5) {
                     Logger.warn(
                        "Incorrect level sync entry for dungeon "
                           + this.contentPackagesConfigFields.getFilename()
                           + " ! Value: "
                           + this.rawLevelSync
                           + " . No level sync will be applied!"
                     );
                     this.levelSync = 0;
                  }
               } else {
                  this.levelSync = 0;
               }

               if (difficulty.get("id") != null) {
                  this.difficultyID = MapListInterpreter.parseString("id", difficulty.get("id"), this.contentPackagesConfigFields.getFilename());
               }
            }
         }
      }
   }

   protected int parseLevelSync(String rawValue, int contentLevel) {
      if (rawValue != null && !rawValue.isEmpty()) {
         rawValue = rawValue.trim();
         if (!rawValue.startsWith("+") && !rawValue.startsWith("-")) {
            try {
               return Integer.parseInt(rawValue);
            } catch (NumberFormatException var5) {
               Logger.warn("Invalid level sync value: " + rawValue);
               return 0;
            }
         } else {
            int offset;
            try {
               offset = Integer.parseInt(rawValue);
            } catch (NumberFormatException var6) {
               Logger.warn("Invalid relative level sync value: " + rawValue);
               return 0;
            }

            return contentLevel < 0 ? 0 : Math.max(1, contentLevel + offset);
         }
      } else {
         return 0;
      }
   }

   protected void recalculateLevelSyncForDynamicLevel(int dynamicLevel) {
      if (this.rawLevelSync != null && !this.rawLevelSync.isEmpty()) {
         String trimmed = this.rawLevelSync.trim();
         if (trimmed.startsWith("+") || trimmed.startsWith("-")) {
            try {
               int offset = Integer.parseInt(trimmed);
               this.levelSync = Math.max(1, dynamicLevel + offset);
            } catch (NumberFormatException var4) {
               this.levelSync = 0;
            }
         }
      } else {
         this.levelSync = 0;
      }
   }

   protected boolean isRelativeLevelSync() {
      if (this.rawLevelSync != null && !this.rawLevelSync.isEmpty()) {
         String trimmed = this.rawLevelSync.trim();
         return trimmed.startsWith("+") || trimmed.startsWith("-");
      } else {
         return false;
      }
   }

   @Override
   protected boolean isInRegion(Location location) {
      return location.getWorld().equals(this.startLocation.getWorld());
   }

   @Override
   protected boolean guardsEntryDuring(MatchPhase phase) {
      return phase != MatchPhase.DESTROYED;
   }

   private void cleanupInstanceReferences() {
      this.removeInstancedBossEntities(RemovalReason.WORLD_UNLOAD);
      this.cancelScheduledTasks();
      this.cancelInitializeEntitiesTask();
      this.cancelDestroyMatchTask();
      this.cancelRemoveInstanceTask();
      this.retireInstance();
      dungeonInstances.remove(this);
      if (this.world != null) {
         worldInstances.remove(this.world.getUID(), this);
      }

      this.players.clear();
      this.spectators.clear();
      this.participants.clear();
      this.playerLives.clear();
      this.previousPlayerLocations.clear();
      new ArrayList<>(this.deathBanners.values()).forEach(deathLocation -> {
         try {
            deathLocation.clear(false);
         } catch (Exception var2) {
            Logger.warn("Failed to clear an instanced dungeon death banner during cleanup: " + var2.getMessage());
         }
      });
      this.deathBanners.clear();
      this.dungeonObjectives.clear();
      this.instancedBossEntities.clear();
      super.world = null;
      this.world = null;
      this.instancedWorldName = null;
      this.startLocation = null;
      this.lobbyLocation = null;
      this.exitLocation = null;
      this.contentPackagesConfigFields = null;
      this.difficultyName = null;
      this.rawLevelSync = null;
   }

   @Generated
   public static Set<DungeonInstance> getDungeonInstances() {
      return dungeonInstances;
   }

   @Generated
   public World getWorld() {
      return this.world;
   }

   @Generated
   public String getInstancedWorldName() {
      return this.instancedWorldName;
   }

   @Generated
   public ContentPackagesConfigFields getContentPackagesConfigFields() {
      return this.contentPackagesConfigFields;
   }

   @Generated
   public int getLevelSync() {
      return this.levelSync;
   }

   @Generated
   public String getDifficultyID() {
      return this.difficultyID;
   }

   private class DestroyMatchTask extends BukkitRunnable {
      public void run() {
         DungeonInstance.this.destroyMatchTask = null;
         DungeonInstance.this.destroyMatch();
      }
   }

   private class InitializeEntitiesTask extends BukkitRunnable {
      private final DungeonInstance dungeonInstance;
      private final ContentPackagesConfigFields contentPackagesConfigFields;
      private final World world;

      public InitializeEntitiesTask(DungeonInstance dungeonInstance, ContentPackagesConfigFields contentPackagesConfigFields, World world) {
         this.dungeonInstance = dungeonInstance;
         this.contentPackagesConfigFields = contentPackagesConfigFields;
         this.world = world;
      }

      public void run() {
         DungeonInstance.this.initializeEntitiesTask = null;
         if (!DungeonInstance.this.isInstanceRemovalScheduled() && this.world != null && DungeonInstance.dungeonInstances.contains(this.dungeonInstance)) {
            DungeonInstance.this.instancedBossEntities = InstancedBossEntity.initializeInstancedBosses(
               this.contentPackagesConfigFields.getWorldName(), this.world, DungeonInstance.this.players.size(), this.dungeonInstance
            );
            NPCEntity.initializeInstancedNPCs(
               this.contentPackagesConfigFields.getWorldName(), this.world, DungeonInstance.this.players.size(), this.dungeonInstance
            );
            TreasureChest.initializeInstancedTreasureChests(this.contentPackagesConfigFields.getWorldName(), this.world);
         }
      }
   }

   private static final class WorldRetirement implements Runnable {
      private final World world;
      private final DungeonInstance owner;
      private BukkitTask pending;
      private int attempts;
      private boolean immediate;
      private boolean running;

      private WorldRetirement(World world, DungeonInstance owner) {
         this.world = world;
         this.owner = owner;
      }

      private void cancelPending() {
         if (this.pending != null) {
            this.pending.cancel();
            this.pending = null;
         }
      }

      private void schedule(long delay) {
         if (this.pending == null) {
            this.pending = Bukkit.getScheduler().runTaskLater(MetadataHandler.PLUGIN, this, delay);
         }
      }

      private void runNow(boolean immediate) {
         this.cancelPending();
         this.immediate |= immediate;
         this.run();
      }

      @Override
      public void run() {
         this.pending = null;
         if (!this.running && DungeonInstance.worldRetirements.get(this.world.getUID()) == this) {
            this.running = true;
            this.attempts++;

            try {
               try {
                  if (Bukkit.getWorld(this.world.getUID()) != null) {
                     this.evacuatePlayers();
                     if (!this.world.getPlayers().isEmpty()) {
                        this.retry();
                        return;
                     }

                     WorldProtectionRules protection = InstanceProtector.getRules(this.world);
                     boolean unloaded = this.immediate
                        ? TemporaryWorldManager.trySyncPermanentlyDeleteWorld(this.world)
                        : TemporaryWorldManager.tryPermanentlyDeleteWorld(this.world);
                     if (!unloaded) {
                        if (protection != null) {
                           InstanceProtector.addProtectedWorld(this.world, protection);
                        }

                        PersistentObjectHandler.loadWorld(this.world);
                        this.retry();
                        return;
                     }
                  }

                  DungeonInstance.cleanupWorldScopedState(this.world);
                  if (this.owner != null) {
                     if (!this.owner.removalEventCalled) {
                        this.owner.removalEventCalled = true;
                        new EventCaller(new InstancedDungeonRemoveEvent(this.owner));
                     }

                     if (this.owner.contentPackagesConfigFields != null) {
                        new EventCaller(new WorldUninstanceEvent(this.owner.contentPackagesConfigFields, this.world.getName()));
                     }

                     this.owner.cleanupInstanceReferences();
                  }

                  DungeonInstance.worldRetirements.remove(this.world.getUID(), this);
               } catch (RuntimeException var6) {
                  Logger.warn("Could not retire dungeon world " + this.world.getName() + ": " + var6.getMessage());
                  this.retry();
               }
            } finally {
               this.running = false;
            }
         }
      }

      private void retry() {
         if (!this.immediate && !MetadataHandler.shutdownRequested && MetadataHandler.PLUGIN.isEnabled()) {
            if (this.attempts == 1 || this.attempts == 12) {
               Logger.warn("Dungeon world " + this.world.getName() + " is still loaded. Protection and cleanup ownership were retained; cleanup will retry.");
            }

            this.schedule(this.attempts < 12 ? 100L : 1200L);
         } else {
            Logger.warn("Dungeon world " + this.world.getName() + " could not be unloaded during shutdown. Its ownership was retained; no files were deleted.");
         }
      }

      private void evacuatePlayers() {
         Location fallback = null;

         for (World other : Bukkit.getWorlds()) {
            if (!other.equals(this.world) && !DungeonInstance.isWorldRetiring(other.getUID())) {
               fallback = other.getSpawnLocation();
               break;
            }
         }

         for (Player player : new ArrayList(this.world.getPlayers())) {
            Location destination = this.owner == null ? null : this.owner.previousPlayerLocations.get(player);
            if (!this.safeExit(destination)) {
               destination = this.owner == null ? null : this.owner.exitLocation;
            }

            if (!this.safeExit(destination)) {
               destination = fallback;
            }

            if (destination != null) {
               try {
                  if (player.getGameMode() == GameMode.SPECTATOR) {
                     player.setSpectatorTarget(null);
                  }

                  if (this.owner == null) {
                     player.teleport(destination);
                  } else {
                     InstancePlayerMovement.teleportForMatch(player, destination, this.owner, false);
                  }
               } catch (RuntimeException var6) {
                  Logger.warn("Could not evacuate " + player.getName() + " from " + this.world.getName() + ": " + var6.getMessage());
               }
            }
         }
      }

      private boolean safeExit(Location location) {
         return location != null
            && location.getWorld() != null
            && !this.world.equals(location.getWorld())
            && Bukkit.getWorld(location.getWorld().getUID()) != null
            && !DungeonInstance.isWorldRetiring(location.getWorld().getUID());
      }
   }
}
