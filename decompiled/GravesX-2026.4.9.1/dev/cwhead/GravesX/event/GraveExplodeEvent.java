package dev.cwhead.GravesX.event;

import com.ranull.graves.type.Grave;
import dev.cwhead.GravesX.event.graveevent.GraveEvent;
import dev.cwhead.GravesX.exception.GravesXNullPointerException;
import java.util.Objects;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class GraveExplodeEvent extends GraveEvent {
   private static final HandlerList HANDLERS = new HandlerList();
   @Nullable
   private final Entity entity;

   public GraveExplodeEvent(@NotNull Location location, @Nullable Entity entity, @NotNull Grave grave) {
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

   public boolean hasPlayer() {
      return this.entity instanceof Player;
   }

   @NotNull
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
