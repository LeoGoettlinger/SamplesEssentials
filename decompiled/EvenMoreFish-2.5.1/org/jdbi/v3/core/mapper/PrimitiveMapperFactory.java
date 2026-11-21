package org.jdbi.v3.core.mapper;

import java.lang.reflect.Type;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.IdentityHashMap;
import java.util.Optional;
import org.jdbi.v3.core.config.ConfigRegistry;
import org.jdbi.v3.core.generic.GenericTypes;
import org.jdbi.v3.core.result.UnableToProduceResultException;

class PrimitiveMapperFactory implements ColumnMapperFactory {
   private final IdentityHashMap<Class<?>, ColumnMapper<?>> mappers = new IdentityHashMap<>();

   PrimitiveMapperFactory() {
      this.mappers.put(boolean.class, primitiveMapper(ResultSet::getBoolean));
      this.mappers.put(byte.class, primitiveMapper(ResultSet::getByte));
      this.mappers.put(char.class, primitiveMapper(PrimitiveMapperFactory::getChar));
      this.mappers.put(short.class, primitiveMapper(ResultSet::getShort));
      this.mappers.put(int.class, primitiveMapper(ResultSet::getInt));
      this.mappers.put(long.class, primitiveMapper(ResultSet::getLong));
      this.mappers.put(float.class, primitiveMapper(ResultSet::getFloat));
      this.mappers.put(double.class, primitiveMapper(ResultSet::getDouble));
   }

   @Override
   public Optional<ColumnMapper<?>> build(Type type, ConfigRegistry config) {
      Class<?> rawType = GenericTypes.getErasedType(type);
      return Optional.ofNullable(this.mappers.get(rawType));
   }

   private static <T> ColumnMapper<T> primitiveMapper(ColumnGetter<T> getter) {
      return (r, i, ctx) -> {
         T value = getter.get(r, i);
         if (r.wasNull() && !ctx.getConfig(ColumnMappers.class).getCoalesceNullPrimitivesToDefaults()) {
            String msg = String.format(
               "Database null values are not allowed for Java primitives by the current configuration: could not map column %s (%s). Change your result type to a boxed primitive to resolve.",
               i,
               r.getMetaData().getColumnLabel(i)
            );
            throw new UnableToProduceResultException(msg);
         } else {
            return value;
         }
      };
   }

   private static char getChar(ResultSet r, int i) throws SQLException {
      Character character = getCharacter(r, i);
      return character == null ? '\u0000' : character;
   }

   private static Character getCharacter(ResultSet r, int i) throws SQLException {
      String s = r.getString(i);
      return s != null && !s.isEmpty() ? s.charAt(0) : null;
   }
}
