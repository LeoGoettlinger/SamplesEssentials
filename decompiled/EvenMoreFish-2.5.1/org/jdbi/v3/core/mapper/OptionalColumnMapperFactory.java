package org.jdbi.v3.core.mapper;

import java.lang.reflect.Type;
import java.sql.ResultSet;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.OptionalLong;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import org.jdbi.v3.core.config.ConfigRegistry;
import org.jdbi.v3.core.generic.GenericTypes;

class OptionalColumnMapperFactory implements ColumnMapperFactory {
   private static final Map<Class<?>, BiFunction<Type, ConfigRegistry, ColumnMapper<?>>> STRATEGIES;

   @Override
   public Optional<ColumnMapper<?>> build(Type type, ConfigRegistry config) {
      return Optional.ofNullable(STRATEGIES.get(GenericTypes.getErasedType(type))).map(strategy -> strategy.apply(type, config));
   }

   static BiFunction<Type, ConfigRegistry, ColumnMapper<?>> singleton(ColumnMapper<?> instance) {
      return (t, c) -> instance;
   }

   static <Opt, Box> ColumnMapper<?> create(ColumnGetter<Box> columnGetter, Supplier<Opt> empty, Function<Box, Opt> present) {
      return (r, columnNumber, ctx) -> Optional.ofNullable(new GetterMapper<>(columnGetter).map(r, columnNumber, ctx)).map(present).orElseGet(empty);
   }

   private static ColumnMapper<?> create(Type type, ConfigRegistry config) {
      ColumnMapper<?> mapper = config.get(ColumnMappers.class)
         .findFor(GenericTypes.findGenericParameter(type, Optional.class).orElseThrow(() -> new NoSuchMapperException("No mapper for raw Optional type")))
         .orElseThrow(() -> new NoSuchMapperException("No column mapper for type " + type + ", nested in Optional"));
      return (r, i, ctx) -> Optional.ofNullable(mapper.map(r, i, ctx));
   }

   static {
      Map<Class<?>, BiFunction<Type, ConfigRegistry, ColumnMapper<?>>> s = new HashMap<>();
      s.put(Optional.class, OptionalColumnMapperFactory::create);
      s.put(OptionalInt.class, singleton(create(ResultSet::getInt, OptionalInt::empty, OptionalInt::of)));
      s.put(OptionalLong.class, singleton(create(ResultSet::getLong, OptionalLong::empty, OptionalLong::of)));
      s.put(OptionalDouble.class, singleton(create(ResultSet::getDouble, OptionalDouble::empty, OptionalDouble::of)));
      STRATEGIES = Collections.unmodifiableMap(s);
   }
}
