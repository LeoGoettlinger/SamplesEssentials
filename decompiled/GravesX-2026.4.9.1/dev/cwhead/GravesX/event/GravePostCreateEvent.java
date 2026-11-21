package dev.cwhead.GravesX.event;

import com.ranull.graves.type.Grave;
import dev.cwhead.GravesX.event.graveevent.GraveEntityEvent;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class GravePostCreateEvent extends GraveEntityEvent {
   private static final HandlerList HANDLERS = new HandlerList();
   @Nullable
   private final Location placedLocation;

   public GravePostCreateEvent(@NotNull Entity entity, @NotNull Grave grave, @Nullable Location placedLocation) {
      super(grave, entity, grave.getLocationDeath(), null, null, entity instanceof LivingEntity ? (LivingEntity)entity : null, null);
      this.placedLocation = placedLocation;
   }

   @Nullable
   public Location getPlacedLocation() {
      return this.placedLocation;
   }

   public boolean isPlaced() {
      return this.placedLocation != null;
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
