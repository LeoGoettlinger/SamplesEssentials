package com.magmaguy.elitemobs.easyminecraftgoals.v1_21_R7_spigot.massblockedit;

import com.magmaguy.elitemobs.easyminecraftgoals.v1_21_R7_spigot.CraftBukkitBridge;
import net.minecraft.core.BlockPosition;
import net.minecraft.server.level.WorldServer;
import net.minecraft.world.level.block.state.IBlockData;
import org.bukkit.World;
import org.bukkit.block.data.BlockData;

public class MassEditBlocks {
   public static void setBlockInNativeDataPalette(World world, int x, int y, int z, BlockData blockData, boolean applyPhysics) {
      WorldServer nmsWorld = CraftBukkitBridge.getServerLevel(world);
      BlockPosition blockPos = new BlockPosition(x, y, z);
      IBlockData blockState = CraftBukkitBridge.getBlockState(blockData);
      int flags = 2 | (applyPhysics ? 1 : 16);
      if (!nmsWorld.a(blockPos, blockState, flags, 512) && !nmsWorld.a_(blockPos).equals(blockState)) {
         throw new IllegalStateException("Native block placement failed at " + x + "," + y + "," + z);
      }
   }
}
