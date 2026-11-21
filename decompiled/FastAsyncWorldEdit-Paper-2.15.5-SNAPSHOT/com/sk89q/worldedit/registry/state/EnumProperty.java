package com.sk89q.worldedit.registry.state;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;

public class EnumProperty extends AbstractProperty<String> {
   private final Map<CharSequence, Integer> offsets = new HashMap<>();

   public EnumProperty(final String name, final List<String> values) {
      this(name, values, 0);
   }

   private EnumProperty(final String name, final List<String> values, int bitOffset) {
      super(name, values, bitOffset);

      for (int i = 0; i < values.size(); i++) {
         String value = values.get(i).intern();
         values.set(i, value);
         this.offsets.put(value, i);
      }
   }

   public EnumProperty withOffset(int bitOffset) {
      return new EnumProperty(this.getName(), this.getValues(), bitOffset);
   }

   @Override
   public int getIndexFor(CharSequence string) throws IllegalArgumentException {
      Integer value = this.offsets.get(string);
      return value == null ? -1 : value;
   }

   @Nullable
   public String getValueFor(String string) {
      Integer offset = this.offsets.get(string);
      if (offset == null) {
         throw new IllegalArgumentException("Invalid value: " + string + ". Must be in " + this.getValues().toString());
      } else {
         return this.getValues().get(offset);
      }
   }
}
