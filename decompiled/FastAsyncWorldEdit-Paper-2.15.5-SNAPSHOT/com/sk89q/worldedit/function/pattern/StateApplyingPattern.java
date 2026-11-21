package com.sk89q.worldedit.function.pattern;

import com.sk89q.worldedit.blocks.Blocks;
import com.sk89q.worldedit.extent.Extent;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.registry.state.Property;
import com.sk89q.worldedit.world.block.BaseBlock;
import com.sk89q.worldedit.world.block.BlockState;
import com.sk89q.worldedit.world.block.BlockType;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

public class StateApplyingPattern extends AbstractExtentPattern {
   private final Map<String, String> states;
   private final Map<BlockType, StateApplyingPattern.PropertyApplication[]> cache = new ConcurrentHashMap<>();

   public StateApplyingPattern(Extent extent, Map<String, String> statesToSet) {
      super(extent);
      this.states = statesToSet;
   }

   @Override
   public BaseBlock applyBlock(BlockVector3 position) {
      BlockState block = position.getBlock(this.getExtent());
      BlockType blockType = block.getBlockType();
      StateApplyingPattern.PropertyApplication[] applications = this.cache.get(blockType);
      if (applications == null) {
         applications = this.resolvePropertiesFor(blockType);
         this.cache.put(blockType, applications);
      }

      for (StateApplyingPattern.PropertyApplication entry : applications) {
         if (blockType.hasProperty(entry.property().getKey())) {
            block = block.with(entry.property(), entry.value());
         }
      }

      return block.toBaseBlock();
   }

   private StateApplyingPattern.PropertyApplication[] resolvePropertiesFor(BlockType blockType) {
      Map<Property<Object>, Object> map = Blocks.resolveProperties(this.states, blockType);
      StateApplyingPattern.PropertyApplication[] applications = new StateApplyingPattern.PropertyApplication[map.size()];
      int i = 0;

      for (Entry<Property<Object>, Object> entry : map.entrySet()) {
         Property<Object> property = entry.getKey();
         Object o = entry.getValue();
         applications[i++] = new StateApplyingPattern.PropertyApplication(property, o);
      }

      return applications;
   }

   private record PropertyApplication(Property<Object> property, Object value) {
   }
}
