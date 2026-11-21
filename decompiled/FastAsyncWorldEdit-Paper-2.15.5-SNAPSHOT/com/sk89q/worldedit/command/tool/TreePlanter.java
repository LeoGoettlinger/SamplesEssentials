package com.sk89q.worldedit.command.tool;

import com.fastasyncworldedit.core.configuration.Caption;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.LocalConfiguration;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.entity.Player;
import com.sk89q.worldedit.extension.platform.Actor;
import com.sk89q.worldedit.extension.platform.Platform;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.util.Direction;
import com.sk89q.worldedit.util.Location;
import com.sk89q.worldedit.world.generation.TreeType;
import javax.annotation.Nullable;

public class TreePlanter implements BlockTool {
   private final TreeType treeType;

   public TreePlanter(TreeType treeType) {
      this.treeType = treeType;
   }

   @Override
   public boolean canUse(Actor player) {
      return player.hasPermission("worldedit.tool.tree");
   }

   @Override
   public boolean actPrimary(Platform server, LocalConfiguration config, Player player, LocalSession session, Location clicked, @Nullable Direction face) {
      try (EditSession editSession = session.createEditSession(player, null)) {
         try {
            boolean successful = false;
            BlockVector3 pos = clicked.toVector().add(0.0, 1.0, 0.0).toBlockPoint();

            for (int i = 0; i < 10; i++) {
               if (player.getWorld().generateTree(this.treeType, editSession, pos)) {
                  successful = true;
                  break;
               }
            }

            if (!successful) {
               player.print(Caption.of("worldedit.tool.tree.obstructed"));
            }
         } catch (MaxChangedBlocksException var17) {
            player.print(Caption.of("worldedit.tool.max-block-changes"));
         } finally {
            session.remember(editSession);
         }
      }

      return true;
   }
}
