package ch.njol.skript.bukkitutil.block;

import java.util.Map;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.FallingBlock;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

public interface BlockCompat {
   BlockCompat INSTANCE = new NewBlockCompat();
   BlockSetter SETTER = INSTANCE.getSetter();

   @Deprecated(
      since = "2.8.4",
      forRemoval = true
   )
   @Nullable
   BlockValues getBlockValues(BlockState var1);

   @Nullable
   default BlockValues getBlockValues(Block block) {
      return this.getBlockValues(block.getBlockData());
   }

   @Nullable
   BlockValues getBlockValues(Material var1);

   @Nullable
   BlockValues getBlockValues(BlockData var1);

   @Nullable
   BlockValues getBlockValues(ItemStack var1);

   @Deprecated(
      since = "2.8.4",
      forRemoval = true
   )
   BlockState fallingBlockToState(FallingBlock var1);

   @Nullable
   default BlockValues getBlockValues(FallingBlock entity) {
      return this.getBlockValues(entity.getBlockData());
   }

   @Nullable
   BlockValues createBlockValues(Material var1, Map<String, String> var2, @Nullable ItemStack var3, int var4);

   @Nullable
   default BlockValues createBlockValues(Material type, Map<String, String> states) {
      return this.createBlockValues(type, states, null, 0);
   }

   BlockSetter getSetter();

   boolean isEmpty(Material var1);

   boolean isLiquid(Material var1);
}
