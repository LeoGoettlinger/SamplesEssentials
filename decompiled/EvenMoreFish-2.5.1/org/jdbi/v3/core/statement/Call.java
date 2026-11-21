package org.jdbi.v3.core.statement;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import org.jdbi.v3.core.Handle;
import org.jdbi.v3.core.argument.Argument;
import org.jdbi.v3.core.internal.exceptions.Sneaky;
import org.jdbi.v3.core.result.ResultBearing;
import org.jdbi.v3.core.result.internal.ResultSetSupplier;

public class Call extends SqlStatement<Call> {
   private final List<Call.OutParamArgument> outParamArguments = new ArrayList<>();

   public Call(Handle handle, CharSequence sql) {
      super(handle, sql);
   }

   public Call(Handle handle, String sql) {
      super(handle, sql);
   }

   @Override
   PreparedStatement createStatement(String parsedSql) throws SQLException {
      return this.getHandle().getStatementBuilder().createCall(this.getHandle().getConnection(), parsedSql, this.getContext());
   }

   public Call registerOutParameter(int position, int sqlType) {
      return this.registerOutParameter(position, sqlType, null);
   }

   public Call registerOutParameter(int position, int sqlType, CallableStatementMapper mapper) {
      Call.OutParamArgument outParam = new Call.OutParamArgument(sqlType, mapper, null);
      Binding binding = this.getBinding();
      if (binding.positionals.containsKey(position)) {
         outParam.setInputValue(binding.positionals.get(position));
      }

      binding.addPositional(position, (Argument)outParam);
      return this;
   }

   public Call registerOutParameter(String name, int sqlType) {
      return this.registerOutParameter(name, sqlType, null);
   }

   public Call registerOutParameter(String name, int sqlType, CallableStatementMapper mapper) {
      Call.OutParamArgument outParam = new Call.OutParamArgument(sqlType, mapper, name);
      Binding binding = this.getBinding();
      if (binding.named.containsKey(name)) {
         outParam.setInputValue(binding.named.get(name));
      }

      binding.addNamed(name, (Argument)outParam);
      return this;
   }

   public OutParameters invoke() {
      return this.invoke(Function.identity());
   }

   public void invoke(Consumer<OutParameters> resultConsumer) {
      this.invoke(r -> {
         resultConsumer.accept(r);
         return null;
      });
   }

   public <T> T invoke(Function<OutParameters, T> resultComputer) {
      this.internalExecute();
      Supplier<ResultSet> resultSetSupplier = () -> {
         try {
            return this.stmt.getResultSet();
         } catch (SQLException var2x) {
            throw Sneaky.throwAnyway(var2x);
         }
      };
      ResultBearing resultSet = ResultBearing.of(ResultSetSupplier.notClosingContext(resultSetSupplier), this.getContext());
      OutParameters out = new OutParameters(resultSet, this.getContext());
      this.outParamArguments.forEach(outparamArgument -> {
         Supplier<Object> supplier = outparamArgument.supplier((CallableStatement)this.stmt);
         out.putValueSupplier(outparamArgument.position - 1, outparamArgument.name, supplier);
      });
      return resultComputer.apply(out);
   }

   public Call setFetchSize(final int fetchSize) {
      return this.addCustomizer(StatementCustomizers.fetchSize(fetchSize));
   }

   public Call setMaxRows(final int maxRows) {
      return this.addCustomizer(StatementCustomizers.maxRows(maxRows));
   }

   public Call setMaxFieldSize(final int maxFields) {
      return this.addCustomizer(StatementCustomizers.maxFieldSize(maxFields));
   }

   public Call concurrentUpdatable() {
      this.getContext().setConcurrentUpdatable(true);
      return this;
   }

   private class OutParamArgument implements Argument {
      private final int sqlType;
      private final CallableStatementMapper mapper;
      private final String name;
      private int position;
      private Object inputValue;
      private boolean hasInput;

      OutParamArgument(int sqlType, CallableStatementMapper mapper, String name) {
         this.sqlType = sqlType;
         this.mapper = mapper;
         this.name = name;
         Call.this.outParamArguments.add(this);
      }

      void setInputValue(Object inputValue) {
         this.inputValue = inputValue;
         this.hasInput = true;
      }

      @Override
      public void apply(int outPosition, PreparedStatement statement, StatementContext ctx) throws SQLException {
         ((CallableStatement)statement).registerOutParameter(outPosition, this.sqlType);
         if (this.hasInput) {
            this.applyInputValue(outPosition, statement, ctx);
         }

         this.position = outPosition;
      }

      private void applyInputValue(int outPosition, PreparedStatement statement, StatementContext ctx) throws SQLException {
         Argument argument;
         if (this.inputValue instanceof Argument arg) {
            argument = arg;
         } else {
            ArgumentBinder.ArgumentFactoryLocator factoryLocator = new ArgumentBinder.ArgumentFactoryLocator(ctx);
            argument = factoryLocator.argumentFactoryForType(factoryLocator.typeOf(this.inputValue)).apply(ArgumentBinder.unwrap(this.inputValue));
         }

         argument.apply(outPosition, statement, ctx);
      }

      public Supplier<Object> supplier(CallableStatement stmt) {
         return () -> {
            Object value = this.map(stmt);
            return this.isNull(stmt) ? null : value;
         };
      }

      private Object map(CallableStatement stmt) {
         try {
            if (this.mapper != null) {
               return this.mapper.map(this.position, stmt);
            } else {
               switch (this.sqlType) {
                  case -16:
                  case -9:
                  case -1:
                  case 12:
                  case 2005:
                  case 2011:
                     return stmt.getString(this.position);
                  case -5:
                     return stmt.getLong(this.position);
                  case -3:
                  case 2004:
                     return stmt.getBytes(this.position);
                  case 3:
                  case 8:
                     return stmt.getDouble(this.position);
                  case 4:
                     return stmt.getInt(this.position);
                  case 5:
                     return stmt.getShort(this.position);
                  case 6:
                     return stmt.getFloat(this.position);
                  case 91:
                     return stmt.getDate(this.position);
                  case 92:
                  case 93:
                     return stmt.getTimestamp(this.position);
                  default:
                     return stmt.getObject(this.position);
               }
            }
         } catch (SQLException var3) {
            throw new UnableToExecuteStatementException("Could not get OUT parameter from statement", var3, Call.this.getContext());
         }
      }

      private boolean isNull(CallableStatement stmt) {
         try {
            return stmt.wasNull();
         } catch (SQLException var3) {
            throw new UnableToExecuteStatementException("Could not get OUT parameter from statement", var3, Call.this.getContext());
         }
      }
   }
}
