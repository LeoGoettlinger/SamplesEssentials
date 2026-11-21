package org.lushplugins.lushrewards.utils;

import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class PlayerUtils {
   public static UUID getUniqueId(String nameOrUuid) throws IllegalArgumentException {
      Player player = Bukkit.getPlayer(nameOrUuid);
      return player != null ? player.getUniqueId() : UUID.fromString(nameOrUuid);
   }
}
