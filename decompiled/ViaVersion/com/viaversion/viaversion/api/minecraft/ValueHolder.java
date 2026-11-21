package com.viaversion.viaversion.api.minecraft;

import com.viaversion.viaversion.libs.fastutil.ints.Int2IntFunction;
import java.util.function.Function;
import java.util.function.Supplier;

record ValueHolder<T>(T value) implements Holder<T> {
   @Override
   public boolean isDirect() {
      return true;
   }

   @Override
   public boolean hasId() {
      return false;
   }

   @Override
   public int id() {
      return -1;
   }

   @Override
   public Holder<T> updateId(Int2IntFunction rewriteFunction, Supplier<Holder<T>> ifMissing) {
      return this;
   }

   @Override
   public Holder<T> updateValue(Function<T, T> rewriteFunction) {
      T rewrittenValue = rewriteFunction.apply(this.value);
      return (Holder<T>)(rewrittenValue != this.value ? Holder.of(rewrittenValue) : this);
   }
}
