package com.google.common.collect;

import com.google.common.annotations.GwtCompatible;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.google.errorprone.annotations.CompatibleWith;
import com.google.errorprone.annotations.DoNotMock;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import javax.annotation.CheckForNull;

@DoNotMock("Use ImmutableTable, HashBasedTable, or another implementation")
@ElementTypesAreNonnullByDefault
@GwtCompatible
public interface Table<R, C, V> {
   boolean contains(@CheckForNull @CompatibleWith("R") Object rowKey, @CheckForNull @CompatibleWith("C") Object columnKey);

   boolean containsRow(@CheckForNull @CompatibleWith("R") Object rowKey);

   boolean containsColumn(@CheckForNull @CompatibleWith("C") Object columnKey);

   boolean containsValue(@CheckForNull @CompatibleWith("V") Object value);

   @CheckForNull
   V get(@CheckForNull @CompatibleWith("R") Object rowKey, @CheckForNull @CompatibleWith("C") Object columnKey);

   boolean isEmpty();

   int size();

   @Override
   boolean equals(@CheckForNull Object obj);

   @Override
   int hashCode();

   void clear();

   @CheckForNull
   @CanIgnoreReturnValue
   V put(@ParametricNullness R rowKey, @ParametricNullness C columnKey, @ParametricNullness V value);

   void putAll(Table<? extends R, ? extends C, ? extends V> table);

   @CheckForNull
   @CanIgnoreReturnValue
   V remove(@CheckForNull @CompatibleWith("R") Object rowKey, @CheckForNull @CompatibleWith("C") Object columnKey);

   Map<C, V> row(@ParametricNullness R rowKey);

   Map<R, V> column(@ParametricNullness C columnKey);

   Set<Table.Cell<R, C, V>> cellSet();

   Set<R> rowKeySet();

   Set<C> columnKeySet();

   Collection<V> values();

   Map<R, Map<C, V>> rowMap();

   Map<C, Map<R, V>> columnMap();

   public interface Cell<R, C, V> {
      @ParametricNullness
      R getRowKey();

      @ParametricNullness
      C getColumnKey();

      @ParametricNullness
      V getValue();

      @Override
      boolean equals(@CheckForNull Object obj);

      @Override
      int hashCode();
   }
}
