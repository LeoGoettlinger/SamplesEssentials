package org.jdbi.v3.core.argument;

import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Function;
import org.jdbi.v3.core.argument.internal.StatementBinder;
import org.jdbi.v3.core.argument.internal.strategies.LoggableBinderArgument;
import org.jdbi.v3.core.config.ConfigRegistry;
import org.jdbi.v3.core.qualifier.QualifiedType;
import org.jdbi.v3.meta.Legacy;

final class LegacyArgumentFactory implements QualifiedArgumentFactory.Preparable {
   private final ConcurrentMap<QualifiedType<?>, Function<Object, Argument>> builders = new ConcurrentHashMap<>();

   LegacyArgumentFactory() {
      this.register(Instant.class, 93, (p, i, v) -> p.setTimestamp(i, Timestamp.from(v)));
      this.register(LocalDate.class, 91, (p, i, v) -> p.setDate(i, Date.valueOf(v)));
      this.register(LocalTime.class, 92, (p, i, v) -> p.setTime(i, Time.valueOf(v)));
      this.register(LocalDateTime.class, 93, (p, i, v) -> p.setTimestamp(i, Timestamp.valueOf(v)));
      this.register(OffsetDateTime.class, 93, (p, i, v) -> p.setTimestamp(i, Timestamp.from(v.toInstant())));
      this.register(ZonedDateTime.class, 93, (p, i, v) -> p.setTimestamp(i, Timestamp.from(v.toInstant())));
      this.register(OffsetTime.class, 92, (p, i, v) -> p.setTime(i, Time.valueOf(v.toLocalTime())));
   }

   @Override
   public Optional<Function<Object, Argument>> prepare(QualifiedType<?> type, ConfigRegistry config) {
      return Optional.of(type).map(this.builders::get);
   }

   @Override
   public Optional<Argument> build(QualifiedType<?> type, Object value, ConfigRegistry config) {
      return this.prepare(type, config).map(f -> f.apply(value));
   }

   <T> void register(Class<T> klazz, int sqlType, StatementBinder<T> binder) {
      QualifiedType<T> type = QualifiedType.of(klazz).with(Legacy.class);
      this.builders.put(type, value -> (Argument)(value == null ? new NullArgument(sqlType) : new LoggableBinderArgument<>(klazz.cast(value), binder)));
   }
}
