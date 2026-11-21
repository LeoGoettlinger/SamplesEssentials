package org.jdbi.v3.core.mapper;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.UnaryOperator;
import org.jdbi.v3.core.config.ConfigRegistry;
import org.jdbi.v3.core.generic.GenericType;
import org.jdbi.v3.core.generic.GenericTypes;
import org.jdbi.v3.core.statement.StatementContext;

public class GenericMapMapperFactory implements RowMapperFactory {
   @Override
   public Optional<RowMapper<?>> build(Type mapType, ConfigRegistry config) {
      return Optional.of(mapType)
         .filter(ParameterizedType.class::isInstance)
         .map(ParameterizedType.class::cast)
         .filter(maybeMap -> Map.class.equals(maybeMap.getRawType()))
         .filter(map -> String.class.equals(GenericTypes.findGenericParameter(map, Map.class, 0).orElse(null)))
         .flatMap(map -> GenericTypes.findGenericParameter(map, Map.class, 1))
         .filter(value -> !Object.class.equals(value))
         .flatMap(config.get(ColumnMappers.class)::findFor)
         .map(GenericMapMapperFactory.GenericMapMapper::new);
   }

   public static <T> RowMapper<Map<String, T>> getMapperForValueType(Class<T> valueType, ConfigRegistry config) {
      return config.get(ColumnMappers.class)
         .findFor(valueType)
         .map(GenericMapMapperFactory.GenericMapMapper::new)
         .orElseThrow(() -> new RuntimeException("no column mapper found for type " + valueType));
   }

   public static <T> RowMapper<Map<String, T>> getMapperForValueType(GenericType<T> valueType, ConfigRegistry config) {
      return config.get(ColumnMappers.class)
         .findFor(valueType)
         .map(GenericMapMapperFactory.GenericMapMapper::new)
         .orElseThrow(() -> new RuntimeException("no column mapper found for type " + valueType));
   }

   private static class GenericMapMapper<T> implements RowMapper<Map<String, T>> {
      private final ColumnMapper<T> mapper;

      private GenericMapMapper(ColumnMapper<T> mapper) {
         this.mapper = mapper;
      }

      public Map<String, T> map(ResultSet rs, StatementContext ctx) throws SQLException {
         return this.specialize(rs, ctx).map(rs, ctx);
      }

      @Override
      public RowMapper<Map<String, T>> specialize(ResultSet rs, StatementContext ctx) throws SQLException {
         List<String> keyNames = getMapKeys(rs.getMetaData(), ctx.getConfig(MapMappers.class).getCaseChange());
         return (r, c) -> {
            Map<String, T> row = new HashMap<>();

            for (int i = 0; i < keyNames.size(); i++) {
               T value = this.mapper.map(r, i + 1, ctx);
               row.put(keyNames.get(i), value);
            }

            return row;
         };
      }

      private static List<String> getMapKeys(ResultSetMetaData meta, UnaryOperator<String> caseChange) throws SQLException {
         Set<String> names = new LinkedHashSet<>();
         int columnCount = meta.getColumnCount();

         for (int i = 0; i < columnCount; i++) {
            String columnName = meta.getColumnName(i + 1);
            String columnLabel = meta.getColumnLabel(i + 1);
            String key = columnLabel == null ? columnName : columnLabel;
            String renamedKey = caseChange.apply(key);
            boolean added = names.add(renamedKey);
            if (!added) {
               throw new RuntimeException(String.format("map key \"%s\" (from column \"%s\") appears twice in this resultset!", renamedKey, key));
            }
         }

         return new ArrayList<>(names);
      }
   }
}
