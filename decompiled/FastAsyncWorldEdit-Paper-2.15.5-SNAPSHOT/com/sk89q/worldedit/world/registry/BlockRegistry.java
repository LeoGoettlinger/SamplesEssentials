package com.sk89q.worldedit.world.registry;

import com.sk89q.worldedit.registry.state.Property;
import com.sk89q.worldedit.util.formatting.text.Component;
import com.sk89q.worldedit.world.block.BlockState;
import com.sk89q.worldedit.world.block.BlockType;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.OptionalInt;
import javax.annotation.Nullable;

public interface BlockRegistry {
   Component getRichName(BlockType blockType);

   @Deprecated
   @Nullable
   default String getName(BlockType blockType) {
      return this.getRichName(blockType).toString();
   }

   @Nullable
   BlockMaterial getMaterial(BlockType blockType);

   @Nullable
   default BlockMaterial getMaterial(BlockState state) {
      return this.getMaterial(state.getBlockType());
   }

   Map<String, ? extends Property<?>> getProperties(BlockType blockType);

   OptionalInt getInternalBlockStateId(BlockState state);

   default Collection<String> values() {
      return Collections.emptyList();
   }

   Map<String, ? extends List<Property<?>>> getAllProperties();
}
