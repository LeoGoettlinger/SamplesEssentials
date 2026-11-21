package com.magmaguy.elitemobs.api;

import com.magmaguy.elitemobs.mobconstructor.EliteEntity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;

public class EliteMobSpawnEvent extends Event implements Cancellable {
   private static final HandlerList handlers = new HandlerList();
   private final LivingEntity livingEntity;
   private final EliteEntity eliteEntity;
   private final SpawnReason spawnReason;
   private boolean isCancelled = false;

   public EliteMobSpawnEvent(EliteEntity eliteEntity) {
      this.livingEntity = eliteEntity.getLivingEntity();
      this.eliteEntity = eliteEntity;
      this.spawnReason = eliteEntity.getSpawnReason();
   }

   public static HandlerList getHandlerList() {
      return handlers;
   }

   public LivingEntity getEntity() {
      return this.livingEntity;
   }

   public EliteEntity getEliteMobEntity() {
      return this.eliteEntity;
   }

   public SpawnReason getReason() {
      return this.spawnReason;
   }

   public boolean isCancelled() {
      return this.isCancelled;
   }

   public void setCancelled(boolean cancel) {
      this.isCancelled = cancel;
   }

   public HandlerList getHandlers() {
      return handlers;
   }
}
