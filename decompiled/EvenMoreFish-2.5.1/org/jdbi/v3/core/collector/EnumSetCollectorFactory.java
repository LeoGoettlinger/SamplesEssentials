package org.jdbi.v3.core.collector;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.AbstractCollection;
import java.util.EnumSet;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collector;
import org.jdbi.v3.core.generic.GenericTypes;

class EnumSetCollectorFactory implements CollectorFactory {
   @Override
   public boolean accepts(Type containerType) {
      return EnumSet.class.isAssignableFrom(GenericTypes.getErasedType(containerType)) && containerType instanceof ParameterizedType;
   }

   @Override
   public Optional<Type> elementType(Type containerType) {
      return GenericTypes.findGenericParameter(containerType, EnumSet.class);
   }

   @Override
   public Collector<?, ?, ?> build(Type containerType) {
      return this.build0(containerType);
   }

   private <E extends Enum<E>> Collector<E, ?, ?> build0(Type containerType) {
      Class<E> componentType = GenericTypes.findGenericParameter(containerType, EnumSet.class)
         .map(GenericTypes::getErasedType)
         .orElseThrow(() -> new IllegalStateException("Cannot determine EnumSet element type"));
      return Collector.of(() -> EnumSet.noneOf(componentType), AbstractCollection::add, (left, right) -> {
         left.addAll(right);
         return left;
      }, Function.identity());
   }
}
