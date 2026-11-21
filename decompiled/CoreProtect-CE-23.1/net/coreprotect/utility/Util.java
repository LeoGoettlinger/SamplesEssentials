package net.coreprotect.utility;

import java.util.regex.Pattern;
import net.coreprotect.consumer.Queue;
import org.bukkit.Location;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;

public class Util extends Queue {
   public static final Pattern tagParser = Pattern.compile("<COMPONENT>(.+?)</COMPONENT>|(.+?)", 32);

   private Util() {
      throw new IllegalStateException("Utility class");
   }

   public static void sendBlockChange(Player player, Location location, BlockData blockData) {
      player.sendBlockChange(location, blockData);
   }
}
