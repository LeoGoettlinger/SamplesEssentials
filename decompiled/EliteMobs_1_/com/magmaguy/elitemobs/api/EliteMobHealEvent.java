package com.magmaguy.elitemobs.api;

import com.magmaguy.elitemobs.mobconstructor.EliteEntity;
import lombok.Generated;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class EliteMobHealEvent extends Event implements Cancellable {
   private static final HandlerList handlers = new HandlerList();
   private final EliteEntity eliteEntity;
   private double healAmount = 0.0;
   private boolean isCancelled = false;
   private boolean fullHeal = false;

   public EliteMobHealEvent(EliteEntity eliteEntity, double healAmount) {
      this.eliteEntity = eliteEntity;
      this.healAmount = healAmount;
   }

   public EliteMobHealEvent(EliteEntity eliteEntity, boolean fullHeal) {
      this.eliteEntity = eliteEntity;
      this.fullHeal = fullHeal;
   }

   public static HandlerList getHandlerList() {
      return handlers;
   }

   public HandlerList getHandlers() {
      return handlers;
   }

   public boolean isCancelled() {
      return this.isCancelled;
   }

   public void setCancelled(boolean b) {
      this.isCancelled = b;
   }

   @Generated
   public EliteEntity getEliteEntity() {
      return this.eliteEntity;
   }

   @Generated
   public double getHealAmount() {
      return this.healAmount;
   }

   @Generated
   public boolean isFullHeal() {
      return this.fullHeal;
   }
}
