package com.magmaguy.elitemobs.thirdparty.custommodels;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.api.EliteMobDeathEvent;
import com.magmaguy.elitemobs.entitytracker.EntityTracker;
import com.magmaguy.elitemobs.mobconstructor.EliteEntity;
import com.magmaguy.elitemobs.mobconstructor.custombosses.CustomBossEntity;
import com.magmaguy.elitemobs.thirdparty.custommodels.freeminecraftmodels.CustomModelFMM;
import com.magmaguy.elitemobs.thirdparty.custommodels.modelengine.CustomModelMEG;
import com.magmaguy.elitemobs.thirdparty.custommodels.modelengine.ModelEngineChecker;
import com.magmaguy.freeminecraftmodels.customentity.ModeledEntityLeftClickCallback;
import com.magmaguy.freeminecraftmodels.customentity.ModeledEntityRightClickCallback;
import java.util.List;
import lombok.Generated;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntitySpawnEvent;

public class CustomModel implements CustomModelInterface {
   private static final boolean usingModels = false;
   private static CustomModel.ModelPlugin modelPlugin;
   private CustomModelMEG customModelMEG;
   private CustomModelFMM customModelFMM;
   private boolean initialized = false;

   private CustomModel(LivingEntity livingEntity, String modelName, String nametagName) {
      switch (modelPlugin) {
         case FREE_MINECRAFT_MODELS:
            this.customModelFMM = new CustomModelFMM(livingEntity, modelName, nametagName);
            this.initialized = this.customModelFMM.getDynamicEntity() != null;
            break;
         case MODEL_ENGINE:
            this.customModelMEG = new CustomModelMEG(livingEntity, modelName, nametagName);
            this.initialized = true;
      }
   }

   private CustomModel(
      LivingEntity livingEntity,
      String modelName,
      String nametagName,
      ModeledEntityLeftClickCallback leftClickCallback,
      ModeledEntityRightClickCallback rightClickCallback
   ) {
      switch (modelPlugin) {
         case FREE_MINECRAFT_MODELS:
            this.customModelFMM = new CustomModelFMM(livingEntity, modelName, nametagName, leftClickCallback, rightClickCallback);
            this.initialized = this.customModelFMM.getDynamicEntity() != null;
            break;
         case MODEL_ENGINE:
            this.customModelMEG = new CustomModelMEG(livingEntity, modelName, nametagName);
            this.initialized = true;
      }
   }

   public static void initialize() {
      if (Bukkit.getPluginManager().isPluginEnabled("FreeMinecraftModels")) {
         modelPlugin = CustomModel.ModelPlugin.FREE_MINECRAFT_MODELS;
      } else if (ModelEngineChecker.modelEngineIsInstalled()) {
         modelPlugin = CustomModel.ModelPlugin.MODEL_ENGINE;
      } else {
         modelPlugin = CustomModel.ModelPlugin.NONE;
      }

      if (modelPlugin == CustomModel.ModelPlugin.FREE_MINECRAFT_MODELS) {
         try {
            Bukkit.getPluginManager().registerEvents(new CustomModelFMM.FmmReloadListener(), MetadataHandler.PLUGIN);
            Bukkit.getPluginManager().registerEvents(new CustomModelFMM.FmmProjectileDamageBridge(), MetadataHandler.PLUGIN);
            Bukkit.getPluginManager().registerEvents(new CustomModelFMM.FmmRemoveBridge(), MetadataHandler.PLUGIN);
         } catch (Throwable var1) {
         }
      }
   }

   public static void reloadModels() {
      switch (modelPlugin) {
         case FREE_MINECRAFT_MODELS:
            CustomModelFMM.reloadModels();
            break;
         case MODEL_ENGINE:
            CustomModelMEG.reloadModels();
      }
   }

   public static boolean modelExists(String modelName) {
      switch (modelPlugin) {
         case FREE_MINECRAFT_MODELS:
            return CustomModelFMM.modelExists(modelName);
         case MODEL_ENGINE:
            return CustomModelMEG.modelExists(modelName);
         default:
            return false;
      }
   }

   public static CustomModel generateCustomModel(LivingEntity livingEntity, String modelName, String nametagName) {
      CustomModel customModel = new CustomModel(livingEntity, modelName, nametagName);
      return customModel.initialized ? customModel : null;
   }

   public static CustomModel generateCustomModel(
      LivingEntity livingEntity,
      String modelName,
      String nametagName,
      ModeledEntityLeftClickCallback leftClickCallback,
      ModeledEntityRightClickCallback rightClickCallback
   ) {
      CustomModel customModel = new CustomModel(livingEntity, modelName, nametagName, leftClickCallback, rightClickCallback);
      return customModel.initialized ? customModel : null;
   }

   public static boolean customModelsEnabled() {
      return modelPlugin != CustomModel.ModelPlugin.NONE;
   }

   public static void runProjectileDamageBypass(Runnable damageCall) {
      if (modelPlugin == CustomModel.ModelPlugin.FREE_MINECRAFT_MODELS) {
         CustomModelFMM.runProjectileDamageBypass(damageCall);
      } else {
         damageCall.run();
      }
   }

   @Override
   public void shoot() {
      switch (modelPlugin) {
         case FREE_MINECRAFT_MODELS:
            this.customModelFMM.shoot();
            break;
         case MODEL_ENGINE:
            this.customModelMEG.shoot();
      }
   }

   @Override
   public void melee() {
      switch (modelPlugin) {
         case FREE_MINECRAFT_MODELS:
            this.customModelFMM.melee();
            break;
         case MODEL_ENGINE:
            this.customModelMEG.melee();
      }
   }

   @Override
   public void playAnimationByName(String animationName) {
      switch (modelPlugin) {
         case FREE_MINECRAFT_MODELS:
            this.customModelFMM.playAnimationByName(animationName);
            break;
         case MODEL_ENGINE:
            this.customModelMEG.playAnimationByName(animationName);
      }
   }

   @Override
   public void setName(String nametagName, boolean visible) {
      switch (modelPlugin) {
         case FREE_MINECRAFT_MODELS:
            this.customModelFMM.setName(nametagName, visible);
            break;
         case MODEL_ENGINE:
            this.customModelMEG.setName(nametagName, visible);
      }
   }

   public boolean setNpcNameLines(List<String> lines, float scale, double gap) {
      return modelPlugin == CustomModel.ModelPlugin.FREE_MINECRAFT_MODELS && this.customModelFMM != null && this.customModelFMM.setNameLines(lines, scale, gap);
   }

   @Override
   public void setNameVisible(boolean visible) {
      switch (modelPlugin) {
         case FREE_MINECRAFT_MODELS:
            this.customModelFMM.setNameVisible(visible);
            break;
         case MODEL_ENGINE:
            this.customModelMEG.setNameVisible(visible);
      }
   }

   @Override
   public void addPassenger(CustomBossEntity passenger) {
      switch (modelPlugin) {
         case FREE_MINECRAFT_MODELS:
            this.customModelFMM.addPassenger(passenger);
            break;
         case MODEL_ENGINE:
            this.customModelMEG.addPassenger(passenger);
      }
   }

   @Override
   public void switchPhase() {
      switch (modelPlugin) {
         case FREE_MINECRAFT_MODELS:
            this.customModelFMM.switchPhase();
            break;
         case MODEL_ENGINE:
            this.customModelMEG.switchPhase();
      }
   }

   @Override
   public Location getNametagBoneLocation() {
      return switch (modelPlugin) {
         case FREE_MINECRAFT_MODELS -> this.customModelFMM.getNametagBoneLocation();
         case MODEL_ENGINE -> this.customModelMEG.getNametagBoneLocation();
         default -> null;
      };
   }

   @Override
   public boolean hasNametagBone() {
      return switch (modelPlugin) {
         case FREE_MINECRAFT_MODELS -> this.customModelFMM.hasNametagBone();
         case MODEL_ENGINE -> this.customModelMEG.hasNametagBone();
         default -> true;
      };
   }

   @Override
   public void setSyncMovement(boolean syncMovement) {
      switch (modelPlugin) {
         case FREE_MINECRAFT_MODELS:
            this.customModelFMM.setSyncMovement(syncMovement);
            break;
         case MODEL_ENGINE:
            this.customModelMEG.setSyncMovement(syncMovement);
      }
   }

   public boolean setLookTarget(Location target) {
      return modelPlugin == CustomModel.ModelPlugin.FREE_MINECRAFT_MODELS && this.customModelFMM != null && this.customModelFMM.setLookTarget(target);
   }

   @Generated
   public static boolean isUsingModels() {
      return false;
   }

   @Generated
   public static CustomModel.ModelPlugin getModelPlugin() {
      return modelPlugin;
   }

   public static class ModelEntityEvents implements Listener {
      @EventHandler(
         ignoreCancelled = true
      )
      public void onMeleeHit(EntityDamageByEntityEvent event) {
         EliteEntity eliteEntity = EntityTracker.getEliteMobEntity(event.getDamager());
         if (eliteEntity instanceof CustomBossEntity) {
            if (((CustomBossEntity)eliteEntity).getCustomModel() != null) {
               ((CustomBossEntity)eliteEntity).getCustomModel().melee();
            }
         }
      }

      @EventHandler(
         ignoreCancelled = true
      )
      public void onRangedShot(EntitySpawnEvent event) {
         if (event.getEntity() instanceof Projectile) {
            if (((Projectile)event.getEntity()).getShooter() instanceof LivingEntity) {
               EliteEntity eliteEntity = EntityTracker.getEliteMobEntity((LivingEntity)((Projectile)event.getEntity()).getShooter());
               if (eliteEntity instanceof CustomBossEntity) {
                  if (((CustomBossEntity)eliteEntity).getCustomModel() != null) {
                     ((CustomBossEntity)eliteEntity).getCustomModel().shoot();
                  }
               }
            }
         }
      }

      @EventHandler
      public void onDeathEvent(EliteMobDeathEvent event) {
         if (event.getEliteEntity() instanceof CustomBossEntity customBossEntity && customBossEntity.getCustomModel() != null) {
            customBossEntity.getCustomModel().playAnimationByName("death");
         }
      }
   }

   public static enum ModelPlugin {
      NONE,
      FREE_MINECRAFT_MODELS,
      MODEL_ENGINE;
   }
}
