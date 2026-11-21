package com.sk89q.worldedit.command.tool;

import com.fastasyncworldedit.core.configuration.Caption;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.LocalConfiguration;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.entity.Player;
import com.sk89q.worldedit.extension.platform.Actor;
import com.sk89q.worldedit.extension.platform.Platform;
import com.sk89q.worldedit.extent.inventory.BlockBag;
import com.sk89q.worldedit.function.mask.Mask;
import com.sk89q.worldedit.function.pattern.Pattern;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.util.Location;
import com.sk89q.worldedit.world.block.BaseBlock;

public class LongRangeBuildTool extends BrushTool implements DoubleActionTraceTool {
   private final Pattern primary;
   private final Pattern secondary;

   public LongRangeBuildTool(Pattern secondary, Pattern primary) {
      super("worldedit.tool.lrbuild");
      this.primary = primary;
      this.secondary = secondary;
   }

   @Override
   public boolean canUse(Actor player) {
      return player.hasPermission("worldedit.tool.lrbuild");
   }

   @Override
   public boolean actSecondary(Platform server, LocalConfiguration config, Player player, LocalSession session) {
      Location pos = this.getTargetFace(player);
      if (pos == null) {
         return false;
      } else {
         BlockBag bag = session.getBlockBag(player);

         try (EditSession editSession = session.createEditSession(player, "LongRangeBuildTool")) {
            try {
               BlockVector3 blockPoint = pos.toVector().toBlockPoint();
               BaseBlock applied = this.secondary.applyBlock(blockPoint);
               if (applied.getBlockType().getMaterial().isAir()) {
                  editSession.setBlock(blockPoint, this.secondary);
               } else {
                  editSession.setBlock(pos.toVector().subtract(pos.getDirection()).toBlockPoint(), this.secondary);
               }
            } catch (MaxChangedBlocksException var23) {
            } finally {
               session.remember(editSession);
            }
         } finally {
            if (bag != null) {
               bag.flushChanges();
            }
         }

         return true;
      }
   }

   @Override
   public boolean actPrimary(Platform server, LocalConfiguration config, Player player, LocalSession session) {
      Location pos = this.getTargetFace(player);
      if (pos == null) {
         return false;
      } else {
         BlockBag bag = session.getBlockBag(player);

         try (EditSession editSession = session.createEditSession(player, "LongRangeBuildTool")) {
            try {
               BlockVector3 blockPoint = pos.toVector().toBlockPoint();
               BaseBlock applied = this.primary.applyBlock(blockPoint);
               if (applied.getBlockType().getMaterial().isAir()) {
                  editSession.setBlock(blockPoint, this.primary);
               } else {
                  editSession.setBlock(pos.toVector().subtract(pos.getDirection()).toBlockPoint(), this.primary);
               }
            } catch (MaxChangedBlocksException var23) {
            } finally {
               session.remember(editSession);
            }
         } finally {
            if (bag != null) {
               bag.flushChanges();
            }
         }

         return true;
      }
   }

   private Location getTargetFace(Player player) {
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
