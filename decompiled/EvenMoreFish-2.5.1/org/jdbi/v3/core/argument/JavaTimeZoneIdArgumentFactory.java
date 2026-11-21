package org.jdbi.v3.core.argument;

import java.time.ZoneId;
import org.jdbi.v3.core.argument.internal.strategies.LoggableBinderArgument;
import org.jdbi.v3.core.config.ConfigRegistry;

public class JavaTimeZoneIdArgumentFactory extends AbstractArgumentFactory<ZoneId> {
   public JavaTimeZoneIdArgumentFactory() {
      super(12);
   }

   protected Argument build(ZoneId value, ConfigRegistry config) {
      return LoggableBinderArgument.bindAsString(value);
   }
}
