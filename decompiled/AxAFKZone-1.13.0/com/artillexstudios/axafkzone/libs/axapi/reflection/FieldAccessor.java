package com.artillexstudios.axafkzone.libs.axapi.reflection;

import com.artillexstudios.axafkzone.libs.axapi.reflection.accessor.AccessorType;
import com.artillexstudios.axafkzone.libs.axapi.utils.UncheckedUtils;
import com.artillexstudios.axafkzone.libs.axapi.utils.logging.LogUtils;
import java.lang.reflect.Field;
import java.util.Arrays;

public interface FieldAccessor {
   static FieldAccessor.Builder builder() {
      return new FieldAccessor.Builder();
   }

   void set(Object instance, Object argument);

   void setVolatile(Object instance, Object argument);

   Object get(Object instance);

   default <T> T get(Object instance, Class<T> clazz) {
      try {
         return clazz.cast(this.get(instance));
      } catch (ClassCastException var4) {
         LogUtils.error("Failed to get field due to mismatching type!", var4);
         throw var4;
      }
   }

   default <T> T getUnchecked(Object instance) {
      return UncheckedUtils.unsafeCast(this.get(instance));
   }

   Object getVolatile(Object instance);

   default <T> T getVolatile(Object instance, Class<T> clazz) {
      return clazz.cast(this.getVolatile(instance));
   }

   default <T> T getVolatileUnchecked(Object instance) {
      return UncheckedUtils.unsafeCast(this.getVolatile(instance));
   }

   Field getReflectedField();

   public static class Builder {
      private AccessorType type = AccessorType.FIELD;
      private Class<?> clazz = null;
      private Field field = null;
      private String fieldName = null;
      private Class<?> fieldType = null;
      private Integer fieldIndex = null;
      private boolean silent = false;
      private boolean disableAccessChecking = false;

      public FieldAccessor.Builder methodHandle() {
         this.type = AccessorType.METHOD_HANDLE;
         return this;
      }

      public FieldAccessor.Builder varHandle() {
         this.type = AccessorType.VAR_HANDLE;
         return this;
      }

      public FieldAccessor.Builder reflect() {
         this.type = AccessorType.FIELD;
         return this;
      }

      public FieldAccessor.Builder silent() {
         this.silent = true;
         return this;
      }

      public FieldAccessor.Builder withClass(Class<?> clazz) {
         this.clazz = clazz;
         this.tryFetchField();
         return this;
      }

      public FieldAccessor.Builder withClass(String clazz) {
         try {
            this.clazz = Class.forName(clazz);
         } catch (ClassNotFoundException var3) {
            this.clazz = null;
         }

         this.tryFetchField();
         return this;
      }

      public FieldAccessor.Builder withField(Field field) {
         this.field = field;
         return this;
      }

      public FieldAccessor.Builder withField(String field) {
         this.fieldName = field;
         this.tryFetchField();
         return this;
      }

      public FieldAccessor.Builder withFieldType(Class<?> type, int index) {
         this.fieldType = type;
         this.fieldIndex = index;
         this.tryFetchField();
         return this;
      }

      public FieldAccessor.Builder disableAccessChecking() {
         this.disableAccessChecking = true;
         return this;
      }

      private void tryFetchField() {
         if (this.clazz != null) {
            if (this.fieldName != null) {
               this.fetchByName();
            } else if (this.fieldType != null) {
               this.fetchByType();
            }

            if (this.field != null) {
               if (!this.disableAccessChecking) {
                  this.field.setAccessible(true);
               }

               this.fieldType = null;
               this.fieldName = null;
               this.fieldIndex = null;
            }
         }
      }

      private void fetchByName() {
         try {
            this.field = this.clazz.getDeclaredField(this.fieldName);
         } catch (NoSuchFieldException var2) {
            if (!this.silent) {
               LogUtils.error(
                  "Failed to find field {} of class {}! Fields of class: {}",
                  this.fieldName,
                  this.clazz.getName(),
                  String.join(", ", Arrays.stream(this.clazz.getDeclaredFields()).map(Field::getName).toList()),
                  var2
               );
               throw new RuntimeException(var2);
            }
         }
      }

      private void fetchByType() {
         Field[] fields = Arrays.stream(this.clazz.getDeclaredFields()).filter(field -> field.getType().equals(this.fieldType)).toArray(Field[]::new);
         if (this.fieldIndex >= fields.length) {
            LogUtils.error(
               "Failed to find field with type {} and index {} of class {}! Fields of class: {}",
               this.fieldType,
               this.fieldIndex,
               this.clazz.getName(),
               String.join(", ", Arrays.stream(this.clazz.getDeclaredFields()).map(Field::getName).toList())
            );
         } else {
            this.field = fields[this.fieldIndex];
         }
      }

      public FieldAccessor build() {
         if (this.field == null) {
            return null;
         } else {
            try {
               return this.type.createNew(this.field);
            } catch (IllegalAccessException var2) {
               if (this.silent) {
                  return null;
               } else {
                  throw new RuntimeException(var2);
               }
            }
         }
      }
   }
}
