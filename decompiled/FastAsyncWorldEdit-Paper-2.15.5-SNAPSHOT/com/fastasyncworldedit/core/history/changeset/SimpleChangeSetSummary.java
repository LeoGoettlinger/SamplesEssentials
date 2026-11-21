package com.fastasyncworldedit.core.history.changeset;

import com.sk89q.worldedit.world.block.BlockState;
import com.sk89q.worldedit.world.block.BlockTypesCache;
import java.util.HashMap;
import java.util.Map;

public class SimpleChangeSetSummary implements ChangeSetSummary {
   public int[] blocks = new int[BlockTypesCache.states.length];
   public int minX;
   public int minZ;
   public int maxX;
   public int maxZ;

   public SimpleChangeSetSummary() {
      this.minX = Integer.MAX_VALUE;
      this.minZ = Integer.MAX_VALUE;
      this.maxX = Integer.MIN_VALUE;
      this.maxZ = Integer.MIN_VALUE;
   }

   public SimpleChangeSetSummary(int x, int z) {
      this.minX = x;
      this.maxX = x;
      this.minZ = z;
      this.maxZ = z;
   }

   public void add(int x, int z, int id) {
      this.blocks[id]++;
      if (x < this.minX) {
         this.minX = x;
      } else if (x > this.maxX) {
         this.maxX = x;
      }

      if (z < this.minZ) {
         this.minZ = z;
      } else if (z > this.maxZ) {
         this.maxZ = z;
      }
   }

   @Override
   public Map<BlockState, Integer> getBlocks() {
      HashMap<BlockState, Integer> map = new HashMap<>();

      for (int i = 0; i < this.blocks.length; i++) {
         if (this.blocks[i] != 0) {
            BlockState state = BlockTypesCache.states[i];
            map.put(state, this.blocks[i]);
         }
      }

      return map;
   }

   @Override
   public int getSize() {
      int count = 0;

      for (int block : this.blocks) {
         count += block;
      }

      return count;
   }
}
