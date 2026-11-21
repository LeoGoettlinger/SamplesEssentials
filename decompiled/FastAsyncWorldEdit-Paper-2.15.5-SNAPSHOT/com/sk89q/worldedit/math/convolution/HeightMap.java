package com.sk89q.worldedit.math.convolution;

import com.fastasyncworldedit.core.registry.state.PropertyGroup;
import com.google.common.base.Preconditions;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.function.mask.Mask;
import com.sk89q.worldedit.math.BlockVector2;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.regions.Regions;
import com.sk89q.worldedit.world.block.BlockState;
import com.sk89q.worldedit.world.block.BlockStateHolder;
import com.sk89q.worldedit.world.block.BlockTypes;
import java.util.Iterator;
import javax.annotation.Nullable;

public class HeightMap {
   private final boolean layers;
   private final int[] data;
   private final int width;
   private final int height;
   private final Region region;
   private final EditSession session;
   private final int minSessionY;
   private final int maxSessionY;

   public HeightMap(EditSession session, Region region) {
      this(session, region, (Mask)null, false);
   }

   public HeightMap(EditSession session, Region region, @Nullable Mask mask) {
      this(session, region, mask, false);
   }

   public HeightMap(EditSession session, Region region, @Nullable Mask mask, boolean layers) {
      Preconditions.checkNotNull(session);
      Preconditions.checkNotNull(region);
      this.session = session;
      this.region = region;
      this.width = region.getWidth();
      this.height = region.getLength();
      this.layers = layers;
      this.minSessionY = session.getMinY();
      this.maxSessionY = session.getMaxY();
      int minX = region.getMinimumPoint().x();
      int minY = region.getMinimumPoint().y();
      int minZ = region.getMinimumPoint().z();
      int maxY = region.getMaximumPoint().y();
      this.data = new int[this.width * this.height];
      if (layers) {
         BlockVector3 min = region.getMinimumPoint();
         int bx = min.x();
         int bz = min.z();
         Iterator<BlockVector2> flat = Regions.asFlatRegion(region).asFlatRegion().iterator();
         int layer = session.getMinY();

         while (flat.hasNext()) {
            BlockVector2 pos = flat.next();
            int x = pos.x();
            int z = pos.z();
            layer = session.getNearestSurfaceLayer(x, z, layer + 7 >> 3, session.getMinY(), maxY);
            this.data[(z - bz) * this.width + (x - bx)] = layer;
         }
      } else {
         int index = 0;

         for (int z = 0; z < this.height; z++) {
            for (int x = 0; x < this.width; index++) {
               if (mask == null) {
                  this.data[index] = session.getHighestTerrainBlock(x + minX, z + minZ, minY, maxY);
               } else {
                  this.data[index] = session.getHighestTerrainBlock(x + minX, z + minZ, minY, maxY, mask);
               }

               x++;
            }
         }
      }
   }

   public HeightMap(EditSession session, Region region, int[] data, boolean layers) {
      this.session = session;
      this.region = region;
      this.width = region.getWidth();
      this.height = region.getLength();
      this.data = data;
      this.layers = layers;
      this.minSessionY = session.getMinY();
      this.maxSessionY = session.getMaxY();
   }

   public int applyFilter(HeightMapFilter filter, int iterations) throws MaxChangedBlocksException {
      Preconditions.checkNotNull(filter);
      int[] newData = new int[this.data.length];
      System.arraycopy(this.data, 0, newData, 0, this.data.length);

      for (int i = 0; i < iterations; i++) {
         newData = filter.filter(newData, this.width, this.height, 0.5F);
      }

      return this.layers ? this.applyLayers(newData) : this.apply(newData);
   }

   public int applyLayers(int[] data) {
      Preconditions.checkNotNull(data);
      BlockVector3 min = this.region.getMinimumPoint();
      int originX = min.x();
      int originZ = min.z();
      int maxY = this.region.getMaximumPoint().y();
      BlockState fillerAir = BlockTypes.AIR.getDefaultState();
      int blocksChanged = 0;
      BlockStateHolder<BlockState> tmpBlock = BlockTypes.AIR.getDefaultState();
      int maxY4 = maxY << 4;
      int index = 0;

      for (int z = 0; z < this.height; z++) {
         int zr = z + originZ;

         for (int x = 0; x < this.width; x++) {
            int curHeight = this.data[index];
            int newHeight = Math.min(maxY4, data[index++]);
            int curBlock = curHeight >> 4;
            int newBlock = newHeight + 15 >> 4;
            int xr = x + originX;
            if (newHeight > curHeight) {
               BlockStateHolder<BlockState> existing = this.session.getBlock(xr, curBlock, zr);
               if (!existing.getBlockType().getMaterial().isLiquid()) {
                  int setY = newBlock - 1;

                  for (int getY = curBlock; setY >= curBlock; getY--) {
                     BlockStateHolder<BlockState> get = this.session.getBlock(xr, getY, zr);
                     if (get != BlockTypes.AIR.getDefaultState()) {
                        tmpBlock = get;
                     }

                     this.session.setBlock(xr, setY, zr, tmpBlock);
                     blocksChanged++;
                     setY--;
                  }

                  setY = newHeight & 15;
                  if (setY != 0) {
                     existing = PropertyGroup.LEVEL.set(existing, setY - 1);
                     this.session.setBlock(xr, newBlock, zr, existing);
                     blocksChanged++;
                  } else {
                     existing = PropertyGroup.LEVEL.set(existing, 15);
                     this.session.setBlock(xr, newBlock, zr, existing);
                     blocksChanged++;
                  }
               }
            } else if (curHeight > newHeight) {
               for (int y = newBlock + 1; y <= curHeight + 15 >> 4; y++) {
                  this.session.setBlock(xr, y, zr, fillerAir);
                  blocksChanged++;
               }

               int setData = newHeight & 15;
               BlockStateHolder<BlockState> existing = this.session.getBlock(xr, curBlock, zr);
               if (setData != 0) {
                  existing = PropertyGroup.LEVEL.set(existing, setData - 1);
                  this.session.setBlock(xr, newBlock, zr, existing);
               } else {
                  existing = PropertyGroup.LEVEL.set(existing, 15);
                  this.session.setBlock(xr, newBlock, zr, existing);
               }

               blocksChanged++;
            }
         }
      }

      return blocksChanged;
   }

   public int apply(int[] data) throws MaxChangedBlocksException {
      Preconditions.checkNotNull(data);
      BlockVector3 min = this.region.getMinimumPoint();
      int originX = min.x();
      int originY = min.y();
      int originZ = min.z();
      int maxY = this.region.getMaximumPoint().y();
      BlockState fillerAir = BlockTypes.AIR.getDefaultState();
      int blocksChanged = 0;
      BlockState tmpBlock = BlockTypes.AIR.getDefaultState();
      int index = 0;

      for (int z = 0; z < this.height; z++) {
         int zr = z + originZ;

         for (int x = 0; x < this.width; index++) {
            int curHeight = this.data[index];
            int newHeight = Math.min(maxY, data[index]);
            int xr = x + originX;
            if (newHeight > curHeight) {
               BlockState existing = this.session.getBlock(xr, curHeight, zr);
               if (existing.getBlockType().getMaterial().isMovementBlocker()) {
                  int y0 = newHeight - 1;
                  int setY = y0;

                  for (int getY = curHeight - 1; setY >= curHeight; getY--) {
                     BlockState get;
                     if (getY >= this.minSessionY && getY <= this.maxSessionY) {
                        get = this.session.getBlock(xr, getY, zr);
                     } else {
                        get = BlockTypes.AIR.getDefaultState();
                     }

                     if (get != BlockTypes.AIR.getDefaultState()) {
                        tmpBlock = get;
                     }

                     this.session.setBlock(xr, setY, zr, tmpBlock);
                     blocksChanged++;
                     setY--;
                  }

                  this.session.setBlock(xr, newHeight, zr, existing);
                  blocksChanged++;
               }
            } else if (curHeight > newHeight) {
               int setY = originY;

               for (int getY = newHeight; setY <= newHeight; getY++) {
                  BlockState getx;
                  if (getY >= this.minSessionY && getY <= this.maxSessionY) {
                     getx = this.session.getBlock(xr, getY, zr);
                  } else {
                     getx = BlockTypes.AIR.getDefaultState();
                  }

                  if (getx != BlockTypes.AIR.getDefaultState()) {
                     tmpBlock = getx;
                  }

                  this.session.setBlock(xr, setY, zr, tmpBlock);
                  blocksChanged++;
                  setY++;
               }

               this.session.setBlock(xr, newHeight, zr, this.session.getBlock(xr, curHeight, zr));
               blocksChanged++;

               for (int y = newHeight + 1; y <= curHeight; y++) {
                  this.session.setBlock(xr, y, zr, fillerAir);
                  blocksChanged++;
               }
            }

            x++;
         }
      }

      return blocksChanged;
   }
}
