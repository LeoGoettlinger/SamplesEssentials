package com.earth2me.essentials.libs.configurate.objectmapping;

import com.earth2me.essentials.libs.configurate.ConfigurationNode;
import com.earth2me.essentials.libs.configurate.objectmapping.meta.Constraint;
import com.earth2me.essentials.libs.configurate.objectmapping.meta.NodeResolver;
import com.earth2me.essentials.libs.configurate.objectmapping.meta.PostProcessor;
import com.earth2me.essentials.libs.configurate.objectmapping.meta.Processor;
import com.earth2me.essentials.libs.configurate.serialize.SerializationException;
import com.earth2me.essentials.libs.configurate.serialize.TypeSerializer;
import com.earth2me.essentials.libs.configurate.util.NamingScheme;
import com.earth2me.essentials.libs.geantyref.TypeToken;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.List;

public interface ObjectMapper<V> {
   static ObjectMapper.Factory factory() {
      return ObjectMapperFactoryImpl.INSTANCE;
   }

   static ObjectMapper.Factory.Builder emptyFactoryBuilder() {
      return new ObjectMapperFactoryImpl.Builder();
   }

   static ObjectMapper.Factory.Builder factoryBuilder() {
      return ObjectMapperFactoryImpl.defaultBuilder();
   }

   V load(ConfigurationNode source) throws SerializationException;

   void save(V value, ConfigurationNode target) throws SerializationException;

   List<? extends FieldData<?, V>> fields();

   Type mappedType();

   boolean canCreateInstances();

   public interface Factory {
      default <V> ObjectMapper<V> get(final TypeToken<V> type) throws SerializationException {
         return (ObjectMapper<V>)this.get(type.getType());
      }

      default <V> ObjectMapper<V> get(final Class<V> clazz) throws SerializationException {
         return (ObjectMapper<V>)this.get(clazz);
      }

      ObjectMapper<?> get(Type type) throws SerializationException;

      TypeSerializer<Object> asTypeSerializer();

      public interface Builder {
         ObjectMapper.Factory.Builder defaultNamingScheme(NamingScheme scheme);

         ObjectMapper.Factory.Builder addNodeResolver(NodeResolver.Factory resolver);

         ObjectMapper.Factory.Builder addDiscoverer(FieldDiscoverer<?> discoverer);

         default <A extends Annotation> ObjectMapper.Factory.Builder addProcessor(final Class<A> definition, final Processor.Factory<A, Object> factory) {
            return this.addProcessor(definition, Object.class, factory);
         }

         <A extends Annotation, T> ObjectMapper.Factory.Builder addProcessor(Class<A> definition, Class<T> valueType, Processor.Factory<A, T> factory);

         default <A extends Annotation> ObjectMapper.Factory.Builder addConstraint(final Class<A> definition, final Constraint.Factory<A, Object> factory) {
            return this.addConstraint(definition, Object.class, factory);
         }

         <A extends Annotation, T> ObjectMapper.Factory.Builder addConstraint(Class<A> definition, Class<T> valueType, Constraint.Factory<A, T> factory);

         ObjectMapper.Factory.Builder addPostProcessor(PostProcessor.Factory factory);

         ObjectMapper.Factory build();
      }
   }

   public interface Mutable<V> extends ObjectMapper<V> {
      void load(V value, ConfigurationNode node) throws SerializationException;
   }
}
