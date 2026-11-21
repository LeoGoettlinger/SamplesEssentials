package com.viaversion.viaversion.api.minecraft.data.predicate;

import com.viaversion.nbt.tag.Tag;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.api.type.types.ArrayType;
import io.netty.buffer.ByteBuf;

public record DataComponentPredicate(DataComponentPredicate.PredicateType type, Tag predicate) {
   public static final Type<DataComponentPredicate> TYPE1_21_5 = new Type<DataComponentPredicate>(DataComponentPredicate.class) {
      public DataComponentPredicate read(ByteBuf buffer) {
         int id = Types.VAR_INT.readPrimitive(buffer);
         Tag predicate = Types.TAG.read(buffer);
         return new DataComponentPredicate(id, predicate);
      }

      public void write(ByteBuf buffer, DataComponentPredicate value) {
         Types.VAR_INT.writePrimitive(buffer, value.type().id());
         Types.TAG.write(buffer, value.predicate());
      }
   };
   public static final Type<DataComponentPredicate[]> ARRAY_TYPE1_21_5 = new ArrayType<>(TYPE1_21_5, 64);
   public static final Type<DataComponentPredicate> TYPE1_21_11 = new Type<DataComponentPredicate>(DataComponentPredicate.class) {
      public DataComponentPredicate read(ByteBuf buffer) {
         boolean isPredicateType = Types.BOOLEAN.read(buffer);
         DataComponentPredicate.PredicateType type = new DataComponentPredicate.PredicateType(Types.VAR_INT.readPrimitive(buffer), isPredicateType);
         Tag predicate = Types.TAG.read(buffer);
         return new DataComponentPredicate(type, predicate);
      }

      public void write(ByteBuf buffer, DataComponentPredicate value) {
         Types.BOOLEAN.write(buffer, value.type().isPredicateType());
         Types.VAR_INT.writePrimitive(buffer, value.type().id());
         Types.TAG.write(buffer, value.predicate());
      }
   };
   public static final Type<DataComponentPredicate[]> ARRAY_TYPE1_21_11 = new ArrayType<>(TYPE1_21_11, 64);

   public DataComponentPredicate(int predicateType, Tag predicate) {
      this(DataComponentPredicate.PredicateType.ofPredicateType(predicateType), predicate);
   }

   public record PredicateType(int id, boolean isPredicateType) {
      public static DataComponentPredicate.PredicateType ofPredicateType(int id) {
         return new DataComponentPredicate.PredicateType(id, true);
      }

      public static DataComponentPredicate.PredicateType ofDataComponentType(int id) {
         return new DataComponentPredicate.PredicateType(id, false);
      }
   }
}
