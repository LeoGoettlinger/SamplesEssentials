package com.earth2me.essentials.libs.configurate.objectmapping;

import com.earth2me.essentials.libs.checkerframework.checker.nullness.qual.Nullable;
import com.earth2me.essentials.libs.configurate.BasicConfigurationNode;
import com.earth2me.essentials.libs.configurate.ConfigurationNode;
import com.earth2me.essentials.libs.configurate.ConfigurationOptions;
import com.earth2me.essentials.libs.configurate.objectmapping.meta.Comment;
import com.earth2me.essentials.libs.configurate.objectmapping.meta.Constraint;
import com.earth2me.essentials.libs.configurate.objectmapping.meta.Matches;
import com.earth2me.essentials.libs.configurate.objectmapping.meta.NodeResolver;
import com.earth2me.essentials.libs.configurate.objectmapping.meta.PostProcessor;
import com.earth2me.essentials.libs.configurate.objectmapping.meta.Processor;
import com.earth2me.essentials.libs.configurate.objectmapping.meta.Required;
import com.earth2me.essentials.libs.configurate.serialize.SerializationException;
import com.earth2me.essentials.libs.configurate.serialize.TypeSerializer;
import com.earth2me.essentials.libs.configurate.util.CheckedFunction;
import com.earth2me.essentials.libs.configurate.util.NamingScheme;
import com.earth2me.essentials.libs.configurate.util.NamingSchemes;
import com.earth2me.essentials.libs.geantyref.GenericTypeReflector;
import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.AnnotatedType;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Map.Entry;

final class ObjectMapperFactoryImpl implements ObjectMapper.Factory, TypeSerializer<Object> {
   private static final int MAXIMUM_MAPPERS_SIZE = 64;
   private final Map<Type, ObjectMapper<?>> mappers = new LinkedHashMap<Type, ObjectMapper<?>>() {
      private static final long serialVersionUID = 1838651306004330732L;

      @Override
      protected boolean removeEldestEntry(final Entry<Type, ObjectMapper<?>> eldest) {
         return this.size() > 64;
      }
   };
   private final List<NodeResolver.Factory> resolverFactories;
   private final List<FieldDiscoverer<?>> fieldDiscoverers;
   private final Map<Class<? extends Annotation>, List<Definition<?, ?, ? extends Constraint.Factory<?, ?>>>> constraints;
   private final Map<Class<? extends Annotation>, List<Definition<?, ?, ? extends Processor.Factory<?, ?>>>> processors;
   private final List<PostProcessor.Factory> postProcessors;
   public static final String CLASS_KEY = "__class__";
   static final ObjectMapper.Factory INSTANCE = defaultBuilder().build();

   ObjectMapperFactoryImpl(final ObjectMapperFactoryImpl.Builder builder) {
      this.resolverFactories = new ArrayList<>(builder.resolvers);
      Collections.reverse(this.resolverFactories);
      NamingScheme scheme = builder.namingScheme;
      if (scheme != null) {
         this.resolverFactories.add((name, element) -> {
            String key = scheme.coerce(name);
            return node -> node.node(key);
         });
      }

      this.fieldDiscoverers = new ArrayList<>(builder.discoverer);
      Collections.reverse(this.fieldDiscoverers);
      this.constraints = new HashMap<>();

      for (Definition<?, ?, ? extends Constraint.Factory<?, ?>> def : builder.constraints) {
         this.constraints.computeIfAbsent((Class<? extends Annotation>)def.annotation(), k -> new ArrayList<>()).add(def);
      }

      this.constraints.values().forEach(Collections::reverse);
      this.processors = new HashMap<>();

      for (Definition<?, ?, ? extends Processor.Factory<?, ?>> def : builder.processors) {
         this.processors.computeIfAbsent((Class<? extends Annotation>)def.annotation(), k -> new ArrayList<>()).add(def);
      }

      this.processors.values().forEach(Collections::reverse);
      this.postProcessors = new ArrayList<>(builder.postProcessors);
      Collections.reverse(this.postProcessors);
   }

   @Override
   public ObjectMapper<?> get(final Type type) throws SerializationException {
      Objects.requireNonNull(type, "type");
      if (GenericTypeReflector.isMissingTypeParameters(type)) {
         throw new SerializationException(type, "Raw types are not supported!");
      } else {
         synchronized (this.mappers) {
            return computeFromMap(this.mappers, type, this::computeMapper);
         }
      }
   }

   @Override
   public TypeSerializer<Object> asTypeSerializer() {
      return this;
   }

   private ObjectMapper<?> computeMapper(final Type type) throws SerializationException {
      for (FieldDiscoverer<?> discoverer : this.fieldDiscoverers) {
         ObjectMapper<?> result = this.newMapper(type, discoverer);
         if (result != null) {
            return result;
         }
      }

      throw new SerializationException(type, "Could not find factory for type " + type);
   }

   private <I, V> ObjectMapper<V> newMapper(final Type type, final FieldDiscoverer<I> discoverer) throws SerializationException {
      List<FieldData<I, V>> fields = new ArrayList<>();
      FieldDiscoverer.InstanceFactory<I> candidate = discoverer.discover(
         GenericTypeReflector.annotate(type),
         (name, fieldType, container, deserializer, serializer) -> this.makeData(fields, name, fieldType, container, deserializer, serializer)
      );
      if (candidate == null) {
         return null;
      } else {
         List<PostProcessor> postProcessors = this.computePostProcessors(type);
         return (ObjectMapper<V>)(candidate instanceof FieldDiscoverer.MutableInstanceFactory
            ? new ObjectMapperImpl.Mutable<>(type, fields, (FieldDiscoverer.MutableInstanceFactory<I>)candidate, postProcessors)
            : new ObjectMapperImpl<>(type, fields, candidate, postProcessors));
      }
   }

   private List<PostProcessor> computePostProcessors(final Type type) throws SerializationException {
      if (this.postProcessors.isEmpty()) {
         return Collections.emptyList();
      } else {
         List<PostProcessor> createdProcessors = new ArrayList<>();

         for (PostProcessor.Factory factory : this.postProcessors) {
            PostProcessor candidate = factory.createProcessor(type);
            if (candidate != null) {
               createdProcessors.add(candidate);
            }
         }

         return Collections.unmodifiableList(createdProcessors);
      }
   }

   private <I, O> void makeData(
      final List<FieldData<I, O>> fields,
      final String name,
      final AnnotatedType type,
      final AnnotatedElement container,
      final FieldData.Deserializer<I> deserializer,
      final CheckedFunction<O, Object, Exception> serializer
   ) {
      NodeResolver resolver = null;

      for (NodeResolver.Factory factory : this.resolverFactories) {
         NodeResolver next = factory.make(name, container);
         if (next != null) {
            if (next != NodeResolver.SKIP_FIELD) {
               resolver = next;
            }
            break;
         }
      }

      if (resolver != null) {
         Type normalizedType = GenericTypeReflector.box(type.getType());
         List<Constraint<?>> constraints = new ArrayList<>();
         List<Processor<?>> processors = new ArrayList<>();

         for (Annotation annotation : container.getAnnotations()) {
            List<Definition<?, ?, ? extends Constraint.Factory<?, ?>>> definitions = this.constraints.get(annotation.annotationType());
            if (definitions != null) {
               for (Definition<?, ?, ? extends Constraint.Factory<?, ?>> def : definitions) {
                  if (GenericTypeReflector.isSuperType(def.type(), normalizedType)) {
                     constraints.add(((Constraint.Factory<Annotation, ?>)def.factory()).make(annotation, type.getType()));
                  }
               }
            }

            List<Definition<?, ?, ? extends Processor.Factory<?, ?>>> processorDefs = this.processors.get(annotation.annotationType());
            if (processorDefs != null) {
               for (Definition<?, ?, ? extends Processor.Factory<?, ?>> processorDef : processorDefs) {
                  if (GenericTypeReflector.isSuperType(processorDef.type(), normalizedType)) {
                     processors.add(((Processor.Factory<Annotation, ?>)processorDef.factory()).make(annotation, type.getType()));
                  }
               }
            }
         }

         AnnotatedType combinedType = GenericTypeReflector.updateAnnotations(type, container.getAnnotations());
         fields.add(FieldData.of(name, combinedType, constraints, processors, deserializer, serializer, resolver));
      }
   }

   @Override
   public Object deserialize(final Type type, final ConfigurationNode node) throws SerializationException {
      Type clazz = this.instantiableType(node, type, node.node("__class__").getString());
      return this.get(clazz).load(node);
   }

   private Type instantiableType(final ConfigurationNode node, final Type type, @Nullable final String configuredName) throws SerializationException {
      Class<?> rawType = GenericTypeReflector.erase(type);
      Type retClass;
      if (!rawType.isInterface() && !Modifier.isAbstract(rawType.getModifiers())) {
         retClass = type;
      } else {
         if (configuredName == null) {
            throw new SerializationException(node, type, "No available configured type for instances of this type");
         }

         try {
            retClass = Class.forName(configuredName);
         } catch (ClassNotFoundException var7) {
            throw new SerializationException(node, type, "Unknown class of object " + configuredName, var7);
         }

         if (!GenericTypeReflector.isSuperType(type, retClass)) {
            throw new SerializationException(node, type, "Configured type " + configuredName + " does not extend " + rawType.getCanonicalName());
         }
      }

      return retClass;
   }

   @Override
   public void serialize(final Type type, @Nullable final Object obj, final ConfigurationNode node) throws SerializationException {
      if (obj == null) {
         ConfigurationNode clazz = node.node("__class__");
         node.set(null);
         if (!clazz.virtual()) {
            node.node("__class__").set(clazz);
         }
      } else {
         Class<?> rawType = GenericTypeReflector.erase(type);
         ObjectMapper<?> mapper;
         if (!rawType.isInterface() && !Modifier.isAbstract(rawType.getModifiers())) {
            mapper = this.get(type);
         } else {
            node.node("__class__").set(obj.getClass().getName());
            mapper = this.get(obj.getClass());
         }

         ((ObjectMapper<Object>)mapper).save(obj, node);
      }
   }

   @Nullable
   @Override
   public Object emptyValue(final Type specificType, final ConfigurationOptions options) {
      try {
         return this.get(specificType).load(BasicConfigurationNode.root(options.shouldCopyDefaults(false)));
      } catch (SerializationException var4) {
         return null;
      }
   }

   private static <K, V, E extends Exception> V computeFromMap(final Map<K, V> map, final K key, final CheckedFunction<K, V, E> creator) throws E {
      try {
         return map.computeIfAbsent(key, k -> {
            try {
               return creator.apply((K)k);
            } catch (Exception var3) {
               if (var3 instanceof RuntimeException) {
                  throw (RuntimeException)var3;
               } else {
                  throw new ObjectMapperFactoryImpl.StacklessWrapper(var3);
               }
            }
         });
      } catch (ObjectMapperFactoryImpl.StacklessWrapper var4) {
         throw (Exception)var4.getCause();
      }
   }

   static ObjectMapper.Factory.Builder defaultBuilder() {
      return new ObjectMapperFactoryImpl.Builder()
         .defaultNamingScheme(NamingSchemes.LOWER_CASE_DASHED)
         .addNodeResolver(NodeResolver.nodeKey())
         .addNodeResolver(NodeResolver.keyFromSetting())
         .addNodeResolver(NodeResolver.nodeFromParent())
         .addProcessor(Comment.class, Processor.comments())
         .addConstraint(Matches.class, String.class, Constraint.pattern())
         .addConstraint(Required.class, Constraint.required())
         .addPostProcessor(PostProcessor.methodsAnnotatedPostProcess())
         .addDiscoverer(FieldDiscoverer.emptyConstructorObject())
         .addDiscoverer(FieldDiscoverer.record());
   }

   static class Builder implements ObjectMapper.Factory.Builder {
      @Nullable
      private NamingScheme namingScheme;
      private final List<NodeResolver.Factory> resolvers = new ArrayList<>();
      private final List<FieldDiscoverer<?>> discoverer = new ArrayList<>();
      private final List<Definition<?, ?, ? extends Constraint.Factory<?, ?>>> constraints = new ArrayList<>();
      private final List<Definition<?, ?, ? extends Processor.Factory<?, ?>>> processors = new ArrayList<>();
      private final List<PostProcessor.Factory> postProcessors = new ArrayList<>();

      @Override
      public ObjectMapper.Factory.Builder defaultNamingScheme(final NamingScheme scheme) {
         this.namingScheme = scheme;
         return this;
      }

      public ObjectMapperFactoryImpl.Builder addNodeResolver(final NodeResolver.Factory resolver) {
         this.resolvers.add(resolver);
         return this;
      }

      public ObjectMapperFactoryImpl.Builder addDiscoverer(final FieldDiscoverer<?> discoverer) {
         this.discoverer.add(discoverer);
         return this;
      }

      public <A extends Annotation, T> ObjectMapperFactoryImpl.Builder addProcessor(
         final Class<A> definition, final Class<T> valueType, final Processor.Factory<A, T> factory
      ) {
         this.processors.add(Definition.of(definition, valueType, factory));
         return this;
      }

      public <A extends Annotation, T> ObjectMapperFactoryImpl.Builder addConstraint(
         final Class<A> definition, final Class<T> valueType, final Constraint.Factory<A, T> factory
      ) {
         this.constraints.add(Definition.of(definition, valueType, factory));
         return this;
      }

      public ObjectMapperFactoryImpl.Builder addPostProcessor(final PostProcessor.Factory factory) {
         this.postProcessors.add(Objects.requireNonNull(factory, "factory"));
         return this;
      }

      @Override
      public ObjectMapper.Factory build() {
         return new ObjectMapperFactoryImpl(this);
      }
   }

   private static class StacklessWrapper extends RuntimeException {
      private static final long serialVersionUID = 1759625646560465255L;

      StacklessWrapper(final Throwable cause) {
         super(cause);
      }

      @Override
      public Throwable fillInStackTrace() {
         return this;
      }
   }
}
