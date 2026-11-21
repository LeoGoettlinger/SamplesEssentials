package github.nighter.smartspawner.hooks.protections;

import org.bukkit.Location;
import org.bukkit.entity.Player;

public interface ProtectionHook {
   default boolean canBreak(Player player, Location location) {
      return true;
   }

   default boolean canStack(Player player, Location location) {
      return true;
   }

   default boolean canOpenMenu(Player player, Location location) {
      return true;
   }
}
