package org.jdbi.v3.core.collector;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import org.jdbi.v3.core.generic.GenericTypes;
import org.jdbi.v3.core.internal.CollectionCollectors;

class SetCollectorFactory implements CollectorFactory {
   private final IdentityHashMap<Class<?>, Collector<?, ?, ?>> collectors = new IdentityHashMap<>();

   SetCollectorFactory() {
      this.collectors.put(Set.class, CollectionCollectors.toUnmodifiableSet());
      this.collectors.put(HashSet.class, Collectors.toCollection(HashSet::new));
      this.collectors.put(LinkedHashSet.class, Collectors.toCollection(LinkedHashSet::new));
      this.collectors.put(SortedSet.class, Collectors.toCollection(TreeSet::new));
      this.collectors.put(TreeSet.class, Collectors.toCollection(TreeSet::new));
   }

   @Override
   public boolean accepts(Type containerType) {
      return containerType instanceof ParameterizedType && this.collectors.containsKey(GenericTypes.getErasedType(containerType));
   }

   @Override
   public Optional<Type> elementType(Type containerType) {
      Class<?> erasedType = GenericTypes.getErasedType(containerType);
      return GenericTypes.findGenericParameter(containerType, erasedType);
   }

   @Override
   public Collector<?, ?, ?> build(Type containerType) {
      Class<?> erasedType = GenericTypes.getErasedType(containerType);
      return this.collectors.get(erasedType);
   }
}
