package org.lushplugins.lushrewards.libraries.lushlib.utils;

import java.util.Random;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.annotation.JsonDeserialize;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.annotation.JsonSerialize;
import org.lushplugins.lushrewards.libraries.lushlib.serializer.IntRangeDeserializer;
import org.lushplugins.lushrewards.libraries.lushlib.serializer.IntRangeSerializer;

@JsonSerialize(
   using = IntRangeSerializer.class
)
@JsonDeserialize(
   using = IntRangeDeserializer.class
)
public class IntRange implements Cloneable {
   private static final Random RANDOM = new Random();
   private final int min;
   private final int max;

   public IntRange(int min, int max) {
      this.min = min;
      this.max = max;
   }

   public IntRange(int value) {
      this.min = value;
      this.max = value;
   }

   public int getMin() {
      return this.min;
   }

   public int getMax() {
      return this.max;
   }

   public boolean contains(int number) {
      return number >= this.min && number <= this.max;
   }

   @Deprecated
   public boolean inRange(int number) {
      return this.contains(number);
   }

   public int next() {
      return this.min == this.max ? this.min : RANDOM.nextInt(this.min, this.max + 1);
   }

   public static IntRange parseIntRange(String string) throws NumberFormatException {
      if (string == null) {
         throw new NumberFormatException("Cannot parse null string");
      } else {
         String[] values = string.split("-");
         switch (values.length) {
            case 1:
               int value = Integer.parseInt(values[0]);
               return new IntRange(value, value);
            case 2:
               return new IntRange(Integer.parseInt(values[0]), Integer.parseInt(values[1]));
            default:
               throw new NumberFormatException("Cannot parse invalid range format");
         }
      }
   }

   public static IntRange valueOf(Object object) throws NumberFormatException {
      String s;
      try {
         s = (String)object;
      } catch (ClassCastException var3) {
         return new IntRange((Integer)object);
      }

      return parseIntRange(s);
   }

   @Override
   public String toString() {
      return this.min + "-" + this.max;
   }

   public IntRange clone() {
      try {
         return (IntRange)super.clone();
      } catch (CloneNotSupportedException var2) {
         throw new AssertionError();
      }
   }
}
