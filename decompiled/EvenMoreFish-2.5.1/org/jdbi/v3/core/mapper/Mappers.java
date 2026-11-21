package org.jdbi.v3.core.mapper;

import java.lang.reflect.Type;
import java.util.Optional;
import java.util.function.Function;
import org.jdbi.v3.core.config.ConfigRegistry;
import org.jdbi.v3.core.config.JdbiConfig;
import org.jdbi.v3.core.generic.GenericType;
import org.jdbi.v3.core.qualifier.QualifiedType;

public class Mappers implements JdbiConfig<Mappers> {
   private RowMappers rowMappers;
   private ColumnMappers columnMappers;

   @Override
   public void setRegistry(ConfigRegistry registry) {
      this.rowMappers = registry.get(RowMappers.class);
      this.columnMappers = registry.get(ColumnMappers.class);
   }

   public <T> Optional<RowMapper<T>> findFor(Class<T> type) {
      RowMapper<T> mapper = (RowMapper<T>)this.findFor(type).orElse(null);
      return Optional.ofNullable(mapper);
   }

   public <T> Optional<RowMapper<T>> findFor(GenericType<T> type) {
      RowMapper<T> mapper = (RowMapper<T>)this.findFor(type.getType()).orElse(null);
      return Optional.ofNullable(mapper);
   }

   public Optional<RowMapper<?>> findFor(Type type) {
      return this.findFor(QualifiedType.of(type)).map(Function.identity());
   }

   public <T> Optional<RowMapper<T>> findFor(QualifiedType<T> type) {
      if (type.hasNoQualifiers()) {
         Optional<RowMapper<T>> result = this.rowMappers.findFor(type.getType()).map(m -> (RowMapper<T>)m);
         if (result.isPresent()) {
            return result;
         }
      }

      return this.columnMappers.findFor(type).map(SingleColumnMapper::new);
   }

   public Mappers createCopy() {
      return new Mappers();
   }
}
