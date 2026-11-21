package com.artillexstudios.axafkzone.libs.axapi.database.handler;

import com.artillexstudios.axafkzone.libs.axapi.config.adapters.TypeAdapterHolder;
import com.artillexstudios.axafkzone.libs.axapi.database.ResultHandler;
import com.artillexstudios.axafkzone.libs.axapi.utils.UncheckedUtils;
import com.artillexstudios.axafkzone.libs.axapi.utils.featureflags.FeatureFlags;
import com.artillexstudios.axafkzone.libs.axapi.utils.logging.LogUtils;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.sql.ResultSet;
import java.sql.SQLException;

public class TransformerHandler<T> implements ResultHandler<T> {
   private final TypeAdapterHolder holder;
   private final Class<T> clazz;

   public TransformerHandler(Class<T> clazz) {
      this(clazz, new TypeAdapterHolder());
   }

   public TransformerHandler(Class<T> clazz, TypeAdapterHolder typeAdapterHolder) {
      this.clazz = clazz;
      this.holder = typeAdapterHolder;
   }

   @Override
   public T handle(ResultSet resultSet, boolean checkNext) throws SQLException {
      if (checkNext && !resultSet.next()) {
         return null;
      } else {
         int columnCount = resultSet.getMetaData().getColumnCount();
         Object[] objects = new Object[columnCount];

         for (int i = 0; i < columnCount; i++) {
            objects[i] = resultSet.getObject(i + 1);
         }

         try {
            Object[] deserialized = new Object[columnCount];

            label58:
            for (Constructor<?> declaredConstructor : this.clazz.getDeclaredConstructors()) {
               if (declaredConstructor.getParameterCount() == columnCount) {
                  Class<?>[] parameterTypes = declaredConstructor.getParameterTypes();

                  for (int i = 0; i < parameterTypes.length; i++) {
                     Class<?> parameterType = parameterTypes[i];
                     Object object = objects[i];
                     if (object != null && parameterType.isAssignableFrom(object.getClass())) {
                        deserialized[i] = object;
                     } else {
                        try {
                           deserialized[i] = this.holder.deserialize(object, parameterType);
                        } catch (Exception var15) {
                           if (FeatureFlags.DEBUG.get()) {
                              LogUtils.info("Failed to convert {} into {}!", object, parameterType);
                           }
                           continue label58;
                        }
                     }
                  }

                  return UncheckedUtils.unsafeCast(declaredConstructor.newInstance(deserialized));
               }
            }

            return null;
         } catch (IllegalAccessException | InvocationTargetException | InstantiationException var16) {
            throw new RuntimeException(var16);
         }
      }
   }
}
