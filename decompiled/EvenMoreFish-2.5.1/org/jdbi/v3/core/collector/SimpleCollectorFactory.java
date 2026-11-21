package org.jdbi.v3.core.collector;

import java.lang.reflect.Type;
import java.util.Optional;
import java.util.stream.Collector;
import org.jdbi.v3.core.generic.GenericTypes;

class SimpleCollectorFactory implements CollectorFactory {
   private final Type resultType;
   private final Collector<?, ?, ?> collector;

   SimpleCollectorFactory(Type resultType, Collector<?, ?, ?> collector) {
      this.resultType = resultType;
      this.collector = collector;
   }

   @Override
   public boolean accepts(Type containerType) {
      return GenericTypes.isSuperType(containerType, this.resultType);
   }

   @Override
   public Optional<Type> elementType(Type containerType) {
      Optional<Type> collectedType = GenericTypes.findGenericParameter(this.collector.getClass(), Collector.class);
      if (collectedType.isPresent()) {
         return collectedType;
      } else {
         return GenericTypes.isSuperType(Iterable.class, containerType) ? GenericTypes.findGenericParameter(containerType, Iterable.class) : Optional.empty();
      }
   }

   @Override
   public Collector<?, ?, ?> build(Type containerType) {
      return this.collector;
   }

   @Override
   public String toString() {
      return "CollectorFactory handling " + this.resultType + " with " + this.collector;
   }
}
