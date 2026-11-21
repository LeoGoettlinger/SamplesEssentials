package com.magmaguy.elitemobs.mobconstructor.custombosses;

import com.google.common.collect.ArrayListMultimap;
import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.api.internal.RemovalReason;
import com.magmaguy.elitemobs.config.ItemSettingsConfig;
import com.magmaguy.elitemobs.config.custombosses.CustomBossesConfig;
import com.magmaguy.elitemobs.config.custombosses.CustomBossesConfigFields;
import com.magmaguy.elitemobs.easyminecraftgoals.NMSManager;
import com.magmaguy.elitemobs.entitytracker.EntityTracker;
import com.magmaguy.elitemobs.magmacore.util.AttributeManager;
import com.magmaguy.elitemobs.magmacore.util.ChunkLocationChecker;
import com.magmaguy.elitemobs.magmacore.util.Logger;
import com.magmaguy.elitemobs.mobconstructor.PersistentMovingEntity;
import com.magmaguy.elitemobs.mobconstructor.PersistentObject;
import com.magmaguy.elitemobs.mobconstructor.custombosses.transitiveblocks.TransitiveBlock;
import com.magmaguy.elitemobs.pathfinding.Navigation;
import com.magmaguy.elitemobs.pathfinding.patrol.PatrolService;
import com.magmaguy.elitemobs.powers.specialpowers.SpiritWalkSupport;
import com.magmaguy.elitemobs.utils.ConfigurationLocation;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import javax.annotation.Nullable;
import lombok.Generated;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

public class RegionalBossEntity extends CustomBossEntity implements PersistentObject, PersistentMovingEntity {
   private static final ArrayListMultimap<CustomBossesConfigFields, RegionalBossEntity> regionalBossesFromConfigFields = ArrayListMultimap.create();
   private final double leashRadius;
   private String rawString;
   private String rawLocationString;
   private long ticksBeforeRespawn = 0L;
   private long unixRespawnTime;
   private int respawnCoolDownInMinutes = -1;
   private boolean isRespawning = false;
   private BukkitTask leashTask;
   private List<TransitiveBlock> onSpawnTransitiveBlocks;
   private List<TransitiveBlock> onRemoveTransitiveBlocks;
   private boolean removed = false;
   private BukkitTask respawnTask = null;
   private long respawnGeneration;
   private boolean freshEncounterPending;

   public RegionalBossEntity(CustomBossesConfigFields customBossesConfigFields, String rawString) {
      super(customBossesConfigFields);
      this.rawString = rawString;
      this.rawLocationString = rawString.split(":")[0];
      this.respawnCoolDownInMinutes = customBossesConfigFields.getSpawnCooldown();
      this.leashRadius = customBossesConfigFields.getLeashRadius();
      regionalBossesFromConfigFields.put(customBossesConfigFields, this);
      this.unixRespawnTime = 0L;
      if (rawString.contains(":")) {
         this.unixRespawnTime = Long.parseLong(rawString.split(":")[1]);
      }

      this.ticksBeforeRespawn = 0L;
      if (this.unixRespawnTime > 0L) {
         this.ticksBeforeRespawn = (this.unixRespawnTime - System.currentTimeMillis()) / 1000L * 20L < 0L
            ? 0L
            : (this.unixRespawnTime - System.currentTimeMillis()) / 1000L * 20L;
      }

      super.worldName = this.rawLocationString.split(",")[0];
      super.spawnLocation = ConfigurationLocation.serialize(this.rawLocationString);
      super.setPersistent(true);
   }

   public RegionalBossEntity(CustomBossesConfigFields customBossesConfigFields, Location location, boolean permanent, boolean persistent) {
      super(customBossesConfigFields);
      super.setPersistent(persistent);
      super.spawnLocation = location;
      this.leashRadius = customBossesConfigFields.getLeashRadius();
      if (permanent) {
         this.rawString = ConfigurationLocation.deserialize(location);
         this.rawLocationString = this.rawString.split(":")[0];
         this.unixRespawnTime = 0L;
         this.respawnCoolDownInMinutes = customBossesConfigFields.getSpawnCooldown();
         regionalBossesFromConfigFields.put(customBossesConfigFields, this);
         this.saveNewLocation();
      }
   }

   @Nullable
   public static RegionalBossEntity SpawnRegionalBoss(String configurationFilename, Location spawnLocation) {
      CustomBossesConfigFields thisCustomBossConfigurationField = CustomBossesConfig.getCustomBoss(configurationFilename);
      if (thisCustomBossConfigurationField == null) {
         return null;
      } else {
         return spawnLocation == null ? null : new RegionalBossEntity(thisCustomBossConfigurationField, spawnLocation, true, true);
      }
   }

   public static void regionalBossesShutdown() {
      regionalBossesFromConfigFields.clear();
   }

   public static List<RegionalBossEntity> getRegionalBossEntities(CustomBossesConfigFields customBossesConfigFields) {
      return regionalBossesFromConfigFields.get(customBossesConfigFields);
   }

   public static Collection<RegionalBossEntity> getRegionalBossEntities() {
      return regionalBossesFromConfigFields.values();
   }

   public static void regionalDataSaver() {
      (new BukkitRunnable() {
         public void run() {
            RegionalBossEntity.save();
         }
      }).runTaskTimer(MetadataHandler.PLUGIN, 100L, 100L);
   }

   public static void save() {
      save(false);
   }

   public static void flushOnShutdown() {
      save(true);
   }

   private static void save(boolean flush) {
      for (CustomBossesConfigFields fields : regionalBossesFromConfigFields.keySet()) {
         if (fields.isFilesOutOfSync()) {
            List<String> locations = new ArrayList<>();

            for (RegionalBossEntity boss : regionalBossesFromConfigFields.get(fields)) {
               if (!boss.removed) {
                  locations.add(boss.rawString);
               }
            }

            try {
               fields.saveRegionalSpawnLocations(locations, flush);
            } catch (RuntimeException var6) {
               Logger.warn("Failed to save regional state for " + fields.getFilename() + ": " + var6.getMessage());
               if (flush) {
                  Logger.warn("Unsaved spawnLocations for " + fields.getFilename() + ": " + locations);
               }
            }
         }
      }
   }

   public static RegionalBossEntity getRegionalBoss(CustomBossesConfigFields customBossesConfigFields, Location spawnLocation) {
      for (RegionalBossEntity regionalBossEntity : regionalBossesFromConfigFields.get(customBossesConfigFields)) {
         if (Objects.equals(regionalBossEntity.getSpawnLocation(), spawnLocation)) {
            return regionalBossEntity;
         }
      }

      return null;
   }

   public static List<RegionalBossEntity> getRegionalBossEntitySet() {
      return new ArrayList<>(regionalBossesFromConfigFields.values());
   }

   public String getConfigurationLocationString() {
      return this.rawString;
   }

   public void rebindPatrolConfig(CustomBossesConfigFields fields) {
      CustomBossesConfigFields previous = this.getCustomBossesConfigFields();
      regionalBossesFromConfigFields.remove(previous, this);
      this.customBossesConfigFields = Objects.requireNonNull(fields, "fields");
      regionalBossesFromConfigFields.put(fields, this);
      previous.setFilesOutOfSync(true);
      fields.setFilesOutOfSync(true);
   }

   @Nullable
   public static RegionalBossEntity createTemporaryRegionalBossEntity(String filename, Location spawnLocation) {
      CustomBossesConfigFields customBossesConfigFields = CustomBossesConfig.getCustomBoss(filename);
      return customBossesConfigFields == null ? null : new RegionalBossEntity(customBossesConfigFields, spawnLocation, false, false);
   }

   public static RegionalBossEntity createPermanentRegionalBossEntity(CustomBossesConfigFields customBossesConfigFields, Location spawnLocation) {
      RegionalBossEntity regionalBossEntity = new RegionalBossEntity(customBossesConfigFields, spawnLocation, true, true);
      regionalBossEntity.queueSpawn(false);
      return regionalBossEntity;
   }

   public boolean isRespawning() {
      return this.isRespawning;
   }

   public void saveNewLocation() {
      if (this.spawnLocation == null) {
         Logger.warn("Failed to save regional boss because it failed to spawn correctly!");
      }

      this.customBossesConfigFields.setFilesOutOfSync(true);
      this.rawString = ConfigurationLocation.deserialize(this.spawnLocation);
      this.rawLocationString = this.rawString;
      this.unixRespawnTime = 0L;
   }

   public void initialize() {
      this.queueSpawn(RegionalBossSpawnPolicy.forPersistedState(this.unixRespawnTime));
   }

   public void queueSpawn(boolean silent) {
      this.queueSpawn(SpawnLifecycle.fromSilentFlag(silent));
   }

   private void cancelQueuedRespawn() {
      this.respawnGeneration++;
      if (this.respawnTask != null) {
         this.respawnTask.cancel();
      }

      this.respawnTask = null;
   }

   private void queueSpawn(final SpawnLifecycle.Context spawnContext) {
      this.cancelQueuedRespawn();
      final long generation = this.respawnGeneration;
      this.isRespawning = true;
      this.respawnTask = (new BukkitRunnable() {
         public void run() {
            if (generation == RegionalBossEntity.this.respawnGeneration && !RegionalBossEntity.this.removed) {
               RegionalBossEntity.this.respawnTask = null;
               RegionalBossEntity.this.ticksBeforeRespawn = 0L;
               RegionalBossEntity.this.clearPersistedRespawnTime();
               if (RegionalBossEntity.this.livingEntity != null && RegionalBossEntity.this.livingEntity.isValid()) {
                  RegionalBossEntity.this.isRespawning = false;
               } else {
                  RegionalBossEntity.this.freshEncounterPending = true;
                  RegionalBossEntity.this.spawn(spawnContext);
               }
            }
         }
      }).runTaskLater(MetadataHandler.PLUGIN, this.ticksBeforeRespawn);
   }

   private void clearPersistedRespawnTime() {
      if (this.unixRespawnTime > 0L) {
         this.unixRespawnTime = 0L;
         this.rawString = this.rawLocationString;
         if (this.phaseBossEntity != null) {
            this.phaseBossEntity.getPhase1Config().setFilesOutOfSync(true);
         } else {
            this.customBossesConfigFields.setFilesOutOfSync(true);
         }
      }
   }

   public void forceRespawn() {
      if (this.respawnTask != null) {
         this.cancelQueuedRespawn();
         this.ticksBeforeRespawn = 0L;
         this.clearPersistedRespawnTime();
         if (this.livingEntity != null && this.livingEntity.isValid()) {
            this.isRespawning = false;
         } else {
            this.freshEncounterPending = true;
            this.spawn(false);
         }
      }
   }

   public void respawn() {
      if (this.respawnCoolDownInMinutes >= 0) {
         this.isRespawning = true;
         this.unixRespawnTime = this.respawnCoolDownInMinutes * 60L * 1000L + System.currentTimeMillis();
         this.ticksBeforeRespawn = this.respawnCoolDownInMinutes * 60L * 20L;
         this.rawString = this.rawLocationString + ":" + this.unixRespawnTime;
         if (this.phaseBossEntity != null) {
            this.phaseBossEntity.getPhase1Config().setFilesOutOfSync(true);
         } else {
            this.customBossesConfigFields.setFilesOutOfSync(true);
         }

         this.queueSpawn(false);
      }
   }

   public void checkLeash() {
      if (!(this.leashRadius < 1.0)) {
         if (this.leashTask != null) {
            this.leashTask.cancel();
         }

         this.leashTask = Bukkit.getScheduler().runTaskTimer(MetadataHandler.PLUGIN, () -> {
            try {
               if (!this.isValid()) {
                  this.cancelLeash();
                  return;
               }

               if (this.getLivingEntity().getLocation().distanceSquared(this.spawnLocation) > Math.pow(this.leashRadius, 2.0)) {
                  SpiritWalkSupport.spiritWalkRegionalBossAnimation(this, this.getLivingEntity().getLocation(), this.getSpawnLocation());
               }
            } catch (Exception var3) {
               var3.printStackTrace();
               Logger.warn("Leash task errored!");
            }
         }, 60L, 60L);
      }
   }

   private void cancelLeash() {
      if (this.leashTask != null) {
         this.leashTask.cancel();
         this.leashTask = null;
      }
   }

   public void setLeashRadius(int leashRadius) {
      this.getCustomBossesConfigFields().setLeashRadius(leashRadius);
      this.getCustomBossesConfigFields().setFilesOutOfSync(true);
   }

   @Override
   public void spawn(boolean silent) {
      this.spawn(SpawnLifecycle.fromSilentFlag(silent));
   }

   @Override
   protected void spawn(SpawnLifecycle.Context spawnContext) {
      if (this.respawnTask != null) {
         this.cancelQueuedRespawn();
         this.ticksBeforeRespawn = 0L;
         this.clearPersistedRespawnTime();
      }

      if (this.livingEntity == null || !this.livingEntity.isValid()) {
         if (this.freshEncounterPending) {
            if (this.phaseBossEntity != null) {
               this.phaseBossEntity.silentReset();
            }

            this.health = null;
         }

         super.spawn(spawnContext);
         if (this.livingEntity != null && this.livingEntity.isValid()) {
            this.isRespawning = false;
            if (!ItemSettingsConfig.isRegionalBossesDropVanillaLoot()) {
               super.vanillaLoot = false;
            }

            if (NMSManager.isEnabled()) {
               if (this.livingEntity != null && this.leashRadius > 0.0) {
                  Navigation.addSoftLeashAI(this);
                  Navigation.addHardLeashAI(this);
               }
            } else if (this.livingEntity != null) {
               this.checkLeash();
            }
         }
      }
   }

   @Override
   protected void onBodyMaterialized() {
      if (this.freshEncounterPending) {
         this.clearDamagers();
         this.freshEncounterPending = false;
      }

      super.onBodyMaterialized();
   }

   @Override
   public void remove(RemovalReason removalReason) {
      this.beginRemovalCall();

      try {
         this.cancelLeash();
         super.remove(removalReason);
         switch (removalReason) {
            case REMOVE_COMMAND:
               this.permanentlyRemove();
               break;
            case DEATH:
            case BOSS_TIMEOUT:
               if (this.customBossesConfigFields.isRemoveAfterDeath() || this.isTemporary()) {
                  this.permanentlyRemove();
               } else if (!(this instanceof InstancedBossEntity)) {
                  this.respawn();
               }
               break;
            case CHUNK_UNLOAD:
               if (this.isTemporary()) {
                  this.permanentlyRemove();
               } else if (!PatrolService.hasConfiguredPatrol(this) && ChunkLocationChecker.chunkAtLocationIsLoaded(this.spawnLocation)) {
                  this.respawn();
               }
            case PHASE_BOSS_RESET:
            case PHASE_BOSS_PHASE_END:
            case EFFECT_TIMEOUT:
            case ENTITY_REPLACEMENT:
               break;
            default:
               if (this.isTemporary()) {
                  this.permanentlyRemove();
               }
         }
      } finally {
         this.finishRemovalCall();
      }
   }

   private boolean isTemporary() {
      return this.rawString == null;
   }

   private void permanentlyRemove() {
      EntityTracker.getEliteMobEntities().remove(super.eliteUUID);
      this.removed = true;
      this.cancelQueuedRespawn();
      this.freshEncounterPending = false;
      if (!this.isTemporary()) {
         CustomBossesConfigFields persistedConfig = this.phaseBossEntity == null ? this.getCustomBossesConfigFields() : this.phaseBossEntity.getPhase1Config();
         persistedConfig.setFilesOutOfSync(true);
      }
   }

   @Override
   public void chunkLoad() {
      this.restorePersistedSpawn(SpawnLifecycle.Context.RESTORED);
   }

   public void removeSlow() {
      if (!this.getCustomBossesConfigFields().isAlert() && this.getLivingEntity() != null) {
         AttributeManager.setAttribute(this.getLivingEntity(), "generic_follow_range", this.getFollowDistance());
         AttributeManager.setAttribute(this.getLivingEntity(), "generic_movement_speed", this.getMovementSpeedAttribute());
      }
   }

   @Generated
   public double getLeashRadius() {
      return this.leashRadius;
   }

   @Generated
   public List<TransitiveBlock> getOnSpawnTransitiveBlocks() {
      return this.onSpawnTransitiveBlocks;
   }

   @Generated
   public void setOnSpawnTransitiveBlocks(List<TransitiveBlock> onSpawnTransitiveBlocks) {
      this.onSpawnTransitiveBlocks = onSpawnTransitiveBlocks;
   }

   @Generated
   public List<TransitiveBlock> getOnRemoveTransitiveBlocks() {
      return this.onRemoveTransitiveBlocks;
   }

   @Generated
   public void setOnRemoveTransitiveBlocks(List<TransitiveBlock> onRemoveTransitiveBlocks) {
      this.onRemoveTransitiveBlocks = onRemoveTransitiveBlocks;
   }

   @Generated
   public boolean isRemoved() {
      return this.removed;
   }
}
