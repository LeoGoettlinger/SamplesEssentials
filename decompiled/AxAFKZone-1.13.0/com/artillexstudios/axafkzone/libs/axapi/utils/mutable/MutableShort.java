package com.artillexstudios.axafkzone.libs.axapi.utils.mutable;

public final class MutableShort implements Mutable<Short> {
   private short value;

   public MutableShort() {
   }

   public MutableShort(Short value) {
      this.value = value;
   }

   public MutableShort(short value) {
      this.value = value;
   }

   public Short get() {
      return this.value;
   }

   public void set(short value) {
      this.value = value;
   }

   public void set(Short value) {
      this.value = value;
   }

   public short shortValue() {
      return this.value;
   }

   @Override
   public boolean equals(Object object) {
      return object instanceof MutableShort that ? this.value == that.value : false;
   }

   @Override
   public int hashCode() {
      return Short.hashCode(this.value);
   }
}
