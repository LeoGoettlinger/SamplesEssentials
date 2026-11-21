package org.evenmorefish.fish.libs.daisylib.blockstate;

import java.util.function.Consumer;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataHolder;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.NonNull;

@ApiStatus.Internal
public interface CustomBlockState<T extends BlockState> {
   @NonNull
   T getState();

   @NonNull
   Block getBlock();

   default void saveToBlock() {
      this.saveToBlock(this.getBlock());
   }

   default void saveToBlock(@NonNull Block block) {
      block.setBlockData(this.getState().getBlockData());
   }

   default void editPersistentDataContainer(@NonNull Consumer<PersistentDataContainer> consumer) {
      if (this.getState() instanceof PersistentDataHolder holder) {
         consumer.accept(holder.getPersistentDataContainer());
      }
   }
}
