package com.artillexstudios.axafkzone.libs.axapi.reflection.accessor;

import com.artillexstudios.axafkzone.libs.axapi.reflection.FieldAccessor;
import java.lang.reflect.Field;

public enum AccessorType {
   FIELD {
      @Override
      public FieldAccessor createNew(Field field) {
         return new ReflectionFieldAccessor(field);
      }
   },
   VAR_HANDLE {
      @Override
      public FieldAccessor createNew(Field field) throws IllegalAccessException {
         return new VarHandleFieldAccessor(field);
      }
   },
   METHOD_HANDLE {
      @Override
      public FieldAccessor createNew(Field field) throws IllegalAccessException {
         return new MethodHandleFieldAccessor(field);
      }
   };

   public abstract FieldAccessor createNew(Field field) throws IllegalAccessException;
}
