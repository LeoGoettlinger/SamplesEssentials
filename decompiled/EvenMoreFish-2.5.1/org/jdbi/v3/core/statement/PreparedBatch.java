package org.jdbi.v3.core.statement;

import java.lang.reflect.Type;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import org.jdbi.v3.core.Handle;
import org.jdbi.v3.core.argument.Argument;
import org.jdbi.v3.core.argument.Arguments;
import org.jdbi.v3.core.argument.NamedArgumentFinder;
import org.jdbi.v3.core.argument.internal.NamedArgumentFinderFactory;
import org.jdbi.v3.core.qualifier.QualifiedType;
import org.jdbi.v3.core.result.BatchResultBearing;
import org.jdbi.v3.core.result.ResultBearing;
import org.jdbi.v3.core.result.ResultIterator;
import org.jdbi.v3.core.result.ResultProducer;
import org.jdbi.v3.core.result.ResultProducers;
import org.jdbi.v3.core.result.ResultSetScanner;
import org.jdbi.v3.core.result.UnableToProduceResultException;
import org.jdbi.v3.core.statement.internal.PreparedBinding;

public class PreparedBatch extends SqlStatement<PreparedBatch> implements ResultBearing {
   private final List<PreparedBinding> bindings = new ArrayList<>();
   final Map<NamedArgumentFinderFactory.PrepareKey, Function<String, Optional<Function<Object, Argument>>>> preparedFinders = new HashMap<>();

   public PreparedBatch(Handle handle, CharSequence sql) {
      super(handle, sql);
      this.getContext().setBinding(new PreparedBinding(this.getContext()));
   }

   public PreparedBatch(Handle handle, String sql) {
      this(handle, (CharSequence)sql);
   }

   PreparedBatch bindNamedArgumentFinder(
      NamedArgumentFinderFactory factory, String prefix, Object value, Type type, Supplier<NamedArgumentFinder> backupArgumentFinder
   ) {
      PreparedBinding binding = this.getBinding();
      NamedArgumentFinderFactory.PrepareKey key = factory.keyFor(prefix, value);
      this.preparedFinders.computeIfAbsent(key, pk -> factory.prepareFor(this.getConfig(), this::buildArgument, prefix, value, type));
      binding.prepareKeys.put(key, value);
      binding.backupArgumentFinders.add(backupArgumentFinder);
      return this;
   }

   protected PreparedBinding getBinding() {
      return (PreparedBinding)super.getBinding();
   }

   Function<Object, Argument> buildArgument(QualifiedType<?> type) {
      return this.getContext()
         .getConfig(Arguments.class)
         .prepareFor(type)
         .orElse(
            value -> (pos, st, ctx) -> ctx.getConfig(Arguments.class)
               .findFor(type, value)
               .orElseThrow(() -> new UnableToCreateStatementException("no argument factory for type " + type, ctx))
               .apply(pos, st, ctx)
         );
   }

   @Override
   public <R> R scanResultSet(ResultSetScanner<R> resultSetScanner) {
      return this.execute(ResultProducers.returningResults()).scanResultSet(resultSetScanner);
   }

   public int[] execute() {
      int[] var1;
      try {
         var1 = this.internalBatchExecute().updateCounts;
      } finally {
         this.close();
      }

      return var1;
   }

   public ResultIterator<Integer> executeAndGetModCount() {
      final StatementContext ctx = this.getContext();
      final int[] modCount = this.execute();
      return new ResultIterator<Integer>() {
         int pos = 0;

         @Override
         public boolean hasNext() {
            return this.pos < modCount.length;
         }

         public Integer next() {
            if (!this.hasNext()) {
               throw new NoSuchElementException();
            } else {
               return modCount[this.pos++];
            }
         }

         @Override
         public StatementContext getContext() {
            return ctx;
         }

         @Override
         public void close() {
            ctx.close();
         }
      };
   }

   @Deprecated(
      since = "3.32.0",
      forRemoval = true
   )
   public ResultBearing executeAndReturnGeneratedKeys(String... columnNames) {
      return this.execute(ResultProducers.returningGeneratedKeys(columnNames));
   }

   public BatchResultBearing executePreparedBatch(String... columnNames) {
      PreparedBatch.ExecutedBatchConsumer executedBatchConsumer = new PreparedBatch.ExecutedBatchConsumer();
      ResultBearing resultBearing = this.execute(ResultProducers.returningGeneratedKeys(columnNames), executedBatchConsumer);
      return new BatchResultBearing(resultBearing, executedBatchConsumer);
   }

   public <R> R execute(ResultProducer<R> producer) {
      return this.execute(producer, x -> {});
   }

   private <R> R execute(ResultProducer<R> producer, Consumer<PreparedBatch.ExecutedBatch> batchConsumer) {
      try {
         return producer.produce(() -> {
            PreparedBatch.ExecutedBatch executedBatch = this.internalBatchExecute();
            batchConsumer.accept(executedBatch);
            return executedBatch.stmt;
         }, this.getContext());
      } catch (SQLException var4) {
         this.cleanUpForException(var4);
         throw new UnableToProduceResultException("Exception producing batch result", var4, this.getContext());
      }
   }

   private PreparedBatch.ExecutedBatch internalBatchExecute() {
      if (!this.getBinding().isEmpty()) {
         this.add();
      }

      this.beforeTemplating();
      StatementContext ctx = this.getContext();
      ParsedSql parsedSql = this.parseSql();
      String sql = parsedSql.getSql();
      ParsedParameters parsedParameters = parsedSql.getParameters();

      PreparedBatch.ExecutedBatch var24;
      try {
         SqlStatements stmtConfig = this.getConfig(SqlStatements.class);

         try {
            this.stmt = this.createStatement(sql);
            this.getContext().addCleanable(() -> this.cleanupStatement(this.stmt));
            stmtConfig.customize(this.stmt);
         } catch (SQLException var18) {
            throw new UnableToCreateStatementException(var18, ctx);
         }

         if (this.bindings.isEmpty()) {
            return new PreparedBatch.ExecutedBatch(this.stmt, new int[0]);
         }

         this.beforeBinding();

         try {
            ArgumentBinder binder = new ArgumentBinder.Prepared(this, parsedParameters, this.bindings.get(0));

            for (Binding binding : this.bindings) {
               ctx.setBinding(binding);
               binder.bind(binding);
               this.stmt.addBatch();
            }
         } catch (SQLException var19) {
            throw new UnableToExecuteStatementException("Exception while binding parameters", var19, ctx);
         }

         this.beforeExecution();

         try {
            int[] modifiedRows = SqlLoggerUtil.wrap(this.stmt::executeBatch, ctx, stmtConfig.getSqlLogger());
            this.afterExecution();
            ctx.setBinding(new PreparedBinding(ctx));
            var24 = new PreparedBatch.ExecutedBatch(this.stmt, modifiedRows);
         } catch (SQLException var17) {
            throw stmtConfig.handleException(Batch.mungeBatchException(var17), ctx);
         }
      } catch (Exception var20) {
         try {
            this.close();
         } catch (Exception var16) {
            var20.addSuppressed(var16);
         }

         throw var20;
      } finally {
         this.bindings.clear();
      }

      return var24;
   }

   public PreparedBatch add() {
      PreparedBinding currentBinding = this.getBinding();
      if (currentBinding.isEmpty()) {
         throw new IllegalStateException("Attempt to add() an empty batch, you probably didn't mean to do this - call add() *after* setting batch parameters");
      } else {
         this.bindings.add(currentBinding);
         this.getContext().setBinding(new PreparedBinding(this.getContext()));
         return this;
      }
   }

   public PreparedBatch add(Object... args) {
      for (int i = 0; i < args.length; i++) {
         this.bind(i, args[i]);
      }

      this.add();
      return this;
   }

   public PreparedBatch add(Map<String, ?> args) {
      this.bindMap(args);
      this.add();
      return this;
   }

   public int size() {
      return this.bindings.size();
   }

   private static class ExecutedBatch {
      final PreparedStatement stmt;
      final int[] updateCounts;

      ExecutedBatch(PreparedStatement stmt, int[] updateCounts) {
         this.stmt = stmt;
         this.updateCounts = Arrays.copyOf(updateCounts, updateCounts.length);
      }
   }

   private static final class ExecutedBatchConsumer implements Consumer<PreparedBatch.ExecutedBatch>, Supplier<int[]> {
      private int[] modifiedRowCounts = new int[0];

      public void accept(PreparedBatch.ExecutedBatch executedBatch) {
         this.modifiedRowCounts = executedBatch.updateCounts;
      }

      public int[] get() {
         return this.modifiedRowCounts;
      }
   }
}
