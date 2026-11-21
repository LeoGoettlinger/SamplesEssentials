package org.jdbi.v3.core.mapper;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Function;
import org.jdbi.v3.core.array.SqlArrayMapperFactory;
import org.jdbi.v3.core.config.ConfigRegistry;
import org.jdbi.v3.core.config.JdbiConfig;
import org.jdbi.v3.core.enums.internal.EnumMapperFactory;
import org.jdbi.v3.core.generic.GenericType;
import org.jdbi.v3.core.interceptor.JdbiInterceptionChainHolder;
import org.jdbi.v3.core.internal.CopyOnWriteHashMap;
import org.jdbi.v3.core.qualifier.QualifiedType;
import org.jdbi.v3.meta.Alpha;

public class ColumnMappers implements JdbiConfig<ColumnMappers> {
   private final JdbiInterceptionChainHolder<ColumnMapper<?>, QualifiedColumnMapperFactory> inferenceInterceptors;
   private final List<QualifiedColumnMapperFactory> factories;
   private final Map<QualifiedType<?>, Optional<? extends ColumnMapper<?>>> cache;
   private boolean coalesceNullPrimitivesToDefaults = true;
   private ConfigRegistry registry;

   public ColumnMappers() {
      this.inferenceInterceptors = new JdbiInterceptionChainHolder<>(InferredColumnMapperFactory::new);
      this.factories = new CopyOnWriteArrayList<>();
      this.cache = new CopyOnWriteHashMap<>();
      this.register(new SqlArrayMapperFactory());
      this.register(new JavaTimeMapperFactory());
      this.register(new SqlTimeMapperFactory());
      this.register(new InternetMapperFactory());
      this.register(new EssentialsMapperFactory());
      this.register(new BoxedMapperFactory());
      this.register(new PrimitiveMapperFactory());
      this.register(new OptionalColumnMapperFactory());
      this.register(new EnumMapperFactory());
      this.register(new NVarcharMapper());
      this.register(new LegacyColumnMapperFactory());
   }

   private ColumnMappers(ColumnMappers that) {
      this.factories = new CopyOnWriteArrayList<>(that.factories);
      this.cache = new CopyOnWriteHashMap<>(that.cache);
      this.inferenceInterceptors = new JdbiInterceptionChainHolder<>(that.inferenceInterceptors);
      this.coalesceNullPrimitivesToDefaults = that.coalesceNullPrimitivesToDefaults;
   }

   @Override
   public void setRegistry(ConfigRegistry registry) {
      this.registry = registry;
   }

   @Alpha
   public JdbiInterceptionChainHolder<ColumnMapper<?>, QualifiedColumnMapperFactory> getInferenceInterceptors() {
      return this.inferenceInterceptors;
   }

   public ColumnMappers register(ColumnMapper<?> mapper) {
      QualifiedColumnMapperFactory factory = this.inferenceInterceptors.process(mapper);
      return this.register(factory);
   }

   public <T> ColumnMappers register(GenericType<T> type, ColumnMapper<T> mapper) {
      return this.register(ColumnMapperFactory.of(type.getType(), mapper));
   }

   public ColumnMappers register(Type type, ColumnMapper<?> mapper) {
      return this.register(ColumnMapperFactory.of(type, mapper));
   }

   public <T> ColumnMappers register(QualifiedType<T> type, ColumnMapper<T> mapper) {
      return this.register(QualifiedColumnMapperFactory.of(type, mapper));
   }

   public ColumnMappers register(ColumnMapperFactory factory) {
      return this.register(QualifiedColumnMapperFactory.adapt(factory));
   }

   public ColumnMappers register(QualifiedColumnMapperFactory factory) {
      this.factories.add(0, factory);
      this.cache.clear();
      return this;
   }

   public <T> Optional<ColumnMapper<T>> findFor(Class<T> type) {
      ColumnMapper<T> mapper = (ColumnMapper<T>)this.findFor(type).orElse(null);
      return Optional.ofNullable(mapper);
   }

   public <T> Optional<ColumnMapper<T>> findFor(GenericType<T> type) {
      ColumnMapper<T> mapper = (ColumnMapper<T>)this.findFor(type.getType()).orElse(null);
      return Optional.ofNullable(mapper);
   }

   public Optional<ColumnMapper<?>> findFor(Type type) {
      return this.findFor(QualifiedType.of(type)).map(Function.identity());
   }

   public <T> Optional<ColumnMapper<T>> findFor(QualifiedType<T> type) {
      Optional<ColumnMapper<T>> cached = (Optional<ColumnMapper<T>>)this.cache.get(type);
      if (cached != null) {
         return cached;
      } else {
         for (QualifiedColumnMapperFactory factory : this.factories) {
            Optional<ColumnMapper<T>> maybeMapper = (Optional<ColumnMapper<T>>)factory.build(type, this.registry);
            ColumnMapper<T> mapper = maybeMapper.orElse(null);
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

   public boolean getCoalesceNullPrimitivesToDefaults() {
      return this.coalesceNullPrimitivesToDefaults;
   }

   public void setCoalesceNullPrimitivesToDefaults(boolean coalesceNullPrimitivesToDefaults) {
      this.coalesceNullPrimitivesToDefaults = coalesceNullPrimitivesToDefaults;
   }

   public ColumnMappers createCopy() {
      return new ColumnMappers(this);
   }
}
