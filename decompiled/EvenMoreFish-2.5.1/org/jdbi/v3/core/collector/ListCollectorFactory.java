package org.jdbi.v3.core.collector;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.IdentityHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import org.jdbi.v3.core.generic.GenericTypes;

class ListCollectorFactory implements CollectorFactory {
   private final IdentityHashMap<Class<?>, Collector<?, ?, ?>> collectors = new IdentityHashMap<>();

   ListCollectorFactory() {
      this.collectors.put(Collection.class, Collectors.toCollection(ArrayList::new));
      this.collectors.put(List.class, Collectors.toList());
      this.collectors.put(ArrayList.class, Collectors.toCollection(ArrayList::new));
      this.collectors.put(LinkedList.class, Collectors.toCollection(LinkedList::new));
      this.collectors.put(CopyOnWriteArrayList.class, Collectors.toCollection(CopyOnWriteArrayList::new));
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
