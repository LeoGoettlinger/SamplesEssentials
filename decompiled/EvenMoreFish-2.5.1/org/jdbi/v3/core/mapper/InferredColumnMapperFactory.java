package org.jdbi.v3.core.mapper;

import java.util.Optional;
import org.jdbi.v3.core.config.ConfigRegistry;
import org.jdbi.v3.core.generic.GenericTypes;
import org.jdbi.v3.core.qualifier.QualifiedType;
import org.jdbi.v3.core.qualifier.Qualifiers;

class InferredColumnMapperFactory implements QualifiedColumnMapperFactory {
   private final QualifiedType<?> maps;
   private final ColumnMapper<?> mapper;

   InferredColumnMapperFactory(ColumnMapper<?> mapper) {
      this.maps = QualifiedType.of(
            GenericTypes.findGenericParameter(mapper.getClass(), ColumnMapper.class)
               .orElseThrow(() -> new UnsupportedOperationException("Must use a concretely typed ColumnMapper here"))
         )
         .withAnnotations(new Qualifiers().findFor(mapper.getClass()));
      this.mapper = mapper;
   }

   @Override
   public Optional<ColumnMapper<?>> build(QualifiedType<?> type, ConfigRegistry config) {
      return this.maps.equals(type) ? Optional.of(this.mapper) : Optional.empty();
   }
}
