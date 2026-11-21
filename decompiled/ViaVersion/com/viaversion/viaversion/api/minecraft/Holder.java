package com.viaversion.viaversion.api.minecraft;

import com.viaversion.viaversion.libs.fastutil.ints.Int2IntFunction;
import java.util.function.Function;
import java.util.function.Supplier;

public interface Holder<T> {
   static <T> Holder<T> of(int id) {
      return new IdHolder<>(id);
   }

   static <T> Holder<T> of(T value) {
      return new ValueHolder<>(value);
   }

   boolean isDirect();

   boolean hasId();

   T value();

   int id();

   default Holder<T> updateId(Int2IntFunction rewriteFunction) {
      return this.updateId(rewriteFunction, null);
   }

   Holder<T> updateId(Int2IntFunction var1, Supplier<Holder<T>> var2);

   Holder<T> updateValue(Function<T, T> var1);
}
