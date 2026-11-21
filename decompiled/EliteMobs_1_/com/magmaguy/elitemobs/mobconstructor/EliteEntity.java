package com.magmaguy.elitemobs.mobconstructor;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.advancedcombat.AdvancedCombatModule;
import com.magmaguy.elitemobs.advancedcombat.AdvancedCombatRuntime;
import com.magmaguy.elitemobs.api.EliteMobExitCombatEvent;
import com.magmaguy.elitemobs.api.EliteMobHealEvent;
import com.magmaguy.elitemobs.api.EliteMobRemoveEvent;
import com.magmaguy.elitemobs.api.internal.RemovalReason;
import com.magmaguy.elitemobs.collateralminecraftchanges.KeepNeutralsAngry;
import com.magmaguy.elitemobs.combatsystem.CombatDamageContext;
import com.magmaguy.elitemobs.combatsystem.LevelScaling;
import com.magmaguy.elitemobs.combatsystem.NaturalEliteCombatTweak;
import com.magmaguy.elitemobs.combatsystem.antiexploit.AntiExploitMessage;
import com.magmaguy.elitemobs.config.AntiExploitConfig;
import com.magmaguy.elitemobs.config.DefaultConfig;
import com.magmaguy.elitemobs.config.MobCombatSettingsConfig;
import com.magmaguy.elitemobs.config.powers.PowersConfigFields;
import com.magmaguy.elitemobs.easyminecraftgoals.NMSManager;
import com.magmaguy.elitemobs.entitytracker.EntityTracker;
import com.magmaguy.elitemobs.magmacore.util.AttributeManager;
import com.magmaguy.elitemobs.magmacore.util.ChatColorConverter;
import com.magmaguy.elitemobs.magmacore.util.Logger;
import com.magmaguy.elitemobs.mobconstructor.custombosses.AdvancedAggroManager;
import com.magmaguy.elitemobs.mobconstructor.custombosses.CustomBossEntity;
import com.magmaguy.elitemobs.mobconstructor.mobdata.aggressivemobs.EliteMobProperties;
import com.magmaguy.elitemobs.playerdata.ElitePlayerInventory;
import com.magmaguy.elitemobs.powers.PowerExecutionOrder;
import com.magmaguy.elitemobs.powers.lua.LuaElitePower;
import com.magmaguy.elitemobs.powers.meta.ElitePower;
import com.magmaguy.elitemobs.powers.scripts.EliteScript;
import com.magmaguy.elitemobs.powerstances.MajorPowerPowerStance;
import com.magmaguy.elitemobs.powerstances.MinorPowerPowerStance;
import com.magmaguy.elitemobs.skills.SkillType;
import com.magmaguy.elitemobs.tagger.PersistentTagger;
import com.magmaguy.elitemobs.thirdparty.libsdisguises.DisguiseEntity;
import com.magmaguy.elitemobs.utils.EntitySearch;
import com.magmaguy.elitemobs.utils.EventCaller;
import com.magmaguy.elitemobs.utils.GameClock;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Consumer;
import lombok.Generated;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.entity.Bee;
import org.bukkit.entity.EnderDragon;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Goat;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.entity.PigZombie;
import org.bukkit.entity.Player;
import org.bukkit.entity.Rabbit;
import org.bukkit.entity.Skeleton;
import org.bukkit.entity.Wither;
import org.bukkit.entity.WitherSkeleton;
import org.bukkit.entity.Wolf;
import org.bukkit.entity.Zombie;
import org.bukkit.entity.EnderDragon.Phase;
import org.bukkit.entity.Rabbit.Type;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;

public class EliteEntity {
   protected final HashMap<Player, Double> damagers = new HashMap<>();
   private final Map<UUID, Player> contributionSessions = new HashMap<>();
   private final Map<UUID, EnumMap<SkillType, Double>> skillDamageContributions = new HashMap<>();
   protected final UUID eliteUUID = UUID.randomUUID();
   private boolean removalEventCalled = false;
   private int removalCallDepth = 0;
   private RemovalReason pendingRemovalEventReason;
   private final HashSet<String> customMetadata = new HashSet<>();
   protected HashSet<ElitePower> elitePowers = new EliteEntity.TrackedElitePowerSet();
   private transient List<ElitePower> orderedElitePowers = null;
   protected final HashMap<Player, Double> aggro = new HashMap<>();
   private UUID forcedTargetPlayerId;
   private UUID forcedTargetLeaseId;
   private long forcedTargetExpiresAtNanos;
   protected LivingEntity livingEntity;
   protected LivingEntity unsyncedLivingEntity;
   protected int level = -1;
   protected double maxHealth;
   protected String name;
   protected int minorPowerCount = 0;
   protected int majorPowerCount = 0;
   protected boolean minorVisualEffect = false;
   protected boolean majorVisualEffect = false;
   protected boolean visualEffectObfuscated = true;
   protected boolean isNaturalEntity;
   protected EntityType entityType;
   protected Boolean isPersistent = false;
   protected boolean vanillaLoot = true;
   protected boolean eliteLoot = true;
   protected boolean randomLoot = true;
   protected SpawnReason spawnReason;
   protected double healthMultiplier = 1.0;
   protected double damageMultiplier = 1.0;
   protected double defaultMaxHealth;
   protected boolean inCooldown = false;
   private long globalPowerCooldownExpiresAt;
   protected boolean triggeredAntiExploit = false;
   protected int antiExploitPoints = 0;
   protected boolean inAntiExploitCooldown = false;
   protected boolean inCombat = false;
   private BukkitTask combatWatchdog;
   protected boolean inCombatGracePeriod = false;
   protected EliteEntity summoningEntity;
   protected List<CustomBossEntity> globalReinforcementEntities = new ArrayList<>();
   protected List<CustomBossEntity> eliteReinforcementEntities = new ArrayList<>();
   protected List<Entity> nonEliteReinforcementEntities = new ArrayList<>();
   protected boolean bypassesProtections = false;
   protected Double health = null;
   protected Location spawnLocation;
   private boolean dying = false;
   private boolean healing = false;
   private final HashMap<NamespacedKey, Object> customData = new HashMap<>();
   private final HashMap<String, Long> sharedCooldowns = new HashMap<>();
   private transient EliteMindBinding eliteMindBinding;
   private transient EliteLuaPowerBinding eliteLuaPowerBinding;
   private final ElitePowerSuppression powerSuppression = new ElitePowerSuppression(this::onPowerPauseReasonChanged);
   private transient List<Runnable> powerStanceCleanup = new ArrayList<>();
   private transient BukkitTask pendingPowerStanceRefresh;
   private transient long powerStanceGeneration;
   private transient boolean serviceManagedPowerMutation;
   private transient boolean preparedMindSpawn;
   private transient double nativeBaseMaxHealth = Double.NaN;
   private transient boolean preparedMindSpawnAttempted;
   private transient List<Runnable> preparedMindSpawnCleanup;
   private boolean mindAIEnabled = true;
   private boolean mindAware = true;

   public boolean isScaledCombat() {
      return this.isNaturalEntity && !this.isDistanceBasedNaturalEliteLevel() && MobCombatSettingsConfig.isUseScaledCombatForNaturalElites();
   }

   private boolean isDistanceBasedNaturalEliteLevel() {
      Location location = this.spawnLocation != null ? this.spawnLocation : this.getLocation();
      return location != null
         && location.getWorld() != null
         && MobCombatSettingsConfig.isUseDistanceBasedNaturalEliteLevelsForWorld(location.getWorld().getName());
   }

   public EliteEntity() {
   }

   public EliteEntity(LivingEntity livingEntity, int level, SpawnReason spawnReason) {
      this.setLevel(level);
      this.isNaturalEntity = spawnReason == SpawnReason.NATURAL;
      this.setLivingEntity(livingEntity, spawnReason);
      if (this.livingEntity != null) {
         EliteMobProperties eliteMobProperties = EliteMobProperties.getPluginData(livingEntity);
         this.setDefaultName(eliteMobProperties);
         this.setArmor();
         this.setMaxHealth();
         this.randomizePowers(eliteMobProperties);
      }
   }

   private void prepareNormalizedActor(LivingEntity livingEntity, int level, SpawnReason spawnReason, boolean randomizePowers) {
      this.setLevel(level);
      this.isNaturalEntity = spawnReason == SpawnReason.NATURAL;
      this.normalizeLivingEntity(livingEntity, spawnReason);
      EliteMobProperties eliteMobProperties = EliteMobProperties.getPluginData(livingEntity);
      NativeMindActorDefaults.requireRandomizedPowerSupport(livingEntity.getType(), eliteMobProperties, randomizePowers);
      this.setArmor();
      if (randomizePowers) {
         this.randomizePowers(eliteMobProperties);
      }
   }

   void prepareMindActor(LivingEntity livingEntity, int level, SpawnReason spawnReason, boolean randomizePowers) {
      Objects.requireNonNull(livingEntity, "livingEntity");
      Objects.requireNonNull(spawnReason, "spawnReason");
      if (!this.preparedMindSpawn && !this.preparedMindSpawnAttempted && this.livingEntity == null) {
         this.preparedMindSpawn = true;
         this.preparedMindSpawnCleanup = new ArrayList<>();
         this.prepareNormalizedActor(livingEntity, level, spawnReason, randomizePowers);
      } else {
         throw new IllegalStateException("Elite actor was already prepared");
      }
   }

   boolean commitPreparedMindSpawn() {
      if (!this.preparedMindSpawn) {
         throw new IllegalStateException("Elite actor does not have a prepared native Mind spawn");
      } else if (this.preparedMindSpawnAttempted) {
         throw new IllegalStateException("Prepared native Mind spawn was already committed");
      } else if (this.eliteMindBinding == null) {
         throw new IllegalStateException("Prepared native Mind spawn has no attached Mind binding");
      } else {
         this.preparedMindSpawnAttempted = true;
         boolean accepted = this.commitNormalizedSpawn();
         if (accepted) {
            this.preparedMindSpawn = false;
            this.preparedMindSpawnCleanup = null;
         }

         return accepted;
      }
   }

   void rollbackPreparedMindSpawn() {
      EntityTracker.getEliteMobEntities().remove(this.eliteUUID, this);
      this.cleanupPreparedMindSpawnEffects();
      this.preparedMindSpawn = false;
      this.preparedMindSpawnAttempted = true;
      this.closeAllPowerRuntimes();
      this.closePowerSuppression();
      this.elitePowers.clear();
      this.clearDamagers();
      if (this.livingEntity != null && this.livingEntity.isValid()) {
         this.livingEntity.remove();
      }

      this.livingEntity = null;
      this.unsyncedLivingEntity = null;
      this.spawnLocation = null;
   }

   EliteMindBinding getEliteMindBinding() {
      return this.eliteMindBinding;
   }

   void setEliteMindBinding(EliteMindBinding eliteMindBinding) {
      this.eliteMindBinding = eliteMindBinding;
      if (eliteMindBinding != null) {
         eliteMindBinding.setAiPaused(!this.mindAIEnabled || !this.mindAware);
      }
   }

   public boolean isAIEnabled() {
      return this.eliteMindBinding == null ? this.livingEntity != null && this.livingEntity.hasAI() : this.mindAIEnabled;
   }

   public void setAIEnabled(boolean enabled) {
      this.mindAIEnabled = enabled;
      if (this.eliteMindBinding != null) {
         this.eliteMindBinding.setAiPaused(!this.mindAIEnabled || !this.mindAware);
      } else if (this.livingEntity != null) {
         this.livingEntity.setAI(enabled);
      }
   }

   public boolean isAware() {
      return this.eliteMindBinding == null ? this.livingEntity instanceof Mob mob && mob.isAware() : this.mindAware;
   }

   public void setAware(boolean aware) {
      this.mindAware = aware;
      if (this.eliteMindBinding != null) {
         this.eliteMindBinding.setAiPaused(!this.mindAIEnabled || !this.mindAware);
      } else if (this.livingEntity instanceof Mob mob) {
         mob.setAware(aware);
      }
   }

   public boolean isAIActive() {
      return this.isAIEnabled() && (this.eliteMindBinding == null || this.mindAware && !this.eliteMindBinding.isPaused());
   }

   EliteLuaPowerBinding getEliteLuaPowerBinding() {
      return this.eliteLuaPowerBinding;
   }

   void setEliteLuaPowerBinding(EliteLuaPowerBinding eliteLuaPowerBinding) {
      this.eliteLuaPowerBinding = eliteLuaPowerBinding;
      if (eliteLuaPowerBinding != null) {
         for (ElitePowerPauseReason reason : ElitePowerPauseReason.values()) {
            eliteLuaPowerBinding.setPauseReason(reason, this.isPowerPaused(reason));
         }
      }
   }

   boolean isPowerPaused(ElitePowerPauseReason reason) {
      return this.powerSuppression.isSuppressed(reason)
         || reason == ElitePowerPauseReason.MIND_SERVICE && this.eliteMindBinding != null && this.eliteMindBinding.isExplicitlyPaused();
   }

   boolean isPreparedMindSpawn() {
      return this.preparedMindSpawn && !this.preparedMindSpawnAttempted;
   }

   void addPreparedMindSpawnCleanup(Runnable cleanup) {
      Objects.requireNonNull(cleanup, "cleanup");
      if (this.isPreparedMindSpawn() && this.preparedMindSpawnCleanup != null) {
         this.preparedMindSpawnCleanup.add(cleanup);
      } else {
         throw new IllegalStateException("Elite actor is not accepting prepared-spawn cleanup");
      }
   }

   void reattachMindBody(LivingEntity replacement) {
      this.livingEntity = Objects.requireNonNull(replacement, "replacement");
      this.unsyncedLivingEntity = replacement;
      this.entityType = replacement.getType();
      if (!(this instanceof CustomBossEntity)) {
         this.spawnLocation = replacement.getLocation().clone();
      }

      PersistentTagger.tagElite(replacement, this.eliteUUID);
   }

   boolean suspendMindBodyForChunkUnload(LivingEntity removedBody) {
      if (this.eliteMindBinding == null) {
         return false;
      } else if (this.livingEntity != null && removedBody != null && !this.livingEntity.getUniqueId().equals(removedBody.getUniqueId())) {
         return false;
      } else {
         LivingEntity previousBody = removedBody != null ? removedBody : this.livingEntity;
         if (previousBody != null && this instanceof CustomBossEntity customBoss) {
            customBoss.updatePersistentLocation(previousBody.getLocation());
         }

         this.livingEntity = null;
         this.unsyncedLivingEntity = null;
         return true;
      }
   }

   protected final boolean hasSuspendedMindBody() {
      return this.livingEntity == null && this.eliteMindBinding != null;
   }

   void terminateMindActor(RemovalReason removalReason) {
      this.cleanupPreparedMindSpawnEffects();
      EntityTracker.getEliteMobEntities().remove(this.eliteUUID);
      this.remove(removalReason);
   }

   private void cleanupPreparedMindSpawnEffects() {
      if (this.preparedMindSpawnCleanup != null) {
         List<Runnable> cleanup = this.preparedMindSpawnCleanup;
         this.preparedMindSpawnCleanup = null;

         for (int index = cleanup.size() - 1; index >= 0; index--) {
            try {
               cleanup.get(index).run();
            } catch (RuntimeException var4) {
               Logger.warn("Failed to roll back a prepared EliteMobs spawn effect: " + var4.getMessage());
            }
         }
      }
   }

   public void addCustomData(@NotNull NamespacedKey namespacedKey, Object object) {
      this.customData.put(namespacedKey, object);
   }

   public void removeCustomData(NamespacedKey namespacedKey) {
      this.customData.remove(namespacedKey);
   }

   public boolean hasCustomData(NamespacedKey namespacedKey) {
      return this.customData.containsKey(namespacedKey);
   }

   public Object getCustomData(NamespacedKey namespacedKey) {
      return this.customData.get(namespacedKey);
   }

   public Location getSpawnLocation() {
      return this.spawnLocation.clone();
   }

   public boolean getBypassesProtections() {
      return this.bypassesProtections;
   }

   public void setBypassesProtections(boolean bypassesProtections) {
      this.bypassesProtections = bypassesProtections;
   }

   public UUID getEliteUUID() {
      return this.eliteUUID;
   }

   public void addDamager(Player player, double damage) {
      this.addDamager(player, damage, null);
   }

   public void addDamager(Player player, double damage, SkillType progressionSkill) {
      this.addDamager(
         player,
         damage,
         progressionSkill,
         CombatDamageContext.currentPlayerToEliteSource().map(CombatDamageContext.PlayerDamageSource::loudStrikesBonus).orElse(null)
      );
   }

   public void addDamager(Player player, double damage, SkillType progressionSkill, Double capturedLoudStrikes) {
      if (player != null && Double.isFinite(damage) && !(damage <= 0.0)) {
         Player trackedPlayer = this.findTrackedPlayer(player);
         this.damagers.merge(trackedPlayer, damage, Double::sum);
         if (progressionSkill != null && progressionSkill.isWeaponSkill()) {
            this.skillDamageContributions
               .computeIfAbsent(player.getUniqueId(), ignored -> new EnumMap<>(SkillType.class))
               .merge(progressionSkill, damage, Double::sum);
         }

         ElitePlayerInventory inventory = ElitePlayerInventory.getPlayer(player);
         double loudStrikesBonus = capturedLoudStrikes != null
            ? capturedLoudStrikes
            : (inventory == null ? 0.0 : inventory.getLoudStrikesBonusMultiplier(false));
         if (!Double.isFinite(loudStrikesBonus) || loudStrikesBonus < 0.0) {
            loudStrikesBonus = 0.0;
         }

         double classThreat = AdvancedCombatRuntime.isActive(player)
               && AdvancedCombatModule.activeClassLineageSnapshot(player.getUniqueId()).map(lineage -> lineage.root().id().equals("spellcaster")).orElse(false)
            ? 0.8
            : 1.0;
         this.aggro.merge(trackedPlayer, damage * (1.0 + loudStrikesBonus) * classThreat, Double::sum);
         AdvancedAggroManager.updateTarget(this);
      }
   }

   public void addThreat(Player player, double threat) {
      if (player != null && Double.isFinite(threat) && !(threat <= 0.0)) {
         Player trackedPlayer = this.findTrackedPlayer(player);
         this.aggro.merge(trackedPlayer, threat, Double::sum);
         AdvancedAggroManager.updateTarget(this);
      }
   }

   public void forceTarget(final Player player, int durationTicks) {
      if (player != null && durationTicks > 0) {
         long now = System.nanoTime();
         long duration = durationTicks > 184467440737L ? Long.MAX_VALUE : durationTicks * 50000000L;
         long expiresAt = duration != Long.MAX_VALUE && Long.MAX_VALUE - now >= duration ? now + duration : Long.MAX_VALUE;
         this.forcedTargetPlayerId = player.getUniqueId();
         final UUID leaseId = UUID.randomUUID();
         this.forcedTargetLeaseId = leaseId;
         this.forcedTargetExpiresAtNanos = expiresAt;
         AdvancedAggroManager.updateTarget(this);
         (new BukkitRunnable() {
            public void run() {
               if (leaseId.equals(EliteEntity.this.forcedTargetLeaseId) && player.getUniqueId().equals(EliteEntity.this.forcedTargetPlayerId)) {
                  EliteEntity.this.clearForcedTarget();
                  if (EliteEntity.this.isValid()) {
                     AdvancedAggroManager.updateTarget(EliteEntity.this);
                  }
               }
            }
         }).runTaskLater(MetadataHandler.PLUGIN, durationTicks);
      }
   }

   public UUID getForcedTargetPlayerId() {
      if (this.forcedTargetPlayerId != null && this.forcedTargetExpiresAtNanos <= System.nanoTime()) {
         this.clearForcedTarget();
      }

      return this.forcedTargetPlayerId;
   }

   public void clearForcedTarget(UUID playerId) {
      if (playerId != null && playerId.equals(this.forcedTargetPlayerId)) {
         this.clearForcedTarget();
      }
   }

   public void clearForcedTarget() {
      this.forcedTargetPlayerId = null;
      this.forcedTargetLeaseId = null;
      this.forcedTargetExpiresAtNanos = 0L;
   }

   private Player findTrackedPlayer(Player player) {
      Player current = Bukkit.getPlayer(player.getUniqueId());
      if (current != null) {
         player = current;
      }

      Player previous = this.contributionSessions.put(player.getUniqueId(), player);
      if (previous != null && previous != player) {
         Double damage = this.damagers.remove(previous);
         Double threat = this.aggro.remove(previous);
         if (damage != null) {
            this.damagers.merge(player, damage, Double::sum);
         }

         if (threat != null) {
            this.aggro.merge(player, threat, Double::sum);
         }
      }

      return player;
   }

   public boolean hasDamagers() {
      return !this.damagers.isEmpty();
   }

   public Map<Player, Double> getDamagers() {
      return this.damagers;
   }

   public Map<SkillType, Double> getSkillDamageContributions(UUID playerId) {
      Map<SkillType, Double> contributions = this.skillDamageContributions.get(playerId);
      return contributions == null ? Map.of() : Collections.unmodifiableMap(new EnumMap<>(contributions));
   }

   public Map<Player, Double> getAggro() {
      return Collections.unmodifiableMap(this.aggro);
   }

   public void inheritAggroFrom(EliteEntity summoningEntity) {
      if (summoningEntity != null && summoningEntity != this) {
         this.clearForcedTarget();
         this.aggro.clear();
         summoningEntity.aggro.forEach((player, threat) -> {
            if (player != null && threat != null && Double.isFinite(threat) && threat > 0.0) {
               Player current = Bukkit.getPlayer(player.getUniqueId());
               this.aggro.put(this.findTrackedPlayer(current == null ? player : current), threat);
            }
         });
         AdvancedAggroManager.updateTarget(this);
      }
   }

   public void clearDamagers() {
      this.contributionSessions.clear();
      this.damagers.clear();
      this.aggro.clear();
      this.skillDamageContributions.clear();
      this.clearForcedTarget();
   }

   public boolean isCustomBossEntity() {
      return this instanceof CustomBossEntity;
   }

   public boolean isEnderDragon() {
      return EntityType.ENDER_DRAGON.equals(this.entityType)
         || this.livingEntity != null && this.livingEntity.getType().equals(EntityType.ENDER_DRAGON)
         || this.unsyncedLivingEntity != null && this.unsyncedLivingEntity.getType().equals(EntityType.ENDER_DRAGON);
   }

   public LivingEntity getLivingEntity() {
      return this.livingEntity;
   }

   public LivingEntity getUnsyncedLivingEntity() {
      return this.unsyncedLivingEntity;
   }

   public void setUnsyncedLivingEntity(LivingEntity newEntity) {
      this.unsyncedLivingEntity = newEntity;
   }

   public void setLivingEntity(LivingEntity livingEntity, SpawnReason spawnReason) {
      if (livingEntity != null) {
         boolean accepted = false;

         try {
            this.normalizeLivingEntity(livingEntity, spawnReason);
            accepted = this.commitNormalizedSpawn();
         } finally {
            if (!accepted) {
               this.discardSpawnBody(livingEntity);
            }
         }
      }
   }

   protected void discardSpawnBody(LivingEntity body) {
      if (body != null) {
         KeepNeutralsAngry.release(body.getUniqueId());
      }

      EntityTracker.getEliteMobEntities().remove(this.eliteUUID, this);

      try {
         this.cleanupPreparedMindSpawnEffects();
         this.closePowerStances();
         this.closeAllPowerRuntimes();
         this.closeEliteLuaPowerBinding();
      } finally {
         try {
            EliteMindServiceModule.detachBehavior(this);
         } finally {
            try {
               if (body != null) {
                  try {
                     body.remove();
                  } finally {
                     if (Bukkit.getPluginManager().isPluginEnabled("LibsDisguises")) {
                        DisguiseEntity.undisguise(body);
                     }
                  }
               }
            } finally {
               this.livingEntity = null;
               this.unsyncedLivingEntity = null;
               this.clearDamagers();
            }
         }
      }
   }

   private void normalizeLivingEntity(LivingEntity livingEntity, SpawnReason spawnReason) {
      if (livingEntity != null) {
         if (this.livingEntity != livingEntity) {
            this.setInCombat(false);
            if (this.livingEntity != null) {
               KeepNeutralsAngry.release(this.livingEntity.getUniqueId());
            }
         }

         if (this.livingEntity != livingEntity || !Double.isFinite(this.nativeBaseMaxHealth)) {
            this.nativeBaseMaxHealth = AttributeManager.getAttributeBaseValue(livingEntity, "generic_max_health");
         }

         this.removalEventCalled = false;
         this.pendingRemovalEventReason = null;
         this.removalCallDepth = 0;
         if (!(this instanceof CustomBossEntity)) {
            this.spawnLocation = livingEntity.getLocation().clone();
         }

         this.livingEntity = livingEntity;
         this.unsyncedLivingEntity = livingEntity;
         this.entityType = livingEntity.getType();
         this.livingEntity.setCanPickupItems(false);
         if (livingEntity.getEquipment() != null) {
            livingEntity.getEquipment().setItemInMainHandDropChance(0.0F);
            livingEntity.getEquipment().setItemInOffHandDropChance(0.0F);
            livingEntity.getEquipment().setHelmetDropChance(0.0F);
            livingEntity.getEquipment().setChestplateDropChance(0.0F);
            livingEntity.getEquipment().setLeggingsDropChance(0.0F);
            livingEntity.getEquipment().setBootsDropChance(0.0F);
         }

         boolean nativeBehavior = !NMSManager.isEnabled() || !NMSManager.getAdapter().isMindBody(livingEntity);
         if (nativeBehavior && livingEntity.getType().equals(EntityType.RABBIT)) {
            ((Rabbit)livingEntity).setRabbitType(Type.THE_KILLER_BUNNY);
         }

         if (nativeBehavior && this.entityType.equals(EntityType.WOLF)) {
            Wolf wolf = (Wolf)livingEntity;
            wolf.setAngry(true);
            wolf.setBreed(false);
            KeepNeutralsAngry.showMeYouWarFace(this);
         }

         if (nativeBehavior && this.entityType.equals(EntityType.POLAR_BEAR)) {
            KeepNeutralsAngry.showMeYouWarFace(this);
         }

         if (this.entityType.equals(EntityType.ENDER_DRAGON) && ((EnderDragon)livingEntity).getBossBar() != null) {
            ((EnderDragon)livingEntity).getBossBar().setTitle(this.getName());
         }

         if (nativeBehavior && this.entityType.equals(EntityType.LLAMA)) {
            KeepNeutralsAngry.showMeYouWarFace(this);
         }

         if (nativeBehavior && this.entityType.equals(EntityType.IRON_GOLEM) && this instanceof CustomBossEntity) {
            KeepNeutralsAngry.showMeYouWarFace(this);
         }

         if (nativeBehavior && this.entityType.equals(EntityType.GOAT)) {
            ((Goat)livingEntity).setScreaming(true);
         }

         if (nativeBehavior && livingEntity instanceof Bee) {
            KeepNeutralsAngry.showMeYouWarFace(this);
            ((Bee)livingEntity).setCannotEnterHiveTicks(Integer.MAX_VALUE);
         }

         if (livingEntity instanceof Wither wither) {
            wither.getBossBar().setVisible(false);
         }

         this.spawnReason = spawnReason;
         this.getLivingEntity().setRemoveWhenFarAway(!this.isPersistent);
         this.getLivingEntity().setPersistent(false);
         this.setMaxHealth();
         if (this.getName() == null) {
            this.setDefaultName(EliteMobProperties.getPluginData(this.entityType));
         }

         this.name = livingEntity.getCustomName();
      }
   }

   private boolean commitNormalizedSpawn() {
      if (this.livingEntity == null) {
         throw new IllegalStateException("Cannot commit an EliteEntity without a living entity");
      } else {
         if (this instanceof CustomBossEntity boss && boss.isNormalizedCombat()) {
            this.setNormalizedMaxHealth();
         }

         PersistentTagger.tagElite(this.livingEntity, this.eliteUUID);
         if (!EntityTracker.tryRegisterEliteMob(this)) {
            return false;
         } else if (!this.preparedMindSpawn || this.eliteMindBinding != null && this.livingEntity != null && this.livingEntity.isValid()) {
            AdvancedAggroManager.updateTarget(this);
            return true;
         } else {
            EntityTracker.getEliteMobEntities().remove(this.eliteUUID, this);
            throw new IllegalStateException("Native Mind actor was removed during EliteMobSpawnEvent");
         }
      }
   }

   public void setNameVisible(boolean isVisible) {
      if (this.livingEntity != null) {
         this.livingEntity.setCustomNameVisible(isVisible);
      }
   }

   public void setMaxHealth() {
      EliteMobProperties properties = EliteMobProperties.getPluginData(this.entityType);
      this.defaultMaxHealth = NativeMindActorDefaults.baseHealth(properties, this.nativeBaseMaxHealth);
      double calculatedHealth = LevelScaling.calculateMobHealth(this.level, this.defaultMaxHealth);
      calculatedHealth = NaturalEliteCombatTweak.getTweakedMobHealth(this, this.level, calculatedHealth);
      this.maxHealth = this.calculateSafeMaxHealth(calculatedHealth);
      if (this.livingEntity != null) {
         AttributeManager.setAttribute(this.livingEntity, "generic_max_health", this.maxHealth);
      }

      if (this.health == null) {
         if (this.livingEntity != null) {
            this.livingEntity.setHealth(this.maxHealth);
         }

         this.health = this.maxHealth;
      } else if (this.livingEntity != null) {
         this.livingEntity.setHealth(Math.min(this.health, AttributeManager.getAttributeBaseValue(this.livingEntity, "generic_max_health")));
      }
   }

   public void setNormalizedMaxHealth() {
      this.defaultMaxHealth = MobCombatSettingsConfig.getNormalizedBaselineHealth();
      this.maxHealth = this.calculateSafeMaxHealth(LevelScaling.calculateMobHealth(this.level, this.defaultMaxHealth));
      if (this.livingEntity != null) {
         AttributeManager.setAttribute(this.livingEntity, "generic_max_health", this.maxHealth);
         this.livingEntity.setHealth(this.maxHealth);
      }

      this.health = this.maxHealth;
   }

   private double calculateSafeMaxHealth(double calculatedHealth) {
      double safeCalculatedHealth = calculatedHealth;
      if (!Double.isFinite(calculatedHealth) || calculatedHealth <= 0.0) {
         Logger.warn(
            "EliteMobs calculated invalid base health "
               + calculatedHealth
               + " for "
               + this.entityType
               + " level "
               + this.level
               + ". Falling back to level 1 health."
         );
         safeCalculatedHealth = LevelScaling.calculateMobHealth(1, this.defaultMaxHealth);
      }

      double safeHealthMultiplier = this.healthMultiplier;
      if (!Double.isFinite(safeHealthMultiplier) || safeHealthMultiplier <= 0.0) {
         Logger.warn(
            "EliteMobs found invalid health multiplier "
               + this.healthMultiplier
               + " for "
               + this.entityType
               + " level "
               + this.level
               + ". Falling back to 1.0."
         );
         safeHealthMultiplier = 1.0;
      }

      double minecraftMaxHealth = LevelScaling.getMinecraftMaxHealth();
      double multipliedHealth = safeCalculatedHealth * safeHealthMultiplier;
      if (Double.isFinite(multipliedHealth) && !(multipliedHealth > minecraftMaxHealth)) {
         return Math.max(1.0, multipliedHealth);
      } else {
         Logger.warn(
            "EliteMobs calculated max health "
               + multipliedHealth
               + " for "
               + this.entityType
               + " level "
               + this.level
               + " with health multiplier "
               + safeHealthMultiplier
               + ". Capping it to "
               + minecraftMaxHealth
               + " to avoid invalid health values."
         );
         return minecraftMaxHealth;
      }
   }

   public void resetMaxHealth() {
      AttributeManager.setAttribute(this.livingEntity, "generic_max_health", this.maxHealth);
      this.livingEntity.setHealth(this.maxHealth);
      this.health = this.maxHealth;
   }

   public double getHealth() {
      if (this.livingEntity != null) {
         return this.livingEntity.getHealth();
      } else if (this.health != null) {
         return this.health;
      } else {
         this.setMaxHealth();
         return this.health;
      }
   }

   public void setHealth(double health) {
      if (this.livingEntity != null) {
         this.health = Math.min(health, Math.min(this.maxHealth, AttributeManager.getAttributeBaseValue(this.livingEntity, "generic_max_health")));
         this.livingEntity.setHealth(this.health);
      }
   }

   public void syncPluginHealth(double health) {
      this.health = health;
   }

   public void heal(double healAmount) {
      EliteMobHealEvent eliteMobHealEvent = new EliteMobHealEvent(this, healAmount);
      new EventCaller(eliteMobHealEvent);
      if (!eliteMobHealEvent.isCancelled()) {
         this.setHealth(this.getHealth() + healAmount);
      }
   }

   public void fullHeal() {
      EliteMobHealEvent eliteMobHealEvent = new EliteMobHealEvent(this, true);
      new EventCaller(eliteMobHealEvent);
      if (!eliteMobHealEvent.isCancelled()) {
         this.setHealth(this.maxHealth);
         this.health = this.maxHealth;
         this.clearDamagers();
      }
   }

   private void setArmor() {
      if (MobCombatSettingsConfig.isDoEliteArmor()) {
         if (this.livingEntity.getEquipment() != null) {
            if (this.livingEntity instanceof Zombie
               || this.livingEntity instanceof PigZombie
               || this.livingEntity instanceof Skeleton
               || this.livingEntity instanceof WitherSkeleton) {
               this.livingEntity.getEquipment().setBoots(new ItemStack(Material.AIR));
               this.livingEntity.getEquipment().setLeggings(new ItemStack(Material.AIR));
               this.livingEntity.getEquipment().setChestplate(new ItemStack(Material.AIR));
               this.livingEntity.getEquipment().setHelmet(new ItemStack(Material.AIR));
               if (this.level >= 5 && MobCombatSettingsConfig.isDoEliteHelmets()) {
                  this.livingEntity.getEquipment().setHelmet(new ItemStack(Material.LEATHER_HELMET));
               }

               if (this.level >= 10) {
                  this.livingEntity.getEquipment().setBoots(new ItemStack(Material.LEATHER_BOOTS));
               }

               if (this.level >= 15) {
                  this.livingEntity.getEquipment().setLeggings(new ItemStack(Material.LEATHER_LEGGINGS));
               }

               if (this.level >= 20) {
                  this.livingEntity.getEquipment().setChestplate(new ItemStack(Material.LEATHER_CHESTPLATE));
               }

               if (this.level >= 25 && MobCombatSettingsConfig.isDoEliteHelmets()) {
                  this.livingEntity.getEquipment().setHelmet(new ItemStack(Material.CHAINMAIL_HELMET));
               }

               if (this.level >= 30) {
                  this.livingEntity.getEquipment().setBoots(new ItemStack(Material.CHAINMAIL_BOOTS));
               }

               if (this.level >= 35) {
                  this.livingEntity.getEquipment().setLeggings(new ItemStack(Material.CHAINMAIL_LEGGINGS));
               }

               if (this.level >= 40) {
                  this.livingEntity.getEquipment().setChestplate(new ItemStack(Material.CHAINMAIL_CHESTPLATE));
               }

               if (this.level >= 45 && MobCombatSettingsConfig.isDoEliteHelmets()) {
                  this.livingEntity.getEquipment().setHelmet(new ItemStack(Material.IRON_HELMET));
               }

               if (this.level >= 50) {
                  this.livingEntity.getEquipment().setBoots(new ItemStack(Material.IRON_BOOTS));
               }

               if (this.level >= 55) {
                  this.livingEntity.getEquipment().setLeggings(new ItemStack(Material.IRON_LEGGINGS));
               }

               if (this.level >= 60) {
                  this.livingEntity.getEquipment().setChestplate(new ItemStack(Material.IRON_CHESTPLATE));
               }

               if (this.level >= 65) {
                  this.livingEntity.getEquipment().setBoots(new ItemStack(Material.DIAMOND_BOOTS));
               }

               if (this.level >= 70 && MobCombatSettingsConfig.isDoEliteHelmets()) {
                  this.livingEntity.getEquipment().setHelmet(new ItemStack(Material.DIAMOND_HELMET));
               }

               if (this.level >= 75) {
                  this.livingEntity.getEquipment().setLeggings(new ItemStack(Material.DIAMOND_LEGGINGS));
               }

               if (this.level >= 80) {
                  this.livingEntity.getEquipment().setChestplate(new ItemStack(Material.DIAMOND_CHESTPLATE));
               }

               if (this.livingEntity.getEquipment().getHelmet() != null) {
                  ItemMeta helmetMeta = this.livingEntity.getEquipment().getHelmet().getItemMeta();
                  if (helmetMeta != null) {
                     helmetMeta.setUnbreakable(true);
                     this.livingEntity.getEquipment().getHelmet().setItemMeta(helmetMeta);
                  }
               }
            }
         }
      }
   }

   public void randomizePowers(EliteMobProperties eliteMobProperties) {
      if (this.level >= 1) {
         int availableDefensivePowers = 0;
         int availableOffensivePowers = 0;
         int availableMiscellaneousPowers = 0;
         int availableMajorPowers = 0;
         if (this.level >= 10) {
            availableDefensivePowers = 1;
         }

         if (this.level >= 20) {
            availableOffensivePowers = 1;
         }

         if (this.level >= 30) {
            availableMiscellaneousPowers = 1;
         }

         if (this.level >= 40) {
            availableMajorPowers = 1;
         }

         if (this.level >= 50) {
            availableDefensivePowers = 2;
         }

         if (this.level >= 60) {
            availableOffensivePowers = 2;
         }

         if (this.level >= 70) {
            availableMiscellaneousPowers = 2;
         }

         if (this.level >= 80) {
            availableMajorPowers = 2;
         }

         this.applyPowers((HashSet<PowersConfigFields>)eliteMobProperties.getValidDefensivePowers().clone(), availableDefensivePowers);
         this.applyPowers((HashSet<PowersConfigFields>)eliteMobProperties.getValidOffensivePowers().clone(), availableOffensivePowers);
         this.applyPowers((HashSet<PowersConfigFields>)eliteMobProperties.getValidMiscellaneousPowers().clone(), availableMiscellaneousPowers);
         this.applyPowers((HashSet<PowersConfigFields>)eliteMobProperties.getValidMajorPowers().clone(), availableMajorPowers);
         this.initializePowerStances();
      }
   }

   public void applyPowers(HashSet<PowersConfigFields> configFields, int availablePowerAmount) {
      configFields.removeIf(iteratedField -> !iteratedField.isEnabled());
      if (availablePowerAmount >= 1) {
         ArrayList<PowersConfigFields> localFields = new ArrayList<>(configFields);

         for (ElitePower mobPower : this.elitePowers) {
            localFields.remove(mobPower);
         }

         for (int i = 0; i < availablePowerAmount && localFields.size() >= 1; i++) {
            PowersConfigFields selectedField = localFields.get(ThreadLocalRandom.current().nextInt(localFields.size()));

            try {
               ElitePower.addPower(this, selectedField);
               localFields.remove(selectedField);
               this.countPower(selectedField);
            } catch (Exception var7) {
               Logger.warn("Failed to instance new power!");
            }
         }
      }
   }

   public void applyPowers(HashSet<PowersConfigFields> powersConfigFields) {
      powersConfigFields.forEach(field -> {
         ElitePower.addPower(this, field);
         this.countPower(field);
      });
      this.initializePowerStances();
   }

   private void countPower(PowersConfigFields powersConfigFields) {
      if (PowersConfigFields.isMajorPowerType(powersConfigFields.getPowerType())) {
         this.majorPowerCount++;
      } else {
         this.minorPowerCount++;
      }
   }

   public void setElitePowers(Collection<ElitePower> elitePowers) {
      this.elitePowers.clear();
      this.elitePowers.addAll(elitePowers);
   }

   List<ElitePower> copyElitePowersInAttachmentOrder() {
      return List.copyOf(this.elitePowers);
   }

   void replaceElitePowersInOrder(Collection<? extends ElitePower> replacement) {
      Objects.requireNonNull(replacement, "replacement");
      boolean previousMutationState = this.serviceManagedPowerMutation;
      this.serviceManagedPowerMutation = true;

      try {
         this.elitePowers.clear();
         this.elitePowers.addAll(replacement);
      } finally {
         this.serviceManagedPowerMutation = previousMutationState;
      }

      this.minorPowerCount = 0;
      this.majorPowerCount = 0;

      for (ElitePower power : replacement) {
         if (PowersConfigFields.isMajorPowerType(power.getPowerType())) {
            this.majorPowerCount++;
         } else {
            this.minorPowerCount++;
         }
      }
   }

   void refreshPowerStances() {
      this.initializePowerStances();
   }

   public void requestPowerStanceRefreshAfterObfuscation() {
      if (this.livingEntity != null && this.pendingPowerStanceRefresh == null) {
         final long scheduledGeneration = this.powerStanceGeneration;
         this.pendingPowerStanceRefresh = (new BukkitRunnable() {
            public void run() {
               if (scheduledGeneration == EliteEntity.this.powerStanceGeneration) {
                  EliteEntity.this.pendingPowerStanceRefresh = null;
                  EliteEntity.this.visualEffectObfuscated = false;
                  EliteEntity.this.refreshPowerStances();
               }
            }
         }).runTask(MetadataHandler.PLUGIN);
      }
   }

   void suspendPowerStances() {
      this.closePowerStances();
   }

   void suspendUnmanagedPowerRuntimes() {
      this.closeAllPowerRuntimes();
   }

   void resumeUnmanagedPowerRuntimes() {
      try {
         for (ElitePower elitePower : this.elitePowers) {
            if (elitePower instanceof LuaElitePower luaElitePower) {
               luaElitePower.startRuntimeOrThrow();
            } else if (elitePower instanceof EliteScript eliteScript) {
               eliteScript.initializeCustomEvents(this);
            }
         }
      } catch (RuntimeException var5) {
         this.closeAllPowerRuntimes();
         throw var5;
      }
   }

   private void initializePowerStances() {
      this.closePowerStances();
      ArrayList<Runnable> cleanup = new ArrayList<>();
      this.powerStanceCleanup = cleanup;
      Consumer<Runnable> cleanupRegistrar = cleanup::add;

      try {
         new MinorPowerPowerStance(this, cleanupRegistrar);
         new MajorPowerPowerStance(this, cleanupRegistrar);
      } catch (RuntimeException var4) {
         this.closePowerStancesIfCurrent(cleanup);
         throw var4;
      }

      if (this.isPreparedMindSpawn()) {
         this.addPreparedMindSpawnCleanup(() -> this.closePowerStancesIfCurrent(cleanup));
      }
   }

   private void closePowerStances() {
      this.closePowerStancesIfCurrent(this.powerStanceCleanup);
   }

   private void closePowerStancesIfCurrent(List<Runnable> expectedCleanup) {
      if (this.powerStanceCleanup == expectedCleanup) {
         this.powerStanceGeneration++;
         if (this.pendingPowerStanceRefresh != null) {
            this.pendingPowerStanceRefresh.cancel();
            this.pendingPowerStanceRefresh = null;
         }

         List<Runnable> cleanup = this.powerStanceCleanup;
         this.powerStanceCleanup = new ArrayList<>();

         for (int index = cleanup.size() - 1; index >= 0; index--) {
            try {
               cleanup.get(index).run();
            } catch (RuntimeException var5) {
               Logger.warn("Failed to close an EliteMobs power stance: " + var5.getMessage());
            }
         }

         this.minorVisualEffect = false;
         this.majorVisualEffect = false;
      }
   }

   public List<ElitePower> getElitePowersInExecutionOrder() {
      if (this.orderedElitePowers == null) {
         this.orderedElitePowers = PowerExecutionOrder.ordered(this.elitePowers);
      }

      return this.orderedElitePowers;
   }

   public boolean hasPower(ElitePower mobPower) {
      for (ElitePower elitePower : this.elitePowers) {
         if (elitePower.getPowersConfigFields().equals(mobPower.getPowersConfigFields())) {
            return true;
         }
      }

      return false;
   }

   public boolean hasPower(PowersConfigFields mobPower) {
      for (ElitePower elitePower : this.elitePowers) {
         if (elitePower.getPowersConfigFields().equals(mobPower)) {
            return true;
         }
      }

      return false;
   }

   public ElitePower getPower(ElitePower elitePower) {
      for (ElitePower iteratedPower : this.getElitePowers()) {
         if (iteratedPower.getClass().equals(elitePower.getClass())) {
            return iteratedPower;
         }
      }

      return null;
   }

   public ElitePower getPower(String elitePower) {
      for (ElitePower iteratedPower : this.getElitePowers()) {
         if (iteratedPower.getFileName().equals(elitePower)) {
            return iteratedPower;
         }
      }

      return null;
   }

   public void setName(EliteMobProperties eliteMobProperties) {
      this.name = ChatColorConverter.convert(MobLevelPlaceholderFormatter.replaceLevelPlaceholders(eliteMobProperties.getName(), this, this.level));
      this.livingEntity.setCustomName(this.name);
      this.livingEntity.setCustomNameVisible(DefaultConfig.isAlwaysShowNametags());
   }

   private void setDefaultName(EliteMobProperties eliteMobProperties) {
      if (eliteMobProperties != null) {
         this.setName(eliteMobProperties);
      } else {
         this.name = ChatColorConverter.convert(
            MobLevelPlaceholderFormatter.replaceLevelPlaceholders(NativeMindActorDefaults.nameTemplate(this.entityType), this, this.level)
         );
         this.livingEntity.setCustomName(this.name);
         this.livingEntity.setCustomNameVisible(DefaultConfig.isAlwaysShowNametags());
      }
   }

   public void setName(String name, boolean applyToLivingEntity) {
      this.name = name;
      if (applyToLivingEntity) {
         this.livingEntity.setCustomName(name);
      }
   }

   public void setPersistent(boolean bool) {
      this.isPersistent = bool;
      if (this.livingEntity != null) {
         this.livingEntity.setRemoveWhenFarAway(!this.isPersistent);
      }
   }

   public void doCooldown() {
      this.doGlobalPowerCooldown(300);
   }

   public void doGlobalPowerCooldown(int ticks) {
      if (ticks > 0) {
         this.globalPowerCooldownExpiresAt = Math.max(this.globalPowerCooldownExpiresAt, GameClock.getCurrentTick() + ticks);
      }
   }

   public boolean isInCooldown() {
      return this.inCooldown || GameClock.getCurrentTick() < this.globalPowerCooldownExpiresAt;
   }

   public void setInCooldown(boolean inCooldown) {
      this.inCooldown = inCooldown;
      if (!inCooldown) {
         this.globalPowerCooldownExpiresAt = 0L;
      }
   }

   public void setTriggeredAntiExploit(boolean triggeredAntiExploit) {
      if (!triggeredAntiExploit || !this.isEnderDragon()) {
         this.triggeredAntiExploit = triggeredAntiExploit;
         if (triggeredAntiExploit) {
            this.eliteLoot = false;
            this.vanillaLoot = false;
         }
      }
   }

   private void invalidateElitePowerOrder() {
      this.orderedElitePowers = null;
   }

   public void incrementAntiExploit(int value, String cause) {
      if (!this.isEnderDragon()) {
         this.antiExploitPoints += value;
         if (this.antiExploitPoints > AntiExploitConfig.getAntiExploitThreshold()) {
            this.setTriggeredAntiExploit(true);
            AntiExploitMessage.sendWarning(this.livingEntity, cause);
         }
      }
   }

   public void decrementAntiExploit(int value) {
      if (!this.isEnderDragon()) {
         this.antiExploitPoints -= value;
      }
   }

   public void setInAntiExploitCooldown() {
      if (!this.isEnderDragon()) {
         this.inAntiExploitCooldown = true;
         (new BukkitRunnable() {
            public void run() {
               EliteEntity.this.inAntiExploitCooldown = false;
            }
         }).runTaskLater(MetadataHandler.PLUGIN, 20L);
      }
   }

   public void setCombatGracePeriod(int delayInTicks) {
      this.inCombatGracePeriod = true;
      (new BukkitRunnable() {
         public void run() {
            EliteEntity.this.inCombatGracePeriod = false;
         }
      }).runTaskLater(MetadataHandler.PLUGIN, delayInTicks);
   }

   public void addGlobalReinforcement(CustomBossEntity customBossEntity) {
      this.globalReinforcementEntities.add(customBossEntity);
      if (customBossEntity.summoningEntity == null) {
         customBossEntity.setSummoningEntity(this);
      }
   }

   public void addReinforcement(CustomBossEntity customBossEntity) {
      this.eliteReinforcementEntities.add(customBossEntity);
      if (customBossEntity.summoningEntity == null) {
         customBossEntity.setSummoningEntity(this);
      }
   }

   public void addReinforcement(Entity entity) {
      this.nonEliteReinforcementEntities.removeIf(existing -> !existing.isValid());
      this.nonEliteReinforcementEntities.add(entity);
   }

   public boolean isReinforcementOrMount() {
      return this.summoningEntity != null || this instanceof CustomBossEntity customBossEntity && customBossEntity.isMount();
   }

   public boolean isValid() {
      return this.livingEntity == null ? false : this.livingEntity.isValid();
   }

   public boolean exists() {
      return this.livingEntity == null ? false : !this.livingEntity.isDead();
   }

   public Location getLocation() {
      if (this.livingEntity != null) {
         return this.livingEntity.getLocation();
      } else {
         return this.unsyncedLivingEntity != null ? this.unsyncedLivingEntity.getLocation() : null;
      }
   }

   public void setInCombat(boolean inCombat) {
      this.inCombat = inCombat;
      if (!inCombat) {
         this.stopCombatWatchdog();
      }
   }

   public void startCombatWatchdog() {
      this.stopCombatWatchdog();
      final LivingEntity encounterBody = this.livingEntity;
      this.combatWatchdog = (new BukkitRunnable() {
            public void run() {
               if (this.ownsWatchdog() && EliteEntity.this.inCombat && EliteEntity.this.livingEntity == encounterBody) {
                  EliteMobExitCombatEvent.EliteMobExitCombatReason reason = null;
                  if (!EliteEntity.this.isValid()) {
                     reason = EliteMobExitCombatEvent.EliteMobExitCombatReason.ELITE_NOT_VALID;
                  } else if (!EliteEntity.this.isInCombatGracePeriod()) {
                     double followRange = encounterBody.getType() == EntityType.ENDER_DRAGON
                        ? 200.0
                        : AttributeManager.getAttributeBaseValue(encounterBody, "generic_follow_range");
                     if (!EntitySearch.hasNearbyCombatPlayer(EliteEntity.this.getLocation(), followRange)) {
                        reason = EliteMobExitCombatEvent.EliteMobExitCombatReason.NO_NEARBY_PLAYERS;
                     }
                  }

                  if (reason != null) {
                     this.cancel();
                     EliteMobExitCombatEvent.EliteMobExitCombatReason exitReason = reason;
                     Bukkit.getScheduler().runTask(MetadataHandler.PLUGIN, () -> {
                        if (this.ownsWatchdog() && EliteEntity.this.inCombat && EliteEntity.this.livingEntity == encounterBody) {
                           new EventCaller(new EliteMobExitCombatEvent(EliteEntity.this, exitReason));
                        }
                     });
                  }
               } else {
                  this.cancel();
               }
            }

            private boolean ownsWatchdog() {
               return EliteEntity.this.combatWatchdog != null && EliteEntity.this.combatWatchdog.getTaskId() == this.getTaskId();
            }
         })
         .runTaskTimer(MetadataHandler.PLUGIN, 20L, 20L);
   }

   private void stopCombatWatchdog() {
      if (this.combatWatchdog != null) {
         this.combatWatchdog.cancel();
         this.combatWatchdog = null;
      }
   }

   public void remove(RemovalReason removalReason) {
      this.beginRemovalCall();

      try {
         this.stopCombatWatchdog();
         if (this.livingEntity != null) {
            KeepNeutralsAngry.release(this.livingEntity.getUniqueId());
         }

         this.closePowerStances();
         this.closeAllPowerRuntimes(removalReason);
         this.closeEliteLuaPowerBinding();
         this.closePowerSuppression();
         EliteMindServiceModule.detachBehavior(this);
         if (this.livingEntity != null && !(this instanceof CustomBossEntity)) {
            EntityTracker.getEliteMobEntities().remove(this.eliteUUID);
         }

         LivingEntity removedBody = this.livingEntity;
         if (removedBody != null && !removalReason.equals(RemovalReason.DEATH)) {
            removedBody.remove();
         }

         if (removedBody != null && Bukkit.getPluginManager().isPluginEnabled("LibsDisguises")) {
            if (removalReason.equals(RemovalReason.DEATH)) {
               Bukkit.getScheduler().runTaskLater(MetadataHandler.PLUGIN, () -> DisguiseEntity.undisguise(removedBody), 60L);
            } else {
               DisguiseEntity.undisguise(removedBody);
            }
         }

         if (this.livingEntity instanceof EnderDragon enderDragon && removalReason.equals(RemovalReason.DEATH)) {
            enderDragon.setPhase(Phase.DYING);
            if (enderDragon.getDragonBattle() != null) {
               enderDragon.getDragonBattle().generateEndPortal(false);
            }
         }

         this.livingEntity = null;
         this.releaseWorldReferences(removalReason);
         if (!(this instanceof CustomBossEntity)) {
            this.callRemoveEventOnce(removalReason);
         }
      } finally {
         this.finishRemovalCall();
      }
   }

   private void closeEliteLuaPowerBinding() {
      EliteLuaPowerBinding binding = this.eliteLuaPowerBinding;
      if (binding != null) {
         this.eliteLuaPowerBinding = null;

         try {
            binding.close();
         } catch (RuntimeException var3) {
            Logger.warn("Failed to close an EliteMobs Lua power binding: " + var3.getMessage());
         }
      }
   }

   private void closeAllPowerRuntimes() {
      this.closeAllPowerRuntimes(RemovalReason.OTHER);
   }

   private void closeAllPowerRuntimes(RemovalReason removalReason) {
      for (ElitePower elitePower : this.elitePowers) {
         try {
            if (elitePower instanceof EliteScript eliteScript) {
               eliteScript.closeRuntime(removalReason);
            } else {
               elitePower.closeRuntime();
            }
         } catch (RuntimeException var5) {
            Logger.warn("Failed to close an EliteMobs power runtime: " + var5.getMessage());
         }
      }
   }

   private void onPowerPauseReasonChanged(ElitePowerPauseReason reason, boolean paused) {
      EliteLuaPowerBinding binding = this.eliteLuaPowerBinding;
      if (binding != null) {
         try {
            binding.setPauseReason(reason, paused);
         } catch (RuntimeException var9) {
            Logger.warn("Failed to update an EliteMobs Lua power binding pause state: " + var9.getMessage());
         }
      }

      for (ElitePower elitePower : this.elitePowers) {
         if (elitePower instanceof LuaElitePower luaElitePower) {
            try {
               luaElitePower.setRuntimePauseReason(reason, paused);
            } catch (RuntimeException var8) {
               Logger.warn("Failed to update an EliteMobs Lua power pause state: " + var8.getMessage());
            }
         }
      }
   }

   private void applyPowerPauseState(ElitePower elitePower) {
      if (elitePower instanceof LuaElitePower luaElitePower) {
         for (ElitePowerPauseReason reason : ElitePowerPauseReason.values()) {
            if (this.powerSuppression.isSuppressed(reason)) {
               luaElitePower.setRuntimePauseReason(reason, true);
            }
         }
      }
   }

   private void closePowerSuppression() {
      try {
         this.powerSuppression.close();
      } catch (RuntimeException var2) {
         Logger.warn("Failed to close Elite power suppression: " + var2.getMessage());
      }
   }

   private void releaseWorldReferences(RemovalReason removalReason) {
      if (removalReason.equals(RemovalReason.WORLD_UNLOAD) || removalReason.equals(RemovalReason.SHUTDOWN)) {
         this.unsyncedLivingEntity = null;
         this.clearDamagers();
      }
   }

   public boolean referencesWorld(World world) {
      if (world == null) {
         return false;
      } else {
         return this.unsyncedLivingEntity != null && world.equals(this.unsyncedLivingEntity.getWorld())
            ? true
            : this.spawnLocation != null && world.equals(this.spawnLocation.getWorld());
      }
   }

   protected final void callRemoveEventOnce(RemovalReason removalReason) {
      if (!this.removalEventCalled) {
         this.pendingRemovalEventReason = removalReason;
         if (this.removalCallDepth <= 0) {
            this.publishPendingRemovalEvent();
         }
      }
   }

   protected final void beginRemovalCall() {
      this.removalCallDepth++;
   }

   protected final void finishRemovalCall() {
      if (this.removalCallDepth > 0) {
         this.removalCallDepth--;
         if (this.removalCallDepth == 0) {
            this.publishPendingRemovalEvent();
         }
      }
   }

   private void publishPendingRemovalEvent() {
      if (!this.removalEventCalled && this.pendingRemovalEventReason != null) {
         RemovalReason removalReason = this.pendingRemovalEventReason;
         this.pendingRemovalEventReason = null;
         this.removalEventCalled = true;
         new EventCaller(new EliteMobRemoveEvent(this, removalReason));
      }
   }

   public void removeReinforcement(CustomBossEntity customBossEntity) {
      this.eliteReinforcementEntities.remove(customBossEntity);
      this.globalReinforcementEntities.remove(customBossEntity);
   }

   public int getGlobalReinforcementsCount() {
      return this.globalReinforcementEntities.size();
   }

   public HashSet<String> getTags() {
      return this.customMetadata;
   }

   public boolean hasTag(String tag) {
      return this.customMetadata.contains(tag);
   }

   public void addTag(String tag) {
      this.customMetadata.add(tag);
   }

   public void addTags(List<String> tags) {
      this.customMetadata.addAll(tags);
   }

   public void removeTag(String tag) {
      this.customMetadata.remove(tag);
   }

   public void removeTags(List<String> tags) {
      this.customMetadata.removeAll(tags);
   }

   public boolean isSharedCooldownReady(String key) {
      this.cleanupSharedCooldown(key);
      return !this.sharedCooldowns.containsKey(key);
   }

   public long getSharedCooldownRemainingTicks(String key) {
      this.cleanupSharedCooldown(key);
      Long expiresAt = this.sharedCooldowns.get(key);
      if (expiresAt == null) {
         return 0L;
      } else {
         long remainingNanos = expiresAt - System.nanoTime();
         if (remainingNanos <= 0L) {
            this.sharedCooldowns.remove(key);
            return 0L;
         } else {
            return Math.max(1L, remainingNanos / 50000000L);
         }
      }
   }

   public void setSharedCooldown(String key, long ticks) {
      if (ticks <= 0L) {
         this.sharedCooldowns.remove(key);
      } else {
         this.sharedCooldowns.put(key, System.nanoTime() + ticks * 50000000L);
      }
   }

   private void cleanupSharedCooldown(String key) {
      Long expiresAt = this.sharedCooldowns.get(key);
      if (expiresAt != null && expiresAt <= System.nanoTime()) {
         this.sharedCooldowns.remove(key);
      }
   }

   @Generated
   public HashSet<ElitePower> getElitePowers() {
      return this.elitePowers;
   }

   @Generated
   public int getLevel() {
      return this.level;
   }

   @Generated
   public void setLevel(int level) {
      this.level = level;
   }

   @Generated
   public double getMaxHealth() {
      return this.maxHealth;
   }

   @Generated
   public String getName() {
      return this.name;
   }

   @Generated
   public int getMinorPowerCount() {
      return this.minorPowerCount;
   }

   @Generated
   public int getMajorPowerCount() {
      return this.majorPowerCount;
   }

   @Generated
   public boolean isMinorVisualEffect() {
      return this.minorVisualEffect;
   }

   @Generated
   public void setMinorVisualEffect(boolean minorVisualEffect) {
      this.minorVisualEffect = minorVisualEffect;
   }

   @Generated
   public boolean isMajorVisualEffect() {
      return this.majorVisualEffect;
   }

   @Generated
   public void setMajorVisualEffect(boolean majorVisualEffect) {
      this.majorVisualEffect = majorVisualEffect;
   }

   @Generated
   public boolean isVisualEffectObfuscated() {
      return this.visualEffectObfuscated;
   }

   @Generated
   public void setVisualEffectObfuscated(boolean visualEffectObfuscated) {
      this.visualEffectObfuscated = visualEffectObfuscated;
   }

   @Generated
   public boolean isNaturalEntity() {
      return this.isNaturalEntity;
   }

   @Generated
   public void setNaturalEntity(boolean isNaturalEntity) {
      this.isNaturalEntity = isNaturalEntity;
   }

   @Generated
   public Boolean getIsPersistent() {
      return this.isPersistent;
   }

   @Generated
   public boolean isVanillaLoot() {
      return this.vanillaLoot;
   }

   @Generated
   public void setVanillaLoot(boolean vanillaLoot) {
      this.vanillaLoot = vanillaLoot;
   }

   @Generated
   public boolean isEliteLoot() {
      return this.eliteLoot;
   }

   @Generated
   public void setEliteLoot(boolean eliteLoot) {
      this.eliteLoot = eliteLoot;
   }

   @Generated
   public boolean isRandomLoot() {
      return this.randomLoot;
   }

   @Generated
   public void setRandomLoot(boolean randomLoot) {
      this.randomLoot = randomLoot;
   }

   @Generated
   public SpawnReason getSpawnReason() {
      return this.spawnReason;
   }

   @Generated
   public double getHealthMultiplier() {
      return this.healthMultiplier;
   }

   @Generated
   public void setHealthMultiplier(double healthMultiplier) {
      this.healthMultiplier = healthMultiplier;
   }

   @Generated
   public double getDamageMultiplier() {
      return this.damageMultiplier;
   }

   @Generated
   public void setDamageMultiplier(double damageMultiplier) {
      this.damageMultiplier = damageMultiplier;
   }

   @Generated
   public boolean isTriggeredAntiExploit() {
      return this.triggeredAntiExploit;
   }

   @Generated
   public boolean isInAntiExploitCooldown() {
      return this.inAntiExploitCooldown;
   }

   @Generated
   public boolean isInCombat() {
      return this.inCombat;
   }

   @Generated
   public boolean isInCombatGracePeriod() {
      return this.inCombatGracePeriod;
   }

   @Generated
   public EliteEntity getSummoningEntity() {
      return this.summoningEntity;
   }

   @Generated
   public void setSummoningEntity(EliteEntity summoningEntity) {
      this.summoningEntity = summoningEntity;
   }

   @Generated
   public void setSpawnLocation(Location spawnLocation) {
      this.spawnLocation = spawnLocation;
   }

   @Generated
   public boolean isDying() {
      return this.dying;
   }

   @Generated
   public void setDying(boolean dying) {
      this.dying = dying;
   }

   @Generated
   public boolean isHealing() {
      return this.healing;
   }

   @Generated
   public void setHealing(boolean healing) {
      this.healing = healing;
   }

   @Generated
   public ElitePowerSuppression getPowerSuppression() {
      return this.powerSuppression;
   }

   private final class TrackedElitePowerSet extends LinkedHashSet<ElitePower> {
      public boolean add(ElitePower elitePower) {
         this.requirePowerMutationAllowed();
         boolean changed = super.add(elitePower);
         if (changed) {
            EliteEntity.this.applyPowerPauseState(elitePower);
            EliteEntity.this.invalidateElitePowerOrder();
         }

         return changed;
      }

      @Override
      public boolean addAll(Collection<? extends ElitePower> collection) {
         this.requirePowerMutationAllowed();
         boolean changed = super.addAll(collection);
         if (changed) {
            collection.forEach(EliteEntity.this::applyPowerPauseState);
            EliteEntity.this.invalidateElitePowerOrder();
         }

         return changed;
      }

      @Override
      public boolean remove(Object object) {
         this.requirePowerMutationAllowed();
         boolean changed = super.remove(object);
         if (changed) {
            EliteEntity.this.invalidateElitePowerOrder();
         }

         return changed;
      }

      @Override
      public boolean removeAll(Collection<?> collection) {
         this.requirePowerMutationAllowed();
         boolean changed = super.removeAll(collection);
         if (changed) {
            EliteEntity.this.invalidateElitePowerOrder();
         }

         return changed;
      }

      @Override
      public boolean retainAll(Collection<?> collection) {
         this.requirePowerMutationAllowed();
         boolean changed = super.retainAll(collection);
         if (changed) {
            EliteEntity.this.invalidateElitePowerOrder();
         }

         return changed;
      }

      @Override
      public void clear() {
         this.requirePowerMutationAllowed();
         if (!this.isEmpty()) {
            super.clear();
            EliteEntity.this.invalidateElitePowerOrder();
         }
      }

      @Override
      public Iterator<ElitePower> iterator() {
         final Iterator<ElitePower> delegate = super.iterator();
         return new Iterator<ElitePower>() {
            @Override
            public boolean hasNext() {
               return delegate.hasNext();
            }

            public ElitePower next() {
               return delegate.next();
            }

            @Override
            public void remove() {
               TrackedElitePowerSet.this.requirePowerMutationAllowed();
               delegate.remove();
               EliteEntity.this.invalidateElitePowerOrder();
            }
         };
      }

      private void requirePowerMutationAllowed() {
         if (!EliteEntity.this.serviceManagedPowerMutation && EliteEntity.this.eliteLuaPowerBinding != null) {
            throw new IllegalStateException("Service-managed Lua powers must be replaced through EliteLuaPowerService");
         }
      }
   }
}
