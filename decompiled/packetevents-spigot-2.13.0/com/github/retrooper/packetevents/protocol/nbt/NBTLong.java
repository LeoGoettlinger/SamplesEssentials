package com.github.retrooper.packetevents.protocol.nbt;

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
public class NBTLong extends NBTNumber {
   protected final long value;

   public NBTLong(long value) {
      this.value = value;
   }

   @Override
   public NBTType<NBTLong> getType() {
      return NBTType.LONG;
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
      return this.value;
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
   public boolean equals(@Nullable Object obj) {
      if (this == obj) {
         return true;
      } else if (obj == null) {
         return false;
      } else if (this.getClass() != obj.getClass()) {
         return false;
      } else {
         NBTLong other = (NBTLong)obj;
         return this.value == other.value;
      }
   }

   @Override
   public int hashCode() {
      return Long.hashCode(this.value);
   }

   public NBTLong copy() {
      return this;
   }

   @Override
   public String toString() {
      return "Long(" + this.value + ")";
   }
}
