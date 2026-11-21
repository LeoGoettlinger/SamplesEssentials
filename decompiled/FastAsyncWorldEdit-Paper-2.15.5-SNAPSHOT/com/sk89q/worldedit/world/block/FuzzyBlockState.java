package com.sk89q.worldedit.world.block;

import com.fastasyncworldedit.core.registry.state.PropertyKey;
import com.google.common.base.Preconditions;
import com.sk89q.jnbt.CompoundTag;
import com.sk89q.worldedit.registry.state.Property;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Map.Entry;

public class FuzzyBlockState extends BlockState {
   private final Map<PropertyKey, Object> props;
   private final Map<Property<?>, Object> values;

   FuzzyBlockState(BlockType blockType) {
      this(blockType.getDefaultState(), null);
   }

   public FuzzyBlockState(BlockState state) {
      this(state, null);
   }

   public FuzzyBlockState(BlockState state, Map<Property<?>, Object> values) {
      super(state.getBlockType(), state.getInternalId(), state.getOrdinal());
      if (values != null && !values.isEmpty()) {
         this.props = new HashMap<>(values.size());

         for (Entry<Property<?>, Object> entry : values.entrySet()) {
            this.props.put(entry.getKey().getKey(), entry.getValue());
         }

         this.values = new HashMap<>(values);
      } else {
         this.props = Collections.emptyMap();
         this.values = Collections.emptyMap();
      }
   }

   public BlockState getFullState() {
      BlockState state = this.getBlockType().getDefaultState();

      for (Entry<Property<?>, Object> entry : this.getStates().entrySet()) {
         Property<Object> objKey = (Property<Object>)entry.getKey();
         state = state.with(objKey, entry.getValue());
      }

      return state;
   }

   @Override
   public BlockState toImmutableState() {
      return this.getFullState();
   }

   @Override
   public boolean equalsFuzzy(BlockStateHolder<?> o) {
      if (!this.getBlockType().equals(o.getBlockType())) {
         return false;
      } else {
         if (!this.props.isEmpty()) {
            for (Entry<PropertyKey, Object> entry : this.props.entrySet()) {
               if (!Objects.equals(o.getState(entry.getKey()), entry.getValue())) {
                  return false;
               }
            }
         }

         return true;
      }
   }

   @Override
   public BaseBlock toBaseBlock() {
      if (this.props != null && !this.props.isEmpty()) {
         BlockState state = this;

         for (Entry<PropertyKey, Object> entry : this.props.entrySet()) {
            state = state.with(entry.getKey(), entry.getValue());
         }

         return new BaseBlock(state);
      } else {
         return super.toBaseBlock();
      }
   }

   @Override
   public Map<Property<?>, Object> getStates() {
      return this.values;
   }

   @Deprecated
   @Override
   public CompoundTag getNbtData() {
      return this.getBlockType().getMaterial().isTile() ? this.getBlockType().getMaterial().getDefaultTile() : null;
   }

   public static FuzzyBlockState.Builder builder() {
      return new FuzzyBlockState.Builder();
   }

   public static class Builder {
      private BlockType type;
      private final Map<Property<?>, Object> values = new HashMap<>();

      public FuzzyBlockState.Builder type(BlockType type) {
         Preconditions.checkNotNull(type);
         this.type = type;
         return this;
      }

      public FuzzyBlockState.Builder type(BlockState state) {
         Preconditions.checkNotNull(state);
         this.type = state.getBlockType();
         return this;
      }

      public <V> FuzzyBlockState.Builder withProperty(Property<V> property, V value) {
         Preconditions.checkNotNull(property);
         Preconditions.checkNotNull(value);
         Preconditions.checkNotNull(this.type, "The type must be set before the properties!");
         Preconditions.checkNotNull(this.type.getProperty(property.getName()));
         this.values.put(property, value);
         return this;
      }

      public FuzzyBlockState build() {
         Preconditions.checkNotNull(this.type);
         return this.values.isEmpty() ? this.type.getFuzzyMatcher() : new FuzzyBlockState(this.type.getDefaultState(), this.values);
      }

      public FuzzyBlockState.Builder reset() {
         this.type = null;
         this.values.clear();
         return this;
      }
   }
}
