package org.jdbi.v3.core.collector;

import java.lang.reflect.Type;
import java.util.Optional;
import java.util.stream.Collector;
import org.jdbi.v3.core.generic.GenericTypes;

class ArrayCollectorFactory implements CollectorFactory {
   @Override
   public boolean accepts(Type containerType) {
      return GenericTypes.getErasedType(containerType).isArray();
   }

   @Override
   public Optional<Type> elementType(Type containerType) {
      return Optional.ofNullable(GenericTypes.getErasedType(containerType).getComponentType());
   }

   @Override
   public Collector<?, ?, ?> build(Type containerType) {
      Class<?> componentType = GenericTypes.getErasedType(containerType).getComponentType();
      return Collector.of(() -> new ArrayBuilder(componentType), ArrayBuilder::add, (left, right) -> {
         left.list.addAll(right.list);
         return left;
      }, ArrayBuilder::build);
   }
}
