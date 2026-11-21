package org.jdbi.v3.core.collector;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Optional;
import java.util.stream.Collector;
import org.jdbi.v3.core.generic.GenericTypes;

class OptionalCollectorFactory implements CollectorFactory {
   @Override
   public boolean accepts(Type containerType) {
      return containerType instanceof ParameterizedType && Optional.class.equals(GenericTypes.getErasedType(containerType));
   }

   @Override
   public Optional<Type> elementType(Type containerType) {
      Class<?> erasedType = GenericTypes.getErasedType(containerType);
      return GenericTypes.findGenericParameter(containerType, erasedType);
   }

   @Override
   public Collector<?, ?, ?> build(Type containerType) {
      return OptionalCollectors.toOptional();
   }
}
