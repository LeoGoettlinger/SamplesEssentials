package com.sk89q.worldedit.registry.state;

import com.fastasyncworldedit.core.util.StringMan;
import java.util.Collections;
import java.util.List;
import javax.annotation.Nullable;

public class IntegerProperty extends AbstractProperty<Integer> {
   private final int[] map;

   public IntegerProperty(final String name, final List<Integer> values) {
      this(name, values, 0);
   }

   private IntegerProperty(final String name, final List<Integer> values, int bitOffset) {
      super(name, values, bitOffset);
      int max = Collections.max(values);
      this.map = new int[max + 1];
      int i = 0;

      while (i < values.size()) {
         this.map[values.get(i)] = i++;
      }
   }

   public IntegerProperty withOffset(int bitOffset) {
      return new IntegerProperty(this.getName(), this.getValues(), bitOffset);
   }

   public int getIndex(Integer value) {
      try {
         return this.map[value];
      } catch (IndexOutOfBoundsException var3) {
         return -1;
      }
   }

   @Override
   public int getIndexFor(CharSequence string) throws IllegalArgumentException {
      return this.map[StringMan.parseInt(string)];
   }

   @Nullable
   public Integer getValueFor(String string) {
      try {
         int val = Integer.parseInt(string);
         if (val > 0 && val >= this.map.length) {
            throw new IllegalArgumentException("Invalid int value: " + string + ". Must be in " + this.getValues().toString());
         } else {
            return val;
         }
      } catch (NumberFormatException var3) {
         throw new IllegalArgumentException("Invalid int value: " + string + ". Not an int.");
      }
   }
}
