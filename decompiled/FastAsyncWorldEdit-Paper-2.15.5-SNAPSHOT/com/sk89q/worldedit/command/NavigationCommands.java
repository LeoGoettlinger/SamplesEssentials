package com.sk89q.worldedit.command;

import com.fastasyncworldedit.core.configuration.Caption;
import com.google.common.base.Preconditions;
import com.sk89q.worldedit.LocalConfiguration;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.command.util.CommandPermissions;
import com.sk89q.worldedit.command.util.CommandPermissionsConditionGenerator;
import com.sk89q.worldedit.command.util.Logging;
import com.sk89q.worldedit.entity.Player;
import com.sk89q.worldedit.util.Location;
import com.sk89q.worldedit.util.formatting.text.TextComponent;
import org.enginehub.piston.annotation.Command;
import org.enginehub.piston.annotation.CommandContainer;
import org.enginehub.piston.annotation.param.Arg;
import org.enginehub.piston.annotation.param.Switch;

@CommandContainer(
   superTypes = {CommandPermissionsConditionGenerator.Registration.class}
)
public class NavigationCommands {
   private final WorldEdit worldEdit;

   public NavigationCommands(WorldEdit worldEdit) {
      Preconditions.checkNotNull(worldEdit);
      this.worldEdit = worldEdit;
   }

   @Command(
      name = "unstuck",
      aliases = {"!", "/unstuck"},
      desc = "Escape from being stuck inside a block"
   )
   @CommandPermissions(
      value = {"worldedit.navigation.unstuck"},
      queued = false
   )
   public void unstuck(Player player) throws WorldEditException {
      player.findFreePosition();
      player.print(Caption.of("worldedit.unstuck.moved"));
   }

   @Command(
      name = "ascend",
      aliases = {"asc", "/asc", "/ascend"},
      desc = "Go up a floor"
   )
   @CommandPermissions(
      value = {"worldedit.navigation.ascend"},
      queued = false
   )
   public void ascend(Player player, @Arg(desc = "# of levels to ascend",def = {"1"}) int levels) throws WorldEditException {
      int ascentLevels = 0;

      while (player.ascendLevel()) {
         if (levels == ++ascentLevels) {
            break;
         }
      }

      if (ascentLevels == 0) {
         player.print(Caption.of("worldedit.ascend.obstructed"));
      } else {
         player.print(Caption.of("worldedit.ascend.moved", TextComponent.of(ascentLevels)));
      }
   }

   @Command(
      name = "descend",
      aliases = {"desc", "/desc", "/descend"},
      desc = "Go down a floor"
   )
   @CommandPermissions(
      value = {"worldedit.navigation.descend"},
      queued = false
   )
   public void descend(Player player, @Arg(desc = "# of levels to descend",def = {"1"}) int levels) throws WorldEditException {
      int descentLevels = 0;

      while (player.descendLevel()) {
         if (levels == ++descentLevels) {
            break;
         }
      }

      if (descentLevels == 0) {
         player.print(Caption.of("worldedit.descend.obstructed"));
      } else {
         player.print(Caption.of("worldedit.descend.moved", TextComponent.of(descentLevels)));
      }
   }

   @Command(
      name = "ceil",
      aliases = {"/ceil", "/ceiling"},
      desc = "Go to the ceiling"
   )
   @CommandPermissions({"worldedit.navigation.ceiling"})
   @Logging(Logging.LogMode.POSITION)
   public void ceiling(
      Player player,
      @Arg(desc = "# of blocks to leave above you",def = {"0"}) int clearance,
      @Switch(name = 'f',desc = "Force using flight to keep you still") boolean forceFlight,
      @Switch(name = 'g',desc = "Force using glass to keep you still") boolean forceGlass
   ) throws WorldEditException {
      clearance = Math.max(0, clearance);
      boolean alwaysGlass = this.getAlwaysGlass(forceFlight, forceGlass);
      if (player.ascendToCeiling(clearance, alwaysGlass)) {
         player.print(Caption.of("worldedit.ceil.moved"));
      } else {
         player.print(Caption.of("worldedit.ceil.obstructed"));
      }
   }

   @Command(
      name = "thru",
      aliases = {"/thru"},
      desc = "Pass through walls"
   )
   @CommandPermissions(
      value = {"worldedit.navigation.thru.command"},
      queued = false
   )
   public void thru(Player player) throws WorldEditException {
      if (player.passThroughForwardWall(6)) {
         player.print(Caption.of("worldedit.thru.moved"));
      } else {
         player.print(Caption.of("worldedit.thru.obstructed"));
      }
   }

   @Command(
      name = "jumpto",
      aliases = {"j", "/jumpto", "/j"},
      desc = "Teleport to a location"
   )
   @CommandPermissions(
      value = {"worldedit.navigation.jumpto.command"},
      queued = false
   )
   public void jumpTo(Player player, @Arg(desc = "Location to jump to",def = {""}) Location pos, @Switch(name = 'f',desc = "force teleport") boolean force) throws WorldEditException {
      if (pos == null) {
         pos = player.getSolidBlockTrace(300);
      }

      if (pos != null) {
         player.findFreePosition(pos);
         player.print(Caption.of("worldedit.jumpto.moved"));
      } else {
         player.print(Caption.of("worldedit.jumpto.none"));
      }
   }

   @Command(
      name = "up",
      aliases = {"/up"},
      desc = "Go upwards some distance"
   )
   @CommandPermissions({"worldedit.navigation.up"})
   @Logging(Logging.LogMode.POSITION)
   public void up(
      Player player,
      @Arg(desc = "Distance to go upwards") int distance,
      @Switch(name = 'f',desc = "Force using flight to keep you still") boolean forceFlight,
      @Switch(name = 'g',desc = "Force using glass to keep you still") boolean forceGlass
   ) throws WorldEditException {
      boolean alwaysGlass = this.getAlwaysGlass(forceFlight, forceGlass);
      if (player.ascendUpwards(distance, alwaysGlass)) {
         player.print(Caption.of("worldedit.up.moved"));
      } else {
         player.print(Caption.of("worldedit.up.obstructed"));
      }
   }

   private boolean getAlwaysGlass(boolean forceFlight, boolean forceGlass) {
      LocalConfiguration config = this.worldEdit.getConfiguration();
      return forceGlass || config.navigationUseGlass && !forceFlight;
   }
}
