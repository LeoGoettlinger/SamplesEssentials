package com.magmaguy.elitemobs.magmacore.ai;

import java.util.UUID;

public interface MindValueCodec<T> {
   MindValueCodec<String> STRING = scalar(String.class);
   MindValueCodec<Boolean> BOOLEAN = scalar(Boolean.class);
   MindValueCodec<Long> LONG = new MindValueCodec<Long>() {
      public Object encode(Long value) {
         return value;
      }

      public Long decode(Object value) {
         if (value instanceof Number number) {
            return number.longValue();
         } else {
            throw new IllegalArgumentException("Expected a numeric persisted value");
         }
      }
   };
   MindValueCodec<Double> DOUBLE = new MindValueCodec<Double>() {
      public Object encode(Double value) {
         return value;
      }

      public Double decode(Object value) {
         if (value instanceof Number number) {
            return number.doubleValue();
         } else {
            throw new IllegalArgumentException("Expected a numeric persisted value");
         }
      }
   };
   MindValueCodec<UUID> UUID = new MindValueCodec<UUID>() {
      public Object encode(UUID value) {
         return value.toString();
      }

      public UUID decode(Object value) {
         if (value instanceof String string) {
            return java.util.UUID.fromString(string);
         } else {
            throw new IllegalArgumentException("Expected a UUID string");
         }
      }
   };

   Object encode(T var1);

   T decode(Object var1);

   static <T> MindValueCodec<T> scalar(final Class<T> type) {
      return new MindValueCodec<T>() {
         @Override
         public Object encode(T value) {
            return value;
         }

         @Override
         public T decode(Object value) {
            if (type.isInstance(value)) {
               return type.cast(value);
            } else {
               throw new IllegalArgumentException("Expected persisted value of type " + type.getSimpleName());
            }
         }
      };
   }
}
