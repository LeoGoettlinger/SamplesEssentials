package io.github.illa4257.opensit;

import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

public final class SitCommand implements CommandExecutor {
   public final double offset;

   public SitCommand(OpenSitListener listener) {
      this.offset = listener.offset - 0.2;
   }

   public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
      if (!(sender instanceof Player plr)) {
         sender.sendMessage("This command is only for players.");
         return true;
      } else if (!plr.hasPermission("OpenSit.Sit")) {
         sender.sendMessage("You don't have permission");
         return true;
      } else if (plr.isInsideVehicle()) {
         sender.sendMessage("You're sitting now.");
         return true;
      } else if (plr.isSneaking()) {
         sender.sendMessage("You're sneaking now.");
         return true;
      } else {
         Location l = plr.getLocation();
         RayTraceResult r = l.getWorld().rayTraceBlocks(l, new Vector(0, -1, 0), 0.001);
         if (r == null) {
            sender.sendMessage("You do not stand on the ground.");
            return true;
         } else {
            BlockDisplay b = (BlockDisplay)l.getWorld()
               .spawnEntity(new Location(l.getWorld(), l.getX(), r.getHitPosition().getY() + this.offset, l.getZ()), EntityType.BLOCK_DISPLAY);
            b.addScoreboardTag("sit");
            b.addPassenger(plr);
            return true;
         }
      }
   }
}
