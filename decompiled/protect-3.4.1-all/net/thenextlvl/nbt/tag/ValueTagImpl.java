package net.thenextlvl.nbt.tag;

import java.util.Objects;

abstract class ValueTagImpl<T> implements ValueTag<T> {
   protected final T value;

   protected ValueTagImpl(T value) {
      this.value = value;
   }

   @Override
   public T getValue() {
      return this.value;
   }

   @Override
   public boolean equals(Object object) {
      if (this == object) {
         return true;
      } else if (object != null && this.getClass() == object.getClass()) {
         ValueTagImpl<?> valueTag = (ValueTagImpl<?>)object;
         return Objects.equals(this.value, valueTag.value);
      } else {
         return false;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.value);
   }

   @Override
   public String toString() {
      return String.valueOf(this.value);
   }
}
