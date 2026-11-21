package de.theredend2000.advancedhunt.model;

import java.util.Objects;
import java.util.UUID;
import org.bukkit.Location;
import org.bukkit.World;

public class TreasureCore {
   private final UUID id;
   private final UUID collectionId;
   private final Location location;
   private final String material;
   private final String blockState;

   public TreasureCore(UUID id, UUID collectionId, Location location, String material, String blockState) {
      this.id = Objects.requireNonNull(id, "id cannot be null");
      this.collectionId = Objects.requireNonNull(collectionId, "collectionId cannot be null");
      this.location = location != null ? location.clone() : null;
      this.material = Objects.requireNonNull(material, "material cannot be null");
      this.blockState = blockState;
   }

   public TreasureCore(UUID id, UUID collectionId, Location location, String material) {
      this(id, collectionId, location, material, null);
   }

   public UUID getId() {
      return this.id;
   }

   public UUID getCollectionId() {
      return this.collectionId;
   }

   public Location getLocation() {
      return this.location != null ? this.location.clone() : null;
   }

   public String getMaterial() {
      return this.material;
   }

   public String getBlockState() {
      return this.blockState;
   }

   public boolean isInLoadedWorld(World world) {
      if (this.location == null) {
         return false;
      } else {
         World locationWorld = this.location.getWorld();
         if (locationWorld == null || world == null) {
            return false;
         } else if (locationWorld == world) {
            return true;
         } else {
            String locationWorldName = locationWorld.getName();
            String worldName = world.getName();
            return locationWorldName != null && locationWorldName.equals(worldName);
         }
      }
   }

   public String getWorldNameOr(String fallback) {
      if (this.location != null && this.location.getWorld() != null) {
         String worldName = this.location.getWorld().getName();
         return worldName != null ? worldName : fallback;
      } else {
         return fallback;
      }
   }

   public static TreasureCore from(Treasure treasure) {
      Objects.requireNonNull(treasure, "treasure cannot be null");
      return new TreasureCore(treasure.getId(), treasure.getCollectionId(), treasure.getLocation(), treasure.getMaterial(), treasure.getBlockState());
   }

   @Override
   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else if (!(o instanceof TreasureCore)) {
         return false;
      } else {
         TreasureCore that = (TreasureCore)o;
         return this.id.equals(that.id);
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.id);
   }

   @Override
   public String toString() {
      String locationSummary = this.location != null
         ? String.format(
            "%s@(%d,%d,%d)",
            this.location.getWorld() != null ? this.location.getWorld().getName() : "null",
            this.location.getBlockX(),
            this.location.getBlockY(),
            this.location.getBlockZ()
         )
         : "null";
      return String.format("TreasureCore{id=%s, material=%s, location=%s}", this.id, this.material, locationSummary);
   }
}
