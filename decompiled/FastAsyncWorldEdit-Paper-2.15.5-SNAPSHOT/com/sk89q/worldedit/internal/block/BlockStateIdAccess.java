package com.sk89q.worldedit.internal.block;

import com.sk89q.worldedit.world.block.BlockState;
import javax.annotation.Nullable;

public final class BlockStateIdAccess {
   private static final int INVALID_ID = -1;

   public static int invalidId() {
      return -1;
   }

   public static boolean isValidInternalId(int internalId) {
      return internalId != -1;
   }

   public static int getBlockStateId(BlockState holder) {
      return holder.getInternalId();
   }

   @Nullable
   public static BlockState getBlockStateById(int id) {
      return BlockState.getFromInternalId(id);
   }

   private BlockStateIdAccess() {
   }
}
