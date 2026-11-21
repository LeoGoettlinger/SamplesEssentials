package com.fastasyncworldedit.core.function.mask;

import com.fastasyncworldedit.core.util.StringMan;
import com.sk89q.worldedit.extent.Extent;
import com.sk89q.worldedit.function.mask.AbstractExtentMask;
import com.sk89q.worldedit.function.mask.BlockMask;
import com.sk89q.worldedit.function.mask.Mask;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.world.block.BlockState;
import com.sk89q.worldedit.world.block.BlockType;
import com.sk89q.worldedit.world.block.BlockTypesCache;
import java.util.ArrayList;
import java.util.List;

public abstract class ABlockMask extends AbstractExtentMask {
   public ABlockMask(Extent extent) {
      super(extent);
   }

   @Override
   public boolean test(Extent extent, BlockVector3 vector) {
      return this.test(vector.getBlock(extent));
   }

   @Override
   public boolean test(BlockVector3 vector) {
      return this.test(vector.getBlock(this.getExtent()));
   }

   public abstract boolean test(BlockState state);

   @Override
   public String toString() {
      List<String> strings = new ArrayList<>();

      for (BlockType type : BlockTypesCache.values) {
         if (type != null) {
            List<BlockState> all = type.getAllStates();
            boolean hasAll = all.stream().map(this::test).reduce(true, (a, b) -> a && b);
            if (hasAll) {
               strings.add(type.id());
            } else {
               for (BlockState state : all) {
                  if (this.test(state)) {
                     strings.add(state.getAsString());
                  }
               }
            }
         }
      }

      return StringMan.join(strings, ",");
   }

   @Override
   public Mask tryCombine(Mask mask) {
      if (mask instanceof ABlockMask other) {
         BlockMask newMask = new BlockMask(this.getExtent());

         for (BlockState state : BlockTypesCache.states) {
            if (state != null && this.test(state) && other.test(state)) {
               newMask.add(state);
            }
         }

         Mask tmp = newMask.tryOptimize();
         if (tmp == null) {
            tmp = newMask;
         }

         return tmp;
      } else {
         return null;
      }
   }

   @Override
   public Mask tryOr(Mask mask) {
      if (!(mask instanceof ABlockMask other)) {
         return null;
      } else {
         BlockMask newMask = new BlockMask(this.getExtent());

         for (BlockState state : BlockTypesCache.states) {
            if (state != null && (this.test(state) || other.test(state))) {
               newMask.add(state);
            }
         }

         Mask tmp = newMask.tryOptimize();
         if (tmp == null) {
            tmp = newMask;
         }

         return tmp;
      }
   }
}
