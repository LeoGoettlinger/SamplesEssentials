package com.magmaguy.elitemobs.easyminecraftgoals.v26.massblockedit;

import com.magmaguy.elitemobs.easyminecraftgoals.v26.CraftBukkitBridge;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;
import org.bukkit.World;
import org.bukkit.block.data.BlockData;

public class MassEditBlocks {
   public static void setBlockInNativeDataPalette(World world, int x, int y, int z, BlockData blockData, boolean applyPhysics) {
      ServerLevel nmsWorld = CraftBukkitBridge.getServerLevel(world);
      BlockPos blockPos = new BlockPos(x, y, z);
      BlockState blockState = CraftBukkitBridge.getBlockState(blockData);
      int flags = 2 | (applyPhysics ? 1 : 16);
      if (!nmsWorld.setBlock(blockPos, blockState, flags, 512) && !nmsWorld.getBlockState(blockPos).equals(blockState)) {
         throw new IllegalStateException("Native block placement failed at " + x + "," + y + "," + z);
      }
   }
}
