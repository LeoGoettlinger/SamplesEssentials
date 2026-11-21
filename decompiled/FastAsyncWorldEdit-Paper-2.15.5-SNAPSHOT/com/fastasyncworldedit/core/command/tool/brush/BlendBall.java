package com.fastasyncworldedit.core.command.tool.brush;

import com.fastasyncworldedit.core.extent.filter.block.FilterBlock;
import com.fastasyncworldedit.core.function.mask.CachedMask;
import com.fastasyncworldedit.core.math.MutableBlockVector3;
import com.fastasyncworldedit.core.queue.Filter;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.command.tool.brush.Brush;
import com.sk89q.worldedit.extent.Extent;
import com.sk89q.worldedit.function.pattern.Pattern;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.world.block.BlockState;
import com.sk89q.worldedit.world.block.BlockTypes;
import java.util.Arrays;
import javax.annotation.Nullable;

public class BlendBall implements Brush {
   private static final BlockState AIR = BlockTypes.AIR.getDefaultState();
   private final int minFreqDiff;
   private final boolean onlyAir;
   @Nullable
   private final CachedMask mask;

   public BlendBall() {
      this(1, false, null);
   }

   public BlendBall(int minFreqDiff, boolean onlyAir, @Nullable CachedMask mask) {
      this.minFreqDiff = minFreqDiff;
      this.onlyAir = onlyAir;
      this.mask = mask;
   }

   @Override
   public void build(EditSession editSession, BlockVector3 position, Pattern pattern, double size) throws MaxChangedBlocksException {
      int outsetSize = (int)(size + 1.0);
      double brushSizeSquared = size * size;
      int tx = position.x();
      int ty = position.y();
      int tz = position.z();
      CuboidRegion region = new CuboidRegion(
         BlockVector3.at(tx - outsetSize, ty - outsetSize, tz - outsetSize), BlockVector3.at(tx + outsetSize, ty + outsetSize, tz + outsetSize)
      );
      editSession.apply(region, new BlendBall.BlendBallFilter((int)brushSizeSquared, tx, ty, tz), true);
   }

   private boolean maskFails(Extent extent, BlockVector3 pos) {
      return this.mask != null && !this.mask.test(extent, pos);
   }

   private class BlendBallFilter implements Filter {
      private final int[] frequency = new int[BlockTypes.size()];
      private final MutableBlockVector3 mutable = new MutableBlockVector3();
      private final int brushSizeSquared;
      private final int centerX;
      private final int centerY;
      private final int centerZ;

      private BlendBallFilter(final int brushSizeSquared, final int centerX, final int centerY, final int centerZ) {
         this.brushSizeSquared = brushSizeSquared;
         this.centerX = centerX;
         this.centerY = centerY;
         this.centerZ = centerZ;
      }

      @Override
      public void applyBlock(final FilterBlock block) {
         Extent extent = block.getExtent();
         int gx = block.x();
         int gy = block.y();
         int gz = block.z();
         int dx = gx - this.centerX;
         int dy = gy - this.centerY;
         int dz = gz - this.centerZ;
         if (dx * dx + dy * dy + dz * dz < this.brushSizeSquared && !BlendBall.this.maskFails(extent, block)) {
            int maxY = extent.getMaxY();
            int minY = extent.getMinY();
            int highest = 1;
            int currentBlockFrequency = 1;
            BlockState currentState = block.getBlock();
            BlockState highestState = currentState;
            int currentStateID = currentState.getInternalBlockTypeId();
            Arrays.fill(this.frequency, 0);
            int air = 0;
            int total = 26;
            boolean tie = false;

            for (int ox = -1; ox <= 1; ox++) {
               for (int oz = -1; oz <= 1; oz++) {
                  for (int oy = -1; oy <= 1; oy++) {
                     if (ox != 0 || oy != 0 || oz != 0) {
                        if (oy + gy >= minY && oy + gy <= maxY) {
                           boolean masked = BlendBall.this.maskFails(extent, this.mutable.setComponents(gx + ox, gy + oy, gz + oz));
                           BlockState state = masked ? BlendBall.AIR : block.getBlock(gx + ox, gy + oy, gz + oz);
                           if (state.getBlockType().getMaterial().isAir()) {
                              air++;
                           }

                           int internalID = state.getInternalBlockTypeId();
                           int count = this.frequency[internalID];
                           if (internalID == currentStateID) {
                              currentBlockFrequency++;
                           }

                           if (++count - highest >= BlendBall.this.minFreqDiff) {
                              highest = count;
                              highestState = state;
                              tie = false;
                           } else if (count == highest) {
                              tie = true;
                           }

                           this.frequency[internalID] = count;
                        } else {
                           total--;
                        }
                     }
                  }
               }
            }

            if (BlendBall.this.onlyAir) {
               if (air * 2 - total >= BlendBall.this.minFreqDiff) {
                  if (!currentState.isAir()) {
                     block.setBlock(BlendBall.AIR);
                  }
               } else if (currentState.isAir() && total - 2 * air >= BlendBall.this.minFreqDiff) {
                  block.setBlock(highestState);
               }
            } else {
               if (highest - currentBlockFrequency >= BlendBall.this.minFreqDiff && !tie && currentState != highestState) {
                  block.setBlock(highestState);
               }
            }
         }
      }

      @Override
      public Filter fork() {
         return BlendBall.this.new BlendBallFilter(this.brushSizeSquared, this.centerX, this.centerY, this.centerZ);
      }
   }
}
