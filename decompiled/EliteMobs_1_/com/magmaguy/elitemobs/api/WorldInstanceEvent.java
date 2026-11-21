package com.magmaguy.elitemobs.api;

import com.magmaguy.elitemobs.config.contentpackages.ContentPackagesConfigFields;
import lombok.Generated;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class WorldInstanceEvent extends Event implements Cancellable {
   private static final HandlerList handlers = new HandlerList();
   private final ContentPackagesConfigFields contentPackagesConfigFields;
   private final String blueprintWorldName;
   private final String instancedWorldName;
   private boolean cancelled = false;

   public WorldInstanceEvent(String blueprintWorldName, String instancedWorldName, ContentPackagesConfigFields contentPackagesConfigFields) {
      this.blueprintWorldName = blueprintWorldName;
      this.instancedWorldName = instancedWorldName;
      this.contentPackagesConfigFields = contentPackagesConfigFields;
   }

   public static HandlerList getHandlerList() {
      return handlers;
   }

   public HandlerList getHandlers() {
      return handlers;
   }

   public boolean isCancelled() {
      return this.cancelled;
   }

   public void setCancelled(boolean cancelled) {
      this.cancelled = cancelled;
   }

   @Generated
   public ContentPackagesConfigFields getContentPackagesConfigFields() {
      return this.contentPackagesConfigFields;
   }

   @Generated
   public String getBlueprintWorldName() {
      return this.blueprintWorldName;
   }

   @Generated
   public String getInstancedWorldName() {
      return this.instancedWorldName;
   }
}
