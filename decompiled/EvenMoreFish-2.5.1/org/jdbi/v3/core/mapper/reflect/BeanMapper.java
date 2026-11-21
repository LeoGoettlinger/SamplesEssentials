package org.jdbi.v3.core.mapper.reflect;

import org.jdbi.v3.core.config.ConfigRegistry;
import org.jdbi.v3.core.generic.GenericTypes;
import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.mapper.RowMapperFactory;
import org.jdbi.v3.core.mapper.reflect.internal.BeanPropertiesFactory;
import org.jdbi.v3.core.mapper.reflect.internal.PojoMapper;
import org.jdbi.v3.core.mapper.reflect.internal.PojoProperties;
import org.jdbi.v3.core.statement.StatementContext;

public final class BeanMapper<T> extends PojoMapper<T> {
   static final String DEFAULT_PREFIX = "";

   public static RowMapperFactory factory(Class<?> type) {
      return RowMapperFactory.of(type, of(type));
   }

   public static RowMapperFactory factory(Class<?> type, String prefix) {
      return RowMapperFactory.of(type, of(type, prefix));
   }

   public static <T> RowMapper<T> of(Class<T> type) {
      return of(type, "");
   }

   public static <T> RowMapper<T> of(Class<T> type, String prefix) {
      return new BeanMapper<>(type, prefix);
   }

   private BeanMapper(Class<T> type, String prefix) {
      super(type, prefix);
      this.strictColumnTypeMapping = false;
   }

   @Override
   protected PojoProperties<T> getProperties(ConfigRegistry config) {
      return (PojoProperties<T>)BeanPropertiesFactory.propertiesFor(this.type, config);
   }

   @Deprecated(
      since = "3.49.0"
   )
   protected BeanMapper<?> createNestedMapper(StatementContext ctx, PojoProperties.PojoProperty<T> property, String nestedPrefix) {
      return new BeanMapper((Class<T>)GenericTypes.getErasedType(property.getQualifiedType().getType()), nestedPrefix);
   }

   @Override
   protected PojoMapper<?> createNestedMapper(StatementContext ctx, Class<?> rawType, String nestedPrefix) {
      return new BeanMapper((Class<T>)rawType, nestedPrefix);
   }
}
