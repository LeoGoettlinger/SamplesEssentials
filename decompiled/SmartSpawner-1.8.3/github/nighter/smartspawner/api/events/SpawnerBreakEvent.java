package github.nighter.smartspawner.api.events;

import lombok.Generated;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SpawnerBreakEvent extends SpawnerEvent {
   private static final HandlerList handlers = new HandlerList();
   private final Entity entity;
   private final EntityType entityType;

   public SpawnerBreakEvent(Entity entity, Location location, int quantity) {
      this(entity, location, quantity, null);
   }

   public SpawnerBreakEvent(Entity entity, Location location, int quantity, @Nullable EntityType entityType) {
      super(location, quantity);
      this.entity = entity;
      this.entityType = entityType;
   }

   @NotNull
   @Override
   public HandlerList getHandlers() {
      return handlers;
   }

   @NotNull
   public static HandlerList getHandlerList() {
      return handlers;
   }

   @Generated
   public Entity getEntity() {
      return this.entity;
   }

   @Generated
   public EntityType getEntityType() {
      return this.entityType;
   }
}
