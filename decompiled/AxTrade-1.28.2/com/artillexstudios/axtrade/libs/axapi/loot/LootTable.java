package com.artillexstudios.axtrade.libs.axapi.loot;

import java.util.List;
import org.bukkit.inventory.ItemStack;

public interface LootTable {
   List<ItemStack> randomItems(LootParams params);
}
