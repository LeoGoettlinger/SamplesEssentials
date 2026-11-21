package com.magmaguy.elitemobs.thirdparty.custommodels.freeminecraftmodels;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.advancedcombat.abilities.ClassAbilityProjectileCarrier;
import com.magmaguy.elitemobs.api.EliteMobDamagedByPlayerEventFilter;
import com.magmaguy.elitemobs.commands.admin.RemoveCommand;
import com.magmaguy.elitemobs.entitytracker.EntityTracker;
import com.magmaguy.elitemobs.magmacore.util.Logger;
import com.magmaguy.elitemobs.mobconstructor.EliteEntity;
import com.magmaguy.elitemobs.mobconstructor.custombosses.CustomBossEntity;
import com.magmaguy.elitemobs.npcs.NPCEntity;
import com.magmaguy.elitemobs.thirdparty.custommodels.CustomModelInterface;
import com.magmaguy.freeminecraftmodels.api.FmmReloadedEvent;
import com.magmaguy.freeminecraftmodels.api.ModeledEntityHitByProjectileEvent;
import com.magmaguy.freeminecraftmodels.api.ModeledEntityLeftClickEvent;
import com.magmaguy.freeminecraftmodels.api.ModeledEntityManager;
import com.magmaguy.freeminecraftmodels.api.ModeledEntityRightClickEvent;
import com.magmaguy.freeminecraftmodels.customentity.DynamicEntity;
import com.magmaguy.freeminecraftmodels.customentity.ModeledEntity;
import com.magmaguy.freeminecraftmodels.customentity.ModeledEntityLeftClickCallback;
import com.magmaguy.freeminecraftmodels.customentity.ModeledEntityRightClickCallback;
import com.magmaguy.freeminecraftmodels.customentity.core.Bone;
import com.magmaguy.freeminecraftmodels.customentity.core.OBBHitDetection;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import lombok.Generated;
import org.bukkit.Location;
import org.bukkit.entity.AbstractArrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;

public class CustomModelFMM implements CustomModelInterface {
   private final DynamicEntity dynamicEntity;

   public CustomModelFMM(LivingEntity livingEntity, String modelName, String nametagName) {
      this.dynamicEntity = DynamicEntity.create(modelName, livingEntity);
      if (this.dynamicEntity != null) {
         this.dynamicEntity.setDisplayName(nametagName);
      }
   }

   public boolean setNameLines(List<String> lines, float scale, double gap) {
      if (this.dynamicEntity == null) {
         return false;
      } else {
         this.dynamicEntity.setDisplayNameLines(lines);
         this.dynamicEntity.setDisplayNameScale(scale);
         this.dynamicEntity.setDisplayNameLineGap(gap);
         this.dynamicEntity.setDisplayNameVisible(true);
         return true;
      }
   }

   public boolean setLookTarget(Location target) {
      if (this.dynamicEntity == null) {
         return false;
      } else {
         this.dynamicEntity.setLookTarget(target);
         return true;
      }
   }

   public CustomModelFMM(
      LivingEntity livingEntity,
      String modelName,
      String nametagName,
      ModeledEntityLeftClickCallback leftClickCallback,
      ModeledEntityRightClickCallback rightClickCallback
   ) {
      this.dynamicEntity = DynamicEntity.create(modelName, livingEntity);
      if (this.dynamicEntity != null) {
         this.dynamicEntity.setDamagesOnContact(false);
         this.dynamicEntity.setHitboxContactCallback(null);
         this.dynamicEntity.setDisplayName(nametagName);
         if (leftClickCallback != null) {
            this.dynamicEntity.setLeftClickCallback(leftClickCallback);
         }

         if (rightClickCallback != null) {
            this.dynamicEntity.setRightClickCallback(rightClickCallback);
         }
      }
   }

   public static void reloadModels() {
      ModeledEntityManager.reload();
   }

   public static boolean modelExists(String modelName) {
      return ModeledEntityManager.modelExists(modelName);
   }

   public static void runProjectileDamageBypass(Runnable damageCall) {
      boolean previousApplyDamage = OBBHitDetection.applyDamage;
      boolean previousBypassProjectileRedirect = OBBHitDetection.bypassProjectileRedirect;
      OBBHitDetection.applyDamage = true;
      OBBHitDetection.bypassProjectileRedirect = true;

      try {
         damageCall.run();
      } finally {
         OBBHitDetection.applyDamage = previousApplyDamage;
         OBBHitDetection.bypassProjectileRedirect = previousBypassProjectileRedirect;
      }
   }

   @Override
   public void shoot() {
      if (this.dynamicEntity != null) {
         if (this.dynamicEntity.hasAnimation("attack_ranged")) {
            this.dynamicEntity.playAnimation("attack_ranged", false, false);
         } else if (this.dynamicEntity.hasAnimation("attack")) {
            this.dynamicEntity.playAnimation("attack", false, false);
         }
      }
   }

   @Override
   public void melee() {
      if (this.dynamicEntity != null) {
         if (this.dynamicEntity.hasAnimation("attack_melee")) {
            this.dynamicEntity.playAnimation("attack_melee", false, false);
         } else if (this.dynamicEntity.hasAnimation("attack")) {
            this.dynamicEntity.playAnimation("attack", false, false);
         }
      }
   }

   @Override
   public void playAnimationByName(String animationName) {
      if (this.dynamicEntity != null) {
         if (this.dynamicEntity.hasAnimation(animationName)) {
            this.dynamicEntity.playAnimation(animationName, false, false);
         }
      }
   }

   @Override
   public void setName(String nametagName, boolean visible) {
      if (this.dynamicEntity != null) {
         this.dynamicEntity.setDisplayName(nametagName);
         this.dynamicEntity.setDisplayNameVisible(visible);
      }
   }

   @Override
   public void setNameVisible(boolean visible) {
      if (this.dynamicEntity != null) {
         this.dynamicEntity.setDisplayNameVisible(visible);
      }
   }

   @Override
   public void addPassenger(CustomBossEntity passenger) {
   }

   @Override
   public void switchPhase() {
      if (this.dynamicEntity != null) {
         this.dynamicEntity.remove();
      }
   }

   @Override
   public Location getNametagBoneLocation() {
      if (this.dynamicEntity == null) {
         return null;
      } else {
         List<Bone> nametagBones = this.dynamicEntity.getNametagBones();
         return nametagBones != null && !nametagBones.isEmpty() ? nametagBones.get(0).getBoneLocation() : null;
      }
   }

   @Override
   public boolean hasNametagBone() {
      return this.dynamicEntity != null;
   }

   @Override
   public void setSyncMovement(boolean syncMovement) {
      if (this.dynamicEntity != null) {
         this.dynamicEntity.setSyncMovement(syncMovement);
      }
   }

   @Generated
   public DynamicEntity getDynamicEntity() {
      return this.dynamicEntity;
   }

   public static class FmmProjectileDamageBridge implements Listener {
      private static final Set<UUID> BRIDGED_PROJECTILES = ConcurrentHashMap.newKeySet();

      @EventHandler(
         priority = EventPriority.LOWEST,
         ignoreCancelled = true
      )
      public void onFmmProjectileHit(ModeledEntityHitByProjectileEvent event) {
         Projectile projectile = event.getProjectile();
         if (ClassAbilityProjectileCarrier.isCarrier(projectile)) {
            event.setCancelled(true);
            Entity underlying = event.getEntity().getUnderlyingEntity();
            ClassAbilityProjectileCarrier.tryModeledImpact(projectile, underlying);
            event.getEntity().getSkeleton().tint();
         } else {
            Entity underlying = event.getEntity().getUnderlyingEntity();
            if (underlying instanceof LivingEntity) {
               EliteEntity eliteEntity = EntityTracker.getEliteMobEntity(underlying);
               if (eliteEntity != null && eliteEntity.isValid()) {
                  if (projectile.getShooter() instanceof Player player) {
                     event.setCancelled(true);
                     final UUID var12 = projectile.getUniqueId();
                     if (BRIDGED_PROJECTILES.add(var12)) {
                        (new BukkitRunnable() {
                           public void run() {
                              CustomModelFMM.FmmProjectileDamageBridge.BRIDGED_PROJECTILES.remove(var12);
                           }
                        }).runTaskLater(MetadataHandler.PLUGIN, 20L);
                        double damage = 1.0;
                        if (projectile instanceof AbstractArrow arrow) {
                           damage = Math.max(1.0, Math.ceil(arrow.getDamage() * projectile.getVelocity().length()));
                        }

                        double modeledDamage = damage;
                        CustomModelFMM.runProjectileDamageBypass(
                           () -> EliteMobDamagedByPlayerEventFilter.applyModeledProjectileHit(player, eliteEntity, projectile, modeledDamage)
                        );
                        event.getEntity().getSkeleton().tint();
                     }
                  }
               }
            }
         }
      }
   }

   public static class FmmReloadListener implements Listener {
      @EventHandler(
         priority = EventPriority.MONITOR
      )
      public void onFmmReloaded(FmmReloadedEvent event) {
         for (NPCEntity npc : new ArrayList<>(EntityTracker.getNpcEntities().values())) {
            try {
               npc.refreshCustomModel();
            } catch (Throwable var5) {
               Logger.warn("Failed to refresh FMM model after reload for NPC " + npc.getUuid() + ": " + var5.getMessage());
            }
         }
      }
   }

   public static class FmmRemoveBridge implements Listener {
      @EventHandler(
         priority = EventPriority.LOWEST,
         ignoreCancelled = false
      )
      public void onFmmLeftClick(ModeledEntityLeftClickEvent event) {
         this.removeModeledTarget(event.getPlayer(), event.getEntity(), event);
      }

      @EventHandler(
         priority = EventPriority.LOWEST,
         ignoreCancelled = false
      )
      public void onFmmRightClick(ModeledEntityRightClickEvent event) {
         this.removeModeledTarget(event.getPlayer(), event.getEntity(), event);
      }

      private void removeModeledTarget(Player player, ModeledEntity modeledEntity, Cancellable event) {
         if (RemoveCommand.isRemoving(player)) {
            Entity underlyingEntity = modeledEntity.getUnderlyingEntity();
            if (underlyingEntity != null) {
               if (RemoveCommand.removeTrackedEntity(player, underlyingEntity)) {
                  event.setCancelled(true);
               }
            }
         }
      }
   }
}
