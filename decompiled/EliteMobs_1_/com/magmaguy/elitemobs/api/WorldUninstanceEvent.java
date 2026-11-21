package com.magmaguy.elitemobs.api;

import com.magmaguy.elitemobs.config.contentpackages.ContentPackagesConfigFields;
import lombok.Generated;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.Nullable;

public class WorldUninstanceEvent extends Event {
   private static final HandlerList handlers = new HandlerList();
   private final ContentPackagesConfigFields contentPackagesConfigFields;
   @Nullable
   private final String instancedWorldName;

   @Deprecated(
      forRemoval = false
   )
   public WorldUninstanceEvent(ContentPackagesConfigFields contentPackagesConfigFields) {
      this(contentPackagesConfigFields, null);
   }

   public WorldUninstanceEvent(ContentPackagesConfigFields contentPackagesConfigFields, @Nullable String instancedWorldName) {
      this.contentPackagesConfigFields = contentPackagesConfigFields;
      this.instancedWorldName = instancedWorldName;
   }

   public static HandlerList getHandlerList() {
      return handlers;
   }

   public HandlerList getHandlers() {
      return handlers;
   }

   @Generated
   public ContentPackagesConfigFields getContentPackagesConfigFields() {
      return this.contentPackagesConfigFields;
   }

   @Nullable
   @Generated
   public String getInstancedWorldName() {
      return this.instancedWorldName;
   }
}
