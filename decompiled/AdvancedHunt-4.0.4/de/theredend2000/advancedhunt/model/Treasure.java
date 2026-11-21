package de.theredend2000.advancedhunt.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import org.bukkit.Location;

public class Treasure {
   private final UUID id;
   private final UUID collectionId;
   private final Location location;
   private final List<Reward> rewards;
   private final String nbtData;
   private final String material;
   private final String blockState;

   public Treasure(UUID id, UUID collectionId, Location location, List<Reward> rewards, String nbtData, String material, String blockState) {
      this.id = Objects.requireNonNull(id, "id must not be null");
      this.collectionId = Objects.requireNonNull(collectionId, "collectionId must not be null");
      this.location = location != null ? location.clone() : null;
      this.rewards = rewards != null ? new ArrayList<>(rewards) : new ArrayList<>();
      this.nbtData = nbtData;
      this.material = material;
      this.blockState = blockState;
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

   public List<Reward> getRewards() {
      return Collections.unmodifiableList(this.rewards);
   }

   public String getNbtData() {
      return this.nbtData;
   }

   public String getMaterial() {
      return this.material;
   }

   public String getBlockState() {
      return this.blockState;
   }

   @Override
   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else if (!(o instanceof Treasure)) {
         return false;
      } else {
         Treasure that = (Treasure)o;
         return this.id.equals(that.id);
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.id);
   }

   @Override
   public String toString() {
      String locationSummary;
      if (this.location == null) {
         locationSummary = "null";
      } else {
         String worldName = this.location.getWorld() != null ? this.location.getWorld().getName() : "null";
         locationSummary = worldName + "@" + this.location.getBlockX() + "," + this.location.getBlockY() + "," + this.location.getBlockZ();
      }

      return "Treasure{id="
         + this.id
         + ", collectionId="
         + this.collectionId
         + ", location="
         + locationSummary
         + ", rewardsCount="
         + this.rewards.size()
         + ", material='"
         + this.material
         + '\''
         + '}';
   }
}
