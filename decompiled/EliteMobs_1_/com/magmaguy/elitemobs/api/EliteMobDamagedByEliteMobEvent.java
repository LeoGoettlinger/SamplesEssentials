package com.magmaguy.elitemobs.api;

import com.magmaguy.elitemobs.entitytracker.EntityTracker;
import com.magmaguy.elitemobs.mobconstructor.EliteEntity;
import com.magmaguy.elitemobs.mobconstructor.custombosses.RegionalBossEntity;
import com.magmaguy.elitemobs.mobconstructor.mobdata.aggressivemobs.EliteMobProperties;
import com.magmaguy.elitemobs.utils.EntityFinder;
import com.magmaguy.elitemobs.utils.EventCaller;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public class EliteMobDamagedByEliteMobEvent extends EliteDamageEvent {
   private static final HandlerList handlers = new HandlerList();
   private final EliteEntity damager;
   private final EliteEntity damagee;
   private final EntityDamageByEntityEvent entityDamageByEntityEvent;

   public EliteMobDamagedByEliteMobEvent(EliteEntity damager, EliteEntity damagee, EntityDamageByEntityEvent event, double damage) {
      super(damage, event);
      this.damager = damager;
      this.damagee = damagee;
      this.entityDamageByEntityEvent = event;
   }

   public static HandlerList getHandlerList() {
      return handlers;
   }

   public EliteEntity getDamager() {
      return this.damager;
   }

   public EliteEntity getDamagee() {
      return this.damagee;
   }

   public EntityDamageByEntityEvent getEntityDamageByEntityEvent() {
      return this.entityDamageByEntityEvent;
   }

   @Override
   public HandlerList getHandlers() {
      return handlers;
   }

   public static class EliteMobDamagedByEliteMobFilter implements Listener {
      @EventHandler(
         priority = EventPriority.HIGH,
         ignoreCancelled = true
      )
      public void onEntityDamageByEntityEvent(EntityDamageByEntityEvent event) {
         EliteEntity damagee = EntityTracker.getEliteMobEntity(event.getEntity());
         if (damagee != null) {
            LivingEntity livingEntity = EntityFinder.filterRangedDamagers(event.getDamager());
            EliteEntity damager = EntityTracker.getEliteMobEntity(livingEntity);
            if (damager != null) {
               if (damager.getLivingEntity() != null) {
                  if (EliteMobProperties.getPluginData(damager.getLivingEntity().getType()) != null) {
                     double damage = EliteMobProperties.getBaselineDamage(damager.getLivingEntity().getType(), damager) + damager.getLevel();
                     EliteMobDamagedByEliteMobEvent eliteMobDamagedByEliteMobEvent = new EliteMobDamagedByEliteMobEvent(damager, damagee, event, damage);
                     new EventCaller(eliteMobDamagedByEliteMobEvent);
                     if (!eliteMobDamagedByEliteMobEvent.isCancelled()) {
                        event.setDamage(eliteMobDamagedByEliteMobEvent.getDamage());
                        if (damagee instanceof RegionalBossEntity regionalBossEntity) {
                           regionalBossEntity.removeSlow();
                        }
                     }
                  }
               }
            }
         }
      }
   }
}
