package com.viaversion.viaversion.api.type.types.misc;

import com.viaversion.viaversion.api.minecraft.EitherHolder;
import com.viaversion.viaversion.api.minecraft.codec.Ops;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.util.Key;
import io.netty.buffer.ByteBuf;

public final class EitherHolderType<T> extends Type<EitherHolder<T>> {
   private final HolderType<T> holderType;

   public EitherHolderType(HolderType<T> holderType) {
      super(EitherHolder.class);
      this.holderType = holderType;
   }

   public EitherHolder<T> read(ByteBuf buffer) {
      return read(buffer, this.holderType);
   }

   public void write(ByteBuf buffer, EitherHolder<T> object) {
      write(buffer, object, this.holderType);
   }

   public void write(Ops ops, EitherHolder<T> value) {
      write(ops, value, this.holderType);
   }

   public static <T> EitherHolder<T> read(ByteBuf buffer, HolderType<T> holderType) {
      return buffer.readBoolean() ? EitherHolder.of(holderType.read(buffer)) : EitherHolder.of(Types.STRING.read(buffer));
   }

   public static <T> void write(ByteBuf buffer, EitherHolder<T> object, HolderType<T> holderType) {
      if (object.hasHolder()) {
         buffer.writeBoolean(true);
         holderType.write(buffer, object.holder());
      } else {
         buffer.writeBoolean(false);
         Types.STRING.write(buffer, object.key());
      }
   }

   public static <T> void write(Ops ops, EitherHolder<T> object, HolderType<T> holderType) {
      if (object.hasHolder()) {
         holderType.write(ops, object.holder());
      } else {
         ops.write(Types.IDENTIFIER, Key.of(object.key()));
      }
   }
}
