package com.sk89q.worldedit.function.mask;

import com.sk89q.worldedit.extent.Extent;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.world.block.BlockState;
import com.sk89q.worldedit.world.block.BlockTypesCache;

public class SolidBlockMask extends AbstractExtentMask {
   private static final boolean[] SOLID = initialize();

   private static boolean[] initialize() {
      boolean[] solid = new boolean[BlockTypesCache.states.length];

      for (int i = 0; i < solid.length; i++) {
         solid[i] = BlockTypesCache.states[i].getBlockType().getMaterial().isMovementBlocker();
      }

      return solid;
   }

   public SolidBlockMask(Extent extent) {
      super(extent);
   }

   @Override
   public boolean test(final Extent extent, final BlockVector3 position) {
      int ordinal = position.getOrdinal(extent);
      return SOLID[ordinal];
   }

   @Override
   public boolean test(final BlockVector3 vector) {
      return this.test(this.getExtent(), vector);
   }

   public static boolean isSolid(BlockState blockState) {
      return SOLID[blockState.getOrdinal()];
   }

   @Override
   public Mask copy() {
      return new SolidBlockMask(this.getExtent());
   }
}
