package com.fastasyncworldedit.core.extent.processor.heightmap;

import com.fastasyncworldedit.core.registry.state.PropertyKey;
import com.sk89q.worldedit.function.mask.SolidBlockMask;
import com.sk89q.worldedit.registry.state.Property;
import com.sk89q.worldedit.world.block.BlockCategories;
import com.sk89q.worldedit.world.block.BlockState;

public enum HeightMapType {
   MOTION_BLOCKING {
      @Override
      public boolean includes(BlockState state) {
         return HeightMapType.isMovementBlocker(state) || HeightMapType.hasFluid(state);
      }
   },
   MOTION_BLOCKING_NO_LEAVES {
      @Override
      public boolean includes(BlockState state) {
         return (HeightMapType.isMovementBlocker(state) || HeightMapType.hasFluid(state)) && !HeightMapType.isLeaf(state);
      }
   },
   OCEAN_FLOOR {
      @Override
      public boolean includes(BlockState state) {
         return HeightMapType.isMovementBlocker(state);
      }
   },
   WORLD_SURFACE {
      @Override
      public boolean includes(BlockState state) {
         return !state.isAir();
      }
   };

   private static boolean isMovementBlocker(BlockState state) {
      return SolidBlockMask.isSolid(state);
   }

   private static boolean isLeaf(BlockState state) {
      return BlockCategories.LEAVES.contains(state);
   }

   private static boolean hasFluid(BlockState state) {
      if (state.getMaterial().isLiquid()) {
         return true;
      } else if (!state.getBlockType().hasProperty(PropertyKey.WATERLOGGED)) {
         return false;
      } else {
         Property<Boolean> waterlogged = state.getBlockType().getProperty(PropertyKey.WATERLOGGED);
         return waterlogged == null ? false : state.getState(waterlogged);
      }
   }

   public abstract boolean includes(BlockState state);

   static {
      BlockCategories.LEAVES.getAll();
   }
}
