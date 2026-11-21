package com.earth2me.essentials.libs.configurate.serialize;

import com.earth2me.essentials.libs.checkerframework.checker.nullness.qual.Nullable;
import com.earth2me.essentials.libs.configurate.ConfigurationNode;
import com.earth2me.essentials.libs.configurate.ConfigurationOptions;
import com.earth2me.essentials.libs.geantyref.TypeToken;
import java.lang.reflect.AnnotatedParameterizedType;
import java.lang.reflect.AnnotatedType;
import java.lang.reflect.Type;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.OptionalLong;

final class OptionalSerializer implements TypeSerializer.Annotated<Optional<?>> {
   static final TypeToken<Optional<?>> TYPE = new TypeToken<Optional<?>>() {};
   static final TypeSerializer<Optional<?>> INSTANCE = new OptionalSerializer();

   private OptionalSerializer() {
   }

   private static AnnotatedType extractParameter(final AnnotatedType optional) throws SerializationException {
      if (!(optional instanceof AnnotatedParameterizedType)) {
         throw new SerializationException(optional, "Required type parameters on annotated type");
      } else {
         return ((AnnotatedParameterizedType)optional).getAnnotatedActualTypeArguments()[0];
      }
   }

   public Optional<?> deserialize(final AnnotatedType type, final ConfigurationNode node) throws SerializationException {
      return node.empty() ? Optional.empty() : Optional.ofNullable(node.get(extractParameter(type)));
   }

   public void serialize(final AnnotatedType type, @Nullable final Optional<?> obj, final ConfigurationNode node) throws SerializationException {
      if (obj != null && obj.isPresent()) {
         node.set(extractParameter(type), obj.get());
      } else {
         node.set(null);
      }
   }

   public Optional<?> emptyValue(final AnnotatedType specificType, final ConfigurationOptions options) {
      return Optional.empty();
   }

   static final class OfDouble implements TypeSerializer<OptionalDouble> {
      static final Class<OptionalDouble> TYPE = OptionalDouble.class;
      static final TypeSerializer<OptionalDouble> INSTANCE = new OptionalSerializer.OfDouble();

      private OfDouble() {
      }

      public OptionalDouble deserialize(final Type type, final ConfigurationNode node) throws SerializationException {
         return node.empty() ? OptionalDouble.empty() : OptionalDouble.of(node.require(double.class));
      }

      public void serialize(final Type type, @Nullable final OptionalDouble obj, final ConfigurationNode node) throws SerializationException {
         if (obj != null && obj.isPresent()) {
            node.set(double.class, obj.getAsDouble());
         } else {
            node.set(null);
         }
      }

      public OptionalDouble emptyValue(final AnnotatedType specificType, final ConfigurationOptions options) {
         return OptionalDouble.empty();
      }
   }

   static final class OfInt implements TypeSerializer<OptionalInt> {
      static final Class<OptionalInt> TYPE = OptionalInt.class;
      static final TypeSerializer<OptionalInt> INSTANCE = new OptionalSerializer.OfInt();

      private OfInt() {
      }

      public OptionalInt deserialize(final Type type, final ConfigurationNode node) throws SerializationException {
         return node.empty() ? OptionalInt.empty() : OptionalInt.of(node.require(int.class));
      }

      public void serialize(final Type type, @Nullable final OptionalInt obj, final ConfigurationNode node) throws SerializationException {
         if (obj != null && obj.isPresent()) {
            node.set(int.class, obj.getAsInt());
         } else {
            node.set(null);
         }
      }

      public OptionalInt emptyValue(final AnnotatedType specificType, final ConfigurationOptions options) {
         return OptionalInt.empty();
      }
   }

   static final class OfLong implements TypeSerializer<OptionalLong> {
      static final Class<OptionalLong> TYPE = OptionalLong.class;
      static final TypeSerializer<OptionalLong> INSTANCE = new OptionalSerializer.OfLong();

      private OfLong() {
      }

      public OptionalLong deserialize(final Type type, final ConfigurationNode node) throws SerializationException {
         return node.empty() ? OptionalLong.empty() : OptionalLong.of(node.require(long.class));
      }

      public void serialize(final Type type, @Nullable final OptionalLong obj, final ConfigurationNode node) throws SerializationException {
         if (obj != null && obj.isPresent()) {
            node.set(long.class, obj.getAsLong());
         } else {
            node.set(null);
         }
      }

      public OptionalLong emptyValue(final AnnotatedType specificType, final ConfigurationOptions options) {
         return OptionalLong.empty();
      }
   }
}
