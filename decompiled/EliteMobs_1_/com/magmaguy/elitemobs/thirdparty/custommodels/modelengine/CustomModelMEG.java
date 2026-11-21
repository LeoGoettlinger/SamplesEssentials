package com.magmaguy.elitemobs.thirdparty.custommodels.modelengine;

import com.magmaguy.elitemobs.magmacore.util.Logger;
import com.magmaguy.elitemobs.mobconstructor.custombosses.CustomBossEntity;
import com.magmaguy.elitemobs.thirdparty.custommodels.CustomModelInterface;
import com.ticxo.modelengine.api.ModelEngineAPI;
import com.ticxo.modelengine.api.animation.state.ModelState;
import com.ticxo.modelengine.api.generator.model.ModelBlueprint;
import com.ticxo.modelengine.api.model.ActiveModel;
import com.ticxo.modelengine.api.model.ModeledEntity;
import com.ticxo.modelengine.api.model.bone.Nameable;
import java.util.Iterator;
import lombok.Generated;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;

public class CustomModelMEG implements CustomModelInterface {
   ActiveModel activeModel;
   ModeledEntity modeledEntity;
   ModelBlueprint modelBlueprint;
   private boolean success = false;

   public CustomModelMEG(LivingEntity livingEntity, String modelName, String nametagName) {
      try {
         if (ModelEngineAPI.api.getModelRegistry().getBlueprint(modelName) == null) {
            Logger.info("Model " + modelName + " was not found! Make sure you install the model correctly if you have it. This entry will be skipped!");
            return;
         }
      } catch (NoSuchMethodError var6) {
         Logger.warn("Model Engine API version is not supported. Currently Elitemobs can only support ModelEngine R3.0.0.");
         return;
      }

      this.modelBlueprint = ModelEngineAPI.api.getModelRegistry().getBlueprint(modelName);
      this.activeModel = ModelEngineAPI.createActiveModel(this.modelBlueprint);
      if (this.activeModel == null) {
         Logger.warn("Failed to load model from " + modelName + " ! Is the model name correct, and has the model been installed correctly?");
      } else {
         this.modeledEntity = ModelEngineAPI.createModeledEntity(livingEntity);
         if (this.modeledEntity == null) {
            Logger.warn("Failed to create model entity " + modelName + " ! This means the entity that was meant to get disguised has a problem!");
         } else {
            try {
               this.modeledEntity.addModel(this.activeModel, true);
               this.activeModel.playDefaultAnimation(ModelState.IDLE);
               this.modeledEntity.setBaseEntityVisible(false);
               this.setName(nametagName, true);
               this.success = true;
            } catch (Exception var5) {
               this.modeledEntity.removeModel(modelName);
               Logger.warn("Failed to make model entity " + modelName + " ! Couldn't assign model or visibility status.");
               var5.printStackTrace();
            }
         }
      }
   }

   public static CustomModelMEG generateCustomModel(LivingEntity livingEntity, String modelName, String nametagName) {
      CustomModelMEG customModel = new CustomModelMEG(livingEntity, modelName, nametagName);
      return customModel.isSuccess() ? customModel : null;
   }

   public static void reloadModels() {
      try {
         ModelEngineAPI.api.getGenerator().importModelsAsync();
      } catch (Exception var1) {
         Logger.warn("Model Engine API version is not supported. Currently Elitemobs can only support ModelEngine R3.0.0.");
      }
   }

   public static boolean modelExists(String modelName) {
      if (modelName != null && !modelName.isEmpty()) {
         try {
            if (ModelEngineAPI.api.getModelRegistry().getBlueprint(modelName) == null) {
               Logger.info("Model " + modelName + " was not found! Make sure you install the model correctly if you have it. This entry will be skipped!");
               return false;
            } else {
               return true;
            }
         } catch (NoSuchMethodError var2) {
            Logger.warn(
               "Model Engine API version is not supported. Currently Elitemobs can only support ModelEngine R3.0.0, documentation for other versions doesn't exist."
            );
            return false;
         }
      } else {
         return false;
      }
   }

   @Override
   public void shoot() {
      if (this.activeModel != null) {
         if (this.modelBlueprint.getAnimations().containsKey("attack_ranged")) {
            this.activeModel.getAnimationHandler().playAnimation("attack_ranged", 0.1, 0.1, 1.0, true);
         } else {
            this.activeModel.getAnimationHandler().playAnimation("attack", 0.1, 0.1, 1.0, true);
         }
      }
   }

   @Override
   public void melee() {
      if (this.activeModel != null) {
         if (this.modelBlueprint.getAnimations().containsKey("attack_melee")) {
            this.activeModel.getAnimationHandler().playAnimation("attack_melee", 0.1, 0.1, 1.0, true);
         } else {
            this.activeModel.getAnimationHandler().playAnimation("attack", 0.1, 0.1, 1.0, true);
         }
      }
   }

   @Override
   public void playAnimationByName(String string) {
      if (this.activeModel != null) {
         if (this.modelBlueprint.getAnimations().containsKey(string)) {
            this.activeModel.getAnimationHandler().playAnimation(string, 0.1, 0.1, 1.0, true);
         }
      }
   }

   @Override
   public void setName(String nametagName, boolean visible) {
      if (this.modeledEntity != null) {
         Nameable nametag = this.getNameableBone();
         if (nametag == null) {
            Logger.warn("Failed to get hitbox nametag for disguise!");
         } else {
            nametag.setCustomName(nametagName);
            nametag.setCustomNameVisible(visible);
         }
      }
   }

   @Override
   public void setNameVisible(boolean visible) {
      Nameable nametag = this.getNameableBone();
      if (nametag != null) {
         nametag.setCustomNameVisible(visible);
      }
   }

   private Nameable getNameableBone() {
      Iterator var1 = this.activeModel.getNametagHandler().getBones().values().iterator();
      return var1.hasNext() ? (Nameable)var1.next() : null;
   }

   @Override
   public void addPassenger(CustomBossEntity passenger) {
      if (passenger.getCustomBossesConfigFields().getCustomModelMountPointID() == null) {
         Logger.warn(
            "Attempted to add "
               + passenger.getCustomBossesConfigFields().getFilename()
               + " as a mounted entity for a custom model but it does not have customModelMountPointID set! The boss can't guess where it needs to be mounted, and therefore this will not work."
         );
      } else {
         this.modeledEntity
            .getMountManager()
            .addPassengerToSeat(
               this.modelBlueprint.getModelId(),
               passenger.getCustomBossesConfigFields().getCustomModelMountPointID(),
               passenger.getLivingEntity(),
               this.modeledEntity.getMountManager().getDriverController()
            );
      }
   }

   @Override
   public void switchPhase() {
      this.activeModel.getAnimationHandler().forceStopAllAnimations();
   }

   @Override
   public Location getNametagBoneLocation() {
      return null;
   }

   @Override
   public boolean hasNametagBone() {
      return true;
   }

   @Override
   public void setSyncMovement(boolean syncMovement) {
   }

   @Generated
   public boolean isSuccess() {
      return this.success;
   }
}
