package ch.njol.skript.bukkitutil.block;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

public interface BlockSetter {
   int ROTATE = 1;
   int ROTATE_FORCE = 2;
   int ROTATE_FIX_TYPE = 4;
   int MULTIPART = 8;
   int APPLY_PHYSICS = 16;

   void setBlock(Block var1, Material var2, @Nullable BlockValues var3, int var4);

   void sendBlockChange(Player var1, Location var2, Material var3, @Nullable BlockValues var4);
}
