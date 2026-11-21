package org.jdbi.v3.core.argument;

import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.jdbi.v3.core.annotation.internal.JdbiAnnotations;
import org.jdbi.v3.core.argument.internal.ObjectPropertyNamedArgumentFinder;
import org.jdbi.v3.core.argument.internal.TypedValue;
import org.jdbi.v3.core.config.ConfigRegistry;
import org.jdbi.v3.core.config.internal.ConfigCache;
import org.jdbi.v3.core.config.internal.ConfigCaches;
import org.jdbi.v3.core.internal.exceptions.Unchecked;
import org.jdbi.v3.core.qualifier.QualifiedType;
import org.jdbi.v3.core.qualifier.Qualifiers;
import org.jdbi.v3.core.statement.StatementContext;

@Deprecated(
   since = "3.11.0"
)
public class ObjectFieldArguments extends ObjectPropertyNamedArgumentFinder {
   private static final ConfigCache<Class<?>, Map<String, Function<Object, TypedValue>>> FIELD_CACHE = ConfigCaches.declare(
      (config, beanClazz) -> Stream.of(beanClazz.getFields()).filter(JdbiAnnotations::isBound).collect(Collectors.toMap(Field::getName, f -> {
         QualifiedType<?> qualifiedType = QualifiedType.of(f.getGenericType()).withAnnotations(config.get(Qualifiers.class).findFor(f));
         Function<Object, Object> getter = Unchecked.function(Unchecked.function(MethodHandles.lookup()::unreflectGetter).apply(f)::invoke);
         return obj -> new TypedValue(qualifiedType, getter.apply(obj));
      }))
   );
   private final Class<?> beanClass;

   public ObjectFieldArguments(String prefix, Object bean) {
      super(prefix, bean);
      this.beanClass = bean.getClass();
   }

   public Optional<Function<Object, TypedValue>> getter(String name, ConfigRegistry config) {
      return Optional.ofNullable(FIELD_CACHE.get(this.beanClass, config).get(name));
   }

   @Override
   protected Optional<TypedValue> getValue(String name, StatementContext ctx) {
      return this.getter(name, ctx.getConfig()).map(getter -> getter.apply(this.obj));
   }

   @Override
   protected NamedArgumentFinder getNestedArgumentFinder(TypedValue value) {
      return new ObjectFieldArguments(null, value.getValue());
   }

   @Override
   public String toString() {
      return "{lazy bean field arguments \"" + this.obj + "\"}";
   }
}
