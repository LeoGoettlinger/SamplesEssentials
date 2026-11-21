package com.artillexstudios.axafkzone.libs.axapi.utils.mutable;

import java.util.Objects;

public final class MutableObject<T> implements Mutable<T> {
   private T value;

   public MutableObject() {
   }

   public MutableObject(T value) {
      this.value = value;
   }

   @Override
   public T get() {
      return this.value;
   }

   @Override
   public void set(T value) {
      this.value = value;
   }

   @Override
   public boolean equals(Object object) {
      return object instanceof MutableObject<?> that ? Objects.equals(this.value, that.value) : false;
   }

   @Override
   public int hashCode() {
      return Objects.hashCode(this.value);
   }
}
