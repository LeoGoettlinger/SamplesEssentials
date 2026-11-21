package com.magmaguy.elitemobs.api;

import com.magmaguy.elitemobs.npcs.NPCEntity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class NPCEntitySpawnEvent extends Event implements Cancellable {
   private static final HandlerList handlers = new HandlerList();
   private final LivingEntity livingEntity;
   private final NPCEntity npcEntity;
   private boolean isCancelled = false;

   public NPCEntitySpawnEvent(LivingEntity livingEntity, NPCEntity npcEntity) {
      this.livingEntity = livingEntity;
      this.npcEntity = npcEntity;
   }

   public static HandlerList getHandlerList() {
      return handlers;
   }

   public LivingEntity getLivingEntity() {
      return this.livingEntity;
   }

   public NPCEntity getNPCEntity() {
      return this.npcEntity;
   }

   public boolean isCancelled() {
      return this.isCancelled;
   }

   public void setCancelled(boolean cancel) {
      this.isCancelled = cancel;
      if (this.isCancelled) {
         this.livingEntity.remove();
      }
   }

   public HandlerList getHandlers() {
      return handlers;
   }
}
