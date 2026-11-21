package org.jdbi.v3.core.collector;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collector;
import org.jdbi.v3.core.config.JdbiConfig;

public class JdbiCollectors implements JdbiConfig<JdbiCollectors> {
   private final List<CollectorFactory> factories;
   private ConcurrentMap<Type, Optional<CollectorFactory>> factoryCache;

   public JdbiCollectors() {
      this.factories = new CopyOnWriteArrayList<>();
      this.factoryCache = new ConcurrentHashMap<>();
      this.register(new MapCollectorFactory());
      this.register(new OptionalCollectorFactory());
      this.register(new ListCollectorFactory());
      this.register(new SetCollectorFactory());
      this.register(new OptionalPrimitiveCollectorFactory());
      this.register(new ArrayCollectorFactory());
      this.register(new EnumSetCollectorFactory());
   }

   private JdbiCollectors(JdbiCollectors that) {
      this.factoryCache = that.factoryCache;
      this.factories = new CopyOnWriteArrayList<>(that.factories);
   }

   public JdbiCollectors register(CollectorFactory factory) {
      this.factories.add(0, factory);
      this.factoryCache = new ConcurrentHashMap<>();
      return this;
   }

   public JdbiCollectors registerCollector(Type collectionType, Collector<?, ?, ?> collector) {
      return this.register(CollectorFactory.collectorFactory(collectionType, collector));
   }

   public Optional<Collector<?, ?, ?>> findFor(Type containerType) {
      return this.findFactoryFor(containerType).map(f -> f.build(containerType));
   }

   public Optional<Type> findElementTypeFor(Type containerType) {
      return this.findFactoryFor(containerType).flatMap(f -> f.elementType(containerType));
   }

   private Optional<CollectorFactory> findFactoryFor(Type containerType) {
      Optional<CollectorFactory> entry = this.factoryCache.get(containerType);
      if (entry != null) {
         return entry;
      } else {
         entry = this.factories.stream().filter(f -> f.accepts(containerType)).findFirst();
         this.factoryCache.putIfAbsent(containerType, entry);
         return entry;
      }
   }

   public JdbiCollectors createCopy() {
      return new JdbiCollectors(this);
   }
}
