package org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.deser;

import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.BeanDescription;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.DeserializationConfig;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.JavaType;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.JsonDeserializer;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.JsonMappingException;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.JsonNode;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.KeyDeserializer;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.jsontype.TypeDeserializer;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.type.ArrayType;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.type.CollectionLikeType;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.type.CollectionType;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.type.MapLikeType;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.type.MapType;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.type.ReferenceType;

public interface Deserializers {
   JsonDeserializer<?> findEnumDeserializer(Class<?> var1, DeserializationConfig var2, BeanDescription var3) throws JsonMappingException;

   JsonDeserializer<?> findTreeNodeDeserializer(Class<? extends JsonNode> var1, DeserializationConfig var2, BeanDescription var3) throws JsonMappingException;

   JsonDeserializer<?> findBeanDeserializer(JavaType var1, DeserializationConfig var2, BeanDescription var3) throws JsonMappingException;

   JsonDeserializer<?> findReferenceDeserializer(
      ReferenceType var1, DeserializationConfig var2, BeanDescription var3, TypeDeserializer var4, JsonDeserializer<?> var5
   ) throws JsonMappingException;

   JsonDeserializer<?> findArrayDeserializer(ArrayType var1, DeserializationConfig var2, BeanDescription var3, TypeDeserializer var4, JsonDeserializer<?> var5) throws JsonMappingException;

   JsonDeserializer<?> findCollectionDeserializer(
      CollectionType var1, DeserializationConfig var2, BeanDescription var3, TypeDeserializer var4, JsonDeserializer<?> var5
   ) throws JsonMappingException;

   JsonDeserializer<?> findCollectionLikeDeserializer(
      CollectionLikeType var1, DeserializationConfig var2, BeanDescription var3, TypeDeserializer var4, JsonDeserializer<?> var5
   ) throws JsonMappingException;

   JsonDeserializer<?> findMapDeserializer(
      MapType var1, DeserializationConfig var2, BeanDescription var3, KeyDeserializer var4, TypeDeserializer var5, JsonDeserializer<?> var6
   ) throws JsonMappingException;

   JsonDeserializer<?> findMapLikeDeserializer(
      MapLikeType var1, DeserializationConfig var2, BeanDescription var3, KeyDeserializer var4, TypeDeserializer var5, JsonDeserializer<?> var6
   ) throws JsonMappingException;

   default boolean hasDeserializerFor(DeserializationConfig config, Class<?> valueType) {
      return false;
   }

   public abstract static class Base implements Deserializers {
      @Override
      public JsonDeserializer<?> findEnumDeserializer(Class<?> type, DeserializationConfig config, BeanDescription beanDesc) throws JsonMappingException {
         return null;
      }

      @Override
      public JsonDeserializer<?> findTreeNodeDeserializer(Class<? extends JsonNode> nodeType, DeserializationConfig config, BeanDescription beanDesc) throws JsonMappingException {
         return null;
      }

      @Override
      public JsonDeserializer<?> findReferenceDeserializer(
         ReferenceType refType,
         DeserializationConfig config,
         BeanDescription beanDesc,
         TypeDeserializer contentTypeDeserializer,
         JsonDeserializer<?> contentDeserializer
      ) throws JsonMappingException {
         return null;
      }

      @Override
      public JsonDeserializer<?> findBeanDeserializer(JavaType type, DeserializationConfig config, BeanDescription beanDesc) throws JsonMappingException {
         return null;
      }

      @Override
      public JsonDeserializer<?> findArrayDeserializer(
         ArrayType type,
         DeserializationConfig config,
         BeanDescription beanDesc,
         TypeDeserializer elementTypeDeserializer,
         JsonDeserializer<?> elementDeserializer
      ) throws JsonMappingException {
         return null;
      }

      @Override
      public JsonDeserializer<?> findCollectionDeserializer(
         CollectionType type,
         DeserializationConfig config,
         BeanDescription beanDesc,
         TypeDeserializer elementTypeDeserializer,
         JsonDeserializer<?> elementDeserializer
      ) throws JsonMappingException {
         return null;
      }

      @Override
      public JsonDeserializer<?> findCollectionLikeDeserializer(
         CollectionLikeType type,
         DeserializationConfig config,
         BeanDescription beanDesc,
         TypeDeserializer elementTypeDeserializer,
         JsonDeserializer<?> elementDeserializer
      ) throws JsonMappingException {
         return null;
      }

      @Override
      public JsonDeserializer<?> findMapDeserializer(
         MapType type,
         DeserializationConfig config,
         BeanDescription beanDesc,
         KeyDeserializer keyDeserializer,
         TypeDeserializer elementTypeDeserializer,
         JsonDeserializer<?> elementDeserializer
      ) throws JsonMappingException {
         return null;
      }

      @Override
      public JsonDeserializer<?> findMapLikeDeserializer(
         MapLikeType type,
         DeserializationConfig config,
         BeanDescription beanDesc,
         KeyDeserializer keyDeserializer,
         TypeDeserializer elementTypeDeserializer,
         JsonDeserializer<?> elementDeserializer
      ) throws JsonMappingException {
         return null;
      }
   }
}
