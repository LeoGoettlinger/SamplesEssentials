package net.milkbowl.vault2.minecraft;

import net.milkbowl.vault2.helper.subject.Subject;
import org.bukkit.OfflinePlayer;

public class BukkitHelper {
   public static Subject fromPlayer(final OfflinePlayer player) {
      return Subject.player(player.getUniqueId(), player.getName());
   }
}
