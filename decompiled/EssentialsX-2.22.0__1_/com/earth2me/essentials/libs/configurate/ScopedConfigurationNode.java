package com.earth2me.essentials.libs.configurate;

import com.earth2me.essentials.libs.checkerframework.checker.nullness.qual.Nullable;
import com.earth2me.essentials.libs.configurate.serialize.SerializationException;
import com.earth2me.essentials.libs.configurate.serialize.TypeSerializer;
import com.earth2me.essentials.libs.configurate.util.CheckedConsumer;
import com.earth2me.essentials.libs.configurate.util.Types;
import com.earth2me.essentials.libs.geantyref.GenericTypeReflector;
import com.earth2me.essentials.libs.geantyref.TypeToken;
import java.lang.reflect.AnnotatedType;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collector;

public interface ScopedConfigurationNode<N extends ScopedConfigurationNode<N>> extends ConfigurationNode {
   N self();

   N appendListNode();

   N copy();

   N node(Object... path);

   N node(Iterable<?> path);

   @Nullable
   N parent();

   N from(ConfigurationNode other);

   N mergeFrom(ConfigurationNode other);

   N set(@Nullable Object value) throws SerializationException;

   default N set(final Type type, final Object value) throws SerializationException {
      if (value == null) {
         return this.set(null);
      } else {
         Type boxed = Types.box(type);
         Class<?> erasedType = GenericTypeReflector.erase(boxed);
         if (!erasedType.isInstance(value)) {
            throw new SerializationException(
               this,
               type,
               "Got a value of unexpected type " + value.getClass().getName() + ", when the value should be an instance of " + erasedType.getSimpleName()
            );
         } else {
            TypeSerializer<?> serial = this.options().serializers().get(boxed);
            if (serial != null) {
               try {
                  ((TypeSerializer<Object>)serial).serialize(boxed, value, this.self());
               } catch (SerializationException var7) {
                  var7.initPath(this::path);
                  var7.initType(type);
                  throw var7;
               }
            } else {
               if (!this.options().acceptsType(value.getClass())) {
                  throw new SerializationException(this, type, "No serializer available for type " + type);
               }

               this.raw(value);
            }

            return this.self();
         }
      }
   }

   default N set(final AnnotatedType type, final Object value) throws SerializationException {
      if (value == null) {
         return this.set(null);
      } else {
         Class<?> erasedType = GenericTypeReflector.erase(type.getType());
         if (!erasedType.isInstance(value)) {
            throw new SerializationException(
               this,
               type,
               "Got a value of unexpected type " + value.getClass().getName() + ", when the value should be an instance of " + erasedType.getSimpleName()
            );
         } else {
            TypeSerializer<?> serial = this.options().serializers().get(type);
            if (serial != null) {
               try {
                  ((TypeSerializer<Object>)serial).serialize(type, value, this);
               } catch (SerializationException var6) {
                  var6.initPath(this::path);
                  var6.initType(type);
                  throw var6;
               }
            } else {
               if (!this.options().acceptsType(value.getClass())) {
                  throw new SerializationException(this, type, "No serializer available for type " + type);
               }

               this.raw(value);
            }

            return this.self();
         }
      }
   }

   default <V> N set(final Class<V> type, @Nullable final V value) throws SerializationException {
      return this.set(type, value);
   }

   default <V> N set(final TypeToken<V> type, @Nullable final V value) throws SerializationException {
      return this.set(type.getType(), value);
   }

   default <V> N setList(final Class<V> elementType, @Nullable final List<V> items) throws SerializationException {
      ConfigurationNode.super.setList(elementType, items);
      return this.self();
   }

   default <V> N setList(final TypeToken<V> elementType, @Nullable final List<V> items) throws SerializationException {
      ConfigurationNode.super.setList(elementType, items);
      return this.self();
   }

   N raw(@Nullable Object value);

   @Override
   List<N> childrenList();

   @Override
   Map<Object, N> childrenMap();

   @Override
   default <V> Collector<Entry<?, V>, N, N> toMapCollector(final TypeToken<V> valueType) {
      return (Collector<Entry<?, V>, N, N>)ConfigurationNode.super.toMapCollector(valueType);
   }

   @Override
   default <V> Collector<Entry<?, V>, N, N> toMapCollector(final Class<V> valueType) {
      return (Collector<Entry<?, V>, N, N>)ConfigurationNode.super.toMapCollector(valueType);
   }

   @Override
   default <V> Collector<V, N, N> toListCollector(final TypeToken<V> valueType) {
      return (Collector<V, N, N>)ConfigurationNode.super.toListCollector(valueType);
   }

   @Override
   default <V> Collector<V, N, N> toListCollector(final Class<V> valueType) {
      return (Collector<V, N, N>)ConfigurationNode.super.toListCollector(valueType);
   }

   default <E extends Exception> N act(final CheckedConsumer<? super N, E> action) throws E {
      action.accept(this.self());
      return this.self();
   }

   <V> N hint(RepresentationHint<V> hint, @Nullable V value);
}
