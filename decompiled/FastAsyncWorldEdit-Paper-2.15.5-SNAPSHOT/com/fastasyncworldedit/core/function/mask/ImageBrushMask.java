package com.fastasyncworldedit.core.function.mask;

import com.fastasyncworldedit.core.command.tool.brush.ImageBrush;
import com.fastasyncworldedit.core.math.MutableVector3;
import com.fastasyncworldedit.core.util.TextureUtil;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.extent.Extent;
import com.sk89q.worldedit.function.mask.AbstractExtentMask;
import com.sk89q.worldedit.function.mask.Mask;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.math.Vector3;
import com.sk89q.worldedit.math.transform.Transform;
import com.sk89q.worldedit.world.block.BlockType;

public class ImageBrushMask extends AbstractExtentMask {
   private final MutableVector3 mutable = new MutableVector3();
   private final Mask solid;
   private final BlockVector3 center;
   private final Transform transform;
   private final double scale;
   private final double centerImageX;
   private final double centerImageZ;
   private final int width;
   private final int height;
   private final ImageBrush.ColorFunction colorFunction;
   private final EditSession session;
   private final TextureUtil texture;

   public ImageBrushMask(
      Mask solid,
      BlockVector3 center,
      Transform transform,
      double scale,
      double centerImageX,
      double centerImageZ,
      int width,
      int height,
      ImageBrush.ColorFunction colorFunction,
      EditSession session,
      TextureUtil texture
   ) {
      super(session);
      this.solid = solid;
      this.center = center;
      this.transform = transform;
      this.scale = scale;
      this.centerImageX = centerImageX;
      this.centerImageZ = centerImageZ;
      this.width = width;
      this.height = height;
      this.colorFunction = colorFunction;
      this.session = session;
      this.texture = texture;
   }

   @Override
   public boolean test(Extent extent, BlockVector3 vector) {
      return this.test(vector);
   }

   @Override
   public boolean test(BlockVector3 vector) {
      if (this.solid.test(vector)) {
         int dx = vector.x() - this.center.x();
         int dy = vector.y() - this.center.y();
         int dz = vector.z() - this.center.z();
         Vector3 pos1 = this.transform.apply(this.mutable.setComponents(dx - 0.5, dy - 0.5, dz - 0.5));
         int x1 = (int)(pos1.x() * this.scale + this.centerImageX);
         int z1 = (int)(pos1.z() * this.scale + this.centerImageZ);
         Vector3 pos2 = this.transform.apply(this.mutable.setComponents(dx + 0.5, dy + 0.5, dz + 0.5));
         int x2 = (int)(pos2.x() * this.scale + this.centerImageX);
         int z2 = (int)(pos2.z() * this.scale + this.centerImageZ);
         if (x2 < x1) {
            int tmp = x1;
            x1 = x2;
            x2 = tmp;
         }

         if (z2 < z1) {
            int tmp = z1;
            z1 = z2;
            z2 = tmp;
         }

         if (x1 < this.width && x2 >= 0 && z1 < this.height && z2 >= 0) {
            int color = this.colorFunction.call(x1, z1, x2, z2, this.session, vector);
            if (color != 0) {
               BlockType block = this.texture.getNearestBlock(color);
               if (block != null) {
                  this.session.setBlock(vector, block.getDefaultState());
               }
            }

            return true;
         } else {
            return false;
         }
      } else {
         return false;
      }
   }

   @Override
   public Mask copy() {
      return new ImageBrushMask(
         this.solid.copy(),
         this.center.toImmutable(),
         this.transform,
         this.scale,
         this.centerImageX,
         this.centerImageZ,
         this.width,
         this.height,
         this.colorFunction,
         this.session,
         this.texture
      );
   }
}
