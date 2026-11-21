package ch.njol.skript.util;

import org.bukkit.block.BlockState;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

public class BlockInventoryHolder extends BlockStateBlock implements InventoryHolder {
   public BlockInventoryHolder(BlockState state) {
      super(state, false);
   }

   public Inventory getInventory() {
      return ((InventoryHolder)this.state).getInventory();
   }
}
