package com.fastasyncworldedit.core.function.mask;

import com.fastasyncworldedit.core.math.LocalBlockVectorSet;
import com.fastasyncworldedit.core.util.collection.BlockVector3Set;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.extent.Extent;
import com.sk89q.worldedit.function.mask.AbstractExtentMask;
import com.sk89q.worldedit.function.mask.Mask;
import com.sk89q.worldedit.math.BlockVector3;
import java.util.concurrent.ThreadLocalRandom;

public class SplatterBrushMask extends AbstractExtentMask {
   private final BlockVector3 position;
   private final int size2;
   private final Mask surface;
   private final BlockVector3Set placed;

   @Deprecated(
      forRemoval = true,
      since = "2.13.0"
   )
   public SplatterBrushMask(EditSession editSession, BlockVector3 position, int size2, Mask surface, LocalBlockVectorSet placed) {
      this(editSession, position, size2, surface, LocalBlockVectorSet.wrap(placed));
   }

   public SplatterBrushMask(EditSession editSession, BlockVector3 position, int size2, Mask surface, BlockVector3Set placed) {
      super(editSession);
      this.position = position;
      this.size2 = size2;
      this.surface = surface;
      this.placed = placed;
   }

   @Override
   public boolean test(Extent extent, BlockVector3 vector) {
      return this.test(vector);
   }

   @Override
   public boolean test(BlockVector3 vector) {
      double dist = vector.distanceSq(this.position);
      synchronized (this.placed) {
         if (dist < this.size2 && !this.placed.contains(vector) && ThreadLocalRandom.current().nextInt(5) < 2 && this.surface.test(vector)) {
            this.placed.add(vector.toImmutable());
            return true;
         } else {
            return false;
         }
      }
   }

   @Override
   public Mask copy() {
      return this;
   }
}
