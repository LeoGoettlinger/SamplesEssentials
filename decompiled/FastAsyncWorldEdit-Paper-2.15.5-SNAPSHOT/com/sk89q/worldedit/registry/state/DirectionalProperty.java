package com.sk89q.worldedit.registry.state;

import com.sk89q.worldedit.util.Direction;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import javax.annotation.Nullable;

public class DirectionalProperty extends AbstractProperty<Direction> {
   private final int[] map = new int[Direction.values().length];

   private DirectionalProperty(final String name, final List<Direction> values, int bitOffset) {
      super(name, values, bitOffset);
      Arrays.fill(this.map, -1);
      int i = 0;

      while (i < values.size()) {
         this.map[values.get(i).ordinal()] = i++;
      }
   }

   public DirectionalProperty withOffset(int bitOffset) {
      return new DirectionalProperty(this.getName(), this.getValues(), bitOffset);
   }

   public int getIndex(Direction value) {
      return this.map[value.ordinal()];
   }

   @Override
   public int getIndexFor(CharSequence string) throws IllegalArgumentException {
      Direction dir = Direction.get(string);
      return dir == null ? -1 : this.getIndex(dir);
   }

   public DirectionalProperty(final String name, final List<Direction> values) {
      this(name, values, 0);
   }

   @Nullable
   public Direction getValueFor(final String string) {
      Direction direction = Direction.valueOf(string.toUpperCase(Locale.ROOT));
      if (!this.getValues().contains(direction)) {
         throw new IllegalArgumentException("Invalid direction value: " + string + ". Must be in " + this.getValues().toString());
      } else {
         return direction;
      }
   }
}
