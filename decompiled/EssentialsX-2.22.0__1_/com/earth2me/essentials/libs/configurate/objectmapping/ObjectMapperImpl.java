package com.earth2me.essentials.libs.configurate.objectmapping;

import com.earth2me.essentials.libs.configurate.ConfigurationNode;
import com.earth2me.essentials.libs.configurate.objectmapping.meta.PostProcessor;
import com.earth2me.essentials.libs.configurate.objectmapping.meta.Processor;
import com.earth2me.essentials.libs.configurate.serialize.SerializationException;
import com.earth2me.essentials.libs.configurate.serialize.TypeSerializer;
import com.earth2me.essentials.libs.configurate.util.CheckedFunction;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

class ObjectMapperImpl<I, V> implements ObjectMapper<V> {
   private final Type type;
   private final List<FieldData<I, V>> fields;
   final FieldDiscoverer.InstanceFactory<I> instanceFactory;
   private final List<PostProcessor> postProcessors;

   ObjectMapperImpl(
      final Type type, final List<FieldData<I, V>> fields, final FieldDiscoverer.InstanceFactory<I> instanceFactory, final List<PostProcessor> postProcessors
   ) {
      this.type = type;
      this.fields = Collections.unmodifiableList(fields);
      this.instanceFactory = instanceFactory;
      this.postProcessors = postProcessors;
   }

   @Override
   public V load(final ConfigurationNode source) throws SerializationException {
      return this.load0(source, intermediate -> (V)this.instanceFactory.complete(intermediate));
   }

   final V load0(final ConfigurationNode source, final CheckedFunction<I, V, SerializationException> completer) throws SerializationException {
      I intermediate = this.instanceFactory.begin();
      List<FieldData<I, V>> unseenFields = null;
      SerializationException failure = null;

      for (FieldData<I, V> field : this.fields) {
         ConfigurationNode node = field.resolveNode(source);
         if (node != null) {
            try {
               TypeSerializer<?> serial = field.serializerFrom(node);
               Object newVal;
               if (node.isNull()) {
                  newVal = null;
               } else {
                  newVal = serial.deserialize(field.resolvedType(), node);
               }

               field.validate(newVal);
               Supplier<Object> implicitInitializer;
               if (newVal == null && node.options().implicitInitialization()) {
                  implicitInitializer = () -> serial.emptyValue(field.resolvedType(), node.options());
               } else {
                  implicitInitializer = () -> null;
               }

               field.deserializer().accept(intermediate, newVal, implicitInitializer);
               if (newVal == null && source.options().shouldCopyDefaults()) {
                  if (unseenFields == null) {
                     unseenFields = new ArrayList<>();
                  }

                  unseenFields.add(field);
               }
            } catch (SerializationException var13) {
               var13.initPath(node::path);
               var13.initType(field.resolvedType());
               if (failure == null) {
                  failure = var13;
               } else {
                  failure.addSuppressed(var13);
               }
            }
         }
      }

      if (failure != null) {
         throw failure;
      } else {
         V complete = completer.apply(intermediate);

         for (PostProcessor postProcessor : this.postProcessors) {
            try {
               postProcessor.postProcess(complete);
            } catch (SerializationException var12) {
               if (failure == null) {
                  failure = var12;
               } else {
                  failure.addSuppressed(var12);
               }
            }
         }

         if (failure != null) {
            throw failure;
         } else {
            if (unseenFields != null) {
               for (FieldData<I, V> fieldx : unseenFields) {
                  this.saveSingle(fieldx, complete, source);
               }
            }

            return complete;
         }
      }
   }

   @Override
   public void save(final V value, final ConfigurationNode target) throws SerializationException {
      for (FieldData<I, V> field : this.fields) {
         this.saveSingle(field, value, target);
      }

      if (target.virtual()) {
         target.set(Collections.emptyMap());
      }
   }

   private void saveSingle(final FieldData<I, V> field, final V value, final ConfigurationNode target) throws SerializationException {
      ConfigurationNode node = field.resolveNode(target);
      if (node != null) {
         try {
            Object fieldVal;
            try {
               fieldVal = field.serializer().apply(value);
            } catch (SerializationException var9) {
               throw var9;
            } catch (Exception var10) {
               throw new SerializationException(node, field.resolvedType(), var10);
            }

            if (fieldVal == null) {
               node.set(null);
            } else {
               TypeSerializer<Object> serial = (TypeSerializer<Object>)field.serializerFrom(node);
               serial.serialize(field.resolvedType(), fieldVal, node);

               for (Processor<?> processor : field.processors()) {
                  ((Processor<Object>)processor).process(fieldVal, node);
               }
            }
         } catch (SerializationException var11) {
            var11.initPath(node::path);
            var11.initType(field.resolvedType());
            throw var11;
         }
      }
   }

   @Override
   public List<FieldData<I, V>> fields() {
      return this.fields;
   }

   @Override
   public Type mappedType() {
      return this.type;
   }

   @Override
   public boolean canCreateInstances() {
      return this.instanceFactory.canCreateInstances();
   }

   static final class Mutable<I, V> extends ObjectMapperImpl<I, V> implements ObjectMapper.Mutable<V> {
      Mutable(
         final Type type,
         final List<FieldData<I, V>> fields,
         final FieldDiscoverer.MutableInstanceFactory<I> instanceFactory,
         final List<PostProcessor> postProcessors
      ) {
         super(type, fields, instanceFactory, postProcessors);
      }

      @Override
      public void load(final V value, final ConfigurationNode node) throws SerializationException {
         this.load0(node, intermediate -> {
            ((FieldDiscoverer.MutableInstanceFactory)this.instanceFactory).complete(value, intermediate);
            return value;
         });
      }
   }
}
