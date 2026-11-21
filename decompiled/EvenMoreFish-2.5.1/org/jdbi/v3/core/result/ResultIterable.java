package org.jdbi.v3.core.result;

import java.lang.reflect.Type;
import java.sql.ResultSet;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.Spliterators;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import org.jdbi.v3.core.generic.GenericType;
import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.result.internal.ResultSetResultIterable;
import org.jdbi.v3.core.statement.StatementContext;
import org.jdbi.v3.meta.Alpha;

@FunctionalInterface
public interface ResultIterable<T> extends Iterable<T> {
   static <T> ResultIterable<T> of(Supplier<ResultSet> resultSetSupplier, RowMapper<T> mapper, StatementContext ctx) {
      return new ResultSetResultIterable<>(mapper, ctx, resultSetSupplier);
   }

   static <T> ResultIterable<T> of(ResultIterator<T> iterator) {
      return () -> iterator;
   }

   ResultIterator<T> iterator();

   default <X extends Exception> void useIterator(IteratorConsumer<T, X> consumer) throws X {
      this.withIterator(iterator -> {
         consumer.useIterator(iterator);
         return null;
      });
   }

   default <R, X extends Exception> R withIterator(IteratorCallback<T, R, X> callback) throws X {
      Object var3;
      try (ResultIterator<T> iterator = this.iterator()) {
         var3 = callback.withIterator(iterator);
      }

      return (R)var3;
   }

   default <R> ResultIterable<R> map(Function<? super T, ? extends R> mapper) {
      return () -> new ResultIterable.ResultIteratorDelegate<T, R>(this.iterator()) {
         @Override
         public R next() {
            return (R)mapper.apply((T)this.getDelegate().next());
         }
      };
   }

   @Override
   default void forEach(Consumer<? super T> action) {
      this.forEachWithCount(action);
   }

   default int forEachWithCount(Consumer<? super T> action) {
      Objects.requireNonNull(action, "Action required");

      int var4;
      try (ResultIterator<T> iter = this.iterator()) {
         int count = 0;

         while (iter.hasNext()) {
            count++;
            action.accept(iter.next());
         }

         var4 = count;
      }

      return var4;
   }

   default T one() {
      Object var3;
      try (ResultIterator<T> iter = this.iterator()) {
         if (!iter.hasNext()) {
            throw new IllegalStateException("Expected one element, but found none");
         }

         T r = iter.next();
         if (iter.hasNext()) {
            throw new IllegalStateException("Expected one element, but found multiple");
         }

         var3 = r;
      }

      return (T)var3;
   }

   default Optional<T> findOne() {
      Optional var3;
      try (ResultIterator<T> iter = this.iterator()) {
         if (!iter.hasNext()) {
            return Optional.empty();
         }

         T r = iter.next();
         if (iter.hasNext()) {
            throw new IllegalStateException("Expected zero to one elements, but found multiple");
         }

         var3 = Optional.ofNullable(r);
      }

      return var3;
   }

   @Deprecated(
      since = "3.8.0",
      forRemoval = true
   )
   default T findOnly() {
      return this.one();
   }

   default T first() {
      Object var2;
      try (ResultIterator<T> iter = this.iterator()) {
         if (!iter.hasNext()) {
            throw new IllegalStateException("Expected at least one element, but found none");
         }

         var2 = iter.next();
      }

      return (T)var2;
   }

   default Optional<T> findFirst() {
      Optional var2;
      try (ResultIterator<T> iter = this.iterator()) {
         var2 = iter.hasNext() ? Optional.ofNullable(iter.next()) : Optional.empty();
      }

      return var2;
   }

   default Stream<T> stream() {
      ResultIterator<T> iterator = this.iterator();
      return StreamSupport.stream(Spliterators.spliteratorUnknownSize(iterator, 0), false).onClose(iterator::close);
   }

   default <X extends Exception> void useStream(StreamConsumer<T, X> consumer) throws X {
      this.withStream(stream -> {
         consumer.useStream(stream);
         return null;
      });
   }

   default <R, X extends Exception> R withStream(StreamCallback<T, R, X> callback) throws X {
      Object var3;
      try (Stream<T> stream = this.stream()) {
         var3 = callback.withStream(stream);
      }

      return (R)var3;
   }

   default List<T> list() {
      return this.collect(Collectors.toList());
   }

   default Set<T> set() {
      return this.collect(Collectors.toSet());
   }

   default <R> R collect(Collector<? super T, ?, R> collector) {
      Object var3;
      try (Stream<T> stream = this.stream()) {
         var3 = stream.collect(collector);
      }

      return (R)var3;
   }

   default <K, V> Map<K, V> collectToMap(Function<? super T, ? extends K> keyFunction, Function<? super T, ? extends V> valueFunction) {
      return this.collect(Collectors.toMap(keyFunction, valueFunction));
   }

   @Alpha
   default <C extends Collection<T>> C toCollection(Supplier<C> supplier) {
      return this.collect(Collectors.toCollection(supplier));
   }

   @Alpha
   default <R> R collectInto(Type containerType) {
      throw new UnsupportedOperationException();
   }

   @Alpha
   default <R> R collectInto(GenericType<R> containerType) {
      return this.collectInto(containerType.getType());
   }

   @Alpha
   default List<T> collectIntoList() {
      return this.list();
   }

   @Alpha
   default Set<T> collectIntoSet() {
      return this.set();
   }

   default <U> U reduce(U identity, BiFunction<U, T, U> accumulator) {
      Object var4;
      try (Stream<T> stream = this.stream()) {
         var4 = stream.reduce(identity, accumulator, (u, v) -> {
            throw new UnsupportedOperationException("parallel operation not supported");
         });
      }

      return (U)var4;
   }

   default ResultIterable<T> filter(Predicate<? super T> predicate) {
      Objects.requireNonNull(predicate, "Filter required");
      return () -> new ResultIterable.ResultIteratorDelegate<T, T>(this.iterator()) {
         private T next;

         @Override
         public boolean hasNext() {
            return this.next != null || this.findNext();
         }

         @Override
         public T next() {
            if (this.next == null && !this.findNext()) {
               throw new NoSuchElementException("No more filtered results");
            } else {
               T n = this.next;
               this.next = null;
               return n;
            }
         }

         private boolean findNext() {
            this.next = null;

            while (this.getDelegate().hasNext()) {
               T n = (T)this.getDelegate().next();
               if (predicate.test(n)) {
                  this.next = n;
                  return true;
               }
            }

            return false;
         }
      };
   }

   public abstract static class ResultIteratorDelegate<T, R> implements ResultIterator<R> {
      private final ResultIterator<T> delegate;

      ResultIteratorDelegate(ResultIterator<T> del) {
         this.delegate = Objects.requireNonNull(del, "Delegate required");
      }

      @Override
      public boolean hasNext() {
         return this.delegate.hasNext();
      }

      @Override
      public final void close() {
         this.delegate.close();
      }

      @Override
      public final StatementContext getContext() {
         return this.delegate.getContext();
      }

      protected final ResultIterator<T> getDelegate() {
         return this.delegate;
      }
   }
}
