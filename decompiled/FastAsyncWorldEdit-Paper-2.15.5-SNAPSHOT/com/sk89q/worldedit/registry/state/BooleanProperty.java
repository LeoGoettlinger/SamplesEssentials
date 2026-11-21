package com.sk89q.worldedit.registry.state;

import java.util.List;
import javax.annotation.Nullable;

public class BooleanProperty extends AbstractProperty<Boolean> {
   private final int defaultIndex;

   public BooleanProperty(final String name, final List<Boolean> values) {
      this(name, values, 0);
   }

   private BooleanProperty(final String name, final List<Boolean> values, int bitOffset) {
      super(name, values, bitOffset);
      this.defaultIndex = values.get(0) == Boolean.TRUE ? 0 : 1;
   }

   public int getIndex(Boolean value) {
      return value ? this.defaultIndex : 1 - this.defaultIndex;
   }

   public BooleanProperty withOffset(int bitOffset) {
      return new BooleanProperty(this.getName(), this.getValues(), bitOffset);
   }

   @Override
   public int getIndexFor(CharSequence string) throws IllegalArgumentException {
      switch (string.charAt(0)) {
         case 'f':
            return 1 - this.defaultIndex;
         case 't':
            return this.defaultIndex;
         default:
            return -1;
      }
   }

   @Nullable
   public Boolean getValueFor(String string) {
      boolean val = Boolean.parseBoolean(string);
      if (!this.getValues().contains(val)) {
         throw new IllegalArgumentException("Invalid boolean value: " + string + ". Must be in " + this.getValues().toString());
      } else {
         return val;
      }
   }
}
