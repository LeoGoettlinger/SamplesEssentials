package org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.ser;

import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.BeanDescription;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.JavaType;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.JsonSerializer;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.SerializationConfig;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.jsontype.TypeSerializer;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.type.ArrayType;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.type.CollectionLikeType;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.type.CollectionType;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.type.MapLikeType;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.type.MapType;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.type.ReferenceType;

public interface Serializers {
   JsonSerializer<?> findSerializer(SerializationConfig var1, JavaType var2, BeanDescription var3);

   JsonSerializer<?> findReferenceSerializer(
      SerializationConfig var1, ReferenceType var2, BeanDescription var3, TypeSerializer var4, JsonSerializer<Object> var5
   );

   JsonSerializer<?> findArraySerializer(SerializationConfig var1, ArrayType var2, BeanDescription var3, TypeSerializer var4, JsonSerializer<Object> var5);

   JsonSerializer<?> findCollectionSerializer(
      SerializationConfig var1, CollectionType var2, BeanDescription var3, TypeSerializer var4, JsonSerializer<Object> var5
   );

   JsonSerializer<?> findCollectionLikeSerializer(
      SerializationConfig var1, CollectionLikeType var2, BeanDescription var3, TypeSerializer var4, JsonSerializer<Object> var5
   );

   JsonSerializer<?> findMapSerializer(
      SerializationConfig var1, MapType var2, BeanDescription var3, JsonSerializer<Object> var4, TypeSerializer var5, JsonSerializer<Object> var6
   );

   JsonSerializer<?> findMapLikeSerializer(
      SerializationConfig var1, MapLikeType var2, BeanDescription var3, JsonSerializer<Object> var4, TypeSerializer var5, JsonSerializer<Object> var6
   );

   public static class Base implements Serializers {
      @Override
      public JsonSerializer<?> findSerializer(SerializationConfig config, JavaType type, BeanDescription beanDesc) {
         return null;
      }

      @Override
      public JsonSerializer<?> findReferenceSerializer(
         SerializationConfig config,
         ReferenceType type,
         BeanDescription beanDesc,
         TypeSerializer contentTypeSerializer,
         JsonSerializer<Object> contentValueSerializer
      ) {
         return this.findSerializer(config, type, beanDesc);
      }

      @Override
      public JsonSerializer<?> findArraySerializer(
         SerializationConfig config,
         ArrayType type,
         BeanDescription beanDesc,
         TypeSerializer elementTypeSerializer,
         JsonSerializer<Object> elementValueSerializer
      ) {
         return null;
      }

      @Override
      public JsonSerializer<?> findCollectionSerializer(
         SerializationConfig config,
         CollectionType type,
         BeanDescription beanDesc,
         TypeSerializer elementTypeSerializer,
         JsonSerializer<Object> elementValueSerializer
      ) {
         return null;
      }

      @Override
      public JsonSerializer<?> findCollectionLikeSerializer(
         SerializationConfig config,
         CollectionLikeType type,
         BeanDescription beanDesc,
         TypeSerializer elementTypeSerializer,
         JsonSerializer<Object> elementValueSerializer
      ) {
         return null;
      }

      @Override
      public JsonSerializer<?> findMapSerializer(
         SerializationConfig config,
         MapType type,
         BeanDescription beanDesc,
         JsonSerializer<Object> keySerializer,
         TypeSerializer elementTypeSerializer,
         JsonSerializer<Object> elementValueSerializer
      ) {
         return null;
      }

      @Override
      public JsonSerializer<?> findMapLikeSerializer(
         SerializationConfig config,
         MapLikeType type,
         BeanDescription beanDesc,
         JsonSerializer<Object> keySerializer,
         TypeSerializer elementTypeSerializer,
         JsonSerializer<Object> elementValueSerializer
      ) {
         return null;
      }
   }
}
