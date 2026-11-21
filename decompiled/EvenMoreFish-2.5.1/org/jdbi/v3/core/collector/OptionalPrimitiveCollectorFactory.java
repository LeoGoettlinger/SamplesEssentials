package org.jdbi.v3.core.collector;

import java.lang.reflect.Type;
import java.util.IdentityHashMap;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.OptionalLong;
import java.util.stream.Collector;

class OptionalPrimitiveCollectorFactory implements CollectorFactory {
   private static final IdentityHashMap<Class<?>, Collector<?, ?, ?>> COLLECTORS = new IdentityHashMap<>();
   private static final IdentityHashMap<Class<?>, Class<?>> ELEMENT_TYPES = new IdentityHashMap<>();

   @Override
   public boolean accepts(Type containerType) {
      return containerType instanceof Class && COLLECTORS.containsKey(containerType);
   }

   @Override
   public Optional<Type> elementType(Type containerType) {
      return containerType instanceof Class ? Optional.of(ELEMENT_TYPES.get(containerType)) : Optional.empty();
   }

   @Override
   public Collector<?, ?, ?> build(Type containerType) {
      return containerType instanceof Class ? COLLECTORS.get(containerType) : null;
   }

   static {
      COLLECTORS.put(OptionalInt.class, OptionalCollectors.toOptionalInt());
      ELEMENT_TYPES.put(OptionalInt.class, Integer.class);
      COLLECTORS.put(OptionalLong.class, OptionalCollectors.toOptionalLong());
      ELEMENT_TYPES.put(OptionalLong.class, Long.class);
      COLLECTORS.put(OptionalDouble.class, OptionalCollectors.toOptionalDouble());
      ELEMENT_TYPES.put(OptionalDouble.class, Double.class);
   }
}
