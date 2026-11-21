package dev.cwhead.GravesX.event;

import com.ranull.graves.type.Grave;
import dev.cwhead.GravesX.event.graveevent.GraveEntityEvent;
import java.util.Objects;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class GraveWalkOverEvent extends GraveEntityEvent {
   private static final HandlerList HANDLERS = new HandlerList();

   public GraveWalkOverEvent(@NotNull Entity entity, @NotNull Location location, @NotNull Grave grave) {
      super(
         Objects.requireNonNull(grave, "grave"),
         Objects.requireNonNull(entity, "entity"),
         Objects.requireNonNull(location, "location"),
         null,
         grave.getLocationDeath().getBlock(),
         entity instanceof LivingEntity le ? le : null,
         null
      );
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
