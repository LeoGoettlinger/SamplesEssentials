package com.artillexstudios.axtrade.libs.axapi.config.adapters.other;

import com.artillexstudios.axtrade.libs.axapi.config.YamlConfiguration;
import com.artillexstudios.axtrade.libs.axapi.config.adapters.TypeAdapter;
import com.artillexstudios.axtrade.libs.axapi.config.adapters.TypeAdapterHolder;
import com.artillexstudios.axtrade.libs.axapi.config.annotation.ConfigurationPart;
import com.artillexstudios.axtrade.libs.axapi.config.annotation.Hidden;
import com.artillexstudios.axtrade.libs.axapi.config.annotation.Ignored;
import com.artillexstudios.axtrade.libs.axapi.config.annotation.PostProcess;
import com.artillexstudios.axtrade.libs.axapi.config.annotation.Serializable;
import com.artillexstudios.axtrade.libs.axapi.utils.UncheckedUtils;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.LinkedHashMap;
import java.util.Map;

public final class ObjectAdapter<T extends ConfigurationPart> implements TypeAdapter<Object, Map<String, Object>> {
   private final YamlConfiguration.Builder<T> configuration;

   public ObjectAdapter(YamlConfiguration.Builder<T> configuration) {
      this.configuration = configuration;
   }

   @Override
   public Object deserialize(TypeAdapterHolder holder, Object input, Type type) {
      if (input.getClass().isAnnotationPresent(Serializable.class)) {
         return input;
      } else if (input instanceof Map<?, ?> map) {
         Map<String, Object> castMap = UncheckedUtils.unsafeCast(map);

         try {
            Object instance = ((Class)type).getDeclaredConstructor().newInstance();

            for (Field field : instance.getClass().getFields()) {
               if (!Modifier.isFinal(field.getModifiers()) && !field.isAnnotationPresent(Ignored.class)) {
                  Object found = castMap.get(this.configuration.keyRenamer().rename(field.getName()));
                  if (found == null) {
                     if (!field.isAnnotationPresent(Hidden.class)) {
                        castMap.put(this.configuration.keyRenamer().rename(field.getName()), field.get(instance));
                     }
                  } else {
                     field.set(instance, holder.deserialize(found, field.getGenericType()));
                  }
               }
            }

            for (Method method : ((Class)type).getMethods()) {
               if (method.isAnnotationPresent(PostProcess.class)) {
                  try {
                     method.invoke(instance);
                  } catch (InvocationTargetException var12) {
                     throw new RuntimeException(var12);
                  }
               }
            }

            return instance;
         } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException | InstantiationException var13) {
            throw new RuntimeException(var13);
         }
      } else {
         throw new RuntimeException();
      }
   }

   public Map<String, Object> serialize(TypeAdapterHolder holder, Object value, Type type) {
      Map<String, Object> map = new LinkedHashMap<>();

      try {
         for (Field field : value.getClass().getFields()) {
            if (!Modifier.isFinal(field.getModifiers()) && !field.isAnnotationPresent(Ignored.class)) {
               Object object = field.get(value);
               if (!field.isAnnotationPresent(Hidden.class) || object != null) {
                  map.put(this.configuration.keyRenamer().rename(field.getName()), holder.serialize(object, field.getGenericType()));
               }
            }
         }

         return map;
      } catch (IllegalAccessException var10) {
         throw new RuntimeException(var10);
      }
   }
}
