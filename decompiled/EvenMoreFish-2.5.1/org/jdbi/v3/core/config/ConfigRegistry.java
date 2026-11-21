package org.jdbi.v3.core.config;

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import org.jdbi.v3.core.argument.Arguments;
import org.jdbi.v3.core.collector.JdbiCollectors;
import org.jdbi.v3.core.config.internal.ConfigCaches;
import org.jdbi.v3.core.internal.JdbiClassUtils;
import org.jdbi.v3.core.mapper.ColumnMappers;
import org.jdbi.v3.core.mapper.Mappers;
import org.jdbi.v3.core.mapper.RowMappers;
import org.jdbi.v3.core.statement.SqlStatements;
import org.jdbi.v3.meta.Alpha;

public final class ConfigRegistry {
   private static final Class<?>[] JDBI_CONFIG_TYPES = new Class[]{ConfigRegistry.class};
   private final Map<Class<? extends JdbiConfig<?>>, JdbiConfig<?>> configs = new ConcurrentHashMap<>(32);
   private final Map<Class<? extends JdbiConfig<?>>, Function<ConfigRegistry, JdbiConfig<?>>> configFactories;
   private final ConfigRegistry source;
   private final AtomicBoolean eagerCopies = new AtomicBoolean();

   public ConfigRegistry() {
      this.source = null;
      this.configFactories = new ConcurrentHashMap<>();
      this.get(ConfigCaches.class);
      this.get(SqlStatements.class);
      this.get(Arguments.class);
      this.get(RowMappers.class);
      this.get(ColumnMappers.class);
      this.get(Mappers.class);
      this.get(JdbiCollectors.class);
   }

   private ConfigRegistry(ConfigRegistry that) {
      this.configFactories = that.configFactories;
      boolean eager = that.eagerCopies.get();
      this.eagerCopies.set(eager);
      if (eager) {
         this.source = null;

         for (Class<? extends JdbiConfig<?>> type : that.knownConfigTypes()) {
            this.configs.put(type, that.lookup(type).createCopy());
         }

         this.configs.values().forEach(c -> c.setRegistry(this));
      } else {
         this.source = that;
      }
   }

   public <C extends JdbiConfig<C>> C get(Class<C> configClass) {
      JdbiConfig<?> lookup = this.configs.get(configClass);
      if (lookup != null) {
         return configClass.cast(lookup);
      } else {
         C config;
         if (this.source != null) {
            config = (C)configClass.cast(this.source.<C>get(configClass).createCopy());
            config.setRegistry(this);
         } else {
            config = (C)configClass.cast(this.configFactory(configClass).apply(this));
         }

         return Optional.ofNullable(configClass.cast(this.configs.putIfAbsent(configClass, config))).orElse(config);
      }
   }

   private Function<ConfigRegistry, JdbiConfig<?>> configFactory(Class<? extends JdbiConfig<?>> configClass) {
      return this.configFactories
         .computeIfAbsent(
            configClass,
            klass -> {
               JdbiClassUtils.MethodHandleHolder<? extends JdbiConfig<?>> handleHolder = JdbiClassUtils.findConstructor(
                  (Class<? extends JdbiConfig<?>>)klass, JDBI_CONFIG_TYPES
               );
               return registry -> {
                  JdbiConfig<?> config = (JdbiConfig<?>)handleHolder.invoke(handle -> (Object)handle.invokeExact((ConfigRegistry)registry));
                  config.setRegistry(registry);
                  return config;
               };
            }
         );
   }

   private Set<Class<? extends JdbiConfig<?>>> knownConfigTypes() {
      Set<Class<? extends JdbiConfig<?>>> types = new LinkedHashSet<>();

      for (ConfigRegistry registry = this; registry != null; registry = registry.source) {
         types.addAll(registry.configs.keySet());
      }

      return types;
   }

   private JdbiConfig<?> lookup(Class<? extends JdbiConfig<?>> configClass) {
      for (ConfigRegistry registry = this; registry != null; registry = registry.source) {
         JdbiConfig<?> config = registry.configs.get(configClass);
         if (config != null) {
            return config;
         }
      }

      return null;
   }

   public ConfigRegistry createCopy() {
      return new ConfigRegistry(this);
   }

   @Alpha
   public void setEagerCopies(boolean eagerCopies) {
      this.eagerCopies.set(eagerCopies);
   }

   @Alpha
   public boolean isEagerCopies() {
      return this.eagerCopies.get();
   }
}
