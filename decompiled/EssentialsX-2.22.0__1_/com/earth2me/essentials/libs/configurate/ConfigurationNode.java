package com.earth2me.essentials.libs.configurate;

import com.earth2me.essentials.libs.checkerframework.checker.nullness.qual.Nullable;
import com.earth2me.essentials.libs.configurate.serialize.Scalars;
import com.earth2me.essentials.libs.configurate.serialize.SerializationException;
import com.earth2me.essentials.libs.configurate.util.Types;
import com.earth2me.essentials.libs.geantyref.TypeFactory;
import com.earth2me.essentials.libs.geantyref.TypeToken;
import java.lang.reflect.AnnotatedType;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Map.Entry;
import java.util.function.Supplier;
import java.util.stream.Collector;

public interface ConfigurationNode {
   int NUMBER_DEF = 0;

   @Nullable
   Object key();

   NodePath path();

   @Nullable
   ConfigurationNode parent();

   ConfigurationNode node(Object... path);

   ConfigurationNode node(Iterable<?> path);

   boolean hasChild(Object... path);

   boolean hasChild(Iterable<?> path);

   boolean virtual();

   ConfigurationOptions options();

   boolean isNull();

   boolean isList();

   boolean isMap();

   boolean empty();

   List<? extends ConfigurationNode> childrenList();

   Map<Object, ? extends ConfigurationNode> childrenMap();

   default <V> Collector<Entry<?, V>, ? extends ConfigurationNode, ? extends ConfigurationNode> toMapCollector(final TypeToken<V> valueType) {
      return Collector.of(() -> this, (node, entry) -> {
         try {
            node.node(entry.getKey()).set(valueType, entry.getValue());
         } catch (SerializationException var4) {
            throw new IllegalArgumentException(var4);
         }
      }, ConfigurationNode::mergeFrom);
   }

   default <V> Collector<Entry<?, V>, ? extends ConfigurationNode, ? extends ConfigurationNode> toMapCollector(final Class<V> valueType) {
      return Collector.of(() -> this, (node, entry) -> {
         try {
            node.node(entry.getKey()).set(valueType, entry.getValue());
         } catch (SerializationException var4) {
            throw new IllegalArgumentException(var4);
         }
      }, ConfigurationNode::mergeFrom);
   }

   default <V> Collector<V, ? extends ConfigurationNode, ? extends ConfigurationNode> toListCollector(final TypeToken<V> valueType) {
      return Collector.of(() -> this, (node, value) -> {
         try {
            node.appendListNode().set(valueType, value);
         } catch (SerializationException var4) {
            throw new IllegalArgumentException(var4);
         }
      }, ConfigurationNode::mergeFrom);
   }

   default <V> Collector<V, ? extends ConfigurationNode, ? extends ConfigurationNode> toListCollector(final Class<V> valueType) {
      return Collector.of(() -> this, (node, value) -> {
         try {
            node.appendListNode().set(valueType, value);
         } catch (SerializationException var4) {
            throw new IllegalArgumentException(var4);
         }
      }, ConfigurationNode::mergeFrom);
   }

   default <V> V require(final TypeToken<V> type) throws SerializationException {
      V ret = this.get(type);
      if (ret == null) {
         throw new NoSuchElementException("Node value was null when a non-null node was require()d");
      } else {
         return ret;
      }
   }

   default <V> V require(final Class<V> type) throws SerializationException {
      V ret = this.get(type);
      if (ret == null) {
         throw new NoSuchElementException("Node value was null when a non-null node was require()d");
      } else {
         return ret;
      }
   }

   default Object require(final Type type) throws SerializationException {
      Object ret = this.get(type);
      if (ret == null) {
         throw new NoSuchElementException("Node value was null when a non-null node was require()d");
      } else {
         return ret;
      }
   }

   @Nullable
   default <V> V get(final TypeToken<V> type) throws SerializationException {
      return (V)this.get(type.getAnnotatedType());
   }

   default <V> V get(final TypeToken<V> type, final V def) throws SerializationException {
      return (V)this.get(type.getAnnotatedType(), def);
   }

   default <V> V get(final TypeToken<V> type, final Supplier<V> defSupplier) throws SerializationException {
      return (V)this.get(type.getAnnotatedType(), defSupplier);
   }

   @Nullable
   default <V> V get(final Class<V> type) throws SerializationException {
      return (V)this.get(type);
   }

   default <V> V get(final Class<V> type, final V def) throws SerializationException {
      return (V)this.get(type, def);
   }

   default <V> V get(final Class<V> type, final Supplier<V> defSupplier) throws SerializationException {
      return (V)this.get(type, defSupplier);
   }

   @Nullable
   Object get(AnnotatedType type) throws SerializationException;

   Object get(AnnotatedType type, Object def) throws SerializationException;

   Object get(AnnotatedType type, Supplier<?> defSupplier) throws SerializationException;

   @Nullable
   Object get(Type type) throws SerializationException;

   Object get(Type type, Object def) throws SerializationException;

   Object get(Type type, Supplier<?> defSupplier) throws SerializationException;

   @Nullable
   default <V> List<V> getList(final TypeToken<V> type) throws SerializationException {
      return this.get(Types.makeListType(type));
   }

   default <V> List<V> getList(final TypeToken<V> elementType, final List<V> def) throws SerializationException {
      TypeToken<List<V>> type = Types.makeListType(elementType);
      List<V> ret = this.get(type, def);
      return ret.isEmpty() ? AbstractConfigurationNode.storeDefault(this, type.getType(), def) : ret;
   }

   default <V> List<V> getList(final TypeToken<V> elementType, final Supplier<List<V>> defSupplier) throws SerializationException {
      TypeToken<List<V>> type = Types.makeListType(elementType);
      List<V> ret = this.get(type, defSupplier);
      return ret.isEmpty() ? AbstractConfigurationNode.storeDefault(this, type.getType(), defSupplier.get()) : ret;
   }

   @Nullable
   default <V> List<V> getList(final Class<V> type) throws SerializationException {
      return (List<V>)this.get(TypeFactory.parameterizedClass(List.class, type));
   }

   default <V> List<V> getList(final Class<V> elementType, final List<V> def) throws SerializationException {
      Type type = TypeFactory.parameterizedClass(List.class, elementType);
      List<V> ret = (List<V>)this.get(type, def);
      return ret.isEmpty() ? AbstractConfigurationNode.storeDefault(this, type, def) : ret;
   }

   default <V> List<V> getList(final Class<V> elementType, final Supplier<List<V>> defSupplier) throws SerializationException {
      Type type = TypeFactory.parameterizedClass(List.class, elementType);
      List<V> ret = (List<V>)this.get(type, defSupplier);
      return ret.isEmpty() ? AbstractConfigurationNode.storeDefault(this, type, defSupplier.get()) : ret;
   }

   @Nullable
   default String getString() {
      return Scalars.STRING.tryDeserialize(this.rawScalar());
   }

   default String getString(final String def) {
      Objects.requireNonNull(def, "def");
      String value = this.getString();
      if (value != null) {
         return value;
      } else {
         if (this.options().shouldCopyDefaults()) {
            Scalars.STRING.serialize(String.class, def, this);
         }

         return def;
      }
   }

   default float getFloat() {
      return this.getFloat(0.0F);
   }

   default float getFloat(final float def) {
      Float val = Scalars.FLOAT.tryDeserialize(this.rawScalar());
      if (val != null) {
         return val;
      } else {
         if (this.options().shouldCopyDefaults() && def != 0.0F) {
            Scalars.FLOAT.serialize(float.class, def, this);
         }

         return def;
      }
   }

   default double getDouble() {
      return this.getDouble(0.0);
   }

   default double getDouble(final double def) {
      Double val = Scalars.DOUBLE.tryDeserialize(this.rawScalar());
      if (val != null) {
         return val;
      } else {
         if (this.options().shouldCopyDefaults() && def != 0.0) {
            Scalars.DOUBLE.serialize(double.class, def, this);
         }

         return def;
      }
   }

   default int getInt() {
      return this.getInt(0);
   }

   default int getInt(final int def) {
      Integer val = Scalars.INTEGER.tryDeserialize(this.rawScalar());
      if (val != null) {
         return val;
      } else {
         if (this.options().shouldCopyDefaults() && def != 0) {
            Scalars.INTEGER.serialize(int.class, def, this);
         }

         return def;
      }
   }

   default long getLong() {
      return this.getLong(0L);
   }

   default long getLong(final long def) {
      Long val = Scalars.LONG.tryDeserialize(this.rawScalar());
      if (val != null) {
         return val;
      } else {
         if (this.options().shouldCopyDefaults() && def != 0L) {
            Scalars.LONG.serialize(long.class, def, this);
         }

         return def;
      }
   }

   default boolean getBoolean() {
      return this.getBoolean(false);
   }

   default boolean getBoolean(final boolean def) {
      Boolean val = Scalars.BOOLEAN.tryDeserialize(this.rawScalar());
      if (val != null) {
         return val;
      } else {
         if (this.options().shouldCopyDefaults()) {
            Scalars.BOOLEAN.serialize(boolean.class, def, this);
         }

         return def;
      }
   }

   ConfigurationNode set(@Nullable Object value) throws SerializationException;

   <V> ConfigurationNode set(TypeToken<V> type, @Nullable V value) throws SerializationException;

   <V> ConfigurationNode set(Class<V> type, @Nullable V value) throws SerializationException;

   ConfigurationNode set(Type type, @Nullable Object value) throws SerializationException;

   ConfigurationNode set(AnnotatedType type, @Nullable Object value) throws SerializationException;

   default <V> ConfigurationNode setList(final Class<V> elementType, @Nullable final List<V> items) throws SerializationException {
      return this.set(TypeFactory.parameterizedClass(List.class, elementType), items);
   }

   default <V> ConfigurationNode setList(final TypeToken<V> elementType, @Nullable final List<V> items) throws SerializationException {
      return this.set(TypeFactory.parameterizedClass(List.class, elementType.getType()), items);
   }

   @Nullable
   Object raw();

   ConfigurationNode raw(@Nullable Object value);

   @Nullable
   Object rawScalar();

   ConfigurationNode from(ConfigurationNode other);

   ConfigurationNode mergeFrom(ConfigurationNode other);

   boolean removeChild(Object key);

   ConfigurationNode appendListNode();

   ConfigurationNode copy();

   default <S, T, E extends Exception> T visit(final ConfigurationVisitor<S, T, E> visitor) throws E {
      return this.visit(visitor, visitor.newState());
   }

   <S, T, E extends Exception> T visit(ConfigurationVisitor<S, T, E> visitor, S state) throws E;

   default <S, T> T visit(final ConfigurationVisitor.Safe<S, T> visitor) {
      return this.visit(visitor, visitor.newState());
   }

   <S, T> T visit(ConfigurationVisitor.Safe<S, T> visitor, S state);

   <V> ConfigurationNode hint(RepresentationHint<V> hint, @Nullable V value);

   @Nullable
   <V> V hint(RepresentationHint<V> hint);

   @Nullable
   <V> V ownHint(RepresentationHint<V> hint);

   Map<RepresentationHint<?>, ?> ownHints();
}
