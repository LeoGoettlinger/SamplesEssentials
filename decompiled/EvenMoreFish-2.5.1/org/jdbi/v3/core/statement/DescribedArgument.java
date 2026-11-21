package org.jdbi.v3.core.statement;

import java.lang.reflect.Method;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Objects;
import org.jdbi.v3.core.argument.Argument;
import org.jdbi.v3.core.config.internal.ConfigCache;
import org.jdbi.v3.core.config.internal.ConfigCaches;
import org.jdbi.v3.core.internal.JdbiClassUtils;

class DescribedArgument implements Argument {
   private static final ConfigCache<Class<?>, Boolean> ARG_CLASS_HAS_TOSTRING = ConfigCaches.declare(
      type -> JdbiClassUtils.safeMethodLookup(type, "toString").map(Method::getDeclaringClass).map(c -> c != Object.class).orElse(false)
   );
   private final Argument arg;
   private final String description;

   DescribedArgument(Argument arg, Object value) {
      this.arg = arg;
      this.description = Objects.toString(ArgumentBinder.unwrap(value));
   }

   @Override
   public void apply(int position, PreparedStatement statement, StatementContext ctx) throws SQLException {
      this.arg.apply(position, statement, ctx);
   }

   @Override
   public String toString() {
      return this.description;
   }

   public static Argument wrap(StatementContext ctx, Argument arg, Object value) {
      return (Argument)(Boolean.TRUE.equals(ARG_CLASS_HAS_TOSTRING.get(arg.getClass(), ctx)) ? arg : new DescribedArgument(arg, value));
   }
}
