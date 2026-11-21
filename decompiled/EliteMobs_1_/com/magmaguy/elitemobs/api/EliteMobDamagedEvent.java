package com.magmaguy.elitemobs.api;

import com.magmaguy.elitemobs.EliteMobs;
import com.magmaguy.elitemobs.dungeons.EliteMobsWorld;
import com.magmaguy.elitemobs.entitytracker.EntityTracker;
import com.magmaguy.elitemobs.mobconstructor.EliteEntity;
import com.magmaguy.elitemobs.thirdparty.worldguard.WorldGuardCompatibility;
import com.magmaguy.elitemobs.thirdparty.worldguard.WorldGuardFlagChecker;
import com.magmaguy.elitemobs.utils.EventCaller;
import lombok.Generated;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;

public class EliteMobDamagedEvent extends EliteDamageEvent {
   private static final HandlerList handlers = new HandlerList();
   private final Entity entity;
   private final EliteEntity eliteEntity;
   private final EntityDamageEvent entityDamageEvent;

   public EliteMobDamagedEvent(EliteEntity eliteEntity, EntityDamageEvent event, double damage) {
      super(damage, event);
      this.entity = eliteEntity.getLivingEntity();
      this.eliteEntity = eliteEntity;
      this.entityDamageEvent = event;
   }

   public static HandlerList getHandlerList() {
      return handlers;
   }

   @Override
   public HandlerList getHandlers() {
      return handlers;
   }

   @Generated
   public Entity getEntity() {
      return this.entity;
   }

   @Generated
   public EliteEntity getEliteEntity() {
      return this.eliteEntity;
   }

   @Generated
   public EntityDamageEvent getEntityDamageEvent() {
      return this.entityDamageEvent;
   }

   public static class EliteMobDamageEventFilter implements Listener {
      @EventHandler(
         priority = EventPriority.HIGH,
         ignoreCancelled = true
      )
      public void onEntityDamagedEvent(EntityDamageEvent event) {
         EliteEntity eliteEntity = EntityTracker.getEliteMobEntity(event.getEntity());
         if (eliteEntity != null) {
            EliteMobDamagedEvent eliteMobDamagedEvent = new EliteMobDamagedEvent(eliteEntity, event, event.getDamage());
            new EventCaller(eliteMobDamagedEvent);
            if (eliteMobDamagedEvent.isCancelled()) {
               eliteMobDamagedEvent.setCancelled(true);
            } else if (eliteEntity.isValid()) {
               if (!eliteEntity.isEnderDragon()) {
                  if (!EliteMobsWorld.isEliteMobsWorld(event.getEntity().getWorld().getUID())) {
                     if (!EliteMobs.worldGuardIsEnabled
                        || WorldGuardFlagChecker.checkFlag(eliteEntity.getLivingEntity().getLocation(), WorldGuardCompatibility.getELITEMOBS_ANTIEXPLOIT())) {
                        Bukkit.getServer().getPluginManager().callEvent(new GenericAntiExploitEvent(eliteEntity, event));
                     }
                  }
               }
            }
         }
      }
   }
}
