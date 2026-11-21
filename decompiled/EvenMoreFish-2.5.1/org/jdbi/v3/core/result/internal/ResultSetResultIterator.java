package org.jdbi.v3.core.result.internal;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.NoSuchElementException;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;
import org.jdbi.v3.core.internal.exceptions.Sneaky;
import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.result.ResultIterator;
import org.jdbi.v3.core.result.ResultSetException;
import org.jdbi.v3.core.statement.StatementContext;

class ResultSetResultIterator<T> implements ResultIterator<T> {
   private final ResultSet resultSet;
   private final RowMapper<T> rowMapper;
   private final ResultSetSupplier resultSetSupplier;
   private final StatementContext context;
   private final AtomicLong mappedRows = new AtomicLong();
   private volatile boolean alreadyAdvanced = false;
   private volatile boolean hasNext = false;
   private volatile boolean closed = false;

   ResultSetResultIterator(Supplier<ResultSet> resultSetSupplier, RowMapper<T> rowMapper, StatementContext context) throws SQLException {
      this.context = context;
      if (resultSetSupplier instanceof ResultSetSupplier rss) {
         this.resultSetSupplier = rss;
      } else {
         this.resultSetSupplier = ResultSetSupplier.closingContext(resultSetSupplier, context);
      }

      this.resultSet = this.resultSetSupplier.get();
      if (this.resultSet != null) {
         context.addCleanable(this.resultSet::close);
         this.rowMapper = rowMapper.specialize(this.resultSet, context);
      } else {
         this.close();
         this.rowMapper = null;
      }
   }

   @Override
   public void close() {
      this.closed = true;
      this.context.setMappedRows(this.mappedRows.get());

      try {
         this.resultSetSupplier.close();
      } catch (SQLException var2) {
         throw Sneaky.throwAnyway(var2);
      }
   }

   @Override
   public boolean hasNext() {
      if (this.closed) {
         return false;
      } else if (this.alreadyAdvanced) {
         return this.hasNext;
      } else {
         this.hasNext = this.safeNext();
         if (this.hasNext) {
            this.alreadyAdvanced = true;
         } else {
            this.close();
         }

         return this.hasNext;
      }
   }

   @Override
   public T next() {
      if (!this.hasNext()) {
         this.close();
         throw new NoSuchElementException("No element to advance to");
      } else {
         this.mappedRows.incrementAndGet();

         Object e;
         try {
            e = this.rowMapper.map(this.resultSet, this.context);
         } catch (SQLException var5) {
            throw new ResultSetException("Exception thrown mapping result set into return type", var5, this.context);
         } finally {
            this.alreadyAdvanced = this.safeNext();
            if (!this.alreadyAdvanced) {
               this.close();
            }
         }

         return (T)e;
      }
   }

   @Override
   public StatementContext getContext() {
      return this.context;
   }

   @Override
   public void remove() {
      throw new UnsupportedOperationException("Deleting from a result set iterator is not yet supported");
   }

   private boolean safeNext() {
      try {
         return this.resultSet.next();
      } catch (SQLException var2) {
         throw new ResultSetException("Unable to advance result set", var2, this.context);
      }
   }
}
