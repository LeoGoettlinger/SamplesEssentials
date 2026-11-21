package com.fastasyncworldedit.bukkit.adapter;

import com.google.common.base.Preconditions;
import com.sk89q.worldedit.world.block.BlockStateHolder;
import com.sk89q.worldedit.world.block.BlockType;
import com.sk89q.worldedit.world.block.BlockTypes;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.data.BlockData;

public class SimpleBukkitAdapter extends CachedBukkitAdapter {
   private BlockData[][] blockDataCache;

   private boolean init() {
      if (this.blockDataCache != null) {
         return false;
      } else {
         this.blockDataCache = new BlockData[BlockTypes.size()][];
         this.blockDataCache[0] = new BlockData[]{Material.AIR.createBlockData()};
         return true;
      }
   }

   @Override
   protected int[] getIbdToOrdinal() {
      return new int[65536];
   }

   @Override
   protected int[] getOrdinalToIbdID() {
      return new int[65536];
   }

   @Override
   public <B extends BlockStateHolder<B>> BlockData adapt(B block) {
      try {
         Preconditions.checkNotNull(block);
         int typeId = block.getInternalBlockTypeId();
         BlockData[] dataCache = this.blockDataCache[typeId];
         if (dataCache == null) {
            BlockType type = BlockTypes.get(typeId);
            this.blockDataCache[typeId] = dataCache = new BlockData[type.getMaxStateId() + 1];
         }

         int propId = block.getInternalPropertiesId();
         BlockData blockData = dataCache[propId];
         if (blockData == null) {
            dataCache[propId] = blockData = Bukkit.createBlockData(block.getAsString());
         }

         return blockData;
      } catch (NullPointerException var6) {
         if (this.init()) {
            return this.adapt(block);
         } else {
            throw var6;
         }
      }
   }
}
