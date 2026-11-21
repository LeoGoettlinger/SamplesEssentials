package net.thenextlvl.nbt.tag;

import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;
import org.intellij.lang.annotations.Flow;
import org.jetbrains.annotations.Contract;

public sealed interface ListTag<T extends Tag> extends ValueTag<List<T>>, List<T> permits ListTagImpl {
   byte ID = 9;

   @Contract(
      pure = true
   )
   byte getContentTypeId();

   @SafeVarargs
   @Contract(
      value = "_, _ -> new",
      pure = true
   )
   static <T extends Tag> ListTag<T> of(byte contentTypeId, T... content) throws IllegalArgumentException {
      return of(contentTypeId, List.of(content));
   }

   @Contract(
      value = "_, _ -> new",
      pure = true
   )
   static <T extends Tag> ListTag<T> of(byte contentTypeId, List<T> content) throws IllegalArgumentException {
      return builder().contentType(contentTypeId).addAll(content).build();
   }

   @SafeVarargs
   @Contract(
      value = "_ -> new",
      pure = true
   )
   static <T extends Tag> ListTag<T> of(T... content) throws IllegalArgumentException {
      return of(List.of(content));
   }

   @Contract(
      value = "_ -> new",
      pure = true
   )
   static <T extends Tag> ListTag<T> of(List<T> content) throws IllegalArgumentException {
      return builder().addAll(content).build();
   }

   @Contract(
      value = "_ -> new",
      pure = true
   )
   static <T extends Tag> ListTag<T> empty(byte contentTypeId) {
      return new ListTagImpl<>(contentTypeId);
   }

   @Contract(
      value = " -> new",
      pure = true
   )
   static <T extends Tag> ListTag.Builder<T> builder() {
      return new ListTagImpl.Builder<>();
   }

   @Deprecated
   @Contract("_ -> fail")
   default boolean add(T t) throws UnsupportedOperationException {
      throw new UnsupportedOperationException("ListTag is immutable");
   }

   @Deprecated
   @Contract("_, _ -> fail")
   default void add(int i, T t) throws UnsupportedOperationException {
      throw new UnsupportedOperationException("ListTag is immutable");
   }

   @Deprecated
   @Contract("_ -> fail")
   @Override
   default boolean addAll(Collection<? extends T> collection) throws UnsupportedOperationException {
      throw new UnsupportedOperationException("ListTag is immutable");
   }

   @Deprecated
   @Contract("_, _ -> fail")
   @Override
   default boolean addAll(int i, Collection<? extends T> collection) throws UnsupportedOperationException {
      throw new UnsupportedOperationException("ListTag is immutable");
   }

   @Deprecated
   @Contract("_ -> fail")
   @Override
   default boolean remove(Object o) throws UnsupportedOperationException {
      throw new UnsupportedOperationException("ListTag is immutable");
   }

   @Deprecated
   @Contract("_ -> fail")
   @Override
   default boolean removeAll(Collection<?> collection) throws UnsupportedOperationException {
      throw new UnsupportedOperationException("ListTag is immutable");
   }

   @Deprecated
   @Contract("_ -> fail")
   @Override
   default boolean retainAll(Collection<?> collection) throws UnsupportedOperationException {
      throw new UnsupportedOperationException("ListTag is immutable");
   }

   @Deprecated
   @Contract("_ -> fail")
   default void addFirst(T t) throws UnsupportedOperationException {
      throw new UnsupportedOperationException("ListTag is immutable");
   }

   @Deprecated
   @Contract("_ -> fail")
   default void addLast(T t) throws UnsupportedOperationException {
      throw new UnsupportedOperationException("ListTag is immutable");
   }

   @Deprecated
   @Contract("_ -> fail")
   @Override
   default boolean removeIf(Predicate<? super T> filter) throws UnsupportedOperationException {
      throw new UnsupportedOperationException("ListTag is immutable");
   }

   @Deprecated
   @Contract("_ -> fail")
   default T remove(int i) throws UnsupportedOperationException {
      throw new UnsupportedOperationException("ListTag is immutable");
   }

   @Deprecated
   @Contract(" -> fail")
   default T removeFirst() throws UnsupportedOperationException {
      throw new UnsupportedOperationException("ListTag is immutable");
   }

   @Deprecated
   @Contract(" -> fail")
   default T removeLast() throws UnsupportedOperationException {
      throw new UnsupportedOperationException("ListTag is immutable");
   }

   @Deprecated
   @Contract("_ -> fail")
   @Override
   default void replaceAll(UnaryOperator<T> operator) throws UnsupportedOperationException {
      throw new UnsupportedOperationException("ListTag is immutable");
   }

   @Deprecated
   @Contract(" -> fail")
   @Override
   default void clear() throws UnsupportedOperationException {
      throw new UnsupportedOperationException("ListTag is immutable");
   }

   @Deprecated
   @Contract("_, _ -> fail")
   default T set(int i, T t) throws UnsupportedOperationException {
      throw new UnsupportedOperationException("ListTag is immutable");
   }

   public sealed interface Builder<T extends Tag> permits ListTagImpl.Builder {
      @Contract(
         value = "_ -> this",
         mutates = "this"
      )
      ListTag.Builder<T> contentType(byte var1) throws IllegalStateException;

      @Contract(
         value = "_ -> this",
         mutates = "this"
      )
      ListTag.Builder<T> add(T var1) throws IllegalArgumentException;

      @Contract(
         value = "_, _ -> this",
         mutates = "this"
      )
      ListTag.Builder<T> add(int var1, T var2) throws IllegalArgumentException;

      @Contract(
         value = "_ -> this",
         mutates = "this"
      )
      ListTag.Builder<T> addFirst(T var1) throws IllegalArgumentException;

      @Contract(
         value = "_ -> this",
         mutates = "this"
      )
      ListTag.Builder<T> addLast(T var1) throws IllegalArgumentException;

      @Contract(
         value = "_ -> this",
         mutates = "this"
      )
      @Flow(
         sourceIsContainer = true,
         targetIsContainer = true
      )
      ListTag.Builder<T> addAll(Iterable<? extends T> var1) throws IllegalArgumentException;

      @Contract(
         value = "_ -> this",
         mutates = "this"
      )
      ListTag.Builder<T> replaceAll(UnaryOperator<T> var1) throws IllegalArgumentException;

      @Contract(
         value = "_, _ -> this",
         mutates = "this"
      )
      ListTag.Builder<T> set(int var1, T var2) throws IllegalArgumentException;

      @Contract(
         value = "_ -> this",
         mutates = "this"
      )
      ListTag.Builder<T> remove(T var1);

      @Contract(
         value = "_ -> this",
         mutates = "this"
      )
      ListTag.Builder<T> removeIf(Predicate<? super T> var1);

      @Contract(
         value = "_ -> this",
         mutates = "this"
      )
      ListTag.Builder<T> removeAll(Iterable<? extends T> var1) throws IllegalArgumentException;

      @Contract(
         value = " -> this",
         mutates = "this"
      )
      ListTag.Builder<T> clear();

      @Contract(
         pure = true
      )
      boolean isEmpty();

      @Contract(
         pure = true
      )
      int size();

      @Contract(
         value = " -> new",
         pure = true
      )
      ListTag<T> build();
   }
}
