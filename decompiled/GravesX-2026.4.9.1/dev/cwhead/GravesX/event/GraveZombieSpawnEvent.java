package dev.cwhead.GravesX.event;

import com.ranull.graves.type.Grave;
import dev.cwhead.GravesX.event.graveevent.GraveEvent;
import dev.cwhead.GravesX.exception.GravesXNullPointerException;
import java.util.Objects;
import java.util.UUID;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class GraveZombieSpawnEvent extends GraveEvent {
   private static final HandlerList HANDLERS = new HandlerList();
   @NotNull
   private final LivingEntity targetEntity;

   public GraveZombieSpawnEvent(@NotNull Location location, @NotNull LivingEntity targetEntity, @NotNull Grave grave) {
      super(Objects.requireNonNull(grave, "grave"), Objects.requireNonNull(location, "location"), null, grave.getLocationDeath().getBlock());
      this.targetEntity = Objects.requireNonNull(targetEntity, "targetEntity");
   }

   @NotNull
   public LivingEntity getTargetEntity() {
      return this.targetEntity;
   }

   @NotNull
   public EntityType getTargetEntityType() {
      return this.targetEntity.getType();
   }

   public boolean hasPlayerTarget() {
      return this.targetEntity instanceof Player;
   }

   @NotNull
   public Player getTargetPlayer() {
      if (this.targetEntity instanceof Player p) {
         return p;
      } else {
         throw new GravesXNullPointerException(this, "player");
      }
   }

   @NotNull
   public String getTargetPlayerName() {
      return this.getTargetPlayer().getName();
   }

   @NotNull
   public UUID getTargetPlayerUniqueId() {
      return this.getTargetPlayer().getUniqueId();
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
