package org.jdbi.v3.core.argument;

import java.lang.reflect.Type;
import java.sql.PreparedStatement;
import java.util.Optional;
import java.util.function.Function;
import org.jdbi.v3.core.config.ConfigRegistry;

class PrimitivesArgumentFactory extends DelegatingArgumentFactory {
   PrimitivesArgumentFactory() {
      this.register(boolean.class, 16, PreparedStatement::setBoolean);
      this.register(byte.class, -6, PreparedStatement::setByte);
      this.register(char.class, 1, new ToStringBinder<>(PreparedStatement::setString));
      this.register(short.class, 5, PreparedStatement::setShort);
      this.register(int.class, 4, PreparedStatement::setInt);
      this.register(long.class, -5, PreparedStatement::setLong);
      this.register(float.class, 6, PreparedStatement::setFloat);
      this.register(double.class, 8, PreparedStatement::setDouble);
   }

   @Override
   public Optional<Function<Object, Argument>> prepare(Type type, ConfigRegistry config) {
      return super.prepare(type, config).map(prepared -> value -> (Argument)prepared.apply(this.checkForNull(config, type, value)));
   }

   @Override
   public Optional<Argument> build(Type expectedType, Object value, ConfigRegistry config) {
      return super.build(expectedType, this.checkForNull(config, expectedType, value), config);
   }

   private Object checkForNull(ConfigRegistry cfg, Type type, Object value) {
      if (value != null && !(value instanceof NullArgument)) {
         return value;
      } else if (cfg.get(Arguments.class).isBindingNullToPrimitivesPermitted()) {
         return null;
      } else {
         throw new IllegalArgumentException(String.format("binding null to a primitive %s is forbidden by configuration, declare a boxed type instead", type));
      }
   }
}
