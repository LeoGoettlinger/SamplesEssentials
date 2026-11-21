package com.artillexstudios.axtrade.libs.axapi.utils.mutable;

public final class MutableByte implements Mutable<Byte> {
   private byte value;

   public MutableByte() {
   }

   public MutableByte(Byte value) {
      this.value = value;
   }

   public MutableByte(byte value) {
      this.value = value;
   }

   public Byte get() {
      return this.value;
   }

   public void set(byte value) {
      this.value = value;
   }

   public void set(Byte value) {
      this.value = value;
   }

   public byte byteValue() {
      return this.value;
   }

   @Override
   public boolean equals(Object object) {
      return object instanceof MutableByte that ? this.value == that.value : false;
   }

   @Override
   public int hashCode() {
      return Byte.hashCode(this.value);
   }
}
