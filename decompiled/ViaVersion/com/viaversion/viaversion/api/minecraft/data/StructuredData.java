package com.viaversion.viaversion.api.minecraft.data;

import com.viaversion.viaversion.util.Copyable;
import com.viaversion.viaversion.util.IdHolder;
import io.netty.buffer.ByteBuf;
import org.checkerframework.checker.nullness.qual.Nullable;

public interface StructuredData<T> extends IdHolder, Copyable {
   static <T> StructuredData<T> of(StructuredDataKey<T> key, T value, int id) {
      return new FilledStructuredData<>(key, value, id);
   }

   static <T> StructuredData<T> empty(StructuredDataKey<T> key, int id) {
      return new EmptyStructuredData<>(key, id);
   }

   @Nullable
   T value();

   void setValue(T var1);

   void setId(int var1);

   StructuredDataKey<T> key();

   StructuredData<T> copy();

   default boolean isPresent() {
      return !this.isEmpty();
   }

   boolean isEmpty();

   void write(ByteBuf var1);
}
