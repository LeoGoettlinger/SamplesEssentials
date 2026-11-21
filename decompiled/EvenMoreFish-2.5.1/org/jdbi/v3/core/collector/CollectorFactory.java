package org.jdbi.v3.core.collector;

import java.lang.reflect.Type;
import java.util.Optional;
import java.util.stream.Collector;
import org.jdbi.v3.core.generic.GenericType;

public interface CollectorFactory {
   boolean accepts(Type containerType);

   Optional<Type> elementType(Type containerType);

   Collector<?, ?, ?> build(Type containerType);

   static CollectorFactory collectorFactory(GenericType<?> containerType, Collector<?, ?, ?> collector) {
      return new SimpleCollectorFactory(containerType.getType(), collector);
   }

   static CollectorFactory collectorFactory(Type containerType, Collector<?, ?, ?> collector) {
      return new SimpleCollectorFactory(containerType, collector);
   }
}
