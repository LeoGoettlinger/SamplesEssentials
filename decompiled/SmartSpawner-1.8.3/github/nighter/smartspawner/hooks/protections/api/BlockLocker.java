package github.nighter.smartspawner.hooks.protections.api;

import github.nighter.smartspawner.hooks.protections.ProtectionHook;
import nl.rutgerkok.blocklocker.BlockLockerAPI;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class BlockLocker implements ProtectionHook {
   @Override
   public boolean canOpenMenu(@NotNull Player player, @NotNull Location location) {
      return hasAccess(player, location);
   }

   @Override
   public boolean canStack(@NotNull Player player, @NotNull Location location) {
      return hasAccess(player, location);
   }

   @Override
   public boolean canBreak(@NotNull Player player, @NotNull Location location) {
      return hasAccess(player, location);
   }

   private static boolean hasAccess(@NotNull Player player, @NotNull Location location) {
      try {
         Block block = location.getBlock();
         return !BlockLockerAPI.isProtected(block) ? true : BlockLockerAPI.isAllowed(player, block, true);
      } catch (Exception var3) {
         return true;
      }
   }
}
