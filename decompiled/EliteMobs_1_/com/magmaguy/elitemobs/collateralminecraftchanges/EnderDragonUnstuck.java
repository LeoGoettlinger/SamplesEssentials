package com.magmaguy.elitemobs.collateralminecraftchanges;

import com.magmaguy.elitemobs.api.EliteMobEnterCombatEvent;
import com.magmaguy.elitemobs.easyminecraftgoals.NMSManager;
import com.magmaguy.elitemobs.entitytracker.EntityTracker;
import com.magmaguy.elitemobs.mobconstructor.EliteEntity;
import com.magmaguy.elitemobs.mobconstructor.custombosses.CustomBossEntity;
import org.bukkit.entity.EnderDragon;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Wolf;
import org.bukkit.entity.EnderDragon.Phase;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityTargetLivingEntityEvent;

public class EnderDragonUnstuck implements Listener {
   @EventHandler(
      ignoreCancelled = true,
      priority = EventPriority.HIGHEST
   )
   public void onDragonEnterCombat(EliteMobEnterCombatEvent event) {
      if (event.getEliteMobEntity() != null) {
         if (event.getEliteMobEntity().getLivingEntity() != null) {
            if (event.getEliteMobEntity().getLivingEntity().getType().equals(EntityType.ENDER_DRAGON)) {
               if (!NMSManager.isEnabled() || !NMSManager.getAdapter().isMindBody(event.getEliteMobEntity().getLivingEntity())) {
                  event.getEliteMobEntity().setAIEnabled(true);
                  ((EnderDragon)event.getEliteMobEntity().getLivingEntity()).setPhase(Phase.LEAVE_PORTAL);
               }
            }
         }
      }
   }

   public static class AggroPrevention implements Listener {
      @EventHandler(
         ignoreCancelled = true
      )
      public void onTarget(EntityTargetLivingEntityEvent event) {
         EliteEntity eliteEntity = EntityTracker.getEliteMobEntity(event.getEntity());
         if (eliteEntity != null) {
            if (!(eliteEntity instanceof CustomBossEntity customBossEntity && customBossEntity.getCustomBossesConfigFields().isNeutral())) {
               if (!(
                  EntityTracker.getEliteMobEntity(event.getTarget()) instanceof CustomBossEntity customBossEntityx
                     && customBossEntityx.getCustomBossesConfigFields().isNeutral()
               )) {
                  if ((!event.getEntity().getType().equals(EntityType.WOLF) || ((Wolf)event.getEntity()).isAngry())
                     && (event.getTarget() == null || !event.getTarget().getType().equals(EntityType.WOLF) || ((Wolf)event.getTarget()).isAngry())) {
                     if (EntityTracker.isEliteMob(event.getEntity()) && event.getTarget() != null && EntityTracker.isEliteMob(event.getTarget())
                        || EntityTracker.isEliteMob(event.getEntity()) && event.getTarget() != null && EntityTracker.isNPCEntity(event.getTarget())) {
                        event.setCancelled(true);
                     }
                  }
               }
            }
         }
      }
   }
}
