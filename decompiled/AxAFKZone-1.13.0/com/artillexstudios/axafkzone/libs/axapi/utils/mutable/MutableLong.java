package com.artillexstudios.axafkzone.libs.axapi.utils.mutable;

public final class MutableLong implements Mutable<Long> {
   private long value;

   public MutableLong() {
   }

   public MutableLong(Long value) {
      this.value = value;
   }

   public MutableLong(long value) {
      this.value = value;
   }

   public Long get() {
      return this.value;
   }

   public void set(long value) {
      this.value = value;
   }

   public void set(Long value) {
      this.value = value;
   }

   public long longValue() {
      return this.value;
   }

   @Override
   public boolean equals(Object object) {
      return object instanceof MutableLong that ? this.value == that.value : false;
   }

   @Override
   public int hashCode() {
      return Long.hashCode(this.value);
   }
}
