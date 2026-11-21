package com.magmaguy.elitemobs.api;

import com.magmaguy.elitemobs.mobconstructor.custombosses.CustomBossEntity;
import com.magmaguy.elitemobs.mobconstructor.custombosses.PhaseBossEntity;
import lombok.Generated;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class ElitePhaseSwitchEvent extends Event {
   private static final HandlerList handlers = new HandlerList();
   private final CustomBossEntity customBossEntity;
   private final PhaseBossEntity phaseBossEntity;

   public ElitePhaseSwitchEvent(CustomBossEntity customBossEntity, PhaseBossEntity phaseBossEntity) {
      this.customBossEntity = customBossEntity;
      this.phaseBossEntity = phaseBossEntity;
   }

   public static HandlerList getHandlerList() {
      return handlers;
   }

   public HandlerList getHandlers() {
      return handlers;
   }

   @Generated
   public CustomBossEntity getCustomBossEntity() {
      return this.customBossEntity;
   }

   @Generated
   public PhaseBossEntity getPhaseBossEntity() {
      return this.phaseBossEntity;
   }
}
