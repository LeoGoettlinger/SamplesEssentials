package org.jdbi.v3.core.mapper;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.IdentityHashMap;
import java.util.Optional;
import java.util.UUID;
import org.jdbi.v3.core.config.ConfigRegistry;
import org.jdbi.v3.core.generic.GenericTypes;
import org.jdbi.v3.core.statement.StatementContext;

class EssentialsMapperFactory implements ColumnMapperFactory {
   private final IdentityHashMap<Class<?>, ColumnMapper<?>> mappers = new IdentityHashMap<>();

   EssentialsMapperFactory() {
      this.mappers.put(BigDecimal.class, new GetterMapper<>(ResultSet::getBigDecimal));
      this.mappers.put(String.class, new GetterMapper<>(ResultSet::getString));
      this.mappers.put(byte[].class, new GetterMapper<>(ResultSet::getBytes));
      this.mappers.put(UUID.class, EssentialsMapperFactory::getUUID);
   }

   @Override
   public Optional<ColumnMapper<?>> build(Type type, ConfigRegistry config) {
      Class<?> rawType = GenericTypes.getErasedType(type);
      return Optional.ofNullable(this.mappers.get(rawType));
   }

   private static UUID getUUID(ResultSet r, int i, StatementContext ctx) throws SQLException {
      String s = r.getString(i);
      return s == null ? null : UUID.fromString(s);
   }
}
