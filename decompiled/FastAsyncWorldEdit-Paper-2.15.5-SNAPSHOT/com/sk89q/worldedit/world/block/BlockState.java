package com.sk89q.worldedit.world.block;

import com.fastasyncworldedit.core.command.SuggestInputParseException;
import com.fastasyncworldedit.core.configuration.Caption;
import com.fastasyncworldedit.core.function.mask.SingleBlockStateMask;
import com.fastasyncworldedit.core.queue.IBlocks;
import com.fastasyncworldedit.core.queue.ITileInput;
import com.fastasyncworldedit.core.registry.state.PropertyKey;
import com.fastasyncworldedit.core.util.MutableCharSequence;
import com.fastasyncworldedit.core.util.StringMan;
import com.fastasyncworldedit.core.world.block.BlanketBaseBlock;
import com.fastasyncworldedit.core.world.block.CompoundInput;
import com.google.common.collect.Maps;
import com.sk89q.jnbt.CompoundTag;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.extension.input.InputParseException;
import com.sk89q.worldedit.extension.platform.Capability;
import com.sk89q.worldedit.extent.Extent;
import com.sk89q.worldedit.extent.NullExtent;
import com.sk89q.worldedit.extent.OutputExtent;
import com.sk89q.worldedit.function.mask.Mask;
import com.sk89q.worldedit.function.pattern.Pattern;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.registry.state.AbstractProperty;
import com.sk89q.worldedit.registry.state.Property;
import com.sk89q.worldedit.util.concurrency.LazyReference;
import com.sk89q.worldedit.util.formatting.text.TextComponent;
import com.sk89q.worldedit.world.registry.BlockMaterial;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.enginehub.linbus.tree.LinCompoundTag;

public class BlockState implements BlockStateHolder<BlockState>, Pattern {
   private final int internalId;
   private final int ordinal;
   private final char ordinalChar;
   private BlockMaterial material;
   private final BaseBlock emptyBaseBlock;
   private CompoundInput compoundInput = CompoundInput.NULL;
   private final BlockType blockType;
   private final LazyReference<String> lazyStringRepresentation;

   public BlockState(BlockType blockType, int internalId, int ordinal) {
      this.blockType = blockType;
      this.internalId = internalId;
      this.ordinal = ordinal;
      this.ordinalChar = (char)ordinal;
      this.emptyBaseBlock = new BlanketBaseBlock(this);
      this.lazyStringRepresentation = LazyReference.from(() -> BlockStateHolder.super.getAsString());
   }

   public BlockState(BlockType blockType, int internalId, int ordinal, @Nonnull CompoundTag tile) {
      this.blockType = blockType;
      this.internalId = internalId;
      this.ordinal = ordinal;
      this.ordinalChar = (char)ordinal;
      this.emptyBaseBlock = new BlanketBaseBlock(this, tile);
      this.lazyStringRepresentation = LazyReference.from(() -> BlockStateHolder.super.getAsString());
   }

   @Deprecated
   public static BlockState getFromInternalId(int combinedId) throws InputParseException {
      return BlockTypes.getFromStateId(combinedId).withStateId(combinedId);
   }

   @Deprecated
   public static BlockState getFromOrdinal(int ordinal) {
      return BlockTypesCache.states[ordinal];
   }

   public static BlockState get(String state) throws InputParseException {
      return get(null, state);
   }

   public static BlockState get(@Nullable BlockType type, String state) throws InputParseException {
      return get(type, state, null);
   }

   public static BlockState get(@Nullable BlockType type, String state, BlockState defaultState) throws InputParseException {
      int propStrStart = state.indexOf(91);
      if (type == null) {
         CharSequence key;
         if (propStrStart == -1) {
            key = state;
         } else {
            MutableCharSequence charSequence = MutableCharSequence.getTemporal();
            charSequence.setString(state);
            charSequence.setSubstring(0, propStrStart);
            key = charSequence;
         }

         type = BlockTypes.get(key);
         if (type == null) {
            String input = key.toString();
            throw new SuggestInputParseException(
               Caption.of("fawe.error.invalid-block-type", TextComponent.of(input)),
               () -> Stream.of(BlockTypesCache.values)
                  .map(BlockType::id)
                  .filter(id -> StringMan.blockStateMatches(input, id))
                  .sorted(StringMan.blockStateComparator(input))
                  .collect(Collectors.toList())
            );
         }
      }

      if (propStrStart == -1) {
         return type.getDefaultState();
      } else {
         List<? extends Property<?>> propList = type.getProperties();
         if (state.charAt(state.length() - 1) != ']') {
            state = state + "]";
         }

         MutableCharSequence charSequence = MutableCharSequence.getTemporal();
         charSequence.setString(state);
         if (propList.size() == 1) {
            AbstractProperty<?> property = (AbstractProperty<?>)propList.get(0);
            String name = property.getName();
            charSequence.setSubstring(propStrStart + name.length() + 2, state.length() - 1);

            try {
               int index = charSequence.length() <= 0 ? -1 : property.getIndexFor(charSequence);
               if (index != -1) {
                  return type.withPropertyId(index);
               }
            } catch (Exception var16) {
               throw new InputParseException(
                  Caption.of(
                     "fawe.error.invalid-block-state-property", TextComponent.of(charSequence.toString()), TextComponent.of(name), TextComponent.of(state)
                  ),
                  var16
               );
            }
         }

         int stateId;
         if (defaultState != null) {
            stateId = defaultState.getInternalId();
         } else {
            stateId = type.getDefaultState().getInternalId();
         }

         int length = state.length();
         AbstractProperty<?> property = null;
         int last = propStrStart + 1;

         for (int i = last; i < length; i++) {
            char c = state.charAt(i);
            switch (c) {
               case ',':
               case ']':
                  charSequence.setSubstring(last, i);
                  if (property == null) {
                     PropertyKey keyx = PropertyKey.getByName(charSequence);
                     if (keyx != null && type.hasProperty(keyx)) {
                        throw new SuggestInputParseException(Caption.of("fawe.error.no-operator-for-input", state), () -> Collections.singletonList("="));
                     }

                     String input = charSequence.toString();
                     BlockType finalType = type;
                     throw new SuggestInputParseException(
                        Caption.of("worldedit.error.parser.unknown-property", keyx + ":" + input, type),
                        () -> finalType.getProperties()
                           .stream()
                           .map(Property::getName)
                           .filter(p -> StringMan.blockStateMatches(input, p))
                           .sorted(StringMan.blockStateComparator(input))
                           .collect(Collectors.toList())
                     );
                  }

                  int index;
                  try {
                     index = property.getIndexFor(charSequence);
                  } catch (Exception var15) {
                     throw new InputParseException(
                        Caption.of(
                           "fawe.error.invalid-block-state-property",
                           TextComponent.of(charSequence.toString()),
                           TextComponent.of(property.getName()),
                           TextComponent.of(state)
                        ),
                        var15
                     );
                  }

                  if (index == -1) {
                     throw SuggestInputParseException.of(charSequence.toString(), (List<Object>)property.getValues());
                  }

                  stateId = property.modifyIndex(stateId, index);
                  property = null;
                  last = i + 1;
                  break;
               case '=':
                  charSequence.setSubstring(last, i);
                  property = (AbstractProperty<?>)type.getPropertyMap().get(charSequence);
                  last = i + 1;
            }
         }

         return type.withPropertyId(stateId >> BlockTypesCache.BIT_OFFSET);
      }
   }

   public BlockState withPropertyId(int propertyId) {
      return this.getBlockType().withPropertyId(propertyId);
   }

   @Override
   public boolean apply(Extent extent, BlockVector3 get, BlockVector3 set) throws WorldEditException {
      return set.setBlock(extent, this);
   }

   @Override
   public BaseBlock applyBlock(BlockVector3 position) {
      return this.toBaseBlock();
   }

   public Mask toMask() {
      return new SingleBlockStateMask(new NullExtent(), this);
   }

   @Override
   public void applyTileEntity(OutputExtent output, int x, int y, int z) {
   }

   @Deprecated
   @Override
   public final int getInternalPropertiesId() {
      return this.getInternalId() >> BlockTypesCache.BIT_OFFSET;
   }

   @Deprecated
   @Override
   public final int getInternalBlockTypeId() {
      return this.getInternalId() & BlockTypesCache.BIT_MASK;
   }

   public <V> BlockState with(final Property<V> property, final V value) {
      try {
         BlockType type = this.getBlockType();
         int newState = ((AbstractProperty)property).modify(this.getInternalId(), value);
         return newState != this.getInternalId() ? type.withStateId(newState) : this;
      } catch (ClassCastException var5) {
         throw new IllegalArgumentException("Property not found: " + property);
      } catch (Exception var6) {
         throw new UnsupportedOperationException(
            "Error resolving property " + property.getName() + " for block type " + this.getBlockType().id() + "(nullable) value " + value, var6
         );
      }
   }

   @Override
   public <V> V getState(final Property<V> property) {
      try {
         AbstractProperty<V> ap = (AbstractProperty<V>)property;
         return ap.getValue(this.getInternalId());
      } catch (ClassCastException var3) {
         throw new IllegalArgumentException("Property not found: " + property);
      } catch (Exception var4) {
         throw new UnsupportedOperationException("Error resolving property " + property.getName() + " for blocktype " + this.getBlockType().id(), var4);
      }
   }

   public <V> BlockState with(final PropertyKey property, final V value) {
      try {
         BlockType type = this.getBlockType();
         AbstractProperty<V> abstractProperty = (AbstractProperty<V>)type.<V>getProperty(property);
         if (abstractProperty == null) {
            return this;
         } else {
            int newState = abstractProperty.modify(this.getInternalId(), value);
            return newState != this.getInternalId() ? type.withStateId(newState) : this;
         }
      } catch (ClassCastException var6) {
         throw new IllegalArgumentException("Property not found: " + property);
      } catch (Exception var7) {
         throw new UnsupportedOperationException(
            "Error resolving property " + property.getName() + " for block type " + this.getBlockType().id() + "(nullable) value " + value, var7
         );
      }
   }

   public <V> BlockState withProperties(final BlockState other) {
      BlockType ot = other.getBlockType();
      if (ot == this.blockType) {
         return other;
      } else if (!ot.getProperties().isEmpty() && !this.blockType.getProperties().isEmpty()) {
         BlockState newState = this;

         for (Property<?> prop : ot.getProperties()) {
            PropertyKey key = prop.getKey();
            if (this.blockType.hasProperty(prop)) {
               newState = newState.with(key, other.getState(key));
            }
         }

         return newState;
      } else {
         return this;
      }
   }

   @Override
   public Map<Property<?>, Object> getStates() {
      BlockType type = this.getBlockType();
      Map<? extends Property<?>, Object> map = Maps.asMap(type.getPropertiesSet(), this::getState);
      return Collections.unmodifiableMap(map);
   }

   @Override
   public BlockType getBlockType() {
      return this.blockType;
   }

   @Override
   public boolean equalsFuzzy(BlockStateHolder<?> o) {
      if (null == o) {
         return false;
      } else if (this == o) {
         return true;
      } else {
         return o.getClass() == BlockState.class ? o.getOrdinal() == this.getOrdinal() : o.equalsFuzzy(this);
      }
   }

   @Override
   public BlockState toImmutableState() {
      return this;
   }

   @Override
   public BaseBlock toBaseBlock() {
      return this.emptyBaseBlock;
   }

   @Deprecated
   @Override
   public <V> V getState(PropertyKey key) {
      return this.getState(this.getBlockType().getProperty(key));
   }

   @Deprecated
   @Override
   public CompoundTag getNbtData() {
      return this.getBlockType().getMaterial().isTile() ? this.getBlockType().getMaterial().getDefaultTile() : null;
   }

   @Override
   public BaseBlock toBaseBlock(LazyReference<LinCompoundTag> compoundTag) {
      return compoundTag == null ? this.toBaseBlock() : new BaseBlock(this, compoundTag);
   }

   @Override
   public int getInternalId() {
      return this.internalId;
   }

   @Override
   public BlockMaterial getMaterial() {
      if (this.material == null) {
         if (this.blockType == BlockTypes.__RESERVED__) {
            return this.material = this.blockType.getMaterial();
         }

         this.material = WorldEdit.getInstance()
            .getPlatformManager()
            .queryCapability(Capability.GAME_HOOKS)
            .getRegistries()
            .getBlockRegistry()
            .getMaterial(this);
         if (this.material.hasContainer()) {
            this.compoundInput = CompoundInput.CONTAINER;
         }
      }

      return this.material;
   }

   @Override
   public final int getOrdinal() {
      return this.ordinal;
   }

   @Override
   public final char getOrdinalChar() {
      return this.ordinalChar;
   }

   @Override
   public int hashCode() {
      return this.getOrdinal();
   }

   public boolean isAir() {
      return this.blockType.getMaterial().isAir();
   }

   @Override
   public BaseBlock toBaseBlock(ITileInput input, int x, int y, int z) {
      return this.compoundInput.get(this, input, x, y, z);
   }

   @Override
   public BaseBlock toBaseBlock(final IBlocks blocks, final int x, final int y, final int z) {
      return this.compoundInput.get(this, blocks, x, y, z);
   }

   @Override
   public String getAsString() {
      return this.lazyStringRepresentation.getValue();
   }

   @Override
   public String toString() {
      return this.getAsString();
   }

   @Override
   public boolean equals(Object obj) {
      return !(obj instanceof BlockState) ? false : this.equalsFuzzy((BlockState)obj);
   }
}
