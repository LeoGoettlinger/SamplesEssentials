package net.thenextlvl.nbt.tag;

abstract class NumberTagImpl<T extends Number> extends ValueTagImpl<T> implements NumberTag<T> {
   protected NumberTagImpl(T number) {
      super(number);
   }

   @Override
   public final boolean isNumber() {
      return true;
   }

   @Override
   public boolean getAsBoolean() {
      return this.value.byteValue() == 1;
   }

   @Override
   public Number getAsNumber() {
      return this.value;
   }
}
