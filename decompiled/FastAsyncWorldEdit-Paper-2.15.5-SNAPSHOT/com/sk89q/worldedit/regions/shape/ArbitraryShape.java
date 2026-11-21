package com.sk89q.worldedit.regions.shape;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.function.pattern.Pattern;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.world.block.BaseBlock;

public abstract class ArbitraryShape {
   protected final Region extent;
   private final int cacheOffsetX;
   private final int cacheOffsetY;
   private final int cacheOffsetZ;
   private final int cacheSizeX;
   private final int cacheSizeY;
   private final int cacheSizeZ;
   private final byte[] cache;

   public ArbitraryShape(Region extent) {
      this.extent = extent;
      BlockVector3 min = extent.getMinimumPoint();
      BlockVector3 max = extent.getMaximumPoint();
      this.cacheOffsetX = min.x() - 1;
      this.cacheOffsetY = min.y() - 1;
      this.cacheOffsetZ = min.z() - 1;
      this.cacheSizeX = max.x() - this.cacheOffsetX + 2;
      this.cacheSizeY = max.y() - this.cacheOffsetY + 2;
      this.cacheSizeZ = max.z() - this.cacheOffsetZ + 2;
      this.cache = new byte[this.cacheSizeX * this.cacheSizeY * this.cacheSizeZ];
   }

   protected Region getExtent() {
      return this.extent;
   }

   protected abstract BaseBlock getMaterial(int x, int y, int z, BaseBlock defaultMaterial);

   public int generate(EditSession editSession, Pattern pattern, boolean hollow) throws MaxChangedBlocksException {
      int affected = 0;

      for (BlockVector3 position : this.getExtent()) {
         int x = position.x();
         int y = position.y();
         int z = position.z();
         if (!hollow) {
            BaseBlock material = this.getMaterial(x, y, z, pattern.applyBlock(position));
            if (material != null && editSession.setBlock(position, material)) {
               affected++;
            }
         } else {
            BaseBlock material = this.getMaterial(x, y, z, pattern.applyBlock(position));
            if (material == null) {
               int index = y - this.cacheOffsetY + (z - this.cacheOffsetZ) * this.cacheSizeY + (x - this.cacheOffsetX) * this.cacheSizeY * this.cacheSizeZ;
               this.cache[index] = -1;
            } else {
               boolean draw = false;
               if (!this.isInsideCached(x + 1, y, z, pattern)) {
                  draw = true;
               } else if (!this.isInsideCached(x - 1, y, z, pattern)) {
                  draw = true;
               } else if (!this.isInsideCached(x, y, z + 1, pattern)) {
                  draw = true;
               } else if (!this.isInsideCached(x, y, z - 1, pattern)) {
                  draw = true;
               } else if (!this.isInsideCached(x, y + 1, z, pattern)) {
                  draw = true;
               } else if (!this.isInsideCached(x, y - 1, z, pattern)) {
                  draw = true;
               }

               if (draw && editSession.setBlock(position, material)) {
                  affected++;
               }
            }
         }
      }

      return affected;
   }

   private boolean isInsideCached(int x, int y, int z, Pattern pattern) {
      int index = y - this.cacheOffsetY + (z - this.cacheOffsetZ) * this.cacheSizeY + (x - this.cacheOffsetX) * this.cacheSizeY * this.cacheSizeZ;
      switch (this.cache[index]) {
         case -1:
            return false;
         case 0:
            BaseBlock mat = this.getMaterial(x, y, z, pattern.applyBlock(BlockVector3.at(x, y, z)));
            if (mat == null) {
               this.cache[index] = -1;
               return false;
            }

            this.cache[index] = 1;
            return true;
         default:
            return true;
      }
   }
}
