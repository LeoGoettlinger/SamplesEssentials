package org.jdbi.v3.core.argument.internal.strategies;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Objects;
import org.jdbi.v3.core.argument.Argument;
import org.jdbi.v3.core.argument.internal.StatementBinder;
import org.jdbi.v3.core.statement.StatementContext;

public class LoggableBinderArgument<T> implements Argument {
   private final T value;
   private final StatementBinder<T> binder;

   public static Argument bindAsString(Object obj) {
      return new LoggableBinderArgument<>(Objects.toString(obj, null), PreparedStatement::setString);
   }

   public LoggableBinderArgument(T value, StatementBinder<T> binder) {
      this.value = value;
      this.binder = binder;
   }

   @Override
   public void apply(int pos, PreparedStatement stmt, StatementContext ctx) throws SQLException {
      this.binder.bind(stmt, pos, this.value);
   }

   @Override
   public String toString() {
      return Objects.toString(this.value);
   }
}
