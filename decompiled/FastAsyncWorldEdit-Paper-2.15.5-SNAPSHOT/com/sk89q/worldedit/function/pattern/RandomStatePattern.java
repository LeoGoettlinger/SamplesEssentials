package com.sk89q.worldedit.function.pattern;

import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.world.block.BaseBlock;
import com.sk89q.worldedit.world.block.BlockState;
import com.sk89q.worldedit.world.block.FuzzyBlockState;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public class RandomStatePattern implements Pattern {
   private final Random rand = new Random();
   private final List<BaseBlock> blocks;

   public RandomStatePattern(FuzzyBlockState state) {
      this.blocks = state.getBlockType().getAllStates().stream().filter(state::equalsFuzzy).map(BlockState::toBaseBlock).collect(Collectors.toList());
   }

   @Override
   public BaseBlock applyBlock(BlockVector3 position) {
      return this.blocks.get(this.rand.nextInt(this.blocks.size()));
   }
}
