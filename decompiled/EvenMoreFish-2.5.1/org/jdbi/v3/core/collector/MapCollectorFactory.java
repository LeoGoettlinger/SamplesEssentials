package org.jdbi.v3.core.collector;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collector;
import org.jdbi.v3.core.generic.GenericTypes;

class MapCollectorFactory implements CollectorFactory {
   private final IdentityHashMap<Class<?>, Collector<?, ?, ?>> collectors = new IdentityHashMap<>();

   MapCollectorFactory() {
      this.collectors.put(Map.class, MapCollectors.toMap(LinkedHashMap::new));
      this.collectors.put(HashMap.class, MapCollectors.toMap(HashMap::new));
      this.collectors.put(LinkedHashMap.class, MapCollectors.toMap(LinkedHashMap::new));
      this.collectors.put(SortedMap.class, MapCollectors.toMap(TreeMap::new));
      this.collectors.put(TreeMap.class, MapCollectors.toMap(TreeMap::new));
      this.collectors.put(ConcurrentMap.class, MapCollectors.toMap(ConcurrentHashMap::new));
      this.collectors.put(ConcurrentHashMap.class, MapCollectors.toMap(ConcurrentHashMap::new));
      this.collectors.put(WeakHashMap.class, MapCollectors.toMap(WeakHashMap::new));
   }

   @Override
   public boolean accepts(Type containerType) {
      Class<?> erasedType = GenericTypes.getErasedType(containerType);
      return containerType instanceof ParameterizedType && this.collectors.containsKey(erasedType);
   }

   @Override
   public Optional<Type> elementType(Type containerType) {
      Class<?> erasedType = GenericTypes.getErasedType(containerType);
      return Map.class.isAssignableFrom(erasedType) ? Optional.of(GenericTypes.resolveMapEntryType(containerType)) : Optional.empty();
   }

   @Override
   public Collector<?, ?, ?> build(Type containerType) {
      Class<?> erasedType = GenericTypes.getErasedType(containerType);
      return this.collectors.get(erasedType);
   }
}
