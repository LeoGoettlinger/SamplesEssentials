package dev.cwhead.GravesX.event;

import com.ranull.graves.type.Grave;
import dev.cwhead.GravesX.event.graveevent.GraveEntityEvent;
import dev.cwhead.GravesX.exception.GravesXNullPointerException;
import java.util.Objects;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class GravePreTeleportEvent extends GraveEntityEvent {
   private static final HandlerList HANDLERS = new HandlerList();
   @NotNull
   private final Entity entity;

   public GravePreTeleportEvent(@NotNull Grave grave, @NotNull Entity entity) {
      super(
         Objects.requireNonNull(grave, "grave"),
         Objects.requireNonNull(entity, "entity"),
         grave.getLocationDeath(),
         null,
         null,
         entity instanceof LivingEntity le ? le : null,
         null
      );
      this.entity = entity;
   }

   public boolean isPlayer() {
      return this.entity instanceof Player;
   }

   @NotNull
   @Override
   public Player getPlayer() {
      if (this.entity instanceof Player p) {
         return p;
      } else {
         throw new GravesXNullPointerException(this, "player");
      }
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
