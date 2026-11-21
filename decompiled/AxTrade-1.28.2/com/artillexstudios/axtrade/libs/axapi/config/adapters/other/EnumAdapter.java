package com.artillexstudios.axtrade.libs.axapi.config.adapters.other;

import com.artillexstudios.axtrade.libs.axapi.config.adapters.TypeAdapter;
import com.artillexstudios.axtrade.libs.axapi.config.adapters.TypeAdapterHolder;
import com.artillexstudios.axtrade.libs.axapi.utils.UncheckedUtils;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;

public final class EnumAdapter implements TypeAdapter<Enum<?>, String> {
   public Enum<?> deserialize(TypeAdapterHolder holder, Object input, Type type) {
      if (input instanceof Enum<?> e) {
         return e;
      } else if (input instanceof String st && type instanceof Class<?> cl && Enum.class.isAssignableFrom(cl)) {
         Class<? extends Enum<?>> clazz = UncheckedUtils.unsafeCast(cl);

         for (Enum<?> enumConstant : clazz.getEnumConstants()) {
            if (enumConstant.name().equalsIgnoreCase(st)) {
               return enumConstant;
            }
         }

         throw new IllegalArgumentException(
            "No enum constant with the name "
               + st
               + " was found! Possible constants: "
               + Arrays.toString(Arrays.stream(clazz.getEnumConstants()).map(Enum::name).toArray())
         );
      } else {
         throw new IllegalArgumentException();
      }
   }

   public String serialize(TypeAdapterHolder holder, Enum<?> value, Type type) {
      return value.name();
   }

   @Override
   public List<String> values(Type type) {
      return type instanceof Class<?> clazz ? Arrays.stream(clazz.getDeclaringClass().getEnumConstants()).map(en -> ((Enum)en).name()).toList() : List.of();
   }
}
