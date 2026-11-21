package org.jdbi.v3.sqlobject.statement.internal;

import java.util.Arrays;
import java.util.function.Supplier;
import org.jdbi.v3.core.config.JdbiConfig;

public class SqlObjectStatementConfiguration implements JdbiConfig<SqlObjectStatementConfiguration> {
   private Supplier<Object> returner;
   private Object[] args;

   public SqlObjectStatementConfiguration() {
   }

   private SqlObjectStatementConfiguration(SqlObjectStatementConfiguration other) {
      this.returner = other.returner;
      this.args = other.args;
   }

   public SqlObjectStatementConfiguration createCopy() {
      return new SqlObjectStatementConfiguration(this);
   }

   void setReturner(Supplier<Object> returner) {
      this.returner = returner;
   }

   Supplier<Object> getReturner() {
      return this.returner;
   }

   void setArgs(Object[] args) {
      this.args = Arrays.copyOf(args, args.length);
   }

   Object[] getArgs() {
      return Arrays.copyOf(this.args, this.args.length);
   }
}
