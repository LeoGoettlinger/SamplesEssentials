package dev.cwhead.GravesX.event;

import com.ranull.graves.type.Grave;
import dev.cwhead.GravesX.event.graveevent.GraveEvent;
import dev.cwhead.GravesX.exception.GravesXIllegalArgumentException;
import dev.cwhead.GravesX.exception.GravesXNullPointerException;
import java.util.Objects;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class GraveObituaryAddEvent extends GraveEvent {
   private static final HandlerList HANDLERS = new HandlerList();
   @Nullable
   private final Entity entity;

   public GraveObituaryAddEvent(@NotNull Grave grave, @NotNull Location location, @Nullable Entity entity) {
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
      if (this.entity instanceof Player p) {
         Player player = p.getPlayer();
         return player != null;
      } else {
         return false;
      }
   }

   @NotNull
   public Player getPlayer() {
      if (this.entity instanceof Player p) {
         Player player = p.getPlayer();
         if (player != null) {
            return player;
         } else {
            throw new GravesXNullPointerException(this, "player");
         }
      } else {
         EntityType entityType = this.entity != null ? this.entity.getType() : EntityType.UNKNOWN;
         throw new GravesXIllegalArgumentException("GraveObituaryAddEvent made a call to get player. Received " + entityType + " instead.");
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
