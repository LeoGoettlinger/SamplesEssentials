package org.jdbi.v3.core.mapper.reflect.internal;

import java.lang.reflect.Type;
import java.util.Map;
import java.util.Optional;
import org.jdbi.v3.core.config.ConfigRegistry;
import org.jdbi.v3.core.config.JdbiConfig;
import org.jdbi.v3.core.generic.GenericTypes;
import org.jdbi.v3.core.internal.CopyOnWriteHashMap;

public class PojoTypes implements JdbiConfig<PojoTypes> {
   private final Map<Class<?>, PojoPropertiesFactory> factories;
   private ConfigRegistry registry;

   public PojoTypes() {
      this.factories = new CopyOnWriteHashMap<>();
   }

   private PojoTypes(PojoTypes other) {
      this.factories = new CopyOnWriteHashMap<>(other.factories);
   }

   @Override
   public void setRegistry(ConfigRegistry registry) {
      this.registry = registry;
   }

   public PojoTypes register(Class<?> key, PojoPropertiesFactory factory) {
      this.factories.put(key, factory);
      return this;
   }

   public Optional<PojoProperties<?>> findFor(Type type) {
      return Optional.ofNullable(this.factories.get(GenericTypes.getErasedType(type))).map(ppf -> ppf.create(type, this.registry));
   }

   public PojoTypes createCopy() {
      return new PojoTypes(this);
   }
}
