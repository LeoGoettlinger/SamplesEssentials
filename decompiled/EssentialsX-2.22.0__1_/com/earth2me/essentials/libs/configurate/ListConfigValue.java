package com.earth2me.essentials.libs.configurate;

import com.earth2me.essentials.libs.checkerframework.checker.nullness.qual.Nullable;
import com.earth2me.essentials.libs.configurate.serialize.Scalars;
import com.earth2me.essentials.libs.configurate.util.UnmodifiableCollections;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;

final class ListConfigValue<N extends ScopedConfigurationNode<N>, A extends AbstractConfigurationNode<N, A>> implements ConfigValue<N, A> {
   static final AtomicReferenceFieldUpdater<ListConfigValue, List> VALUES_HANDLE = AtomicReferenceFieldUpdater.newUpdater(
      ListConfigValue.class, List.class, "values"
   );
   static final Object UNALLOCATED_IDX = new Object() {
      @Override
      public String toString() {
         return "<list unallocated>";
      }
   };
   private final A holder;
   volatile List<A> values = new ArrayList<>();

   static boolean likelyNewListKey(@Nullable final Object key) {
      return key instanceof Integer && (Integer)key == 0 || key == UNALLOCATED_IDX;
   }

   static boolean likelyListKey(@Nullable final ConfigValue<?, ?> configValue, @Nullable final Object key) {
      if (!(configValue instanceof ListConfigValue)) {
         return false;
      } else if (!(key instanceof Integer)) {
         return false;
      } else {
         ListConfigValue<?, ?> listConfigValue = (ListConfigValue<?, ?>)configValue;
         int keyAsInt = (Integer)key;
         return keyAsInt >= 0 && keyAsInt <= listConfigValue.values.size();
      }
   }

   ListConfigValue(final A holder) {
      this.holder = holder;
   }

   ListConfigValue(final A holder, @Nullable final Object startValue) {
      this.holder = holder;
      if (startValue != null) {
         A child = holder.createNode(0);
         child.attached = true;
         child.raw(startValue);
         this.values.add(child);
      }
   }

   @Override
   public Object get() {
      List<A> values = this.values;
      synchronized (values) {
         List<Object> ret = new ArrayList<>(values.size());

         for (A obj : values) {
            ret.add(obj.raw());
         }

         return ret;
      }
   }

   public List<N> unwrapped() {
      List<A> orig = this.values;
      synchronized (orig) {
         List<N> ret = new ArrayList<>(orig.size());

         for (A element : orig) {
            ret.add(element.self());
         }

         return Collections.unmodifiableList(ret);
      }
   }

   @Override
   public void set(Object value) {
      if (!(value instanceof Collection)) {
         value = Collections.singleton(value);
      }

      Collection<? extends Object> valueAsList = (Collection<? extends Object>)value;
      List<A> newValue = new ArrayList<>(valueAsList.size());
      int count = 0;

      for (Object o : valueAsList) {
         if (o != null) {
            A child = this.holder.createNode(count);
            newValue.add(count, child);
            child.attached = true;
            child.raw(o);
            count++;
         }
      }

      this.detachNodes(VALUES_HANDLE.getAndSet(this, newValue));
   }

   @Nullable
   @Override
   public A putChild(final Object key, @Nullable final A value) {
      return this.putChildInternal(key, value, false);
   }

   @Nullable
   @Override
   public A putChildIfAbsent(final Object key, @Nullable final A value) {
      return this.putChildInternal(key, value, true);
   }

   @Nullable
   private A putChildInternal(final Object index, @Nullable final A value, final boolean onlyIfAbsent) {
      if (index != UNALLOCATED_IDX) {
         return this.putChildInternal(((Integer)index).intValue(), value, onlyIfAbsent);
      } else {
         List<A> values;
         if (value != null) {
            do {
               values = this.values;
               values.add(value);
               value.key = values.lastIndexOf(value);
            } while (!VALUES_HANDLE.compareAndSet(this, values, values));
         }

         return null;
      }
   }

   private A putChildInternal(final int index, final A value, final boolean onlyIfAbsent) {
      A ret = null;

      List<A> values;
      do {
         values = this.values;
         synchronized (values) {
            if (value == null) {
               if (index >= 0 && index < values.size()) {
                  ret = values.remove(index);

                  for (int i = index; i < values.size(); i++) {
                     values.get(i).key = index;
                  }
               }
            } else if (index >= 0 && index < values.size()) {
               if (onlyIfAbsent) {
                  return values.get(index);
               }

               ret = values.set(index, value);
            } else {
               values.add(index, value);
            }
         }
      } while (!VALUES_HANDLE.compareAndSet(this, values, values));

      return ret;
   }

   @Override
   public A child(final Object key) {
      Integer value = Scalars.INTEGER.tryDeserialize(key);
      if (value != null && value >= 0) {
         List<A> values = this.values;
         synchronized (values) {
            return value >= values.size() ? null : values.get(value);
         }
      } else {
         return null;
      }
   }

   @Override
   public Iterable<A> iterateChildren() {
      List<A> values = this.values;
      synchronized (values) {
         return UnmodifiableCollections.copyOf(values);
      }
   }

   public ListConfigValue<N, A> copy(final A holder) {
      ListConfigValue<N, A> copy = new ListConfigValue<>(holder);
      List<A> values = this.values;
      List<A> copyValues;
      synchronized (values) {
         copyValues = new ArrayList<>(values.size());

         for (A obj : values) {
            copyValues.add(obj.copy(holder));
         }
      }

      copy.values = copyValues;
      return copy;
   }

   @Override
   public boolean isEmpty() {
      return this.values.isEmpty();
   }

   private void detachNodes(final List<? extends AbstractConfigurationNode<?, ?>> children) {
      synchronized (children) {
         for (AbstractConfigurationNode<?, ?> node : children) {
            node.attached = false;
            if (Objects.equals(node.parent(), this.holder)) {
               node.clear();
            }
         }
      }
   }

   @Override
   public void clear() {
      List<A> oldValues = VALUES_HANDLE.getAndSet(this, new ArrayList());
      this.detachNodes(oldValues);
   }

   @Override
   public boolean equals(final Object other) {
      if (this == other) {
         return true;
      } else if (!(other instanceof ListConfigValue)) {
         return false;
      } else {
         ListConfigValue<?, ?> that = (ListConfigValue<?, ?>)other;
         return Objects.equals(this.values, that.values);
      }
   }

   @Override
   public int hashCode() {
      return this.values.hashCode();
   }

   @Override
   public String toString() {
      return "ListConfigValue{values=" + this.values.toString() + '}';
   }
}
