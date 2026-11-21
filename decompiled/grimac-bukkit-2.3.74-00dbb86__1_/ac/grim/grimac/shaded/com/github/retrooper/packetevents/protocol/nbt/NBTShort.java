package ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.nbt;

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
public class NBTShort extends NBTNumber {
   protected final short value;

   public NBTShort(short value) {
      this.value = value;
   }

   @Override
   public NBTType<NBTShort> getType() {
      return NBTType.SHORT;
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
      return this.value;
   }

   @Override
   public int getAsInt() {
      return this.value;
   }

   @Override
   public long getAsLong() {
      return this.value;
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
         NBTShort other = (NBTShort)obj;
         return this.value == other.value;
      }
   }

   @Override
   public int hashCode() {
      return Short.hashCode(this.value);
   }

   public NBTShort copy() {
      return this;
   }

   @Override
   public String toString() {
      return "Short(" + this.value + ")";
   }
}
