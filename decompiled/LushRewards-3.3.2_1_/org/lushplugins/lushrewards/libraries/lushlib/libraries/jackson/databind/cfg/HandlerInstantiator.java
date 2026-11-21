package org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.cfg;

import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.annotation.ObjectIdGenerator;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.annotation.ObjectIdResolver;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.DeserializationConfig;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.JsonDeserializer;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.JsonSerializer;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.KeyDeserializer;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.PropertyNamingStrategy;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.SerializationConfig;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.deser.ValueInstantiator;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.introspect.Annotated;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.introspect.BeanPropertyDefinition;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.jsontype.TypeIdResolver;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.jsontype.TypeResolverBuilder;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.ser.VirtualBeanPropertyWriter;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.jackson.databind.util.Converter;

public abstract class HandlerInstantiator {
   public abstract JsonDeserializer<?> deserializerInstance(DeserializationConfig var1, Annotated var2, Class<?> var3);

   public abstract KeyDeserializer keyDeserializerInstance(DeserializationConfig var1, Annotated var2, Class<?> var3);

   public abstract JsonSerializer<?> serializerInstance(SerializationConfig var1, Annotated var2, Class<?> var3);

   public abstract TypeResolverBuilder<?> typeResolverBuilderInstance(MapperConfig<?> var1, Annotated var2, Class<?> var3);

   public abstract TypeIdResolver typeIdResolverInstance(MapperConfig<?> var1, Annotated var2, Class<?> var3);

   public ValueInstantiator valueInstantiatorInstance(MapperConfig<?> config, Annotated annotated, Class<?> resolverClass) {
      return null;
   }

   public ObjectIdGenerator<?> objectIdGeneratorInstance(MapperConfig<?> config, Annotated annotated, Class<?> implClass) {
      return null;
   }

   public ObjectIdResolver resolverIdGeneratorInstance(MapperConfig<?> config, Annotated annotated, Class<?> implClass) {
      return null;
   }

   public PropertyNamingStrategy namingStrategyInstance(MapperConfig<?> config, Annotated annotated, Class<?> implClass) {
      return null;
   }

   public Converter<?, ?> converterInstance(MapperConfig<?> config, Annotated annotated, Class<?> implClass) {
      return null;
   }

   public VirtualBeanPropertyWriter virtualPropertyWriterInstance(MapperConfig<?> config, Class<?> implClass) {
      return null;
   }

   public Object includeFilterInstance(SerializationConfig config, BeanPropertyDefinition forProperty, Class<?> filterClass) {
      return null;
   }
}
