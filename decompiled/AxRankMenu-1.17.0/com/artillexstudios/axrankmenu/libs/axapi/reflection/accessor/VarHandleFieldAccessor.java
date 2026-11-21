package com.artillexstudios.axrankmenu.libs.axapi.reflection.accessor;

import com.artillexstudios.axrankmenu.libs.axapi.reflection.FieldAccessor;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.lang.reflect.Field;

public class VarHandleFieldAccessor implements FieldAccessor {
   private final Field field;
   private final VarHandle handle;

   public VarHandleFieldAccessor(Field field) throws IllegalAccessException {
      this.field = field;
      this.handle = MethodHandles.privateLookupIn(field.getDeclaringClass(), MethodHandles.lookup()).unreflectVarHandle(field);
   }

   @Override
   public void set(Object instance, Object argument) {
      this.handle.set((Object)instance, (Object)argument);
   }

   @Override
   public void setVolatile(Object instance, Object argument) {
      this.handle.setVolatile((Object)instance, (Object)argument);
   }

   @Override
   public Object get(Object instance) {
      return (Object)this.handle.get((Object)instance);
   }

   @Override
   public Object getVolatile(Object instance) {
      return (Object)this.handle.getVolatile((Object)instance);
   }

   @Override
   public Field getReflectedField() {
      return this.field;
   }
}
