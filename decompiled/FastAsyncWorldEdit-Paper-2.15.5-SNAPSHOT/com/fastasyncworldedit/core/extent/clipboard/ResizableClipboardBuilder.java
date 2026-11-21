package com.fastasyncworldedit.core.extent.clipboard;

import com.fastasyncworldedit.core.history.MemoryOptimizedHistory;
import com.fastasyncworldedit.core.history.change.MutableBlockChange;
import com.fastasyncworldedit.core.history.change.MutableTileChange;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.extent.clipboard.BlockArrayClipboard;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.history.change.Change;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.world.World;
import com.sk89q.worldedit.world.block.BlockState;
import java.util.Iterator;

public class ResizableClipboardBuilder extends MemoryOptimizedHistory {
   private int minX = Integer.MAX_VALUE;
   private int minY = Integer.MAX_VALUE;
   private int minZ = Integer.MAX_VALUE;
   private int maxX = Integer.MIN_VALUE;
   private int maxY = Integer.MIN_VALUE;
   private int maxZ = Integer.MIN_VALUE;

   public ResizableClipboardBuilder(World world) {
      super(world);
   }

   @Override
   public void add(int x, int y, int z, int combinedFrom, int combinedTo) {
      super.add(x, y, z, combinedFrom, combinedTo);
      if (x < this.minX) {
         if (this.maxX == Integer.MIN_VALUE) {
            this.maxX = x;
         }

         this.minX = x;
      } else if (x > this.maxX) {
         this.maxX = x;
      }

      if (y < this.minY) {
         if (this.maxY == Integer.MIN_VALUE) {
            this.maxY = y;
         }

         this.minY = y;
      } else if (y > this.maxY) {
         this.maxY = y;
      }

      if (z < this.minZ) {
         if (this.maxZ == Integer.MIN_VALUE) {
            this.maxZ = z;
         }

         this.minZ = z;
      } else if (z > this.maxZ) {
         this.maxZ = z;
      }
   }

   public Clipboard build() {
      if (this.longSize() == 0L) {
         return EmptyClipboard.getInstance();
      } else {
         BlockVector3 pos1 = BlockVector3.at(this.minX, this.minY, this.minZ);
         BlockVector3 pos2 = BlockVector3.at(this.maxX, this.maxY, this.maxZ);
         CuboidRegion region = new CuboidRegion(pos1, pos2);
         BlockArrayClipboard clipboard = new BlockArrayClipboard(region);
         Iterator<Change> iterator = this.getIterator(true);

         try {
            while (iterator.hasNext()) {
               Change change = iterator.next();
               if (change instanceof MutableBlockChange blockChange) {
                  BlockState block = BlockState.getFromOrdinal(blockChange.ordinal);
                  clipboard.setBlock(blockChange.x, blockChange.y, blockChange.z, block);
               } else if (change instanceof MutableTileChange tileChange) {
                  int x = tileChange.tag.getInt("x");
                  int y = tileChange.tag.getInt("y");
                  int z = tileChange.tag.getInt("z");
                  clipboard.setTile(x, y, z, tileChange.tag);
               }
            }
         } catch (WorldEditException var12) {
            var12.printStackTrace();
         }

         return clipboard;
      }
   }
}
