package com.fastasyncworldedit.core.math.heightmap;

import com.fastasyncworldedit.core.math.MutableVector3;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.math.transform.AffineTransform;

public class RotatableHeightMap extends AbstractDelegateHeightMap {
   private AffineTransform transform;
   private final MutableVector3 mutable = new MutableVector3();

   public RotatableHeightMap(HeightMap parent) {
      super(parent);
      this.transform = new AffineTransform();
   }

   public void rotate(double angle) {
      this.transform = this.transform.rotateY(angle);
   }

   @Override
   public double getHeight(int x, int z) {
      this.mutable.mutX(x);
      this.mutable.mutZ(z);
      BlockVector3 pos = this.transform.apply(this.mutable.setComponents(x, 0, z)).toBlockPoint();
      return super.getHeight(pos.x(), pos.z());
   }
}
