package com.earth2me.essentials.libs.configurate.serialize;

import com.earth2me.essentials.libs.checkerframework.checker.nullness.qual.Nullable;
import com.earth2me.essentials.libs.configurate.ConfigurationNode;
import com.earth2me.essentials.libs.geantyref.GenericTypeReflector;
import com.earth2me.essentials.libs.geantyref.TypeToken;
import java.lang.reflect.AnnotatedType;
import java.lang.reflect.Type;
import java.util.List;
import java.util.function.Predicate;

public abstract class ScalarSerializer<T> implements TypeSerializer.Annotated<T> {
   private final TypeToken<T> type;

   protected ScalarSerializer(final TypeToken<T> type) {
      Type boxed = GenericTypeReflector.box(type.getType());
      this.type = boxed == type.getType() ? type : TypeToken.get(boxed);
   }

   protected ScalarSerializer(final Class<T> type) {
      if (type.getTypeParameters().length > 0) {
         throw new IllegalArgumentException("Provided type " + type + " has type parameters but was not provided as a TypeToken!");
      } else {
         this.type = TypeToken.get(type);
      }
   }

   ScalarSerializer(final Type type) {
      this.type = (TypeToken<T>)TypeToken.get(type);
   }

   public final TypeToken<T> type() {
      return this.type;
   }

   @Override
   public final T deserialize(final Type type, final ConfigurationNode node) throws SerializationException {
      return (T)TypeSerializer.Annotated.super.deserialize(type, node);
   }

   @Override
   public final T deserialize(AnnotatedType type, final ConfigurationNode node) throws SerializationException {
      ConfigurationNode deserializeFrom = node;
      if (node.isList()) {
         List<? extends ConfigurationNode> children = node.childrenList();
         if (children.size() == 1) {
            deserializeFrom = children.get(0);
         }
      }

      if (!deserializeFrom.isList() && !deserializeFrom.isMap()) {
         Object value = deserializeFrom.rawScalar();
         if (value == null) {
            throw new SerializationException(type, "No scalar value present");
         } else {
            type = GenericTypeReflector.toCanonicalBoxed(type);
            T possible = this.cast(value);
            return possible != null ? possible : this.deserialize(type, value);
         }
      } else {
         throw new SerializationException(type, "Value must be provided as a scalar!");
      }
   }

   public final T deserialize(final Object value) throws SerializationException {
      T possible = this.cast(value);
      return possible != null ? possible : this.deserialize(this.type().getAnnotatedType(), value);
   }

   public T deserialize(final AnnotatedType type, final Object obj) throws SerializationException {
      return this.deserialize(type.getType(), obj);
   }

   public abstract T deserialize(Type type, Object obj) throws SerializationException;

   @Override
   public final void serialize(final AnnotatedType type, @Nullable final T obj, final ConfigurationNode node) {
      if (obj == null) {
         node.raw(null);
      } else if (node.options().acceptsType(obj.getClass())) {
         node.raw(obj);
      } else {
         node.raw(this.serialize(type, obj, node.options()::acceptsType));
      }
   }

   @Override
   public final void serialize(final Type type, @Nullable final T obj, final ConfigurationNode node) {
      this.serialize(GenericTypeReflector.annotate(type), obj, node);
   }

   protected Object serialize(final AnnotatedType type, final T item, final Predicate<Class<?>> typeSupported) {
      return this.serialize(item, typeSupported);
   }

   protected abstract Object serialize(T item, Predicate<Class<?>> typeSupported);

   @Nullable
   private T cast(final Object value) {
      Class<?> rawType = GenericTypeReflector.erase(this.type().getType());
      return (T)(rawType.isInstance(value) ? value : null);
   }

   @Nullable
   public final T tryDeserialize(@Nullable final Object obj) {
      if (obj == null) {
         return null;
      } else {
         try {
            return this.deserialize(obj);
         } catch (SerializationException var3) {
            return null;
         }
      }
   }

   public final String serializeToString(final T item) {
      return item instanceof CharSequence
         ? item.toString()
         : (String)this.serialize(GenericTypeReflector.annotate(item.getClass()), item, clazz -> clazz.isAssignableFrom(String.class));
   }

   public abstract static class Annotated<V> extends ScalarSerializer<V> {
      protected Annotated(final Class<V> type) {
         super(type);
      }

      protected Annotated(final TypeToken<V> type) {
         super(type);
      }

      @Override
      public abstract V deserialize(AnnotatedType type, Object obj) throws SerializationException;

      @Override
      public V deserialize(final Type type, final Object obj) throws SerializationException {
         return this.deserialize(GenericTypeReflector.annotate(type), obj);
      }

      @Override
      protected abstract Object serialize(AnnotatedType type, V item, Predicate<Class<?>> typeSupported);

      @Override
      protected Object serialize(final V item, final Predicate<Class<?>> typeSupported) {
         return this.serialize(GenericTypeReflector.annotate(item.getClass()), item, typeSupported);
      }
   }
}
