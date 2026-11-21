package org.jdbi.v3.core.enums.internal;

import java.util.Optional;
import org.jdbi.v3.core.config.ConfigRegistry;
import org.jdbi.v3.core.enums.EnumStrategy;
import org.jdbi.v3.core.generic.GenericTypes;
import org.jdbi.v3.core.internal.EnumStrategies;
import org.jdbi.v3.core.mapper.ColumnMapper;
import org.jdbi.v3.core.mapper.EnumMapper;
import org.jdbi.v3.core.mapper.QualifiedColumnMapperFactory;
import org.jdbi.v3.core.qualifier.QualifiedType;

public class EnumMapperFactory implements QualifiedColumnMapperFactory {
   @Override
   public Optional<ColumnMapper<?>> build(QualifiedType<?> givenType, ConfigRegistry config) {
      return Optional.of(givenType.getType())
         .map(GenericTypes::getErasedType)
         .filter(Enum.class::isAssignableFrom)
         .map(clazz -> makeEnumArgument(givenType, (Class<?>)clazz, config));
   }

   private static <E extends Enum<E>> ColumnMapper<?> makeEnumArgument(QualifiedType<E> givenType, Class<E> enumClass, ConfigRegistry config) {
      boolean byName = EnumStrategy.BY_NAME == config.get(EnumStrategies.class).findStrategy(givenType);
      return byName ? EnumMapper.byName(enumClass) : EnumMapper.byOrdinal(enumClass);
   }
}
