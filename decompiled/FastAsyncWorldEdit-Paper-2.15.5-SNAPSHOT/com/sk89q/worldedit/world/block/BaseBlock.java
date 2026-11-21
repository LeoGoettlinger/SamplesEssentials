package com.sk89q.worldedit.world.block;

import com.fastasyncworldedit.core.nbt.FaweCompoundTag;
import com.fastasyncworldedit.core.registry.state.PropertyKey;
import com.google.common.base.Preconditions;
import com.sk89q.jnbt.CompoundTag;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.blocks.TileEntityBlock;
import com.sk89q.worldedit.extent.Extent;
import com.sk89q.worldedit.extent.OutputExtent;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.registry.state.Property;
import com.sk89q.worldedit.util.concurrency.LazyReference;
import com.sk89q.worldedit.world.registry.BlockMaterial;
import com.sk89q.worldedit.world.registry.LegacyMapper;
import java.util.Map;
import java.util.Objects;
import javax.annotation.Nullable;
import org.enginehub.linbus.format.snbt.LinStringIO;
import org.enginehub.linbus.tree.LinCompoundTag;
import org.enginehub.linbus.tree.LinTagType;

public class BaseBlock implements BlockStateHolder<BaseBlock>, TileEntityBlock {
   private final BlockState blockState;
   @Nullable
   private final LazyReference<LinCompoundTag> nbtData;

   @Deprecated
   public BaseBlock(BlockType blockType) {
      this(blockType.getDefaultState());
   }

   public BaseBlock(BlockState blockState) {
      this.blockState = blockState;
      this.nbtData = null;
   }

   @Deprecated
   public BaseBlock(BlockState state, CompoundTag nbtData) {
      this(state, LazyReference.from(((CompoundTag)Preconditions.checkNotNull(nbtData))::toLinTag));
   }

   protected BaseBlock(BlockState state, LazyReference<LinCompoundTag> nbtData) {
      Preconditions.checkNotNull(nbtData);
      this.blockState = state;
      this.nbtData = nbtData;
   }

   @Deprecated
   public BaseBlock(int id, int data) {
      this(getState(id, data));
   }

   public static BlockState getState(int id, int data) {
      BlockState blockState = LegacyMapper.getInstance().getBlockFromLegacy(id, data);
      if (blockState == null) {
         blockState = BlockTypes.AIR.getDefaultState();
      }

      return blockState;
   }

   @Override
   public Map<Property<?>, Object> getStates() {
      return this.toImmutableState().getStates();
   }

   @Override
   public BlockType getBlockType() {
      return this.blockState.getBlockType();
   }

   public <V> BaseBlock with(Property<V> property, V value) {
      return this.toImmutableState().with(property, value).toBaseBlock(this.getNbtReference());
   }

   @Override
   public <V> V getState(Property<V> property) {
      return this.toImmutableState().getState(property);
   }

   @Override
   public String getNbtId() {
      LazyReference<LinCompoundTag> nbtData = this.nbtData;
      return nbtData == null ? "" : nbtData.getValue().getTag("id", LinTagType.stringTag()).value();
   }

   @Nullable
   @Override
   public LazyReference<LinCompoundTag> getNbtReference() {
      return this.nbtData;
   }

   @Override
   public void setNbtReference(@Nullable LazyReference<LinCompoundTag> nbtData) {
      throw new UnsupportedOperationException("This class is immutable.");
   }

   @Override
   public boolean equals(Object o) {
      if (!(o instanceof BaseBlock otherBlock)) {
         return this.nbtData == null && o instanceof BlockStateHolder
            ? Objects.equals(this.toImmutableState(), ((BlockStateHolder)o).toImmutableState())
            : false;
      } else {
         return this.blockState.equalsFuzzy(otherBlock.blockState) && Objects.equals(this.getNbt(), otherBlock.getNbt());
      }
   }

   @Override
   public int getInternalId() {
      return this.blockState.getInternalId();
   }

   @Override
   public BlockMaterial getMaterial() {
      return this.blockState.getMaterial();
   }

   @Override
   public int getOrdinal() {
      return this.blockState.getOrdinal();
   }

   @Override
   public final char getOrdinalChar() {
      return this.blockState.getOrdinalChar();
   }

   @Override
   public boolean equalsFuzzy(BlockStateHolder<?> o) {
      return this.blockState.equalsFuzzy(o);
   }

   @Override
   public BlockState toImmutableState() {
      return this.blockState;
   }

   @Override
   public BaseBlock toBaseBlock() {
      return this;
   }

   @Override
   public BaseBlock toBaseBlock(LazyReference<LinCompoundTag> compoundTag) {
      if (compoundTag == null) {
         return this.blockState.toBaseBlock();
      } else {
         return compoundTag == this.nbtData ? this : new BaseBlock(this.blockState, compoundTag);
      }
   }

   @Override
   public boolean apply(Extent extent, BlockVector3 get, BlockVector3 set) throws WorldEditException {
      set.setFullBlock(extent, this);
      return true;
   }

   @Override
   public void applyTileEntity(OutputExtent output, int x, int y, int z) {
      LinCompoundTag nbt = this.getNbt();
      if (nbt != null) {
         output.tile(x, y, z, FaweCompoundTag.of(nbt));
      }
   }

   public BaseBlock withPropertyId(int propertyId) {
      return this.getBlockType().withPropertyId(propertyId).toBaseBlock(this.getNbtReference());
   }

   @Override
   public int getInternalBlockTypeId() {
      return this.toImmutableState().getInternalBlockTypeId();
   }

   @Override
   public int getInternalPropertiesId() {
      return this.toImmutableState().getInternalPropertiesId();
   }

   public <V> BaseBlock with(PropertyKey property, V value) {
      return this.toImmutableState().with(property, value).toBaseBlock(this.getNbtReference());
   }

   @Override
   public <V> V getState(PropertyKey property) {
      return this.toImmutableState().getState(property);
   }

   public BlockState toBlockState() {
      return this.blockState;
   }

   @Override
   public int hashCode() {
      int ret = this.getOrdinal() << 3;
      LinCompoundTag nbtData = this.getNbt();
      if (nbtData != null) {
         ret += nbtData.hashCode();
      }

      return ret;
   }

   @Override
   public String getAsString() {
      String nbtString = "";
      if (this.nbtData != null) {
         nbtString = LinStringIO.writeToString(this.nbtData.getValue());
      }

      return this.blockState.getAsString() + nbtString;
   }

   @Override
   public String toString() {
      return this.getAsString();
   }
}
