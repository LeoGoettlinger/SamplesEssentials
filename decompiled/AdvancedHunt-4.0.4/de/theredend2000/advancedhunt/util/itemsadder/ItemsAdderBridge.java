package de.theredend2000.advancedhunt.util.itemsadder;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.ItemStack;

public interface ItemsAdderBridge {
   boolean isItemsAdderBlock(Block var1);

   String getCustomBlockId(Block var1);

   boolean isCustomBlockItem(ItemStack var1);

   String getCustomBlockId(ItemStack var1);

   boolean isCustomFurniture(ItemStack var1);

   String getCustomFurnitureId(ItemStack var1);

   String getCustomFurnitureId(Entity var1);

   ItemStack getCustomItem(String var1);

   BlockData getCustomBlockData(String var1);

   boolean isCustomFurnitureId(String var1);

   boolean placeCustomBlock(String var1, Location var2);

   boolean removeCustomBlock(Location var1);

   boolean spawnCustomFurniture(String var1, Location var2);

   boolean removeCustomFurniture(Location var1);
}
