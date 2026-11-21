package com.earth2me.essentials.libs.configurate.serialize;

import com.earth2me.essentials.libs.configurate.util.CheckedFunction;
import java.lang.reflect.Type;
import java.util.function.BiFunction;
import java.util.function.Predicate;

final class FunctionScalarSerializer<T> extends ScalarSerializer<T> {
   private final CheckedFunction<Object, T, SerializationException> deserializer;
   private final BiFunction<T, Predicate<Class<?>>, Object> serializer;

   FunctionScalarSerializer(
      final Type type, final CheckedFunction<Object, T, SerializationException> deserializer, final BiFunction<T, Predicate<Class<?>>, Object> serializer
   ) {
      super(type);
      this.deserializer = deserializer;
      this.serializer = serializer;
   }

   @Override
   public T deserialize(final Type type, final Object obj) throws SerializationException {
      try {
         return this.deserializer.apply(obj);
      } catch (SerializationException var4) {
         var4.initType(type);
         throw var4;
      }
   }

   @Override
   public Object serialize(final T item, final Predicate<Class<?>> typeSupported) {
      return this.serializer.apply(item, typeSupported);
   }
}
