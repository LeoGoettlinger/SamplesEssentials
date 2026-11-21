package com.sk89q.worldedit.registry.state;

import com.fastasyncworldedit.core.registry.state.PropertyKey;
import com.fastasyncworldedit.core.util.MathMan;
import com.google.common.base.Preconditions;
import com.sk89q.worldedit.world.block.BlockTypesCache;
import java.util.List;
import javax.annotation.Nullable;

public abstract class AbstractProperty<T> implements Property<T> {
   private final PropertyKey key;
   private String name;
   private final List<T> values;
   private final int bitMask;
   private final int bitMaskInverse;
   private final int bitOffset;
   private final int numBits;

   public AbstractProperty(final String name, final List<T> values) {
      this(name, values, 0);
   }

   public AbstractProperty(final String name, final List<T> values, int bitOffset) {
      this.name = name;
      this.values = values;
      this.numBits = MathMan.log2nlz(values.size());
      this.bitOffset = bitOffset + BlockTypesCache.BIT_OFFSET;
      this.bitMask = (1 << this.numBits) - 1 << this.bitOffset;
      this.bitMaskInverse = ~this.bitMask;
      this.key = PropertyKey.getOrCreate(name);
   }

   @Override
   public PropertyKey getKey() {
      return this.key;
   }

   @Deprecated
   public int getNumBits() {
      return this.numBits;
   }

   @Deprecated
   public int getBitOffset() {
      return this.bitOffset;
   }

   @Deprecated
   public int getBitMask() {
      return this.bitMask;
   }

   public abstract <C extends AbstractProperty<T>> C withOffset(int bitOffset);

   @Deprecated
   public int modify(int state, T value) {
      int index = this.getIndex(value);
      return index != -1 ? this.modifyIndex(state, index) : state;
   }

   public int modifyIndex(int state, int index) {
      return state & this.bitMaskInverse | index << this.bitOffset;
   }

   public T getValue(int state) {
      return this.values.get((state & this.bitMask) >> this.bitOffset);
   }

   public int getIndex(int state) {
      return (state & this.bitMask) >> this.bitOffset;
   }

   @Override
   public List<T> getValues() {
      return this.values;
   }

   @Override
   public String toString() {
      return this.getClass().getSimpleName() + "{name=" + this.name + "}";
   }

   @Override
   public int hashCode() {
      return this.name.hashCode();
   }

   @Override
   public String getName() {
      return this.name;
   }

   @Nullable
   @Override
   public T getValueFor(String string) throws IllegalArgumentException {
      return (T)string;
   }

   public void setName(final String name) {
      Preconditions.checkState(this.name == null, "name already set");
      this.name = name;
   }

   @Override
   public boolean equals(Object obj) {
      return !(obj instanceof Property property) ? false : this.getName().equals(property.getName()) && property.getValues().equals(this.getValues());
   }
}
