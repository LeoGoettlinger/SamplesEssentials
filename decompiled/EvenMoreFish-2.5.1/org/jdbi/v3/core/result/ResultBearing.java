package org.jdbi.v3.core.result;

import java.lang.reflect.Type;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Stream;
import org.jdbi.v3.core.collector.ElementTypeNotFoundException;
import org.jdbi.v3.core.collector.NoSuchCollectorException;
import org.jdbi.v3.core.generic.GenericType;
import org.jdbi.v3.core.mapper.ColumnMapper;
import org.jdbi.v3.core.mapper.GenericMapMapperFactory;
import org.jdbi.v3.core.mapper.MapMapper;
import org.jdbi.v3.core.mapper.NoSuchMapperException;
import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.mapper.RowViewMapper;
import org.jdbi.v3.core.mapper.SingleColumnMapper;
import org.jdbi.v3.core.mapper.reflect.BeanMapper;
import org.jdbi.v3.core.qualifier.QualifiedType;
import org.jdbi.v3.core.result.internal.RowViewImpl;
import org.jdbi.v3.core.statement.StatementContext;

public interface ResultBearing {
   static ResultBearing of(Supplier<ResultSet> resultSetSupplier, StatementContext ctx) {
      return new ResultBearing() {
         @Override
         public <R> R scanResultSet(ResultSetScanner<R> resultSetScanner) {
            try {
               return resultSetScanner.scanResultSet(resultSetSupplier, ctx);
            } catch (SQLException var3) {
               throw new ResultSetException("Error reading result set", var3, ctx);
            }
         }
      };
   }

   <R> R scanResultSet(ResultSetScanner<R> resultSetScanner);

   default <T> ResultIterable<T> mapTo(Class<T> type) {
      return this.mapTo(QualifiedType.of(type));
   }

   default <T> ResultIterable<T> mapTo(GenericType<T> type) {
      return this.mapTo(QualifiedType.of(type));
   }

   default ResultIterable<?> mapTo(Type type) {
      return this.mapTo(QualifiedType.of(type));
   }

   default <T> ResultIterable<T> mapTo(QualifiedType<T> type) {
      return this.scanResultSet((resultSetSupplier, ctx) -> {
         RowMapper<T> rowMapper = ctx.findMapperFor(type).orElseThrow(() -> new NoSuchMapperException("No mapper registered for type " + type));
         return ResultIterable.of(resultSetSupplier, rowMapper, ctx);
      });
   }

   default <T> ResultIterable<T> mapToBean(Class<T> type) {
      return this.map(BeanMapper.of(type));
   }

   default ResultIterable<Map<String, Object>> mapToMap() {
      return this.map(new MapMapper());
   }

   default <T> ResultIterable<Map<String, T>> mapToMap(Class<T> valueType) {
      return this.scanResultSet(
         (resultSetSupplier, ctx) -> ResultIterable.of(resultSetSupplier, GenericMapMapperFactory.getMapperForValueType(valueType, ctx.getConfig()), ctx)
      );
   }

   default <T> ResultIterable<Map<String, T>> mapToMap(GenericType<T> valueType) {
      return this.scanResultSet(
         (resultSetSupplier, ctx) -> ResultIterable.of(resultSetSupplier, GenericMapMapperFactory.getMapperForValueType(valueType, ctx.getConfig()), ctx)
      );
   }

   default <T> ResultIterable<T> map(ColumnMapper<T> mapper) {
      return this.map(new SingleColumnMapper<>(mapper));
   }

   default <T> ResultIterable<T> map(RowMapper<T> mapper) {
      return this.scanResultSet((resultSetSupplier, ctx) -> ResultIterable.of(resultSetSupplier, mapper, ctx));
   }

   default <T> ResultIterable<T> map(RowViewMapper<T> mapper) {
      return this.map(mapper);
   }

   default <C, R> Stream<R> reduceRows(RowReducer<C, R> rowReducer) {
      return this.scanResultSet((resultSetSupplier, context) -> {
         try {
            StatementContext ctx = context;

            Stream var7;
            try {
               ResultSet resultSet = resultSetSupplier.get();
               RowView rowView = new RowViewImpl(resultSet, ctx);
               C container = rowReducer.container();

               while (resultSet.next()) {
                  rowReducer.accumulate(container, rowView);
               }

               var7 = rowReducer.stream(container).onClose(ctx::close);
            } catch (Throwable var9) {
               if (context != null) {
                  try {
                     ctx.close();
                  } catch (Throwable var8) {
                     var9.addSuppressed(var8);
                  }
               }

               throw var9;
            }

            if (context != null) {
               context.close();
            }

            return var7;
         } catch (SQLException var10) {
            throw new UnableToProduceResultException(var10, context);
         }
      });
   }

   default <K, V> Stream<V> reduceRows(BiConsumer<Map<K, V>, RowView> accumulator) {
      return this.reduceRows(accumulator::accept);
   }

   default <U> U reduceRows(U seed, BiFunction<U, RowView, U> accumulator) {
      return this.scanResultSet((resultSetSupplier, context) -> {
         try {
            StatementContext ctx = context;

            Object var8;
            try {
               ResultSet resultSet = resultSetSupplier.get();
               RowView rowView = new RowViewImpl(resultSet, ctx);
               U result = seed;

               while (resultSet.next()) {
                  result = accumulator.apply(result, rowView);
               }

               var8 = result;
            } catch (Throwable var10) {
               if (context != null) {
                  try {
                     ctx.close();
                  } catch (Throwable var9) {
                     var10.addSuppressed(var9);
                  }
               }

               throw var10;
            }

            if (context != null) {
               context.close();
            }

            return (U)var8;
         } catch (SQLException var11) {
            throw new UnableToProduceResultException(var11, context);
         }
      });
   }

   default <U> U reduceResultSet(U seed, ResultSetAccumulator<U> accumulator) {
      return this.scanResultSet((resultSetSupplier, context) -> {
         try {
            Object var7;
            try (StatementContext ctx = context) {
               ResultSet resultSet = resultSetSupplier.get();
               U result = seed;

               while (resultSet.next()) {
                  result = accumulator.apply(result, resultSet, ctx);
               }

               var7 = result;
            }

            return (U)var7;
         } catch (SQLException var10) {
            throw new UnableToProduceResultException(var10, context);
         }
      });
   }

   default <A, R> R collectRows(Collector<RowView, A, R> collector) {
      return this.scanResultSet((resultSetSupplier, context) -> {
         try {
            StatementContext ctx = context;

            Object var8;
            try {
               ResultSet resultSet = resultSetSupplier.get();
               RowView rowView = new RowViewImpl(resultSet, ctx);
               A accumulator = collector.supplier().get();
               BiConsumer<A, RowView> consumer = collector.accumulator();

               while (resultSet.next()) {
                  consumer.accept(accumulator, rowView);
               }

               var8 = collector.finisher().apply(accumulator);
            } catch (Throwable var10) {
               if (context != null) {
                  try {
                     ctx.close();
                  } catch (Throwable var9) {
                     var10.addSuppressed(var9);
                  }
               }

               throw var10;
            }

            if (context != null) {
               context.close();
            }

            return (R)var8;
         } catch (SQLException var11) {
            throw new UnableToProduceResultException(var11, context);
         }
      });
   }

   default <R> R collectInto(Class<R> containerType) {
      return (R)this.collectInto(containerType);
   }

   default <R> R collectInto(GenericType<R> containerType) {
      return (R)this.collectInto(containerType.getType());
   }

   default Object collectInto(Type containerType) {
      return this.scanResultSet(
         (resultSetSupplier, ctx) -> {
            Collector collector = ctx.findCollectorFor(containerType)
               .orElseThrow(() -> new NoSuchCollectorException("No collector registered for container type " + containerType));
            Type elementType = ctx.findElementTypeFor(containerType)
               .orElseThrow(() -> new ElementTypeNotFoundException("Unknown element type for container type " + containerType));
            RowMapper<?> rowMapper = ctx.findMapperFor(elementType)
               .orElseThrow(() -> new NoSuchMapperException("No mapper registered for element type " + elementType));
            return ResultIterable.of(resultSetSupplier, rowMapper, ctx).collect(collector);
         }
      );
   }
}
