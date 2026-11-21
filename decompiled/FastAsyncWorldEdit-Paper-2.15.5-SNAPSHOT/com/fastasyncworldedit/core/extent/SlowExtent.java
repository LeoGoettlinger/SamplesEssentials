package com.fastasyncworldedit.core.extent;

import com.fastasyncworldedit.core.Fawe;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.extent.AbstractDelegateExtent;
import com.sk89q.worldedit.extent.Extent;
import com.sk89q.worldedit.world.block.BlockStateHolder;

public class SlowExtent extends AbstractDelegateExtent {
   private final long THRESHOLD = 50000000L;
   private final long nanos;
   private long increment;

   public SlowExtent(Extent extent, long nanos) {
      super(extent);
      this.nanos = nanos;
   }

   @Override
   public <T extends BlockStateHolder<T>> boolean setBlock(int x, int y, int z, T block) throws WorldEditException {
      this.delay();
      return super.setBlock(x, y, z, block);
   }

   public void delay() {
      this.increment = this.increment + this.nanos;
      if (this.increment >= 50000000L) {
         long wait = this.increment / 1000000L;
         if (!Fawe.isMainThread()) {
            try {
               Thread.sleep(wait);
            } catch (InterruptedException var4) {
               var4.printStackTrace();
            }
         }

         this.increment -= wait * 1000000L;
      }
   }
}
