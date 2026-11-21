package org.jdbi.v3.core.mapper;

import java.lang.reflect.Type;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Map;
import java.util.Optional;
import org.jdbi.v3.core.config.ConfigRegistry;

class JavaTimeMapperFactory extends GetObjectColumnMapperFactory {
   private static final Map<Class<?>, ColumnMapper<?>> MAPPERS = Map.of(
      ZoneId.class,
      (r, i, ctx) -> getZoneId(r, i),
      ZoneOffset.class,
      (r, i, ctx) -> getZoneOffset(r, i),
      ZonedDateTime.class,
      (r, i, ctx) -> getZonedDateTime(r, i),
      Instant.class,
      (r, i, ctx) -> getInstant(r, i)
   );

   JavaTimeMapperFactory() {
      super(LocalDate.class, LocalTime.class, LocalDateTime.class, OffsetDateTime.class, OffsetTime.class);
   }

   @Override
   public Optional<ColumnMapper<?>> build(Type type, ConfigRegistry config) {
      Optional<ColumnMapper<?>> res = Optional.of(type).filter(Class.class::isInstance).map(Class.class::cast).map(MAPPERS::get);
      if (res.isEmpty()) {
         res = super.build(type, config);
      }

      return res;
   }

   private static ZoneId getZoneId(ResultSet rs, int columnNumber) throws SQLException {
      String zoneId = rs.getString(columnNumber);
      return zoneId == null ? null : ZoneId.of(zoneId);
   }

   private static ZoneOffset getZoneOffset(ResultSet rs, int columnNumber) throws SQLException {
      String zoneOffset = rs.getString(columnNumber);
      return zoneOffset == null ? null : ZoneOffset.of(zoneOffset);
   }

   private static ZonedDateTime getZonedDateTime(ResultSet rs, int columnNumber) throws SQLException {
      OffsetDateTime offsetDateTime = rs.getObject(columnNumber, OffsetDateTime.class);
      return offsetDateTime == null ? null : offsetDateTime.toZonedDateTime();
   }

   private static Instant getInstant(ResultSet r, int i) throws SQLException {
      Timestamp timestamp = r.getTimestamp(i);
      return timestamp == null ? null : timestamp.toInstant();
   }
}
