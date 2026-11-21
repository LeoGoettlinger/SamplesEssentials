package com.viaversion.viaversion.api.type.types;

import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.util.Either;
import io.netty.buffer.ByteBuf;

public class EitherType<T, V> extends Type<Either<T, V>> {
   private final Type<T> leftType;
   private final Type<V> rightType;

   public EitherType(Type<T> leftType, Type<V> rightType) {
      super(Either.class);
      this.leftType = leftType;
      this.rightType = rightType;
   }

   public Either<T, V> read(ByteBuf buffer) {
      return read(buffer, this.leftType, this.rightType);
   }

   public void write(ByteBuf buffer, Either<T, V> value) {
      write(buffer, value, this.leftType, this.rightType);
   }

   public static <X, Y> Either<X, Y> read(ByteBuf buf, Type<X> leftType, Type<Y> rightType) {
      return buf.readBoolean() ? Either.left(leftType.read(buf)) : Either.right(rightType.read(buf));
   }

   public static <X, Y> void write(ByteBuf buf, Either<X, Y> value, Type<X> leftType, Type<Y> rightType) {
      if (value.isLeft()) {
         buf.writeBoolean(true);
         leftType.write(buf, value.left());
      } else {
         buf.writeBoolean(false);
         rightType.write(buf, value.right());
      }
   }
}
