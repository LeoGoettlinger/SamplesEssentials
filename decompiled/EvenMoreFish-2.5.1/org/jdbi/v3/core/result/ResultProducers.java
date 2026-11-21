package org.jdbi.v3.core.result;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.function.Supplier;
import org.jdbi.v3.core.config.JdbiConfig;
import org.jdbi.v3.core.result.internal.EmptyResultSet;
import org.jdbi.v3.core.statement.StatementContext;

public class ResultProducers implements JdbiConfig<ResultProducers> {
   private boolean allowNoResults = false;

   public ResultProducers() {
   }

   private ResultProducers(ResultProducers that) {
      this.allowNoResults = that.allowNoResults;
   }

   public static ResultProducer<Integer> returningUpdateCount() {
      return (statementSupplier, ctx) -> {
         StatementContext unused = ctx;

         Integer t$;
         try {
            t$ = statementSupplier.get().getUpdateCount();
         } catch (Throwable var6) {
            if (ctx != null) {
               try {
                  unused.close();
               } catch (Throwable var5) {
                  var6.addSuppressed(var5);
               }
            }

            throw var6;
         }

         if (ctx != null) {
            ctx.close();
         }

         return t$;
      };
   }

   public static ResultProducer<Long> returningLargeUpdateCount() {
      return (statementSupplier, ctx) -> {
         StatementContext unused = ctx;

         Long t$;
         try {
            t$ = statementSupplier.get().getLargeUpdateCount();
         } catch (Throwable var6) {
            if (ctx != null) {
               try {
                  unused.close();
               } catch (Throwable var5) {
                  var6.addSuppressed(var5);
               }
            }

            throw var6;
         }

         if (ctx != null) {
            ctx.close();
         }

         return t$;
      };
   }

   public static ResultProducer<ResultBearing> returningResults() {
      return (statementSupplier, ctx) -> createResultBearing(statementSupplier, Statement::getResultSet, ctx);
   }

   public static ResultProducer<ResultBearing> returningGeneratedKeys(String... generatedKeyColumnNames) {
      return (preparedStatementSupplier, ctx) -> {
         ctx.setReturningGeneratedKeys(true);
         if (generatedKeyColumnNames.length > 0) {
            ctx.setGeneratedKeysColumnNames(generatedKeyColumnNames);
         }

         return createResultBearing(preparedStatementSupplier, Statement::getGeneratedKeys, ctx);
      };
   }

   public static ResultBearing createResultBearing(
      Supplier<PreparedStatement> preparedStatementSupplier, ResultProducers.ResultSetCreator resultSetCreator, StatementContext ctx
   ) {
      return ResultBearing.of(() -> {
         try {
            ResultSet resultSet = resultSetCreator.createResultSet(preparedStatementSupplier.get());
            if (resultSet == null) {
               if (ctx.getConfig(ResultProducers.class).allowNoResults) {
                  return new EmptyResultSet();
               } else {
                  throw new NoResultsException("Statement returned no results", ctx);
               }
            } else {
               ctx.addCleanable(resultSet::close);
               return resultSet;
            }
         } catch (SQLException var4) {
            throw new ResultSetException("Could not process result set", var4, ctx);
         }
      }, ctx);
   }

   public ResultProducers createCopy() {
      return new ResultProducers(this);
   }

   public ResultProducers allowNoResults(boolean allowNoResults) {
      this.allowNoResults = allowNoResults;
      return this;
   }

   @FunctionalInterface
   public interface ResultSetCreator {
      ResultSet createResultSet(Statement statement) throws SQLException;
   }
}
