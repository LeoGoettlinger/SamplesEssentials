package org.jdbi.v3.core.mapper;

import java.lang.reflect.Type;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.IdentityHashMap;
import java.util.Optional;
import org.jdbi.v3.core.config.ConfigRegistry;
import org.jdbi.v3.core.generic.GenericTypes;

class SqlTimeMapperFactory implements ColumnMapperFactory {
   private final IdentityHashMap<Class<?>, ColumnMapper<?>> mappers = new IdentityHashMap<>();

   SqlTimeMapperFactory() {
      this.mappers.put(Timestamp.class, new GetterMapper<>(ResultSet::getTimestamp));
      this.mappers.put(Date.class, new GetterMapper<>(ResultSet::getDate));
      this.mappers.put(Time.class, new GetterMapper<>(ResultSet::getTime));
   }

   @Override
   public Optional<ColumnMapper<?>> build(Type type, ConfigRegistry config) {
      Class<?> rawType = GenericTypes.getErasedType(type);
      return Optional.ofNullable(this.mappers.get(rawType));
   }
}
