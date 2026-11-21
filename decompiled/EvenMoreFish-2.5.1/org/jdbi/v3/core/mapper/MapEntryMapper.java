package org.jdbi.v3.core.mapper;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;
import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.Map.Entry;
import org.jdbi.v3.core.config.ConfigRegistry;
import org.jdbi.v3.core.generic.GenericTypes;
import org.jdbi.v3.core.statement.StatementContext;

public class MapEntryMapper<K, V> implements RowMapper<Entry<K, V>> {
   private static final TypeVariable<Class<Entry>> KEY_PARAM;
   private static final TypeVariable<Class<Entry>> VALUE_PARAM;
   private final RowMapper<K> keyMapper;
   private final RowMapper<V> valueMapper;

   static RowMapperFactory factory() {
      return (type, config) -> {
         if (type instanceof ParameterizedType && GenericTypes.getErasedType(type).equals(Entry.class)) {
            Type keyType = GenericTypes.resolveType(KEY_PARAM, type);
            Type valueType = GenericTypes.resolveType(VALUE_PARAM, type);
            RowMapper<?> keyMapper = getKeyMapper(keyType, config);
            RowMapper<?> valueMapper = getValueMapper(valueType, config);
            return Optional.of(new MapEntryMapper<>(keyMapper, valueMapper));
         } else {
            return Optional.empty();
         }
      };
   }

   private static RowMapper<?> getKeyMapper(Type keyType, ConfigRegistry config) {
      String column = config.get(MapEntryMappers.class).getKeyColumn();
      return column == null
         ? config.get(RowMappers.class)
            .findFor(keyType)
            .orElseThrow(
               () -> new NoSuchMapperException(
                  String.format("Map key column is not declared (missing @KeyColumn annotation?) and no row mapper for key type '%s' is registered!", keyType)
               )
            )
         : config.get(ColumnMappers.class)
            .findFor(keyType)
            .map(mapper -> new SingleColumnMapper<>((ColumnMapper<?>)mapper, column))
            .orElseThrow(() -> new NoSuchMapperException(String.format("Type '%s' for map key column '%s' has no column mapper registered!", keyType, column)));
   }

   private static RowMapper<?> getValueMapper(Type valueType, ConfigRegistry config) {
      String column = config.get(MapEntryMappers.class).getValueColumn();
      return column == null
         ? config.get(RowMappers.class)
            .findFor(valueType)
            .orElseThrow(
               () -> new NoSuchMapperException(
                  String.format(
                     "Map value column is not declared (missing @ValueColumn annotation?) and no row mapper for value type '%s' is registered!", valueType
                  )
               )
            )
         : config.get(ColumnMappers.class)
            .findFor(valueType)
            .map(mapper -> new SingleColumnMapper<>((ColumnMapper<?>)mapper, column))
            .orElseThrow(
               () -> new NoSuchMapperException(String.format("Type '%s' for map value column '%s' has no column mapper registered!", valueType, column))
            );
   }

   private MapEntryMapper(RowMapper<K> keyMapper, RowMapper<V> valueMapper) {
      this.keyMapper = keyMapper;
      this.valueMapper = valueMapper;
   }

   public Entry<K, V> map(ResultSet rs, StatementContext ctx) throws SQLException {
      return new SimpleImmutableEntry<>(this.keyMapper.map(rs, ctx), this.valueMapper.map(rs, ctx));
   }

   @Override
   public RowMapper<Entry<K, V>> specialize(ResultSet rs, StatementContext ctx) throws SQLException {
      return new MapEntryMapper<>(this.keyMapper.specialize(rs, ctx), this.valueMapper.specialize(rs, ctx));
   }

   static {
      TypeVariable<Class<Entry>>[] mapParams = Entry.class.getTypeParameters();
      KEY_PARAM = mapParams[0];
      VALUE_PARAM = mapParams[1];
   }
}
