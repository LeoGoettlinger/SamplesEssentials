package com.viaversion.viaversion.api.type.types;

import com.viaversion.viaversion.api.minecraft.codec.Ops;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.api.type.Types;
import io.netty.buffer.ByteBuf;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

public class ArrayType<T> extends Type<T[]> {
   protected final Type<T> elementType;
   private final int maxLength;

   public ArrayType(Type<T> type) {
      this(type, -1);
   }

   public ArrayType(Type<T> type, int maxLength) {
      super(type.getTypeName() + " Array", (Class<? super T[]>)getArrayClass(type.getOutputClass()));
      this.elementType = type;
      this.maxLength = maxLength;
   }

   private ArrayType(Class<? super T[]> outputClass, Type<T> type, int maxLength) {
      super(type.getTypeName() + " Array", outputClass);
      this.elementType = type;
      this.maxLength = maxLength;
   }

   public static Class<?> getArrayClass(Class<?> componentType) {
      return Array.newInstance(componentType, 0).getClass();
   }

   public T[] read(ByteBuf buffer) {
      int amount = Types.VAR_INT.readPrimitive(buffer);
      if (this.maxLength != -1 && amount > this.maxLength) {
         throw new IllegalArgumentException("Array length " + amount + " is longer than maximum " + this.maxLength);
      } else {
         return amount < 32767 ? this.readArray(buffer, amount) : this.readList(buffer, amount);
      }
   }

   protected T[] readArray(ByteBuf buffer, int length) {
      T[] array = this.createArray(length);

      for (int i = 0; i < length; i++) {
         array[i] = this.elementType.read(buffer);
      }

      return array;
   }

   private T[] readList(ByteBuf buffer, int length) {
      List<T> list = new ArrayList<>();

      for (int i = 0; i < length; i++) {
         list.add(this.elementType.read(buffer));
      }

      return (T[])list.toArray(this.createArray(0));
   }

   private T[] createArray(int length) {
      return (T[])((Object[])Array.newInstance(this.elementType.getOutputClass(), length));
   }

   public void write(ByteBuf buffer, T[] object) {
      if (this.maxLength != -1 && object.length > this.maxLength) {
         throw new IllegalArgumentException("Array length " + object.length + " is longer than maximum " + this.maxLength);
      } else {
         Types.VAR_INT.writePrimitive(buffer, object.length);

         for (T o : object) {
            this.elementType.write(buffer, o);
         }
      }
   }

   public void write(Ops ops, T[] value) {
      ops.writeList(list -> {
         for (T element : value) {
            list.write(this.elementType, element);
         }
      });
   }

   public ArrayType<T> withMaxLength(int maxLength) {
      return new ArrayType<>(this.getOutputClass(), this.elementType, maxLength);
   }

   static int checkLength(int length, int elementBytes, int readable) {
      if (length < 0) {
         throw new IllegalArgumentException("Length " + length + " is negative");
      } else if ((long)length * elementBytes > readable) {
         throw new IllegalArgumentException("Length " + length + " is larger than readable bytes");
      } else {
         return length;
      }
   }
}
