package com.artillexstudios.axafkzone.libs.axapi.reflection.accessor;

import com.artillexstudios.axafkzone.libs.axapi.reflection.FieldAccessor;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;

public class MethodHandleFieldAccessor implements FieldAccessor {
   private final Field field;
   private final MethodHandle getter;
   private final MethodHandle setter;

   public MethodHandleFieldAccessor(Field field) throws IllegalAccessException {
      this.field = field;
      this.getter = MethodHandles.privateLookupIn(field.getDeclaringClass(), MethodHandles.lookup()).unreflectGetter(field);
      this.setter = MethodHandles.privateLookupIn(field.getDeclaringClass(), MethodHandles.lookup()).unreflectSetter(field);
   }

   @Override
   public void set(Object instance, Object argument) {
      try {
         this.setter.invoke((Object)instance, (Object)argument);
      } catch (Throwable var4) {
         throw new RuntimeException(var4);
      }
   }

   @Override
   public void setVolatile(Object instance, Object argument) {
      try {
         this.setter.invoke((Object)instance, (Object)argument);
      } catch (Throwable var4) {
         throw new RuntimeException(var4);
      }
   }

   @Override
   public Object get(Object instance) {
      try {
         return (Object)this.getter.invoke((Object)instance);
      } catch (Throwable var3) {
         throw new RuntimeException(var3);
      }
   }

   @Override
   public Object getVolatile(Object instance) {
      try {
         return (Object)this.getter.invoke((Object)instance);
      } catch (Throwable var3) {
         throw new RuntimeException(var3);
      }
   }

   @Override
   public Field getReflectedField() {
      return this.field;
   }
}
