package com.earth2me.essentials.libs.configurate;

import com.earth2me.essentials.libs.checkerframework.checker.nullness.qual.Nullable;
import com.earth2me.essentials.libs.configurate.serialize.SerializationException;
import com.earth2me.essentials.libs.configurate.serialize.TypeSerializer;
import com.earth2me.essentials.libs.configurate.util.UnmodifiableCollections;
import com.earth2me.essentials.libs.geantyref.GenericTypeReflector;
import java.lang.reflect.AnnotatedType;
import java.lang.reflect.Type;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

abstract class AbstractConfigurationNode<N extends ScopedConfigurationNode<N>, A extends AbstractConfigurationNode<N, A>> implements ScopedConfigurationNode<N> {
   private final ConfigurationOptions options;
   volatile boolean attached;
   @Nullable
   volatile Object key;
   @Nullable
   private A parent;
   volatile ConfigValue<N, A> value;
   final Map<RepresentationHint<?>, Object> hints;

   protected AbstractConfigurationNode(@Nullable final Object key, @Nullable final A parent, final ConfigurationOptions options) {
      Objects.requireNonNull(options, "options");
      if (key == null != (parent == null)) {
         throw new IllegalArgumentException("A node's key and parent must share the same nullability status");
      } else {
         this.key = key;
         this.options = options;
         this.parent = parent;
         this.value = NullConfigValue.instance();
         this.hints = new ConcurrentHashMap<>();
         if (parent == null) {
            this.attached = true;
         }
      }
   }

   protected AbstractConfigurationNode(@Nullable final A parent, final A copyOf) {
      this.options = copyOf.options();
      this.attached = true;
      this.key = copyOf.key;
      this.parent = parent;
      this.value = copyOf.value.copy(this.implSelf());
      this.hints = new ConcurrentHashMap<>(copyOf.hints);
   }

   static <V> V storeDefault(final ConfigurationNode node, final Type type, final V defValue) throws SerializationException {
      Objects.requireNonNull(defValue, "defValue");
      if (node.options().shouldCopyDefaults()) {
         node.set(type, defValue);
      }

      return defValue;
   }

   static <V> V storeDefault(final ConfigurationNode node, final AnnotatedType type, final V defValue) throws SerializationException {
      Objects.requireNonNull(defValue, "defValue");
      if (node.options().shouldCopyDefaults()) {
         node.set(type, defValue);
      }

      return defValue;
   }

   @Nullable
   @Override
   public final Object get(final AnnotatedType type) throws SerializationException {
      return this.get0(type, true);
   }

   @Override
   public final Object get(final AnnotatedType type, final Object def) throws SerializationException {
      Object value = this.get0(type, false);
      return value == null ? storeDefault(this, type, def) : value;
   }

   @Override
   public final Object get(final AnnotatedType type, final Supplier<?> defSupplier) throws SerializationException {
      Object value = this.get0(type, false);
      return value == null ? storeDefault(this, type, defSupplier.get()) : value;
   }

   @Nullable
   @Override
   public final Object get(final Type type) throws SerializationException {
      return this.get0(type, true);
   }

   @Override
   public final Object get(final Type type, final Object def) throws SerializationException {
      Object value = this.get0(type, false);
      return value == null ? storeDefault(this, type, def) : value;
   }

   @Override
   public final Object get(final Type type, final Supplier<?> defSupplier) throws SerializationException {
      Object value = this.get0(type, false);
      return value == null ? storeDefault(this, type, defSupplier.get()) : value;
   }

   final Object get0(final Type type, final boolean doImplicitInit) throws SerializationException {
      Objects.requireNonNull(type, "type");
      if (GenericTypeReflector.isMissingTypeParameters(type)) {
         throw new SerializationException(this, type, "Raw types are not supported");
      } else {
         TypeSerializer<?> serial = this.options().serializers().get(type);
         if (this.value instanceof NullConfigValue) {
            if (serial != null && doImplicitInit && this.options().implicitInitialization()) {
               Object emptyValue = serial.emptyValue(type, this.options);
               if (emptyValue != null) {
                  return storeDefault(this, type, emptyValue);
               }
            }

            return null;
         } else if (serial == null) {
            Object value = this.raw();
            Class<?> erasure = GenericTypeReflector.erase(type);
            return erasure.isInstance(value) ? value : null;
         } else {
            try {
               return serial.deserialize(type, this.self());
            } catch (SerializationException var6) {
               var6.initPath(this::path);
               var6.initType(type);
               throw var6;
            }
         }
      }
   }

   final Object get0(final AnnotatedType type, final boolean doImplicitInit) throws SerializationException {
      Objects.requireNonNull(type, "type");
      if (GenericTypeReflector.isMissingTypeParameters(type.getType())) {
         throw new SerializationException(this, type, "Raw types are not supported");
      } else {
         TypeSerializer<?> serial = this.options().serializers().get(type);
         if (this.value instanceof NullConfigValue) {
            if (serial != null && doImplicitInit && this.options().implicitInitialization()) {
               Object emptyValue = serial.emptyValue(type, this.options);
               if (emptyValue != null) {
                  return storeDefault(this, type, emptyValue);
               }
            }

            return null;
         } else if (serial == null) {
            Object value = this.raw();
            Class<?> erasure = GenericTypeReflector.erase(type.getType());
            return erasure.isInstance(value) ? value : null;
         } else {
            try {
               return serial.deserialize(type, this);
            } catch (SerializationException var6) {
               var6.initPath(this::path);
               var6.initType(type);
               throw var6;
            }
         }
      }
   }

   @Override
   public final N set(final Object newValue) throws SerializationException {
      if (newValue instanceof ConfigurationNode) {
         this.from((ConfigurationNode)newValue);
         return this.self();
      } else if (newValue != null) {
         if (!(newValue instanceof Collection) && !(newValue instanceof Map)) {
            return this.set(newValue.getClass(), newValue);
         } else {
            this.insertNewValue(newValue, false);
            return this.self();
         }
      } else {
         Object key = this.key;
         if (this.parent != null && key != null) {
            this.parent.removeChild(key);
         } else {
            this.clear();
         }

         return this.self();
      }
   }

   @Override
   public N from(final ConfigurationNode that) {
      if (that == this) {
         return this.self();
      } else {
         this.hints.clear();
         this.hints.putAll((Map<? extends RepresentationHint<?>, ? extends Object>)that.ownHints());
         if (that.isList()) {
            this.attachIfNecessary();
            ListConfigValue<N, A> newList = new ListConfigValue<>(this.implSelf());
            synchronized (that) {
               List<? extends ConfigurationNode> children = that.childrenList();

               for (int i = 0; i < children.size(); i++) {
                  A node = this.createNode(i);
                  node.attached = true;
                  node.from(children.get(i));
                  newList.putChild(i, node);
               }
            }

            this.value = newList;
         } else if (that.isMap()) {
            this.attachIfNecessary();
            MapConfigValue<N, A> newMap = new MapConfigValue<>(this.implSelf());
            synchronized (that) {
               for (Entry<Object, ? extends ConfigurationNode> entry : that.childrenMap().entrySet()) {
                  A node = this.createNode(entry.getKey());
                  node.attached = true;
                  node.from(entry.getValue());
                  newMap.putChild(entry.getKey(), node);
               }
            }

            this.value = newMap;
         } else {
            this.raw(that.raw());
         }

         return this.self();
      }
   }

   private void insertNewValue(final Object newValue, final boolean onlyIfNull) {
      if (newValue instanceof ConfigurationNode) {
         throw new IllegalArgumentException("Cannot set a node as the raw value of another node");
      } else {
         this.attachIfNecessary();
         synchronized (this) {
            ConfigValue<N, A> value = this.value;
            ConfigValue<N, A> oldValue = this.value;
            if (!onlyIfNull || oldValue instanceof NullConfigValue) {
               if (newValue instanceof Collection) {
                  if (!(value instanceof ListConfigValue)) {
                     value = new ListConfigValue<>(this.implSelf());
                  }
               } else if (newValue instanceof Map) {
                  if (!(value instanceof MapConfigValue)) {
                     value = new MapConfigValue<>(this.implSelf());
                  }
               } else if (!(value instanceof ScalarConfigValue)) {
                  value = new ScalarConfigValue<>(this.implSelf());
               }

               value.set(newValue);
               this.value = value;
            }
         }
      }
   }

   @Override
   public N mergeFrom(final ConfigurationNode other) {
      if ((this.virtual() || this.empty()) && !other.virtual()) {
         return this.from(other);
      } else {
         this.hints.putAll((Map<? extends RepresentationHint<?>, ? extends Object>)other.ownHints());
         if (other.isMap()) {
            synchronized (this) {
               ConfigValue<N, A> newValue = this.value;
               ConfigValue<N, A> oldValue = this.value;
               if (!(oldValue instanceof MapConfigValue)) {
                  if (!(oldValue instanceof NullConfigValue)) {
                     return this.self();
                  }

                  newValue = new MapConfigValue<>(this.implSelf());
               }

               for (Entry<Object, ? extends ConfigurationNode> ent : other.childrenMap().entrySet()) {
                  A currentChild = newValue.child(ent.getKey());
                  if (currentChild == null || currentChild.raw() == null || ent.getValue().raw() != null) {
                     A newChild = this.createNode(ent.getKey());
                     newChild.attached = true;
                     newChild.from(ent.getValue());
                     A existing = newValue.putChildIfAbsent(ent.getKey(), newChild);
                     if (existing != null) {
                        existing.mergeFrom(newChild);
                     }
                  }
               }

               this.value = newValue;
            }
         } else if (other.isList()) {
            if (this.virtual()) {
               this.from(other);
            }
         } else if (other.rawScalar() != null) {
            this.insertNewValue(other.rawScalar(), true);
         }

         return this.self();
      }
   }

   @Nullable
   @Override
   public final Object raw() {
      return this.value.get();
   }

   @Override
   public final N raw(final Object newValue) {
      if (newValue == null) {
         Object key = this.key;
         if (this.parent != null && key != null) {
            this.parent.removeChild(key);
         } else {
            this.clear();
         }
      } else {
         this.insertNewValue(newValue, false);
      }

      return this.self();
   }

   @Nullable
   @Override
   public final Object rawScalar() {
      ConfigValue<N, A> value = this.value;
      return value instanceof ScalarConfigValue ? value.get() : null;
   }

   @Override
   public final N node(final Object... path) {
      A pointer = this.implSelf();

      for (Object el : path) {
         pointer = pointer.child(Objects.requireNonNull(el, () -> "element in path " + Arrays.toString(path)), false);
      }

      return pointer.self();
   }

   @Override
   public final N node(final Iterable<?> path) {
      A pointer = this.implSelf();

      for (Object el : path) {
         pointer = pointer.child(Objects.requireNonNull(el, () -> "element in path " + path), false);
      }

      return pointer.self();
   }

   @Override
   public final boolean hasChild(final Object... path) {
      A pointer = this.implSelf();

      for (Object el : path) {
         A child = pointer.value.child(Objects.requireNonNull(el, () -> "element in path " + Arrays.toString(path)));
         if (child == null) {
            return false;
         }

         pointer = child;
      }

      return true;
   }

   @Override
   public final boolean hasChild(final Iterable<?> path) {
      A pointer = this.implSelf();

      for (Object el : path) {
         A child = pointer.value.child(Objects.requireNonNull(el, () -> "element in path " + path));
         if (child == null) {
            return false;
         }

         pointer = child;
      }

      return true;
   }

   @Override
   public final boolean virtual() {
      return !this.attached;
   }

   @Override
   public boolean isNull() {
      return this.value instanceof NullConfigValue;
   }

   @Override
   public final boolean isList() {
      return this.value instanceof ListConfigValue;
   }

   @Override
   public final boolean isMap() {
      return this.value instanceof MapConfigValue;
   }

   @Override
   public final List<N> childrenList() {
      ConfigValue<N, A> value = this.value;
      return value instanceof ListConfigValue ? ((ListConfigValue)value).unwrapped() : Collections.emptyList();
   }

   @Override
   public final Map<Object, N> childrenMap() {
      ConfigValue<N, A> value = this.value;
      return value instanceof MapConfigValue ? ((MapConfigValue)value).unwrapped() : Collections.emptyMap();
   }

   @Override
   public boolean empty() {
      return this.value.isEmpty();
   }

   protected final A child(final Object key, final boolean attach) {
      A child = this.value.child(key);
      if (child == null) {
         if (attach) {
            this.attachIfNecessary();
            A existingChild = this.value.putChildIfAbsent(key, child = this.createNode(key));
            if (existingChild != null) {
               child = existingChild;
            } else {
               this.attachChild(child);
            }
         } else {
            child = this.createNode(key);
         }
      }

      return child;
   }

   @Override
   public final boolean removeChild(final Object key) {
      return detachIfNonNull(this.value.putChild(key, null)) != null;
   }

   @Nullable
   private static <N extends ScopedConfigurationNode<N>, T extends AbstractConfigurationNode<N, T>> T detachIfNonNull(@Nullable final T node) {
      if (node != null) {
         node.attached = false;
         node.clear();
      }

      return node;
   }

   @Override
   public final N appendListNode() {
      return this.child(ListConfigValue.UNALLOCATED_IDX, false).self();
   }

   @Nullable
   @Override
   public final Object key() {
      return this.key;
   }

   @Override
   public final NodePath path() {
      N pointer = this.self();
      if (pointer.parent() == null) {
         return NodePath.path();
      } else {
         Deque<Object> pathElements = new ArrayDeque<>();

         do {
            pathElements.addFirst(pointer.key());
            pointer = Objects.requireNonNull(pointer.parent());
         } while (pointer.parent() != null);

         return NodePath.of(pathElements);
      }
   }

   @Override
   public final N parent() {
      A parent = this.parent;
      return parent == null ? null : parent.self();
   }

   @Override
   public final ConfigurationOptions options() {
      return this.options;
   }

   @Override
   public final N copy() {
      return this.copy(null).self();
   }

   protected abstract A copy(@Nullable A parent);

   final A parentEnsureAttached() {
      A parent = this.parent;
      if (parent != null && parent.virtual()) {
         parent = parent.parentEnsureAttached().attachChildIfAbsent(parent);
      }

      return this.parent = parent;
   }

   protected final void attachIfNecessary() {
      if (!this.attached) {
         A parent = this.parentEnsureAttached();
         if (parent != null) {
            parent.attachChild(this.implSelf());
         }
      }
   }

   protected final A attachChildIfAbsent(final A child) {
      return this.attachChild(child, true);
   }

   final void attachChild(final A child) {
      this.attachChild(child, false);
   }

   private A attachChild(final A child, final boolean onlyIfAbsent) {
      if (this.virtual()) {
         throw new IllegalStateException("This parent is not currently attached. This is an internal state violation.");
      } else if (!Objects.equals(child.parentEnsureAttached(), this)) {
         throw new IllegalStateException("Child " + child + " path is not a direct parent of me (" + this.path() + "), cannot attach");
      } else {
         ConfigValue<N, A> oldValue;
         ConfigValue<N, A> newValue;
         synchronized (this) {
            oldValue = this.value;
            newValue = this.value;
            if (oldValue instanceof MapConfigValue) {
               if (child.key == ListConfigValue.UNALLOCATED_IDX) {
                  newValue = new ListConfigValue<>(this.implSelf());
               }
            } else if (!ListConfigValue.likelyNewListKey(child.key) && !ListConfigValue.likelyListKey(oldValue, child.key)) {
               newValue = new MapConfigValue<>(this.implSelf());
            } else if (oldValue instanceof NullConfigValue) {
               newValue = new ListConfigValue<>(this.implSelf());
            } else if (!(oldValue instanceof ListConfigValue)) {
               newValue = new ListConfigValue<>(this.implSelf(), oldValue.get());
            }

            Object childKey = child.key;
            if (childKey == null) {
               throw new IllegalArgumentException("Cannot attach a child with null key");
            }

            if (onlyIfAbsent) {
               A oldChild = newValue.putChildIfAbsent(childKey, child);
               if (oldChild != null) {
                  return oldChild;
               }
            } else {
               detachIfNonNull(newValue.putChild(childKey, child));
            }

            this.value = newValue;
         }

         if (newValue != oldValue) {
            oldValue.clear();
         }

         child.attached = true;
         return child;
      }
   }

   protected final void clear() {
      synchronized (this) {
         ConfigValue<N, A> oldValue = this.value;
         this.value = NullConfigValue.instance();
         oldValue.clear();
      }
   }

   @Override
   public final <S, T, E extends Exception> T visit(final ConfigurationVisitor<S, T, E> visitor, final S state) throws E {
      return this.visitInternal(visitor, state);
   }

   @Override
   public final <S, T> T visit(final ConfigurationVisitor.Safe<S, T> visitor, final S state) {
      try {
         return this.visitInternal(visitor, state);
      } catch (VisitorSafeNoopException var4) {
         throw new AssertionError("Exception was thrown on a Safe visitor", var4);
      }
   }

   private <S, T, E extends Exception> T visitInternal(final ConfigurationVisitor<S, T, E> visitor, final S state) throws E {
      visitor.beginVisit(this.self(), state);
      if (!(this.value instanceof NullConfigValue)) {
         LinkedList<Object> toVisit = new LinkedList<>();
         toVisit.add(this);

         Object active;
         while ((active = toVisit.pollFirst()) != null) {
            A current = VisitorNodeEnd.popFromVisitor(active, visitor, state);
            if (current != null) {
               try {
                  visitor.enterNode(current.self(), state);
                  ConfigValue<N, A> value = current.value;
                  if (value instanceof MapConfigValue) {
                     visitor.enterMappingNode(current.self(), state);
                     toVisit.addFirst(new VisitorNodeEnd(current, true));
                     toVisit.addAll(0, ((MapConfigValue)value).values.values());
                  } else if (value instanceof ListConfigValue) {
                     visitor.enterListNode(current.self(), state);
                     toVisit.addFirst(new VisitorNodeEnd(current, false));
                     toVisit.addAll(0, ((ListConfigValue)value).values);
                  } else if (value instanceof ScalarConfigValue) {
                     visitor.enterScalarNode(current.self(), state);
                  } else if (!(value instanceof NullConfigValue)) {
                     throw new IllegalStateException("Unknown value type " + value.getClass() + " at " + current.path());
                  }
               } catch (Exception var7) {
                  if (var7 instanceof ConfigurateException) {
                     ((ConfigurateException)var7).initPath(current::path);
                  }

                  throw var7;
               }
            }
         }
      }

      return visitor.endVisit(state);
   }

   @Override
   public final <V> N hint(final RepresentationHint<V> hint, @Nullable final V value) {
      if (value == null) {
         this.hints.remove(hint);
      } else {
         this.hints.put(hint, value);
      }

      return this.self();
   }

   @Override
   public final <V> V hint(final RepresentationHint<V> hint) {
      Object value = this.hints.get(hint);
      if (value != null) {
         return (V)value;
      } else {
         A parent = this.parent;
         return parent != null && hint.inheritable() ? parent.hint(hint) : hint.defaultValue();
      }
   }

   @Nullable
   @Override
   public final <V> V ownHint(final RepresentationHint<V> hint) {
      return (V)this.hints.get(hint);
   }

   @Override
   public final Map<RepresentationHint<?>, ?> ownHints() {
      return UnmodifiableCollections.copyOf(this.hints);
   }

   @Override
   public boolean equals(final Object o) {
      if (this == o) {
         return true;
      } else if (!(o instanceof AbstractConfigurationNode)) {
         return false;
      } else {
         AbstractConfigurationNode<?, ?> that = (AbstractConfigurationNode<?, ?>)o;
         return Objects.equals(this.key, that.key) && Objects.equals(this.value, that.value);
      }
   }

   @Override
   public int hashCode() {
      return Objects.hashCode(this.key) ^ Objects.hashCode(this.value);
   }

   @Override
   public String toString() {
      return "AbstractConfigurationNode{key=" + this.key + ", value=" + this.value + '}';
   }

   protected abstract A createNode(Object path);

   protected abstract A implSelf();
}
