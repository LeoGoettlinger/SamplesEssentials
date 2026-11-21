package com.earth2me.essentials.libs.configurate.objectmapping;

import com.earth2me.essentials.libs.checkerframework.checker.nullness.qual.Nullable;
import com.earth2me.essentials.libs.configurate.ConfigurationNode;
import com.earth2me.essentials.libs.configurate.objectmapping.meta.Constraint;
import com.earth2me.essentials.libs.configurate.objectmapping.meta.NodeResolver;
import com.earth2me.essentials.libs.configurate.objectmapping.meta.Processor;
import com.earth2me.essentials.libs.configurate.serialize.SerializationException;
import com.earth2me.essentials.libs.configurate.serialize.TypeSerializer;
import com.earth2me.essentials.libs.configurate.util.CheckedFunction;
import com.earth2me.essentials.libs.configurate.util.UnmodifiableCollections;
import com.earth2me.essentials.libs.geantyref.GenericTypeReflector;
import com.google.auto.value.AutoValue;
import java.lang.reflect.AnnotatedType;
import java.util.List;
import java.util.function.Supplier;

@AutoValue
public abstract class FieldData<I, O> {
   static <I, O> FieldData<I, O> of(
      final String name,
      final AnnotatedType resolvedFieldType,
      final List<Constraint<?>> constraints,
      final List<Processor<?>> processors,
      final FieldData.Deserializer<I> deserializer,
      final CheckedFunction<O, Object, Exception> serializer,
      final NodeResolver resolver
   ) {
      return new AutoValue_FieldData<>(
         name, resolvedFieldType, UnmodifiableCollections.copyOf(constraints), UnmodifiableCollections.copyOf(processors), deserializer, serializer, resolver
      );
   }

   FieldData() {
   }

   public abstract String name();

   public abstract AnnotatedType resolvedType();

   abstract List<Constraint<?>> constraints();

   abstract List<Processor<?>> processors();

   abstract FieldData.Deserializer<I> deserializer();

   abstract CheckedFunction<O, Object, Exception> serializer();

   abstract NodeResolver nodeResolver();

   public boolean isValid(final Object instance) {
      try {
         this.validate(instance);
         return true;
      } catch (SerializationException var3) {
         return false;
      }
   }

   public void validate(@Nullable final Object instance) throws SerializationException {
      if (instance != null && !GenericTypeReflector.erase(GenericTypeReflector.box(this.resolvedType().getType())).isInstance(instance)) {
         throw new SerializationException("Object " + instance + " is not of expected type " + this.resolvedType().getType());
      } else {
         for (Constraint<?> constraint : this.constraints()) {
            ((Constraint<Object>)constraint).validate(instance);
         }
      }
   }

   TypeSerializer<?> serializerFrom(final ConfigurationNode node) throws SerializationException {
      TypeSerializer<?> serial = node.options().serializers().get(this.resolvedType());
      if (serial == null) {
         throw new SerializationException("No TypeSerializer found for field " + this.name() + " of type " + this.resolvedType().getType());
      } else {
         return serial;
      }
   }

   @Nullable
   public ConfigurationNode resolveNode(final ConfigurationNode source) {
      return this.nodeResolver().resolve(source);
   }

   @FunctionalInterface
   public interface Deserializer<I> {
      void accept(I intermediate, @Nullable Object newValue, Supplier<Object> implicitInitializer);
   }
}
