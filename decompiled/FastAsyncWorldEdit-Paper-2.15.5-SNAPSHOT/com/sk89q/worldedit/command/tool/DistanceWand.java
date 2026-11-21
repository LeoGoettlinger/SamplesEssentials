package com.sk89q.worldedit.command.tool;

import com.fastasyncworldedit.core.configuration.Caption;
import com.sk89q.worldedit.LocalConfiguration;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.entity.Player;
import com.sk89q.worldedit.extension.platform.Platform;
import com.sk89q.worldedit.extension.platform.permission.ActorSelectorLimits;
import com.sk89q.worldedit.function.mask.Mask;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.RegionSelector;
import com.sk89q.worldedit.util.Location;

public class DistanceWand extends BrushTool implements DoubleActionTraceTool {
   public DistanceWand() {
      super("worldedit.selection.pos");
   }

   @Override
   public boolean actSecondary(Platform server, LocalConfiguration config, Player player, LocalSession session) {
      Location target = this.getTarget(player);
      if (target == null) {
         return true;
      } else {
         RegionSelector selector = session.getRegionSelector(player.getWorld());
         BlockVector3 blockPoint = target.toVector().toBlockPoint();
         if (selector.selectPrimary(blockPoint, ActorSelectorLimits.forActor(player))) {
            selector.explainPrimarySelection(player, session, blockPoint);
         }

         return true;
      }
   }

   @Override
   public boolean actPrimary(Platform server, LocalConfiguration config, Player player, LocalSession session) {
      Location target = this.getTarget(player);
      if (target == null) {
         return true;
      } else {
         RegionSelector selector = session.getRegionSelector(player.getWorld());
         BlockVector3 blockPoint = target.toVector().toBlockPoint();
         if (selector.selectSecondary(blockPoint, ActorSelectorLimits.forActor(player))) {
            selector.explainSecondarySelection(player, session, blockPoint);
         }

         return true;
      }
   }

   private Location getTarget(Player player) {
      Mask mask = this.getTraceMask();
      Location target;
      if (this.range > -1) {
         target = player.getBlockTrace(this.getRange(), true, mask);
      } else {
         target = player.getBlockTrace(MAX_RANGE, false, mask);
      }

      if (target == null) {
         player.print(Caption.of("worldedit.tool.no-block"));
         return null;
      } else {
         return target;
      }
   }
}
