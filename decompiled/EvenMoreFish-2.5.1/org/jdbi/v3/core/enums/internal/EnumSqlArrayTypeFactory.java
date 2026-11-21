package org.jdbi.v3.core.enums.internal;

import java.lang.reflect.Type;
import java.util.Optional;
import org.jdbi.v3.core.array.SqlArrayType;
import org.jdbi.v3.core.array.SqlArrayTypeFactory;
import org.jdbi.v3.core.config.ConfigRegistry;
import org.jdbi.v3.core.enums.EnumStrategy;
import org.jdbi.v3.core.generic.GenericTypes;
import org.jdbi.v3.core.internal.EnumStrategies;
import org.jdbi.v3.core.qualifier.QualifiedType;

public class EnumSqlArrayTypeFactory implements SqlArrayTypeFactory {
   @Override
   public Optional<SqlArrayType<?>> build(Type elementType, ConfigRegistry config) {
      return Optional.of(elementType)
         .map(GenericTypes::getErasedType)
         .filter(Enum.class::isAssignableFrom)
         .map(clazz -> this.makeSqlArrayType((Class<?>)clazz, config));
   }

   private <E extends Enum<E>> SqlArrayType<E> makeSqlArrayType(Class<E> enumClass, ConfigRegistry config) {
      boolean byName = EnumStrategy.BY_NAME == config.get(EnumStrategies.class).findStrategy(QualifiedType.of(enumClass));
      return byName ? SqlArrayType.of("varchar", Enum::name) : SqlArrayType.of("integer", Enum::ordinal);
   }
}
