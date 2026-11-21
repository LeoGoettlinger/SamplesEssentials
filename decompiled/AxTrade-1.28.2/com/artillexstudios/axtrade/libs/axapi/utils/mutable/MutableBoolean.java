package com.artillexstudios.axtrade.libs.axapi.utils.mutable;

public final class MutableBoolean implements Mutable<Boolean> {
   private boolean value;

   public MutableBoolean() {
   }

   public MutableBoolean(Boolean value) {
      this.value = value;
   }

   public MutableBoolean(boolean value) {
      this.value = value;
   }

   public Boolean get() {
      return this.value;
   }

   public void set(boolean value) {
      this.value = value;
   }

   public void set(Boolean value) {
      this.value = value;
   }

   public boolean booleanValue() {
      return this.value;
   }

   @Override
   public boolean equals(Object object) {
      return object instanceof MutableBoolean that ? this.value == that.value : false;
   }

   @Override
   public int hashCode() {
      return Boolean.hashCode(this.value);
   }
}
