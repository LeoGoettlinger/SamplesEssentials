package com.earth2me.essentials.libs.configurate.serialize;

import com.earth2me.essentials.libs.checkerframework.checker.nullness.qual.Nullable;
import com.earth2me.essentials.libs.configurate.ConfigurationNode;
import com.earth2me.essentials.libs.configurate.ConfigurationOptions;
import com.earth2me.essentials.libs.configurate.util.CheckedFunction;
import com.earth2me.essentials.libs.geantyref.GenericTypeReflector;
import java.lang.reflect.AnnotatedType;
import java.lang.reflect.Type;
import java.util.function.BiFunction;
import java.util.function.Predicate;

public interface TypeSerializer<T> {
   static <T> ScalarSerializer<T> of(
      final Type type, final BiFunction<T, Predicate<Class<?>>, Object> serializer, final CheckedFunction<Object, T, SerializationException> deserializer
   ) {
      return new FunctionScalarSerializer<>(type, deserializer, serializer);
   }

   static <T> ScalarSerializer<T> of(
      final Class<T> type, final BiFunction<T, Predicate<Class<?>>, Object> serializer, final CheckedFunction<Object, T, SerializationException> deserializer
   ) {
      if (type.getTypeParameters().length > 0) {
         throw new IllegalArgumentException("Parameterized types must be specified using TypeTokens, not raw classes");
      } else {
         return new FunctionScalarSerializer<>(type, deserializer, serializer);
      }
   }

   default T deserialize(final AnnotatedType type, final ConfigurationNode node) throws SerializationException {
      return this.deserialize(type.getType(), node);
   }

   T deserialize(Type type, ConfigurationNode node) throws SerializationException;

   default void serialize(final AnnotatedType type, @Nullable final T obj, final ConfigurationNode node) throws SerializationException {
      this.serialize(type.getType(), obj, node);
   }

   void serialize(Type type, @Nullable T obj, ConfigurationNode node) throws SerializationException;

   @Nullable
   default T emptyValue(final Type specificType, final ConfigurationOptions options) {
      return null;
   }

   @Nullable
   default T emptyValue(final AnnotatedType specificType, final ConfigurationOptions options) {
      return this.emptyValue(specificType.getType(), options);
   }

   public interface Annotated<V> extends TypeSerializer<V> {
      @Override
      V deserialize(AnnotatedType type, ConfigurationNode node) throws SerializationException;

      @Override
      default V deserialize(final Type type, final ConfigurationNode node) throws SerializationException {
         return this.deserialize(GenericTypeReflector.annotate(type), node);
      }

      @Override
      void serialize(AnnotatedType type, @Nullable V obj, ConfigurationNode node) throws SerializationException;

      @Override
      default void serialize(final Type type, @Nullable final V obj, final ConfigurationNode node) throws SerializationException {
         this.serialize(GenericTypeReflector.annotate(type), obj, node);
      }

      @Nullable
      @Override
      default V emptyValue(final AnnotatedType specificType, final ConfigurationOptions options) {
         return null;
      }

      @Nullable
      @Override
      default V emptyValue(final Type specificType, final ConfigurationOptions options) {
         return this.emptyValue(GenericTypeReflector.annotate(specificType), options);
      }
   }
}
