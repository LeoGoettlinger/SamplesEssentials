package org.jdbi.v3.core.mapper;

import java.util.Optional;
import org.jdbi.v3.core.config.ConfigRegistry;
import org.jdbi.v3.core.qualifier.QualifiedType;
import org.jdbi.v3.core.qualifier.Qualifiers;

@FunctionalInterface
public interface QualifiedColumnMapperFactory {
   Optional<ColumnMapper<?>> build(QualifiedType<?> type, ConfigRegistry config);

   static QualifiedColumnMapperFactory adapt(ColumnMapperFactory factory) {
      return (type, config) -> type.hasQualifiers(config.get(Qualifiers.class).findFor(factory.getClass()))
         ? factory.build(type.getType(), config)
         : Optional.empty();
   }

   static <T> QualifiedColumnMapperFactory of(QualifiedType<T> type, ColumnMapper<T> mapper) {
      return (t, config) -> t.equals(type) ? Optional.of(mapper) : Optional.empty();
   }
}
