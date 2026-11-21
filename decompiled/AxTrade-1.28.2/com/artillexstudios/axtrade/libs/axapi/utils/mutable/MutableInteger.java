package com.artillexstudios.axtrade.libs.axapi.utils.mutable;

public final class MutableInteger implements Mutable<Integer> {
   private int value;

   public MutableInteger() {
   }

   public MutableInteger(Integer value) {
      this.value = value;
   }

   public MutableInteger(int value) {
      this.value = value;
   }

   public Integer get() {
      return this.value;
   }

   public void set(int value) {
      this.value = value;
   }

   public void set(Integer value) {
      this.value = value;
   }

   public int intValue() {
      return this.value;
   }

   public void increment(int amount) {
      this.value += amount;
   }

   public void increment() {
      this.value++;
   }

   public int incrementAndGet() {
      this.value++;
      return this.value;
   }

   public int getAndIncrement() {
      return this.value++;
   }

   public void decrement(int amount) {
      this.value -= amount;
   }

   public void decrement() {
      this.value--;
   }

   public int decrementAndGet() {
      this.value--;
      return this.value;
   }

   public int getAndDecrement() {
      return this.value--;
   }

   @Override
   public boolean equals(Object object) {
      return object instanceof MutableInteger that ? this.value == that.value : false;
   }

   @Override
   public int hashCode() {
      return Integer.hashCode(this.value);
   }
}
