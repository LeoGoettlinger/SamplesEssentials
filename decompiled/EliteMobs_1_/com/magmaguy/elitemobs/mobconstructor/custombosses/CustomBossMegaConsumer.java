package com.magmaguy.elitemobs.mobconstructor.custombosses;

import com.magmaguy.elitemobs.EliteMobs;
import com.magmaguy.elitemobs.config.DefaultConfig;
import com.magmaguy.elitemobs.config.custombosses.CustomBossesConfigFields;
import com.magmaguy.elitemobs.entitytracker.EntityTracker;
import com.magmaguy.elitemobs.magmacore.util.AttributeManager;
import com.magmaguy.elitemobs.magmacore.util.ChatColorConverter;
import com.magmaguy.elitemobs.magmacore.util.Logger;
import com.magmaguy.elitemobs.mobconstructor.EliteMindServiceModule;
import com.magmaguy.elitemobs.mobconstructor.MobLevelPlaceholderFormatter;
import com.magmaguy.elitemobs.powers.meta.ElitePower;
import com.magmaguy.elitemobs.tagger.PersistentTagger;
import com.magmaguy.elitemobs.thirdparty.custommodels.CustomModel;
import com.magmaguy.elitemobs.thirdparty.libsdisguises.DisguiseEntity;
import com.magmaguy.elitemobs.thirdparty.worldguard.WorldGuardFlagChecker;
import com.magmaguy.elitemobs.thirdparty.worldguard.WorldGuardSpawnEventBypasser;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Function;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Ageable;
import org.bukkit.entity.EnderDragon;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Slime;
import org.bukkit.entity.EnderDragon.Phase;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.inventory.meta.ItemMeta;

public class CustomBossMegaConsumer {
   private static final Set<String> NAMETAG_BONE_WARNINGS = ConcurrentHashMap.newKeySet();
   private final CustomBossesConfigFields customBossesConfigFields;
   private final HashSet<ElitePower> powers;
   private final int level;
   private final boolean bypassesWorldGuardSpawn;
   private final Location spawnLocation;
   CustomBossEntity customBossEntity;

   public CustomBossMegaConsumer(CustomBossEntity customBossEntity) {
      this.customBossesConfigFields = customBossEntity.getCustomBossesConfigFields();
      this.customBossEntity = customBossEntity;
      if (customBossEntity.getRespawnOverrideLocation() != null) {
         this.spawnLocation = customBossEntity.getRespawnOverrideLocation();
         customBossEntity.setRespawnOverrideLocation(null);
      } else {
         this.spawnLocation = customBossEntity.getSpawnLocation();
      }

      this.powers = customBossEntity.getElitePowers();
      this.level = customBossEntity.getLevel();
      this.bypassesWorldGuardSpawn = customBossEntity.getBypassesProtections();
   }

   protected static void setName(LivingEntity livingEntity, CustomBossEntity customBossEntity, int level) {
      String parsedName = parseName(customBossEntity, level);
      livingEntity.setCustomName(parsedName);
      boolean showName = DefaultConfig.isAlwaysShowNametags() || customBossEntity.customBossesConfigFields.isAlwaysShowName();
      livingEntity.setCustomNameVisible(showName);
      if (Bukkit.getPluginManager().isPluginEnabled("LibsDisguises")) {
         DisguiseEntity.setDisguiseNameVisibility(showName, livingEntity, parsedName);
      }

      customBossEntity.setName(parsedName, false);
   }

   protected static String parseName(CustomBossEntity customBossEntity, int level) {
      return ChatColorConverter.convert(
         MobLevelPlaceholderFormatter.replaceLevelPlaceholders(customBossEntity.customBossesConfigFields.getName(), customBossEntity, level)
      );
   }

   public LivingEntity spawn() {
      return this.spawn(null);
   }

   public LivingEntity spawn(Function<Consumer<LivingEntity>, LivingEntity> bodyFactory) {
      if (this.spawnLocation == null) {
         Logger.warn(
            "Custom Boss Entity "
               + this.customBossesConfigFields.getFilename()
               + " tried to spawn without a valid spawn location getting assigned! Report this to the developer!"
         );
         return null;
      } else {
         if (bodyFactory == null) {
            try {
               bodyFactory = EliteMindServiceModule.behaviorBodyFactory(this.customBossEntity, this.spawnLocation);
            } catch (IllegalStateException | IllegalArgumentException var8) {
               Logger.warn("Cannot spawn " + this.customBossesConfigFields.getFilename() + ": " + var8.getMessage());
               return null;
            }
         }

         if (EliteMobs.worldGuardIsEnabled) {
            if (!WorldGuardFlagChecker.doEliteMobsSpawnFlag(this.spawnLocation)) {
               Logger.warn(
                  "Attempted to spawn "
                     + this.customBossesConfigFields.getFilename()
                     + " in location "
                     + this.spawnLocation
                     + " which is protected by WorldGuard with elitemobs-spawning deny! This should not have happened."
               );
               return null;
            }

            if (this.bypassesWorldGuardSpawn || this.customBossEntity instanceof RegionalBossEntity) {
               WorldGuardSpawnEventBypasser.forceSpawn();
            }
         }

         LivingEntity livingEntity = null;
         boolean accepted = false;

         LivingEntity var4;
         try {
            livingEntity = bodyFactory == null
               ? (LivingEntity)this.spawnLocation
                  .getWorld()
                  .spawn(
                     this.spawnLocation,
                     this.customBossesConfigFields.getEntityType().getEntityClass(),
                     false,
                     entity -> this.applyBossFeatures((LivingEntity)entity)
                  )
               : bodyFactory.apply(this::applyBossFeatures);
            if (livingEntity == null || !livingEntity.isValid()) {
               return null;
            }

            this.setDisguise(livingEntity);
            this.setCustomModel(livingEntity);
            this.customBossEntity.setLivingEntity(livingEntity, SpawnReason.CUSTOM);
            accepted = this.customBossEntity.getLivingEntity() == livingEntity
               && livingEntity.isValid()
               && EntityTracker.getEliteMobEntities().get(this.customBossEntity.getEliteUUID()) == this.customBossEntity;
            var4 = accepted ? livingEntity : null;
         } finally {
            if (!accepted) {
               this.customBossEntity.discardFailedMaterialization(livingEntity == null ? this.customBossEntity.getUnsyncedLivingEntity() : livingEntity);
            }
         }

         return var4;
      }
   }

   private void setBaby(LivingEntity livingEntity) {
      if (livingEntity instanceof Ageable) {
         if (this.customBossesConfigFields.isBaby()) {
            ((Ageable)livingEntity).setBaby();
         } else {
            ((Ageable)livingEntity).setAdult();
         }
      }
   }

   private void setDisguise(LivingEntity livingEntity) {
      if (this.customBossesConfigFields.getDisguise() != null
         && (
            !CustomModel.customModelsEnabled()
               || !this.customBossesConfigFields.isCustomModelExists()
               || this.customBossesConfigFields.getCustomModel() == null
               || this.customBossesConfigFields.getCustomModel().isEmpty()
         )) {
         if (Bukkit.getPluginManager().isPluginEnabled("LibsDisguises")) {
            try {
               DisguiseEntity.disguise(
                  this.customBossesConfigFields.getDisguise(),
                  livingEntity,
                  this.customBossesConfigFields.getCustomDisguiseData(),
                  this.customBossesConfigFields.getFilename()
               );
            } catch (Exception var3) {
               Logger.warn("Failed to load LibsDisguises disguise correctly!");
            }
         }
      }
   }

   private void setCustomModel(LivingEntity livingEntity) {
      if (this.customBossesConfigFields.isCustomModelExists()) {
         if (this.customBossesConfigFields.getCustomModel() != null && !this.customBossesConfigFields.getCustomModel().isEmpty()) {
            try {
               this.customBossEntity
                  .setCustomModel(
                     CustomModel.generateCustomModel(livingEntity, this.customBossesConfigFields.getCustomModel(), this.customBossEntity.getName())
                  );
               this.warnIfNameCanNeverRender();
            } catch (Exception var3) {
               this.customBossEntity.setCustomModel(null);
               Logger.warn("Failed to initialize Custom Model for Custom Boss " + this.customBossesConfigFields.getFilename());
               var3.printStackTrace();
            }
         }
      }
   }

   private void warnIfNameCanNeverRender() {
      CustomModel customModel = this.customBossEntity.getCustomModel();
      if (customModel != null) {
         if (nameCanNeverRender(
            DefaultConfig.isAlwaysShowNametags(),
            this.customBossesConfigFields.isAlwaysShowName(),
            this.customBossesConfigFields.getName(),
            customModel.hasNametagBone()
         )) {
            if (NAMETAG_BONE_WARNINGS.add(this.customBossesConfigFields.getFilename())) {
               Logger.warn(
                  "Custom Boss "
                     + this.customBossesConfigFields.getFilename()
                     + " asks for a visible name but its model \""
                     + this.customBossesConfigFields.getCustomModel()
                     + "\" has no nametag anchor bone, so the name can never be shown. Add a bone whose name starts with \"tag_\" (for example \"tag_head\") to that model."
               );
            }
         }
      }
   }

   static boolean nameCanNeverRender(boolean alwaysShowNametagsGlobally, boolean alwaysShowNameForBoss, String configuredName, boolean modelHasNametagBone) {
      if (modelHasNametagBone) {
         return false;
      } else {
         return !alwaysShowNametagsGlobally && !alwaysShowNameForBoss ? false : configuredName != null && !configuredName.isEmpty();
      }
   }

   private void setFrozen(LivingEntity livingEntity) {
      if (this.customBossesConfigFields.isFrozen()) {
         AttributeManager.setAttribute(livingEntity, "generic_movement_speed", 0.0);
      }
   }

   private void setEquipment(LivingEntity livingEntity) {
      try {
         livingEntity.getEquipment().setHelmet(this.customBossesConfigFields.getHelmet());
         livingEntity.getEquipment().setChestplate(this.customBossesConfigFields.getChestplate());
         livingEntity.getEquipment().setLeggings(this.customBossesConfigFields.getLeggings());
         livingEntity.getEquipment().setBoots(this.customBossesConfigFields.getBoots());
         livingEntity.getEquipment().setItemInMainHand(this.customBossesConfigFields.getMainHand());
         livingEntity.getEquipment().setItemInOffHand(this.customBossesConfigFields.getOffHand());
         if (livingEntity.getEquipment().getHelmet() != null) {
            ItemMeta helmetMeta = livingEntity.getEquipment().getHelmet().getItemMeta();
            if (helmetMeta != null) {
               helmetMeta.setUnbreakable(true);
               livingEntity.getEquipment().getHelmet().setItemMeta(helmetMeta);
            }
         }
      } catch (Exception var3) {
         Logger.warn("Tried to assign a material slot to an invalid entity! Boss is from file" + this.customBossesConfigFields.getFilename());
      }
   }

   public void applyBossFeatures(LivingEntity livingEntity) {
      this.customBossEntity.setUnsyncedLivingEntity(livingEntity);
      PersistentTagger.tagElite(livingEntity, this.customBossEntity.getEliteUUID());

      for (ElitePower elitePower : this.powers) {
         elitePower.applyPowers(livingEntity);
      }

      this.setEquipment(livingEntity);
      this.setBaby(livingEntity);
      setName(livingEntity, this.customBossEntity, this.level);
      if (livingEntity.isValid()) {
         this.setDisguise(livingEntity);
      }

      this.setFollowRange(livingEntity);
      this.setMovementSpeed(livingEntity);
      this.setFrozen(livingEntity);
      this.setScale(livingEntity);
      this.setSilent(livingEntity);
      this.setAI(livingEntity);
      this.customBossEntity.setMovementSpeedAttribute(AttributeManager.getAttributeBaseValue(livingEntity, "generic_movement_speed"));
      this.customBossEntity.setFollowDistance(AttributeManager.getAttributeBaseValue(livingEntity, "generic_follow_range"));
      if (livingEntity.getType().equals(EntityType.ENDER_DRAGON)) {
         ((EnderDragon)livingEntity).setPhase(Phase.CIRCLING);
         if (((EnderDragon)livingEntity).getDragonBattle() != null) {
            ((EnderDragon)livingEntity).getDragonBattle().generateEndPortal(false);
         }
      }

      if (livingEntity instanceof Slime) {
         ((Slime)livingEntity).setSize(this.customBossEntity.getCustomBossesConfigFields().getSlimeSize());
      }
   }

   private void setFollowRange(LivingEntity livingEntity) {
      if (this.customBossesConfigFields.getFollowDistance() != null && this.customBossesConfigFields.getFollowDistance() > 0 && livingEntity instanceof Mob) {
         AttributeManager.setAttribute(livingEntity, "generic_follow_range", this.customBossesConfigFields.getFollowDistance().intValue());
      }
   }

   private void setMovementSpeed(LivingEntity livingEntity) {
      if (this.customBossesConfigFields.getMovementSpeedAttribute() != null) {
         AttributeManager.setAttribute(livingEntity, "generic_movement_speed", this.customBossesConfigFields.getMovementSpeedAttribute());
      }
   }

   private void setScale(LivingEntity livingEntity) {
      if (this.customBossesConfigFields.getScale() != 1.0) {
         AttributeManager.setAttribute(livingEntity, "generic_scale", this.customBossesConfigFields.getScale());
      }
   }

   private void setSilent(LivingEntity livingEntity) {
      if (this.customBossesConfigFields.isSilent()) {
         livingEntity.setSilent(true);
      }
   }

   private void setAI(LivingEntity livingEntity) {
      if (!this.customBossesConfigFields.isAi() && livingEntity instanceof Mob mob) {
         mob.setAI(false);
      }
   }
}
