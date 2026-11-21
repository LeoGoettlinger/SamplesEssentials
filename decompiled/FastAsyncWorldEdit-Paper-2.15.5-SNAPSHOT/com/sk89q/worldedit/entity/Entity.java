package com.sk89q.worldedit.entity;

import com.sk89q.worldedit.extension.platform.Locatable;
import com.sk89q.worldedit.util.Faceted;
import com.sk89q.worldedit.world.entity.EntityType;
import javax.annotation.Nullable;

public interface Entity extends Faceted, Locatable {
   @Nullable
   BaseEntity getState();

   default EntityType getType() {
      BaseEntity state = this.getState();
      return state != null ? state.getType() : null;
   }

   boolean remove();
}
