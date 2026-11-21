package com.magmaguy.elitemobs.api;

import com.magmaguy.elitemobs.mobconstructor.EliteEntity;
import lombok.Generated;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class ScriptZoneLeaveEvent extends Event {
   private static final HandlerList handlers = new HandlerList();
   private final EliteEntity eliteEntity;
   private final LivingEntity entity;

   public ScriptZoneLeaveEvent(EliteEntity customBossEntity, LivingEntity entity) {
      this.eliteEntity = customBossEntity;
      this.entity = entity;
   }

   public static HandlerList getHandlerList() {
      return handlers;
   }

   public HandlerList getHandlers() {
      return handlers;
   }

   @Generated
   public EliteEntity getEliteEntity() {
      return this.eliteEntity;
   }

   @Generated
   public LivingEntity getEntity() {
      return this.entity;
   }
}
