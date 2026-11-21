package com.magmaguy.elitemobs.powers.meta;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.api.EliteMobDamagedByPlayerEvent;
import com.magmaguy.elitemobs.api.PlayerDamagedByEliteMobEvent;
import com.magmaguy.elitemobs.config.CustomConfigFields;
import com.magmaguy.elitemobs.config.powers.LuaPowerConfigFields;
import com.magmaguy.elitemobs.config.powers.PowersConfigFields;
import com.magmaguy.elitemobs.magmacore.util.Logger;
import com.magmaguy.elitemobs.mobconstructor.EliteEntity;
import com.magmaguy.elitemobs.powers.lua.LuaElitePower;
import com.magmaguy.elitemobs.powers.scripts.EliteScript;
import com.magmaguy.elitemobs.shaded.reflections.Reflections;
import com.magmaguy.elitemobs.utils.GameClock;
import java.util.HashMap;
import java.util.HashSet;
import lombok.Generated;
import org.bukkit.entity.LivingEntity;
import org.bukkit.scheduler.BukkitRunnable;

public class ElitePower {
   private static final HashMap<String, CustomConfigFields> elitePowers = new HashMap<>();
   private static final HashSet<CustomConfigFields> bossPowers = new HashSet<>();
   private static final HashSet<CustomConfigFields> majorPowers = new HashSet<>();
   private static final HashSet<CustomConfigFields> defensivePowers = new HashSet<>();
   private static final HashSet<CustomConfigFields> miscellaneousPowers = new HashSet<>();
   private static final HashSet<CustomConfigFields> offensivePowers = new HashSet<>();
   private static final HashSet<CustomConfigFields> specialPowers = new HashSet<>();
   private final String fileName;
   private final CustomConfigFields powersConfigFields;
   private String trail = null;
   private int powerCooldownTime = 0;
   private int globalCooldownTime = 0;
   private boolean inGlobalCooldown = false;
   private long powerCooldownExpiresAt;
   private boolean isFiring = false;
   private EliteEntity ownerEntity = null;

   public ElitePower(CustomConfigFields customConfigFields) {
      this.fileName = customConfigFields.getFilename();
      this.powersConfigFields = customConfigFields;
   }

   public ElitePower(PowersConfigFields powersConfigFields) {
      this.powersConfigFields = powersConfigFields;
      this.fileName = powersConfigFields.getFilename();
      this.trail = powersConfigFields.getEffect();
      this.powerCooldownTime = powersConfigFields.getPowerCooldown();
      this.globalCooldownTime = powersConfigFields.getGlobalCooldown();
   }

   public static void addPower(EliteEntity eliteEntity, PowersConfigFields configFields) {
      if (configFields instanceof LuaPowerConfigFields luaPowerConfigFields) {
         LuaElitePower luaElitePower = new LuaElitePower(luaPowerConfigFields);
         luaElitePower.setOwnerEntity(eliteEntity);
         eliteEntity.getElitePowers().add(luaElitePower);
         luaElitePower.applyPowers(eliteEntity.getLivingEntity());
      } else {
         if (configFields.getEliteScriptBlueprints().isEmpty()) {
            try {
               ElitePower elitePower = configFields.getElitePowerClass().newInstance();
               elitePower.setOwnerEntity(eliteEntity);
               eliteEntity.getElitePowers().add(elitePower);
               elitePower.applyPowers(eliteEntity.getLivingEntity());
            } catch (Exception var4) {
               Logger.warn("Failed to assign power for config field " + configFields.getFilename());
            }
         } else {
            eliteEntity.getElitePowers().addAll(EliteScript.generateBossScripts(configFields.getEliteScriptBlueprints(), eliteEntity));
            eliteEntity.getElitePowers().forEach(power -> {
               if (power.getOwnerEntity() == null) {
                  power.setOwnerEntity(eliteEntity);
               }
            });
         }
      }
   }

   public static void shutdown() {
      elitePowers.clear();
      bossPowers.clear();
      majorPowers.clear();
      defensivePowers.clear();
      miscellaneousPowers.clear();
      offensivePowers.clear();
      specialPowers.clear();
   }

   public static void initializePowers() {
      Reflections reflections = new Reflections("com.magmaguy.elitemobs.powers");
      reflections.getSubTypesOf(ElitePower.class).forEach(power -> {
         try {
            ElitePower thisPower = power.newInstance();
            switch (((PowersConfigFields)thisPower.getPowersConfigFields()).getPowerType()) {
               case DEFENSIVE:
                  defensivePowers.add(thisPower.getPowersConfigFields());
                  break;
               case OFFENSIVE:
                  offensivePowers.add(thisPower.getPowersConfigFields());
                  break;
               case MAJOR_BLAZE:
               case MAJOR_ENDERMAN:
               case MAJOR_SKELETON:
               case MAJOR_GHAST:
               case MAJOR_ZOMBIE:
                  majorPowers.add(thisPower.getPowersConfigFields());
                  break;
               case MISCELLANEOUS:
                  miscellaneousPowers.add(thisPower.getPowersConfigFields());
            }

            elitePowers.put(thisPower.getFileName(), thisPower.getPowersConfigFields());
         } catch (Exception var2) {
         }
      });
   }

   protected static boolean eventIsValid(EliteMobDamagedByPlayerEvent event, ElitePower elitePower, boolean ignoreGlobalCooldown) {
      if (event.isCancelled()) {
         return false;
      } else if (event.getEliteMobEntity().getPowerSuppression().isSuppressed()) {
         return false;
      } else if (event.getEliteMobEntity().getLivingEntity() == null) {
         return false;
      } else if (!event.getEliteMobEntity().isAIActive()) {
         return false;
      } else if (ignoreGlobalCooldown) {
         return true;
      } else {
         return elitePower.isInGlobalCooldown() ? false : !event.getEliteMobEntity().isInCooldown();
      }
   }

   protected static boolean eventIsValid(EliteMobDamagedByPlayerEvent event, ElitePower elitePower) {
      if (event.isCancelled()) {
         return false;
      } else if (event.getEliteMobEntity().getPowerSuppression().isSuppressed()) {
         return false;
      } else if (event.getEliteMobEntity().getLivingEntity() == null) {
         return false;
      } else if (!event.getEliteMobEntity().isAIActive()) {
         return false;
      } else if (elitePower.isInGlobalCooldown()) {
         return false;
      } else {
         return elitePower.isInCooldown(event.getEliteMobEntity()) ? false : !event.getEliteMobEntity().isInCooldown();
      }
   }

   protected static boolean eventIsValid(PlayerDamagedByEliteMobEvent event, ElitePower elitePower) {
      if (event.isCancelled()) {
         return false;
      } else if (event.getEliteMobEntity().getPowerSuppression().isSuppressed()) {
         return false;
      } else if (event.getEliteMobEntity().getLivingEntity() == null) {
         return false;
      } else if (!event.getEliteMobEntity().isAIActive()) {
         return false;
      } else {
         return elitePower.isInGlobalCooldown() ? false : !event.getEliteMobEntity().isInCooldown();
      }
   }

   public void applyPowers(LivingEntity livingEntity) {
   }

   public int getExecutionPriority() {
      return 0;
   }

   public void closeRuntime() {
   }

   public PowersConfigFields.PowerType getPowerType() {
      return this.powersConfigFields instanceof PowersConfigFields fields ? fields.getPowerType() : null;
   }

   public static void registerConfiguredPower(PowersConfigFields configFields) {
      elitePowers.put(configFields.getFilename(), configFields);
      if (configFields.getPowerType() != null) {
         switch (configFields.getPowerType()) {
            case DEFENSIVE:
               defensivePowers.add(configFields);
               break;
            case OFFENSIVE:
               offensivePowers.add(configFields);
               break;
            case MAJOR_BLAZE:
            case MAJOR_ENDERMAN:
            case MAJOR_SKELETON:
            case MAJOR_GHAST:
            case MAJOR_ZOMBIE:
               majorPowers.add(configFields);
               break;
            case MISCELLANEOUS:
               miscellaneousPowers.add(configFields);
               break;
            case UNIQUE:
               specialPowers.add(configFields);
         }
      }
   }

   public boolean isInCooldown(EliteEntity eliteEntity) {
      return this.isPowerCooldownActive() || eliteEntity.isInCooldown();
   }

   public boolean isPowerCooldownActive() {
      return GameClock.getCurrentTick() < this.powerCooldownExpiresAt;
   }

   public void setInCooldown(EliteEntity eliteEntity, boolean inCooldown) {
      eliteEntity.setInCooldown(inCooldown);
      this.setInGlobalCooldown(inCooldown);
   }

   public void doCooldown(EliteEntity eliteEntity) {
      this.applyCooldowns(eliteEntity, 20);
   }

   public void doCooldownTicks(EliteEntity eliteEntity) {
      this.applyCooldowns(eliteEntity, 1);
   }

   private void applyCooldowns(EliteEntity eliteEntity, int ticksPerUnit) {
      if (this.globalCooldownTime > 0) {
         eliteEntity.doGlobalPowerCooldown(Math.multiplyExact(this.globalCooldownTime, ticksPerUnit));
      }

      if (this.powerCooldownTime > 0) {
         this.powerCooldownExpiresAt = Math.max(this.powerCooldownExpiresAt, GameClock.getCurrentTick() + (long)this.powerCooldownTime * ticksPerUnit);
      }
   }

   protected void doGlobalCooldown(int ticks, EliteEntity eliteEntity) {
      this.setInGlobalCooldown(true);
      eliteEntity.doCooldown();
      (new BukkitRunnable() {
         public void run() {
            ElitePower.this.setInGlobalCooldown(false);
         }
      }).runTaskLater(MetadataHandler.PLUGIN, ticks);
   }

   protected void doGlobalCooldown(int ticks) {
      this.setInGlobalCooldown(true);
      (new BukkitRunnable() {
         public void run() {
            ElitePower.this.setInGlobalCooldown(false);
         }
      }).runTaskLater(MetadataHandler.PLUGIN, ticks);
   }

   @Generated
   public static HashMap<String, CustomConfigFields> getElitePowers() {
      return elitePowers;
   }

   @Generated
   public static HashSet<CustomConfigFields> getBossPowers() {
      return bossPowers;
   }

   @Generated
   public static HashSet<CustomConfigFields> getMajorPowers() {
      return majorPowers;
   }

   @Generated
   public static HashSet<CustomConfigFields> getDefensivePowers() {
      return defensivePowers;
   }

   @Generated
   public static HashSet<CustomConfigFields> getMiscellaneousPowers() {
      return miscellaneousPowers;
   }

   @Generated
   public static HashSet<CustomConfigFields> getOffensivePowers() {
      return offensivePowers;
   }

   @Generated
   public static HashSet<CustomConfigFields> getSpecialPowers() {
      return specialPowers;
   }

   @Generated
   public String getFileName() {
      return this.fileName;
   }

   @Generated
   public CustomConfigFields getPowersConfigFields() {
      return this.powersConfigFields;
   }

   @Generated
   public String getTrail() {
      return this.trail;
   }

   @Generated
   public int getPowerCooldownTime() {
      return this.powerCooldownTime;
   }

   @Generated
   public void setPowerCooldownTime(int powerCooldownTime) {
      this.powerCooldownTime = powerCooldownTime;
   }

   @Generated
   public int getGlobalCooldownTime() {
      return this.globalCooldownTime;
   }

   @Generated
   public void setGlobalCooldownTime(int globalCooldownTime) {
      this.globalCooldownTime = globalCooldownTime;
   }

   @Generated
   public boolean isInGlobalCooldown() {
      return this.inGlobalCooldown;
   }

   @Generated
   public void setInGlobalCooldown(boolean inGlobalCooldown) {
      this.inGlobalCooldown = inGlobalCooldown;
   }

   @Generated
   public boolean isFiring() {
      return this.isFiring;
   }

   @Generated
   public void setFiring(boolean isFiring) {
      this.isFiring = isFiring;
   }

   @Generated
   public EliteEntity getOwnerEntity() {
      return this.ownerEntity;
   }

   @Generated
   public void setOwnerEntity(EliteEntity ownerEntity) {
      this.ownerEntity = ownerEntity;
   }
}
