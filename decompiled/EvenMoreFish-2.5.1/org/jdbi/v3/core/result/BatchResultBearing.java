package org.jdbi.v3.core.result;

import java.lang.reflect.Type;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Stream;
import org.jdbi.v3.core.generic.GenericType;
import org.jdbi.v3.core.mapper.ColumnMapper;
import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.mapper.RowViewMapper;
import org.jdbi.v3.core.qualifier.QualifiedType;

public final class BatchResultBearing implements ResultBearing {
   private final ResultBearing delegate;
   private final Supplier<int[]> modifiedRowCountsSupplier;

   public BatchResultBearing(ResultBearing delegate, Supplier<int[]> modifiedRowCountsSupplier) {
      this.delegate = delegate;
      this.modifiedRowCountsSupplier = modifiedRowCountsSupplier;
   }

   @Override
   public <R> R scanResultSet(ResultSetScanner<R> resultSetScanner) {
      return this.delegate.scanResultSet(resultSetScanner);
   }

   public <T> BatchResultIterable<T> mapTo(Class<T> type) {
      return BatchResultIterable.of(this.delegate.mapTo(type), this.modifiedRowCountsSupplier);
   }

   public <T> BatchResultIterable<T> mapTo(GenericType<T> type) {
      return BatchResultIterable.of(this.delegate.mapTo(type), this.modifiedRowCountsSupplier);
   }

   public BatchResultIterable<?> mapTo(Type type) {
      return BatchResultIterable.of(this.delegate.mapTo(type), this.modifiedRowCountsSupplier);
   }

   public <T> BatchResultIterable<T> mapTo(QualifiedType<T> type) {
      return BatchResultIterable.of(this.delegate.mapTo(type), this.modifiedRowCountsSupplier);
   }

   public <T> BatchResultIterable<T> mapToBean(Class<T> type) {
      return BatchResultIterable.of(this.delegate.mapToBean(type), this.modifiedRowCountsSupplier);
   }

   public BatchResultIterable<Map<String, Object>> mapToMap() {
      return BatchResultIterable.of(this.delegate.mapToMap(), this.modifiedRowCountsSupplier);
   }

   public <T> BatchResultIterable<Map<String, T>> mapToMap(Class<T> valueType) {
      return BatchResultIterable.of(this.delegate.mapToMap(valueType), this.modifiedRowCountsSupplier);
   }

   public <T> BatchResultIterable<Map<String, T>> mapToMap(GenericType<T> valueType) {
      return BatchResultIterable.of(this.delegate.mapToMap(valueType), this.modifiedRowCountsSupplier);
   }

   public <T> BatchResultIterable<T> map(ColumnMapper<T> mapper) {
      return BatchResultIterable.of(this.delegate.map(mapper), this.modifiedRowCountsSupplier);
   }

   public <T> BatchResultIterable<T> map(RowMapper<T> mapper) {
      return BatchResultIterable.of(this.delegate.map(mapper), this.modifiedRowCountsSupplier);
   }

   public <T> BatchResultIterable<T> map(RowViewMapper<T> mapper) {
      return BatchResultIterable.of(this.delegate.map(mapper), this.modifiedRowCountsSupplier);
   }

   @Override
   public <C, R> Stream<R> reduceRows(RowReducer<C, R> rowReducer) {
      return this.delegate.reduceRows(rowReducer);
   }

   @Override
   public <K, V> Stream<V> reduceRows(BiConsumer<Map<K, V>, RowView> accumulator) {
      return this.delegate.reduceRows(accumulator);
   }

   @Override
   public <U> U reduceRows(U seed, BiFunction<U, RowView, U> accumulator) {
      return this.delegate.reduceRows(seed, accumulator);
   }

   @Override
   public <U> U reduceResultSet(U seed, ResultSetAccumulator<U> accumulator) {
      return this.delegate.reduceResultSet(seed, accumulator);
   }

   @Override
   public <A, R> R collectRows(Collector<RowView, A, R> collector) {
      return this.delegate.collectRows(collector);
   }

   @Override
   public <R> R collectInto(Class<R> containerType) {
      return this.delegate.collectInto(containerType);
   }

   @Override
   public <R> R collectInto(GenericType<R> containerType) {
      return this.delegate.collectInto(containerType);
   }

   @Override
   public Object collectInto(Type containerType) {
      return this.delegate.collectInto(containerType);
   }

   public int[] modifiedRowCounts() {
      return this.modifiedRowCountsSupplier.get();
   }
}
