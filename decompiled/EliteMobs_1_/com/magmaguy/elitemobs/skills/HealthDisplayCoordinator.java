package com.magmaguy.elitemobs.skills;

import java.util.EnumSet;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public final class HealthDisplayCoordinator {
   private static final double DISPLAY_HEALTH = 20.0;
   private static final Map<UUID, HealthDisplayCoordinator.OwnershipState> states = new ConcurrentHashMap<>();

   private HealthDisplayCoordinator() {
   }

   public static void acquire(Player player, HealthDisplayCoordinator.Owner owner) {
      if (player != null && owner != null) {
         states.compute(
            player.getUniqueId(),
            (ignored, currentState) -> {
               HealthDisplayCoordinator.DisplayBaseline baseline = currentState == null
                  ? new HealthDisplayCoordinator.DisplayBaseline(player.isHealthScaled(), player.getHealthScale())
                  : currentState.baseline();
               EnumSet<HealthDisplayCoordinator.Owner> updated = currentState == null
                  ? EnumSet.noneOf(HealthDisplayCoordinator.Owner.class)
                  : EnumSet.copyOf(currentState.owners());
               updated.add(owner);
               return new HealthDisplayCoordinator.OwnershipState(updated, baseline);
            }
         );
         player.setHealthScale(20.0);
         player.setHealthScaled(true);
      }
   }

   public static void release(Player player, HealthDisplayCoordinator.Owner owner) {
      if (player != null && owner != null) {
         HealthDisplayCoordinator.OwnershipState current = states.get(player.getUniqueId());
         if (current != null && current.owners().contains(owner)) {
            EnumSet<HealthDisplayCoordinator.Owner> updated = EnumSet.copyOf(current.owners());
            updated.remove(owner);
            if (!updated.isEmpty()) {
               states.put(player.getUniqueId(), new HealthDisplayCoordinator.OwnershipState(updated, current.baseline()));
            } else if (!states.remove(player.getUniqueId(), current)) {
               release(player, owner);
            } else {
               restore(player, current.baseline());
            }
         }
      }
   }

   public static void clear(Player player) {
      if (player != null) {
         HealthDisplayCoordinator.OwnershipState state = states.remove(player.getUniqueId());
         if (state != null) {
            restore(player, state.baseline());
         }
      }
   }

   public static void shutdown() {
      for (Player player : Bukkit.getOnlinePlayers()) {
         clear(player);
      }

      states.clear();
   }

   public static void clearStaleAdvancedDisplay(Player player) {
      if (player != null && !states.containsKey(player.getUniqueId())) {
         if (player.isHealthScaled() && Math.abs(player.getHealthScale() - 20.0) < 1.0E-6) {
            player.setHealthScaled(false);
         }
      }
   }

   private static void restore(Player player, HealthDisplayCoordinator.DisplayBaseline baseline) {
      if (baseline.scaled()) {
         player.setHealthScale(baseline.scale());
         player.setHealthScaled(true);
      } else {
         player.setHealthScaled(false);
      }
   }

   private record DisplayBaseline(boolean scaled, double scale) {
   }

   public static enum Owner {
      ARMOR_SKILL,
      ADVANCED_COMBAT;
   }

   private record OwnershipState(EnumSet<HealthDisplayCoordinator.Owner> owners, HealthDisplayCoordinator.DisplayBaseline baseline) {
      private OwnershipState(EnumSet<HealthDisplayCoordinator.Owner> owners, HealthDisplayCoordinator.DisplayBaseline baseline) {
         owners = EnumSet.copyOf(owners);
         this.owners = owners;
         this.baseline = baseline;
      }
   }
}
