package com.earth2me.essentials.libs.configurate.serialize;

import com.earth2me.essentials.libs.configurate.util.EnumLookup;
import com.earth2me.essentials.libs.geantyref.GenericTypeReflector;
import com.earth2me.essentials.libs.geantyref.TypeToken;
import java.lang.reflect.Type;
import java.util.function.Predicate;

final class EnumValueSerializer extends ScalarSerializer<Enum<?>> {
   EnumValueSerializer() {
      super(new TypeToken<Enum<?>>() {});
   }

   public Enum<?> deserialize(final Type type, final Object obj) throws SerializationException {
      String enumConstant = obj.toString();
      Enum<?> ret = EnumLookup.lookupEnum((Class<Enum<?>>)GenericTypeReflector.erase(type).asSubclass(Enum.class), enumConstant);
      if (ret == null) {
         throw new SerializationException(type, "Invalid enum constant provided, expected a value of enum, got " + enumConstant);
      } else {
         return ret;
      }
   }

   public Object serialize(final Enum<?> item, final Predicate<Class<?>> typeSupported) {
      return item.name();
   }
}
