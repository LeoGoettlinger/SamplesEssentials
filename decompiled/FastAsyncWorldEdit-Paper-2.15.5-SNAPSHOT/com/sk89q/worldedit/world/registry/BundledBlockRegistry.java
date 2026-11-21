package com.sk89q.worldedit.world.registry;

import com.fastasyncworldedit.core.configuration.Caption;
import com.sk89q.worldedit.registry.state.Property;
import com.sk89q.worldedit.util.formatting.text.Component;
import com.sk89q.worldedit.util.formatting.text.TextComponent;
import com.sk89q.worldedit.util.translation.TranslationManager;
import com.sk89q.worldedit.world.block.BlockState;
import com.sk89q.worldedit.world.block.BlockType;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.OptionalInt;
import javax.annotation.Nullable;

public class BundledBlockRegistry implements BlockRegistry {
   @Override
   public Component getRichName(BlockType blockType) {
      BundledBlockData.BlockEntry blockEntry = BundledBlockData.getInstance().findById(blockType.id());
      return (Component)(blockEntry != null
         ? TextComponent.of(blockEntry.localizedName)
         : Caption.of(TranslationManager.makeTranslationKey("block", blockType.id())));
   }

   @Nullable
   @Deprecated
   @Override
   public String getName(BlockType blockType) {
      BundledBlockData.BlockEntry blockEntry = BundledBlockData.getInstance().findById(blockType.id());
      return blockEntry != null ? blockEntry.localizedName : null;
   }

   @Nullable
   @Override
   public BlockMaterial getMaterial(BlockType blockType) {
      return new PassthroughBlockMaterial(BundledBlockData.getInstance().getMaterialById(blockType.id()));
   }

   @Nullable
   @Override
   public Map<String, ? extends Property<?>> getProperties(BlockType blockType) {
      return Collections.emptyMap();
   }

   @Override
   public OptionalInt getInternalBlockStateId(BlockState state) {
      return OptionalInt.empty();
   }

   @Override
   public Map<String, ? extends List<Property<?>>> getAllProperties() {
      return Collections.emptyMap();
   }
}
