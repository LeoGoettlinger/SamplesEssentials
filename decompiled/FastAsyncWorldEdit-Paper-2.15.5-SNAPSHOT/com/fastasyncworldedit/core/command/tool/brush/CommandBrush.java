package com.fastasyncworldedit.core.command.tool.brush;

import com.fastasyncworldedit.core.FaweCache;
import com.fastasyncworldedit.core.util.StringMan;
import com.fastasyncworldedit.core.wrappers.LocationMaskedPlayerWrapper;
import com.fastasyncworldedit.core.wrappers.SilentPlayerWrapper;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.command.tool.brush.Brush;
import com.sk89q.worldedit.entity.Player;
import com.sk89q.worldedit.event.platform.CommandEvent;
import com.sk89q.worldedit.extension.platform.Actor;
import com.sk89q.worldedit.extension.platform.PlatformCommandManager;
import com.sk89q.worldedit.function.pattern.Pattern;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.selector.CuboidRegionSelector;
import com.sk89q.worldedit.util.Location;

public class CommandBrush implements Brush {
   private final String command;
   private final boolean print;

   @Deprecated(
      forRemoval = true
   )
   public CommandBrush(String command) {
      this(command, false);
   }

   public CommandBrush(String command, boolean print) {
      this.command = command;
      this.print = print;
   }

   @Override
   public void build(EditSession editSession, BlockVector3 position, Pattern pattern, double size) throws MaxChangedBlocksException {
      int radius = (int)size;
      CuboidRegionSelector selector = new CuboidRegionSelector(
         editSession.getWorld(), position.subtract(radius, radius, radius), position.add(radius, radius, radius)
      );
      String replaced = this.command
         .replace("{x}", Integer.toString(position.x()))
         .replace("{y}", Integer.toString(position.y()))
         .replace("{z}", Integer.toString(position.z()))
         .replace("{world}", editSession.getWorld().getName())
         .replace("{size}", Integer.toString(radius));
      if (!(editSession.getActor() instanceof Player player)) {
         throw FaweCache.PLAYER_ONLY;
      } else {
         player.setSelection(selector);
         Object wePlayer = new LocationMaskedPlayerWrapper(player, new Location(player.getExtent(), position.toVector3()));
         if (!this.print) {
            wePlayer = new SilentPlayerWrapper((Player)wePlayer);
         }

         for (String cmd : StringMan.split(replaced, ';')) {
            if (!cmd.isBlank()) {
               cmd = cmd.charAt(0) != '/' ? "/" + cmd : cmd;
               cmd = cmd.length() > 1 && cmd.charAt(1) == '/' ? cmd.substring(1) : cmd;
               CommandEvent event = new CommandEvent((Actor)wePlayer, cmd, editSession);
               PlatformCommandManager.getInstance().handleCommandOnCurrentThread(event);
            }
         }
      }
   }
}
