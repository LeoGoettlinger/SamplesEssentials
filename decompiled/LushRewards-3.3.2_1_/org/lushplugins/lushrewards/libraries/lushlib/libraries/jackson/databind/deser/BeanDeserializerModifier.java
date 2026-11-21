package org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.deser;

import java.util.List;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.BeanDescription;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.DeserializationConfig;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.JavaType;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.JsonDeserializer;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.KeyDeserializer;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.introspect.BeanPropertyDefinition;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.type.ArrayType;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.type.CollectionLikeType;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.type.CollectionType;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.type.MapLikeType;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.type.MapType;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.type.ReferenceType;

public abstract class BeanDeserializerModifier {
   public List<BeanPropertyDefinition> updateProperties(DeserializationConfig config, BeanDescription beanDesc, List<BeanPropertyDefinition> propDefs) {
      return propDefs;
   }

   public BeanDeserializerBuilder updateBuilder(DeserializationConfig config, BeanDescription beanDesc, BeanDeserializerBuilder builder) {
      return builder;
   }

   public JsonDeserializer<?> modifyDeserializer(DeserializationConfig config, BeanDescription beanDesc, JsonDeserializer<?> deserializer) {
      return deserializer;
   }

   public JsonDeserializer<?> modifyEnumDeserializer(DeserializationConfig config, JavaType type, BeanDescription beanDesc, JsonDeserializer<?> deserializer) {
      return deserializer;
   }

   public JsonDeserializer<?> modifyReferenceDeserializer(
      DeserializationConfig config, ReferenceType type, BeanDescription beanDesc, JsonDeserializer<?> deserializer
   ) {
      return deserializer;
   }

   public JsonDeserializer<?> modifyArrayDeserializer(
      DeserializationConfig config, ArrayType valueType, BeanDescription beanDesc, JsonDeserializer<?> deserializer
   ) {
      return deserializer;
   }

   public JsonDeserializer<?> modifyCollectionDeserializer(
      DeserializationConfig config, CollectionType type, BeanDescription beanDesc, JsonDeserializer<?> deserializer
   ) {
      return deserializer;
   }

   public JsonDeserializer<?> modifyCollectionLikeDeserializer(
      DeserializationConfig config, CollectionLikeType type, BeanDescription beanDesc, JsonDeserializer<?> deserializer
   ) {
      return deserializer;
   }

   public JsonDeserializer<?> modifyMapDeserializer(DeserializationConfig config, MapType type, BeanDescription beanDesc, JsonDeserializer<?> deserializer) {
      return deserializer;
   }

   public JsonDeserializer<?> modifyMapLikeDeserializer(
      DeserializationConfig config, MapLikeType type, BeanDescription beanDesc, JsonDeserializer<?> deserializer
   ) {
      return deserializer;
   }

   public KeyDeserializer modifyKeyDeserializer(DeserializationConfig config, JavaType type, KeyDeserializer deserializer) {
      return deserializer;
   }
}
