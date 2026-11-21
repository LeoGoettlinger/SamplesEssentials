package com.ranull.graves.compatibility;

import com.ranull.graves.Graves;
import com.ranull.graves.data.BlockData;
import com.ranull.graves.type.Grave;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public interface Compatibility {
   BlockData setBlockData(Location var1, Material var2, Grave var3, Graves var4);

   boolean canBuild(Player var1, Location var2, Graves var3);

   boolean hasTitleData(Block var1);

   ItemStack getSkullItemStack(Grave var1, Graves var2);

   String getSkullTexture(ItemStack var1);
}
