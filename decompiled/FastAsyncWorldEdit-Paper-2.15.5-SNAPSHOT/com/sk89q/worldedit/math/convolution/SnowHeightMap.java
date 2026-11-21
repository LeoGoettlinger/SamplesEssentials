package com.sk89q.worldedit.math.convolution;

import com.google.common.base.Preconditions;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.function.mask.Mask;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.registry.state.Property;
import com.sk89q.worldedit.world.block.BlockState;
import com.sk89q.worldedit.world.block.BlockTypes;
import javax.annotation.Nullable;

public class SnowHeightMap {
   private static final Property<Integer> LAYERS = BlockTypes.SNOW.getProperty("layers");
   private final float[] data;
   private final int width;
   private final int height;
   private final Region region;
   private final EditSession session;

   public SnowHeightMap(EditSession session, Region region, @Nullable Mask mask) {
      Preconditions.checkNotNull(session);
      Preconditions.checkNotNull(region);
      this.session = session;
      this.region = region;
      this.width = region.getWidth();
      this.height = region.getLength();
      int minX = region.getMinimumPoint().x();
      int minY = region.getMinimumPoint().y();
      int minZ = region.getMinimumPoint().z();
      int maxY = region.getMaximumPoint().y();
      this.data = new float[this.width * this.height];

      for (int z = 0; z < this.height; z++) {
         for (int x = 0; x < this.width; x++) {
            int highestBlockY;
            if (mask != null) {
               highestBlockY = session.getHighestTerrainBlock(x + minX, z + minZ, minY, maxY, mask);
            } else {
               highestBlockY = session.getHighestTerrainBlock(x + minX, z + minZ, minY, maxY);
            }

            BlockState upper = session.getBlock(x + minX, highestBlockY + 1, z + minZ);
            if (upper.getBlockType() == BlockTypes.SNOW) {
               Integer amountLayers = upper.getState(LAYERS);
               this.data[z * this.width + x] = highestBlockY + 1 + (amountLayers.intValue() - 1.0F) / 8.0F;
            } else {
               BlockState block = session.getBlock(x + minX, highestBlockY, z + minZ);
               if (block.getBlockType().getMaterial().isAir()) {
                  this.data[z * this.width + x] = highestBlockY;
               } else {
                  this.data[z * this.width + x] = highestBlockY + 1;
               }
            }
         }
      }
   }

   public float[] applyFilter(HeightMapFilter filter, int iterations) {
      Preconditions.checkNotNull(filter);
      float[] newData = new float[this.data.length];
      System.arraycopy(this.data, 0, newData, 0, this.data.length);

      for (int i = 0; i < iterations; i++) {
         newData = filter.filter(newData, this.width, this.height, 0.0625F);
      }

      return newData;
   }

   public int applyChanges(float[] data, int layerBlocks) throws MaxChangedBlocksException {
      Preconditions.checkNotNull(data);
      BlockVector3 minY = this.region.getMinimumPoint();
      int originX = minY.x();
      int originY = minY.y();
      int originZ = minY.z();
      int maxY = this.region.getMaximumPoint().y();
      BlockState fillerAir = BlockTypes.AIR.getDefaultState();
      BlockState fillerSnow = BlockTypes.SNOW_BLOCK.getDefaultState();
      int blocksChanged = 0;

      for (int z = 0; z < this.height; z++) {
         for (int x = 0; x < this.width; x++) {
            int index = z * this.width + x;
            float curHeight = this.data[index];
            if (curHeight != originY) {
               float newHeight = Math.min((float)maxY, data[index]);
               int xr = x + originX;
               int zr = z + originZ;
               double scale = (double)(curHeight - originY) / (newHeight - originY);
               if (newHeight >= curHeight) {
                  BlockState existing = this.session.getBlock(xr, (int)Math.floor(curHeight), zr);
                  if (!existing.getBlockType().getMaterial().isLiquid()) {
                     this.setSnowLayer(xr, zr, newHeight);
                     blocksChanged++;

                     for (int y = (int)Math.floor(newHeight - 1.0F - originY); y >= 0; y--) {
                        if (y >= Math.floor(newHeight - 1.0F - originY - layerBlocks)) {
                           this.session.setBlock(xr, originY + y, zr, fillerSnow);
                        } else {
                           int copyFrom = (int)Math.floor(y * scale);
                           BlockState block = this.session.getBlock(xr, originY + copyFrom, zr);
                           this.session.setBlock(xr, originY + y, zr, block);
                        }

                        blocksChanged++;
                     }
                  }
               } else {
                  for (int y = 0; y < (int)Math.floor(newHeight - originY); y++) {
                     if (y >= (int)Math.floor(newHeight - originY - layerBlocks)) {
                        this.session.setBlock(xr, originY + y, zr, fillerSnow);
                     } else {
                        int copyFrom = (int)Math.floor(y * scale);
                        BlockState block = this.session.getBlock(xr, originY + copyFrom, zr);
                        this.session.setBlock(xr, originY + y, zr, block);
                     }

                     blocksChanged++;
                  }

                  this.setSnowLayer(xr, zr, newHeight);
                  blocksChanged++;

                  for (int y = (int)Math.floor(newHeight + 1.0F); y <= Math.floor(curHeight); y++) {
                     this.session.setBlock(xr, y, zr, fillerAir);
                     blocksChanged++;
                  }
               }
            }
         }
      }

      return blocksChanged;
   }

   private void setSnowLayer(int x, int z, float newHeight) throws MaxChangedBlocksException {
      int y = (int)Math.floor(newHeight);
      int numOfLayers = (int)((newHeight - y) * 8.0F) + 1;
      this.session.setBlock(x, y, z, BlockTypes.SNOW.getDefaultState().with(LAYERS, numOfLayers));
   }
}
