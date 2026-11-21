package dev.cwhead.GravesX.event;

import com.ranull.graves.type.Grave;
import dev.cwhead.GravesX.event.graveevent.GraveEvent;
import java.util.Objects;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class GravePlayerHeadDropEvent extends GraveEvent {
   private static final HandlerList HANDLERS = new HandlerList();
   @Nullable
   private final Entity entity;

   public GravePlayerHeadDropEvent(@NotNull Grave grave, @NotNull Location location, @Nullable Entity entity) {
      super(Objects.requireNonNull(grave, "grave"), Objects.requireNonNull(location, "location"), null, null);
      this.entity = entity;
   }

   public boolean hasEntity() {
      return this.entity != null;
   }

   @NotNull
   public Entity getEntity() {
      return this.entity;
   }

   @NotNull
   @Override
   public HandlerList getHandlers() {
      return HANDLERS;
   }

   @NotNull
   public static HandlerList getHandlerList() {
      return HANDLERS;
   }
}
