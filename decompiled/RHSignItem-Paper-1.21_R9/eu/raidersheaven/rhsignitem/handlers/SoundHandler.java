package eu.raidersheaven.rhsignitem.handlers;

import eu.raidersheaven.rhsignitem.configurations.ConfigFile;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.entity.Player;

public class SoundHandler {
   public static void fail(Player player) {
      if (ConfigFile.enableSounds) {
         player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_XYLOPHONE, SoundCategory.MASTER, 0.4F, 0.6F);
      }
   }

   public static void success(Player player) {
      if (ConfigFile.enableSounds) {
         player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_XYLOPHONE, SoundCategory.MASTER, 0.4F, 0.9F);
      }
   }

   public static void version(Player player) {
      if (ConfigFile.enableSounds) {
         player.playSound(player.getLocation(), Sound.BLOCK_AMETHYST_BLOCK_CHIME, SoundCategory.MASTER, 0.9F, 1.6F);
      }
   }
}
