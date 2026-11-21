package org.evenmorefish.fish.libs.daisylib.blockstate;

import org.bukkit.block.Block;
import org.bukkit.block.BrushableBlock;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public class CustomBrushable implements CustomBlockState<BrushableBlock> {
   private final BrushableBlock state;

   private CustomBrushable(@NonNull BrushableBlock state) {
      this.state = state;
   }

   @NonNull
   public static CustomBrushable customBrushable(@NonNull BrushableBlock state) {
      return new CustomBrushable(state);
   }

   @Nullable
   public static CustomBrushable customBrushable(@NonNull Block block) {
      return block.getState() instanceof BrushableBlock state ? new CustomBrushable(state) : null;
   }

   @NonNull
   public BrushableBlock getState() {
      return this.state;
   }

   @NonNull
   @Override
   public Block getBlock() {
      return this.state.getBlock();
   }
}
