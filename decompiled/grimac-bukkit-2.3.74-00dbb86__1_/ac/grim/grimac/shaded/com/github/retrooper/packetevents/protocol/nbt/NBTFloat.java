package ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.nbt;

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
public class NBTFloat extends NBTNumber {
   protected final float value;

   public NBTFloat(float value) {
      this.value = value;
   }

   @Override
   public NBTType<NBTFloat> getType() {
      return NBTType.FLOAT;
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
      return this.value;
   }

   @Override
   public double getAsDouble() {
      return this.value;
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
         NBTFloat other = (NBTFloat)obj;
         return Float.floatToIntBits(this.value) == Float.floatToIntBits(other.value);
      }
   }

   @Override
   public int hashCode() {
      return Float.hashCode(this.value);
   }

   public NBTFloat copy() {
      return this;
   }

   @Override
   public String toString() {
      return "Float(" + this.value + ")";
   }
}
