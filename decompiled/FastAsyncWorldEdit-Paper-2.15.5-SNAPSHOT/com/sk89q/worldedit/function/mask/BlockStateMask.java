package com.sk89q.worldedit.function.mask;

import com.google.common.collect.Maps;
import com.sk89q.worldedit.blocks.Blocks;
import com.sk89q.worldedit.extent.Extent;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.registry.state.Property;
import com.sk89q.worldedit.world.block.BlockState;
import com.sk89q.worldedit.world.block.BlockType;
import com.sk89q.worldedit.world.block.BlockTypes;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nullable;

@Deprecated
public class BlockStateMask extends AbstractExtentMask {
   private final Map<String, String> states;
   private final boolean strict;
   private final Map<BlockType, Map<Property<Object>, Object>> cache = Maps.newHashMap();

   public BlockStateMask(Extent extent, Map<String, String> states, boolean strict) {
      super(extent);
      this.states = states;
      this.strict = strict;
   }

   @Override
   public boolean test(BlockVector3 vector) {
      return this.test(vector.getBlock(this.getExtent()));
   }

   @Override
   public boolean test(Extent extent, BlockVector3 vector) {
      return this.test(extent.getBlock(vector));
   }

   public boolean test(BlockState block) {
      Map<Property<Object>, Object> checkProps = this.cache.computeIfAbsent(block.getBlockType(), b -> Blocks.resolveProperties(this.states, b));
      return this.strict && checkProps.isEmpty() ? false : checkProps.entrySet().stream().allMatch(entry -> block.getState(entry.getKey()) == entry.getValue());
   }

   @Override
   public boolean replacesAir() {
      return this.test(BlockTypes.AIR.getDefaultState())
         || this.test(BlockTypes.CAVE_AIR.getDefaultState())
         || this.test(BlockTypes.VOID_AIR.getDefaultState());
   }

   @Nullable
   @Override
   public Mask2D toMask2D() {
      return null;
   }

   @Override
   public Mask copy() {
      Map<String, String> statesClone = new HashMap<>();
      this.states.forEach(statesClone::put);
      return new BlockStateMask(this.getExtent(), statesClone, this.strict);
   }
}
