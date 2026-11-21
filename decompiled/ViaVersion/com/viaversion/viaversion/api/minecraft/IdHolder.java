package com.viaversion.viaversion.api.minecraft;

import com.google.common.base.Preconditions;
import com.viaversion.viaversion.libs.fastutil.ints.Int2IntFunction;
import java.util.function.Function;
import java.util.function.Supplier;

record IdHolder<T>(int id) implements Holder<T> {
   IdHolder(int id) {
      Preconditions.checkArgument(id >= 0, "id cannot be negative");
      this.id = id;
   }

   @Override
   public boolean isDirect() {
      return false;
   }

   @Override
   public boolean hasId() {
      return true;
   }

   @Override
   public T value() {
      throw new IllegalArgumentException("Holder is not direct");
   }

   @Override
   public Holder<T> updateId(Int2IntFunction rewriteFunction, Supplier<Holder<T>> ifMissing) {
      int rewrittenId = rewriteFunction.applyAsInt(this.id);
      if (rewrittenId == this.id) {
         return this;
      } else if (rewrittenId == -1) {
         if (ifMissing != null) {
            return ifMissing.get();
         } else {
            throw new IllegalArgumentException("Received invalid id in updateId");
         }
      } else {
         return Holder.of(rewrittenId);
      }
   }

   @Override
   public Holder<T> updateValue(Function<T, T> rewriteFunction) {
      return this;
   }
}
