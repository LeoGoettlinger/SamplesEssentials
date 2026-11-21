package com.viaversion.viaversion.api.type;

import com.viaversion.viaversion.api.minecraft.codec.Ops;
import io.netty.buffer.ByteBuf;
import org.checkerframework.checker.nullness.qual.Nullable;

public class OptionalType<T> extends Type<T> {
   private final Type<T> type;

   public OptionalType(Type<T> type) {
      super(type.getOutputClass());
      this.type = type;
   }

   @Nullable
   @Override
   public T read(ByteBuf buffer) {
      return buffer.readBoolean() ? this.type.read(buffer) : null;
   }

   @Override
   public void write(ByteBuf buffer, @Nullable T value) {
      if (value == null) {
         buffer.writeBoolean(false);
      } else {
         buffer.writeBoolean(true);
         this.type.write(buffer, value);
      }
   }

   @Override
   public void write(Ops ops, T value) {
      this.type.write(ops, value);
   }
}
