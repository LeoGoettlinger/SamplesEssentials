package dev.cwhead.GravesX.event;

import com.ranull.graves.type.Grave;
import dev.cwhead.GravesX.event.graveevent.GraveEvent;
import dev.cwhead.GravesX.exception.GravesXNullPointerException;
import java.util.Objects;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class GravePreExplodeEvent extends GraveEvent {
   @Nullable
   private Entity source;
   private float radius;

   public GravePreExplodeEvent(@NotNull Grave grave, @NotNull Location explosionLocation, @Nullable Entity source, float radius) {
      super(grave, explosionLocation, null, null);
      this.source = source;
      this.radius = radius;
   }

   @Nullable
   public Entity getSource() {
      return this.source;
   }

   public void setSource(@Nullable Entity source) {
      this.source = source;
   }

   @Nullable
   public EntityType getSourceType() {
      return this.source != null ? this.source.getType() : null;
   }

   @NotNull
   public Location getExplosionLocation() {
      return this.getLocation();
   }

   public void setExplosionLocation(@NotNull Location location) {
      this.setLocation(Objects.requireNonNull(location, "location"));
   }

   public float getRadius() {
      return this.radius;
   }

   public void setRadius(float radius) {
      this.radius = radius;
   }

   public boolean hasWorld() {
      return this.getExplosionLocation().getWorld() != null;
   }

   @NotNull
   public World getWorld() {
      World world = this.getExplosionLocation().getWorld();
      if (world != null) {
         return world;
      } else {
         throw new GravesXNullPointerException(this, "world");
      }
   }

   @Nullable
   public World getWorldNullable() {
      return this.getExplosionLocation().getWorld();
   }

   public void setWorld(@NotNull World world) {
      Location loc = this.getExplosionLocation();
      loc.setWorld(Objects.requireNonNull(world, "world"));
      this.setLocation(loc);
   }
}
