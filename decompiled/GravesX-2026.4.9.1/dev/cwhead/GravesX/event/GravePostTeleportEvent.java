package dev.cwhead.GravesX.event;

import com.ranull.graves.type.Grave;
import dev.cwhead.GravesX.event.graveevent.GraveEntityEvent;
import java.util.Objects;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class GravePostTeleportEvent extends GraveEntityEvent {
   private static final HandlerList HANDLERS = new HandlerList();
   private final Location from;
   private final Location to;

   public GravePostTeleportEvent(@NotNull Grave grave, @NotNull Entity entity, @NotNull Location from, @NotNull Location to) {
      super(
         Objects.requireNonNull(grave, "grave"),
         Objects.requireNonNull(entity, "entity"),
         Objects.requireNonNull(to, "to"),
         null,
         null,
         entity instanceof LivingEntity le ? le : null,
         null
      );
      this.from = Objects.requireNonNull(from, "from").clone();
      this.to = to.clone();
   }

   @NotNull
   public Location getFrom() {
      return this.from.clone();
   }

   @NotNull
   public Location getTo() {
      return this.to.clone();
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
