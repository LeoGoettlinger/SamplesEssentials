package com.fastasyncworldedit.core.function.mask;

import com.fastasyncworldedit.core.math.MutableVector3;
import com.fastasyncworldedit.core.math.heightmap.HeightMap;
import com.fastasyncworldedit.core.util.MathMan;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.extent.Extent;
import com.sk89q.worldedit.function.mask.AbstractExtentMask;
import com.sk89q.worldedit.function.mask.Mask;
import com.sk89q.worldedit.function.pattern.Pattern;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.math.Vector3;
import com.sk89q.worldedit.math.transform.Transform;
import java.util.concurrent.ThreadLocalRandom;

public class StencilBrushMask extends AbstractExtentMask {
   private final MutableVector3 mutable = new MutableVector3();
   private final EditSession editSession;
   private final Mask solid;
   private final BlockVector3 center;
   private final Transform transform;
   private final int size2;
   private final HeightMap map;
   private final double scale;
   private final int add;
   private final int cutoff;
   private final int maxY;
   private final Pattern pattern;

   public StencilBrushMask(
      EditSession editSession,
      Mask solid,
      BlockVector3 center,
      Transform transform,
      int size2,
      HeightMap map,
      double scale,
      int add,
      int cutoff,
      int maxY,
      Pattern pattern
   ) {
      super(editSession);
      this.editSession = editSession;
      this.solid = solid;
      this.center = center;
      this.transform = transform;
      this.size2 = size2;
      this.map = map;
      this.scale = scale;
      this.add = add;
      this.cutoff = cutoff;
      this.maxY = maxY;
      this.pattern = pattern;
   }

   @Override
   public boolean test(Extent extent, BlockVector3 vector) {
      return this.test(vector);
   }

   @Override
   public boolean test(BlockVector3 vector) {
      if (!this.solid.test(vector)) {
         return false;
      } else {
         int dx = vector.x() - this.center.x();
         int dy = vector.y() - this.center.y();
         int dz = vector.z() - this.center.z();
         Vector3 srcPos = this.transform.apply(this.mutable.setComponents(dx, dy, dz));
         dx = MathMan.roundInt(srcPos.x());
         dz = MathMan.roundInt(srcPos.z());
         int distance = dx * dx + dz * dz;
         if (distance <= this.size2 && Math.abs(dx) <= 256 && Math.abs(dz) <= 256) {
            double raise = this.map.getHeight(dx, dz);
            int val = (int)Math.ceil(raise * this.scale) + this.add;
            if (val < this.cutoff) {
               return true;
            } else {
               if (val >= 255 || ThreadLocalRandom.current().nextInt(this.maxY) < val) {
                  this.editSession.setBlock(vector.x(), vector.y(), vector.z(), this.pattern);
               }

               return true;
            }
         } else {
            return false;
         }
      }
   }

   @Override
   public Mask copy() {
      return new StencilBrushMask(
         this.editSession,
         this.solid.copy(),
         this.center.toImmutable(),
         this.transform,
         this.size2,
         this.map,
         this.scale,
         this.add,
         this.cutoff,
         this.maxY,
         this.pattern
      );
   }
}
