package dev.cwhead.GravesX.event;

import com.ranull.graves.type.Grave;
import dev.cwhead.GravesX.event.graveevent.GraveEntityEvent;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class GraveAutoLootEvent extends GraveEntityEvent {
   private static final HandlerList HANDLERS = new HandlerList();

   public GraveAutoLootEvent(@NotNull Entity entity, @NotNull Location location, @NotNull Grave grave) {
      super(grave, entity, location, null, null, entity instanceof LivingEntity ? (LivingEntity)entity : null, null);
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
