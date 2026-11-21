package com.sk89q.worldedit.world.block;

import com.fastasyncworldedit.core.queue.IBlocks;
import com.fastasyncworldedit.core.queue.ITileInput;
import com.fastasyncworldedit.core.registry.state.PropertyKey;
import com.sk89q.jnbt.CompoundTag;
import com.sk89q.worldedit.blocks.TileEntityBlock;
import com.sk89q.worldedit.extent.OutputExtent;
import com.sk89q.worldedit.function.pattern.Pattern;
import com.sk89q.worldedit.internal.util.DeprecationUtil;
import com.sk89q.worldedit.internal.util.NonAbstractForCompatibility;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.registry.state.Property;
import com.sk89q.worldedit.util.concurrency.LazyReference;
import com.sk89q.worldedit.world.registry.BlockMaterial;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;
import org.enginehub.linbus.tree.LinCompoundTag;

public interface BlockStateHolder<B extends BlockStateHolder<B>> extends TileEntityBlock, Pattern {
   BlockType getBlockType();

   @Deprecated
   B withPropertyId(int propertyId);

   @Deprecated
   int getInternalId();

   @Deprecated
   int getOrdinal();

   @Deprecated
   char getOrdinalChar();

   BlockMaterial getMaterial();

   @Deprecated
   int getInternalBlockTypeId();

   @Deprecated
   int getInternalPropertiesId();

   <V> B with(final Property<V> property, final V value);

   <V> V getState(Property<V> property);

   <V> B with(final PropertyKey property, final V value);

   <V> V getState(final PropertyKey property);

   Map<Property<?>, Object> getStates();

   boolean equalsFuzzy(BlockStateHolder<?> o);

   BlockState toImmutableState();

   BaseBlock toBaseBlock();

   @Deprecated
   default BaseBlock toBaseBlock(CompoundTag compoundTag) {
      return this.toBaseBlock(compoundTag == null ? null : LazyReference.from(compoundTag::toLinTag));
   }

   @NonAbstractForCompatibility(
      delegateName = "toBaseBlock",
      delegateParams = {CompoundTag.class}
   )
   default BaseBlock toBaseBlock(LazyReference<LinCompoundTag> compoundTag) {
      DeprecationUtil.checkDelegatingOverride(this.getClass());
      return this.toBaseBlock(compoundTag == null ? null : new CompoundTag(compoundTag.getValue()));
   }

   default BaseBlock toBaseBlock(LinCompoundTag compoundTag) {
      return this.toBaseBlock(compoundTag == null ? null : LazyReference.computed(compoundTag));
   }

   @Override
   default BaseBlock applyBlock(BlockVector3 position) {
      return this.toBaseBlock();
   }

   void applyTileEntity(OutputExtent output, int x, int y, int z);

   default BaseBlock toBaseBlock(ITileInput input, int x, int y, int z) {
      throw new UnsupportedOperationException("State is immutable");
   }

   default BaseBlock toBaseBlock(IBlocks blocks, int x, int y, int z) {
      throw new UnsupportedOperationException("State is immutable");
   }

   default String getAsString() {
      if (this.getStates().isEmpty()) {
         return this.getBlockType().id();
      } else {
         String properties = this.getStates()
            .entrySet()
            .stream()
            .map(entry -> entry.getKey().getName() + "=" + entry.getValue().toString().toLowerCase(Locale.ROOT))
            .collect(Collectors.joining(","));
         return this.getBlockType().id() + "[" + properties + "]";
      }
   }
}
