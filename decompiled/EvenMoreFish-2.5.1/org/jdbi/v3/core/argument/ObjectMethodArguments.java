package org.jdbi.v3.core.argument;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.jdbi.v3.core.annotation.internal.JdbiAnnotations;
import org.jdbi.v3.core.argument.internal.ObjectPropertyNamedArgumentFinder;
import org.jdbi.v3.core.argument.internal.TypedValue;
import org.jdbi.v3.core.config.ConfigRegistry;
import org.jdbi.v3.core.config.internal.ConfigCache;
import org.jdbi.v3.core.config.internal.ConfigCaches;
import org.jdbi.v3.core.internal.JdbiClassUtils;
import org.jdbi.v3.core.internal.exceptions.Unchecked;
import org.jdbi.v3.core.qualifier.QualifiedType;
import org.jdbi.v3.core.qualifier.Qualifiers;
import org.jdbi.v3.core.statement.StatementContext;

@Deprecated(
   since = "3.11.0"
)
public class ObjectMethodArguments extends ObjectPropertyNamedArgumentFinder {
   private static final ConfigCache<Class<?>, Map<String, Function<Object, TypedValue>>> NULLARY_METHOD_CACHE = ConfigCaches.declare(
      ObjectMethodArguments::load
   );

   public ObjectMethodArguments(String prefix, Object object) {
      super(prefix, object);
   }

   private static Map<String, Function<Object, TypedValue>> load(ConfigRegistry config, Class<?> type) {
      HashMap<String, Function<Object, TypedValue>> methodMap = new HashMap<>();
      if (Modifier.isPublic(type.getModifiers())) {
         Arrays.stream(type.getMethods())
            .filter(m -> m.getParameterCount() == 0)
            .filter(JdbiAnnotations::isBound)
            .collect(Collectors.toMap(Method::getName, Function.identity(), ObjectMethodArguments::bridgeMethodMerge))
            .forEach((name, method) -> {
               QualifiedType<?> qualifiedType = QualifiedType.of(method.getGenericReturnType()).withAnnotations(config.get(Qualifiers.class).findFor(method));
               MethodHandle mh = JdbiClassUtils.unreflect(config, method);
               methodMap.put(name, Unchecked.function(value -> new TypedValue(qualifiedType, (Object)mh.invoke((Object)value))));
            });
      } else {
         Optional.ofNullable(type.getSuperclass()).ifPresent(superclass -> methodMap.putAll(load(config, (Class<?>)superclass)));
         Arrays.stream(type.getInterfaces()).forEach(interfaceClass -> methodMap.putAll(load(config, (Class<?>)interfaceClass)));
      }

      return methodMap;
   }

   @Override
   protected Optional<TypedValue> getValue(String name, StatementContext ctx) {
      return this.getter(name, ctx.getConfig()).map(m -> m.apply(this.obj));
   }

   public Optional<Function<Object, TypedValue>> getter(String name, ConfigRegistry config) {
      return Optional.ofNullable(NULLARY_METHOD_CACHE.get(this.obj.getClass(), config).get(name));
   }

   @Override
   protected NamedArgumentFinder getNestedArgumentFinder(TypedValue value) {
      return new ObjectMethodArguments(null, value.getValue());
   }

   @Override
   public String toString() {
      return "{lazy object functions arguments \"" + this.obj + "\"";
   }

   private static Method bridgeMethodMerge(Method a, Method b) {
      return a.isBridge() ? b : a;
   }
}
