package com.fastasyncworldedit.core.limit;

import com.sk89q.worldedit.registry.state.Property;
import com.sk89q.worldedit.world.block.BlockStateHolder;
import com.sk89q.worldedit.world.block.BlockType;

public record PropertyRemap<T>(Property<T> property, T oldValue, T newValue) {
   public <B extends BlockStateHolder<B>> B apply(B state) {
      if (!state.getBlockType().hasProperty(this.property.getKey())) {
         return state;
      } else {
         T current = state.getState(this.property);
         if (current == this.oldValue) {
            state = state.with(this.property.getKey(), this.newValue);
         }

         return state;
      }
   }

   public T apply(BlockType type, T value) {
      if (type.hasProperty(this.property.getKey())) {
         return value == this.oldValue ? this.newValue : value;
      } else {
         return value;
      }
   }
}
