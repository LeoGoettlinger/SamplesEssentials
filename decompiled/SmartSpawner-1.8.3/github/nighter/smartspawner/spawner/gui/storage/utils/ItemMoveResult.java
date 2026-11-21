package github.nighter.smartspawner.spawner.gui.storage.utils;

import java.util.List;
import org.bukkit.inventory.ItemStack;

public record ItemMoveResult(int amountMoved, List<ItemStack> movedItems) {
}
