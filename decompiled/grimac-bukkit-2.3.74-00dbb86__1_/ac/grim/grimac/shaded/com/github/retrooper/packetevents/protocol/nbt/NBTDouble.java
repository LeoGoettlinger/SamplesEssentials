package ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.nbt;

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
public class NBTDouble extends NBTNumber {
   protected final double value;

   public NBTDouble(double value) {
      this.value = value;
   }

   @Override
   public NBTType<NBTDouble> getType() {
      return NBTType.DOUBLE;
   }

   @Override
   public Number getAsNumber() {
      return this.value;
   }

   @Override
   public byte getAsByte() {
      return (byte)this.value;
   }

   @Override
   public short getAsShort() {
      return (short)this.value;
   }

   @Override
   public int getAsInt() {
      return (int)this.value;
   }

   @Override
   public long getAsLong() {
      return (long)this.value;
   }

   @Override
   public float getAsFloat() {
      return (float)this.value;
   }

   @Override
   public double getAsDouble() {
      return this.value;
   }

   @Override
   public int hashCode() {
      return Double.hashCode(this.value);
   }

   @Override
   public boolean equals(@Nullable Object obj) {
      if (this == obj) {
         return true;
      } else if (obj == null) {
         return false;
      } else if (this.getClass() != obj.getClass()) {
         return false;
      } else {
         NBTDouble other = (NBTDouble)obj;
         return Double.doubleToLongBits(this.value) == Double.doubleToLongBits(other.value);
      }
   }

   public NBTDouble copy() {
      return this;
   }

   @Override
   public String toString() {
      return "Double(" + this.value + ")";
   }
}
