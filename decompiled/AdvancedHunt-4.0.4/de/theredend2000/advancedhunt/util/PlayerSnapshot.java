package de.theredend2000.advancedhunt.util;

import java.util.Objects;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public final class PlayerSnapshot {
   private final Player player;
   private final Location location;

   public PlayerSnapshot(Player player, Location location) {
      this.player = player;
      this.location = location != null ? location.clone() : null;
   }

   public Player player() {
      return this.player;
   }

   public Location location() {
      return this.location != null ? this.location.clone() : null;
   }

   @Override
   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else if (!(o instanceof PlayerSnapshot)) {
         return false;
      } else {
         PlayerSnapshot that = (PlayerSnapshot)o;
         return Objects.equals(this.player, that.player) && Objects.equals(this.location, that.location);
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.player, this.location);
   }

   @Override
   public String toString() {
      return "PlayerSnapshot{player=" + this.player + ", location=" + this.location + '}';
   }
}
