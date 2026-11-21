package org.jdbi.v3.core.result.internal;

import java.lang.reflect.Type;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collector;
import org.jdbi.v3.core.generic.GenericTypes;
import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.result.ResultIterable;
import org.jdbi.v3.core.result.ResultIterator;
import org.jdbi.v3.core.result.ResultSetException;
import org.jdbi.v3.core.result.UnableToProduceResultException;
import org.jdbi.v3.core.statement.StatementContext;

public class ResultSetResultIterable<T> implements ResultIterable<T> {
   private final RowMapper<T> mapper;
   private final StatementContext ctx;
   private final Supplier<ResultSet> resultSetSupplier;

   public ResultSetResultIterable(RowMapper<T> mapper, StatementContext ctx, Supplier<ResultSet> resultSetSupplier) {
      this.mapper = mapper;
      this.ctx = ctx;
      this.resultSetSupplier = resultSetSupplier;
   }

   @Override
   public ResultIterator<T> iterator() {
      try {
         return new ResultSetResultIterator<>(this.resultSetSupplier, this.mapper, this.ctx);
      } catch (SQLException var2) {
         throw new ResultSetException("Unable to iterate result set", var2, this.ctx);
      }
   }

   @Override
   public <R> R collectInto(Type containerType) {
      Type type = containerType;
      if (containerType instanceof Class<?> clazz) {
         type = GenericTypes.parameterizeClass(clazz, Object.class);
      }

      Collector<? super T, ?, R> collector = (Collector<? super T, ?, R>)this.ctx
         .findCollectorFor(type)
         .orElseThrow(() -> new UnableToProduceResultException("Could not find collector for " + containerType));
      return this.collect(collector);
   }

   @Override
   public List<T> collectIntoList() {
      return this.collectInto(List.class);
   }

   @Override
   public Set<T> collectIntoSet() {
      return this.collectInto(Set.class);
   }
}
