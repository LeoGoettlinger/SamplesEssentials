package org.jdbi.v3.core.mapper;

import java.lang.reflect.Type;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import org.jdbi.v3.core.config.ConfigRegistry;
import org.jdbi.v3.core.statement.StatementContext;

public class GetObjectColumnMapperFactory implements ColumnMapperFactory {
   private final Set<Class<?>> supportedTypes;

   protected GetObjectColumnMapperFactory(Class<?>... types) {
      this(Arrays.asList(types));
   }

   protected GetObjectColumnMapperFactory(Collection<Class<?>> types) {
      this.supportedTypes = new HashSet<>(types);
   }

   public static ColumnMapperFactory forClasses(Class<?>... types) {
      return new GetObjectColumnMapperFactory(Arrays.asList(types));
   }

   public static ColumnMapperFactory forClasses(Set<Class<?>> types) {
      return new GetObjectColumnMapperFactory(types);
   }

   @Override
   public Optional<ColumnMapper<?>> build(Type type, ConfigRegistry config) {
      return Optional.of(type)
         .filter(Class.class::isInstance)
         .map(Class.class::cast)
         .filter(this.supportedTypes::contains)
         .map(GetObjectColumnMapperFactory.GetObjectColumnMapper::new);
   }

   private static class GetObjectColumnMapper<T> implements ColumnMapper<T> {
      private final Class<T> clazz;

      private GetObjectColumnMapper(Class<T> clazz) {
         this.clazz = clazz;
      }

      @Override
      public T map(ResultSet r, int columnNumber, StatementContext ctx) throws SQLException {
         return r.getObject(columnNumber, this.clazz);
      }
   }
}
