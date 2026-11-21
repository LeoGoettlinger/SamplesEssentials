package com.magmaguy.elitemobs.api;

import com.magmaguy.elitemobs.config.contentpackages.ContentPackagesConfigFields;
import lombok.Generated;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class DungeonUninstallEvent extends Event implements Cancellable {
   private static final HandlerList handlers = new HandlerList();
   private final ContentPackagesConfigFields contentPackagesConfigFields;
   private final String dungeonName;
   private final String dungeonFilename;
   private boolean isCancelled = false;

   public DungeonUninstallEvent(ContentPackagesConfigFields contentPackagesConfigFields) {
      this.contentPackagesConfigFields = contentPackagesConfigFields;
      this.dungeonName = contentPackagesConfigFields.getName();
      this.dungeonFilename = contentPackagesConfigFields.getFilename();
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
   public ContentPackagesConfigFields getContentPackagesConfigFields() {
      return this.contentPackagesConfigFields;
   }

   @Generated
   public String getDungeonName() {
      return this.dungeonName;
   }

   @Generated
   public String getDungeonFilename() {
      return this.dungeonFilename;
   }
}
