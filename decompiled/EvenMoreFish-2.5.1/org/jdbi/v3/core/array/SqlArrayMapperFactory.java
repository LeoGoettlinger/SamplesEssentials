package org.jdbi.v3.core.array;

import java.lang.reflect.Type;
import java.util.Optional;
import org.jdbi.v3.core.collector.JdbiCollectors;
import org.jdbi.v3.core.config.ConfigRegistry;
import org.jdbi.v3.core.generic.GenericTypes;
import org.jdbi.v3.core.mapper.ColumnMapper;
import org.jdbi.v3.core.mapper.ColumnMapperFactory;
import org.jdbi.v3.core.mapper.ColumnMappers;

public class SqlArrayMapperFactory implements ColumnMapperFactory {
   @Override
   public Optional<ColumnMapper<?>> build(Type type, ConfigRegistry config) {
      Class<?> erasedType = GenericTypes.getErasedType(type);
      if (erasedType.isArray()) {
         Class<?> elementType = erasedType.getComponentType();
         return this.elementTypeMapper(elementType, config).map(elementMapper -> new ArrayColumnMapper((ColumnMapper<?>)elementMapper, elementType));
      } else {
         JdbiCollectors collectorRegistry = config.get(JdbiCollectors.class);
         return collectorRegistry.findFor(type)
            .flatMap(
               collector -> collectorRegistry.findElementTypeFor(type)
                  .flatMap(elementType -> this.elementTypeMapper(elementType, config))
                  .map(elementMapper -> new CollectorColumnMapper<>((ColumnMapper<?>)elementMapper, collector))
            );
      }
   }

   private Optional<ColumnMapper<?>> elementTypeMapper(Type elementType, ConfigRegistry config) {
      Optional<ColumnMapper<?>> mapper = config.get(ColumnMappers.class).findFor(elementType);
      return !mapper.isPresent() && elementType == Object.class ? Optional.of((rs, num, context) -> rs.getObject(num)) : mapper;
   }
}
