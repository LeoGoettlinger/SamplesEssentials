package org.jdbi.v3.core.mapper;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import org.jdbi.v3.core.config.ConfigRegistry;
import org.jdbi.v3.core.config.JdbiConfig;
import org.jdbi.v3.core.generic.GenericType;
import org.jdbi.v3.core.interceptor.JdbiInterceptionChainHolder;
import org.jdbi.v3.core.internal.CopyOnWriteHashMap;
import org.jdbi.v3.core.internal.PrefixedMapperKey;
import org.jdbi.v3.core.mapper.reflect.internal.PojoMapperFactory;
import org.jdbi.v3.meta.Alpha;

public class RowMappers implements JdbiConfig<RowMappers> {
   private final JdbiInterceptionChainHolder<RowMapper<?>, RowMapperFactory> inferenceInterceptors;
   private final List<RowMapperFactory> factories;
   private final Map<Type, Optional<RowMapper<?>>> cache;
   private final Map<PrefixedMapperKey, Optional<RowMapper<?>>> prefixedCache;
   private ConfigRegistry registry;

   public RowMappers() {
      this.inferenceInterceptors = new JdbiInterceptionChainHolder<>(InferredRowMapperFactory::new);
      this.factories = new CopyOnWriteArrayList<>();
      this.cache = new CopyOnWriteHashMap<>();
      this.prefixedCache = new CopyOnWriteHashMap<>();
      this.register(MapEntryMapper.factory());
      this.register(new PojoMapperFactory());
      this.register(new OptionalRowMapperFactory());
   }

   private RowMappers(RowMappers that) {
      this.factories = new CopyOnWriteArrayList<>(that.factories);
      this.cache = new CopyOnWriteHashMap<>(that.cache);
      this.prefixedCache = new CopyOnWriteHashMap<>(that.prefixedCache);
      this.inferenceInterceptors = new JdbiInterceptionChainHolder<>(that.inferenceInterceptors);
   }

   @Override
   public void setRegistry(ConfigRegistry registry) {
      this.registry = registry;
   }

   @Alpha
   public JdbiInterceptionChainHolder<RowMapper<?>, RowMapperFactory> getInferenceInterceptors() {
      return this.inferenceInterceptors;
   }

   public RowMappers register(RowMapper<?> mapper) {
      RowMapperFactory factory = this.inferenceInterceptors.process(mapper);
      return this.register(factory);
   }

   public <T> RowMappers register(GenericType<T> type, RowMapper<T> mapper) {
      return this.register(RowMapperFactory.of(type.getType(), mapper));
   }

   public RowMappers register(Type type, RowMapper<?> mapper) {
      return this.register(RowMapperFactory.of(type, mapper));
   }

   public RowMappers register(RowMapperFactory factory) {
      this.factories.add(0, factory);
      this.cache.clear();
      this.prefixedCache.clear();
      return this;
   }

   public <T> Optional<RowMapper<T>> findFor(Class<T> type) {
      RowMapper<T> mapper = (RowMapper<T>)this.findFor(type).orElse(null);
      return Optional.ofNullable(mapper);
   }

   public <T> Optional<RowMapper<T>> findFor(GenericType<T> type) {
      RowMapper<T> mapper = (RowMapper<T>)this.findFor(type.getType()).orElse(null);
      return Optional.ofNullable(mapper);
   }

   public Optional<RowMapper<?>> findFor(Type type) {
      Optional<RowMapper<?>> cached = this.cache.get(type);
      if (cached != null) {
         return cached;
      } else {
         for (RowMapperFactory factory : this.factories) {
            Optional<RowMapper<?>> maybeMapper = factory.build(type, this.registry);
            RowMapper<?> mapper = maybeMapper.orElse(null);
            if (mapper != null) {
               mapper.init(this.registry);
               this.cache.put(type, maybeMapper);
               return maybeMapper;
            }
         }

         this.cache.put(type, Optional.empty());
         return Optional.empty();
      }
   }

   @Alpha
   public Optional<RowMapper<?>> findFor(Type type, String prefix) {
      Objects.requireNonNull(prefix, "prefix; use findFor(Type) to look up a mapper by type alone");
      PrefixedMapperKey key = new PrefixedMapperKey(type, prefix);
      Optional<RowMapper<?>> cached = this.prefixedCache.get(key);
      if (cached != null) {
         return cached;
      } else {
         for (RowMapperFactory factory : this.factories) {
            Optional<RowMapper<?>> maybeMapper = factory.build(type, this.registry);
            RowMapper<?> mapper = maybeMapper.orElse(null);
            if (mapper != null && declaresPrefix(mapper, prefix)) {
               mapper.init(this.registry);
               this.prefixedCache.put(key, maybeMapper);
               return maybeMapper;
            }
         }

         this.prefixedCache.put(key, Optional.empty());
         return Optional.empty();
      }
   }

   private static boolean declaresPrefix(RowMapper<?> mapper, String prefix) {
      return mapper instanceof PrefixedRowMapper<?> prefixed ? prefix.equals(prefixed.getPrefix()) : prefix.isEmpty();
   }

   public RowMappers createCopy() {
      return new RowMappers(this);
   }
}
