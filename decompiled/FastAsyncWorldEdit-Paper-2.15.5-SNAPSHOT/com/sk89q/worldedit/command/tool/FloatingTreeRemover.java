package com.sk89q.worldedit.command.tool;

import com.fastasyncworldedit.core.configuration.Caption;
import com.fastasyncworldedit.core.math.LocalBlockVectorSet;
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
import com.sk89q.worldedit.world.World;
import com.sk89q.worldedit.world.block.BlockCategories;
import com.sk89q.worldedit.world.block.BlockState;
import com.sk89q.worldedit.world.block.BlockType;
import com.sk89q.worldedit.world.block.BlockTypes;
import java.util.Iterator;
import java.util.Set;
import javax.annotation.Nullable;

public class FloatingTreeRemover implements BlockTool {
   private final int rangeSq;
   private final BlockVector3[] recurseDirections = new BlockVector3[]{
      Direction.NORTH.toBlockVector(),
      Direction.EAST.toBlockVector(),
      Direction.SOUTH.toBlockVector(),
      Direction.WEST.toBlockVector(),
      Direction.UP.toBlockVector(),
      Direction.DOWN.toBlockVector()
   };

   public FloatingTreeRemover() {
      this.rangeSq = 10000;
   }

   @Override
   public boolean canUse(Actor player) {
      return player.hasPermission("worldedit.tool.deltree");
   }

   private boolean isTreeBlock(BlockType type) {
      return BlockCategories.LEAVES.contains(type)
         || BlockCategories.LOGS.contains(type)
         || type == BlockTypes.RED_MUSHROOM_BLOCK
         || type == BlockTypes.BROWN_MUSHROOM_BLOCK
         || type == BlockTypes.MUSHROOM_STEM
         || type == BlockTypes.VINE
         || type == BlockTypes.NETHER_WART_BLOCK
         || type == BlockTypes.WARPED_WART_BLOCK
         || type == BlockTypes.CRIMSON_STEM
         || type == BlockTypes.WARPED_STEM;
   }

   @Override
   public boolean actPrimary(Platform server, LocalConfiguration config, Player player, LocalSession session, Location clicked, @Nullable Direction face) {
      World world = (World)clicked.getExtent();
      BlockState state = world.getBlock(clicked.toVector().toBlockPoint());
      if (!this.isTreeBlock(state.getBlockType())) {
         player.print(Caption.of("worldedit.tool.deltree.not-tree"));
         return true;
      } else {
         try (EditSession editSession = session.createEditSession(player, "FloatingTreeRemover")) {
            try {
               Set<BlockVector3> blockSet = this.bfs(world, clicked.toVector().toBlockPoint());
               if (blockSet == null) {
                  player.print(Caption.of("worldedit.tool.deltree.not-floating"));
                  return true;
               }

               for (BlockVector3 blockVector : blockSet) {
                  BlockState otherState = editSession.getBlock(blockVector);
                  if (this.isTreeBlock(otherState.getBlockType())) {
                     editSession.setBlock(blockVector, BlockTypes.AIR.getDefaultState());
                  }
               }
            } catch (MaxChangedBlocksException var20) {
               player.print(Caption.of("worldedit.tool.max-block-changes"));
            } finally {
               session.remember(editSession);
            }
         }

         return true;
      }
   }

   private Set<BlockVector3> bfs(World world, BlockVector3 origin) {
      LocalBlockVectorSet visited = new LocalBlockVectorSet();
      LocalBlockVectorSet queue = new LocalBlockVectorSet();
      queue.add(origin);
      visited.add(origin);

      while (!queue.isEmpty()) {
         Iterator<BlockVector3> iter = queue.iterator();

         while (iter.hasNext()) {
            BlockVector3 current = iter.next();
            iter.remove();

            for (BlockVector3 recurseDirection : this.recurseDirections) {
               BlockVector3 next = current.add(recurseDirection);
               if (origin.distanceSq(next) <= this.rangeSq && visited.add(next)) {
                  BlockState state = world.getBlock(next);
                  if (!state.getBlockType().getMaterial().isAir() && state.getBlockType() != BlockTypes.SNOW) {
                     if (this.isTreeBlock(state.getBlockType())) {
                        queue.add(next);
                     } else {
                        BlockType currentType = world.getBlock(current).getBlockType();
                        if (!BlockCategories.LEAVES.contains(currentType) && currentType != BlockTypes.VINE) {
                           return null;
                        }
                     }
                  }
               }
            }
         }
      }

      return visited;
   }
}
