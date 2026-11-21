package com.magmaguy.elitemobs.mobconstructor.custombosses;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.api.EliteMobEnterCombatEvent;
import com.magmaguy.elitemobs.api.EliteMobExitCombatEvent;
import com.magmaguy.elitemobs.api.EliteMobSpawnEvent;
import com.magmaguy.elitemobs.api.internal.RemovalReason;
import com.magmaguy.elitemobs.config.DefaultConfig;
import com.magmaguy.elitemobs.config.EventsConfig;
import com.magmaguy.elitemobs.config.MobCombatSettingsConfig;
import com.magmaguy.elitemobs.config.custombosses.CustomBossesConfig;
import com.magmaguy.elitemobs.config.custombosses.CustomBossesConfigFields;
import com.magmaguy.elitemobs.dungeons.EMPackage;
import com.magmaguy.elitemobs.entitytracker.EntityTracker;
import com.magmaguy.elitemobs.magmacore.util.AttributeManager;
import com.magmaguy.elitemobs.magmacore.util.ChatColorConverter;
import com.magmaguy.elitemobs.magmacore.util.ChunkLocationChecker;
import com.magmaguy.elitemobs.magmacore.util.Logger;
import com.magmaguy.elitemobs.mobconstructor.CustomSpawn;
import com.magmaguy.elitemobs.mobconstructor.EliteEntity;
import com.magmaguy.elitemobs.mobconstructor.MobLevelPlaceholderFormatter;
import com.magmaguy.elitemobs.mobconstructor.PersistentMovingEntity;
import com.magmaguy.elitemobs.mobconstructor.PersistentObject;
import com.magmaguy.elitemobs.mobconstructor.PersistentObjectHandler;
import com.magmaguy.elitemobs.mobconstructor.custombosses.transitiveblocks.TransitiveBlock;
import com.magmaguy.elitemobs.mobconstructor.mobdata.aggressivemobs.EliteMobProperties;
import com.magmaguy.elitemobs.pathfinding.patrol.PatrolService;
import com.magmaguy.elitemobs.playerdata.ElitePlayerInventory;
import com.magmaguy.elitemobs.powers.meta.CustomSummonPower;
import com.magmaguy.elitemobs.powers.meta.ElitePower;
import com.magmaguy.elitemobs.thirdparty.custommodels.CustomModel;
import com.magmaguy.elitemobs.thirdparty.discordsrv.DiscordSRVAnnouncement;
import com.magmaguy.elitemobs.thirdparty.libsdisguises.DisguiseEntity;
import com.magmaguy.elitemobs.utils.CommandRunner;
import com.magmaguy.elitemobs.utils.EventCaller;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.logging.Level;
import javax.annotation.Nullable;
import lombok.Generated;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

public class CustomBossEntity extends EliteEntity implements Listener, PersistentObject, PersistentMovingEntity {
   public static Set<CustomBossEntity> dynamicLevelBossEntities = new HashSet<>();
   protected static HashSet<CustomBossEntity> trackableCustomBosses = new HashSet<>();
   private static BukkitTask dynamicLevelUpdater = null;
   private final List<BukkitTask> globalReinforcements = new ArrayList<>();
   BukkitTask mountAttachmentTask;
   protected CustomBossesConfigFields customBossesConfigFields;
   protected CustomBossEntity customBossMount = null;
   protected LivingEntity livingEntityMount = null;
   protected CustomBossEntity mount;
   protected PersistentObjectHandler persistentObjectHandler = null;
   protected Location persistentLocation;
   protected CustomBossTrail customBossTrail;
   private final BossTrackingLifecycle<BossTrackingBar> bossTrackingLifecycle;
   protected Integer escapeMechanism;
   protected PhaseBossEntity phaseBossEntity = null;
   protected String worldName;
   private long lastTick = 0L;
   private int attemptsCounter = 1;
   private Location respawnOverrideLocation = null;
   private EMPackage emPackage = null;
   private CustomSpawn customSpawn = null;
   private int existsFailureCount = 0;
   private boolean isMount = false;
   private CustomModel customModel = null;
   private boolean normalizedCombat;
   private boolean scaledCombat;
   private CustomMusic bossMusic = null;
   private double followDistance;
   private double movementSpeedAttribute;
   private boolean dynamicLevel = false;

   public CustomBossEntity(CustomBossesConfigFields customBossesConfigFields) {
      this.bossTrackingLifecycle = new BossTrackingLifecycle<>(
         () -> trackableCustomBosses.add(this), () -> trackableCustomBosses.remove(this), () -> new BossTrackingBar(this)
      );
      this.customBossesConfigFields = Objects.requireNonNull(customBossesConfigFields);
      if (customBossesConfigFields.getPhases() != null) {
         this.phaseBossEntity = new PhaseBossEntity(this);
      }

      this.setCustomBossesConfigFields(customBossesConfigFields);
      if (customBossesConfigFields.getSong() != null) {
         this.bossMusic = new CustomMusic(customBossesConfigFields.getSong(), this);
      }

      super.setPersistent(customBossesConfigFields.isPersistent());
      if (this instanceof RegionalBossEntity) {
         super.bypassesProtections = true;
      }

      this.emPackage = EMPackage.getContent(customBossesConfigFields.getFilename());
      this.followDistance = customBossesConfigFields.getFollowDistance().intValue();
      this.movementSpeedAttribute = customBossesConfigFields.getMovementSpeedAttribute() == null ? 0.0 : customBossesConfigFields.getMovementSpeedAttribute();
   }

   @Nullable
   public static CustomBossEntity createCustomBossEntity(String filename) {
      CustomBossesConfigFields customBossesConfigFields = CustomBossesConfig.getCustomBoss(filename);
      return customBossesConfigFields == null ? null : new CustomBossEntity(customBossesConfigFields);
   }

   public static void addToUpdatingDynamicLevels(CustomBossEntity customBossEntity) {
      if (customBossEntity.dynamicLevel) {
         dynamicLevelBossEntities.add(customBossEntity);
      }
   }

   public static void startUpdatingDynamicLevels() {
      dynamicLevelUpdater = (new BukkitRunnable() {
         public void run() {
            CustomBossEntity.DynamicLevelPass pass = new CustomBossEntity.DynamicLevelPass();
            Iterator<CustomBossEntity> iterator = CustomBossEntity.dynamicLevelBossEntities.iterator();

            while (iterator.hasNext()) {
               CustomBossEntity customBossEntity = iterator.next();
               if (!customBossEntity.isValid()) {
                  iterator.remove();
               } else {
                  int currentLevel = customBossEntity.getLevel();
                  customBossEntity.getDynamicLevel(customBossEntity.getLocation(), pass);
                  int newLevel = customBossEntity.getLevel();
                  if (currentLevel != newLevel) {
                     customBossEntity.setMaxHealth();
                     customBossEntity.setNormalizedHealth();
                     CustomBossMegaConsumer.setName(customBossEntity.getLivingEntity(), customBossEntity, customBossEntity.level);
                  }
               }
            }
         }
      }).runTaskTimer(MetadataHandler.PLUGIN, 100L, 100L);
   }

   public static void shutdown() {
      if (dynamicLevelUpdater != null) {
         dynamicLevelUpdater.cancel();
      }

      dynamicLevelBossEntities.clear();

      for (CustomBossEntity customBossEntity : new HashSet<>(trackableCustomBosses)) {
         try {
            customBossEntity.removeTracking();
         } catch (RuntimeException var3) {
            MetadataHandler.PLUGIN.getLogger().log(Level.WARNING, "Failed to remove a boss tracking bar during shutdown", (Throwable)var3);
         }
      }

      trackableCustomBosses.clear();
   }

   @Override
   public void setSummoningEntity(EliteEntity summoningEntity) {
      this.summoningEntity = summoningEntity;
      if (summoningEntity instanceof CustomBossEntity) {
         this.emPackage = ((CustomBossEntity)summoningEntity).emPackage;
      }
   }

   public void setCustomBossesConfigFields(CustomBossesConfigFields customBossesConfigFields) {
      this.customBossesConfigFields = customBossesConfigFields;
      this.normalizedCombat = customBossesConfigFields.isNormalizedCombat();
      if (this.phaseBossEntity != null) {
         this.normalizedCombat = this.phaseBossEntity.getPhase1Config().isNormalizedCombat();
      }

      if (MobCombatSettingsConfig.isNormalizeRegionalBosses() && this instanceof RegionalBossEntity) {
         this.normalizedCombat = true;
      }

      this.scaledCombat = customBossesConfigFields.isScaledCombat();
      super.setDamageMultiplier(customBossesConfigFields.getDamageMultiplier());
      super.setHealthMultiplier(customBossesConfigFields.getHealthMultiplier());
      super.setEliteLoot(customBossesConfigFields.isDropsEliteMobsLoot());
      super.setRandomLoot(customBossesConfigFields.isDropsRandomLoot());
      super.setVanillaLoot(customBossesConfigFields.isDropsVanillaLoot());
      int newConfigLevel = customBossesConfigFields.getLevel();
      if (newConfigLevel != -1 || super.level == -1) {
         super.setLevel(newConfigLevel);
      }

      this.setPluginName();
      super.setElitePowers(ElitePowerParser.parsePowers(customBossesConfigFields, this));
      if (this instanceof RegionalBossEntity) {
         ((RegionalBossEntity)this)
            .setOnSpawnTransitiveBlocks(
               TransitiveBlock.serializeTransitiveBlocks(customBossesConfigFields.getOnSpawnBlockStates(), customBossesConfigFields.getFilename())
            );
         ((RegionalBossEntity)this)
            .setOnRemoveTransitiveBlocks(
               TransitiveBlock.serializeTransitiveBlocks(customBossesConfigFields.getOnRemoveBlockStates(), customBossesConfigFields.getFilename())
            );
      }
   }

   public void spawn(Location spawnLocation, int level, boolean silent) {
      super.level = level;
      super.spawnLocation = spawnLocation;
      this.spawn(spawnLocation, silent);
   }

   public void spawn(Location spawnLocation, boolean silent) {
      if (spawnLocation != null && spawnLocation.getWorld() != null && this.lastTick == spawnLocation.getWorld().getFullTime()) {
         this.attemptsCounter++;
      } else {
         this.attemptsCounter = 1;
      }

      if (spawnLocation != null && spawnLocation.getWorld() != null) {
         this.lastTick = spawnLocation.getWorld().getFullTime();
      }

      if (this.exists()) {
         Logger.warn("Warning: " + this.customBossesConfigFields.getFilename() + " attempted to double spawn " + this.attemptsCounter + " times!", true);
      } else {
         super.spawnLocation = spawnLocation;
         this.persistentLocation = spawnLocation;
         this.spawn(silent);
      }
   }

   public void spawn(boolean silent) {
      this.spawn(SpawnLifecycle.fromSilentFlag(silent));
   }

   protected void spawn(SpawnLifecycle.Context spawnContext) {
      this.spawn(spawnContext, null);
   }

   public void spawnWithBody(Location location, Function<Consumer<LivingEntity>, LivingEntity> bodyFactory) {
      if (this.exists()) {
         throw new IllegalStateException("Custom boss is already spawned");
      } else {
         this.spawnLocation = location.clone();
         this.persistentLocation = location.clone();
         this.spawn(SpawnLifecycle.fromSilentFlag(true), Objects.requireNonNull(bodyFactory));
      }
   }

   private void spawn(SpawnLifecycle.Context spawnContext, Function<Consumer<LivingEntity>, LivingEntity> bodyFactory) {
      if (!this.hasSuspendedMindBody()) {
         if (this.livingEntity == null || !this.livingEntity.isValid()) {
            Location effectiveSpawnLocation = this.resolveSpawnLocation();
            if (effectiveSpawnLocation != null) {
               this.persistentLocation = effectiveSpawnLocation.clone();
            }

            if (this.isPersistent && this.persistentObjectHandler == null) {
               this.persistentObjectHandler = new PersistentObjectHandler(this);
            } else if (this.isPersistent) {
               this.persistentObjectHandler.updatePersistentLocation(effectiveSpawnLocation);
            }

            if (effectiveSpawnLocation == null) {
               Logger.warn(
                  "Boss "
                     + this.customBossesConfigFields.getFilename()
                     + " has a null location! This is probably due to an incorrectly configured regional location!"
               );
            } else if (this.isMount || PatrolService.canMaterialize(this, effectiveSpawnLocation)) {
               if (this.customBossesConfigFields.getLevel() == -1 && this.level == -1) {
                  this.dynamicLevel = true;
                  this.getDynamicLevel(effectiveSpawnLocation);
               }

               this.setRespawnOverrideLocation(effectiveSpawnLocation);
               super.livingEntity = new CustomBossMegaConsumer(this).spawn(bodyFactory);
               if (super.livingEntity == null) {
                  Logger.warn("Something just prevented EliteMobs from spawning a Custom Boss! More info up next.");
               }

               if (this.livingEntity != null && this.livingEntity.isValid()) {
                  this.onBodyMaterialized();
                  if (this.livingEntity != null) {
                     if (this.bossMusic != null) {
                        this.bossMusic.start(this);
                     }

                     this.startBossTrails();
                     this.mountEntity();
                     this.getLivingEntity().setRemoveWhenFarAway(false);
                  } else {
                     this.persistentLocation = this.spawnLocation;
                  }

                  SpawnLifecycle.apply(spawnContext, this::setTracking, this::spawnMessage, this::startEscapeMechanismDelay);
                  if (this.summoningEntity != null) {
                     this.summoningEntity.addReinforcement(this);
                  }

                  this.cancelGlobalReinforcementTasks();
                  if (this.spawnLocation.getWorld() != null) {
                     for (ElitePower elitePower : this.elitePowers) {
                        if (elitePower instanceof CustomSummonPower) {
                           ((CustomSummonPower)elitePower).getCustomBossReinforcements().forEach(customBossReinforcement -> {
                              if (customBossReinforcement.summonType.equals(CustomSummonPower.SummonType.GLOBAL)) {
                                 this.globalReinforcements.add(CustomSummonPower.summonGlobalReinforcement(customBossReinforcement, this));
                              }
                           });
                        }
                     }
                  }

                  CommandRunner.runCommandFromList(this.customBossesConfigFields.getOnSpawnCommands(), new ArrayList<>());
               } else {
                  this.discardFailedMaterialization(this.livingEntity);
                  this.existsFailureCount++;
                  if (this.existsFailureCount > 10) {
                     if (this.existsFailureCount == 11) {
                        Logger.warn(
                           "EliteMobs tried and failed to spawn "
                              + this.customBossesConfigFields.getFilename()
                              + " "
                              + this.existsFailureCount
                              + "times, probably due to regional protections or third party plugin incompatibilities."
                        );
                        Logger.warn("To avoid cluttering up console, these warnings will now only appear once 6000 attempts for this boss. ");
                     }

                     if (this.existsFailureCount % 6000 == 0) {
                        Logger.warn(
                           "EliteMobs tried and failed to spawn "
                              + this.customBossesConfigFields.getFilename()
                              + " "
                              + this.existsFailureCount
                              + "times, probably due to regional protections or third party plugin incompatibilities."
                        );
                        Logger.warn("To avoid cluttering up console, these warnings will now only appear once 6000 attempts for this boss. ");
                     }
                  } else if (this.existsFailureCount > 1) {
                     Logger.warn(
                        "EliteMobs tried and failed to spawn "
                           + this.customBossesConfigFields.getFilename()
                           + " "
                           + this.existsFailureCount
                           + "times, probably due to regional protections."
                     );
                  } else {
                     Logger.warn("EliteMobs tried and failed to spawn " + this.customBossesConfigFields.getFilename() + " . Possible reasons for this:");
                     Logger.warn("- The region was protected by a plugin (most likely)");
                     Logger.warn("- The spawn was interfered with by some incompatible third party plugin");
                     Logger.warn("Debug data: ");
                     Logger.warn("Chunk is loaded: " + ChunkLocationChecker.chunkAtLocationIsLoaded(effectiveSpawnLocation));
                     Logger.warn("Attempted spawn location: " + effectiveSpawnLocation, true);
                  }
               }
            }
         }
      }
   }

   protected void onBodyMaterialized() {
      if (this.phaseBossEntity != null) {
         this.phaseBossEntity.onBodyMaterialized();
      }
   }

   private Location resolveSpawnLocation() {
      return this.respawnOverrideLocation != null ? this.respawnOverrideLocation : PatrolService.materializationLocation(this).orElse(this.spawnLocation);
   }

   private void setNormalizedHealth() {
      if (this.normalizedCombat) {
         this.setNormalizedMaxHealth();
      }
   }

   public void setNormalizedCombat() {
      this.normalizedCombat = true;
      if (this.livingEntity != null) {
         this.setNormalizedMaxHealth();
      }
   }

   @Override
   public boolean isScaledCombat() {
      if (this.scaledCombat) {
         return true;
      } else {
         return this.normalizedCombat ? false : super.isScaledCombat();
      }
   }

   private void setPluginName() {
      if (this.customBossesConfigFields.getName() != null) {
         if (this.level == -1) {
            this.setName(
               ChatColorConverter.convert(
                  this.customBossesConfigFields
                     .getName()
                     .replace("$level", "?")
                     .replace("$normalLevel", ChatColorConverter.convert("&2[&a?&2]&f"))
                     .replace("$minibossLevel", ChatColorConverter.convert("&6〖&e?&6〗&f"))
                     .replace("$bossLevel", ChatColorConverter.convert("&4『&c?&4』&f"))
                     .replace("$reinforcementLevel", ChatColorConverter.convert("&8〔&7") + "?&8〕&f")
                     .replace("$eventBossLevel", ChatColorConverter.convert("&4「&c?&4」&f"))
               ),
               false
            );
         } else {
            this.setName(
               ChatColorConverter.convert(MobLevelPlaceholderFormatter.replaceLevelPlaceholders(this.customBossesConfigFields.getName(), this, this.level)),
               false
            );
         }
      }
   }

   @Override
   public void setName(String name, boolean applyToLivingEntity) {
      super.setName(name, applyToLivingEntity);
      if (this.isValid() && this.customModel != null) {
         this.customModel.setName(name, true);
      }
   }

   @Override
   public void setName(EliteMobProperties eliteMobProperties) {
      super.setName(eliteMobProperties);
      if (this.isValid() && this.customModel != null) {
         boolean showName = DefaultConfig.isAlwaysShowNametags() || this.customBossesConfigFields.isAlwaysShowName();
         this.customModel.setName(this.name, showName);
      }
   }

   @Override
   public void setNameVisible(boolean isVisible) {
      if (this.livingEntity != null) {
         boolean effectiveVisibility = isVisible || this.customBossesConfigFields.isAlwaysShowName();
         super.setNameVisible(effectiveVisibility);
         if (Bukkit.getPluginManager().isPluginEnabled("LibsDisguises")) {
            DisguiseEntity.setDisguiseNameVisibility(effectiveVisibility, this.livingEntity, this.name);
         }

         if (this.customModel != null && this.isValid()) {
            this.customModel.setNameVisible(effectiveVisibility);
         }
      }
   }

   public void announceSpawn() {
      SpawnLifecycle.apply(SpawnLifecycle.Context.ANNOUNCED, this::setTracking, this::spawnMessage, this::startEscapeMechanismDelay);
   }

   private void setTracking(boolean notifyPlayers) {
      if (this.customBossesConfigFields.getAnnouncementPriority() >= 1 && MobCombatSettingsConfig.isShowCustomBossLocation()) {
         this.bossTrackingLifecycle.activate(notifyPlayers);
      }
   }

   public BossTrackingBar getBossTrackingBar() {
      return this.bossTrackingLifecycle.current();
   }

   void removeTracking(BossTrackingBar expected) {
      this.bossTrackingLifecycle.deactivate(expected);
   }

   private void removeTracking() {
      this.bossTrackingLifecycle.deactivate();
   }

   private void spawnMessage() {
      if (this.customBossesConfigFields.getSpawnMessage() != null) {
         if (this.customBossesConfigFields.getAnnouncementPriority() >= 1) {
            if (this.getLocation() != null) {
               if (this.getLocation().getWorld() != null) {
                  if (!EventsConfig.isAnnouncementBroadcastWorldOnly()) {
                     Bukkit.broadcastMessage(ChatColorConverter.convert(this.customBossesConfigFields.getSpawnMessage()));
                  } else {
                     for (Player player : this.getLocation().getWorld().getPlayers()) {
                        player.sendMessage(ChatColorConverter.convert(this.customBossesConfigFields.getSpawnMessage()));
                     }
                  }

                  if (this.customBossesConfigFields.getAnnouncementPriority() >= 3) {
                     new DiscordSRVAnnouncement(ChatColorConverter.convert(this.customBossesConfigFields.getSpawnMessage()));
                  }
               }
            }
         }
      }
   }

   public void getDynamicLevel(Location bossLocation) {
      this.getDynamicLevel(bossLocation, new CustomBossEntity.DynamicLevelPass());
   }

   private void getDynamicLevel(Location location, CustomBossEntity.DynamicLevelPass pass) {
      this.startUpdatingDynamicLevel();
      super.setLevel(pass.highestLevel(location));
   }

   private void startUpdatingDynamicLevel() {
      if (dynamicLevelUpdater == null) {
         dynamicLevelBossEntities.add(this);
      }
   }

   private void startBossTrails() {
      this.customBossTrail = new CustomBossTrail(this);
   }

   private void mountEntity() {
      this.mount = CustomBossMount.generateMount(this);
   }

   @Nullable
   public Entity getMountEntity() {
      if (this.livingEntity != null && this.livingEntity.isInsideVehicle()) {
         return this.livingEntity.getVehicle();
      } else if (this.customBossMount != null && this.customBossMount.getLivingEntity() != null && this.customBossMount.getLivingEntity().isValid()) {
         return this.customBossMount.getLivingEntity();
      } else if (this.livingEntityMount != null && this.livingEntityMount.isValid()) {
         return this.livingEntityMount;
      } else {
         return this.mount != null && this.mount.getLivingEntity() != null && this.mount.getLivingEntity().isValid() ? this.mount.getLivingEntity() : null;
      }
   }

   @Nullable
   public CustomBossEntity getCustomBossMount() {
      return this.customBossMount != null ? this.customBossMount : this.mount;
   }

   @Nullable
   public LivingEntity getLivingEntityMount() {
      return this.livingEntityMount;
   }

   private void startEscapeMechanismDelay() {
      if (this.escapeMechanism != null) {
         Bukkit.getScheduler().cancelTask(this.escapeMechanism);
      }

      this.escapeMechanism = CustomBossEscapeMechanism.startEscape(this.customBossesConfigFields.getTimeout(), this);
   }

   private void cullReinforcements(boolean cullGlobal) {
      if (cullGlobal) {
         new ArrayList<>(this.globalReinforcementEntities).forEach(customBossEntity -> customBossEntity.remove(RemovalReason.REINFORCEMENT_CULL));
      }

      for (CustomBossEntity customBossEntity : new ArrayList<>(this.eliteReinforcementEntities)) {
         if (customBossEntity != null) {
            customBossEntity.remove(RemovalReason.REINFORCEMENT_CULL);
         }
      }

      for (Entity entity : this.nonEliteReinforcementEntities) {
         if (entity != null) {
            entity.remove();
         }
      }

      if (cullGlobal) {
         this.globalReinforcementEntities.clear();
      }

      this.eliteReinforcementEntities.clear();
      this.nonEliteReinforcementEntities.clear();
   }

   private void cancelGlobalReinforcementTasks() {
      for (BukkitTask task : this.globalReinforcements) {
         if (task != null) {
            task.cancel();
         }
      }

      this.globalReinforcements.clear();
   }

   @Override
   public Location getLocation() {
      return this.getLivingEntity() != null ? this.getLivingEntity().getLocation() : PatrolService.logicalLocation(this).orElse(this.persistentLocation);
   }

   @Override
   public Location getPersistentLocation() {
      if (this.respawnOverrideLocation != null) {
         return this.respawnOverrideLocation;
      } else {
         Location patrolLocation = PatrolService.materializationLocation(this).orElse(null);
         if (patrolLocation != null) {
            return patrolLocation;
         } else {
            if (this.persistentLocation == null && this.getLocation() != null) {
               this.persistentLocation = this.getLocation();
            }

            if (this.persistentLocation == null && this.spawnLocation != null) {
               this.persistentLocation = this.spawnLocation;
            }

            return this.persistentLocation;
         }
      }
   }

   public void updatePersistentLocation(Location location) {
      if (location != null) {
         this.persistentLocation = location.clone();
         if (this.persistentObjectHandler != null) {
            this.persistentObjectHandler.updatePersistentLocation(this.persistentLocation);
         }
      }
   }

   @Override
   public String getWorldName() {
      return this.worldName;
   }

   public double getDamageModifier(Material material) {
      return this.customBossesConfigFields.getDamageModifier(material);
   }

   public void resetLivingEntity(LivingEntity livingEntity, SpawnReason spawnReason) {
      new CustomBossMegaConsumer(this).applyBossFeatures(livingEntity);
      super.setLivingEntity(livingEntity, spawnReason);
   }

   void discardFailedMaterialization(LivingEntity body) {
      try {
         this.discardSpawnBody(body);
      } finally {
         this.customModel = null;
      }
   }

   @Override
   public boolean exists() {
      return super.exists() || this.persistentObjectHandler != null;
   }

   @Override
   public void fullHeal() {
      if (!(this instanceof InstancedBossEntity)) {
         if (this.phaseBossEntity != null && !this.phaseBossEntity.isInFirstPhase()) {
            this.phaseBossEntity.resetToFirstPhase();
            this.clearDamagers();
         } else {
            if (this.phaseBossEntity != null) {
               this.phaseBossEntity.resetToFirstPhase();
            }

            super.fullHeal();
         }
      }
   }

   @Override
   public void remove(RemovalReason removalReason) {
      this.beginRemovalCall();

      try {
         this.cancelGlobalReinforcementTasks();
         if (this.mountAttachmentTask != null) {
            this.mountAttachmentTask.cancel();
            this.mountAttachmentTask = null;
         }

         dynamicLevelBossEntities.remove(this);
         if (this.livingEntity != null) {
            this.persistentLocation = this.livingEntity.getLocation();
         }

         super.remove(removalReason);
         if (this.customBossTrail != null) {
            this.customBossTrail.terminateTrails();
         }

         if (this.livingEntityMount != null) {
            this.livingEntityMount.remove();
         }

         if (this.customBossMount != null) {
            this.customBossMount.remove(RemovalReason.REINFORCEMENT_CULL);
         }

         if (this.customBossesConfigFields.isCullReinforcements() || removalReason.equals(RemovalReason.PHASE_BOSS_RESET)) {
            this.cullReinforcements(false);
         }

         if (removalReason.equals(RemovalReason.PHASE_BOSS_PHASE_END) && this.inCombat) {
            new EventCaller(new EliteMobExitCombatEvent(this, EliteMobExitCombatEvent.EliteMobExitCombatReason.PHASE_SWITCH));
         }

         boolean bossInstanceEnd = removalReason.equals(RemovalReason.KILL_COMMAND)
            || removalReason.equals(RemovalReason.DEATH)
            || removalReason.equals(RemovalReason.BOSS_TIMEOUT)
            || removalReason.equals(RemovalReason.SHUTDOWN)
            || removalReason.equals(RemovalReason.ARENA_RESET)
            || removalReason.equals(RemovalReason.REMOVE_COMMAND)
            || removalReason.equals(RemovalReason.REINFORCEMENT_CULL)
            || removalReason.equals(RemovalReason.WORLD_UNLOAD) && this instanceof InstancedBossEntity;
         if (!this.isPersistent) {
            bossInstanceEnd = true;
         }

         if (!bossInstanceEnd) {
            if (removalReason.equals(RemovalReason.CHUNK_UNLOAD) || removalReason.equals(RemovalReason.WORLD_UNLOAD)) {
               this.removeTracking();
               if (this.persistentObjectHandler != null) {
                  this.persistentObjectHandler.updatePersistentLocation(this.getPersistentLocation());
               }
            }
         } else {
            if (!(this instanceof RegionalBossEntity) || this instanceof InstancedBossEntity) {
               EntityTracker.getEliteMobEntities().remove(super.eliteUUID);
            }

            this.callRemoveEventOnce(removalReason);
            if (this.escapeMechanism != null) {
               Bukkit.getScheduler().cancelTask(this.escapeMechanism);
            }

            trackableCustomBosses.remove(this);
            if (this.persistentObjectHandler != null) {
               this.persistentObjectHandler.remove();
               this.persistentObjectHandler = null;
            }

            this.removeTracking();
            if (!removalReason.equals(RemovalReason.SHUTDOWN)
               && !removalReason.equals(RemovalReason.DEATH)
               && !removalReason.equals(RemovalReason.ARENA_RESET)
               && this.phaseBossEntity != null) {
               this.phaseBossEntity.silentReset();
            }

            if (this.summoningEntity != null) {
               this.summoningEntity.removeReinforcement(this);
            }

            if (this.customSpawn != null) {
               this.customSpawn.setKeepTrying(false);
            }

            if (this.customBossesConfigFields.isCullReinforcements()) {
               this.cullReinforcements(true);
            } else {
               for (CustomBossEntity pending : new ArrayList<>(this.globalReinforcementEntities)) {
                  if (!pending.exists()) {
                     pending.remove(RemovalReason.REINFORCEMENT_CULL);
                  }
               }
            }
         }

         if (!removalReason.equals(RemovalReason.PHASE_BOSS_PHASE_END) && this.bossMusic != null) {
            this.bossMusic.stop();
         }
      } finally {
         this.finishRemovalCall();
      }
   }

   @Override
   public void chunkLoad() {
      this.restorePersistedSpawn(SpawnLifecycle.Context.SILENT);
   }

   protected final void restorePersistedSpawn(SpawnLifecycle.Context spawnContext) {
      Objects.requireNonNull(spawnContext, "context");
      if (this.respawnOverrideLocation == null) {
         this.setRespawnOverrideLocation(PatrolService.materializationLocation(this).orElse(this.persistentLocation));
      }

      this.spawn(spawnContext);
   }

   @Override
   public void chunkUnload() {
      this.remove(RemovalReason.CHUNK_UNLOAD);
   }

   @Override
   public void worldLoad(World world) {
      if (this.spawnLocation != null) {
         this.spawnLocation.setWorld(world);
      }

      if (this.persistentLocation != null) {
         this.persistentLocation.setWorld(world);
      }

      if (this.respawnOverrideLocation != null) {
         this.respawnOverrideLocation.setWorld(world);
      }

      Location effectiveLocation = this.resolveSpawnLocation();
      if (effectiveLocation != null && effectiveLocation.getWorld() != null) {
         if (PatrolService.canMaterialize(this, effectiveLocation)) {
            this.chunkLoad();
         }
      }
   }

   @Override
   public void worldUnload() {
      this.remove(RemovalReason.WORLD_UNLOAD);
      if (this.spawnLocation != null) {
         this.spawnLocation.setWorld(null);
      }

      if (this.persistentLocation != null) {
         this.persistentLocation.setWorld(null);
      }

      if (this.respawnOverrideLocation != null) {
         this.respawnOverrideLocation.setWorld(null);
      }
   }

   @Override
   public boolean referencesWorld(World world) {
      return super.referencesWorld(world) ? true : this.persistentLocation != null && world.equals(this.persistentLocation.getWorld());
   }

   @Generated
   public static HashSet<CustomBossEntity> getTrackableCustomBosses() {
      return trackableCustomBosses;
   }

   @Generated
   public CustomBossesConfigFields getCustomBossesConfigFields() {
      return this.customBossesConfigFields;
   }

   @Generated
   public void setPersistentLocation(Location persistentLocation) {
      this.persistentLocation = persistentLocation;
   }

   @Generated
   public PhaseBossEntity getPhaseBossEntity() {
      return this.phaseBossEntity;
   }

   @Generated
   public Location getRespawnOverrideLocation() {
      return this.respawnOverrideLocation;
   }

   @Generated
   public void setRespawnOverrideLocation(Location respawnOverrideLocation) {
      this.respawnOverrideLocation = respawnOverrideLocation;
   }

   @Generated
   public EMPackage getEmPackage() {
      return this.emPackage;
   }

   @Generated
   public void setEmPackage(EMPackage emPackage) {
      this.emPackage = emPackage;
   }

   @Generated
   public void setCustomSpawn(CustomSpawn customSpawn) {
      this.customSpawn = customSpawn;
   }

   @Generated
   public boolean isMount() {
      return this.isMount;
   }

   @Generated
   public void setMount(boolean isMount) {
      this.isMount = isMount;
   }

   @Generated
   public CustomModel getCustomModel() {
      return this.customModel;
   }

   @Generated
   public void setCustomModel(CustomModel customModel) {
      this.customModel = customModel;
   }

   @Generated
   public boolean isNormalizedCombat() {
      return this.normalizedCombat;
   }

   @Generated
   public CustomMusic getBossMusic() {
      return this.bossMusic;
   }

   @Generated
   public void setBossMusic(CustomMusic bossMusic) {
      this.bossMusic = bossMusic;
   }

   @Generated
   public double getFollowDistance() {
      return this.followDistance;
   }

   @Generated
   public void setFollowDistance(double followDistance) {
      this.followDistance = followDistance;
   }

   @Generated
   public double getMovementSpeedAttribute() {
      return this.movementSpeedAttribute;
   }

   @Generated
   public void setMovementSpeedAttribute(double movementSpeedAttribute) {
      this.movementSpeedAttribute = movementSpeedAttribute;
   }

   @Generated
   public boolean isDynamicLevel() {
      return this.dynamicLevel;
   }

   @Generated
   public void setDynamicLevel(boolean dynamicLevel) {
      this.dynamicLevel = dynamicLevel;
   }

   public static class CustomBossEntityEvents implements Listener {
      public static void slowRegionalBoss(RegionalBossEntity regionalBossEntity) {
         if (!regionalBossEntity.getCustomBossesConfigFields().isAlert()) {
            if (regionalBossEntity.getLivingEntity() != null) {
               if (regionalBossEntity.getMovementSpeedAttribute() > 0.12) {
                  AttributeManager.setAttribute(regionalBossEntity.getLivingEntity(), "generic_movement_speed", 0.12);
               }

               AttributeManager.setAttribute(
                  regionalBossEntity.getLivingEntity(),
                  "generic_follow_range",
                  AttributeManager.getAttributeBaseValue(regionalBossEntity.getLivingEntity(), "generic_follow_range") / 3.0
               );
            }
         }
      }

      @EventHandler
      public void slowAndBlindRegionalBossesOutOfCombat(EliteMobExitCombatEvent event) {
         if (event.getEliteMobEntity() instanceof RegionalBossEntity regionalBossEntity && regionalBossEntity.exists()) {
            slowRegionalBoss(regionalBossEntity);
         }
      }

      @EventHandler
      public void slowAndBlindRegionalBossesOnSpawn(EliteMobSpawnEvent event) {
         if (event.getEliteMobEntity() instanceof RegionalBossEntity regionalBossEntity) {
            slowRegionalBoss(regionalBossEntity);
         }
      }

      @EventHandler(
         ignoreCancelled = true
      )
      public void removeSlowEvent(EliteMobEnterCombatEvent eliteMobEnterCombatEvent) {
         if (eliteMobEnterCombatEvent.getEliteMobEntity() instanceof RegionalBossEntity regionalBossEntity) {
            regionalBossEntity.removeSlow();
         }
      }

      @EventHandler(
         ignoreCancelled = true
      )
      public void onExitCombat(EliteMobExitCombatEvent event) {
         if (event.getEliteMobEntity() instanceof CustomBossEntity) {
            if (((CustomBossEntity)event.getEliteMobEntity()).customBossesConfigFields.isCullReinforcements()) {
               ((CustomBossEntity)event.getEliteMobEntity()).cullReinforcements(false);
            }
         }
      }

      @EventHandler
      public void onEliteSpawnEvent(EliteMobSpawnEvent event) {
         if (event.getEliteMobEntity() instanceof CustomBossEntity customBossEntity) {
            CustomBossEntity.addToUpdatingDynamicLevels(customBossEntity);
         }
      }
   }

   private static final class DynamicLevelPass {
      private final double radiusSquared = Math.pow(16.0 * (Bukkit.getViewDistance() + 2.0), 2.0);
      private final Map<World, List<CustomBossEntity.PlayerLevel>> playersByWorld = new HashMap<>();

      private int highestLevel(Location location) {
         if (location != null && location.getWorld() != null) {
            List<CustomBossEntity.PlayerLevel> players = this.playersByWorld
               .computeIfAbsent(location.getWorld(), world -> world.getPlayers().stream().map(CustomBossEntity.PlayerLevel::new).toList());
            int level = 1;

            for (CustomBossEntity.PlayerLevel player : players) {
               if (player.location.distanceSquared(location) <= this.radiusSquared) {
                  level = Math.max(level, player.level());
               }
            }

            return level;
         } else {
            return 1;
         }
      }
   }

   private static final class PlayerLevel {
      private final Player player;
      private final Location location;
      private Integer level;

      private PlayerLevel(Player player) {
         this.player = player;
         this.location = player.getLocation();
      }

      private int level() {
         if (this.level == null) {
            ElitePlayerInventory inventory = ElitePlayerInventory.getPlayer(this.player);
            this.level = inventory == null ? 0 : inventory.getNaturalMobSpawnLevel(false);
         }

         return this.level;
      }
   }
}
