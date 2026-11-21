package com.magmaguy.elitemobs.easyminecraftgoals.internal;

import java.util.Set;
import java.util.UUID;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

public interface TrackedPacketEntity {
   default void updateTracking() {
   }

   Location getTrackingLocation();

   World getWorld();

   void showToPlayer(Player var1);

   void hideFromPlayer(Player var1);

   boolean isVisibleTo(Player var1);

   default boolean canBeSeenBy(Player player) {
      return true;
   }

   Set<UUID> getCurrentViewers();

   boolean isValid();

   UUID getUniqueId();

   Entity getVehicle();

   void remount();
}
