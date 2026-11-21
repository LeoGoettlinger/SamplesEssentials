package org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.ser;

import java.util.List;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.BeanDescription;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.JavaType;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.JsonSerializer;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.SerializationConfig;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.type.ArrayType;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.type.CollectionLikeType;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.type.CollectionType;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.type.MapLikeType;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.type.MapType;

public abstract class BeanSerializerModifier {
   public List<BeanPropertyWriter> changeProperties(SerializationConfig config, BeanDescription beanDesc, List<BeanPropertyWriter> beanProperties) {
      return beanProperties;
   }

   public List<BeanPropertyWriter> orderProperties(SerializationConfig config, BeanDescription beanDesc, List<BeanPropertyWriter> beanProperties) {
      return beanProperties;
   }

   public BeanSerializerBuilder updateBuilder(SerializationConfig config, BeanDescription beanDesc, BeanSerializerBuilder builder) {
      return builder;
   }

   public JsonSerializer<?> modifySerializer(SerializationConfig config, BeanDescription beanDesc, JsonSerializer<?> serializer) {
      return serializer;
   }

   public JsonSerializer<?> modifyArraySerializer(SerializationConfig config, ArrayType valueType, BeanDescription beanDesc, JsonSerializer<?> serializer) {
      return serializer;
   }

   public JsonSerializer<?> modifyCollectionSerializer(
      SerializationConfig config, CollectionType valueType, BeanDescription beanDesc, JsonSerializer<?> serializer
   ) {
      return serializer;
   }

   public JsonSerializer<?> modifyCollectionLikeSerializer(
      SerializationConfig config, CollectionLikeType valueType, BeanDescription beanDesc, JsonSerializer<?> serializer
   ) {
      return serializer;
   }

   public JsonSerializer<?> modifyMapSerializer(SerializationConfig config, MapType valueType, BeanDescription beanDesc, JsonSerializer<?> serializer) {
      return serializer;
   }

   public JsonSerializer<?> modifyMapLikeSerializer(SerializationConfig config, MapLikeType valueType, BeanDescription beanDesc, JsonSerializer<?> serializer) {
      return serializer;
   }

   public JsonSerializer<?> modifyEnumSerializer(SerializationConfig config, JavaType valueType, BeanDescription beanDesc, JsonSerializer<?> serializer) {
      return serializer;
   }

   public JsonSerializer<?> modifyKeySerializer(SerializationConfig config, JavaType valueType, BeanDescription beanDesc, JsonSerializer<?> serializer) {
      return serializer;
   }
}
