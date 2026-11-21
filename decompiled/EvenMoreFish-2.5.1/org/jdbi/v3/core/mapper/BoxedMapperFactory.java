package org.jdbi.v3.core.mapper;

import java.lang.reflect.Type;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.IdentityHashMap;
import java.util.Optional;
import org.jdbi.v3.core.config.ConfigRegistry;
import org.jdbi.v3.core.generic.GenericTypes;

class BoxedMapperFactory implements ColumnMapperFactory {
   private final IdentityHashMap<Class<?>, ColumnMapper<?>> mappers = new IdentityHashMap<>();

   BoxedMapperFactory() {
      this.mappers.put(Boolean.class, new GetterMapper<>(ResultSet::getBoolean));
      this.mappers.put(Byte.class, new GetterMapper<>(ResultSet::getByte));
      this.mappers.put(Character.class, new GetterMapper<>(BoxedMapperFactory::getCharacter));
      this.mappers.put(Short.class, new GetterMapper<>(ResultSet::getShort));
      this.mappers.put(Integer.class, new GetterMapper<>(ResultSet::getInt));
      this.mappers.put(Long.class, new GetterMapper<>(ResultSet::getLong));
      this.mappers.put(Float.class, new GetterMapper<>(ResultSet::getFloat));
      this.mappers.put(Double.class, new GetterMapper<>(ResultSet::getDouble));
   }

   @Override
   public Optional<ColumnMapper<?>> build(Type type, ConfigRegistry config) {
      Class<?> rawType = GenericTypes.getErasedType(type);
      return Optional.ofNullable(this.mappers.get(rawType));
   }

   private static Character getCharacter(ResultSet r, int i) throws SQLException {
      String s = r.getString(i);
      return s != null && !s.isEmpty() ? s.charAt(0) : null;
   }
}
