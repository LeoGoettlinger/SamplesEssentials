package com.fastasyncworldedit.core.function.mask;

import com.fastasyncworldedit.core.math.MutableBlockVector3;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.extent.Extent;
import com.sk89q.worldedit.function.mask.AbstractExtentMask;
import com.sk89q.worldedit.function.mask.Mask;
import com.sk89q.worldedit.function.visitor.BreadthFirstSearch;
import com.sk89q.worldedit.function.visitor.RecursiveVisitor;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.world.block.BlockState;

public class LayerBrushMask extends AbstractExtentMask {
   private final EditSession editSession;
   private final RecursiveVisitor visitor;
   private final BlockState[] layers;
   private final MutableBlockVector3 mutable = new MutableBlockVector3();
   private final Mask adjacent;

   public LayerBrushMask(EditSession editSession, RecursiveVisitor visitor, BlockState[] layers, Mask adjacent) {
      super(editSession);
      this.editSession = editSession;
      this.visitor = visitor;
      this.layers = layers;
      this.adjacent = adjacent;
   }

   @Override
   public boolean test(Extent extent, BlockVector3 vector) {
      return this.test(vector);
   }

   @Override
   public boolean test(BlockVector3 pos) {
      int depth = (this.visitor.getDepth() + 1) % this.layers.length;
      if (depth > 1) {
         boolean found = false;
         BlockState previous = this.layers[depth - 1];
         BlockState previous2 = this.layers[depth - 2];
         BlockVector3[] var6 = BreadthFirstSearch.DEFAULT_DIRECTIONS;
         int var7 = var6.length;
         int var8 = 0;

         while (true) {
            if (var8 < var7) {
               BlockVector3 dir = var6[var8];
               this.mutable.setComponents(pos.x() + dir.x(), pos.y() + dir.y(), pos.z() + dir.z());
               if (!this.visitor.isVisited(this.mutable) || this.editSession.getBlock(this.mutable.x(), this.mutable.y(), this.mutable.z()) != previous) {
                  var8++;
                  continue;
               }

               this.mutable.setComponents(pos.x() + dir.x() * 2, pos.y() + dir.y() * 2, pos.z() + dir.z() * 2);
               if (!this.visitor.isVisited(this.mutable) || this.editSession.getBlock(this.mutable.x(), this.mutable.y(), this.mutable.z()) != previous2) {
                  return false;
               }

               found = true;
            }

            if (!found) {
               return false;
            }
            break;
         }
      }

      return !this.adjacent.test(pos);
   }

   @Override
   public Mask copy() {
      return new LayerBrushMask(this.editSession, this.visitor, (BlockState[])this.layers.clone(), this.adjacent.copy());
   }
}
