package com.artillexstudios.axrankmenu.libs.axapi.reflection.accessor;

import com.artillexstudios.axrankmenu.libs.axapi.reflection.FieldAccessor;
import java.lang.reflect.Field;

public class ReflectionFieldAccessor implements FieldAccessor {
   private final Field field;

   public ReflectionFieldAccessor(Field field) {
      this.field = field;
   }

   @Override
   public void set(Object instance, Object argument) {
      try {
         this.field.set(instance, argument);
      } catch (IllegalAccessException var4) {
         throw new RuntimeException(var4);
      }
   }

   @Override
   public void setVolatile(Object instance, Object argument) {
      try {
         this.field.set(instance, argument);
      } catch (IllegalAccessException var4) {
         throw new RuntimeException(var4);
      }
   }

   @Override
   public Object get(Object instance) {
      try {
         return this.field.get(instance);
      } catch (IllegalAccessException var3) {
         throw new RuntimeException(var3);
      }
   }

   @Override
   public Object getVolatile(Object instance) {
      try {
         return this.field.get(instance);
      } catch (IllegalAccessException var3) {
         throw new RuntimeException(var3);
      }
   }

   @Override
   public Field getReflectedField() {
      return this.field;
   }
}
