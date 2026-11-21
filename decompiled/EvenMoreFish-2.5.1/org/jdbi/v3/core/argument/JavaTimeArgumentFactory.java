package org.jdbi.v3.core.argument;

import java.lang.reflect.Type;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.time.ZonedDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import org.jdbi.v3.core.argument.internal.strategies.LoggableBinderArgument;
import org.jdbi.v3.core.config.ConfigRegistry;

class JavaTimeArgumentFactory extends SetObjectArgumentFactory {
   private static final Map<Class<?>, Function<Object, Argument>> TYPES = Map.of(
      ZonedDateTime.class,
      value -> (Argument)(value == null ? new NullArgument(2014) : ObjectArgument.of(((ZonedDateTime)value).toOffsetDateTime(), 2014)),
      Instant.class,
      value -> (Argument)(value == null
         ? new NullArgument(93)
         : new LoggableBinderArgument<>(value, (p, i, v) -> p.setTimestamp(i, Timestamp.from((Instant)v))))
   );

   JavaTimeArgumentFactory() {
      super(Map.of(LocalDate.class, 91, LocalTime.class, 92, LocalDateTime.class, 93, OffsetDateTime.class, 2014, OffsetTime.class, 2013));
   }

   @Override
   public Optional<Function<Object, Argument>> prepare(Type type, ConfigRegistry config) {
      Optional<Function<Object, Argument>> res = Optional.of(type).filter(Class.class::isInstance).map(Class.class::cast).map(TYPES::get);
      if (res.isEmpty()) {
         res = super.prepare(type, config);
      }

      return res;
   }
}
