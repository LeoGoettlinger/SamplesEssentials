package org.jdbi.v3.core.internal;

import java.util.Optional;
import org.jdbi.v3.core.config.ConfigRegistry;
import org.jdbi.v3.core.config.JdbiConfig;
import org.jdbi.v3.core.enums.EnumByName;
import org.jdbi.v3.core.enums.EnumByOrdinal;
import org.jdbi.v3.core.enums.EnumStrategy;
import org.jdbi.v3.core.enums.Enums;
import org.jdbi.v3.core.generic.GenericTypes;
import org.jdbi.v3.core.qualifier.QualifiedType;
import org.jdbi.v3.core.qualifier.Qualifiers;

public class EnumStrategies implements JdbiConfig<EnumStrategies> {
   private ConfigRegistry registry;

   @Override
   public void setRegistry(ConfigRegistry registry) {
      this.registry = registry;
   }

   public <E extends Enum<E>> EnumStrategy findStrategy(QualifiedType<E> type) {
      Class<?> erasedType = GenericTypes.getErasedType(type.getType());
      return JdbiOptionals.<EnumStrategy>findFirstPresent(
            () -> doFindStrategy(type),
            () -> doFindStrategy(QualifiedType.of(erasedType).withAnnotations(this.registry.get(Qualifiers.class).findFor(erasedType)))
         )
         .orElseGet(() -> this.registry.get(Enums.class).getDefaultStrategy());
   }

   private static <T> Optional<EnumStrategy> doFindStrategy(QualifiedType<T> type) {
      boolean hasByName = type.hasQualifier(EnumByName.class);
      boolean hasByOrdinal = type.hasQualifier(EnumByOrdinal.class);
      if (hasByName && hasByOrdinal) {
         throw new IllegalArgumentException(
            String.format("%s is both %s and %s", type.getType(), EnumByName.class.getSimpleName(), EnumByOrdinal.class.getSimpleName())
         );
      } else if (hasByName) {
         return Optional.of(EnumStrategy.BY_NAME);
      } else {
         return hasByOrdinal ? Optional.of(EnumStrategy.BY_ORDINAL) : Optional.empty();
      }
   }

   public EnumStrategies createCopy() {
      return new EnumStrategies();
   }
}
