package de.theredend2000.advancedhunt.model;

import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

public class PlayerData {
   private final UUID playerUuid;
   private final Set<UUID> foundTreasures;

   public PlayerData(UUID playerUuid) {
      this.playerUuid = Objects.requireNonNull(playerUuid, "playerUuid must not be null");
      this.foundTreasures = new HashSet<>();
   }

   public UUID getPlayerUuid() {
      return this.playerUuid;
   }

   public Set<UUID> getFoundTreasures() {
      return Collections.unmodifiableSet(this.foundTreasures);
   }

   public void addFoundTreasure(UUID treasureId) {
      this.foundTreasures.add(treasureId);
   }

   public boolean hasFound(UUID treasureId) {
      return this.foundTreasures.contains(treasureId);
   }

   public void reset() {
      this.foundTreasures.clear();
   }

   @Override
   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else if (!(o instanceof PlayerData)) {
         return false;
      } else {
         PlayerData that = (PlayerData)o;
         return this.playerUuid.equals(that.playerUuid);
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.playerUuid);
   }

   @Override
   public String toString() {
      return "PlayerData{playerUuid=" + this.playerUuid + ", foundCount=" + this.foundTreasures.size() + "}";
   }
}
